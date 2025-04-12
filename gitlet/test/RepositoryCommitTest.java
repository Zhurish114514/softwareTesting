package gitlet.test;

import gitlet.Main;
import gitlet.Repository;
import gitlet.test.TestUtils.ExitCapture.NoExitSecurityManager.ExitException;
import org.testng.annotations.Test;

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
}
