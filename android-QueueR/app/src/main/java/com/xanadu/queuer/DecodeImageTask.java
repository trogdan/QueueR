package com.xanadu.queuer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.HybridBinarizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by dan on 7/10/14.
 */
public class DecodeImageTask extends AsyncTask<File, Integer, Integer> {

    private final DecodeImageCallback mDecodeImageCallback;
    private final MultiFormatReader mMultiFormatReader;
    private ArrayList<DecodedFile> mResultList = new ArrayList<DecodedFile>();
    private QRSQLiteHelper mSqlHelper;

    private static final int DEFAULT_THUMB_SIZE = 64;

    public DecodeImageTask(DecodeImageCallback decodeImageCallback,
                           Map<DecodeHintType,Object> hints,
                           Context context)
    {
        mDecodeImageCallback = decodeImageCallback;

        mMultiFormatReader = new MultiFormatReader();
        mMultiFormatReader.setHints(hints);

        mSqlHelper = QRSQLiteHelper.instance(context);
    }

    protected byte[] bitmapToBytes(Bitmap bitmap)
    {
        //calculate how many bytes our image consists of.
        int bytes = bitmap.getByteCount();

        //Create a new buffer
        ByteBuffer buffer = ByteBuffer.allocate(bytes);

        //Move the byte data to the buffer
        bitmap.copyPixelsToBuffer(buffer);

        //Get the raw bytes
        byte[] place = buffer.array();

        //Return the underlying array containing the data.
        return place;
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
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height)
    {
        /* Taken from zxing barcode scanner
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

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private String makeBarcodeFileName(String contents) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA");

            byte[] hash = messageDigest.digest(contents.getBytes("UTF-8"));

            for (int i = 0; i < hash.length; i++) {
                StringBuffer hexString = new StringBuffer();
                if ((0xff & hash[i]) < 0x10) {
                    hexString.append("0"
                            + Integer.toHexString((0xFF & hash[i])));
                } else {
                    hexString.append(Integer.toHexString(0xFF & hash[i]));
                }
                return hexString.toString();
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e("", e.toString());
            //e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            Log.e("", e.toString());
            //e.printStackTrace();
        }

        return null;
    }

    public File cacheResult(Result result)
    {
        if(isExternalStorageWritable())
        {
            //Convert the result to a bitmap
            Bitmap bitmap;
            try {
                bitmap = QRCodeEncoder.encodeAsBitmap(result.getText(), DEFAULT_THUMB_SIZE);
            } catch (WriterException we) {
                Log.w("", we);
                return null;
            }
            if (bitmap == null) {
                return null;
            }

            //Get the thumb folder
            File thumbRoot = new File(GlobalValues.THUMBS_DIRECTORY);

            if (!thumbRoot.exists() && !thumbRoot.mkdirs()) {
                Log.w("", "Couldn't make dir " + thumbRoot);
                //showErrorMessage(R.string.msg_unmount_usb);
                return null;
            }

            //Generate a UUID from the result text *should* only be one file per result
            String filename = makeBarcodeFileName(result.getText());
            if(filename == null) {
                return null;
            }

            File barcodeFile = new File(thumbRoot, filename + ".png");
            //Delete the old file if it exists
            if (!barcodeFile.delete()) {
                Log.w("", "Could not delete " + barcodeFile);
                // continue anyway
            }

            //Write the new file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(barcodeFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
            } catch (FileNotFoundException fnfe) {
                Log.w("", "Couldn't access file " + barcodeFile + " due to " + fnfe);
                //showErrorMessage(R.string.msg_unmount_usb);
                return null;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                        //success! phew
                        return barcodeFile;
                    } catch (IOException ioe) {
                        // do nothing
                    }
                }
            }
        }

        return null;
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private Result decode(int[] data, int width, int height)
    {
        Result rawResult = null;

        //Decoding regular ole rgb
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);

        if (source != null) {

            //create a binary representation
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            try {
                //try to decode the code, cross your fingers
                rawResult = mMultiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                Log.e("", re.toString());
                // continue
            } finally {
                // prepare for next image
                mMultiFormatReader.reset();
            }
        }
        return rawResult;
    }

    @Override
    protected Integer doInBackground(File... files)
    {
        //Ugh, fine, query the DB again.  should be the same as in ScanDirectoryTask
        Map<String, FileEntry> entries = mSqlHelper.getAllFilesByPath();

        int count = files.length;

        for (int i = 0; i < count; i++) {
            Bitmap bitmap = BitmapFactory.decodeFile(files[i].getAbsolutePath());

            //Get the dimensions
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            //Allocate space to store the bitmap pixels
            int[] pixels = new int[width * height];

            //Store the bitmap pixels, and clear the bitmap
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            bitmap.recycle();

            //See if we find a QR code
            Result result = decode(pixels, width, height);
            if(result != null)
            {
                //Cache a thumbnail for zee GUI
                File thumbnail = cacheResult(result);

                //Store the result on success
                DecodedFile decoded = new DecodedFile(files[i], thumbnail, result);
                mResultList.add(decoded);
            }

            //Make sure we record this file as having been processed
            try {
                FileEntry entry = entries.get(files[i].getCanonicalPath());
                if(entry != null)
                {
                    //Assume same path, new file time
                    entry.setLastModified(files[i].lastModified());
                    mSqlHelper.updateFile(entry);
                }
                else
                {
                    //don't think we need an id on an insert
                    entry = new FileEntry();
                    entry.setPath(files[i].getCanonicalPath());
                    entry.setLastModified(files[i].lastModified());
                    mSqlHelper.addFile(entry);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Sure, why not
            publishProgress((int) ((i / (float) count) * 100));

            // Escape early if cancel() is called
            if (isCancelled()) break;
        }

        return mResultList.size();
    }


    protected void onProgressUpdate(Integer... progress)
    {
        //TODO
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Integer result)
    {
        mDecodeImageCallback.onTaskDone(mResultList);
        //showDialog("Found " + result + " files");
    }
}
