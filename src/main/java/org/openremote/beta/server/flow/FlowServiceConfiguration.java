package org.openremote.beta.server.flow;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.server.WebserverConfiguration.RestRouteBuilder;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.shared.flow.Flow;
import org.openremote.beta.shared.flow.Node;
import org.openremote.beta.shared.flow.Slot;
import org.openremote.beta.shared.flow.Wire;
import org.openremote.beta.shared.model.Identifier;

import java.util.Map;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;
import static org.openremote.beta.server.util.IdentifierUtil.generateGlobalUniqueId;
import static org.openremote.beta.shared.util.Util.createMap;

public class FlowServiceConfiguration implements Configuration {

    static Flow[] flows = new Flow[3];

    static {

        String SENSOR_COLOR = "blue";
        String ACTUATOR_COLOR = "darkblue";
        String VIRTUAL_COLOR = "darkturquoise";
        String PROCESSOR_COLOR = "sandybrown";
        String UI_WIDGET_COLOR = "violet";

        {
        /* ###################################################################################### */

            Slot temperatureSensorSource = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node temperatureSensor = new Node(
                "Livingroom Temperature",
                new Identifier(generateGlobalUniqueId(), "ZWave Sensor"),
                temperatureSensorSource
            );
            Map<String, Object> properties = createMap();
            Map<String, Object> editor = createMap(properties, "editor");
            editor.put("x", 50);
            editor.put("y", 50);
            editor.put("color", SENSOR_COLOR);
            temperatureSensor.setProperties(properties);

            Slot setpointSensorSource = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node setpointSensor = new Node(
                "Livingroom Setpoint",
                new Identifier(generateGlobalUniqueId(), "ZWave Sensor"),
                setpointSensorSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("x", 50);
            editor.put("y", 150);
            editor.put("color", SENSOR_COLOR);
            setpointSensor.setProperties(properties);

            Slot temperatureControlCurrentSink = new Slot("Current", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Slot temperatureControlSetpointSink = new Slot("Setpoint", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Slot temperatureControlSetpointSource = new Slot("Setpoint", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node temperatureControl = new Node(
                "Thermostat Control",
                new Identifier(generateGlobalUniqueId(), "Flow"),
                temperatureControlCurrentSink,
                temperatureControlSetpointSink,
                temperatureControlSetpointSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", VIRTUAL_COLOR);
            editor.put("x", 400);
            editor.put("y", 100);
            temperatureControl.setProperties(properties);

            Slot setpointActuatorSink = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Node setpointActuator = new Node(
                "Livingroom Setpoint",
                new Identifier(generateGlobalUniqueId(), "ZWave Actuator"),
                setpointActuatorSink
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", ACTUATOR_COLOR);
            editor.put("x", 800);
            editor.put("y", 150);
            setpointActuator.setProperties(properties);


            Node[] nodes = new Node[4];
            nodes[0] = temperatureSensor;
            nodes[1] = setpointSensor;
            nodes[2] = temperatureControl;
            nodes[3] = setpointActuator;

            flows[0] = new Flow(
                "Livingroom Environment",
                new Identifier(generateGlobalUniqueId(), Flow.TYPE),
                nodes,
                new Wire[]{
                    new Wire(temperatureSensorSource, temperatureControlCurrentSink),
                    new Wire(setpointSensorSource, temperatureControlSetpointSink),
                    new Wire(temperatureControlSetpointSource, setpointActuatorSink)
                }
            );

        /* ###################################################################################### */
        }
        flows[1] = SampleTemperatureProcessor.FLOW;
        {
        /* ###################################################################################### */

            Slot temperatureConsumerSource = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node temperatureConsumer = new Node(
                "Current",
                new Identifier(generateGlobalUniqueId(), Node.TYPE_CONSUMER),
                temperatureConsumerSource
            );
            Map<String, Object> properties = createMap();
            Map<String, Object> editor = createMap(properties, "editor");
            editor.put("color", VIRTUAL_COLOR);
            editor.put("x", 20);
            editor.put("y", 20);
            temperatureConsumer.setProperties(properties);

            Slot setpointConsumerSource = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node setpointConsumer = new Node(
                "Setpoint",
                new Identifier(generateGlobalUniqueId(), Node.TYPE_CONSUMER),
                setpointConsumerSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", VIRTUAL_COLOR);
            editor.put("x", 20);
            editor.put("y", 200);
            setpointConsumer.setProperties(properties);

            Slot temperatureProcessorFahrenheitInput = new Slot("Fahrenheit", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Slot temperatureProcessorCelciusOutput = new Slot("Celcius", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Slot temperatureProcessorLabelOutput = new Slot("Label", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node temperatureProcessor = new Node(
                "Temperature Processor",
                new Identifier(generateGlobalUniqueId(), "Flow"),
                temperatureProcessorFahrenheitInput,
                temperatureProcessorCelciusOutput,
                temperatureProcessorLabelOutput
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", VIRTUAL_COLOR);
            editor.put("x", 300);
            editor.put("y", 20);
            temperatureProcessor.setProperties(properties);

            Slot setpointProcessorFahrenheitInput = new Slot("Fahrenheit", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Slot setpointProcessorCelciusOutput = new Slot("Celcius", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Slot setpointProcessorLabelOutput = new Slot("Label", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node setpointProcessor = new Node(
                "Temperature Processor",
                new Identifier(generateGlobalUniqueId(), "Flow"),
                setpointProcessorFahrenheitInput,
                setpointProcessorCelciusOutput,
                setpointProcessorLabelOutput
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", VIRTUAL_COLOR);
            editor.put("x", 300);
            editor.put("y", 150);
            setpointProcessor.setProperties(properties);

            Slot temperatureLabelSink = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Node temperatureLabel = new Node(
                "Temperature Label",
                new Identifier(generateGlobalUniqueId(), "TextLabel"),
                temperatureLabelSink
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", UI_WIDGET_COLOR);
            editor.put("x", 750);
            editor.put("y", 50);
            temperatureLabel.setProperties(properties);

            Slot temperatureSetpointLabelSink = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Node temperatureSetpointLabel = new Node(
                "Temperature Setpoint Label",
                new Identifier(generateGlobalUniqueId(), "TextLabel"),
                temperatureSetpointLabelSink
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", UI_WIDGET_COLOR);
            editor.put("x", 750);
            editor.put("y", 150);
            temperatureSetpointLabel.setProperties(properties);

            Slot temperaturePlusSource = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node temperaturePlusButton = new Node(
                "Increase Temperature Button",
                new Identifier(generateGlobalUniqueId(), "PushButton"),
                temperaturePlusSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", UI_WIDGET_COLOR);
            editor.put("x", 50);
            editor.put("y", 400);
            temperaturePlusButton.setProperties(properties);

            Slot temperatureMinusSource = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node temperatureMinusButton = new Node(
                "Decrease Temperature Button",
                new Identifier(generateGlobalUniqueId(), "PushButton"),
                temperatureMinusSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", UI_WIDGET_COLOR);
            editor.put("x", 50);
            editor.put("y", 500);
            temperatureMinusButton.setProperties(properties);

            Slot setpointPlusFilterSink = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Slot setpointPlusTriggerSink = new Slot("Trigger", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Slot setpointPlusFilterSource = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node setpointPlusFilter = new Node(
                "Forward on trigger",
                new Identifier(generateGlobalUniqueId(), "Filter"),
                setpointPlusFilterSink, setpointPlusTriggerSink, setpointPlusFilterSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", PROCESSOR_COLOR);
            editor.put("x", 450);
            editor.put("y", 300);
            setpointPlusFilter.setProperties(properties);

            Slot setpointMinusFilterSink = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Slot setpointMinusFilterTriggerSink = new Slot("Trigger", new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Slot setpointMinusFilterSource = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node setpointMinusFilter = new Node(
                "Forward on trigger",
                new Identifier(generateGlobalUniqueId(), "Filter"),
                setpointMinusFilterSink, setpointMinusFilterTriggerSink, setpointMinusFilterSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", PROCESSOR_COLOR);
            editor.put("x", 450);
            editor.put("y", 450);
            setpointMinusFilter.setProperties(properties);

            Slot incrementFunctionSink = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Slot incrementFunctionSource = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node incrementFunction = new Node(
                "Increment by 1",
                new Identifier(generateGlobalUniqueId(), Node.TYPE_FUNCTION),
                incrementFunctionSink, incrementFunctionSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", PROCESSOR_COLOR);
            editor.put("x", 750);
            editor.put("y", 300);
            incrementFunction.setProperties(properties);

            Slot decrementFunctionSink = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Slot decrementFunctionSource = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SOURCE));
            Node decrementFunction = new Node(
                "Decrement by 1",
                new Identifier(generateGlobalUniqueId(), Node.TYPE_FUNCTION),
                decrementFunctionSink, decrementFunctionSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", PROCESSOR_COLOR);
            editor.put("x", 750);
            editor.put("y", 450);
            decrementFunction.setProperties(properties);

            Slot setpointProducerSink = new Slot(new Identifier(generateGlobalUniqueId(), Slot.TYPE_SINK));
            Node setpointProducer = new Node(
                "Setpoint",
                new Identifier(generateGlobalUniqueId(), Node.TYPE_PRODUCER),
                setpointProducerSink
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", VIRTUAL_COLOR);
            editor.put("x", 1050);
            editor.put("y", 375);
            setpointProducer.setProperties(properties);

            Node[] nodes = new Node[13];
            nodes[0] = temperatureLabel;
            nodes[1] = temperatureSetpointLabel;
            nodes[2] = temperaturePlusButton;
            nodes[3] = temperatureMinusButton;
            nodes[4] = setpointPlusFilter;
            nodes[5] = setpointMinusFilter;
            nodes[6] = temperatureConsumer;
            nodes[7] = setpointConsumer;
            nodes[8] = incrementFunction;
            nodes[9] = decrementFunction;
            nodes[10] = setpointProducer;
            nodes[11] = temperatureProcessor;
            nodes[12] = setpointProcessor;

            flows[2] = new Flow(
                "Thermostat Control",
                new Identifier(generateGlobalUniqueId(), Flow.TYPE),
                nodes,
                new Wire[]{
                    new Wire(temperatureConsumerSource, temperatureProcessorFahrenheitInput),
                    new Wire(temperatureProcessorLabelOutput, temperatureLabelSink),
                    new Wire(setpointConsumerSource, setpointProcessorFahrenheitInput),
                    new Wire(setpointProcessorLabelOutput, temperatureSetpointLabelSink),
                    new Wire(setpointConsumerSource, setpointPlusFilterSink),
                    new Wire(setpointConsumerSource, setpointMinusFilterSink),
                    new Wire(temperaturePlusSource, setpointPlusTriggerSink),
                    new Wire(temperatureMinusSource, setpointMinusFilterTriggerSink),
                    new Wire(setpointPlusFilterSource, incrementFunctionSink),
                    new Wire(setpointMinusFilterSource, decrementFunctionSink),
                    new Wire(incrementFunctionSource, setpointProducerSink),
                    new Wire(decrementFunctionSource, setpointProducerSink)
                }
            );

        /* ###################################################################################### */
        }
    }

    class FlowServiceRouteBuilder extends RestRouteBuilder {
        @Override
        public void configure() throws Exception {

            rest("/flow")

                .get()
                .route().id("GET all flows")
                .process(exchange -> {

                    Flow[] flowsInfo = new Flow[flows.length];
                    for (int i = 0; i < flowsInfo.length; i++) {
                        Flow flow = flows[i];
                        flowsInfo[i] = new Flow(flow.getLabel(), flow.getIdentifier());
                    }

                    exchange.getOut().setBody(flowsInfo);
                })
                .endRest()

                .get("{id}")
                .route().id("GET flow by ID")
                .process(exchange -> {

                    for (Flow flow : flows) {
                        if (flow.getIdentifier().getId().equals(exchange.getIn().getHeader(("id")))) {
                            exchange.getOut().setBody(flow);
                        }
                    }
                    if (exchange.getOut().getBody() == null)
                        exchange.getOut().setHeader(HTTP_RESPONSE_CODE, 404);

                })
                .endRest();
        }
    }

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {
        context.addRoutes(new FlowServiceRouteBuilder());
    }

}
