package gitlet.test;

import gitlet.Commit;
import gitlet.Main;
import gitlet.Repository;
import gitlet.test.TestUtils.ExitCapture.NoExitSecurityManager.ExitException;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;


public class RepositoryResetTest {
    @Test
    public void testInvalidCommitIDReset() {
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
        // Commit the changes
        Main.main(new String[]{"commit", "normal commit"});

        try {


            Method readLastCommit = Repository.class.getDeclaredMethod("readLastCommit");
            readLastCommit.setAccessible(true);

            // Get the last commit
            Commit lastCommit = (Commit) readLastCommit.invoke(null);

            // Get the commit ID
            String commitId = lastCommit.getCommitID();

            // Change the content of the file
            TestUtils.createFile("test.txt", "Hello, Gitlet! Updated!");

            // Add the changes
            Main.main(new String[]{"add", "test.txt"});

            // Commit the changes
            Main.main(new String[]{"commit", "updated commit"});

            // Create an invalid commit ID
            String invalidCommitId = commitId.substring(0, commitId.length() - 1) + "X";

            // Capture the console output
            TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();

            // Capture exit code
            TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();

            try {
                // Reset to the invalid commit ID
                Main.main(new String[]{"reset", invalidCommitId});
                fail("Expected exit with code 0, but not actually exited.");

            } catch (ExitException e) {

                assertTrue(consoleCapture.getOutput().contains("No commit with that id exists."), "Unexpected error message: " + consoleCapture.getOutput());
                // Check if the exit code is 0
                assertEquals(0, e.getExitCode(), "Expected exit code 0, but actually exited with code: " + e.getExitCode());

            } finally {
                consoleCapture.destroy();
                exitCapture.destroy();
            }
        } catch (NoSuchMethodException e) {
            fail("Method readLastCommit not found in Repository class.");
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUntrackedFileReset() {
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
        // Commit the changes
        Main.main(new String[]{"commit", "normal commit"});

        try {


            Method readLastCommit = Repository.class.getDeclaredMethod("readLastCommit");
            readLastCommit.setAccessible(true);

            // Get the last commit
            Commit lastCommit = (Commit) readLastCommit.invoke(null);

            // Get the commit ID
            String commitId = lastCommit.getCommitID();

            // Remove the file from the working directory
            Main.main(new String[]{"rm", "test.txt"});

            // Commit the changes
            Main.main(new String[]{"commit", "removed test.txt"});

            // Create an untracked file
            TestUtils.createFile("test.txt", "Hello, Gitlet! Updated!");


            // Capture the console output
            TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();

            // Capture exit code
            TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();

            try {
                // Checkout the commit
                Main.main(new String[]{"reset", commitId});
                fail("Expected exit with code 0, but not actually exited.");

            } catch (ExitException e) {

                assertTrue(consoleCapture.getOutput().contains("There is an untracked file in the way; delete it, or add and commit it first."), "Unexpected error message: " + consoleCapture.getOutput());
                // Check if the exit code is 0
                assertEquals(0, e.getExitCode(), "Expected exit code 0, but actually exited with code: " + e.getExitCode());

            } finally {
                consoleCapture.destroy();
                exitCapture.destroy();
            }
        } catch (NoSuchMethodException e) {
            fail("Method readLastCommit not found in Repository class.");
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
