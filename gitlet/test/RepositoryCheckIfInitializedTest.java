package gitlet.test;

import static org.junit.jupiter.api.Assertions.*;

import gitlet.Repository;
import gitlet.test.TestUtils.ExitCapture.NoExitSecurityManager.ExitException;
import org.testng.annotations.Test;

public class RepositoryCheckIfInitializedTest
{
    @Test public void testExistCheckIfInitialized()
    {

        // Check if the .gitlet directory exists
        if (!Repository.GITLET_DIR.exists())
        {
            // If it doesn't exist, create it
            if (!Repository.GITLET_DIR.mkdir())
            {
                fail("Failed to create .gitlet directory.");
            }
        }

        // Capture exit code
        TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();

        // Try to check if initialized
        // Expecting exit code 0
        try
        {
            Repository.checkIfInitialized();
        }
        catch (ExitException e)
        {
            int exitCode = e.getExitCode();
            fail("Expected not to exit, but actually exited with code: " + exitCode);
        }
        finally
        {
            exitCapture.destroy();
        }
    }
    @Test public void testNotExistCheckIfInitialized()
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

        // Capture the console output
        TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();
        // Capture exit code
        TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();

        // Try to check if initialized
        // Expecting exit code 0
        try
        {
            Repository.checkIfInitialized();
            fail("Expected exit with code 0, but not actually exited.");
        }
        catch (ExitException e)
        {
            int exitCode = e.getExitCode();
            assertTrue(consoleCapture.getOutput().contains("Not in an initialized Gitlet directory."),
                       "Unexpected error message: " + consoleCapture.getOutput());
            assertEquals(0, exitCode, "Expected exit code 0.");
        }
        finally
        {
            consoleCapture.destroy();
            exitCapture.destroy();
        }
    }
}
