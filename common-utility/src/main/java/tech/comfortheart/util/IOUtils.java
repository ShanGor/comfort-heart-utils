package tech.comfortheart.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class IOUtils {
    public static final void removeFileOrDir(final String path) throws IOException {
        removeFileOrDir(new File(path));
    }

    public static final void writeString(final File file, final String str) throws IOException {
        BufferedWriter out = Files.newBufferedWriter(file.toPath(), StandardOpenOption.CREATE);
        out.write(str);
        out.flush();
        out.close();
    }

    public static final void removeFileOrDir(final File path) throws IOException {
        if (path.isDirectory()) {
            for (File file: path.listFiles()) {
                removeFileOrDir(file);
            }
        }
        Files.delete(path.toPath());
    }

    /**
     * Utility class to read things from STDIO
     */
    public static class Stdin implements Closeable{
        private BufferedReader in;
        public Stdin() {
            in = new BufferedReader(new InputStreamReader(System.in));
        }

        public String readLine() throws IOException {
            return in.readLine();
        }

        /**
         * Prompt a string, then read the input.
         * @param prompt
         * @return
         * @throws IOException
         */
        public String readLine(String prompt) throws IOException {
            System.out.print(prompt);
            System.out.flush();
            return readLine();
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }
}
