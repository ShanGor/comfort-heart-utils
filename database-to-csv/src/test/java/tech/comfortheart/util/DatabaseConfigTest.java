package tech.comfortheart.util;

import org.junit.Test;
import tech.comfortheart.DatabaseToCsvApp;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DatabaseConfigTest {
    @Test
    public void testCustomizeSql() {
        DatabaseConfig config = new DatabaseConfig();
        config.setVariable("JDBC_URL", "myJdbc");
        config.setVariable("USERNAME", "myUser");
        config.setVariable("PASSWORD", "my password");
        config.setVariable("DB_TYPE", "MYSQL");
        config.setVariable("CSV_LOCATION", "dont tell you");
        config.setVariable("TABLES", "you, okay");
        config.setVariable("user.sql", "select * from user");
        assertEquals("okay", config.getTables().get(1).getTableName());
        assertEquals("you", config.getTables().get(0).getTableName());
        assertEquals("myJdbc", config.getJdbcUrl());
        assertEquals("myUser", config.getUsername());
        assertEquals("my password", config.getPassword());
        assertEquals("MYSQL", config.getDatabaseType());
        assertEquals("dont tell you", config.getCsvLocation());

        config.setTables("user, test");
        config.customizeSql("user ", "select * from user");

        System.out.println(config.getTables().get(0).getCustomSql());
        assert "select * from user".equals(config.getTables().get(0).getCustomSql());

        assert config.getTables().get(1).getCustomSql() == null;
        config.customizeSql("test ", "select * from test");

        assert "select * from user".equals(config.getTables().get(0).getCustomSql());
        assert "select * from test".equals(config.getTables().get(1).getCustomSql());
    }

    @Test
    public void testMassageVariables() {
        String testStr = "i love $today and $yesterday and $tomorrow, today is '$today'";
        String res = DatabaseConfig.massageVariables(testStr);
        System.out.println(res);

        System.setProperty("businessDate", "20200101");
        testStr = "$yesterday";
        res = DatabaseConfig.massageVariables(testStr);
        assertEquals("20191231", res);
    }

    @Test
    public void stupidTests() {
        new DatabaseConfig.Table().setTableName("hey");
        new DatabaseConfig.Table().setCustomSql("hey");
        List<DatabaseConfig.Table> tableList = new LinkedList<>();
        new DatabaseConfig().setTables(tableList);
    }

    @Test
    public void testSetEncryptedPassword() throws Exception {
        String testHome = "/tmp/job-runner-test-main-432432dfds3445";
        System.setProperty("user.home", testHome);
        File homeFolder = new File(testHome);
        if (homeFolder.exists()) {
            IOUtils.removeFileOrDir(homeFolder);
        }
        homeFolder.mkdirs();
        initHomeWithPresetPassword();

        File encryptFolder = new File(homeFolder, "hey");
        String encryptedText = EncryptUtil.encrypt(new File(encryptFolder, DatabaseToCsvApp.CERT_FILENAME ).getAbsolutePath(), "hey");


        DatabaseConfig config = new DatabaseConfig();
        config.setPassword("{cipher}" + encryptedText);

        assertEquals("hey", config.getPassword());

        IOUtils.removeFileOrDir(homeFolder);
    }


    public static void initHomeWithPresetPassword() throws Exception {
        String folderPath = System.getProperty("user.home");
        File folder = new File(folderPath);
        if (folder.exists()) {
            IOUtils.removeFileOrDir(folder);
        }
        DatabaseToCsvApp.ALIAS_NAME = "E2E_Alias";

        folder.mkdirs();
        File appId = new File(folder, DatabaseToCsvApp.APP_ID_FILE);
        IOUtils.writeString(appId, "hey");
        File encryptFolder = new File(folder, "hey");
        encryptFolder.mkdir();
        Files.copy(TestUtil.getResourceFile("demo.jks").toPath(), Paths.get(encryptFolder.getAbsolutePath(), DatabaseToCsvApp.KEYSTORE_FILE_NAME));
        Files.copy(TestUtil.getResourceFile("demo.cer").toPath(), Paths.get(encryptFolder.getAbsolutePath(), DatabaseToCsvApp.CERT_FILENAME));
        new KeystorePasswordUtil(encryptFolder).saveEncryptedStorePassword("changeit");
    }
}
