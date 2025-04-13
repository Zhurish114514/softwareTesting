package gitlet.test;

import gitlet.Main;
import gitlet.Repository;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryBranchTest {
    @Test
    public void testCreateBranchSuccessfully() {
        if (Repository.GITLET_DIR.exists()) {
            TestUtils.deleteDirectory(Repository.GITLET_DIR);
        }

        Main.main(new String[]{"init"});
        Main.main(new String[]{"branch", "dev"});

        java.io.File branchFile = new java.io.File(Repository.REFS_DIR, "dev");
        assertTrue(branchFile.exists(), "Branch file should be created.");
    }

    @Test
    public void testCreateExistingBranch() {
        if (Repository.GITLET_DIR.exists()) {
            TestUtils.deleteDirectory(Repository.GITLET_DIR);
        }

        Main.main(new String[]{"init"});
        Main.main(new String[]{"branch", "dev"});

        TestUtils.ConsoleCapture cc = new TestUtils.ConsoleCapture();
        TestUtils.ExitCapture ec = new TestUtils.ExitCapture();

        try {
            Main.main(new String[]{"branch", "dev"});
        } catch (TestUtils.ExitCapture.NoExitSecurityManager.ExitException e) {
            assertTrue(cc.getOutput().contains("A branch with that name already exists."));
        } finally {
            cc.destroy();
            ec.destroy();
        }
    }
}
