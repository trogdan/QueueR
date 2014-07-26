package com.xanadu.queuer;

import com.google.zxing.Result;

import java.util.ArrayList;

/**
 * Created by dan on 7/6/14.
 */
public interface DecodeImageCallback {
    public void onTaskDone(ArrayList<Result> results);
}
