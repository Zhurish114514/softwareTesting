package gitlet.test;

import gitlet.Commit;
import gitlet.Main;
import gitlet.Repository;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RepositoryMerge {
    @Test
    public void testMergeConflict() {
        // for normal occasions, commit with conflict should be solved correctly
        // Clean up existing repository
        if (Repository.GITLET_DIR.exists()) {
            if (!TestUtils.deleteDirectory(Repository.GITLET_DIR)) {
                fail("Failed to delete existing .gitlet directory.");
            }
        }
        // Initialize repository
        Main.main(new String[]{"init"});
        // Create test commits

        TestUtils.createFile("test.txt", "Hello, Gitlet!");
        Main.main(new String[]{"add", "test.txt"});
        Main.main(new String[]{"commit", "normal commit"});

        // this is the split point of the merge
        Main.main(new String[] {"branch", "new-branch"});

        TestUtils.createFile("test2.txt", "Hello, Gitlet, too!");
        Main.main(new String[]{"add", "test2.txt"});
        Main.main(new String[]{"commit", "another commit"});

        Main.main(new String[]{"checkout", "new-branch"});
        TestUtils.createFile("test2.txt", "Hello!!!!!!, Gitlet, too!");
        Main.main(new String[]{"add", "test2.txt"});
        Main.main(new String[]{"commit", "another commit"});
        // Merge the branches
        Main.main(new String[]{"merge", "master"});
        Commit lastCommit = Repository.readLastCommit();
        // has two parents
        // the merged file has conflict text
        String content = String.valueOf(TestUtils.readFile("test2.txt"));
        assertTrue(lastCommit.isMergeCommit());
        System.out.println(content);
        assertTrue(content.contains("<<<<<<< HEAD"));
        assertTrue(content.contains("Hello, Gitlet, too!"));
        assertTrue(content.contains("======="));
        assertTrue(content.contains("Hello!!!!!!, Gitlet, too!"));
        assertTrue(content.contains(">>>>>>>"));
        TestUtils.deleteTestFiles();
    }

    @Test
    public void testSplitPoint() {
        // the commits have two parents, the original commit and the C1 commit
        // if the code works, the f.txt file should be removed
        if (Repository.GITLET_DIR.exists()) {
            if (!TestUtils.deleteDirectory(Repository.GITLET_DIR)) {
                fail("Failed to delete existing .gitlet directory.");
            }
        }

        // Initialize repository
        Main.main(new String[]{"init"});
        Main.main(new String[] {"branch", "B1"});
        Main.main(new String[] {"branch", "B2"});
        Main.main(new String[] {"checkout", "B1"});
        TestUtils.createFile("hello.txt", "Hello, Gitlet!");
        Main.main(new String[]{"add", "hello.txt"});
        Main.main(new String[]{"commit", "Add h.txt"});
        Main.main(new String[] {"checkout", "B2"});
        TestUtils.createFile("f.txt", "Hello, Gitlet!");
        Main.main(new String[]{"add", "f.txt"});
        Main.main(new String[]{"commit", "f.txt added"});
        Main.main(new String[] {"branch", "C1"});
        TestUtils.createFile("g.txt", "Gitlet!");
        Main.main(new String[]{"add", "g.txt"});
        Main.main(new String[]{"rm", "f.txt"});
        Main.main(new String[]{"commit", "g.txt added, f.txt removed"});
        Main.main(new String[] {"checkout", "B1"});
        Main.main(new String[]{"merge", "C1"});
        Main.main(new String[]{"merge", "B2"});

        String contentG = String.valueOf(TestUtils.readFile("g.txt"));
        String contentH = String.valueOf(TestUtils.readFile("hello.txt"));
        // f.txt should not exist

        assertFalse(TestUtils.fileExists("f.txt"));
        assertTrue(contentG.contains("Gitlet!"));
        assertTrue(contentH.contains("Hello, Gitlet!"));
        TestUtils.deleteTestFiles();

    }
}
