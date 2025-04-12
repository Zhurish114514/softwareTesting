package gitlet.test;

import static org.junit.jupiter.api.Assertions.*;

import gitlet.Main;
import gitlet.Repository;
import gitlet.test.TestUtils;
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
    }
}
