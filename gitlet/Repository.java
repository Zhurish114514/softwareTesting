package gitlet;

import static gitlet.Utils.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Represents a gitlet repository.
 *  does at a high level.
 *
 *  @author Zhurish
 */
public class Repository
{
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /*
     *   .gitlet
     *      |--objects
     *      |     |--commits
     *      |     |--blobs
     *      |
     *      |--refs
     *      |    |--heads
     *      |         |--master
     *      |--HEAD
     *      |--addStage
     *      |--removeStage
     *heads文件夹存放的是分支，文件名是各个分支名，内容是该分支最新的commit的哈希值id
     *HEAD文件内容是当前分支的文件名字符串
     */

    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    public static final File ADD_STAGE_DIR = join(GITLET_DIR, "addStage");
    public static final File REMOVE_STAGE_DIR = join(GITLET_DIR, "removeStage");

    private static Commit lastCommit;
    private static Stage addStage;
    private static Stage removeStage;

    private static String getCurrentBranch()
    {
        // 读取HEAD文件，获取当前分支的文件路径
        return readObject(HEAD_FILE, String.class);
    }

    public static void checkCommandLength(String[] args, int length)
    {
        if (args.length != length)
        {
            message("Incorrect operands.");
            System.exit(0);
        }
    }

    public static void init()
    {
        if (GITLET_DIR.exists())
        {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();

        // 没有Blob文件，生成commit文件，然后是HEAD_FILE,HEADS_DIR中的master
        Commit initialCommit = initCommit();
        initHEAD(); // HEAD文件存储的是当前分支的文件路径，是字符串表示的分支名
        initHeads(
            initialCommit); // Heads文件夹存放的是分支文件，文件内容是该分支最新的commit的哈希值id(文件名是分支名，文件内容是commitID)
    }

    private static Commit initCommit()
    {
        Commit initialCommit = new Commit();
        initialCommit.saveCommit();
        return initialCommit;
    }

    private static void initHEAD()
    {
        writeObject(HEAD_FILE, "master");
    }

    private static void initHeads(Commit initialCommit)
    {
        File master = join(HEADS_DIR, "master");
        writeObject(master, initialCommit.getCommitID());
    }

    public static void checkIfInitialized()
    {
        if (!GITLET_DIR.exists())
        {
            message("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void add(String fileName)
    {
        File file = getFile(fileName);
        if (!file.exists())
        {
            message("File does not exist.");
            System.exit(0);
        }
        Blob blob = new Blob(file); // Blob对象存储add的文件
        storeBlob(blob);
    }

    public static File getFile(String fileString)
    {
        // 如果文件字符串是绝对路径，则直接返回文件对象，否则构造绝对路径文件对象
        return Paths.get(fileString).isAbsolute() ? new File(fileString) : join(CWD, fileString);
    }

    private static void storeBlob(Blob blob)
    {
        Commit previousCommit = readLastCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        String path = blob.getPath();
        String id = blob.getBlobId();
        // 针对该文件的各种情况进行考虑：对于commit，不在commit中进；如果在commit中且也在removeStage中，也进
        // 然后进入了，有两种逻辑：添加到addStage中，在removeStage中删除
        // 对于removeStage中的文件，因为是rm命令加入的，所以addStage中必不含，在removeStage中删除键值对，保存removeStage
        // 对于addStage中的文件，根据之前是否含有这个path，如果含有则删除path对应的键值对，然后在addStage中添加键值对，保存Blob，最后保存Stage文件
        if (!previousCommit.containsId(id) || removeStage.containsId(id))
        {
            if (removeStage.containsId(id))
            {
                removeStage.deleteBlobById(id);
                removeStage.saveStage(REMOVE_STAGE_DIR);
            }
            else
            {
                if (!addStage.containsId(id))
                {
                    if (addStage.containsPath(path))
                    {
                        addStage.deleteBlobByPath(path);
                    }
                    blob.saveBlob();
                    addStage.addBlob(blob);
                    addStage.saveStage(ADD_STAGE_DIR);
                }
            }
        }
    }

    private static Stage readRemoveStage()
    {
        if (REMOVE_STAGE_DIR.exists())
        {
            return readObject(REMOVE_STAGE_DIR, Stage.class);
        }
        return new Stage();
    }

    private static Stage readAddStage()
    {
        if (ADD_STAGE_DIR.exists())
        {
            return readObject(ADD_STAGE_DIR, Stage.class);
        }
        return new Stage();
    }

    public static Commit readLastCommit()
    {
        // 读取之前最新的commit:首先读取HEAD文件，获取当前分支的文件路径，然后读取该文件，获取最新的commitID
        String branchName = readObject(HEAD_FILE, String.class); // HEAD文件获取分支名
        File branchFile = join(HEADS_DIR, branchName);
        String commitId = readObject(branchFile, String.class);
        File commitFile = join(OBJECTS_DIR, commitId);
        return readObject(commitFile, Commit.class);
    }

    public static void commit(String message)
    {
        lastCommit = readLastCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        checkMessage(message);
        Commit newCommit = newCommit(message);
        newCommit.saveCommit();
        addStage.clear();
        removeStage.clear();
        addStage.saveStage(ADD_STAGE_DIR);
        removeStage.saveStage(REMOVE_STAGE_DIR);
        alterHEADS(newCommit);
    }

    private static void alterHEADS(Commit newCommit)
    {
        // 进行commit操作并未涉及branch的改变，所以直接读取HEAD文件，获取当前分支名
        // 然后在HEADS_DIR中找到该分支文件，将新的commitID写入
        String branchName = readObject(HEAD_FILE, String.class);
        File branchFile = join(HEADS_DIR, branchName);
        writeObject(branchFile, newCommit.getCommitID());
    }

    private static Commit newCommit(String message)
    {
        ArrayList<String> parents = getParents();
        HashMap<String, String> commitMap = calculateCommitMap();
        return new Commit(message, commitMap, parents);
    }

    private static HashMap<String, String> calculateCommitMap()
    {
        // 如果两个stage的map都为空，则报错
        // 计算的逻辑是这样的：首先照搬previousCommit中的Blob
        // 对于addStage中的Blob，如果和之前的path一致，表明新提交中要用新的覆盖旧的，所以删去旧的
        // 对于removeStage中的Blob，直接删去
        if (addStage.isEmpty() && removeStage.isEmpty())
        {
            message("No changes added to the commit.");
            System.exit(0);
        }
        HashMap<String, String> commitMap = new HashMap<>();
        for (String path : lastCommit.getPathToBlobID().keySet())
        {
            String blobID = lastCommit.getPathToBlobID().get(path);
            commitMap.put(path, blobID);
        }
        if (!addStage.isEmpty())
        {
            for (String path : addStage.getBlobMap().keySet())
            {
                String blobID = addStage.getBlobMap().get(path);
                commitMap.put(path, blobID);
            }
        }
        if (!removeStage.isEmpty())
        {
            for (String path : removeStage.getBlobMap().keySet())
            {
                commitMap.remove(path);
            }
        }
        return commitMap;
    }

    private static ArrayList<String> getParents()
    {
        ArrayList<String> parents = new ArrayList<>();
        parents.add(lastCommit.getCommitID());
        return parents;
    }

    private static void checkMessage(String message)
    {
        if (message.equals(""))
        {
            message("Please enter a commit message.");
            System.exit(0);
        }
    }

    public static void rm(String fileString)
    {
        // 如果在addStage中追踪，在addStage中删去该键值对（不管是否被上次commit追踪）
        // 如果没有被addStage追踪且如果在previousCommit中追踪，在removeStage中加入该键值对，并且删除该文件
        // 如果没有被addStage追踪且没有在previousCommit中追踪，报错
        File rmFile = getFile(fileString);
        lastCommit = readLastCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        // 然后根据文件路径判断
        String path = rmFile.getPath();
        if (addStage.containsPath(path))
        {
            addStage.deleteBlobByPath(path);
            addStage.saveStage(ADD_STAGE_DIR);
        }
        else if (lastCommit.containsPath(path))
        {
            removeStage.addBlob(lastCommit.getBlobByPath(path));
            removeStage.saveStage(REMOVE_STAGE_DIR);
            if (rmFile.exists())
            {
                restrictedDelete(rmFile);
            }
        }
        else
        {
            message("No reason to remove the file.");
            System.exit(0);
        }
    }

    public static void log()
    {
        for (lastCommit = readLastCommit(); hasParentCommit(lastCommit); moveLastToPreviousCommit())
        {
            lastCommit.printCommit();
        }
        lastCommit.printCommit(); // 打印initialCommit
    }

    public  static void moveLastToPreviousCommit()
    {
        // 这里是通过lastCommit全局变量来方便处理，真正的分支lastCommit是通过HEAD文件来获取的，是不变的
        String parentID = lastCommit.getParents().get(0);
        File parentFile = join(OBJECTS_DIR, parentID);
        lastCommit = readObject(parentFile, Commit.class);
    }

    public static boolean hasParentCommit(Commit curCommit)
    {
        return !curCommit.getParents().isEmpty();
    }

    public static void globalLog()
    {
        List<String> commitFiles = plainFilenamesIn(OBJECTS_DIR);
        // 读取./gitlet/objects目录下所有文件(包含Blob和Commit文件)
        // 读取的文件是所有文件的文件名(相对路径，文件名为id)
        if (commitFiles != null)
        {
            for (String idString : commitFiles)
            {
                try
                {
                    File commitFile = join(OBJECTS_DIR, idString);
                    Commit commit = readObject(commitFile, Commit.class);
                    commit.printCommit();
                    // readObject抛出IllegalArgumentException如果类型错误，捕获后不做处理，处理下一个文件
                }
                catch (IllegalArgumentException e)
                {
                    continue;
                }
            }
        }
    }

    public static void find(String message)
    {
        // 读取./gitlet/objects目录下所有文件(包含Blob和Commit文件)
        // List中是所有objects目录下文件的文件名(相对路径，文件名为id)
        List<String> fileNameList = plainFilenamesIn(OBJECTS_DIR);
        boolean containsMessage = false;
        if (fileNameList != null)
        {
            for (String fileName : fileNameList)
            {
                try
                {
                    File commitFile = join(OBJECTS_DIR, fileName);
                    Commit commit = readObject(commitFile, Commit.class);
                    if (commit.getMessage().equals(message))
                    {
                        containsMessage = true;
                        System.out.println(commit.getCommitID());
                    }
                }
                catch (IllegalArgumentException e)
                {
                    continue;
                }
            }
        }
        if (!containsMessage)
        {
            message("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status()
    {
        lastCommit = readLastCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        printBranches();
        printStagedFiles();
        printModificationsNotStaged();
        printUntrackedFiles();
    }
    public static Commit getBlobID(){
        return lastCommit;
    }
    private static void printModificationsNotStaged()
    {
        System.out.println("=== Modifications Not Staged For Commit ===");
        Map<String, String> committedFiles = lastCommit.getPathToBlobID();
        Map<String, String> addStageFiles = addStage.getBlobMap();
        Map<String, String> removeStageFiles = removeStage.getBlobMap();
        Map<String, String> modifiedFiles = new TreeMap<>();
        List<String> cwdFiles = plainFilenamesIn(CWD); // 这里的文件名是相对路径，而上面的map都是绝对路径
        for (String committedFile : committedFiles.keySet())
        {
            // 跟踪在当前提交中，更改在工作目录中，但未暂存;
            // 未标记为删除，但在当前提交中跟踪并从工作目录中删除。

            // 已知跟踪的文件，如果修改、添加后再修改为原来的内容，再进行添加，添加指令不会进行操作
            // 所以如果提交追踪的文件，如果在暂存区中有，内容必不和提交中内容相同
            // 这里没有检测暂存区中的文件和当前目录中的文件是否相同，只检测了是否在暂存区中存在
            String relativePath = relativize(committedFile);
            if (cwdFiles.contains(relativePath))
            {
                String blobID = committedFiles.get(committedFile);
                File blobFile = join(OBJECTS_DIR, blobID);
                Blob blob = readObject(blobFile, Blob.class);
                byte[] blobContent = blob.getContent();
                byte[] cwdContent = readContents(new File(committedFile));
                if (!Arrays.equals(blobContent, cwdContent) && !addStageFiles.containsKey(committedFile))
                {
                    modifiedFiles.put(relativePath, "modified");
                }
            }
            else
            {
                if (!removeStageFiles.containsKey(committedFile))
                {
                    modifiedFiles.put(relativePath, "deleted");
                }
            }
        }
        for (String addStageFile : addStageFiles.keySet())
        {
            // 暂存已添加，但内容与工作目录中的不同；
            // 暂存已添加，但在工作目录中已删除；
            String relativePath = relativize(addStageFile);
            if (cwdFiles.contains(relativePath))
            {
                String blobID = addStageFiles.get(addStageFile);
                File blobFile = join(OBJECTS_DIR, blobID);
                Blob blob = readObject(blobFile, Blob.class);
                byte[] blobContent = blob.getContent();
                byte[] cwdContent = readContents(new File(addStageFile));
                if (!Arrays.equals(blobContent, cwdContent))
                {
                    modifiedFiles.put(relativePath, "modified");
                }
            }
            else
            {
                modifiedFiles.put(relativePath, "deleted");
            }
        }
        for (String modifiedFile : modifiedFiles.keySet())
        {
            System.out.println(modifiedFile + " (" + modifiedFiles.get(modifiedFile) + ")");
        }
        System.out.println();
    }

    private static void printUntrackedFiles()
    {
        // 在当前工作目录中，但是既不在暂存区，也不在最新提交中的文件
        System.out.println("=== Untracked Files ===");
        Map<String, String> committedFiles = lastCommit.getPathToBlobID();
        Map<String, String> addStageFiles = addStage.getBlobMap();
        Map<String, String> removeStageFiles = removeStage.getBlobMap();
        List<String> cwdFiles = plainFilenamesIn(CWD); // 这里的文件名是相对路径，而上面的map都是绝对路径
        for (String cwdFile : cwdFiles)
        {
            String absolutePath = CWD.toPath().resolve(cwdFile).toString();
            if (!committedFiles.containsKey(absolutePath) && !addStageFiles.containsKey(absolutePath) &&
                !removeStageFiles.containsKey(absolutePath))
            {
                System.out.println(cwdFile);
            }
        }
    }

    private static void printStagedFiles()
    {
        System.out.println("=== Staged Files ===");
        List<String> addStageFileNames = new ArrayList<>(addStage.getBlobMap().keySet());
        Collections.sort(addStageFileNames);
        for (String fileName : addStageFileNames)
        {
            String relativePath = relativize(fileName);
            System.out.println(relativePath);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        List<String> removeStageFileNames = new ArrayList<>(removeStage.getBlobMap().keySet());
        Collections.sort(removeStageFileNames);
        for (String fileName : removeStageFileNames)
        {
            String relativePath = relativize(fileName);
            System.out.println(relativePath);
        }
        System.out.println();
    }

    private static String relativize(String pathString)
    {
        // addStage,removeStage,commit的pathToBlob存储的都是绝对路径，需要进行转换
        Path path = Paths.get(pathString);
        Path cwdPath = CWD.toPath();
        return cwdPath.relativize(path).toString();
    }

    private static void printBranches()
    {
        System.out.println("=== Branches ===");
        String curBranch = readObject(HEAD_FILE, String.class);
        List<String> branchFiles = plainFilenamesIn(HEADS_DIR);
        if (branchFiles != null)
        {
            Collections.sort(branchFiles);
            for (String branchName : branchFiles)
            {
                if (branchName.equals(curBranch))
                {
                    System.out.println("*" + branchName);
                }
                else
                {
                    System.out.println(branchName);
                }
            }
        }
        System.out.println();
    }

    public static void checkoutBranch(String branchName)
    {
        lastCommit = readLastCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        List<String> branchFiles = plainFilenamesIn(HEADS_DIR);
        // 判断：分支是否存在
        if (!branchFiles.contains(branchName))
        {
            message("No such branch exists.");
            System.exit(0);
        }
        String curBranch = readObject(HEAD_FILE, String.class);
        // 判断：是否为当前分支
        if (curBranch.equals(branchName))
        {
            message("No need to checkout the current branch.");
            System.exit(0);
        }
        // If a working file is untracked in the current branch
        //  and would be overwritten by the checkout
        List<String> untrackedFiles = checkUntrackedFiles(); // 未被当前commit追踪的文件，这里的路径都是绝对路径
        File assignedbranchFile = join(HEADS_DIR, branchName);
        String commitId = readObject(assignedbranchFile, String.class);
        Commit assignedCommit = readObject(join(OBJECTS_DIR, commitId), Commit.class);
        // 这里有一个问题，如果原分支存在的未被跟踪文件在新分支中，是判断内容相同后报错还是直接报错
        for (String untrackedFile : untrackedFiles)
        {
            if (assignedCommit.containsPath(untrackedFile))
            {
                byte[] blobContent = assignedCommit.getBlobByPath(untrackedFile).getContent();
                byte[] cwdContent = readContents(new File(untrackedFile));
                if (!Arrays.equals(blobContent, cwdContent))
                {
                    message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        // 删除所有在当前分支追踪但不在检出分支的文件
        for (String path : lastCommit.getPathToBlobID().keySet())
        {
            if (!assignedCommit.containsPath(path))
            {
                restrictedDelete(new File(path));
            }
        }
        // 将当前分支的文件全部复制到工作目录
        for (String path : assignedCommit.getPathToBlobID().keySet())
        {
            Blob blob = assignedCommit.getBlobByPath(path);
            writeContents(new File(path), (Object)blob.getContent());
        }
        writeObject(HEAD_FILE, branchName);
        addStage.clear();
        removeStage.clear();
        addStage.saveStage(ADD_STAGE_DIR);
        removeStage.saveStage(REMOVE_STAGE_DIR);
    }

    private static List<String> checkUntrackedFiles()
    {
        // 返回的是未被当前commit追踪的文件
        List<String> untrackedFiles = new ArrayList<>();
        List<String> cwdFiles = plainFilenamesIn(CWD);
        for (String cwdFile : cwdFiles)
        {
            String absolutePath = CWD.toPath().resolve(cwdFile).toString();
            if (!lastCommit.containsPath(absolutePath))
            {
                untrackedFiles.add(absolutePath);
            }
        }
        return untrackedFiles;
    }

    public static void checkoutFile(String fileName)
    {
        lastCommit = readLastCommit();
        String absolutePath = CWD.toPath().resolve(fileName).toString();
        if (!lastCommit.containsPath(absolutePath))
        {
            message("File does not exist in that commit.");
            System.exit(0);
        }
        Blob blob = lastCommit.getBlobByPath(absolutePath);
        writeContents(new File(fileName), (Object)blob.getContent());
    }

    public static void checkoutCommit(String commitId, String fileName)
    {
        String absolutePath = CWD.toPath().resolve(fileName).toString();
        File commitFile = checkCommitId(commitId);
        if (commitFile == null)
        {
            message("No commit with that id exists.");
            System.exit(0);
        }
        Commit assginedcommit = readObject(commitFile, Commit.class);
        if (!assginedcommit.containsPath(absolutePath))
        {
            message("File does not exist in that commit.");
            System.exit(0);
        }
        Blob blob = assginedcommit.getBlobByPath(absolutePath);
        writeContents(new File(fileName), (Object)blob.getContent());
    }

    private static File checkCommitId(String commitId)
    {
        // 输入的是commitId的前缀，判断Objects文件夹中是否存在匹配的commit文件，如果存在返回文件，否则返回null
        // 输入的是40位哈希值的前缀，首先判断文件夹中是否有对应的文件存在，如果存在再进行判断，是否是commit文件，只当是commit文件时才返回File
        List<String> commitFiles = plainFilenamesIn(OBJECTS_DIR);
        if (commitFiles != null)
        {
            for (String fileName : commitFiles)
            {
                try
                {
                    if (fileName.startsWith(commitId))
                    {
                        File commitFile = join(OBJECTS_DIR, fileName);
                        Commit commit = readObject(commitFile, Commit.class);
                        return commitFile;
                    }
                }
                catch (IllegalArgumentException e)
                {
                    continue;
                }
            }
        }
        return null;
    }

    public static void branch(String branchName)
    {
        lastCommit = readLastCommit();
        File branchFile = join(HEADS_DIR, branchName);
        if (branchFile.exists())
        {
            message("A branch with that name already exists.");
            System.exit(0);
        }
        writeObject(branchFile, lastCommit.getCommitID());
    }

    public static void rmBranch(String assignedBranch)
    {
        String curBranch = readObject(HEAD_FILE, String.class);
        if (curBranch.equals(assignedBranch))
        {
            message("Cannot remove the current branch.");
            System.exit(0);
        }
        File branchFile = join(HEADS_DIR, assignedBranch);
        if (!branchFile.exists())
        {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        branchFile.delete();
    }

    public static void reset(String commitId)
    {
        // 首先检查commitId，commitId可能以前缀形式给出
        File commitFile = checkCommitId(commitId);
        if (commitFile == null)
        {
            message("No commit with that id exists.");
            System.exit(0);
        }
        lastCommit = readLastCommit();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        // 如果指定commit中追踪的文件在当前分支中未被追踪，且文件内容不同，报错
        List<String> untrackedFiles = checkUntrackedFiles(); // 未被当前commit追踪的文件，这里的路径都是绝对路径
        Commit assignedCommit = readObject(commitFile, Commit.class);
        for (String untrackedFile : untrackedFiles)
        {
            if (assignedCommit.containsPath(untrackedFile))
            {
                byte[] blobContent = assignedCommit.getBlobByPath(untrackedFile).getContent();
                byte[] cwdContent = readContents(new File(untrackedFile));
                if (!Arrays.equals(blobContent, cwdContent))
                {
                    message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        // 删除所有在当前分支追踪但不在指定commit中的文件
        for (String path : lastCommit.getPathToBlobID().keySet())
        {
            if (!assignedCommit.containsPath(path))
            {
                restrictedDelete(new File(path));
            }
        }
        for (String path : assignedCommit.getPathToBlobID().keySet())
        {
            Blob blob = assignedCommit.getBlobByPath(path);
            writeContents(new File(path), (Object)blob.getContent());
        }
        // 将当前分支的指针移向指定commit，清空并保存缓冲区
        String assignedCommitId = assignedCommit.getCommitID();
        File curBranchFile = join(HEADS_DIR, readObject(HEAD_FILE, String.class));
        writeObject(curBranchFile, assignedCommitId);
        addStage = readAddStage();
        removeStage = readRemoveStage();
        addStage.clear();
        removeStage.clear();
        addStage.saveStage(ADD_STAGE_DIR);
        removeStage.saveStage(REMOVE_STAGE_DIR);
    }

    public static void merge(String targetBranch)
    {
        lastCommit = readLastCommit();
        String curCommitId = lastCommit.getCommitID();
        addStage = readAddStage();
        removeStage = readRemoveStage();
        File targetBranchFile = join(HEADS_DIR, targetBranch); // 目标分支文件，存储目标分支最后一个commit的id
        String curBranch = readObject(HEAD_FILE, String.class); // 当前分支名
        if (!addStage.isEmpty() || !removeStage.isEmpty())
        {
            message("You have uncommitted changes.");
            System.exit(0);
        }
        if (!targetBranchFile.exists())
        {
            message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (curBranch.equals(targetBranch))
        {
            message("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String targetBranchCommitId = readObject(targetBranchFile, String.class);
        Commit targetCommit = readObject(join(OBJECTS_DIR, targetBranchCommitId), Commit.class);
        String splitPointId = findSplitPoint(targetCommit);
        Commit splitPoint = readObject(join(OBJECTS_DIR, splitPointId), Commit.class);
        if (splitPointId.equals(targetBranchCommitId))
        {
            message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitPointId.equals(curCommitId))
        {
            message("Current branch fast-forwarded.");
            checkoutBranch(targetBranch);
            return;
        }
        lastCommit = readLastCommit();
        Map<String, String> splitPointMap = splitPoint.getPathToBlobID();
        Map<String, String> curCommitMap = lastCommit.getPathToBlobID();
        Map<String, String> targetCommitMap = targetCommit.getPathToBlobID();
        Map<String, String> overwriteMap = overWriteFiles(splitPointMap, curCommitMap, targetCommitMap);
        Map<String, String> writeMap = writeMap(splitPointMap, curCommitMap, targetCommitMap);
        Map<String, String> removeMap = removeMap(splitPointMap, curCommitMap, targetCommitMap);
        checkMerge(writeMap, removeMap, curCommitMap);
        writeFiles(writeMap);
        removeFiles(removeMap);
        overWrite(overwriteMap);
        String message = "Merged " + targetBranch + " into " + curBranch + ".";
        List<String> parents = new ArrayList<>();
        parents.add(lastCommit.getCommitID());
        parents.add(targetBranchCommitId);
        List<String> allFiles = allFilesList(splitPoint, lastCommit, targetCommit);
        Map<String, String> conflictMap =
            calculateConflict(allFiles, splitPointMap, curCommitMap, targetCommitMap); // 处理冲突的情况
        Map<String, String> commitMap = new HashMap<>();
        for (String path : curCommitMap.keySet())
        {
            String blobId = curCommitMap.get(path);
            commitMap.put(path, blobId);
        }
        for (String path : overwriteMap.keySet())
        {
            String blobId = overwriteMap.get(path);
            commitMap.put(path, blobId);
        }
        for (String path : writeMap.keySet())
        {
            String blobId = writeMap.get(path);
            commitMap.put(path, blobId);
        }
        for (String path : conflictMap.keySet())
        {
            String blobId = conflictMap.get(path);
            commitMap.put(path, blobId);
        }
        for (String path : removeMap.keySet())
        {
            commitMap.remove(path);
        }
        Commit mergeCommit = new Commit(message, commitMap, parents);
        mergeCommit.saveCommit();
        addStage.clear();
        removeStage.clear();
        addStage.saveStage(ADD_STAGE_DIR);
        removeStage.saveStage(REMOVE_STAGE_DIR);
        alterHEADS(mergeCommit);
    }

    private static void checkMerge(Map<String, String> writeMap, Map<String, String> removeMap,
                                   Map<String, String> curCommitMap)
    {
        // 所有当前目录下的文件，不被当前commit追踪
        // 但是要被writeMap重写或者removeMap删除的文件，报错
        // 这里的文件名是相对路径，改为绝对路径
        List<String> cwdFiles = plainFilenamesIn(CWD);
        List<String> absoluteCwdFiles = new ArrayList<>();
        for (String cwdFile : cwdFiles)
        {
            String absolutePath = CWD.toPath().resolve(cwdFile).toString();
            absoluteCwdFiles.add(absolutePath);
        }
        for (String cwdFile : absoluteCwdFiles)
        {
            if (!curCommitMap.containsKey(cwdFile))
            {
                if (writeMap.containsKey(cwdFile) || removeMap.containsKey(cwdFile))
                {
                    message("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }

    private static Map<String, String> calculateConflict(List<String> allFiles, Map<String, String> splitPointMap,
                                                         Map<String, String> curCommitMap,
                                                         Map<String, String> targetCommitMap)
    {
        // conflictMap是否要追踪冲突的文件内容
        Map<String, String> conflictMap = new HashMap<>();
        boolean conflict = false;
        for (String path : allFiles)
        {
            String curBranchContent = "";
            String assignedBranchContent = "";
            int fileBits = 0;
            if (splitPointMap.containsKey(path))
            {
                fileBits += 1;
            }
            if (curCommitMap.containsKey(path))
            {
                fileBits += 2;
            }
            if (targetCommitMap.containsKey(path))
            {
                fileBits += 4;
            }
            switch (fileBits)
            {
            case 3:
                if (!splitPointMap.get(path).equals(curCommitMap.get(path)))
                {
                    conflict = true;
                    curBranchContent = blobContent(curCommitMap.get(path));
                    writeConflictFile(path, curBranchContent, assignedBranchContent);
                    conflictMap.put(path, conflictBlob(path));
                }
                break;
            case 5:
                if (!splitPointMap.get(path).equals(targetCommitMap.get(path)))
                {
                    conflict = true;
                    assignedBranchContent = blobContent(targetCommitMap.get(path));
                    writeConflictFile(path, curBranchContent, assignedBranchContent);
                    conflictMap.put(path, conflictBlob(path));
                }
                break;
            case 6:
                if (!curCommitMap.get(path).equals(targetCommitMap.get(path)))
                {
                    conflict = true;
                    curBranchContent = blobContent(curCommitMap.get(path));
                    assignedBranchContent = blobContent(targetCommitMap.get(path));
                    writeConflictFile(path, curBranchContent, assignedBranchContent);
                    conflictMap.put(path, conflictBlob(path));
                }
                break;
            case 7:
                if (!splitPointMap.get(path).equals(curCommitMap.get(path)) &&
                    !splitPointMap.get(path).equals(targetCommitMap.get(path)))
                {
                    if (!curCommitMap.get(path).equals(targetCommitMap.get(path)))
                    {
                        conflict = true;
                        curBranchContent = blobContent(curCommitMap.get(path));
                        assignedBranchContent = blobContent(targetCommitMap.get(path));
                        writeConflictFile(path, curBranchContent, assignedBranchContent);
                        conflictMap.put(path, conflictBlob(path));
                    }
                }
                break;
            default:
                break;
            }
        }
        if (conflict)
        {
            System.out.println("Encountered a merge conflict.");
        }
        return conflictMap;
    }

    private static String conflictBlob(String path)
    {
        // 冲突文件已经写好了，现在创建对应的blob文件
        // 返回blobId
        Blob conflictBlob = new Blob(new File(path));
        writeObject(join(OBJECTS_DIR, conflictBlob.getBlobId()), conflictBlob);
        return conflictBlob.getBlobId();
    }

    private static String blobContent(String blobId)
    {
        File blobFile = join(OBJECTS_DIR, blobId);
        Blob blob = readObject(blobFile, Blob.class);
        return new String(blob.getContent());
    }

    private static void writeConflictFile(String path, String curBranchContent, String assignedBranchContent)
    {
        File conflictFile = new File(path);
        String conflictContent =
            "<<<<<<< HEAD\n" + curBranchContent + "=======\n" + assignedBranchContent + ">>>>>>>\n";
        writeContents(conflictFile, conflictContent);
    }

    private static void removeFiles(Map<String, String> removeMap)
    {
        for (String path : removeMap.keySet())
        {
            restrictedDelete(new File(path));
        }
    }

    private static void writeFiles(Map<String, String> writeMap)
    {
        // 只在给定分支中存在，在分割点和当前分支都不存在,在工作目录直接写
        for (String path : writeMap.keySet())
        {
            String blobId = writeMap.get(path);
            File blobFile = join(OBJECTS_DIR, blobId);
            Blob blob = readObject(blobFile, Blob.class);
            writeContents(new File(path), (Object)blob.getContent());
        }
    }

    private static void overWrite(Map<String, String> overwriteMap)
    {
        // 对于三个commit中都存在并且分割点和当前分支内容相同
        //  但是和目标分支内容不同的文件，直接覆盖
        for (String path : overwriteMap.keySet())
        {
            String blobId = overwriteMap.get(path);
            File blobFile = join(OBJECTS_DIR, blobId);
            Blob blob = readObject(blobFile, Blob.class);
            writeContents(new File(path), (Object)blob.getContent());
        }
    }

    private static Map<String, String> removeMap(Map<String, String> splitPointMap, Map<String, String> curCommitMap,
                                                 Map<String, String> targetCommitMap)
    {
        // 只在分割点和当前分支存在，在目标分支中不存在
        // 且分割点和当前分支的内容相同
        Map<String, String> removeMap = new HashMap<>();

        for (String splitFilepath : splitPointMap.keySet())
        {
            if (curCommitMap.containsKey(splitFilepath) && !targetCommitMap.containsKey(splitFilepath))
            {
                String splitPointBlobId = splitPointMap.get(splitFilepath);
                String curCommitBlobId = curCommitMap.get(splitFilepath);
                if (splitPointBlobId.equals(curCommitBlobId))
                {
                    removeMap.put(splitFilepath, curCommitBlobId);
                }
            }
        }
        return removeMap;
    }

    private static Map<String, String> writeMap(Map<String, String> splitPointMap, Map<String, String> curCommitMap,
                                                Map<String, String> targetCommitMap)
    {
        Map<String, String> writeMap = new HashMap<>();
        // 只在给定分支中存在，在分割点和当前分支都不存在
        for (String targetFilePath : targetCommitMap.keySet())
        {
            if (!splitPointMap.containsKey(targetFilePath) && !curCommitMap.containsKey(targetFilePath))
            {
                writeMap.put(targetFilePath, targetCommitMap.get(targetFilePath));
            }
        }
        return writeMap;
    }

    private static Map<String, String> overWriteFiles(Map<String, String> splitPointMap,
                                                      Map<String, String> curCommitMap,
                                                      Map<String, String> targetCommitMap)
    {
        // 寻找所有在三个commit中都存在但是只有给定分支不同的文件
        // 要将当前目录下改写为目标分支的内容，并且追踪的也是目标分支
        Map<String, String> overwriteMap = new HashMap<>();
        for (String path : splitPointMap.keySet())
        {
            if (curCommitMap.containsKey(path) && targetCommitMap.containsKey(path))
            {
                String splitPointBlobId = splitPointMap.get(path);
                String curCommitBlobId = curCommitMap.get(path);
                String targetCommitBlobId = targetCommitMap.get(path);
                if (splitPointBlobId.equals(curCommitBlobId) && !splitPointBlobId.equals(targetCommitBlobId))
                {
                    overwriteMap.put(path, targetCommitBlobId);
                }
            }
        }
        return overwriteMap;
    }

    private static List<String> allFilesList(Commit splitPoint, Commit curCommit, Commit targetCommit)
    {
        List<String> allFiles = new ArrayList<>();
        allFiles.addAll(splitPoint.getPathToBlobID().keySet());
        allFiles.addAll(curCommit.getPathToBlobID().keySet());
        allFiles.addAll(targetCommit.getPathToBlobID().keySet());
        Set<String> allFilesSet = new HashSet<>(allFiles);
        allFiles.clear();
        allFiles.addAll(allFilesSet);
        return allFiles;
    }

    private static String findSplitPoint(Commit targetCommit)
    {
        // 首先按bfs的方式遍历当前分支的所有commit，将所有commit的id存入set中
        lastCommit = readLastCommit();
        Queue<String> commitQueue = new LinkedList<>();
        Set<String> curBranchCommits = new HashSet<>();
        commitQueue.add(lastCommit.getCommitID());
        while (!commitQueue.isEmpty())
        {
            String curCommitId = commitQueue.poll();
            curBranchCommits.add(curCommitId);
            File curCommitFile = join(OBJECTS_DIR, curCommitId);
            Commit curCommit = readObject(curCommitFile, Commit.class);
            for (String parent : curCommit.getParents())
            {
                commitQueue.add(parent);
            }
        }
        // 然后按bfs的方式遍历目标分支的所有commit，如果遇到当前分支的commit，返回
        commitQueue.add(targetCommit.getCommitID());
        while (!commitQueue.isEmpty())
        {
            String assignedBranchCommitId = commitQueue.poll();
            if (curBranchCommits.contains(assignedBranchCommitId))
            {
                return assignedBranchCommitId;
            }
            File curCommitFile = join(OBJECTS_DIR, assignedBranchCommitId);
            Commit curCommit = readObject(curCommitFile, Commit.class);
            for (String parent : curCommit.getParents())
            {
                commitQueue.add(parent);
            }
        }
        return null;
    }
}
