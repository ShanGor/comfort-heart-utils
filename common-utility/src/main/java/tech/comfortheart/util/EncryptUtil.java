package tech.comfortheart.util;


import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import javax.crypto.Cipher;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Use public key to encrypt the password, and private key to decrypt.
 */
public class EncryptUtil {
    private static final Logger logger = Logger.getLogger(EncryptUtil.class.getSimpleName());

    public static final String PADDING = "RSA/ECB/OAEPWithSHA1AndMGF1Padding";
    public static final String STORE_TYPE = "PKCS12";

    private static final PublicKey readPublicKey(final String filename) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        logger.info("Using cert: " + filename);
        try(InputStream ins = new FileInputStream(filename)) {
            X509Certificate cert = (X509Certificate) cf.generateCertificate(ins);
            return cert.getPublicKey();
        }
    }

    private static final PrivateKey readPrivateKey(final String filename, final String storePassword, final String keyPassword, final String alias) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, CertificateException, UnrecoverableKeyException, KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance(STORE_TYPE);

        logger.info("Using keystore: " + filename);
        try(InputStream ins = Files.newInputStream(Paths.get(filename), StandardOpenOption.READ)) {
            keyStore.load(ins, storePassword.toCharArray());
            Key key = keyStore.getKey(alias, keyPassword.toCharArray());
            return (PrivateKey) key;
        }
    }

    /**
     * Encrypt with public key in X.509 format, final as base64 String.
     * @param certPath
     * @param password
     * @return
     * @throws Exception
     */
    public static final String encrypt(final String certPath, final String password) throws Exception {
        PublicKey key = readPublicKey(certPath);

        Cipher cipher = Cipher.getInstance(PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes()));

    }

    /**
     * Decrypt the base64 string with private key in PKCS12 keystore.
     * @param keystorePath
     * @param alias
     * @param storePassword
     * @param keyPassword
     * @param encryptedBase64Str
     * @return
     * @throws Exception
     */
    public static final String decrypt(final String keystorePath,
                                       final String alias,
                                       final String storePassword,
                                       final String keyPassword,
                                       final String encryptedBase64Str) throws Exception {
        PrivateKey key = readPrivateKey(keystorePath, storePassword, keyPassword, alias);
        Cipher cipher = Cipher.getInstance(PADDING);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedBase64Str)));
    }


    /**
     * Generate keystore and self-signed cert.
     * @param commonName
     * @param organizationalUnit
     * @param organization
     * @param city
     * @param state
     * @param country
     * @param keySize
     * @param validity days
     * @param alias
     * @param keyStorePath
     * @param certPath
     * @return the password of the keystore. (The store password equals to the key password)
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static final String generateKeystore(final String commonName,
                                              final String organizationalUnit,
                                              final String organization,
                                              final String city,
                                              final String state,
                                              final String country,
                                              final int keySize,
                                              final int validity,
                                              final String alias,
                                              final String keyStorePath,
                                              final String certPath) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException, InvalidKeyException, SignatureException {
        String keyAndStorePassword = generateRandomPassword(32);

        KeyStore ks = KeyStore.getInstance(STORE_TYPE);
        ks.load(null, null);
        CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA256withRSA", null);
        X500Name x500Name = new X500Name(commonName, organizationalUnit, organization, city, state, country);
        keypair.generate(keySize);

        PrivateKey privateKey = keypair.getPrivateKey();
        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = keypair.getSelfCertificate(x500Name, new Date(), (long)validity*24*60*60);

        // store away the key store
        FileOutputStream fos = new FileOutputStream(keyStorePath);
        ks.setKeyEntry(alias, privateKey, keyAndStorePassword.toCharArray(), chain);
        ks.store(fos, keyAndStorePassword.toCharArray());
        fos.close();
        logger.info("Keystore created successfully!");

        // store away the key store
        fos = new FileOutputStream(certPath);

        fos.write(chain[0].getEncoded());
        fos.close();
        logger.info("Public key cert created successfully!");

        return keyAndStorePassword;
    }

    /**
     * Used to generate the random password; Duplicate the numbers and special tokens, to make them with the same possibility of alphabets.
     */
    private static final String PASSWORD_TOKENS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz012345678901234567890123456789012345678901234567890123456789!@#$%^&*()-_+=|\\][{}!@#$%^&*()-_+=|\\][{}!@#$%^&*()-_+=|\\][{}!@#$%^&*()-_+=|\\][{}";

    /**
     * Generate a random password, with the tokens in PASSWORD_TOKENS;
     * @param len
     * @return
     */
    public static final String generateRandomPassword(final int len) {
        char[] chars = new char[len];
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            int index = Math.abs(random.nextInt()) % PASSWORD_TOKENS.length();
            chars[i] = PASSWORD_TOKENS.charAt(index);
        }

        return new String(chars);
    }

    public static void initKeystore(String appId,
                                    String appIdFileStr,
                                    String alias,
                                    String keystoreFileName,
                                    String certFileName) throws Exception {
        File home = new File(System.getProperty("user.home"));
        File appIdFile = new File(home, appIdFileStr);
        logger.info("AppId file is: " + appIdFile.getAbsolutePath());
        if (appIdFile.exists()) {
            appIdFile.delete();
        }
        logger.info("Writing appId info to the file..");
        Files.write(appIdFile.toPath(), appId.getBytes(), StandardOpenOption.CREATE);
        logger.info("Written!");

        File folder = new File(home, appId);
        if (folder.exists()) {
            tech.comfortheart.util.IOUtils.removeFileOrDir(folder);
        }
        folder.mkdir();

        try(IOUtils.Stdin stdin = new IOUtils.Stdin()) {
            String commonName = stdin.readLine("Common Name: ");
            String organizationUnit = stdin.readLine("Organization Unit: ");
            String organization = stdin.readLine("Organization: ");
            String city = stdin.readLine("City: ");
            String state = stdin.readLine("State: ");
            String country = stdin.readLine("Country: ");

            int keySize = 2048;
            int validity = 730; // Expire after 2 years, however it can still be used.
            File keyStorePath = new File(folder, keystoreFileName);
            File certPath = new File(folder, certFileName);

            logger.info(commonName);

            String password = EncryptUtil.generateKeystore(commonName,
                    organizationUnit,
                    organization,
                    city,
                    state,
                    country,
                    keySize,
                    validity,
                    alias,
                    keyStorePath.getAbsolutePath(),
                    certPath.getAbsolutePath());
            KeystorePasswordUtil keystorePasswordUtil = new KeystorePasswordUtil(folder);
            keystorePasswordUtil.saveEncryptedStorePassword(password);
        }
    }

    /**
     * Get the keystore password
     * @return
     */
    public static final KeystoreAndCert getKeystoreAndCert(
            String appIdFileStr,
            String keystoreFileName,
            String certFileName) throws Exception{
        File home = new File(System.getProperty("user.home"));
        File appIdFile = new File(home, appIdFileStr);
        logger.info("AppId file is: " + appIdFile.getAbsolutePath());
        if (!appIdFile.exists()) {
            throw new RuntimeException("Your application is not initialized! the AppId file not found!");
        }

        String appId = Files.readAllLines(appIdFile.toPath()).get(0).trim();
        File folder = new File(home, appId);
        if (folder.exists()) {KeystorePasswordUtil keystorePasswordUtil = new KeystorePasswordUtil(folder);
            File keyStorePath = new File(folder, keystoreFileName);
            File certPath = new File(folder, certFileName);
            String password = keystorePasswordUtil.retrieveKeystorePassword();
            return new KeystoreAndCert(keyStorePath.getAbsolutePath(), certPath.getAbsolutePath(), password);
        } else {
            throw new RuntimeException("Your application is not initialized! the AppId folder not found!");
        }

    }

    public static class KeystoreAndCert {
        private String keystorePath;
        private String certPath;
        private String password;
        public KeystoreAndCert(String keystorePath, String certPath, String password) {
            setCertPath(certPath);
            setKeystorePath(keystorePath);
            setPassword(password);
        }

        public String getKeystorePath() {
            return keystorePath;
        }

        public void setKeystorePath(String keystorePath) {
            this.keystorePath = keystorePath;
        }

        public String getCertPath() {
            return certPath;
        }

        public void setCertPath(String certPath) {
            this.certPath = certPath;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
