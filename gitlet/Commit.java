package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Represents a gitlet commit object.
 *
 *  @author Gabriel and Umar
 */
public class Commit implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String id;
    private String message;
    private ArrayList<String> parentIds;
    private Date timestamp;
    public TreeMap<String, String> blobs;

    /**
     * Creates a new commit with the given message and parent commit ID.
     *
     * @param message   The commit message.
     * @param parentId  The ID of the parent commit.
     */
    public Commit(String message, String parentId) {
        this.message = message;
        this.parentIds = new ArrayList<>();
        this.parentIds.add(parentId);
        this.blobs = new TreeMap<>();
        if (Repository.isDirEmpty(Repository.COMMITS_DIR)) {
            // Create the initial commit
            this.id = Utils.sha1((Object) message);
            this.timestamp = new Date(0);
        } else {
            this.timestamp = new Date();
            this.id = createCommitId(this.message, timestamp, this.parentIds);
        }
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        return formatter.format(timestamp);
    }


    /**
     * Returns the ID of the commit.
     *
     * @return The commit ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the commit message.
     *
     * @return The commit message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the ID of the parent commit.
     *
     * @return The parent commit ID.
     */
    public ArrayList<String> getParentIds() {
        return this.parentIds;
    }

    /**
     * Returns the timestamp of the commit.
     *
     * @return The commit timestamp.
     */
    public Date getTimestamp() {
        return timestamp;
    }


    public TreeMap<String, String> getBlobs() {
        return blobs;
    }

    public TreeMap copyThisBlobs (TreeMap copy) {
        this.blobs = new TreeMap<>();
        for (Object key: copy.keySet()) {
            String value = (String) copy.get(key);
            blobs.put(new String((String) key), new String(value));
        }
        return blobs;
    }

    /**
     * Returns the ID of the commit calculated based on its message, timestamp, and parent commit ID.
     *
     * @return The commit ID.
     */
    private String createCommitId(String message, Date timestamp, ArrayList<String> parentIds) {
        // Concatenate the commit's relevant data: message, timestamp, and parent commit ID
        String contents = message + timestamp.toString() + String.join(",", parentIds);
        return Utils.sha1((Object) contents);
    }
}