package org.openremote.server;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HackZwaveConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(HackZwaveConfiguration.class);

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        // TODO We must do discovery either on system startup or better in route policies in SensorRoute/ActuatorRoute
/*
        context.addStartupListener((ctx, alreadyStarted) -> {
                if (!alreadyStarted) {
                    LOG.info("### ##############################################################################################");
                    ctx.createProducerTemplate().sendBody("zwave://discovery?serialPort={{env:ZWAVE_SERIAL_PORT}}", null);
                    LOG.info("### ##############################################################################################");
                }
            }
        );
*/

/* TODO Some test code for debugging

        context.addRoutes(new WebserverConfiguration.RestRouteBuilder() {
            @Override
            public void configure() throws Exception {
                super.configure();

                rest("/test/zwave")
                    .get("dim")
                    .route().id("Test DIM")
                    .process(exchange -> {
                        LOG.info("##################### SENDING LIGHT DIM");
                        ProducerTemplate producerTemplate = getContext().createProducerTemplate();
                        producerTemplate.sendBodyAndHeader(
                            "zwave://3?serialPort={{env:ZWAVE_SERIAL_PORT}}", "25", HEADER_COMMAND, "DIM"
                        );
                    })
                    .endRest()

                    .get("off")
                    .route().id("Test OFF")
                    .process(exchange -> {
                        LOG.info("##################### SENDING LIGHT OFF");
                        ProducerTemplate producerTemplate = getContext().createProducerTemplate();
                        producerTemplate.sendBodyAndHeader(
                            "zwave://3?serialPort={{env:ZWAVE_SERIAL_PORT}}", null, HEADER_COMMAND, "OFF"
                        );
                    })
                    .endRest();
            }
        });

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("zwave://3?serialPort={{env:ZWAVE_SERIAL_PORT}}&command=STATUS")
                    .process(exchange -> {
                        String currentStatus = exchange.getIn().getBody(String.class);
                        LOG.info("######################## RECEIVED STATUS: " + currentStatus);
                    });
            }
        });
*/

    }
}
