package gitlet.test;

import gitlet.Commit;
import gitlet.Main;
import gitlet.Repository;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;

import static gitlet.Repository.*;
import static org.junit.Assert.*;

public class RepositoryLog {
    @Test
    public void testLog() {
        // Clean up existing repository
        if (Repository.GITLET_DIR.exists()) {
            if (!TestUtils.deleteDirectory(Repository.GITLET_DIR)) {
                fail("Failed to delete existing .gitlet directory.");
            }
        }

        // Initialize repository
        Main.main(new String[]{"init"});

        // Create test commits
        TestUtils.createFile("test.txt", "Hello, Gitlet!");
        Main.main(new String[]{"add", "test.txt"});
        Main.main(new String[]{"commit", "normal commit"});

        TestUtils.createFile("test2.txt", "Hello, Gitlet, too!");
        Main.main(new String[]{"add", "test2.txt"});
        Main.main(new String[]{"commit", "another commit"});

        TestUtils.createFile("test3.txt", "Hello, Gitlet, too!");
        Main.main(new String[]{"add", "test3.txt"});
        Main.main(new String[]{"commit", "yet another commit"});

        // Capture and verify log output
        TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();
        try {
            Main.main(new String[]{"log"});
            String output = consoleCapture.getOutput();

            // Verify log output contains all commit information
            Commit lastCommit = readLastCommit();
            while (lastCommit != null) {
                assertTrue("Log missing commit message: " + lastCommit.getMessage(),
                        output.contains(lastCommit.getMessage()));
                assertTrue("Log missing commit ID: " + lastCommit.getCommitID(),
                        output.contains("commit " + lastCommit.getCommitID()));
                assertTrue("Log missing timestamp: " + lastCommit.getTimeStamp(),
                        output.contains("Date: " + lastCommit.getTimeStamp()));

                if (lastCommit.isMergeCommit()) {
                    assertTrue("Log missing merge info",
                            output.contains("Merge: " +
                                    lastCommit.getParentCommitIDs().get(0).substring(0, 7) + " " +
                                    lastCommit.getParentCommitIDs().get(1).substring(0, 7)));
                }

                // Move to parent commit
                lastCommit = getParentCommit(lastCommit);
            }
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        } finally {
            consoleCapture.destroy();
        }
    }

    private Commit getParentCommit(Commit commit) throws Exception {
        if (commit.getParents().isEmpty()) {
            return null;
        }
        String parentID = commit.getParents().get(0);
        File parentFile = join(OBJECTS_DIR, parentID);
        return (Commit) readObject(parentFile, Commit.class);
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