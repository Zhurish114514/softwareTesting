package gitlet.test;

import static org.junit.jupiter.api.Assertions.*;

import gitlet.Main;
import gitlet.Repository;
import gitlet.test.TestUtils.ExitCapture.NoExitSecurityManager.ExitException;
import org.testng.annotations.Test;

public class RepositoryFindTest
{
    @Test public void testNotExitMessageFind()
    {
        // Check if the .gitlet directory exists
        if (Repository.GITLET_DIR.exists())
        {
            // If it doesn't exist, create it
            if (!TestUtils.deleteDirectory(Repository.GITLET_DIR))
            {
                fail("Failed to delete existing .gitlet directory.");
            }
        }

        // Initialize the repository
        Main.main(new String[] {"init"});

        // Create a new file in the repository
        TestUtils.createFile("test.txt", "Hello, Gitlet!");
        Main.main(new String[] {"add", "test.txt"});

        // Commit the changes
        Main.main(new String[] {"commit", "normal commit"});

        // Capture the console output
        TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();
        // Capture exit code
        TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();

        try
        {
            // Try to find a commit
            Main.main(new String[] {"find", "missing commit"});
            fail("Expected to exit with code 0, but actually did not.");
        }
        catch (ExitException e)
        {
            int exitCode = e.getExitCode();
            assertTrue(consoleCapture.getOutput().contains("Found no commit with that message."),
                       "Expected error message not found.");
            assertEquals(0, exitCode, "Expected exit code 0, but got: " + exitCode);
        }
        finally
        {
            consoleCapture.destroy();
            exitCapture.destroy();
        }
    }
}
