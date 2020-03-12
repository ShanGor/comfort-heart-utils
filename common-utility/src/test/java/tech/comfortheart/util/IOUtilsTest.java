package tech.comfortheart.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class IOUtilsTest {

    @Test
    public void deleteFolder() throws IOException {
        File file = new File("/tmp/fjdx43127349280174");
        file.createNewFile();
        IOUtils.removeFileOrDir(file.getAbsolutePath());
        file.createNewFile();
        IOUtils.removeFileOrDir(file);

        file.mkdir();
        new File(file, "hey").createNewFile();
        IOUtils.removeFileOrDir(file);

        new IOUtils();


    }

    @Test
    public void testReadStandardInput() throws IOException {
        InputStream ins = new ByteArrayInputStream("hello".getBytes());
        System.setIn(ins);
        String result = new IOUtils.Stdin().readLine("hello");
        assertEquals("hello", result);
        System.setIn(System.in);
    }

}
