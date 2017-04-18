package edu.goldenhammer.database.postgresql;

import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by devonkinghorn on 2/1/17.
 */
public class SQLConnectionFactory {

    private static SQLConnectionFactory singleton;
    public static SQLConnectionFactory getInstance() {
        if (singleton == null) {
            String host = "ec2-35-167-43-52.us-west-2.compute.amazonaws.com";
            String database = "postgres";
            String schema = null;
            int maxConnections = 10;
            String userName = "postgres";
            String password = "goldenhammerteam";
            singleton = new SQLConnectionFactory(host, database, schema, maxConnections, userName, password);
        }
        return singleton;
    }

    SQLConnectionFactory(String host, String database, String schema, int maxConnections, String userName, String password) {
        this.databaseName = database;
        this.dataSource = configureDataSource(host, database, maxConnections, userName, password);
        if (schema != null) {
            this.schema = schema;
        }
        else {
            this.schema = "public";
        }
    }

    private static final String POSTGRESQL_DRIVER_NAME = "org.postgresql.Driver";
    private DataSource dataSource;
    private String schema;
    private String databaseName;

    public Connection getConnection() {
        try {
            Connection conn = dataSource.getConnection();
            conn.setSchema(schema);
            return conn;
        }
        catch (SQLException e) {
            throw new RuntimeException(String.format("Failed to get connection to database: %s schema: %s", databaseName, schema), e);
        }
    }

    private DataSource configureDataSource(String host, String database, int maxConnections, String userName, String password) {

        String url = String.format("jdbc:postgresql://%s:5432/%s", host, database);
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(POSTGRESQL_DRIVER_NAME);
        dataSource.setUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        dataSource.setMaxTotal(maxConnections);
        return dataSource;
    }
}
