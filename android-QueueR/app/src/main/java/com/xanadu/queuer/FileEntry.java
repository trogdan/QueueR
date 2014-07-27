package com.xanadu.queuer;

/**
 * Created by dan on 7/26/14.
 */
public class FileEntry {

    @Override
    public String toString() {
        return "FileEntry [" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", lastModified=" + lastModified +
                ']';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    private int id;
    private String path;
    private long lastModified;

    public FileEntry(){}

}
