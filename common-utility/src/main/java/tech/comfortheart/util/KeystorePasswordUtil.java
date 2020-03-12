package tech.comfortheart.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.UUID;

public class KeystorePasswordUtil {
    private static final String XOR_KEY_PATH1 = "pk1";
    private static final String XOR_KEY1_NAME = "ks.k1";
    private static final String XOR_KEY_PATH2 = "pk2";
    private static final String XOR_KEY2_NAME = "ks.k2";
    private static final String XOR_KEY_PATH_SELF = "pk3";
    private static final String XOR_KEY_SELF_NAME = "ks.k3";

    private File keystoreFolder;
    private KeystorePasswordUtil() {}

    public KeystorePasswordUtil(File keystoreFolder) {
        this.keystoreFolder = keystoreFolder;
    }

    public Path getXorKeyPath1() {
        String folder = checkAndCreateFolder(XOR_KEY_PATH1);
        return Paths.get(folder, XOR_KEY1_NAME);
    }

    public Path getXorKeyPath2() {
        String folder = checkAndCreateFolder(XOR_KEY_PATH2);
        return Paths.get(folder, XOR_KEY2_NAME);
    }

    public Path getXorKeyPathSelf() {
        String folder = checkAndCreateFolder(XOR_KEY_PATH_SELF);
        return Paths.get(folder, XOR_KEY_SELF_NAME);
    }

    private String checkAndCreateFolder(String folderName) {
        File folder = new File(this.keystoreFolder, folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return folder.getAbsolutePath();
    }

    /**
     * Generate the key and store password
     */
    public void saveEncryptedStorePassword(String password) {
        Path key1 = getXorKeyPath1();
        Path key2 = getXorKeyPath2();
        Path keySelf = getXorKeyPathSelf();

        try {
            String key = UUID.randomUUID().toString();
            byte[] passwordXor = XorUtils.xorAsBitSet(password, key);
            int length1 = passwordXor.length / 2;
            byte[] passwordSnippet1 = Arrays.copyOfRange(passwordXor, 0, length1);
            byte[] passwordSnippet2 = Arrays.copyOfRange(passwordXor, length1, passwordXor.length);

            Files.write(key1, passwordSnippet1, StandardOpenOption.CREATE);
            Files.write(key2, passwordSnippet2, StandardOpenOption.CREATE);
            Files.write(keySelf, XorUtils.encodeBase64(key.getBytes(StandardCharsets.US_ASCII)), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String retrieveKeystorePassword() throws IOException {
        Path key1 = getXorKeyPath1();
        Path key2 = getXorKeyPath2();
        Path keySelf = getXorKeyPathSelf();

        byte[] keys1 = Files.readAllBytes(key1);
        byte[] keys2 = Files.readAllBytes(key2);
        byte[] keysSelfBase64 = Files.readAllBytes(keySelf);
        String key = new String(XorUtils.decodeBase64(keysSelfBase64), StandardCharsets.US_ASCII);

        String password = XorUtils.XOR(new String(keys1, StandardCharsets.US_ASCII) + new String(keys2, StandardCharsets.US_ASCII), key);
        return password;
    }
}
