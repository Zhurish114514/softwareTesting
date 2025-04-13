package gitlet.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Permission;
import java.util.Optional;

public class TestUtils {
    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteDirectory(child))
                        return false;
                }
            }
        }
        return dir.delete();
    }

    public static boolean createFile(String path, String content) {
        Path filePath = Paths.get(path);

        try {
            Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Optional<String> readFile(String path) {
        Path filePath = Paths.get(path);
        try {
            return Optional.of(new String(Files.readAllBytes(filePath)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static boolean fileExists(String file) {
        Path filePath = Paths.get(file);
        return Files.exists(filePath);
    }


    public static class ConsoleCapture {
        private final PrintStream originalOut = System.out;
        private final PrintStream originalErr = System.err;
        private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

        public ConsoleCapture() {
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        public String getOutput() {
            return outContent.toString();
        }

        public String getError() {
            return errContent.toString();
        }

        public void refreshOutput() {
            outContent.reset();
        }

        public void refreshError() {
            errContent.reset();
        }

        public void refresh() {
            refreshOutput();
            refreshError();
        }

        public void destroy() {
            System.setOut(originalOut);
            System.setErr(originalErr);
            try {
                outContent.close();
                errContent.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static String captureOutput(Runnable runnable) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        try {
            runnable.run();
            return outContent.toString();
        } finally {
            System.setOut(originalOut);
            try {
                outContent.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static String captureError(Runnable runnable) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
        try {
            runnable.run();
            return errContent.toString();
        } finally {
            System.setErr(originalErr);
            try {
                errContent.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static class ExitCapture {
        public static class NoExitSecurityManager extends SecurityManager {
            private final SecurityManager originalSecurityManager;

            public NoExitSecurityManager() {
                this.originalSecurityManager = System.getSecurityManager();
            }

            public static class ExitException extends SecurityException {
                final int exitCode;

                public ExitException(int status) {
                    super("Exit with status: " + status);
                    this.exitCode = status;
                }

                public int getExitCode() {
                    return exitCode;
                }
            }

            @Override
            public void checkExit(int status) {
                throw new ExitException(status);
            }

            @Override
            public Object getSecurityContext() {
                return this.originalSecurityManager == null ? super.getSecurityContext()
                        : this.originalSecurityManager.getSecurityContext();
            }

            @Override
            public void checkPermission(Permission perm) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkPermission(perm);
                }
            }

            @Override
            public void checkPermission(Permission perm, Object context) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkPermission(perm, context);
                }
            }

            @Override
            public void checkCreateClassLoader() {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkCreateClassLoader();
                }
            }

            @Override
            public void checkAccess(Thread t) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkAccess(t);
                }
            }

            @Override
            public void checkAccess(ThreadGroup g) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkAccess(g);
                }
            }

            @Override
            public void checkExec(String cmd) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkExec(cmd);
                }
            }

            @Override
            public void checkLink(String lib) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkLink(lib);
                }
            }

            @Override
            public void checkRead(FileDescriptor fd) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkRead(fd);
                }
            }

            @Override
            public void checkRead(String file) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkRead(file);
                }
            }

            @Override
            public void checkRead(String file, Object context) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkRead(file, context);
                }
            }

            @Override
            public void checkWrite(FileDescriptor fd) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkWrite(fd);
                }
            }

            @Override
            public void checkWrite(String file) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkWrite(file);
                }
            }

            @Override
            public void checkDelete(String file) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkDelete(file);
                }
            }

            @Override
            public void checkConnect(String host, int port) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkConnect(host, port);
                }
            }

            @Override
            public void checkConnect(String host, int port, Object context) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkConnect(host, port, context);
                }
            }

            @Override
            public void checkListen(int port) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkListen(port);
                }
            }

            @Override
            public void checkAccept(String host, int port) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkAccept(host, port);
                }
            }

            @Override
            public void checkMulticast(InetAddress maddr) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkMulticast(maddr);
                }
            }

            @Override
            public void checkPropertiesAccess() {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkPropertiesAccess();
                }
            }

            @Override
            public void checkPropertyAccess(String key) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkPropertyAccess(key);
                }
            }

            @Override
            public void checkPrintJobAccess() {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkPrintJobAccess();
                }
            }

            @Override
            public void checkPackageAccess(String pkg) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkPackageAccess(pkg);
                }
            }

            @Override
            public void checkPackageDefinition(String pkg) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkPackageDefinition(pkg);
                }
            }

            @Override
            public void checkSetFactory() {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkSetFactory();
                }
            }

            @Override
            public void checkSecurityAccess(String target) {
                if (this.originalSecurityManager != null) {
                    this.originalSecurityManager.checkSecurityAccess(target);
                }
            }
        }

        public ExitCapture() {
            System.setSecurityManager(new NoExitSecurityManager());
        }

        public void destroy() {
            SecurityManager sm = System.getSecurityManager();
            if (sm instanceof NoExitSecurityManager) {
                System.setSecurityManager(null);
            }
        }
    }
}
