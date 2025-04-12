package gitlet.test;

import java.io.File;

public class TestUtils
{
    public static boolean deleteDirectory(File dir)
    {
        if (dir.isDirectory())
        {
            File[] children = dir.listFiles();
            if (children != null)
            {
                for (File child : children)
                {
                    if (!deleteDirectory(child))
                        return false;
                }
            }
        }
        return dir.delete();
    }
}
