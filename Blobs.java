package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Main.PATH;

public class Blobs implements Serializable {
    /** sha-1 of a file. **/
    private String _fileid;
    /** content of a file. **/
    private String _content;
    /** bytes of a file. **/
    private byte[] _bytes;
    /** name of a file. **/
    private String _name;
    /** a file. **/
    private File _file;

    public Blobs(String name) {
        File f = Utils.join(PATH, name);
        if (f.exists()) {
            _file = f;
            _name = name;
            _bytes = Utils.readContents(f);
            _fileid = Utils.sha1(_bytes);
            _content = Utils.readContentsAsString(f);
        }
    }
    public String getName() {
        return _name;
    }
    public String getFileid() {
        return _fileid;
    }
    public byte[] getBytes() {
        return _bytes;
    }
    public File getFile() {
        return _file;
    }
    public String getContent() {
        return _content;
    }

    public boolean equals(Blobs o) {
        if (_name.equals(o.getName()) && _fileid.equals(o.getFileid())) {
            return true;
        }
        return false;
    }
    public boolean exists() {
        if (_name == null) {
            return false;
        }
        return true;
    }
}
