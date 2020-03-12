package tech.comfortheart.app;

import org.junit.Test;
import tech.comfortheart.util.IOUtils;
import tech.comfortheart.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class JobRunnerConfigTest {
    private static Logger log = Logger.getLogger(JobRunnerConfigTest.class.getSimpleName());
    @Test
    public void testJobRunnerConfig() throws Exception {
        String folderPath = "/tmp/fhdipafdkja324324sfldshfrewq2";
        System.setProperty("user.home", folderPath);
        JobRunner.ALIAS_NAME = "E2E_Alias";

        File folder = new File(folderPath);
        JobRunnerTest.initHomeWithPresetPassword();

        JobRunnerConfig.JobRow row = new JobRunnerConfig.JobRow();
        TestUtil.testGetterAndSetters(row);

        File jobsExcel = TestUtil.getResourceFile("Jobs.xlsx");
        JobRunnerConfig config = new JobRunnerConfig(jobsExcel, "LOAD_WECHAT");
        config.getJobs().forEach(job -> {
            log.info(job.toString());
        });

        try {
            Files.copy(TestUtil.getResourceFile("test-job-runner/test1.sh").toPath(), Paths.get("/tmp/test1.sh"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(TestUtil.getResourceFile("test-job-runner/test2-fail.sh").toPath(), Paths.get("/tmp/test2.sh"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(TestUtil.getResourceFile("test-job-runner/test3-fail.sh").toPath(), Paths.get("/tmp/test3.sh"), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(TestUtil.getResourceFile("test-job-runner/test4.sh").toPath(), Paths.get("/tmp/test4.sh"), StandardCopyOption.REPLACE_EXISTING);

            // Test with failure version
            try {
                config.runJobs();
            } catch (Exception e) {
                log.info("First time, expect failure");
            }

            // Test again with normal version
            Files.delete(Paths.get("/tmp/test2.sh"));
            Files.copy(TestUtil.getResourceFile("test-job-runner/test2.sh").toPath(), Paths.get("/tmp/test2.sh"));

            // Test with failure version
            try {
                config = new JobRunnerConfig(jobsExcel, "LOAD_WECHAT");
                config.runJobs();
            } catch (Exception e) {
                log.info("Second time, expect no failure");
            }

            Files.delete(Paths.get("/tmp/test3.sh"));
            Files.copy(TestUtil.getResourceFile("test-job-runner/test3.sh").toPath(), Paths.get("/tmp/test3.sh"));

            config = new JobRunnerConfig(jobsExcel, "LOAD_WECHAT");
            config.runJobs();


        } finally {
            Files.delete(Paths.get("/tmp/test1.sh"));
            Files.delete(Paths.get("/tmp/test2.sh"));
            Files.delete(Paths.get("/tmp/test3.sh"));
            Files.delete(Paths.get("/tmp/test4.sh"));
        }

        IOUtils.removeFileOrDir(folder);
    }

    @Test
    public void testCommandEnv() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("sh", TestUtil.getResourceFile("test-job-runner/test4.sh").getAbsolutePath(), "$USERNAME");
        pb.environment().put("USERNAME", "my-username");
        pb.environment().put("PASSWORD", "my-password");
        pb.redirectErrorStream(true);
        pb.redirectOutput(new File("/tmp/hey.txt"));

        pb.start().waitFor();
    }
}
