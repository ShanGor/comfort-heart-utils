package tech.comfortheart.util;

import org.junit.Test;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class EncryptUtilTest {
    @Test
    public void testEncrypt() throws Exception {
        String certPath = TestUtil.getResourceFile("demo.cer").getAbsolutePath();
        String encrypted = EncryptUtil.encrypt(certPath, "Are you okay");
        System.out.println(encrypted);
        new EncryptUtil();
    }

    @Test
    public void testDecrypt() throws Exception {
        String encrypted = "adjYtGwFk1S4KQTYiNTUHD0qLJWMzfjLFthGljTnMYf2lMHMVbBzBtms4GjKjkSMDky1fG/si2ZpGQJaxvGyr766asrGOo4NJ6JxQMbK0YJRmEb6hK8eOYk82Bzff7ItO5wYD9ErH/x29gq0sjoxQc9pLIK4kTiVvtkqEp5kqI2Jc6vgnphkIS0JuoKRQYg+IXgK6x0VniReMDXr29jRjPQjyBoFkHfSYpLO5VPHlqeITb2bq/+g0cnwSXv+Hr8aB8NlBFFTLTJMbe1wZjPAHh4BPm/AZsFtz0aZk8tmdwJHgeazOMu1rVNwt39Ls2s7jng5+iVy4sG0T1PwPiXpew==";
        String keystorePath = TestUtil.getResourceFile("demo.jks").getAbsolutePath();

        String decrypted = EncryptUtil.decrypt(keystorePath,
                "E2E_Alias",
                "changeit",
                "changeit", encrypted);
        assert decrypted.equals("Are you okay");
    }

    @Test
    public void testGenerateKeystore() {
        try {
            String password = EncryptUtil.generateKeystore("comfortheart.tech",
                    "main",
                    "comfortheart",
                    "gz",
                    "gd",
                    "CN",
                    2048,
                    730,
                    "test",
                    "/tmp/test.jks",
                    "/tmp/test.cer");

            String encrypted = EncryptUtil.encrypt("/tmp/test.cer", "Are you okay");

            String decrypted = EncryptUtil.decrypt("/tmp/test.jks",
                    "test",
                    password,
                    password, encrypted);
            assertEquals(decrypted, "Are you okay");
            System.out.println("Tested the decryption and encryption successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGenerateRandomPassword() {
        String str = EncryptUtil.generateRandomPassword(10);
        System.out.println(str);
    }

    @Test
    public void testKeystoreAndCert() {
        EncryptUtil.KeystoreAndCert keystoreAndCert = new EncryptUtil.KeystoreAndCert("my_keystorePath", "myCertPath", "hey");
        assert keystoreAndCert.getPassword().equals("hey");
        assert keystoreAndCert.getCertPath().equals("myCertPath");
        assert keystoreAndCert.getKeystorePath().equals("my_keystorePath");
    }

    @Test
    public void testInitKeyStore() throws Exception {
        String folderPath = "/tmp/fhdipafdkja324324sfldsh";
        System.setProperty("user.home", folderPath);
        File folder = new File(folderPath);
        folder.mkdirs();

        StringBuilder sb = new StringBuilder();
        sb.append("myCommonName").append("\r")
                .append("myOU").append("\r")
                .append("myOrg").append("\r")
                .append("myCity").append("\r")
                .append("myState").append("\r")
                .append("myCountry").append("\r");

        InputStream ins = new ByteArrayInputStream(sb.toString().getBytes());
        System.setIn(ins);


        EncryptUtil.initKeystore("hey", ".database_to_csv_app_id", "db2csv", "db2csv.jks", "db2csv.cer");

        String keystorePath = EncryptUtil.getKeystoreAndCert(".database_to_csv_app_id", "db2csv.jks", "db2csv.cer").getKeystorePath();
        String certPath = EncryptUtil.getKeystoreAndCert(".database_to_csv_app_id", "db2csv.jks", "db2csv.cer").getCertPath();
        assertEquals(new File("/tmp/fhdipafdkja324324sfldsh/hey/db2csv.jks").getAbsolutePath(), keystorePath);
        assert certPath.equals(new File("/tmp/fhdipafdkja324324sfldsh/hey/db2csv.cer").getAbsolutePath());
        String password = EncryptUtil.getKeystoreAndCert(".database_to_csv_app_id", "db2csv.jks", "db2csv.cer").getPassword();
        assertEquals(password, new KeystorePasswordUtil(new File(folder, "hey")).retrieveKeystorePassword());

        System.setIn(System.in);

        IOUtils.removeFileOrDir(folder);
    }
}
