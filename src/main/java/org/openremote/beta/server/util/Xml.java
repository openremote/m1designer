package org.openremote.beta.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for easy XML processing:
 * <ul>
 *     <li>1. Parsing, Validating: Create a new instance with or without schema sources,
 *     call {@link #parse(File)}, {@link #validate(Document)}, etc.</li>
 *     <li>
 *         2. Printing, XPath evaluation, other utility functions are static methods.
 *     </li>
 * </ul>
 * <p>
 *     The {@link EntityResolver} for parsing and validating understands <code>file://</code>
 *     and <code>classpath://</code> prefixes of system identifiers, but it will not attempt to retrieve
 *     data from any HTTP URLs. Parsed documents are always normalized.
 * </p>
 * <h2>Example: Execute XPath query</h2>
 * <pre>{@code
 *     XPath xpath = Xml.createXPath(new NamespaceContextMap(
 *          new Mapping("soapenv", URI_NS_SOAP_1_1_ENVELOPE)
 *     ));
 *     Element envelope = Xml.getXPathResultElement(xpath, document, "//soapenv:Envelope");
 * }</pre>
 */
public class Xml implements ErrorHandler, EntityResolver {

    public static class ParserException extends Exception {

        public ParserException() {
        }

        public ParserException(String s) {
            super(s);
        }

        public ParserException(String s, Throwable throwable) {
            super(s, throwable);
        }

        public ParserException(Throwable throwable) {
            super(throwable);
        }

        public ParserException(SAXParseException ex) {
            super("(Line/Column: " + ex.getLineNumber() + ":" + ex.getColumnNumber() + ") " + ex.getMessage());
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(Xml.class);

    public static final URI XML_SCHEMA_NAMESPACE = URI.create("http://www.w3.org/2001/xml.xsd");
    public static final URL XML_SCHEMA_RESOURCE = Xml.class.getClassLoader().getResource("xml.xsd");

    final protected Source[] schemaSources;
    protected Schema schema;

    public Xml() {
        this(new Source[0]);
    }

    public Xml(Source[] schemaSources) {
        this.schemaSources = schemaSources;
    }

    /**
     * Adds all schemas declared in this (WSDL?) document as <code>DOMSource</code> schema sources.
     */
    public Xml(Document wsdl) {
        // http://stackoverflow.com/questions/8979044/validating-soap-message-against-wsdl-with-multiple-xsds
        final NodeList schemaNodes = wsdl.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "schema");
        final int nrSchemas = schemaNodes.getLength();
        this.schemaSources = new DOMSource[nrSchemas];
        for (int i = 0; i < nrSchemas; i++) {
            this.schemaSources[i] = new DOMSource(schemaNodes.item(i));
        }
    }

    public Schema getSchema() {

        if (schema == null) {
            // Lazy initialization
            // TODO: http://stackoverflow.com/questions/3129934/schemafactory-doesnt-support-w3c-xml-schema-in-platform-level-8
            try {
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

                schemaFactory.setResourceResolver(new CatalogResourceResolver(
                        new HashMap<URI, URL>() {{
                            put(XML_SCHEMA_NAMESPACE, XML_SCHEMA_RESOURCE);
                        }}
                ));

                if (schemaSources != null && schemaSources.length > 0) {
                    schema = schemaFactory.newSchema(schemaSources);
                }
                else {
                    schema = schemaFactory.newSchema();
                }

            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return schema;
    }

    // =================================================================================================

    public DocumentBuilderFactory createFactory(boolean validating) throws ParserException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {

            // Yes, namespaces would be nice... because they make XML sooooo eXtensible!
            factory.setNamespaceAware(true);

            if (validating) {

                // Well this in theory works without validation but requires namespaces. We don't want
                // namespaces when we are not validating because we get funny xmlns="" in output.
                factory.setXIncludeAware(true);

                // Whatever that does... we want it
                factory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
                factory.setFeature("http://apache.org/xml/features/xinclude/fixup-language", false);

                // Good idea to set a schema when you want to validate! Tell me, how does it work
                // without a schema?!
                factory.setSchema(getSchema());

                // Oh, it's dynamic! Soooo dynamic! This is hilarious:
                //
                // "The parser will validate the document only if a grammar is specified."
                //
                // What, you are surprised that it won't validate without a grammar?!?! Well,
                // I'm going to turn that smart feature on, of course! Seriously, it won't
                // validate without this switch. And I'm so proud having 'apache.org' in my
                // source, a true sign of quality.
                //
                factory.setFeature("http://apache.org/xml/features/validation/dynamic", true);
            }

        }
        catch (ParserConfigurationException ex) {
            // Lovely, of course it couldn't have been a RuntimeException!
            throw new ParserException(ex);
        }
        return factory;
    }

    public static Transformer createTransformer(String method, int indent, boolean standalone) throws ParserException {
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();

            if (indent > 0) {
                try {
                    transFactory.setAttribute("indent-number", indent);
                }
                catch (IllegalArgumentException ex) {
                    // Fuck you, Apache morons.
                }
            }

            Transformer transformer = transFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, standalone ? "no" : "yes");

            // JDK 7 bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=7150637
            if (standalone) {
                try {
                    transformer.setOutputProperty("http://www.oracle.com/xml/is-standalone", "yes");
                }
                catch (IllegalArgumentException e) {
                    // Expected on older versions
                }
            }

            transformer.setOutputProperty(OutputKeys.INDENT, indent > 0 ? "yes" : "no");
            if (indent > 0)
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
            transformer.setOutputProperty(OutputKeys.METHOD, method);

            return transformer;
        }
        catch (Exception ex) {
            throw new ParserException(ex);
        }
    }

    public Document createDocument() {
        try {
            return createFactory(false).newDocumentBuilder().newDocument();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // =================================================================================================

    public Document parse(URL url) throws ParserException {
        return parse(url, true);
    }

    public Document parse(String string) throws ParserException {
        return parse(string, true);
    }

    public Document parse(File file) throws ParserException {
        return parse(file, true);
    }

    public Document parse(InputStream stream) throws ParserException {
        return parse(stream, true);
    }

    public Document parse(URL url, boolean validate) throws ParserException {
        if (url == null)
            throw new IllegalArgumentException("Can't parse null URL");
        try {
            return parse(url.openStream(), validate);
        }
        catch (Exception ex) {
            throw new ParserException("Parsing URL failed: " + url, ex);
        }
    }

    public Document parse(String string, boolean validate) throws ParserException {
        if (string == null)
            throw new IllegalArgumentException("Can't parse null string");
        return parse(new InputSource(new StringReader(string)), validate);
    }

    public Document parse(File file, boolean validate) throws ParserException {
        if (file == null)
            throw new IllegalArgumentException("Can't parse null file");
        try {
            return parse(file.toURI().toURL(), validate);
        }
        catch (Exception ex) {
            throw new ParserException("Parsing file failed: " + file, ex);
        }
    }

    public Document parse(InputStream stream, boolean validate) throws ParserException {
        return parse(new InputSource(stream), validate);
    }

    public Document parse(InputSource source, boolean validate) throws ParserException {
        try {

            DocumentBuilder parser = createFactory(validate).newDocumentBuilder();

            parser.setEntityResolver(this);

            parser.setErrorHandler(this);

            Document dom = parser.parse(source);

            dom.normalizeDocument();

            return dom;

        }
        catch (Exception ex) {
            throw unwrapException(ex);
        }
    }

    // =================================================================================================

    public void validate(URL url) throws ParserException {
        if (url == null)
            throw new IllegalArgumentException("Can't validate null URL");
        LOG.trace("Validating XML of URL: " + url);
        validate(new StreamSource(url.toString()));
    }

    public void validate(String string) throws ParserException {
        if (string == null)
            throw new IllegalArgumentException("Can't validate null string");
        LOG.trace("Validating XML string characters: " + string.length());
        validate(new SAXSource(new InputSource(new StringReader(string))));
    }

    public void validate(Document document) throws ParserException {
        validate(new DOMSource(document));
    }

    public void validate(Source source) throws ParserException {
        try {
            Validator validator = getSchema().newValidator();
            validator.setErrorHandler(this);
            validator.validate(source);
        }
        catch (Exception ex) {
            throw unwrapException(ex);
        }
    }

    // =================================================================================================

    public static XPathFactory createXPathFactory() {
        return XPathFactory.newInstance();
    }

    public static XPath createXPath(NamespaceContext nsContext) {
        XPath xpath = createXPathFactory().newXPath();
        xpath.setNamespaceContext(nsContext);
        return xpath;
    }

    public static XPath createXPath(XPathFactory factory, NamespaceContext nsContext) {
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(nsContext);
        return xpath;
    }

    public static Object getXPathResult(XPath xpath, Node context, String expr) {
        return getXPathResult(xpath, context, expr, XPathConstants.NODESET);
    }

    public static Object getXPathResult(XPath xpath, Node context, String expr, QName returnType) {
        try {
            LOG.trace("Evaluating xpath query: " + expr);
            return xpath.evaluate(expr, context, returnType);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Element[] getXPathResultElements(XPath xpath, Node context, String expr) {
        return narrowNodeSet(
                getXPathResult(xpath, context, expr, XPathConstants.NODESET)
        );
    }

    public static Element getXPathResultElement(XPath xpath, Node context, String expr) {
        return (Element)getXPathResult(xpath, context, expr, XPathConstants.NODE);
    }

    // =================================================================================================

    public static String print(Document document) throws ParserException {
        return print(document, 4, true);
    }

    public static String print(Document document, int indent) throws ParserException {
        return print(document, indent, false);
    }

    public static String print(Document document, int indent, boolean standalone) throws ParserException {
        return print(document, indent, standalone, false);
    }

    public static String print(Document document, int indent, boolean standalone, boolean stripWhitespaceNodes) throws ParserException {
        if (stripWhitespaceNodes)
            document = stripWhitespaceOnlyNodes(document);
        return print(new DOMSource(document.getDocumentElement()), indent, standalone);
    }

    public static String print(Node node, int indent) throws ParserException {
        return print(node, indent, false);
    }

    public static String print(Node node, int indent, boolean stripWhitespaceNodes) throws ParserException {
        if (stripWhitespaceNodes) {
            node = node.cloneNode(true);
            stripWhitespaceOnlyChildren(node);
        }
        return print(new DOMSource(node), indent, false);
    }

    public static String print(String string, int indent, boolean standalone) throws ParserException {
        return print(new StreamSource(new StringReader(string)), indent, standalone);
    }

    public static String print(Source source, int indent, boolean standalone) throws ParserException {
        try {
            Transformer transformer = createTransformer("xml", indent, standalone);
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");

            StringWriter out = new StringWriter();
            transformer.transform(source, new StreamResult(out));
            out.flush();

            return out.toString();

        }
        catch (Exception e) {
            throw new ParserException(e);
        }
    }

    /**
     * This prints XHTML into HTML that IE understands, and that validates with W3C rules.
     */
    public static String printHTML(Document dom) throws ParserException {
        return printHTML(dom, 4, true, true);
    }


    /**
     * This prints XHTML into HTML that IE understands, and that validates with W3C rules.
     */
    public static String printHTML(Document dom, int indent, boolean standalone, boolean doctype) throws ParserException {

        // Make a copy so we can remove stuff from the DOM that violates W3C when rendered as HTML (go figure!)
        dom = (Document) dom.cloneNode(true);

        // CDATA will be escaped by the transformer for HTML output but we
        // need to copy it into text nodes (yes, I know XML is fantastic...)
        accept(dom.getDocumentElement(), new NodeVisitor(Node.CDATA_SECTION_NODE) {
            @Override
            public void visit(Node node) {
                CDATASection cdata = (CDATASection) node;
                cdata.getParentNode().setTextContent(cdata.getData());
            }
        });

        stripWhitespaceOnlyChildren(dom.getDocumentElement());

        try {
            Transformer transformer = createTransformer("html", indent, standalone);

            if (doctype) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD HTML 4.01 Transitional//EN");
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/html4/loose.dtd");
            }

            StringWriter out = new StringWriter();
            transformer.transform(new DOMSource(dom), new StreamResult(out));
            out.flush();
            String output = out.toString();

            // Rip out the idiotic META http-equiv tag - we have HTTP headers for that!
            String meta = "\\s*<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">";
            output = output.replaceFirst(meta, "");

            // Rip out the even dumber xmlns attribute that magically got added (seems to be a difference between JDK 1.4 and 5)
            String xmlns = "<html xmlns=\"http://www.w3.org/1999/xhtml\">";
            output = output.replaceFirst(xmlns, "<html>");

            return output;

        }
        catch (Exception ex) {
            throw new ParserException(ex);
        }
    }

    // =================================================================================================

    public void warning(SAXParseException e) throws SAXException {
        LOG.warn(e.toString());
    }

    public void error(SAXParseException e) throws SAXException {
        throw new SAXException(new ParserException(e));
    }

    public void fatalError(SAXParseException e) throws SAXException {
        throw new SAXException(new ParserException(e));
    }

    protected static ParserException unwrapException(Exception ex) {
        // Another historic moment in Java XML API design!
        if (ex.getCause() != null && ex.getCause() instanceof ParserException) {
            return (ParserException) ex.getCause();
        }
        return new ParserException(ex);
    }

    // =================================================================================================

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        // By default this builds an EntityResolver that _stays offline_.
        // Damn you XML clowns, just because a URI looks like a URL does NOT mean you should fetch it!
        InputSource is;
        if (systemId.startsWith("file://")) {
            is = new InputSource(new FileInputStream(new File(URI.create(systemId))));
        } else if (systemId.startsWith("classpath://")) {
                is = new InputSource(Xml.class.getResourceAsStream(systemId.substring("classpath://".length())));
            }
        else {
            is = new InputSource(new ByteArrayInputStream(new byte[0]));
        }
        is.setPublicId(publicId);
        is.setSystemId(systemId);
        return is;

    }

    // ======================================= Utility Methods =============================================

    public static Element[] narrowNodeSet(Object nodeSet) {
        List<Element> elements = new ArrayList<>();
        if (nodeSet != null && nodeSet instanceof NodeList) {
            NodeList nodes = (NodeList) nodeSet;
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    throw new IllegalStateException("Node is not an element but type: " + node.getNodeType());
                elements.add((Element) (node));
            }
            return elements.toArray(new Element[elements.size()]);
        }
        return null;
    }

    public static String escape(String string) {
        return escape(string, false, false);
    }

    /**
     * Replaces reserved HTML characters with HTML entities.
     *
     * @param convertNewlines Convert all newlines into HTML line breaks.
     * @param convertSpaces Convert all spaces at start of a line into non-breaking spaces.
     */
    public static String escape(String string, boolean convertNewlines, boolean convertSpaces) {
        if (string == null)
            return null;
        StringBuilder sb = new StringBuilder();
        String entity;
        char c;
        for (int i = 0; i < string.length(); ++i) {
            entity = null;
            c = string.charAt(i);
            switch (c) {
            case '<':
                entity = "&#60;";
                break;
            case '>':
                entity = "&#62;";
                break;
            case '&':
                entity = "&#38;";
                break;
            case '"':
                entity = "&#34;";
                break;
            }
            if (entity != null) {
                sb.append(entity);
            }
            else {
                sb.append(c);
            }
        }
        String result = sb.toString();
        if (convertSpaces) {
            // Converts the _beginning_ of line whitespaces into non-breaking spaces
            Matcher matcher = Pattern.compile("(\\n+)(\\s*)(.*)").matcher(result);
            StringBuffer temp = new StringBuffer();
            while (matcher.find()) {
                String group = matcher.group(2);
                StringBuilder spaces = new StringBuilder();
                for (int i = 0; i < group.length(); i++) {
                    spaces.append("&#160;");
                }
                matcher.appendReplacement(temp, "$1" + spaces.toString() + "$3");
            }
            matcher.appendTail(temp);
            result = temp.toString();
        }
        if (convertNewlines) {
            result = result.replaceAll("\n", "<br/>");
        }
        return result;
    }

    /**
     * Tries to remove XML elements with a crude regex.
     */
    public static String stripElements(String xml) {
        if (xml == null)
            return null;
        return xml.replaceAll("<([a-zA-Z]|/).*?>", "");
    }

    /**
     * Recursively remove any TEXT_NODE that is blank.
     */
    public static Document stripWhitespaceOnlyNodes(Document doc) {
        final Document copy = (Document) doc.cloneNode(true);
        stripWhitespaceOnlyChildren(copy);
        return copy;
    }

    /**
     * Recursively remove any TEXT_NODE that is blank.
     */
    public static void stripWhitespaceOnlyChildren(Node n) {
        final NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            final Node child = nl.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                stripWhitespaceOnlyChildren(child);
            }
            else if (child.getNodeType() == Node.TEXT_NODE) {
                final String value = child.getNodeValue();
                if (value == null || value.length() == 0) {
                    n.removeChild(child);
                    --i;
                }
            }
        }
    }

    /**
     * Serializes the document to string, then makes the string available as a stream of UTF8 bytes.
     */
    public static InputStream toStream(Document document) throws ParserException {
        try {
            return new ByteArrayInputStream(new Xml().print(document, 0).getBytes("utf-8"));
        } catch (Exception ex) {
            throw unwrapException(ex);
        }
    }

    /**
     * Set the document as content of a {@link SOAPMessage}, hence, this must
     * be a document with SOAP envelope and body.
     */
    public static SOAPMessage toSOAPMessage(Document message) throws ParserException {
        try {
            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            SOAPPart soapPart = soapMessage.getSOAPPart();
            soapPart.setContent(new DOMSource(message));
            soapMessage.saveChanges();
            return soapMessage;
        } catch (Exception ex) {
            throw unwrapException(ex);
        }
    }

    /**
     * Clone and wrap the document in a SOAP body and envelope.
     */
    public static SOAPMessage wrapInSOAPMessage(final Document document) throws ParserException {
        try {
            final SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();

            // Read the JavaDoc: if using #addDocument the behavior of an application that continues to use such references is undefined.
            soapMessage.getSOAPBody().addDocument((Document) document.cloneNode(true));
            return soapMessage;

        } catch (Exception ex) {
            throw unwrapException(ex);
        }
    }

    public static Source getContent(SOAPMessage soapMessage) throws ParserException {
        try {
            return soapMessage.getSOAPPart().getContent();
        } catch (Exception ex) {
            throw unwrapException(ex);
        }
    }

    /**
     * Visitor pattern implementation.
     *
     * <h2>Example:</h2>
     * <pre>{@code
     * NodeVisitor myVisitor = new NodeVisitor(Node.TEXT_NODE) {
     *     @Override
     *     public void visit(Node node) {
     *         if (node.getTextContent().equals("123"))
     *             System.out.println("Found in " + node.getParentNode().getLocalName());
     *     }
     * };
     *
     * accept(document.getDocumentElement(), myVisitor);
     * }</pre>
     */
    public static void accept(Node node, NodeVisitor visitor) {
        if (node == null)
            return;
        if (visitor.isHalted())
            return;
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            boolean cont = true;
            if (child.getNodeType() == visitor.nodeType) {
                visitor.visit(child);
                if (visitor.isHalted())
                    break;
            }
            accept(child, visitor);
        }
    }

    public static abstract class NodeVisitor {
        private short nodeType;

        protected NodeVisitor(short nodeType) {
            assert nodeType < Node.NOTATION_NODE; // All other node types are below
            this.nodeType = nodeType;
        }

        public boolean isHalted() {
            return false;
        }

        public abstract void visit(Node node);

    }

    /**
     * Wrap a string in an XML element and return the wrapped string.
     */
    public static String wrap(String wrapperName, String fragment) {
        return wrap(wrapperName, null, fragment);
    }

    /**
     * Wrap a string in an XML element and return the wrapped string.
     */
    public static String wrap(String wrapperName, String xmlns, String fragment) {
        StringBuilder wrapper = new StringBuilder();
        wrapper.append("<").append(wrapperName);
        if (xmlns != null) {
            wrapper.append(" xmlns=\"").append(xmlns).append("\"");
        }
        wrapper.append(">");
        wrapper.append(fragment);
        wrapper.append("</").append(wrapperName).append(">");
        return wrapper.toString();
    }

}