package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.*;
import java.text.SimpleDateFormat;


import static gitlet.Utils.join;
import static gitlet.Utils.writeObject;


/** Represents a gitlet commit object.
 *  does at a high level.
 *
 *  @author Zhurish
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date curTime;
    private List<String> parents;
    private Map<String, String> pathToBlobID;
    private String id;
    private String timeStamp;
    private File commitSaveFileName;    //commitSaveFileName是commit对象的保存文件名(使用哈希计算的commitID)

    //这是针对git init的第一个commit
    public Commit() {
        this.curTime = new Date(0);
        this.message = "initial commit";
        this.parents = new ArrayList<>();
        this.pathToBlobID = new HashMap<>();
        this.timeStamp = dateToTimeStamp(this.curTime);
        this.id = generateID();
        this.commitSaveFileName = generateFileName();
    }

    //对于commit而言，外部需要确定的东西：commitMessage、parentsId、追踪的pathToBlob哈希表，然后就生成对应的东西
    public Commit(String message, Map<String, String> pathToBlobID, List<String> parents) {
        this.message = message;
        this.pathToBlobID = pathToBlobID;
        this.parents = parents;
        this.curTime = new Date();
        this.timeStamp = dateToTimeStamp(this.curTime);
        this.id = generateID();
        this.commitSaveFileName = generateFileName();
    }

    private String generateID() {
        return Utils.sha1(this.message, this.timeStamp,
                this.parents.toString(), this.pathToBlobID.toString());
    }

    private static String dateToTimeStamp(Date curTime) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(curTime);
    }

    private File generateFileName() {
        return join(Repository.OBJECTS_DIR, this.id);
    }

    public void saveCommit() {
        writeObject(this.commitSaveFileName, this);
    }

    public String getCommitID() {
        return this.id;
    }

    public boolean containsId(String blobId) {
        return this.pathToBlobID.containsValue(blobId);
    }

    public Map<String, String> getPathToBlobID() {
        return this.pathToBlobID;
    }

    public boolean containsPath(String path) {
        return this.pathToBlobID.containsKey(path);
    }

    public Blob getBlobByPath(String path) {
        //输入文件的path，返回文件对应的Blob对象
        //根据输入的文件path，通过map找到Blob的id
        //然后根据id找到Blob对象
        return Utils.readObject(Utils.join
                (Repository.OBJECTS_DIR, this.pathToBlobID.get(path)), Blob.class);
    }

    public List<String> getParents() {
        return this.parents;
    }

    void printCommit() {
        //按照规定要求，对于mergeCommit输出格式与普通commit(单parent)不同
        if (this.parents.size() == 2) {
            printMergeCommit();

        } else {
            printSingleCommit();
        }
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    private void printSingleCommit() {
        System.out.println("===");
        System.out.println("commit " + this.id);
        System.out.println("Date: " + this.timeStamp);
        System.out.println(this.message);
        System.out.println();
    }

    private void printMergeCommit() {
        System.out.println("===");
        System.out.println("commit " + this.id);
        System.out.println("Merge: " + this.parents.get(0).substring(0, 7)
                + " " + this.parents.get(1).substring(0, 7));
        System.out.println("Date: " + this.timeStamp);
        System.out.println(this.message);
        System.out.println();
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isMergeCommit() {
        return this.parents.size() == 2;
    }

    public List<String> getParentCommitIDs() {
        return this.parents;
    }
}
