package org.openremote.server.persistence;

import org.apache.camel.StaticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class PersistenceService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceService.class);

    public static final String PERSISTENCE_UNIT_NAME = "ControllerPU";

    final protected DatabaseProduct databaseProduct;
    final protected Map<String, String> properties = new HashMap<>();

    protected EntityManagerFactory entityManagerFactory;

    public PersistenceService(DatabaseProduct databaseProduct) {
        this.databaseProduct = databaseProduct;
    }

    @Override
    public void start() throws Exception {
        LOG.info("Configuration JPA service for database product: " + databaseProduct);

        properties.put("hibernate.dialect", databaseProduct.getHibernateDialect());

        entityManagerFactory =
            Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
    }

    @Override
    public void stop() throws Exception {
        entityManagerFactory.close();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public void createSchema() {
        generateSchema("create");
    }

    public void dropSchema() {
        generateSchema("drop");
    }

    public void generateSchema(String action) {
        // Take exiting EMF properties, override the schema generation setting on a copy
        Map<String, String> createSchemaProperties = new HashMap<>(properties);
        createSchemaProperties.put(
            "javax.persistence.schema-generation.database.action",
            action
        );
        Persistence.generateSchema(PERSISTENCE_UNIT_NAME, createSchemaProperties);
    }

}
