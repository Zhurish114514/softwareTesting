package gitlet.test;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Permission;
import java.util.Optional;

public class TestUtils
{
    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                 | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }
    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }
    public static void deleteTestFiles() {
        File currentDir = new File(System.getProperty("user.dir"));
        File[] txtFiles = currentDir.listFiles((dir, name) -> name.endsWith(".txt"));
        File[] stuffFiles = currentDir.listFiles((dir, name) -> name.endsWith(".stuff"));

        if (txtFiles != null) {
            for (File file : txtFiles) {
                if (!file.delete()) {
                    System.err.println("Failed to delete: " + file.getPath());
                }
            }
        }
        if (stuffFiles != null) {
            for (File file : stuffFiles) {
                if (!file.delete()) {
                    System.err.println("Failed to delete: " + file.getPath());
                }
            }
        }
    }
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

    public static boolean createFile(String path, String content)
    {
        Path filePath = Paths.get(path);

        try
        {
            Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteFile(String path) {
        Path filePath = Paths.get(path);
        try
        {
            return Files.deleteIfExists(filePath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static Optional<String> readFile(String path)
    {
        Path filePath = Paths.get(path);
        try
        {
            return Optional.of(new String(Files.readAllBytes(filePath)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static boolean fileExists(String file) {
        Path filePath = Paths.get(file);
        return Files.exists(filePath);
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

        public void refreshOutput()
        {
            outContent.reset();
        }

        public void refreshError()
        {
            errContent.reset();
        }

        public void refresh()
        {
            refreshOutput();
            refreshError();
        }

        public void destroy()
        {
            System.setOut(originalOut);
            System.setErr(originalErr);
            try
            {
                outContent.close();
                errContent.close();
            }
            catch (Exception ignored)
            {
            }
        }
    }

    public static String captureOutput(Runnable runnable)
    {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        try
        {
            runnable.run();
            return outContent.toString();
        }
        finally
        {
            System.setOut(originalOut);
            try
            {
                outContent.close();
            }
            catch (Exception ignored)
            {
            }
        }
    }

    public static String captureError(Runnable runnable)
    {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        try
        {
            runnable.run();
            return errContent.toString();
        }
        finally
        {
            System.setErr(originalErr);
            try
            {
                errContent.close();
            }
            catch (Exception ignored)
            {
            }
        }
    }

    public static class ExitCapture
    {
        public static class NoExitSecurityManager extends SecurityManager
        {
            private final SecurityManager originalSecurityManager;
            public NoExitSecurityManager()
            {
                this.originalSecurityManager = System.getSecurityManager();
            }

            public static class ExitException extends SecurityException
            {
                final int exitCode;
                public ExitException(int status)
                {
                    super("Exit with status: " + status);
                    this.exitCode = status;
                }
                public int getExitCode()
                {
                    return exitCode;
                }
            }
            @Override public void checkExit(int status)
            {
                throw new ExitException(status);
            }

            public Object getSecurityContext()
            {
                return this.originalSecurityManager == null ? super.getSecurityContext()
                                                            : this.originalSecurityManager.getSecurityContext();
            }

            public void checkPermission(Permission perm)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkPermission(perm);
                }
            }

            public void checkPermission(Permission perm, Object context)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkPermission(perm, context);
                }
            }

            public void checkCreateClassLoader()
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkCreateClassLoader();
                }
            }

            public void checkAccess(Thread t)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkAccess(t);
                }
            }

            public void checkAccess(ThreadGroup g)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkAccess(g);
                }
            }

            public void checkExec(String cmd)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkExec(cmd);
                }
            }

            public void checkLink(String lib)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkLink(lib);
                }
            }

            public void checkRead(FileDescriptor fd)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkRead(fd);
                }
            }

            public void checkRead(String file)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkRead(file);
                }
            }

            public void checkRead(String file, Object context)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkRead(file, context);
                }
            }

            public void checkWrite(FileDescriptor fd)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkWrite(fd);
                }
            }

            public void checkWrite(String file)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkWrite(file);
                }
            }

            public void checkDelete(String file)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkDelete(file);
                }
            }

            public void checkConnect(String host, int port)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkConnect(host, port);
                }
            }

            public void checkConnect(String host, int port, Object context)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkConnect(host, port, context);
                }
            }

            public void checkListen(int port)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkListen(port);
                }
            }

            public void checkAccept(String host, int port)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkAccept(host, port);
                }
            }

            public void checkMulticast(InetAddress maddr)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkMulticast(maddr);
                }
            }

            public void checkPropertiesAccess()
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkPropertiesAccess();
                }
            }

            public void checkPropertyAccess(String key)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkPropertyAccess(key);
                }
            }

            public void checkPrintJobAccess()
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkPrintJobAccess();
                }
            }

            public void checkPackageAccess(String pkg)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkPackageAccess(pkg);
                }
            }

            public void checkPackageDefinition(String pkg)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkPackageDefinition(pkg);
                }
            }

            public void checkSetFactory()
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkSetFactory();
                }
            }

            public void checkSecurityAccess(String target)
            {
                if (this.originalSecurityManager != null)
                {
                    this.originalSecurityManager.checkSecurityAccess(target);
                }
            }
        }

        public ExitCapture()
        {
            System.setSecurityManager(new NoExitSecurityManager());
        }
        public void destroy()
        {
            SecurityManager sm = System.getSecurityManager();
            if (sm instanceof NoExitSecurityManager)
            {
                System.setSecurityManager(null);
            }
        }
    }
}
