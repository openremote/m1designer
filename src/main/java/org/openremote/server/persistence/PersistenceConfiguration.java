package org.openremote.server.persistence;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;

public class PersistenceConfiguration implements Configuration {

    public static final String TRANSACTION_SERVER_ID = "TRANSACTION_SERVER_ID";
    public static final String TRANSACTION_SERVER_ID_DEFAULT = "MyOpenRemoteController123";
    public static final String DATABASE_PRODUCT = "DATABASE_PRODUCT";
    public static final String DATABASE_PRODUCT_DEFAULT = "H2";
    public static final String DATABASE_CONNECTION_URL = "DATABASE_CONNECTION_URL";
    public static final String DATABASE_CONNECTION_URL_DEFAULT = "jdbc:h2:file:./or-controller-database";
    public static final String DATABASE_USERNAME = "DATABASE_USERNAME";
    public static final String DATABASE_USERNAME_DEFAULT = "sa";
    public static final String DATABASE_PASSWORD = "DATABASE_PASSWORD";
    public static final String DATABASE_PASSWORD_DEFAULT = "";
    public static final String DATABASE_MIN_POOL_SIZE = "DATABASE_MIN_POOL_SIZE";
    public static final String DATABASE_MIN_POOL_SIZE_DEFAULT = "5";
    public static final String DATABASE_MAX_POOL_SIZE = "DATABASE_MAX_POOL_SIZE";
    public static final String DATABASE_MAX_POOL_SIZE_DEFAULT = "25";
    public static final String DATABASE_STATEMENT_CACHE_SIZE = "DATABASE_STATEMENT_CACHE_SIZE";
    public static final String DATABASE_STATEMENT_CACHE_SIZE_DEFAULT = "20";

    @Override
    public void apply(Environment environment, CamelContext context) throws Exception {

        DatabaseProduct databaseProduct =
            DatabaseProduct.valueOf(environment.getProperty(DATABASE_PRODUCT, DATABASE_PRODUCT_DEFAULT));

        TransactionManagerService transactionManagerService =
            new TransactionManagerService(
                environment.getProperty(TRANSACTION_SERVER_ID, TRANSACTION_SERVER_ID_DEFAULT),
                databaseProduct,
                environment.getProperty(DATABASE_CONNECTION_URL, DATABASE_CONNECTION_URL_DEFAULT),
                environment.getProperty(DATABASE_USERNAME, DATABASE_USERNAME_DEFAULT),
                environment.getProperty(DATABASE_PASSWORD, DATABASE_PASSWORD_DEFAULT),
                Integer.parseInt(environment.getProperty(DATABASE_MIN_POOL_SIZE, DATABASE_MIN_POOL_SIZE_DEFAULT)),
                Integer.parseInt(environment.getProperty(DATABASE_MAX_POOL_SIZE, DATABASE_MAX_POOL_SIZE_DEFAULT)),
                Integer.parseInt(environment.getProperty(DATABASE_STATEMENT_CACHE_SIZE, DATABASE_STATEMENT_CACHE_SIZE_DEFAULT))
            );

        PersistenceService persistenceService = new PersistenceService(databaseProduct);

        context.addService(transactionManagerService);
        context.addService(persistenceService);
    }
}
