package gitlet.test;

import antlr.Utils;
import gitlet.Main;
import gitlet.Repository;
import gitlet.Stage;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.testng.Assert.assertTrue;

public class RepositoryStatus {
    @Test
    public void testStatus() {
        // Remove the .gitlet directory if it exists
        if (Repository.GITLET_DIR.exists()) {
            if (!TestUtils.deleteDirectory(Repository.GITLET_DIR)) {
                fail("Failed to delete existing .gitlet directory.");
            }
        }
        // Initialize the repository
        Main.main(new String[]{"init"});
        Main.main(new String[] {"branch", "other-branch"});
        // Create files in the repository
        TestUtils.createFile("wug.txt", "Hello1, Gitlet!");
        TestUtils.createFile("wug2.txt", "Hello2, Gitlet!");
        TestUtils.createFile("wug3.txt", "Hello2, Gitlet!");
        TestUtils.createFile("goodbye.txt", "goodbye");
        TestUtils.createFile("junk.txt", "junk");
        Main.main(new String[]{"add", "wug.txt"});
        Main.main(new String[]{"add", "wug2.txt"});
        Main.main(new String[]{"add", "wug3.txt"});
        Main.main(new String[]{"add", "goodbye.txt"});
        Main.main(new String[]{"add", "junk.txt"});
        // Commit the changes
        Main.main(new String[]{"commit", "normal commit"});
        // make changes to the files
        TestUtils.createFile("random.stuff", "1234143");
        TestUtils.deleteFile("wug3.txt");
        TestUtils.createFile("wug3.txt", "Hello, Gitlet!");
        TestUtils.deleteFile("junk.txt");
        Main.main(new String[]{"rm", "goodbye.txt"});

        // Capture the console output
        TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();
        TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();
        try {
            Main.main(new String[]{"status"});
            // Check the console output
            String output = consoleCapture.getOutput();
            assertTrue(output.contains("=== Branches ==="));
            assertTrue(output.contains("*master"));
            assertTrue(output.contains("other-branch"));
            assertTrue(output.contains("=== Staged Files ==="));
            assertTrue(output.contains("=== Removed Files ==="));
            assertTrue(output.contains("goodbye.txt"));
            assertTrue(output.contains("=== Modifications Not Staged For Commit ==="));
            assertTrue(output.contains("junk.txt (deleted)"));
            assertTrue(output.contains("wug3.txt (modified)"));
            assertTrue(output.contains("=== Untracked Files ==="));
            assertTrue(output.contains("random.stuff"));
        } catch (TestUtils.ExitCapture.NoExitSecurityManager.ExitException e) {
            fail("Expected System.exit(0) to be called");
        } finally {
            consoleCapture.destroy();
            exitCapture.destroy();
        }
        TestUtils.deleteTestFiles();
    }
}
