package tech.comfortheart.app;

import tech.comfortheart.util.EncryptUtil;

import java.io.File;
import java.util.logging.Logger;

/**
 * Run job sequentially. If you want to run job parallelly, use control-m outside.
 */
public class JobRunner {
    private  static Logger log = Logger.getLogger(JobRunner.class.getSimpleName());
    public static final String APP_ID_FILE = ".comfortheart_util";
    public static final String KEYSTORE_FILE_NAME = "comfortheart_util.jks";
    public static final String CERT_FILENAME = "comfortheart_util.cer";
    public static String ALIAS_NAME = "comfortheart_util";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            String path = JobRunner.class.getResource(JobRunner.class.getSimpleName() + ".class").getFile();
            if (path.startsWith("file:")) {
                path = path.substring("file:".length(), path.lastIndexOf('!'));
                log.info( "Usages:");
                System.out.println( "     1: java -jar " + path + " jobs.xslx");
                System.out.println( "     2: java -jar " + path  + " -init <app_id>");
                System.out.println( "     3: java -jar " + path  + " -encrypt your_password");
            } else {
                path = JobRunner.class.getName();
                log.info( "Usages:");
                System.out.println( "     1: java " + path  + " jobs.xslx");
                System.out.println( "     2: java " + path  + " -init <app_id>");
                System.out.println( "     3: java " + path  + " -encrypt your_password");
            }
        } else if (args.length == 2){
            if (args[0].trim().toLowerCase().equals("-encrypt")) {
                EncryptUtil.KeystoreAndCert keyInfo = EncryptUtil.getKeystoreAndCert(APP_ID_FILE, KEYSTORE_FILE_NAME, CERT_FILENAME);
                System.out.println("Encrypted as: " + EncryptUtil.encrypt(keyInfo.getCertPath(), args[1].trim()));
            } else if (args[0].trim().toLowerCase().equals("-init")) {
                EncryptUtil.initKeystore(args[1].trim(), APP_ID_FILE, ALIAS_NAME, KEYSTORE_FILE_NAME, CERT_FILENAME);
            } else {
                String jobGroup = args[0];
                File configFile = new File(args[1]);
                if (!configFile.exists()) {
                    log.severe("Config file does not exist: " + configFile);
                } else {
                    JobRunnerConfig config = new JobRunnerConfig(configFile, "LOAD_WECHAT");
                    config.runJobs();
                }
            }
        }
    }
}
