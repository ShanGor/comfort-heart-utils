package tech.comfortheart;

import org.junit.Test;
import tech.comfortheart.util.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

import static tech.comfortheart.util.DatabaseConfigTest.initHomeWithPresetPassword;

/**
 * Unit test for simple App.
 */
public class DatabaseToCsvAppTest
{
    private static Logger logger = Logger.getLogger("MainTests");
    /**
     * Rigorous Test :-)
     */
    @Test
    public void testStupidThings() {
        SupportedDatabase.DatabaseType.valueOf("MYSQL");
        SupportedDatabase.DatabaseType.values();
        System.out.println(SupportedDatabase.DatabaseType.MYSQL);
        System.out.println(SupportedDatabase.DatabaseType.SQLSERVER);
        System.out.println(SupportedDatabase.DatabaseType.ORACLE);
        System.out.println(SupportedDatabase.DatabaseType.POSTGRESQL);
        logger.info(SupportedDatabase.MYSQL);
        logger.info(SupportedDatabase.POSTGRESQL);
        logger.info(SupportedDatabase.SQL_SQLSERVER);
        logger.info(SupportedDatabase.ORACLE);
        new SupportedDatabase();
    }

    @Test
    public void testMain() throws Exception {
        String[] args = new String[] {};
        DatabaseToCsvApp.main(args);

        String testHome = "/tmp/job-runner-test-main-432432dfds34458";
        System.setProperty("user.home", testHome);
        File homeFolder = new File(testHome);
        if (homeFolder.exists()) {
            IOUtils.removeFileOrDir(homeFolder);
        }
        homeFolder.mkdirs();

        /**
         * Init
         */
        args = new String[]{"-init", "hey"};
        StringBuilder sb = new StringBuilder();
        sb.append("myCommonName").append("\r")
                .append("myOU").append("\r")
                .append("myOrg").append("\r")
                .append("myCity").append("\r")
                .append("myState").append("\r")
                .append("myCountry").append("\r");

        InputStream ins = new ByteArrayInputStream(sb.toString().getBytes());
        System.setIn(ins);
        DatabaseToCsvApp.main(args);

        /**
         * Encrypt Password
         */
        args = new String[]{"-encrypt", "hey-you"};
        DatabaseToCsvApp.main(args);

        /**
         * Test
         */
        args = new String[]{TestUtil.getResourceFile("config.xlsx").getAbsolutePath()};

        initHomeWithPresetPassword();
        initTestData();
        DatabaseToCsvApp.main(args);

        IOUtils.removeFileOrDir(homeFolder);
    }

    public void initTestData() throws ClassNotFoundException, SQLException {
        File dbFile = new File("/tmp/db_test_432432dfds34458");
        if(dbFile.exists()) {
            dbFile.delete();
        }

        Driver driver = new org.h2.Driver();
        Properties p = new Properties();
        p.put("user", "sa");
        p.put("password", "");
        try(Connection conn = driver.connect("jdbc:h2:/tmp/db_test_432432dfds34458;MODE=Oracle", p);) {
            conn.createStatement().execute("drop table if exists my_user");
            conn.createStatement().execute("create table my_user(username varchar(30), last_update_date date)");

            PreparedStatement stmt = conn.prepareStatement("insert into my_user(username, last_update_date) values(?, ?)");

            int count = 0;
            // Test one million
            for(int i=0; i < 1_005; i++) {
                stmt.setString(1, "user " + i);
                stmt.setDate(2, new Date(new java.util.Date().getTime()));
                stmt.addBatch();
                count++;
                if (count >= 100) {
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
