package gitlet.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

public class TestUtils
{
    public static boolean deleteDirectory(File dir)
    {
        if (dir.isDirectory())
        {
            File[] children = dir.listFiles();
            if (children != null)
            {
                for (File child : children)
                {
                    if (!deleteDirectory(child))
                        return false;
                }
            }
        }
        return dir.delete();
    }

    public static class ConsoleCapture
    {
        private final PrintStream originalOut = System.out;
        private final PrintStream originalErr = System.err;
        private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

        public ConsoleCapture()
        {
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        public String getOutput()
        {
            return outContent.toString();
        }

        public String getError()
        {
            return errContent.toString();
        }

        public void destroy()
        {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
}
