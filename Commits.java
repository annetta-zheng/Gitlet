package gitlet;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.Locale;

public class Commits implements Serializable {
    /** message of a commit. **/
    private String _msg;
    /** timestamp of a commit. **/
    private String _ts;
    /** files of a commit. **/
    private TreeMap<String, Blobs> _blobs;
    /** parent of a commit. **/
    private Commits _parent;
    /** parent2 of a commit. **/
    private Commits _parent2;
    /** sha-1 of a commit. **/
    private String _id;
    /** timestamp format. **/
    public static final DateFormat DT
            = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy XX", Locale.US);


    /** Main metadata folder.
     * @param msg
     * @param blobs
     * @param parent
     * @param parent2*/
    public Commits(String msg, TreeMap<String, Blobs> blobs,
                   Commits parent, Commits parent2) {
        _msg = msg;
        _blobs = blobs;
        _parent = parent;
        _parent2 = parent2;
        _id = Utils.sha1(Utils.serialize(this));
        if (msg.equals("initial commit")) {
            _ts = DT.format(new Date(0));
        } else {
            _ts = DT.format(new Date());
        }
    }
    public boolean equals(Commits o) {
        if (_id.equals(o._id)) {
            return true;
        }
        return false;
    }

    public String getMsg() {
        return _msg;
    }
    public String getTs() {
        return _ts;
    }
    public Commits getParent1() {
        return _parent;
    }
    public Commits getParent2() {
        return _parent2;
    }
    public String getId() {
        return _id;
    }
    public TreeMap<String, Blobs> getBlobs() {
        return _blobs;
    }
    public boolean containEqual(String f, Commits c) {
        if (!c.getBlobs().containsKey(f)) {
            return false;
        } else {
            if (_blobs.get(f).equals(c.getBlobs().get(f))) {
                return true;
            }
            return false;
        }
    }
    public boolean contains(String f) {
        return _blobs.containsKey(f);
    }
    public Blobs getB(String f) {
        return _blobs.get(f);
    }
}
