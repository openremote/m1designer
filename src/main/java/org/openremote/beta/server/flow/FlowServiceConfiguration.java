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

            Slot temperatureSensorSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
            Node temperatureSensor = new Node(
                generateGlobalUniqueId(),
                "ZWave Sensor",
                "Livingroom Temperature",
                temperatureSensorSource
            );
            Map<String, Object> properties = createMap();
            Map<String, Object> editor = createMap(properties, "editor");
            editor.put("x", 50);
            editor.put("y", 50);
            editor.put("color", SENSOR_COLOR);
            temperatureSensor.setProperties(properties);

            Slot setpointSensorSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
            Node setpointSensor = new Node(
                generateGlobalUniqueId(),
                "ZWave Sensor",
                "Livingroom Setpoint",
                setpointSensorSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("x", 50);
            editor.put("y", 150);
            editor.put("color", SENSOR_COLOR);
            setpointSensor.setProperties(properties);

            Slot temperatureControlCurrentSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK, "Current");
            Slot temperatureControlSetpointSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK, "Setpoint");
            Slot temperatureControlSetpointSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE, "Setpoint");
            Node temperatureControl = new Node(
                generateGlobalUniqueId(),
                "Flow",
                "Thermostat Control",
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

            Slot setpointActuatorSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
            Node setpointActuator = new Node(
                generateGlobalUniqueId(),
                "ZWave Actuator",
                "Livingroom Setpoint",
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
                generateGlobalUniqueId(),
                "Livingroom Environment",
                nodes,
                new Wire[]{
                    new Wire(temperatureSensorSource.getId(), temperatureControlCurrentSink.getId()),
                    new Wire(setpointSensorSource.getId(), temperatureControlSetpointSink.getId()),
                    new Wire(temperatureControlSetpointSource.getId(), setpointActuatorSink.getId())
                }
            );

        /* ###################################################################################### */
        }
        flows[1] = SampleTemperatureProcessor.FLOW;
        {
        /* ###################################################################################### */

            Slot temperatureConsumerSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
            Node temperatureConsumer = new Node(
                generateGlobalUniqueId(),
                "Consumer",
                "Current",
                temperatureConsumerSource
            );
            Map<String, Object> properties = createMap();
            Map<String, Object> editor = createMap(properties, "editor");
            editor.put("color", VIRTUAL_COLOR);
            editor.put("x", 20);
            editor.put("y", 20);
            temperatureConsumer.setProperties(properties);

            Slot setpointConsumerSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
            Node setpointConsumer = new Node(
                generateGlobalUniqueId(),
                "Consumer",
                "Setpoint",
                setpointConsumerSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", VIRTUAL_COLOR);
            editor.put("x", 20);
            editor.put("y", 200);
            setpointConsumer.setProperties(properties);

            Slot temperatureProcessorFahrenheitInput = new Slot(generateGlobalUniqueId(), Slot.Type.SINK, "Fahrenheit");
            Slot temperatureProcessorCelciusOutput = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE, "Celcius");
            Slot temperatureProcessorLabelOutput = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE, "Label");
            Node temperatureProcessor = new Node(
                generateGlobalUniqueId(),
                "Flow",
                "Temperature Processor",
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

            Slot setpointProcessorFahrenheitInput = new Slot(generateGlobalUniqueId(), Slot.Type.SINK, "Fahrenheit");
            Slot setpointProcessorCelciusOutput = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE, "Celcius");
            Slot setpointProcessorLabelOutput = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE, "Label");
            Node setpointProcessor = new Node(
                generateGlobalUniqueId(),
                "Flow",
                "Temperature Processor",
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

            Slot temperatureLabelSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
            Node temperatureLabel = new Node(
                generateGlobalUniqueId(),
                "TextLabel",
                "Temperature Label",
                temperatureLabelSink
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", UI_WIDGET_COLOR);
            editor.put("x", 750);
            editor.put("y", 50);
            temperatureLabel.setProperties(properties);

            Slot temperatureSetpointLabelSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
            Node temperatureSetpointLabel = new Node(
                generateGlobalUniqueId(),
                "TextLabel",
                "Temperature Setpoint Label",
                temperatureSetpointLabelSink
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", UI_WIDGET_COLOR);
            editor.put("x", 750);
            editor.put("y", 150);
            temperatureSetpointLabel.setProperties(properties);

            Slot temperaturePlusSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
            Node temperaturePlusButton = new Node(
                generateGlobalUniqueId(),
                "PushButton",
                "Increase Temperature Button",
                temperaturePlusSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", UI_WIDGET_COLOR);
            editor.put("x", 50);
            editor.put("y", 400);
            temperaturePlusButton.setProperties(properties);

            Slot temperatureMinusSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
            Node temperatureMinusButton = new Node(
                generateGlobalUniqueId(),
                "PushButton",
                "Decrease Temperature Button",
                temperatureMinusSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", UI_WIDGET_COLOR);
            editor.put("x", 50);
            editor.put("y", 500);
            temperatureMinusButton.setProperties(properties);

            Slot setpointPlusFilterSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
            Slot setpointPlusTriggerSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK, "Trigger");
            Slot setpointPlusFilterSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
            Node setpointPlusFilter = new Node(
                generateGlobalUniqueId(),
                "Filter",
                "Forward on trigger",
                setpointPlusFilterSink, setpointPlusTriggerSink, setpointPlusFilterSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", PROCESSOR_COLOR);
            editor.put("x", 450);
            editor.put("y", 300);
            setpointPlusFilter.setProperties(properties);

            Slot setpointMinusFilterSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
            Slot setpointMinusFilterTriggerSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK, "Trigger");
            Slot setpointMinusFilterSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
            Node setpointMinusFilter = new Node(
                generateGlobalUniqueId(),
                "Filter",
                "Forward on trigger",
                setpointMinusFilterSink, setpointMinusFilterTriggerSink, setpointMinusFilterSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", PROCESSOR_COLOR);
            editor.put("x", 450);
            editor.put("y", 450);
            setpointMinusFilter.setProperties(properties);

            Slot incrementFunctionSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
            Slot incrementFunctionSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
            Node incrementFunction = new Node(
                generateGlobalUniqueId(),
                "Function",
                "Increment by 1",
                incrementFunctionSink, incrementFunctionSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", PROCESSOR_COLOR);
            editor.put("x", 750);
            editor.put("y", 300);
            incrementFunction.setProperties(properties);

            Slot decrementFunctionSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
            Slot decrementFunctionSource = new Slot(generateGlobalUniqueId(), Slot.Type.SOURCE);
            Node decrementFunction = new Node(
                generateGlobalUniqueId(),
                "Function",
                "Decrement by 1",
                decrementFunctionSink, decrementFunctionSource
            );
            properties = createMap();
            editor = createMap(properties, "editor");
            editor.put("color", PROCESSOR_COLOR);
            editor.put("x", 750);
            editor.put("y", 450);
            decrementFunction.setProperties(properties);

            Slot setpointProducerSink = new Slot(generateGlobalUniqueId(), Slot.Type.SINK);
            Node setpointProducer = new Node(
                generateGlobalUniqueId(),
                "Producer",
                "Setpoint",
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
                generateGlobalUniqueId(),
                "Thermostat Control",
                nodes,
                new Wire[]{
                    new Wire(temperatureConsumerSource.getId(), temperatureProcessorFahrenheitInput.getId()),
                    new Wire(temperatureProcessorLabelOutput.getId(), temperatureLabelSink.getId()),
                    new Wire(setpointConsumerSource.getId(), setpointProcessorFahrenheitInput.getId()),
                    new Wire(setpointProcessorLabelOutput.getId(), temperatureSetpointLabelSink.getId()),
                    new Wire(setpointConsumerSource.getId(), setpointPlusFilterSink.getId()),
                    new Wire(setpointConsumerSource.getId(), setpointMinusFilterSink.getId()),
                    new Wire(temperaturePlusSource.getId(), setpointPlusTriggerSink.getId()),
                    new Wire(temperatureMinusSource.getId(), setpointMinusFilterTriggerSink.getId()),
                    new Wire(setpointPlusFilterSource.getId(), incrementFunctionSink.getId()),
                    new Wire(setpointMinusFilterSource.getId(), decrementFunctionSink.getId()),
                    new Wire(incrementFunctionSource.getId(), setpointProducerSink.getId()),
                    new Wire(decrementFunctionSource.getId(), setpointProducerSink.getId())
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
                        flowsInfo[i] = new Flow(flow.getId(), flow.getLabel());
                    }

                    exchange.getOut().setBody(flowsInfo);
                })
                .endRest()

                .get("{id}")
                .route().id("GET flow by ID")
                .process(exchange -> {

                    for (Flow flow : flows) {
                        if (flow.getId().equals(exchange.getIn().getHeader(("id")))) {
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
