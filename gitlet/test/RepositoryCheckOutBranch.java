package gitlet.test;

import antlr.Utils;
import gitlet.Main;
import gitlet.Repository;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.testng.Assert.assertTrue;


public class RepositoryCheckOutBranch {
    @Test
    public void testCheckoutNonExistentBranch() {
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
        TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();
        TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();

        try {
            Main.main(new String[]{"checkout", "non-existent-branch"});
            fail("Expected System.exit(0) to be called");
        } catch (TestUtils.ExitCapture.NoExitSecurityManager.ExitException e) {
            assertEquals(0, e.getExitCode());
            assertTrue(consoleCapture.getOutput().contains("No such branch exists."));
        } finally {
            consoleCapture.destroy();
            exitCapture.destroy();
        }
    }

    @Test
    public void testCheckoutBranch() {
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

        // Create a new branch
        Main.main(new String[]{"branch", "new-branch"});
        // Add a new file and commit it to the default branch (master)
        TestUtils.createFile("hello.txt", "Hello, world!");
        Main.main(new String[]{"add", "hello.txt"});
        Main.main(new String[]{"commit", "added hello.txt"});

        // Checkout the new branch
        Main.main(new String[]{"checkout", "new-branch"});

        // Check if the branch is indeed 'new-branch'
        try {
            Method getCurrentBranch = Repository.class.getDeclaredMethod("getCurrentBranch");
            getCurrentBranch.setAccessible(true);
            String currentBranch = (String) getCurrentBranch.invoke(null);
            assertEquals("The current branch should be 'new-branch'.", currentBranch, "new-branch");
        } catch (NoSuchMethodException e) {
            fail("Method getCurrentBranch not found in Repository class.");
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Verify the expected files in the working directory after switching to 'new-branch'
        assertTrue(TestUtils.fileExists("test.txt"), "File 'test.txt' should exist in the working directory.");
        assertFalse("File 'hello.txt' should not exist in the working directory on 'new-branch'.", TestUtils.fileExists("hello.txt"));
    }

    @Test
    public void testCheckoutBranchWithUncommittedChanges() {
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

        // Create a new branch
        Main.main(new String[]{"branch", "new-branch"});
        // Add a new file and commit it to the default branch (master)
        TestUtils.createFile("hello.txt", "Hello, world!");
        Main.main(new String[]{"add", "hello.txt"});
        Main.main(new String[]{"commit", "hello.txt added"});
        TestUtils.createFile("test.txt", "Hello, world!");
        // Attempt to check out the new branch with uncommitted changes
        TestUtils.ConsoleCapture consoleCapture = new TestUtils.ConsoleCapture();
        TestUtils.ExitCapture exitCapture = new TestUtils.ExitCapture();

        try {
            Main.main(new String[]{"checkout", "new-branch"});
            fail("Expected System.exit(0) to be called");
        } catch (TestUtils.ExitCapture.NoExitSecurityManager.ExitException e) {
            assertEquals(0, e.getExitCode());
            assertTrue(consoleCapture.getOutput().contains("You have uncommitted changes."));
        } finally {
            consoleCapture.destroy();
            exitCapture.destroy();
        }
    }

}
