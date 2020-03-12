package tech.comfortheart.app;

import org.junit.Test;
import tech.comfortheart.util.IOUtils;
import tech.comfortheart.util.KeystorePasswordUtil;
import tech.comfortheart.util.TestUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class JobRunnerTest {
    @Test
    public void testMain() throws Exception {
        String[] args = {};
        JobRunner.main(args);

        String testHome = "/tmp/job-runner-test-main-432432dfds344";
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
        JobRunner.main(args);

        /**
         * Encrypt Password
         */
        args = new String[]{"-encrypt", "hey-you"};
        JobRunner.main(args);

        /**
         * Test
         */
        args = new String[]{"LOAD_WECHAT", TestUtil.getResourceFile("Jobs.xlsx").getAbsolutePath()};
        Files.copy(TestUtil.getResourceFile("test-job-runner/test1.sh").toPath(), Paths.get("/tmp/test1.sh"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(TestUtil.getResourceFile("test-job-runner/test2.sh").toPath(), Paths.get("/tmp/test2.sh"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(TestUtil.getResourceFile("test-job-runner/test3.sh").toPath(), Paths.get("/tmp/test3.sh"), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(TestUtil.getResourceFile("test-job-runner/test4.sh").toPath(), Paths.get("/tmp/test4.sh"), StandardCopyOption.REPLACE_EXISTING);
        initHomeWithPresetPassword();
        JobRunner.main(args);

        Files.delete(Paths.get("/tmp/test1.sh"));
        Files.delete(Paths.get("/tmp/test2.sh"));
        Files.delete(Paths.get("/tmp/test3.sh"));
        Files.delete(Paths.get("/tmp/test4.sh"));

        IOUtils.removeFileOrDir(homeFolder);
    }

    public static void initHomeWithPresetPassword() throws Exception {
        String folderPath = System.getProperty("user.home");
        File folder = new File(folderPath);
        if (folder.exists()) {
            IOUtils.removeFileOrDir(folder);
        }
        JobRunner.ALIAS_NAME = "E2E_Alias";

        folder.mkdirs();
        File appId = new File(folder, JobRunner.APP_ID_FILE);
        IOUtils.writeString(appId, "hey");
        File encryptFolder = new File(folder, "hey");
        encryptFolder.mkdir();
        Files.copy(TestUtil.getResourceFile("demo.jks").toPath(), Paths.get(encryptFolder.getAbsolutePath(), JobRunner.KEYSTORE_FILE_NAME));
        Files.copy(TestUtil.getResourceFile("demo.cer").toPath(), Paths.get(encryptFolder.getAbsolutePath(), JobRunner.CERT_FILENAME));
        new KeystorePasswordUtil(encryptFolder).saveEncryptedStorePassword("changeit");
    }
}
