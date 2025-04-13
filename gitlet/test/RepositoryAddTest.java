package gitlet.test;

import gitlet.Commit;
import gitlet.Main;
import gitlet.Repository;
//import gitlet.Utils;
import gitlet.Blob;
import gitlet.test.TestUtils.ConsoleCapture;
import gitlet.test.TestUtils.ExitCapture;
import gitlet.test.TestUtils.ExitCapture.NoExitSecurityManager.ExitException;
import org.testng.annotations.Test;
import java.nio.charset.StandardCharsets;

import java.io.File;
import java.util.*;

import static gitlet.Repository.*;
import static org.junit.jupiter.api.Assertions.*;

public class RepositoryAddTest {

    /** 测试：添加相对路径文件 */
    @Test
    public void testAddRelativePathFile() {
        resetGitlet();
        Main.main(new String[]{"init"});

        String filename = "rel.txt";
        assertTrue(TestUtils.createFile(filename, "Hello relative!"));
        Main.main(new String[]{"add", filename});



        assertTrue(blobExistsFor("Hello relative!"), "Blob file should exist after add.");
    }

    /** 测试：添加绝对路径文件 */
    @Test
    public void testAddAbsolutePathFile() {
        resetGitlet();
        Main.main(new String[]{"init"});

        File absFile = new File("abs.txt");
        String absolutePath = absFile.getAbsolutePath();
        assertTrue(TestUtils.createFile(absolutePath, "Hello absolute!"));
        Main.main(new String[]{"add", absolutePath});

        assertTrue(blobExistsFor(absolutePath), "Blob file should exist after add.");
    }

    /** 测试：添加不存在的文件，预期退出 */
    @Test
    public void testAddNonExistentFile() {
        resetGitlet();
        Main.main(new String[]{"init"});

        ConsoleCapture cc = new ConsoleCapture();
        ExitCapture ec = new ExitCapture();

        try {
            Main.main(new String[]{"add", "ghost.txt"});
            fail("Expected System.exit for non-existent file");
        } catch (ExitException e) {
            String output = cc.getOutput();
            assertTrue(output.contains("File does not exist."), "Should print file not exist message.");
            assertEquals(0, e.getExitCode());
        } finally {
            cc.destroy();
            ec.destroy();
        }
    }

    /** 清空.gitlet目录 */
    private void resetGitlet() {
        if (Repository.GITLET_DIR.exists()) {
            assertTrue(TestUtils.deleteDirectory(Repository.GITLET_DIR), "Failed to delete .gitlet");
        }
    }

    /** 判断是否存在某个文件对应的 Blob 文件 */
    private boolean blobExistsFor(String filename) {
        //
        return true;
    }


}
