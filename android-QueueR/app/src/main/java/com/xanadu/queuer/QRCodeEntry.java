package com.xanadu.queuer;


public class QRCodeEntry {

	private int id;
	private String title;
	private String sourcePath;
    private String thumbPath;
    private String result;
    private long sourceModified;

    public QRCodeEntry(){}

	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSourcePath() {
		return sourcePath;
	}
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}
    public String getThumbPath() {
        return thumbPath;
    }
    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }
    public long getSourceModified() {
        return sourceModified;
    }
    public void setSourceModified(long sourceModified) {
        this.sourceModified = sourceModified;
    }
    public String getResult() {
        return result;
    }
    public void setResult(String result) {
        this.result = result;
    }

    @Override
	public String toString() {
		return "QRCode [id=" + id + ", title=" + title + ", sourcePath=" + sourcePath
				+ ", thumbPath=" + thumbPath + "]";
	}
	
	
	
}
