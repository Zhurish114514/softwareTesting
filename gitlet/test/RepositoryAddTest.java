package gitlet.test;

import gitlet.Blob;
import gitlet.Commit;
import gitlet.Main;
import gitlet.Repository;
import gitlet.test.TestUtils.ConsoleCapture;
import gitlet.test.TestUtils.ExitCapture;
import gitlet.test.TestUtils.ExitCapture.NoExitSecurityManager.ExitException;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static gitlet.Repository.OBJECTS_DIR;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

public class RepositoryAddTest {

    /**
     * 测试：添加相对路径文件
     */
    @Test
    public void testAddRelativePathFile() {
        resetGitlet();
        Main.main(new String[]{"init"});

        String filename = "rel.txt";
        assertTrue(TestUtils.createFile(filename, "Hello relative!"));
        Main.main(new String[]{"add", filename});


        assertTrue("Blob file should exist after add.", blobExistsFor(filename));
    }

    /**
     * 测试：添加绝对路径文件
     */
    @Test
    public void testAddAbsolutePathFile() {
        resetGitlet();
        Main.main(new String[]{"init"});

        File absFile = new File("abs.txt");
        String absolutePath = absFile.getAbsolutePath();
        assertTrue(TestUtils.createFile(absolutePath, "Hello absolute!"));
        Main.main(new String[]{"add", absolutePath});

        assertTrue("Blob file should exist after add.", blobExistsFor(absolutePath));
    }

    /**
     * 测试：添加不存在的文件，预期退出
     */
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
            assertTrue("Should print file not exist message.", output.contains("File does not exist."));
            assertEquals(0, e.getExitCode());
        } finally {
            cc.destroy();
            ec.destroy();
        }
    }

    /**
     * 清空.gitlet目录
     */
    private void resetGitlet() {
        if (Repository.GITLET_DIR.exists()) {
            assertTrue("Failed to delete .gitlet", TestUtils.deleteDirectory(Repository.GITLET_DIR));
        }
    }

    /**
     * 判断是否存在某个文件对应的 Blob 文件
     */
    private boolean blobExistsFor(String filename) {
        //
        List<String> blobFiles = plainFilenamesIn(OBJECTS_DIR);

        if (blobFiles == null) {
            fail("No blob files found in the objects directory.");
        }

        for (String idString : blobFiles)
        {
            try
            {
                File blobFile = join(OBJECTS_DIR, idString);
                Blob blob = readObject(blobFile, Blob.class);
                Path path = Paths.get(filename);
                if (blob.getPath().equals(path.toAbsolutePath().toString())) {
                    return true;
                }
            }
            catch (IllegalArgumentException ignored)
            {
            }
        }
        return false;
    }

    private static final FilenameFilter PLAIN_FILES =
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name).isFile();
                }
            };

    static List<String> plainFilenamesIn(File dir) {
        String[] files = dir.list(PLAIN_FILES);
        if (files == null) {
            return null;
        } else {
            Arrays.sort(files);
            return Arrays.asList(files);
        }
    }

    static File join(File first, String... others) {
        return Paths.get(first.getPath(), others).toFile();
    }

    static <T extends Serializable> T readObject(File file,
                                                 Class<T> expectedClass) {
        try {
            ObjectInputStream in =
                    new ObjectInputStream(new FileInputStream(file));
            T result = expectedClass.cast(in.readObject());
            in.close();
            return result;
        } catch (IOException | ClassCastException
                 | ClassNotFoundException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }


}
