package tech.comfortheart.util;

import tech.comfortheart.app.JobRunnerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandRunner {
    private static final Logger log = Logger.getLogger(CommandRunner.class.getSimpleName());

    /**
     * With customized env variables, it will not bring the original system env.
     * @param command
     * @param variables
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static int runCommand(String command, Map<String, Object> variables) throws IOException, InterruptedException {
        Process process;
        if (variables == null) {
            process = Runtime.getRuntime().exec(command);
        } else {
            String[] envp = new String[variables.size()];
            AtomicInteger i = new AtomicInteger(0);
            variables.forEach((key, value) -> {
                envp[i.getAndIncrement()] = String.join("=", key, value.toString());
            });
            process = Runtime.getRuntime().exec(command, envp);
        }
        CountDownLatch latch = new CountDownLatch(2);

        /**
         * Read the output and log.
         */
        new Thread(() -> {
            try {
                readAndLog(process.getInputStream(), Level.INFO);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        }).start();

        /**
         * Read the error and log.
         */
        new Thread(() -> {
            try {
                readAndLog(process.getErrorStream(), Level.SEVERE);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        }).start();

        /**
         * Wait for the process to end.
         */
        int result = process.waitFor();
        /**
         * Ensure all the logs are printed.
         */
        latch.await();

        log.info("All things processed completed");
        return result;
    }

    /**
     * Run command with original system env.
     * @param command
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static int runCommand(String command)  throws IOException, InterruptedException {
        return runCommand(command, null);
    }

    private static final void readAndLog(final InputStream inputStream, final Level level) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        try {
            while ((line = in.readLine()) != null) {
                log.log(level, line);
            }
        } finally {
            in.close();
        }
    }
}
