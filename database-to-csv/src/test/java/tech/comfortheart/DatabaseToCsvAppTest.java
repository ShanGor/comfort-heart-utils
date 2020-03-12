package tech.comfortheart;

import org.junit.Test;
import tech.comfortheart.util.EncryptUtil;
import tech.comfortheart.util.IOUtils;
import tech.comfortheart.util.KeystorePasswordUtil;
import tech.comfortheart.util.SupportedDatabase;

import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
public class DatabaseToCsvAppTest
{
    Logger logger = Logger.getLogger("MainTests");
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


}
