package org.openremote.server.persistence;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.apache.camel.StaticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

public class TransactionManagerService implements StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionManagerService.class);

    public static final String DATA_SOURCE_NAME = "ControllerDS";

    final protected Context namingContext;
    final protected String serverId, connectionUrl, username, password;
    final protected int minPoolSize, maxPoolSize, preparedStatementCacheSize;
    final protected DatabaseProduct databaseProduct;

    protected PoolingDataSource datasource;

    public TransactionManagerService(String serverId,
                                     DatabaseProduct databaseProduct,
                                     String connectionUrl, String username, String password,
                                     int minPoolSize, int maxPoolSize, int preparedStatementCacheSize) {
        try {
            namingContext = new InitialContext();
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
        this.serverId = serverId;
        this.databaseProduct = databaseProduct;
        this.connectionUrl = connectionUrl;
        this.username = username;
        this.password = password;
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.preparedStatementCacheSize = preparedStatementCacheSize;
    }

    @Override
    public void start() throws Exception {
        LOG.info("Configuration pooling transactional data source for database product: " + databaseProduct);

        TransactionManagerServices.getConfiguration().setServerId(serverId);
        TransactionManagerServices.getConfiguration().setDisableJmx(true);

        // TODO We might want to enable this in production
        // Disable transaction recovery journal
        TransactionManagerServices.getConfiguration().setJournal("null");

        // Disabling warnings when the database isn't accessed in a transaction (e.g. schema export)
        TransactionManagerServices.getConfiguration().setWarnAboutZeroResourceTransaction(false);

        datasource = new PoolingDataSource();
        datasource.setUniqueName(DATA_SOURCE_NAME);
        datasource.setMinPoolSize(minPoolSize);
        datasource.setMaxPoolSize(maxPoolSize);
        datasource.setPreparedStatementCacheSize(preparedStatementCacheSize);
        datasource.setIsolationLevel("READ_COMMITTED");

        // Hibernate's SQL schema generator calls connection.setAutoCommit(true)
        // and we use auto-commit mode when the EntityManager is in suspended
        // mode and not joined with a transaction.
        datasource.setAllowLocalTransactions(true);

        LOG.info("Using data source connection URL: " + connectionUrl);
        databaseProduct.getConfiguration().configure(datasource, connectionUrl, username, password);

        datasource.init();
    }

    @Override
    public void stop() throws Exception {
        datasource.close();
        TransactionManagerServices.getTransactionManager().shutdown();
    }

    public DatabaseProduct getDatabaseProduct() {
        return databaseProduct;
    }

    public Context getNamingContext() {
        return namingContext;
    }

    public UserTransaction getUserTransaction() {
        try {
            return (UserTransaction) getNamingContext()
                .lookup("java:comp/UserTransaction");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public DataSource getDataSource() {
        try {
            return (DataSource) getNamingContext().lookup(DATA_SOURCE_NAME);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void rollback() {
        UserTransaction tx = getUserTransaction();
        try {
            if (tx.getStatus() == Status.STATUS_ACTIVE ||
                tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                tx.rollback();
        } catch (Exception ex) {
            // TODO configurable escalation strategy, e.g. stop everything and notify admin
            System.err.println("Rollback of transaction failed, trace follows!");
            ex.printStackTrace(System.err);
        }
    }
}
