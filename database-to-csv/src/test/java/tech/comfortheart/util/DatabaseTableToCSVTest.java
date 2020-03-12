package tech.comfortheart.util;

import java.io.IOException;
import java.sql.*;
import java.util.logging.Logger;

public class DatabaseTableToCSVTest {
    Logger logger = Logger.getLogger(DatabaseTableToCSVTest.class.getSimpleName());

    String mysqlUrl = "jdbc:mysql://localhost:3306/test";
    String mysqlUser = "test";
    String mysqlPsw ="test";

    String postgresUrl = "jdbc:postgresql://localhost:5432/test";
    String pgUsr = "hello";
    String pgPsw = "world";

    String mssserverUrl = "jdbc:sqlserver://localhost:1433;DatabaseName=test";
    String mssserverUsr = "hello";
    String mssserverPsw = "world@1234!";

//    @Test
    public void testMySQLConvert() throws SQLException, IOException {

        DatabaseTableToCSV converter = new DatabaseTableToCSV(mysqlUrl,
                SupportedDatabase.MYSQL,
                mysqlUser,
                mysqlPsw);
        try {
            converter.convertTable("test", "/tmp/test.csv");
            converter.convertWithCustomSql("select * from test where id <= 100001", "/tmp/test1.csv");
        } finally {
            converter.close();
        }
    }

//    @Test
    public void testSqlServerConvert() throws SQLException, IOException {

        DatabaseTableToCSV converter = new DatabaseTableToCSV(mssserverUrl,
                SupportedDatabase.SQL_SQLSERVER,
                mssserverUsr,
                mssserverPsw);
        try {
            converter.convertTable("test", "/tmp/test.csv");
            converter.convertWithCustomSql("select * from test where id <= 100001", "/tmp/test1.csv");
        } finally {
            converter.close();
        }
    }

//    @Test
    public void testPGConvert() throws SQLException, IOException {

        DatabaseTableToCSV converter = new DatabaseTableToCSV(postgresUrl,
                SupportedDatabase.POSTGRESQL,
                pgUsr,
                pgPsw);
        try {
            converter.convertTable("test", "/tmp/test.csv");
            converter.convertWithCustomSql("select * from test where id <= 100001", "/tmp/test1.csv");
        } finally {
            converter.close();
        }
    }

//    @Test
    public void initMySQLData() {
        String driver = com.mysql.cj.jdbc.Driver.class.getName();

        try {
            initData(driver, mysqlUrl, mysqlUser, mysqlPsw);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    @Test
    public void initPGData() {
        String driver = org.postgresql.Driver.class.getName();

        try {
            initData(driver, postgresUrl, pgUsr, pgPsw);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    @Test
    public void initSqlServerData() {
        String driver = com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName();

        try {
            initData(driver, mssserverUrl, mssserverUsr, mssserverPsw);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void initData(String driverName, String url, String username, String password) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class.forName(driverName).newInstance();
        int batchSize = 10000;
        try(Connection conn = DriverManager.getConnection(url, username, password);
            PreparedStatement stmt = conn.prepareStatement("insert into test(username, last_update_date) values(?, ?)")) {
            int count = 0;
            // Test one million
            for(int i=0; i < 1_000_000; i++) {
                stmt.setString(1, "user " + i);
                stmt.setDate(2, new Date(new java.util.Date().getTime()));
                stmt.addBatch();
                count++;
                if (count >= batchSize) {
                    stmt.executeBatch();
                    count = 0;
                    logger.info("Inserted " + i + " records!");
                }
            }
            if (count > 0) {
                stmt.executeBatch();
            }

        }

    }
}
