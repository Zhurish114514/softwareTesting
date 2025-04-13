package gitlet.test;

import gitlet.Blob;
import gitlet.Commit;
import gitlet.Main;
import gitlet.Repository;
import gitlet.test.TestUtils.ExitCapture.NoExitSecurityManager.ExitException;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryCommitTest {
    @Test
    public void testNormalCommit() {
        // Remove the .gitlet directory if it exists
        if (Repository.GITLET_DIR.exists()) {
            if (!TestUtils.deleteDirectory(Repository.GITLET_DIR)) {
                fail("Failed to delete existing .gitlet directory.");
            }
        }

        // Initialize the repository
        Main.main(new String[]{"init"});
        // Create a new file in the repository
        TestUtils.createFile("test.txt", "Hello, Gitlet!");
        Main.main(new String[]{"add", "test.txt"});

        // Capture exit code
        TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();

        try {
            // Commit the changes
            Main.main(new String[]{"commit", "normal commit"});

            // Check if the commit was successful
            List<String> commitFiles = plainFilenamesIn(Repository.OBJECTS_DIR);
            if (commitFiles == null) {
                fail("No commit files found in the objects directory.");
            }
            for (String idString : commitFiles) {
                try {
                    File commitFile = join(Repository.OBJECTS_DIR, idString);
                    Commit commit = readObject(commitFile, Commit.class);
                    if (commit.getMessage().equals("normal commit")) {
                        Path filePath = Paths.get("test.txt");
                        Blob blob = commit.getBlobByPath(filePath.toAbsolutePath().toString());
                        if (blob != null) {
                            // Check if the blob content matches the original file content
                            String content = new String(blob.getContent());
                            assertEquals("Hello, Gitlet!", content, "File content does not match.");
                        } else {
                            fail("Blob not found in the commit.");
                        }
                        return;
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            fail("Commit not found in the objects directory.");
        } catch (ExitException e) {
            fail("Expected not to exit, but actually exited with code: " + e.getExitCode());
        } finally {
            exitCapture.destroy();
        }
    }

    @Test
    public void testNotAddCommit() {
        // Remove the .gitlet directory if it exists
        if (Repository.GITLET_DIR.exists()) {
            if (!TestUtils.deleteDirectory(Repository.GITLET_DIR)) {
                fail("Failed to delete existing .gitlet directory.");
            }
        }

        // Initialize the repository
        Main.main(new String[]{"init"});

        // Capture the console output
        TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();

        // Capture exit code
        TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();

        try {
            // Try to commit without adding any files
            Main.main(new String[]{"commit", "not add commit"});
            fail("Expected exit with code 0, but not actually exited.");
        } catch (ExitException e) {
            assertTrue(consoleCapture.getOutput().contains("No changes added to the commit."),
                    "Unexpected error message: " + consoleCapture.getOutput());
            // Check if the exit code is 0
            assertEquals(0, e.getExitCode(), "Expected exit code 0, but actually exited with code: " + e.getExitCode());
        } finally {
            consoleCapture.destroy();
            exitCapture.destroy();
        }
    }

    private static final FilenameFilter PLAIN_FILES =
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isFile();
                }
            };

    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }

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
}
