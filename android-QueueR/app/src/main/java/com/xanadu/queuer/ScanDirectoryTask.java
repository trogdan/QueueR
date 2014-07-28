package com.xanadu.queuer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by dan on 7/6/14.
 */
public class ScanDirectoryTask extends AsyncTask<String, Integer, Integer>{

    private ScanDirectoryCallback mScanDirectoryCallback;
    private ArrayList<File> mArrayList = new ArrayList<File>();
    private QRSQLiteHelper mSqlHelper;

    public ScanDirectoryTask(ScanDirectoryCallback scanDirectoryCallback, Context context) {
        mScanDirectoryCallback = scanDirectoryCallback;
        mSqlHelper = QRSQLiteHelper.instance(context);
    }

    private ArrayList<File> getChangedFiles(ArrayList<File> scanResults)
    {
        Map<String, FileEntry> entries = mSqlHelper.getAllFilesByPath();

        //Take all ZEE files and remove ones that are already in the db
        //i.e. have tried to be decoded
        for(File f: scanResults)
        {
            try {
                FileEntry entry = entries.get(f.getCanonicalPath());
                if(entry != null)
                {
                    //Compare the date
                    if(f.lastModified() <= entry.getLastModified())
                    {
                        scanResults.remove(f);
                    }
                }
            } catch (IOException e) {
                //wtf
                scanResults.remove(f);
                e.printStackTrace();
            }
        }

        return scanResults;
    }

    private class CustomComparator implements Comparator<File> {
        @Override
        public int compare(File o1, File o2) {
            return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
        }
    }

    protected void walk(File root) {
        try {
            //Don't walk our own directory
            if(root.getCanonicalPath().equals(GlobalValues.THUMBS_DIRECTORY))
                return;
        } catch (IOException e) {
            //e.printStackTrace();
        }

        File[] list = root.listFiles(new ImageFileFilter());

        if(list == null)
            return;

        for (File f : list) {
            if (f.isDirectory()) {
                //Log.d("", "Dir: " + f.getAbsoluteFile());
                walk(f);
            }
            else {
                //Log.d("", "File: " + f.getAbsoluteFile());
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
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            if (isCancelled()) break;
        }

        //Check the db for already decoded images
        mArrayList = getChangedFiles(mArrayList);

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

