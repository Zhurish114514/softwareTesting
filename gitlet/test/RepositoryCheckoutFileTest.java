package gitlet.test;

import gitlet.Main;
import gitlet.Repository;
import gitlet.Commit;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryCheckoutFileTest {
    @Test
    public void testCheckoutFileSuccess() throws Exception {
        if (Repository.GITLET_DIR.exists()) {
            TestUtils.deleteDirectory(Repository.GITLET_DIR);
        }

        Main.main(new String[]{"init"});
        TestUtils.createFile("a.txt", "initial");
        Main.main(new String[]{"add", "a.txt"});
        Main.main(new String[]{"commit", "v1"});

        Method method = Repository.class.getDeclaredMethod("readLastCommit");
        method.setAccessible(true);
        Commit commit = (Commit) method.invoke(null);
        String commitId = commit.getCommitID();

        TestUtils.createFile("a.txt", "modified");
        Main.main(new String[]{"add", "a.txt"});
        Main.main(new String[]{"commit", "v2"});

        Main.main(new String[]{"checkout", commitId, "--", "a.txt"});
        Optional<String> content = TestUtils.readFile("a.txt");
        assertEquals("initial", content.orElse(""));
    }

    @Test
    public void testCheckoutWithWrongFormat() {
        if (Repository.GITLET_DIR.exists()) {
            TestUtils.deleteDirectory(Repository.GITLET_DIR);
        }

        Main.main(new String[]{"init"});
        TestUtils.ConsoleCapture cc = new TestUtils.ConsoleCapture();
        TestUtils.ExitCapture ec = new TestUtils.ExitCapture();

        try {
            Main.main(new String[]{"checkout", "wrong", "a.txt"});
        } catch (TestUtils.ExitCapture.NoExitSecurityManager.ExitException e) {
            assertTrue(cc.getOutput().contains("Incorrect operands"));
        } finally {
            cc.destroy();
            ec.destroy();
        }
    }

    @Test
    public void testCheckoutFileNotExistInCommit() throws Exception {
        if (Repository.GITLET_DIR.exists()) {
            TestUtils.deleteDirectory(Repository.GITLET_DIR);
        }

        Main.main(new String[]{"init"});
        TestUtils.createFile("a.txt", "aaa");
        Main.main(new String[]{"add", "a.txt"});
        Main.main(new String[]{"commit", "first"});

        Method method = Repository.class.getDeclaredMethod("readLastCommit");
        method.setAccessible(true);
        Commit commit = (Commit) method.invoke(null);
        String commitId = commit.getCommitID();

        TestUtils.ConsoleCapture cc = new TestUtils.ConsoleCapture();
        TestUtils.ExitCapture ec = new TestUtils.ExitCapture();

        try {
            Main.main(new String[]{"checkout", commitId, "--", "notfound.txt"});
        } catch (TestUtils.ExitCapture.NoExitSecurityManager.ExitException e) {
            assertTrue(cc.getOutput().contains("File does not exist in that commit."));
        } finally {
            cc.destroy();
            ec.destroy();
        }
    }
}
