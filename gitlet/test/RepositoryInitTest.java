package gitlet.test;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.*;

import gitlet.Main;
import gitlet.Repository;
import org.testng.annotations.Test;

public class RepositoryInitTest
{
    @Test public void testNormalInit()
    {
        // Remove the .gitlet directory if it exists
        if (Repository.GITLET_DIR.exists())
        {
            if (!TestUtils.deleteDirectory(Repository.GITLET_DIR))
            {
                fail("Failed to delete existing .gitlet directory.");
            }
        }
        // Initialize the repository
        Main.main(new String[] {"init"});
        // Check if the .gitlet directory exists
        assertTrue(Repository.GITLET_DIR.exists() && Repository.GITLET_DIR.isDirectory(),
                   "Failed to create .gitlet directory.");
        assertTrue(Repository.OBJECTS_DIR.exists() && Repository.OBJECTS_DIR.isDirectory(),
                   "Failed to create objects directory.");
        assertTrue(Repository.REFS_DIR.exists() && Repository.REFS_DIR.isDirectory(),
                   "Failed to create refs directory.");
        assertTrue(Repository.HEADS_DIR.exists() && Repository.HEADS_DIR.isDirectory(),
                   "Failed to create heads directory.");
        assertTrue(Repository.HEAD_FILE.exists() && Repository.HEAD_FILE.isFile(), "Failed to create HEAD file.");
    }

    @Test public void testInitWithExistingDirectory()
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
        // Capture the console output
        TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();

        // Try to initialize the repository again
        // Expecting exit code 0
        try
        {
            int exitCode = catchSystemExit(() -> Main.main(new String[] {"init"}));
            // Check if the error message is as expected
            assertTrue(consoleCapture.getOutput().contains(
                           "A Gitlet version-control system already exists in the current directory."),
                       "Unexpected error message: " + consoleCapture.getOutput());
            assertEquals(0, exitCode, "Expected exit code 0.");
        }
        catch (Exception e)
        {
            fail("Expected exit with code 0, but not actually exited.");
        }
        finally
        {
            consoleCapture.destroy();
        }
    }
}
