package gitlet.test;

import gitlet.Commit;
import gitlet.Main;
import gitlet.Repository;
import org.junit.Test;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static gitlet.Repository.*;
import static org.junit.Assert.*;

public class RepositoryGlobalLog {
    @Test
    public void testGlobalLog() {
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
            Main.main(new String[]{"globalLog"});
            String output = consoleCapture.getOutput();
            // Verify log output contains all commit information
            List<String> commitFiles = plainFilenamesIn(OBJECTS_DIR);
            {
                if (commitFiles == null) {
                    fail("No commit files found in the objects directory.");
                }
                for (String idString : commitFiles)
                {
                    try
                    {
                        File commitFile = join(OBJECTS_DIR, idString);
                        Commit commit = readObject(commitFile, Commit.class);
                        assertTrue("Log missing commit message: " + commit.getMessage(),
                                output.contains(commit.getMessage()));
                        assertTrue("Log missing commit ID: " + commit.getCommitID(),
                                output.contains("commit " + commit.getCommitID()));
                        assertTrue("Log missing timestamp: " + commit.getTimeStamp(),
                                output.contains("Date: " + commit.getTimeStamp()));
                        if (commit.isMergeCommit()) {
                            assertTrue("Log missing merge info",
                                    output.contains("Merge: " +
                                            commit.getParentCommitIDs().get(0).substring(0, 7) + " " +
                                            commit.getParentCommitIDs().get(1).substring(0, 7)));
                        }
                    }
                    catch (IllegalArgumentException ignored)
                    {
                    }
                }
            }
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        } finally {
            consoleCapture.destroy();
        }
        // Clean up test files
        TestUtils.deleteTestFiles();
    }

    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    private static final FilenameFilter PLAIN_FILES =
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isFile();
                }
            };


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
