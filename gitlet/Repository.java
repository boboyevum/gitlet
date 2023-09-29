package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  Manages the repository state and operations.
 *
 *  @author Gabriel and Umar
 */
public class Repository implements Serializable {
    public static Repository load() {
        File repoFile = new File(GITLET_DIR, "repo.ser");
        if (repoFile.exists()) {
            return Utils.readObject(repoFile, Repository.class);
        } else {
            return new Repository();
        }
    }

    public void save() {
        File repoFile = new File(GITLET_DIR, "repo.ser");
        Utils.writeObject(repoFile, this);
    }
    /**
     * The current working directory.
     * This variable represents the current working directory where the Gitlet repository is located.
     * It is used to construct file paths relative to the current directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /**
     * The .gitlet directory.
     * This variable represents the root directory of the Gitlet repository.
     * It is used as the base directory for storing all Gitlet-related files and folders.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * The directory for storing commit objects.
     * This variable represents the directory where commit objects are stored.
     * It is a subdirectory of the .gitlet directory.
     */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    public static final File STAGED = join(GITLET_DIR, "staged");
    // Adding and Removing Areas within the Staging Area
    public static final File ADDING_AREA = join(STAGED, "adding_area");
    public static final File REMOVING_AREA = join(STAGED, "removing_area");

    /**
     * The directory for storing branch files.
     * This variable represents the directory where branch files are stored.
     * It is a subdirectory of the .gitlet directory.
     */
    public static final File BRANCHES_DIR = join(GITLET_DIR, "branches");

    /**
     * This directory stores files that were committed at some point.
     * A single file in this directory is named after the sha1 of the committed file and
     * that file contains the copy of the contents of the committed file.
     */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");

    /**
     * The current commit pointed to by the HEAD.
     * This variable represents the current commit that the HEAD points to.
     * It is used to track the current state of the repository.
     */
    public static final File headCommit = join(GITLET_DIR, "headcommit.txt");

    /**
     * The name of the current branch.
     * This variable represents the name of the current branch in the repository.
     * It is used to keep track of the active branch during repository operations.
     */
    public static final File currentBranch = join(GITLET_DIR, "currentBranch.txt");

    public static final File main = join(BRANCHES_DIR, "main.txt");


    /**
     * Initializes a new Gitlet repository.
     */
    public void init() {
        // Check if Gitlet directory already exists
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
        }
        else {
            // Create the necessary directories
            GITLET_DIR.mkdirs();
            COMMITS_DIR.mkdirs();
            STAGED.mkdirs();
            ADDING_AREA.mkdirs();
            REMOVING_AREA.mkdirs();
            BLOBS_DIR.mkdirs();
            BRANCHES_DIR.mkdirs();

            // Create the initial commit
            Commit initialCommit = new Commit("initial commit", null);
            writeObject(join(COMMITS_DIR, initialCommit.getId()), initialCommit);

            writeContents(headCommit, initialCommit.getId());
            writeContents(currentBranch, "main.txt");
            writeObject(main, initialCommit.getId());
        }
    }

    /**
     * Adds a file to the staging area.
     *
     * @param fileName the name of the file to add
     */
    public void add(String fileName) {
        if (!gitletExists()) {
            return;
        }

        if (!join(Repository.CWD, fileName).exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String contents1 = fileName + readContentsAsString(join(Repository.CWD, fileName));
        String fileSHA1 = sha1(contents1);

        TreeMap currTreeMap = getHeadCommit().getBlobs();

        // If the file was in the staged for removal area, remove it from there
        File removedFile = join(REMOVING_AREA, fileName);
        if (removedFile.exists()) {
            removedFile.delete();
        }

        // checks if our file is in the current commit by checking the current commit's treeMap
        if (currTreeMap.containsValue(fileSHA1)) {
            if (join(Repository.ADDING_AREA, fileName).exists()) {
                join(Repository.ADDING_AREA, fileName).delete();
            }

        } else {
            if (join(Repository.ADDING_AREA, fileName).exists()) {
                join(Repository.ADDING_AREA, fileName).delete();
            }
            File file1 = join(Repository.ADDING_AREA, fileName);
            writeContents(file1, readContents(join(Repository.CWD, fileName)));
        }
    }

    /**
     * Commits the changes in the staging area.
     *
     * @param message the commit message
     */
    public void commit(String message) {
        if (!gitletExists()) {
            return;
        }

        List<String> filesToAdd = plainFilenamesIn(ADDING_AREA);
        List<String> filesToRemove = plainFilenamesIn(REMOVING_AREA);
        if (filesToAdd.isEmpty() && filesToRemove.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            return;
        }

        Commit newCommit = new Commit(message, getHeadCommit().getId());
        newCommit.copyThisBlobs(getHeadCommit().getBlobs());

        // loop through the staging area, updating the blobs directory
        for (String fileName : filesToAdd) {
            String fileContent = readContentsAsString(join(ADDING_AREA, fileName));
            String fileHash = sha1(fileName + fileContent);

            // add file to the Blobs Directory
            File blob1 = join(BLOBS_DIR, fileHash);
            writeContents(blob1, fileContent);

            // add the blob to the newCommit's TreeMap
            newCommit.blobs.put(fileName, fileHash);

            // delete this file from the staging area
            File staged_file = join(Repository.ADDING_AREA, fileName);
            staged_file.delete();
        }

        // loop through the removal area, updating the new commit's blobs
        for (String fileName : filesToRemove) {
            // remove the blob from the newCommit's TreeMap
            newCommit.blobs.remove(fileName);

            // delete this file from the removal area
            File removed_file = join(Repository.REMOVING_AREA, fileName);
            removed_file.delete();
        }

        // save the new commit to the commits directory
        File commitFile = join(Repository.COMMITS_DIR, newCommit.getId());
        writeObject(commitFile, newCommit);

        // set the headCommit and currBranch
        writeContents(headCommit, newCommit.getId());
        File branchFile = join(BRANCHES_DIR, readContentsAsString(currentBranch));
        writeContents(branchFile, newCommit.getId());
    }

    /**
     * Removes a file from the repository.
     *
     * @param fileName the name of the file to remove
     */
    public void remove(String fileName) {
        if (!gitletExists()) {
            return;
        }

        File stagedFile = join(ADDING_AREA, fileName);
        TreeMap<String, String> trackedFiles = getHeadCommit().getBlobs();

        boolean isStaged = stagedFile.exists();
        boolean isTracked = trackedFiles.containsKey(fileName);

        if (!isStaged && !isTracked) {
            Utils.message("No reason to remove the file.");
            return;
        }

        if (isTracked) {
            // Stage for removal and remove the file from the working directory.
            String blobId = trackedFiles.get(fileName);
            File blob = join(BLOBS_DIR, blobId);
            String contents = readContentsAsString(blob);
            File stagedForRemoval = join(REMOVING_AREA, fileName);
            writeContents(stagedForRemoval, contents);

            File workingDirFile = join(CWD, fileName);
            if (workingDirFile.exists()) {
                workingDirFile.delete();
            }
        }

        if (isStaged) {
            // Unstage the file if it is currently staged.
            stagedFile.delete();
        }
    }


    /**
     * Prints the commit history.
     */
    public void log() {
        if (!gitletExists()) {
            return;
        }

        // Load the head commit
        Commit headCommit = getHeadCommit();

        // Print commit history
        printLog(headCommit);
    }

    private void printLog(Commit commit) {
        if (commit == null) {
            return;
        }

        // Print commit info
        System.out.println("===");
        System.out.println("commit " + commit.getId());

        // If this commit is a merge commit, print the parent ids
        if (commit.getParentIds().size() > 1) {
            System.out.println("Merge: " + commit.getParentIds().get(0).substring(0, 7) + " "
                    + commit.getParentIds().get(1).substring(0, 7));
        }

        System.out.println("Date: " + commit.getFormattedTimestamp());
        System.out.println(commit.getMessage());
        System.out.println();

        // Recursively print the first parent commit
        if (!commit.getParentIds().isEmpty()) {
            if (commit.getParentIds().get(0) == null) {
                return;
            }
            Commit parentCommit = loadCommitFromId(commit.getParentIds().get(0));
            printLog(parentCommit);
        }
    }


    public Commit loadCommitFromId(String commitId) {
        int len = commitId.length();
        for (String commit: plainFilenamesIn(COMMITS_DIR)) {
            if (commit.substring(0, len).equals(commitId)) {
                return readObject(join(COMMITS_DIR, commit), Commit.class);
            }
        }
        System.out.println("No commit with that id exists.");
        return null;
    }

    /**
     * Prints the global commit history.
     */
    public void globalLog() {
        if (!gitletExists()) {
            return;
        }

        for (String commitId : plainFilenamesIn(COMMITS_DIR)) {
            Commit commit = loadCommitFromId(commitId);

            // If the commit is null (which should not happen), skip it
            if (commit == null) {
                continue;
            }
            printCommitInfo(commit);
        }
    }

    private void printCommitInfo(Commit commit) {
        System.out.println("===");
        System.out.println("commit " + commit.getId());

        // If this commit is a merge commit, print the parent ids
        if (commit.getParentIds().size() > 1) {
            System.out.println("Merge: " + commit.getParentIds().get(0).substring(0, 7) + " "
                    + commit.getParentIds().get(1).substring(0, 7));
        }

        System.out.println("Date: " + commit.getFormattedTimestamp());
        System.out.println(commit.getMessage());
        System.out.println();
    }

    /**
     * Finds commits with a matching message.
     *
     * @param message the commit message to search for
     */
    public void find(String message) {
        if (!gitletExists()) {
            return;
        }
        // A flag to check if any commit with the given message was found
        boolean commitFound = false;

        // Iterate through each commit file
        for (String commitId : plainFilenamesIn(COMMITS_DIR)) {
            File commitFile = join(COMMITS_DIR, commitId);
            Commit commit = readObject(commitFile, Commit.class);

            // Check if the commit's message matches the input message
            if (commit.getMessage().equals(message)) {
                System.out.println(commit.getId());
                commitFound = true;
            }
        }

        // If no commit with the given message was found, print an error message
        if (!commitFound) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * Prints the status of the repository.
     */
    public void status() {
        if (!gitletExists()) {
            return;
        }

        if (BRANCHES_DIR.exists()) {
            System.out.println("=== Branches ===");
            for (String branchName : plainFilenamesIn(BRANCHES_DIR)) {

                int length = readContentsAsString(currentBranch).length();
                int bNameLength = branchName.length();
                if (branchName.substring(0, bNameLength - 4).equals(readContentsAsString(currentBranch).substring(0, length - 4))) {
                    System.out.print("*");
                }
                System.out.println(branchName.substring(0, bNameLength - 4));
            }
        } else {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        System.out.println();

        // Staged files for addition
        System.out.println("=== Staged Files ===");
        File addingDir = ADDING_AREA;
        List<String> stagedFiles = plainFilenamesIn(addingDir);
        Collections.sort(stagedFiles);
        for (String fileName : stagedFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        // Staged files for removal
        System.out.println("=== Removed Files ===");
        File removingDir = REMOVING_AREA;
        List<String> removedFiles = plainFilenamesIn(removingDir);
        Collections.sort(removedFiles);
        for (String fileName : removedFiles) {
            System.out.println(fileName);
        }
        System.out.println();

        // Modifications not staged for commit
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        // Untracked files
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }


    public void restore(String commitId, String fileName) {
        if (!gitletExists()) {
            return;
        }
        Commit usingCommit;

        if (commitId == null) {
            usingCommit = getHeadCommit();
        } else {
            usingCommit = loadCommitFromId(commitId);
            if (usingCommit == null) {
                return;
            }
        }

        if (!usingCommit.getBlobs().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        // checking if there's such a file in CWD. If yes, delete it and do the job. If not, just do the job
        if (join(Repository.CWD, fileName).exists()) {
            join(Repository.CWD, fileName).delete();
        }
        String contents = readContentsAsString(join(BLOBS_DIR, usingCommit.getBlobs().get(fileName)));
        File restoredFile = join(Repository.CWD, fileName);
        writeContents(restoredFile, contents);
    }


    /**
     * Creates a new branch.
     *
     * @param branchName the name of the new branch
     */
    public void branch(String branchName) {
        if (!gitletExists()) {
            return;
        }
        String txtName = branchName + ".txt";

        // check if branchName already exists in BRANCHES_DIR
        if (join(BRANCHES_DIR, txtName).exists()) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        // otherwise, create a new file in BRANCHES_DIR and write contents
        File newBranchFile = join(BRANCHES_DIR, txtName);
        writeContents(newBranchFile, getHeadCommit().getId());
    }

    /**
     * Switches to a different branch.
     *
     * @param branchName the name of the branch to switch to
     */
    public void switchBranch(String branchName) {
        if (!gitletExists()) {
            return;
        }
        String txtName = branchName + ".txt";

        if (!join(BRANCHES_DIR, txtName).exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        if (readContentsAsString(currentBranch).equals(txtName)) {
            System.out.println("No need to switch to the current branch.");
            return;
        }
        String commitId = readContentsAsString(join(BRANCHES_DIR, txtName));
        overwriteCWD(commitId);

        // Clear the staging area and update currBranch and headCommit
        clearStagingArea();
        writeContents(currentBranch, txtName);
        writeContents(headCommit, commitId);
    }

    /**
     * Removes a branch.
     *
     * @param branchName the name of the branch to remove
     */
    public void removeBranch(String branchName) {
        if (!gitletExists()) {
            return;
        }
        String txtName = branchName + ".txt";
        File branch = join(BRANCHES_DIR, txtName);

        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if (txtName.equals(readContentsAsString(currentBranch))) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branch.delete();
        }
    }

    /**
     * Resets the repository to a specific commit.
     *
     * @param commitId the ID of the commit to reset to
     */
    public void reset(String commitId) {
        if (!gitletExists()) {
            return;
        }

        if (!join(COMMITS_DIR, commitId).exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        // Load the new commit from the given ID
        Commit newCommit = loadCommitFromId(commitId);

        // Overwrite the current working directory with the new commit's state
        overwriteCWD(commitId);

        // Clear the staging area
        clearStagingArea();

        // Update the current branch's HEAD and the global HEAD to the new commit
        String currentBranch = readContentsAsString(Repository.currentBranch);
        writeContents(join(BRANCHES_DIR, currentBranch), newCommit.getId());
        writeContents(headCommit, newCommit.getId());
    }


    /**
     * Merges changes from a branch.
     *
     * @param branchName the name of the branch to merge
     */
    public void merge(String branchName) {
        // TODO: Implement merge logic
    }

    /**
     * Checks if dir f is empty
     *
     * @return true is f is empty. false otherwise
     */
    public static boolean isDirEmpty(File f) {
        return Utils.plainFilenamesIn(f).isEmpty();
    }

    public boolean gitletExists() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        }
        return true;
    }

    public static Commit getHeadCommit() {
        String headCommitId = readContentsAsString(headCommit);
        File currCommitFile = join(COMMITS_DIR, headCommitId);
        Commit currCommit = readObject(currCommitFile, Commit.class);
        return currCommit;
    }

    public void clearStagingArea() {
        for (String filename : plainFilenamesIn(ADDING_AREA)) {
            join(ADDING_AREA, filename).delete();
        }
        for (String filename : plainFilenamesIn(REMOVING_AREA)) {
            join(REMOVING_AREA, filename).delete();
        }
    }

    public void overwriteCWD(String commitId) {
        TreeMap<String, String> trackedFiles = getHeadCommit().getBlobs();
        List<String> CWDFiles = plainFilenamesIn(CWD);
        Commit newCommit = loadCommitFromId(commitId);
        TreeMap<String, String> newTrackedFiles = newCommit.getBlobs();

        for (String file: CWDFiles) {
            if (!trackedFiles.containsKey(file) && newTrackedFiles.containsKey(file)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                return;
            } else if (trackedFiles.containsKey(file) && !newTrackedFiles.containsKey(file)) {
                join(Repository.CWD, file).delete();
            } else if (trackedFiles.containsKey(file) && newTrackedFiles.containsKey(file)) {
                join(Repository.CWD, file).delete();
                File overwrittenFile = join(Repository.CWD, file);
                writeContents(overwrittenFile, readContentsAsString(join(BLOBS_DIR, newTrackedFiles.get(file))));
            }
        }
        // handles the case when there's no such file in CWD but there's in the newBranch.
        // the following is an enhanced for loop for a TreeMap
        for (Map.Entry<String, String> entry : newTrackedFiles.entrySet()) {
            String file = entry.getKey();
            if (!join(CWD, file).exists()) {
                File overwrittenFile = join(CWD, file);
                writeContents(overwrittenFile, readContentsAsString(join(BLOBS_DIR, newTrackedFiles.get(file))));
            }
        }
    }
}
