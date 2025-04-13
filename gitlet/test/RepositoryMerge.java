package gitlet.test;

import gitlet.Commit;
import gitlet.Main;
import gitlet.Repository;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.testng.Assert.assertTrue;

public class RepositoryMerge {
    @Test
    public void testMergeConflict() {
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
        if (Repository.GITLET_DIR.exists()) {
            if (!TestUtils.deleteDirectory(Repository.GITLET_DIR)) {
                fail("Failed to delete existing .gitlet directory.");
            }
        }

        // Initialize repository
        Main.main(new String[]{"init"});

        // Create initial file (split point)
        TestUtils.createFile("wug.txt", "initial content");
        Main.main(new String[]{"add", "wug.txt"});
        Main.main(new String[]{"commit", "Initial commit"});

        // Create branch B1 and B2
        Main.main(new String[]{"branch", "B1"});
        Main.main(new String[]{"branch", "B2"});

        // Checkout B1 and add h.txt
        Main.main(new String[]{"checkout", "B1"});
        TestUtils.createFile("h.txt", "wug.txt");
        Main.main(new String[]{"add", "h.txt"});
        Main.main(new String[]{"commit", "Add h.txt"});

        // Checkout B2 and add f.txt
        Main.main(new String[]{"checkout", "B2"});
        TestUtils.createFile("f.txt", "wug.txt");
        Main.main(new String[]{"add", "f.txt"});
        Main.main(new String[]{"commit", "f.txt added"});

        // Create branch C1 from B2
        Main.main(new String[]{"branch", "C1"});

        // Modify B2: add g.txt and remove f.txt
        TestUtils.createFile("g.txt", "notwug.txt");
        Main.main(new String[]{"add", "g.txt"});
        Main.main(new String[]{"rm", "f.txt"});
        Main.main(new String[]{"commit", "g.txt added, f.txt removed"});
        Main.main(new String[]{"merge", "B2"});
    }
}
