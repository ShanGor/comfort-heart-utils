package tech.comfortheart.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

public class KeystorePasswordUtilTest {
    @Test
    public void testSaveEncryptedStorePassword() throws IOException {
        File file = new File("/tmp/fueqfhdsan438127534");
        file.mkdir();
        KeystorePasswordUtil util = new KeystorePasswordUtil(file);
        util.saveEncryptedStorePassword("hey");

        assertEquals("ks.k1", util.getXorKeyPath1().toFile().getName());
        assertEquals("ks.k2", util.getXorKeyPath2().toFile().getName());
        assertEquals("ks.k3", util.getXorKeyPathSelf().toFile().getName());
        assertEquals("hey", util.retrieveKeystorePassword());

        IOUtils.removeFileOrDir(file);
    }
}
