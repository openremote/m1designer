package org.openremote.server;

import org.apache.camel.CamelContext;
import org.openremote.server.event.EventService;
import org.openremote.server.persistence.PersistenceService;
import org.openremote.server.persistence.TransactionManagerService;
import org.openremote.server.persistence.flow.FlowDAO;
import org.openremote.server.persistence.flow.FlowDAOImpl;
import org.openremote.server.testdata.SampleEnvironmentWidget;
import org.openremote.server.testdata.SampleTemperatureProcessor;
import org.openremote.server.testdata.SampleThermostatControl;
import org.openremote.shared.event.FlowDeployEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.openremote.server.Environment.DEV_MODE;
import static org.openremote.server.Environment.DEV_MODE_DEFAULT;

public class SampleConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(SampleConfiguration.class);

    public static final String START_SAMPLE_FLOWS = "START_SAMPLE_FLOWS";
    public static final String START_SAMPLE_FLOWS_DEFAULT = "true";

    public static final String CREATE_DATABASE_SCHEMA = "CREATE_DATABASE_SCHEMA";
    public static final String CREATE_DATABASE_SCHEMA_DEFAULT = "true";

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        if (!Boolean.valueOf(environment.getProperty(DEV_MODE, DEV_MODE_DEFAULT))) {
            return;
        }

        context.addStartupListener((camelContext, alreadyStarted) -> {
            if (!alreadyStarted) {
                LOG.info("Dev mode enabled, importing sample data...");

                TransactionManagerService tm = camelContext.hasService(TransactionManagerService.class);
                PersistenceService ps = camelContext.hasService(PersistenceService.class);

                if (Boolean.valueOf(environment.getProperty(CREATE_DATABASE_SCHEMA, CREATE_DATABASE_SCHEMA_DEFAULT))) {
                    ps.dropSchema();
                    ps.createSchema();
                }

                UserTransaction tx = tm.getUserTransaction();
                tx.begin();
                try {
                    EntityManager em = ps.createEntityManager();
                    FlowDAO flowDAO = new FlowDAOImpl(em);

                    flowDAO.makePersistent(SampleTemperatureProcessor.FLOW, false);
                    flowDAO.makePersistent(SampleThermostatControl.FLOW, false);
                    flowDAO.makePersistent(SampleEnvironmentWidget.FLOW, false);

                    tx.commit();
                    em.close();
                } finally {
                    tm.rollback();
                }

                if (Boolean.valueOf(environment.getProperty(START_SAMPLE_FLOWS, START_SAMPLE_FLOWS_DEFAULT))) {
                    camelContext.createProducerTemplate().sendBody(EventService.INCOMING_EVENT_QUEUE, new FlowDeployEvent(SampleEnvironmentWidget.FLOW.getId()));
                    camelContext.createProducerTemplate().sendBody(EventService.INCOMING_EVENT_QUEUE, new FlowDeployEvent(SampleTemperatureProcessor.FLOW.getId()));
                    camelContext.createProducerTemplate().sendBody(EventService.INCOMING_EVENT_QUEUE, new FlowDeployEvent(SampleThermostatControl.FLOW.getId()));
                }
            }
        });
    }

}
