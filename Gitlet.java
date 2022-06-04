package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;


import static gitlet.Main.*;

public class Gitlet implements Serializable {
    /** head commit map of a Gitlet. **/
    private TreeMap<String, Commits> _head;
    /** head commit of a Gitlet. **/
    private Commits _headcommit;
    /** current branch of a Gitlet. **/
    private String _curbranch;
    /** all branches of a Gitlet. **/
    private ArrayList<String> _allbranches = new ArrayList<>();
    /** all sha-1 but substring of a Gitlet. **/
    private TreeMap<String, String> _allshort = new TreeMap<>();
    /** branch tree of a Gitlet. **/
    private TreeMap<String, Branch> _t = new TreeMap<>();
    /** all commits of a Gitlet. **/
    private TreeMap<String, Commits> _allcommits = new TreeMap<>();
    /** additional files staged of a Gitlet. **/
    private TreeMap<String, Blobs> _staged;
    /** removed files staged of a Gitlet. **/
    private TreeMap<String, Blobs> _removed;
    /** remote dir.**/
    private String _remoteddir;
    /** remote name.**/
    private String _remotedname;
    /** remote under folder.**/
    private String _remoteunderdir;

    public Gitlet(TreeMap<String, Commits> head) {
        _head = head;
        _headcommit = head.lastEntry().getValue();
        _curbranch = head.lastEntry().getKey();
        _allbranches.add(_curbranch);
        _allshort.put(head.get(_curbranch).getId().substring(0, 6),
                head.get(_curbranch).getId());
        _allcommits.put(head.get(_curbranch).getId(), _headcommit);
        ArrayList<Commits> cs = new ArrayList<>();
        cs.add(_headcommit);
        _t.put(_curbranch, new Branch(_curbranch, cs));
        _staged = new TreeMap<>();
        _removed = new TreeMap<>();
        _remoteddir = null;
        _remotedname = null;
        _remoteunderdir = null;
    }
    public void saveGit() throws IOException {
        Gitlet g = new Gitlet(_head);
        g._headcommit = _headcommit;
        g._curbranch = _curbranch;
        g._allbranches = _allbranches;
        g._allshort = _allshort;
        g._staged = _staged;
        g._removed = _removed;
        g._allcommits = _allcommits;
        g._t = _t;
        g._remoteddir = _remoteddir;
        g._remotedname = _remotedname;
        g._remoteunderdir = _remoteunderdir;
        File d = Utils.join(GITLET_FOLDER, "gitlet");
        if (!d.exists()) {
            d.createNewFile();
        }
        Utils.writeObject(d, g);
    }
    public static Gitlet fromFile() {
        File d = Utils.join(GITLET_FOLDER, "gitlet");
        if (!d.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        return Utils.readObject(d, Gitlet.class);
    }
    public static Gitlet fromRemoteFile(String name) {
        File d = Utils.join(REMOTE, name);
        if (!d.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        File f = Utils.join(d, "gitlet");
        if (!f.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        return Utils.readObject(f, Gitlet.class);
    }
    public void savelocalGitfromRemote(String name, String dir)
            throws IOException {
        Gitlet g = new Gitlet(_head);
        g._headcommit = _headcommit;
        g._curbranch = _curbranch;
        g._allbranches = _allbranches;
        g._allshort = _allshort;
        g._staged = _staged;
        g._removed = _removed;
        g._allcommits = _allcommits;
        g._t = _t;
        g._remoteddir = dir;
        g._remotedname = name;
        g._remoteunderdir = System.getProperty("user.dir");
        File d = Utils.join(dir, "gitlet");
        if (!d.exists()) {
            d.createNewFile();
        }
        Utils.writeObject(d, g);
    }
    public void savefromRemote(Gitlet g1, String name, String dir)
            throws IOException {
        Gitlet g = new Gitlet(g1._head);
        g._headcommit = g1._headcommit;
        g._curbranch = g1._curbranch;
        g._allbranches = g1._allbranches;
        g._allshort = g1._allshort;
        g._staged = g1._staged;
        g._removed = g1._removed;
        g._allcommits = g1._allcommits;
        g._t = g1._t;
        g._remoteddir = dir;
        g._remotedname = name;
        g._remoteunderdir = g1._remoteunderdir;
        File d = Utils.join(dir, "gitlet");
        if (!d.exists()) {
            d.createNewFile();
        }
        Utils.writeObject(d, g);
    }
    public void saveRemote(String name) throws IOException {
        Gitlet g = new Gitlet(_head);
        g._headcommit = _headcommit;
        g._curbranch = _curbranch;
        g._allbranches = _allbranches;
        g._allshort = _allshort;
        g._staged = _staged;
        g._removed = _removed;
        g._allcommits = _allcommits;
        g._t = _t;
        g._remoteddir = _remoteddir;
        g._remotedname = _remotedname;
        File d = Utils.join(REMOTE, name);
        if (!d.exists()) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        File f = Utils.join(d, "gitlet");
        if (!f.exists()) {
            f.createNewFile();
        }
        Utils.writeObject(f, g);
    }
    public static Gitlet fromFile(String dir) {
        File f = Utils.join(dir, "gitlet");
        if (!f.exists()) {
            return null;
        }
        return Utils.readObject(f, Gitlet.class);
    }
    public static Gitlet fromFile(File f) {
        return Utils.readObject(f, Gitlet.class);
    }
    public void setRemote(String name, String dir) {
        _remoteddir = dir;
        _remotedname = name;
        _remoteunderdir = System.getProperty("user.dir");
    }
    public String getRemoteDir() {
        return _remoteddir;
    }
    public String getUnerDir() {
        return _remoteunderdir;
    }
    public String getRemoteName() {
        return _remotedname;
    }
    public void addBranch(String branch) {
        _allbranches.add(branch);
        _t.put(branch, new Branch(branch));
        _t.get(branch).addC(_headcommit);
    }
    public void rmBranch(String branch) {
        _allbranches.remove(branch);
        _t.remove(branch);
    }
    public void addCommit(TreeMap<String, Commits> c) {
        _head = c;
        _curbranch = c.lastEntry().getKey();
        _headcommit = c.lastEntry().getValue();
        _allcommits.put(c.get(_curbranch).getId(), _headcommit);
        _allshort.put(c.get(_curbranch).getId().substring(0, 6),
                c.get(_curbranch).getId());
        _t.get(_curbranch).addC(_headcommit);
    }
    public void clearStage() {
        _removed.clear();
        _staged.clear();
    }
    public void setHead(String branch, Commits c) {
        _curbranch = branch;
        _headcommit = c;
        _head = new TreeMap<>();
        _head.put(branch, c);
        if (_t.get(_curbranch).getBranchC().contains(c)) {
            _t.get(_curbranch).resetC(c);
        } else {
            _t.get(_curbranch).addC(c);
        }
    }

    public TreeMap<String, Branch> getT() {
        return _t;
    }
    public String getBranch() {
        return _curbranch;
    }
    public TreeMap<String, Blobs> getBlobs() {
        return _headcommit.getBlobs();
    }
    public ArrayList<String> getAllBranch() {
        return _allbranches;
    }
    public Commits getHeadC() {
        return _headcommit;
    }
    public TreeMap<String, Commits> getHead() {
        return _head;
    }
    public TreeMap<String, Commits> getAllCommits() {
        return _allcommits;
    }
    public TreeMap<String, String> getShort() {
        return _allshort;
    }
    public TreeMap<String, Blobs> getStaged() {
        return _staged;
    }
    public TreeMap<String, Blobs> getRemoved() {
        return _removed;
    }
}
