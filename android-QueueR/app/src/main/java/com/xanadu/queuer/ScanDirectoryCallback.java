package com.xanadu.queuer;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dan on 7/6/14.
 */
public interface ScanDirectoryCallback {
    public void onTaskDone(ArrayList<File> results);
}
