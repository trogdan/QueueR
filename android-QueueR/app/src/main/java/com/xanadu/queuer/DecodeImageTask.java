package com.xanadu.queuer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by dan on 7/10/14.
 */
public class DecodeImageTask extends AsyncTask<File, Integer, Integer> {

    private final DecodeImageCallback mDecodeImageCallback;
    private final MultiFormatReader mMultiFormatReader;
    private ArrayList<Bitmap> mThumbnailList = new ArrayList<Bitmap>();

    public DecodeImageTask(DecodeImageCallback decodeImageCallback, Map<DecodeHintType,Object> hints) {
        mDecodeImageCallback = decodeImageCallback;

        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(hints);
    }

    protected byte[] bitmapToBytes(Bitmap bitmap)
    {
        //calculate how many bytes our image consists of.
        int bytes = bitmap.getByteCount();

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
        bitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer

        byte[] place = buffer.array();

        return place; //Get the underlying array containing the data.
    }
    /**
     * A factory method to build the appropriate LuminanceSource object based on the format
     * of the preview buffers, as described by Camera.Parameters.
     *
     * @param data A preview frame.
     * @param width The width of the image.
     * @param height The height of the image.
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        /*
        Rect rect = getFramingRectInPreview();
        if (rect == null) {
            return null;
        }
        */
        // For now, encompass the entire image
        Rect rect = new Rect(0, 0, width-1, height-1);

        // Go ahead and assume it's YUV rather than die.
        return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private Result decode(int[] data, int width, int height) {
        Result rawResult = null;
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = mMultiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                String exception = re.toString();
                Log.e("", exception);
                // continue
            } finally {
                mMultiFormatReader.reset();
            }
        }
        return rawResult;
    }
    @Override
    protected Integer doInBackground(File... files) {
        int count = files.length;
        for (int i = 0; i < count; i++) {
            Bitmap bitmap = BitmapFactory.decodeFile(files[i].getAbsolutePath());
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            bitmap.recycle();
            Result result = decode(pixels, width, height);
            //publishProgress((int) ((i / (float) count) * 100));
            // Escape early if cancel() is called
            if (isCancelled()) break;
        }

        return mThumbnailList.size();
    }


    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Integer result) {
        mDecodeImageCallback.onTaskDone(mThumbnailList);
        //showDialog("Found " + result + " files");
    }
}
