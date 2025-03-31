package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

//Stage
public class Stage implements Serializable {
    private HashMap<String, String> pathToBlobId;

    public Stage() {
        pathToBlobId = new HashMap<>();
    }

    public boolean containsId(String blobId) {
        return pathToBlobId.containsValue(blobId);
    }


    public boolean containsPath(String path) {
        return pathToBlobId.containsKey(path);
    }

    public void addBlob(Blob blob) {
        pathToBlobId.put(blob.getPath(), blob.getBlobId());
    }

    public void deleteBlobByPath(String path) {
        pathToBlobId.remove(path);
    }

    public void saveStage(File addStageDir) {
        Utils.writeObject(addStageDir, this);
    }

    public void deleteBlobById(String id) {
        pathToBlobId.values().removeIf(value -> value.equals(id));
    }

    public void print() {
        //输出stage中所有的文件名和对应的id
        for (String path : pathToBlobId.keySet()) {
            System.out.println(path + " " + pathToBlobId.get(path));
        }
    }

    //因为stage中的变量就是HashMap，所以HashMap为空代表stage为空
    public boolean isEmpty() {
        return pathToBlobId.isEmpty();
    }

    public Map<String, String> getBlobMap() {
        return pathToBlobId;
    }

    public void clear() {
        pathToBlobId.clear();
    }
}
