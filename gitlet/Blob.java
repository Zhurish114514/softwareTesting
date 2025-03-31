package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Repository.OBJECTS_DIR;


public class Blob implements Serializable {
    //对于git add操作，输入的是文件名
    private String path;    //文件的路径字符串表示(绝对路径)
    private String id;  //文件哈希得到的id,作为Blob文件的文件名
    private File blobSaveFileName;  //保存到Objects文件夹的文件名，形式如：Objects/id
    private byte[] content; //文件内容
    private File filePath;  //文件的绝对路径
    private String fileString;    //文件名

    public Blob(File file) {
        this.filePath = file;
        this.path = file.getPath();
        this.content = readFile(file);
        this.id = Utils.sha1(path, content);
        this.blobSaveFileName = generateBlobSaveFileName();
        this.fileString = file.getName();
    }

    public File generateBlobSaveFileName() {
        return Utils.join(OBJECTS_DIR, id);
    }

    public byte[] readFile(File pathOfFile) {
        return Utils.readContents(pathOfFile);
    }


    public String getBlobId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void saveBlob() {
        Utils.writeObject(blobSaveFileName, this);
    }

    public byte[] getContent() {
        return content;
    }
}
