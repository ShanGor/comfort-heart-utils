package tech.comfortheart.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


public class CommandRunnerTest {
    private static final Logger log = Logger.getLogger(CommandRunnerTest.class.getSimpleName());

    @Test
    public void testRunCommand() throws IOException, InterruptedException {
        File file = TestUtil.getResourceFile("test.sh");
        String command = "sh " + file.getAbsolutePath();
        log.info("running command: " + command);
        CommandRunner.runCommand(command);
    }

    @Test
    public void testRunCommandWithEnv() throws IOException, InterruptedException {
        File file = TestUtil.getResourceFile("test.sh");
        String command = "sh " + file.getAbsolutePath();
        log.info("running command: " + command);
        Map<String, Object> variables = new HashMap<>();
        variables.putAll(System.getenv());
        variables.put("MY_VAR", "My variable");
        CommandRunner.runCommand(command, variables);
    }
}
