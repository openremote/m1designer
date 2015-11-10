/*
 * Copyright 2015, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openremote.server.persistence;

import org.apache.camel.CamelContext;
import org.openremote.server.Configuration;
import org.openremote.server.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openremote.server.Environment.DEV_MODE;
import static org.openremote.server.Environment.DEV_MODE_DEFAULT;

public class PersistenceConfiguration implements Configuration {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceConfiguration.class);

    public static final String TRANSACTION_SERVER_ID = "TRANSACTION_SERVER_ID";
    public static final String TRANSACTION_SERVER_ID_DEFAULT = "MyOpenRemoteController123";
    public static final String DATABASE_PRODUCT = "DATABASE_PRODUCT";
    public static final String DATABASE_PRODUCT_DEFAULT = "H2";
    public static final String DATABASE_CONNECTION_URL = "DATABASE_CONNECTION_URL";
    public static final String DATABASE_CONNECTION_URL_DEFAULT = "jdbc:h2:file:./or-controller-database";
    public static final String DATABASE_CONNECTION_URL_DEFAULT_DEV_MODE = "jdbc:h2:mem:test";
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

        boolean devMode = Boolean.valueOf(environment.getProperty(DEV_MODE, DEV_MODE_DEFAULT));

        DatabaseProduct databaseProduct =
            DatabaseProduct.valueOf(environment.getProperty(DATABASE_PRODUCT, DATABASE_PRODUCT_DEFAULT));

        TransactionManagerService transactionManagerService =
            new TransactionManagerService(
                environment.getProperty(TRANSACTION_SERVER_ID, TRANSACTION_SERVER_ID_DEFAULT),
                databaseProduct,
                environment.getProperty(
                    DATABASE_CONNECTION_URL,
                    devMode
                        ? DATABASE_CONNECTION_URL_DEFAULT_DEV_MODE
                        : DATABASE_CONNECTION_URL_DEFAULT
                ),
                environment.getProperty(DATABASE_USERNAME, DATABASE_USERNAME_DEFAULT),
                environment.getProperty(DATABASE_PASSWORD, DATABASE_PASSWORD_DEFAULT),
                Integer.parseInt(environment.getProperty(DATABASE_MIN_POOL_SIZE, DATABASE_MIN_POOL_SIZE_DEFAULT)),
                Integer.parseInt(environment.getProperty(DATABASE_MAX_POOL_SIZE, DATABASE_MAX_POOL_SIZE_DEFAULT)),
                Integer.parseInt(environment.getProperty(DATABASE_STATEMENT_CACHE_SIZE, DATABASE_STATEMENT_CACHE_SIZE_DEFAULT))
            );

        PersistenceService persistenceService =
            new PersistenceService(transactionManagerService, databaseProduct);

        context.addService(transactionManagerService);
        context.addService(persistenceService);
    }
}
