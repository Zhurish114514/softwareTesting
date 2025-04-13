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

public class RepositoryRm {
    @Test
    public void rmUntrackedFile() {
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
        TestUtils.createFile("test2.txt", "Hello, Gitlet, too!");
        TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();
        TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();

        try {
            Main.main(new String[]{"rm", "test2.txt"});
            fail("Expected System.exit(0) to be called");
        } catch (TestUtils.ExitCapture.NoExitSecurityManager.ExitException e) {
            assertEquals(0, e.getExitCode());
            assertTrue(consoleCapture.getOutput().contains("No reason to remove the file."));
        } finally {
            consoleCapture.destroy();
            exitCapture.destroy();
        }
        TestUtils.deleteTestFiles();
    }

    @Test
    public void rmTrackedFile() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
        TestUtils.createFile("test2.txt", "Hello, Gitlet, too!");
        Main.main(new String[]{"add", "test.txt"});
        Main.main(new String[]{"add", "test2.txt"});
        // Commit the changes
        Main.main(new String[]{"commit", "normal commit"});
        Main.main(new String[]{"rm", "test.txt"});
        Method readRemoveStage = Repository.class.getDeclaredMethod("readRemoveStage");
        readRemoveStage.setAccessible(true);
        Stage rmStage = (Stage) readRemoveStage.invoke(null);
        String absolutePath = new File("test.txt").getAbsolutePath();
        assertTrue(rmStage.containsPath(absolutePath));

        // Check if the file is removed from the working directory
        File file = new File("test.txt");
        assertFalse(file.exists());
        TestUtils.deleteTestFiles();
    }

    @Test
    public void rmStagedFile() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
        TestUtils.createFile("test2.txt", "Hello, Gitlet, too!");
        Main.main(new String[]{"add", "test2.txt"});
        Main.main(new String[]{"rm", "test2.txt"});

        String absolutePath = new File("test2.txt").getAbsolutePath();
        Method readAddStage = Repository.class.getDeclaredMethod("readAddStage");
        readAddStage.setAccessible(true);
        Stage adStage = (Stage) readAddStage.invoke(null);
        assertFalse(adStage.containsPath(absolutePath));
        TestUtils.deleteTestFiles();
    }

}
