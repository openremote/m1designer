package org.openremote.beta.server.catalog;

import org.openremote.beta.shared.flow.Node;

import java.util.List;

public abstract class WidgetNodeDescriptor extends ClientNodeDescriptor {

    @Override
    public Node initialize(Node node) {
        node = super.initialize(node);
        node.setClientWidget(true);
        return node;
    }


    @Override
    public void addEditorComponents(List<String> editorComponents) {
        super.addEditorComponents(editorComponents);
        editorComponents.add("or-editor-node-widget");
    }

    @Override
    protected WidgetProperties getInitialProperties() {
        return new WidgetProperties(getWidgetComponent(), 0, 0);
    }
    protected abstract String getWidgetComponent();

}
