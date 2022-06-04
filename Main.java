package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Map;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;


import static gitlet.Gitlet.*;
import static gitlet.Utils.plainFilenamesIn;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author annetta
 */
public class Main {
    /** remote path. */
    public static final String REMOTE_PATH = ".remote";
    /** remote dir. */
    public static final File REMOTE = new File(REMOTE_PATH);
    /** CWD path. */
    static final String PATH = System.getProperty("user.dir");
    /** Current Working Directory. */
    static final File CWD = new File(PATH);
    /** folder of a Gitlet. **/
    public static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");
    /** SHA-1 length. */
    static final int UID_LENGTH = 40;

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (args[0].equals("init") && validateNumArgs(args, 1)) {
            init();
        } else if (args[0].equals("add") && validateNumArgs(args, 2)) {
            add(args[1]);
        } else if (args[0].equals("commit") && validateNumArgs(args, 2)) {
            commit(args[1]);
        } else if (args[0].equals("rm") && validateNumArgs(args, 2)) {
            rm(args[1]);
        } else if (args[0].equals("log") && validateNumArgs(args, 1)) {
            log();
        } else if (args[0].equals("global-log") && validateNumArgs(args, 1)) {
            globallog();
        } else if (args[0].equals("find") && validateNumArgs(args, 2)) {
            find(args[1]);
        } else if (args[0].equals("status") && validateNumArgs(args, 1)) {
            status();
        } else if (args[0].equals("checkout")) {
            checkout(args);
        } else if (args[0].equals("branch") && validateNumArgs(args, 2)) {
            branch(args[1]);
        } else if (args[0].equals("rm-branch") && validateNumArgs(args, 2)) {
            rmbranch(args[1]);
        } else if (args[0].equals("reset") && validateNumArgs(args, 2)) {
            reset(args[1]);
        } else if (args[0].equals("merge") && validateNumArgs(args, 2)) {
            merge(args[1]);
        } else if (args[0].equals("add-remote") && validateNumArgs(args, 3)) {
            addremote(args[1], args[2]);
        } else if (args[0].equals("rm-remote") && validateNumArgs(args, 2)) {
            rmremote(args[1]);
        } else if (args[0].equals("push") && validateNumArgs(args, 3)) {
            push(args[1], args[2]);
        } else if (args[0].equals("fetch") && validateNumArgs(args, 3)) {
            fetch(args[1], args[2]);
        } else if (args[0].equals("pull") && validateNumArgs(args, 3)) {
            pull(args[1], args[2]);
        } else if (args[0].equals("diff")) {
            diff(args);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        return;
    }
    private static boolean validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        return true;
    }
    public static void init() throws IOException {
        if (GITLET_FOLDER.exists()) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        } else {
            GITLET_FOLDER.mkdirs();
            Commits init = new Commits("initial commit",
                    new TreeMap<String, Blobs>(), null, null);
            TreeMap<String, Commits> head = new TreeMap<>();
            head.put("master", init);
            Gitlet g = new Gitlet(head);
            g.saveGit();
        }
    }
    public static void add(String name) throws IOException {
        File b = Utils.join(PATH, name);
        if (!b.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Gitlet g = fromFile();
        if (g.getRemoved().containsKey(name)) {
            g.getRemoved().remove(name);
            g.saveGit();
            System.exit(0);
        }
        Blobs blobs = new Blobs(name);
        if (g.getBlobs().containsKey(name)) {
            if (!g.getBlobs().get(name).equals(blobs)) {
                g.getStaged().put(name, blobs);
            } else {
                g.getStaged().remove(name);
            }
        } else {
            g.getStaged().put(name, blobs);
        }
        g.saveGit();
    }
    public static void commit(String msg) throws IOException {
        if (msg.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Gitlet g = fromFile();
        if (g.getStaged().isEmpty() && g.getRemoved().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commits prev = g.getHeadC();
        TreeMap<String, Blobs> newblob = new TreeMap<>();
        newblob.putAll(prev.getBlobs());
        for (String s : g.getStaged().keySet()) {
            if (!newblob.containsKey(s)) {
                newblob.put(s, g.getStaged().get(s));
            } else {
                if (newblob.get(s).equals(g.getStaged().get(s))) {
                    System.out.println("No changes added to the commit.");
                    System.exit(0);
                } else {
                    newblob.put(s, g.getStaged().get(s));
                }
            }
        }
        Iterator<Map.Entry<String, Blobs>> iter = newblob.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Blobs> entry = iter.next();
            if (g.getRemoved().containsKey(entry.getKey())) {
                iter.remove();
            }
        }
        Commits newc = new Commits(msg, newblob, prev, null);
        g.addCommit(newHead(g.getBranch(), newc));
        g.clearStage();
        g.saveGit();
        if (g.getRemoteName() != null) {
            help(g);
        }
    }
    public static void help(Gitlet local) throws IOException {
        String dir = local.getUnerDir();
        String remote = dir + "/" + REMOTE_PATH;
        File d = Utils.join(remote, local.getRemoteName());
        File f = Utils.join(d, "gitlet");
        Gitlet g = fromFile(f);
        String branch = local.getBranch();
        if (!local.getT().get(branch).contains(g.getHeadC().getId())) {
            System.out.println("Please pull down"
                    + " remote changes before pushing.");
            System.exit(0);
        }
        ArrayList<Commits> cs = new ArrayList<>();
        if (!g.getAllBranch().contains(branch)) {
            g.addBranch(branch);
        }
        Commits localhead = local.getHeadC();
        while (localhead.getParent1() != null) {
            cs.add(localhead);
            localhead = localhead.getParent1();
        }
        cs.add(localhead);
        int i = cs.size() - 1;
        ArrayList<Commits> b = new ArrayList<>();
        while (i >= 0) {
            b.add(cs.get(i));
            i -= 1;
        }
        Branch bs = new Branch(branch, b);
        g.getT().put(branch, bs);
        for (Commits c : b) {
            g.getAllCommits().put(c.getId(), c);
        }
        g.setHead(branch, local.getHeadC());
        Utils.writeObject(f, g);
    }
    public static void rm(String name) throws IOException {
        Gitlet g = fromFile();
        Commits head = g.getHeadC();
        if (!g.getStaged().containsKey(name)
                && !g.getRemoved().containsKey(name)
                && !head.contains(name)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (g.getStaged().containsKey(name)) {
            g.getStaged().remove(name);
        } else if (head.contains(name)) {
            g.getRemoved().put(name, head.getBlobs().get(name));
            Utils.restrictedDelete(Utils.join(PATH, name));
        }
        g.saveGit();
    }
    public static void log() {
        Gitlet g = fromFile();
        Commits head = g.getHeadC();
        while (head.getId() != null) {
            if (head.getParent1() == null) {
                break;
            }
            System.out.print("===\n" + "commit " + head.getId() + "\n");
            if (head.getParent2() != null) {
                System.out.printf("Merge: %s %s %n",
                        head.getParent1().getId().substring(0, 7),
                        head.getParent2().getId().substring(0, 7));
            }
            System.out.println("Date: " + head.getTs());
            System.out.println(head.getMsg());
            System.out.println();
            head = head.getParent1();
        }
        System.out.println("===");
        System.out.println("commit " + head.getId());
        if (head.getParent2() != null) {
            System.out.println("Merge: "
                    + (head.getParent1().getId().substring(0, 7)) + " "
                    + head.getParent2().getId().substring(0, 7));
        }
        System.out.println("Date: " + head.getTs());
        System.out.println(head.getMsg());
    }
    public static void globallog() {
        Gitlet g = fromFile();
        TreeMap<String, Commits> all = g.getAllCommits();
        if (all.size() > 1) {
            for (Map.Entry<String,  Commits> entry : all.entrySet()) {
                System.out.println("===");
                System.out.println("commit " + entry.getKey());
                Commits p2 = entry.getValue().getParent2();
                if (p2 != null) {
                    String parent1 = entry.getValue().getParent1().getId();
                    System.out.println("Merge: "
                            + (parent1.substring(0, 7)) + " "
                            + p2.getId().substring(0, 7));
                }
                System.out.println("Date: " + entry.getValue().getTs());
                System.out.println(entry.getValue().getMsg());
                System.out.println();
            }
        } else {
            System.out.println("===");
            System.out.println("commit " + g.getHeadC().getId());
            System.out.println("Date: " + g.getHeadC().getTs());
            System.out.println(g.getHeadC().getMsg());
        }
    }
    public static void find(String msg) {
        if (msg.equals("")) {
            System.out.println("Incorrect operand.");
            System.exit(0);
        }
        Gitlet g = fromFile();
        String id = "";
        TreeMap<String, Commits> all = g.getAllCommits();
        for (Map.Entry<String, Commits> entry : all.entrySet()) {
            if (entry.getValue().getMsg().equals(msg)) {
                id = entry.getValue().getId();
                System.out.println(id);
            }
        }
        if (id.equals("")) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }
    public static void status() {
        Gitlet g = fromFile();
        TreeMap<String, Blobs> s = g.getStaged();
        TreeMap<String, Blobs> rm = g.getRemoved();
        TreeSet<String> modified = new TreeSet<>();
        TreeSet<String> deleted = new TreeSet<>();
        System.out.println("=== Branches ===");
        for (String b : g.getAllBranch()) {
            if (b.equals(g.getBranch())) {
                System.out.println("*" + b);
            } else {
                System.out.println(b);
            }
        }
        System.out.println("\n" + "=== Staged Files ===");
        for (String f : s.keySet()) {
            if (!plainFilenamesIn(PATH).contains(f)) {
                deleted.add(f);
            } else if (plainFilenamesIn(PATH).contains(f)
                    && !Arrays.equals(Utils.readContents(Utils.join(PATH, f)),
                    s.get(f).getBytes())) {
                modified.add(f);
            }
            if (s.get(f).equals(new Blobs(f))) {
                System.out.println(f);
            }
        }
        System.out.println("\n" + "=== Removed Files ===");
        for (String f : rm.keySet()) {
            if (plainFilenamesIn(PATH).contains(f)) {
                modified.add(f);
            } else {
                System.out.println(f);
            }
        }
        System.out.println("\n=== Modifications Not Staged For Commit ===");
        for (String f : g.getBlobs().keySet()) {
            if (!plainFilenamesIn(PATH).contains(f) && !rm.containsKey(f)) {
                deleted.add(f);
            }
            if (plainFilenamesIn(PATH).contains(f)
                    && !Arrays.equals(Utils.readContents(Utils.join(PATH, f)),
                    g.getBlobs().get(f).getBytes())) {
                modified.add(f);
            }
        }
        printD(deleted);
        printM(modified);
        System.out.println("\n" + "=== Untracked Files ===");
        if (plainFilenamesIn(PATH) != null) {
            for (String n : plainFilenamesIn(PATH)) {
                if (!s.containsKey(n) && !rm.containsKey(n)
                        && !g.getBlobs().containsKey(n)) {
                    System.out.println(n);
                }
            }
        }
    }
    public static void printM(TreeSet<String> modified) {
        for (String f : modified) {
            System.out.println(f + " (modified)");
        }
    }
    public static void printD(TreeSet<String> deleted) {
        for (String f : deleted) {
            System.out.println(f + " (deleted)");
        }
    }
    public static void checkout(String[] args) throws IOException {
        if (args.length == 3 && args[1].equals("--")) {
            checkoutfile(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            checkoutIDfile(args[1], args[3]);
        } else if (args.length == 2) {
            checkoutbranch(args[1]);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
    public static void checkoutfile(String name) throws IOException {
        Gitlet g = fromFile();
        if (g.getHead() == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if (!g.getBlobs().containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blobs commitblob = g.getBlobs().get(name);
        File f = Utils.join(PATH, name);
        Utils.writeContents(f, commitblob.getBytes());
        g.getStaged().remove(name);
        g.saveGit();
    }
    public static void checkoutIDfile(String id, String f) throws IOException {
        Gitlet g = fromFile();
        id = shortID(g, id);
        if (!g.getAllCommits().containsKey(id)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        if (!g.getAllCommits().get(id).contains(f)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blobs commitblob = g.getAllCommits().get(id).getBlobs().get(f);
        File file = Utils.join(PATH, f);
        if (!file.exists()) {
            file.createNewFile();
        }
        Utils.writeContents(file, commitblob.getBytes());
        g.getStaged().remove(f);
        g.saveGit();

    }
    public static void checkoutbranch(String branch) throws IOException {
        Gitlet g = fromFile();
        if (!g.getAllBranch().contains(branch)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branch.equals(g.getBranch())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        unTrack(g);
        ArrayList<Commits> l = g.getT().get(branch).getBranchC();
        g.setHead(branch, l.get(l.size() - 1));
        for (String f : plainFilenamesIn(PATH)) {
            if (!g.getBlobs().containsKey(f)) {
                Utils.restrictedDelete(f);
            } else {
                Utils.writeContents(g.getBlobs().get(f).getFile(),
                        g.getBlobs().get(f).getBytes());
            }
        }
        Iterator<Map.Entry<String, Blobs>> iter =
                g.getBlobs().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Blobs> entry = iter.next();
            Utils.writeContents(entry.getValue().getFile(),
                    entry.getValue().getBytes());
        }
        g.clearStage();
        g.saveGit();
    }
    public static void branch(String branch) throws IOException {
        Gitlet g = fromFile();
        if (g.getAllBranch().contains(branch)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        g.addBranch(branch);
        g.saveGit();
    }
    public static void rmbranch(String branch) throws IOException {
        Gitlet g = fromFile();
        if (!g.getAllBranch().contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (g.getBranch().equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        g.rmBranch(branch);
        g.saveGit();
    }
    public static String shortID(Gitlet g, String id) {
        if (id.length() < UID_LENGTH) {
            id = id.substring(0, 6);
            if (!g.getShort().containsKey(id)) {
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            id = g.getShort().get(id);
        }
        return id;
    }
    public static void reset(String id) throws IOException {
        Gitlet g = fromFile();
        id = shortID(g, id);
        if (!g.getAllCommits().containsKey(id)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        unTrack(g);
        Commits c = g.getAllCommits().get(id);
        String cur = g.getBranch();
        g.setHead(cur, c);
        for (String f : c.getBlobs().keySet()) {
            checkoutIDfile(id, f);
        }
        for (String f : plainFilenamesIn(PATH)) {
            if (!c.contains(f)) {
                Utils.restrictedDelete(f);
            }
        }
        g.clearStage();
        g.saveGit();
    }
    public static void unTrack(Gitlet g) {
        for (String n : plainFilenamesIn(PATH)) {
            if (!g.getStaged().containsKey(n) && !g.getRemoved().containsKey(n)
                    && !g.getBlobs().containsKey(n)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }
    public static void checkMerge(String branch, Gitlet g) {
        if (!g.getStaged().isEmpty() || !g.getRemoved().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!g.getAllBranch().contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (g.getBranch().equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        unTrack(g);
    }
    public static void checkSplit(Commits split, Commits given) {
        if (split.equals(given)) {
            System.out.println("Given branch is "
                    + "an ancestor of the current branch.");
            System.exit(0);
        }
    }
    public static void merge(String branch) throws IOException {
        Gitlet g = fromFile();
        checkMerge(branch, g);
        ArrayList<Commits> current = g.getT().get(g.getBranch()).getBranchC();
        ArrayList<Commits> other = g.getT().get(branch).getBranchC();
        Commits split = splitPoint2(current, other);
        Commits start = current.get(current.size() - 1);
        Commits given = other.get(other.size() - 1);
        checkSplit(split, given);
        if (split.equals(start)) {
            System.out.println("Current branch fast-forwarded.");
            checkoutbranch(branch);
            System.exit(0);
        }
        boolean isConflict = false;
        for (String f : given.getBlobs().keySet()) {
            if ((!split.contains(f) && !start.contains(f))
                    || (split.contains(f) && start.contains(f)
                    && split.getB(f).equals(start.getB(f))
                    && !split.getB(f).equals(given.getB(f)))) {
                checkoutIDfile(given.getId(), f);
                g.getStaged().put(f, given.getB(f));
            } else if (given.containEqual(f, split) && !start.contains(f)) {
                Utils.restrictedDelete(f);
            } else if ((split.contains(f) && !given.containEqual(f, split)
                && start.contains(f) && !start.containEqual(f, split))
                || (!split.contains(f) && start.contains(f)
                    && !given.containEqual(f, start))) {
                isConflict = true;
                mergeFile(f, start.getB(f), given.getB(f));
            }
        }
        for (String f : split.getBlobs().keySet()) {
            if (!given.getBlobs().containsKey(f)) {
                if (split.containEqual(f, start) && !given.contains(f)) {
                    rm(f);
                } else if ((start.contains(f) && !split.containEqual(f, start)
                        && !given.contains(f))
                        || (given.contains(f) && start.contains(f)
                        && !start.containEqual(f, given)
                        && !given.containEqual(f, split))) {
                    isConflict = true;
                    mergeFile(f, start.getB(f), given.getB(f));
                }
            }
        }
        Commits c = new Commits("Merged " + branch + " into " + g.getBranch()
                + ".", newBlobs(), start, given);
        g.addCommit(newHead(g.getBranch(), c));
        g.setHead(g.getBranch(), c);
        g.clearStage();
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
        g.saveGit();
    }
    public static TreeMap<String, Blobs> newBlobs() {
        TreeMap<String, Blobs> newblob = new TreeMap<>();
        for (String f : plainFilenamesIn(PATH)) {
            newblob.put(f, new Blobs(f));
        }
        return newblob;
    }
    public static TreeMap<String, Commits> newHead(String b, Commits c) {
        TreeMap<String, Commits> head = new TreeMap<>();
        head.put(b, c);
        return head;
    }
    public static Commits splitPoint2(ArrayList<Commits> current,
                                     ArrayList<Commits> other) {
        Commits split = null, start = current.get(current.size() - 1);
        Commits startOther = other.get(other.size() - 1);
        while (start != null && split == null) {
            startOther = other.get(other.size() - 1);
            while (startOther != null && split == null) {
                if (start.equals(startOther)) {
                    split = start;
                    break;
                }
                if (startOther.getParent2() != null) {
                    if (start.equals(startOther.getParent2())) {
                        split = start;
                        break;
                    }
                }
                if (start.getParent2() != null) {
                    if (start.getParent2().equals(startOther)) {
                        split = start.getParent2();
                        break;
                    }
                }
                if (startOther.getParent2() != null) {
                    if (start.getParent1().equals(startOther.getParent2())) {
                        split = start.getParent1();
                        break;
                    }
                }
                if (start.getParent2() != null) {
                    if (start.getParent2().equals(startOther.getParent1())) {
                        split = start.getParent2();
                        break;
                    }
                }
                startOther = startOther.getParent1();
            }
            start = start.getParent1();
        }
        return split;
    }
    public static void mergeFile(String f, Blobs cur, Blobs given)
            throws IOException {
        String a1 = "";
        String a2 = "";
        if (cur != null) {
            a1 = cur.getContent();
        }
        if (given != null) {
            a2 = given.getContent();
        }
        File file = Utils.join(PATH, f);
        if (!file.exists()) {
            file.createNewFile();
        }
        String b = "<<<<<<< HEAD\n" + a1 + "=======\n" + a2 + ">>>>>>>\n";
        Utils.writeContents(file, b);
    }
    public static void addremote(String name, String dir) throws IOException {
        if (!dir.contains("/.gitlet")) {
            System.exit(0);
        }
        REMOTE.mkdir();
        File file = Utils.join(REMOTE, name);
        if (file.exists()) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
        file.mkdir();
        Gitlet g = fromFile(dir);
        if (g != null) {
            g.setRemote(name, dir);
            g.savelocalGitfromRemote(name, dir);
            g.saveRemote(name);
        }
    }
    public static void rmremote(String name) throws IOException {
        File file = Utils.join(REMOTE, name);
        if (name.equals("") || !file.exists()) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        file.delete();
    }
    public static void checkRemote(String name) {
        File file = Utils.join(REMOTE, name);
        if (!file.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
    }
    public static void push(String name, String branch) throws IOException {
        checkRemote(name);
        Gitlet g = fromRemoteFile(name);
        Gitlet local = fromFile();
        if (!local.getT().get(branch).contains(g.getHeadC().getId())) {
            System.out.println("Please pull down"
                    + " remote changes before pushing.");
            System.exit(0);
        }
        ArrayList<Commits> cs = new ArrayList<>();
        if (!g.getAllBranch().contains(branch)) {
            g.addBranch(branch);
        }
        Commits localhead = local.getHeadC();
        while (localhead.getParent1() != null) {
            cs.add(localhead);
            localhead = localhead.getParent1();
        }
        cs.add(localhead);
        int i = cs.size() - 1;
        ArrayList<Commits> b = new ArrayList<>();
        while (i >= 0) {
            b.add(cs.get(i));
            i -= 1;
        }
        Branch bs = new Branch(branch, b);
        g.getT().put(branch, bs);
        for (Commits c : b) {
            g.getAllCommits().put(c.getId(), c);
        }
        g.setHead(branch, local.getHeadC());
        g.saveRemote(name);
        Gitlet other = fromFile(g.getRemoteDir());
        other.getT().put(branch, bs);
        for (Commits c : b) {
            other.getAllCommits().put(c.getId(), c);
        }
        other.setHead(branch, g.getHeadC());
        other.savefromRemote(g, g.getRemoteName(), g.getRemoteDir());
    }
    public static void fetch(String name, String branch) throws IOException {
        checkRemote(name);
        Gitlet g = fromFile();
        Gitlet remote = fromRemoteFile(name);
        if (!remote.getAllBranch().contains(branch)) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }
        String b = name + "/" + branch;
        Branch a = remote.getT().get(branch);
        Branch newa = new Branch(b, a.getBranchC());
        g.getAllBranch().add(b);
        g.getT().put(b, newa);
        for (Commits c : newa.getBranchC()) {
            g.getAllCommits().put(c.getId(), c);
        }
        g.saveGit();
    }
    public static void pull(String name, String branch) throws IOException {
        checkRemote(name);
        fetch(name, branch);
        merge(name + "/" + branch);
    }
    public static void diff(String[] args) throws IOException {
        if (args.length == 3) {
            diff3(args[1], args[2]);
        } else if (args.length == 2) {
            diff2(args[1]);
        } else if (args.length == 1) {
            diff1();
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
    public static void diff3(String branch1, String branch2) {
    }
    public static void diff2(String branch1) {
        Gitlet g = fromFile();
        if (!g.getAllBranch().contains(branch1)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }
    public static void diff1() {
        ArrayList<String> n1 = new ArrayList<>(), n2 = new ArrayList<>();
        Commits c = fromFile().getHeadC();
        List<String> cwd = plainFilenamesIn(CWD);
        for (String s : c.getBlobs().keySet()) {
            if (cwd.contains(s)) {
                print0(s, s);
            } else {
                System.out.println("diff --git a/" + s + "/dev/null");
                System.out.println("--- a/" + s);
                System.out.println("+++ " + "/dev/null");
            }
            File f = new File(s);
            String[] f1 = c.getB(s).getContent().replace("\s", "").split("\n");
            String[] f2 = Utils.readContentsAsString(f).replace("\s", "").split("\n");
            Collections.addAll(n1, f1); Collections.addAll(n2, f2);
        }
    }
    public static void print0(String f1, String f2) {
        System.out.println("diff --git a/" + f1 + " b/" + f2);
        System.out.println("--- a/" + f1);
        System.out.println("+++ b/" + f2);
    }
    public static void printout(int L1, int N1,
                                int L2, int N2, ArrayList<String> out) {
        printLine(L1, N1, L2, N2);
        while (!out.isEmpty()) {
            System.out.println("+" + out.get(0));
            out.remove(0);
        }
    }
    public static void printLine(int L1, int N1, int L2, int N2) {
        if (N1 != 1 && N2 != 1) {
            System.out.printf("@@ -%d,%d +%d,%d @@%n", L1, N1, L2, N2);
        } else if (N1 == 1 && N2 != 1) {
            System.out.printf("@@ -%d +%d,%d @@%n", L1, L2, N2);
        } else if (N1 != 1 && N2 == 1) {
            System.out.printf("@@ -%d,%d +%d @@%n", L1, N1, L2);
        } else {
            System.out.printf("@@ -%d +%d @@%n", L1, L2);
        }
    }
}
