package tech.comfortheart.util;


import com.microsoft.sqlserver.jdbc.SQLServerResultSet;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

import static tech.comfortheart.util.SupportedDatabase.*;

public class DatabaseTableToCSV implements AutoCloseable {
    static final Logger logger = Logger.getLogger(DatabaseTableToCSV.class.getSimpleName());

    private static final int BUFFER_SIZE = 1000;

    private DatabaseType databaseType;

    private Connection dbConn;

    private DatabaseTableToCSV(){}

    public DatabaseTableToCSV(DatabaseConfig config) throws SQLException {
        this(config.getJdbcUrl(), config.getDatabaseType(), config.getUsername(), config.getPassword());
    }

    public DatabaseTableToCSV(String jdbcUrl,
                              String dbType,
                              String username,
                              String password
                        ) throws SQLException {

        Properties info = new Properties();
        info.put("user", username);
        info.put("password", password);
        Driver driver;
        logger.info("Loading driver of " + dbType);
        if (dbType.trim().toUpperCase().equals(MYSQL)) {
            driver = new com.mysql.cj.jdbc.Driver();
            databaseType = DatabaseType.MYSQL;
        } else if (dbType.trim().toUpperCase().equals(ORACLE)) {
            driver = new oracle.jdbc.OracleDriver();
            databaseType = DatabaseType.ORACLE;
        } else if (dbType.trim().toUpperCase().equals(SQL_SQLSERVER)) {
            driver = new com.microsoft.sqlserver.jdbc.SQLServerDriver();
            databaseType = DatabaseType.SQLSERVER;
        } else if (dbType.trim().toUpperCase().equals(POSTGRESQL)) {
            driver = new org.postgresql.Driver();
            databaseType = DatabaseType.POSTGRESQL;
        } else {
            throw new SQLException("The Database is not supported: " + dbType);
        }
        logger.info("Driver loaded!");
        logger.info("Trying to connect to Database..");
        dbConn = driver.connect(jdbcUrl, info);
        logger.info("Database connected!");
        dbConn.setReadOnly(true);

        /**
         * Add this to avoid POSTGRESQL issue on fetchSize.
         */
        dbConn.setAutoCommit(false);
    }

    /**
     * Extract the whole table to be a csv.
     * @param table
     * @param csvPath
     * @throws IOException
     * @throws SQLException
     */
    public final void convertTable(final String table, final String csvPath) throws IOException, SQLException {
        convert(table, null, csvPath);
    }

    /**
     * Extract the records with a customized SQL, and save to a csv.
     * @param customSql
     * @param csvPath
     * @throws IOException
     * @throws SQLException
     */
    public final void convertWithCustomSql(final String customSql, final String csvPath) throws IOException, SQLException {
        convert(null, customSql, csvPath);
    }

    /**
     * Extract records by custom sql or whole table, save as a csv.
     * @param table
     * @param customSql
     * @param csvPath
     * @throws SQLException
     * @throws IOException
     */
    private final void convert(final String table, final String customSql, final String csvPath) throws SQLException, IOException {
        String sql;
        if (customSql != null) {
            sql = customSql;
        } else {
            sql = "select * from " + table;
        }

        PreparedStatement statement;
        /**
         * Create statement, set parameters properly to avoid OOM.
         */
        if (databaseType.equals(DatabaseType.MYSQL)) {
            /**
             * For MySQL, only this way can force the driver to fetch in stream. Do NOT change the fetch size to other values.
             */

            statement = dbConn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(Integer.MIN_VALUE);
            statement.setFetchDirection(ResultSet.FETCH_FORWARD);
        } else if (databaseType.equals(DatabaseType.SQLSERVER)) {
            /**
             * Ref to https://docs.microsoft.com/en-us/previous-versions/sql/legacy/aa342344(v=sql.90)?redirectedfrom=MSDN
             *
             * SQL Server: tested with good performance, with the fetch size, can solve the OOM issue, and performance increased.
             */
            statement = dbConn.prepareStatement(sql, SQLServerResultSet.TYPE_SS_SERVER_CURSOR_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(BUFFER_SIZE);
            statement.setFetchDirection(ResultSet.FETCH_FORWARD);
        } else {
            /**
             * PostgreSQL: tested with good performance, with the fetch size, can solve the OOM issue.
             * Oracle: Not tested, per check the document, below code should be able to avoid OOM.
             */
            statement = dbConn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.setFetchSize(BUFFER_SIZE);
            statement.setFetchDirection(ResultSet.FETCH_FORWARD);
        }
        ResultSet rs = statement.executeQuery();
        CsvWriter csvWriter = new CsvWriter(csvPath);
        try {
            csvWriter.writeResultSetWithHeader(rs);
        } finally {
            csvWriter.close();
        }
    }

    /**
     * Must call this in finally{}
     */
    @Override
    public void close() {
        try {
            dbConn.close();
            logger.info("DB connection closed!");
        } catch (SQLException e) {

        }
    }
}
