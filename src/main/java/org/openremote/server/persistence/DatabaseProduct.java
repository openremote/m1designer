package org.openremote.server.persistence;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.hibernate.dialect.H2Dialect;

public enum DatabaseProduct {

    H2((ds, connectionURL, username, password) -> {
        ds.setClassName("org.h2.jdbcx.JdbcDataSource");

        // External instance: jdbc:h2:tcp://localhost/mem:test;USER=sa
        ds.getDriverProperties().put("URL", connectionURL);
        ds.getDriverProperties().put("user", username);
        ds.getDriverProperties().put("password", password);

        // TODO: Don't trace log values larger than X bytes (especially useful for debugging LOBs, which are accessed in toString()!)
        System.setProperty("h2.maxTraceDataLength", "256"); // 256 bytes, default is 64 kilobytes

    }, H2Dialect.class.getName());

    public DataSourceConfiguration configuration;
    public String hibernateDialect;

    private DatabaseProduct(DataSourceConfiguration configuration,
                            String hibernateDialect) {
        this.configuration = configuration;
        this.hibernateDialect = hibernateDialect;
    }

    public DataSourceConfiguration getConfiguration() {
        return configuration;
    }

    public String getHibernateDialect() {
        return hibernateDialect;
    }

    public interface DataSourceConfiguration {

        void configure(PoolingDataSource ds, String connectionURL, String username, String password);
    }

}
