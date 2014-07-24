package com.xanadu.queuer;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by dan on 7/6/14.
 */
public interface DecodeImageCallback {
    public void onTaskDone(ArrayList<Bitmap> results);
}
