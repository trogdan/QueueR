package com.xanadu.queuer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by dan on 7/6/14.
 */
public class ScanDirectoryTask extends AsyncTask<String, Integer, Integer>{

    private ScanDirectoryCallback mScanDirectoryCallback;
    private ArrayList<File> mArrayList = new ArrayList<File>();

    public ScanDirectoryTask(ScanDirectoryCallback scanDirectoryCallback) {
        mScanDirectoryCallback = scanDirectoryCallback;
    }


    private class CustomComparator implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
        }
    }

    protected void walk(File root) {

        File[] list = root.listFiles(new ImageFileFilter());

        if(list == null)
            return;

        for (File f : list) {
            if (f.isDirectory()) {
                Log.d("", "Dir: " + f.getAbsoluteFile());
                walk(f);
            }
            else {
                Log.d("", "File: " + f.getAbsoluteFile());
                //TODO find QR code
                try {
                    //See if the file is already found
                    if (!mArrayList.contains(f.getCanonicalFile())) {
                        mArrayList.add(f.getCanonicalFile());
                    }
                } catch (IOException ioe) {
                    Log.e("", ioe.toString());
                }
            }
        }
    }

    protected Integer doInBackground(String... paths) {
        int count = paths.length;
        for (int i = 0; i < count; i++) {
            File root = new File(paths[i]);
            walk(root);
            //totalSize += Downloader.downloadFile(urls[i]);
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            if (isCancelled()) break;
        }
        //Sort
        Collections.sort(mArrayList, new CustomComparator());

        return mArrayList.size();
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Integer result) {
        mScanDirectoryCallback.onTaskDone(mArrayList);
        //showDialog("Found " + result + " files");
    }
}

