package com.xanadu.queuer;

import com.google.zxing.Result;

import java.io.File;

/**
 * Created by dan on 7/26/14.
 */
public class DecodedFile {
    public DecodedFile(File sourceFile, File cacheFile, Result decodeResult) {
        this.sourceFile = sourceFile;
        this.cacheFile = cacheFile;
        this.decodeResult = decodeResult;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public Result getDecodeResult() {
        return decodeResult;
    }

    public void setDecodeResult(Result decodeResult) {
        this.decodeResult = decodeResult;
    }

    protected File sourceFile;
    protected File cacheFile;
    protected Result decodeResult;

}
