package tech.comfortheart.util;

import org.junit.Test;

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
    }

    @Test
    public void stupidTests() {
        new DatabaseConfig.Table().setTableName("hey");
        new DatabaseConfig.Table().setCustomSql("hey");

    }
}
