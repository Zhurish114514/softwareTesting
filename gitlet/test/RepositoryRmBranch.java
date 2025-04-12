package gitlet.test;

import gitlet.Main;
import gitlet.Repository;
import gitlet.test.TestUtils.ExitCapture.NoExitSecurityManager.ExitException;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryRmBranch {
    @Test
    public static void testNormalRmBranch() {
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
            Main.main(new String[]{"rm-branch", "master"});
            fail("Expected exit with code 0, but not actually exited.");
        } catch (ExitException e) {
            int exitCode = e.getExitCode();
            assertTrue(consoleCapture.getOutput().contains("Cannot remove the current branch."),
                    "Expected error message not found.");
            assertEquals(0, exitCode, "Expected exit code 0, but got: " + exitCode);
        } finally {
            consoleCapture.destroy();
            exitCapture.destroy();
        }
    }
}
