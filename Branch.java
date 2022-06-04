package gitlet;

import java.io.Serializable;
import java.util.ArrayList;

public class Branch implements Serializable {
    /** string of a branch. **/
    private String _branch;
    /** commits chain under the branch. **/
    private ArrayList<Commits> _commit;

    public Branch(String branch, ArrayList<Commits> commits) {
        _branch = branch;
        _commit = commits;
    }
    public Branch(String branch) {
        _branch = branch;
        _commit = new ArrayList<>();
    }
    public ArrayList<Commits> getBranchC() {
        return _commit;
    }
    public boolean contains(String cid) {
        for (Commits c : _commit) {
            if (c.getId().equals(cid)) {
                return true;
            }
        }
        return false;
    }
    public Commits getC(String id) {
        for (Commits c : _commit) {
            if (c.getId().equals(id)) {
                return c;
            }
        }
        return null;
    }
    public void addC(Commits c) {
        _commit.add(c);
    }
    public void resetC(Commits c) {
        ArrayList<Commits> a = new ArrayList<>();
        while (c.getParent1() != null) {
            a.add(c);
            c = c.getParent1();
        }
        a.add(c);
        int i = a.size() - 1;
        ArrayList<Commits> b = new ArrayList<>();
        while (i >= 0) {
            b.add(a.get(i));
            i -= 1;
        }
        _commit = b;
    }
}
