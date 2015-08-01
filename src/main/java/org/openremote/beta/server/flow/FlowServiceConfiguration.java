package org.openremote.beta.server.flow;

import org.apache.camel.CamelContext;
import org.openremote.beta.server.Configuration;
import org.openremote.beta.server.Environment;
import org.openremote.beta.server.WebserverConfiguration.RestRouteBuilder;
import org.openremote.beta.server.testdata.SampleEnvironmentWidget;
import org.openremote.beta.server.testdata.SampleTemperatureProcessor;
import org.openremote.beta.server.testdata.SampleThermostatControl;
import org.openremote.beta.shared.flow.Flow;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

public class FlowServiceConfiguration implements Configuration {

    static Flow[] SAMPLE_FLOWS = new Flow[]{
        SampleEnvironmentWidget.FLOW,
        SampleTemperatureProcessor.FLOW,
        SampleThermostatControl.FLOW
    };

    class FlowServiceRouteBuilder extends RestRouteBuilder {
        @Override
        public void configure() throws Exception {

            rest("/flow")

                .get()
                .route().id("GET all flows")
                .process(exchange -> {

                    Flow[] flowsInfo = new Flow[SAMPLE_FLOWS.length];
                    for (int i = 0; i < flowsInfo.length; i++) {
                        Flow flow = SAMPLE_FLOWS[i];
                        flowsInfo[i] = new Flow(flow.getLabel(), flow.getIdentifier());
                    }

                    exchange.getOut().setBody(flowsInfo);
                })
                .endRest()

                .get("{id}")
                .route().id("GET flow by ID")
                .process(exchange -> {

                    for (Flow flow : SAMPLE_FLOWS) {
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
