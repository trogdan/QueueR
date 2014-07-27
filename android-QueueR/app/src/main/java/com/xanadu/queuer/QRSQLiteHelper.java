package com.xanadu.queuer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QRSQLiteHelper extends SQLiteOpenHelper {
	
	// Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "QrDb";

    private static QRSQLiteHelper mInstance;

    public static QRSQLiteHelper instance(Context context)
    {
        if(mInstance == null)
            mInstance = new QRSQLiteHelper(context);

        return mInstance;
    }

	private QRSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);	
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// SQL statement to create qr table
		String CREATE_QR_TABLE = "CREATE TABLE qrs ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				"title TEXT, "+
                "sourcePath TEXT, "+
				"thumbPath TEXT, "+
                "result TEXT, "+
                "sourceModified INTEGER)";

        // SQL statement to create files table
        String CREATE_FILE_TABLE = "CREATE TABLE files ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "path TEXT, "+
                "lastModified INTEGER)";

		// create qr table
		db.execSQL(CREATE_QR_TABLE);

        // create files table
        db.execSQL(CREATE_FILE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older qrs table if existed
        db.execSQL("DROP TABLE IF EXISTS qrs");

        // Drop older file table if existed
        db.execSQL("DROP TABLE IF EXISTS files");

        // create fresh qrs table
        this.onCreate(db);
	}
	//---------------------------------------------------------------------
   
    /**
     * CRUD operations (create "add", read "get", update, delete) qr + get all qrs + delete all qrs
     */
	
	// QRs table name
    private static final String TABLE_QRS = "qrs";
    
    // QRs Table Columns names
    private static final String QRS_KEY_ID = "id";
    private static final String QRS_KEY_TITLE = "title";
    private static final String QRS_KEY_SOURCE_PATH = "sourcePath";
    private static final String QRS_KEY_THUMB_PATH = "thumbPath";
    private static final String QRS_KEY_RESULT = "result";
    private static final String QRS_KEY_SOURCE_MODIFIED = "sourceModified";

    private static final String[] QRS_COLUMNS = {
            QRS_KEY_ID,
            QRS_KEY_TITLE,
            QRS_KEY_SOURCE_PATH,
            QRS_KEY_THUMB_PATH,
            QRS_KEY_RESULT,
            QRS_KEY_SOURCE_MODIFIED
    };
    
	public void addQRCode(QRCodeEntry entry){
		Log.d("addQRCode", entry.toString());
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		 
		// 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(QRS_KEY_TITLE, entry.getTitle()); // get title
        values.put(QRS_KEY_SOURCE_PATH, entry.getSourcePath()); // get source path
        values.put(QRS_KEY_THUMB_PATH, entry.getThumbPath()); // get thumb path
        values.put(QRS_KEY_RESULT, entry.getResult()); // get result
        values.put(QRS_KEY_SOURCE_MODIFIED, entry.getSourceModified()); // get modified

        // 3. insert
        db.insert(TABLE_QRS, // table
        		null, //nullColumnHack
        		values); // key/value -> keys = column names/ values = column values
        
        // 4. close
        db.close(); 
	}
	
	public QRCodeEntry getQRCode(int id){

		// 1. get reference to readable DB
		SQLiteDatabase db = this.getReadableDatabase();
		 
		// 2. build query
        Cursor cursor = 
        		db.query(TABLE_QRS, // a. table
                        QRS_COLUMNS, // b. column names
        		" id = ?", // c. selections 
                new String[] { String.valueOf(id) }, // d. selections args
                null, // e. group by
                null, // f. having
                null, // g. order by
                null); // h. limit
        
        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();
 
        // 4. build qr code object
        QRCodeEntry entry = new QRCodeEntry();
        entry.setId(Integer.parseInt(cursor.getString(0)));
        entry.setTitle(cursor.getString(1));
        entry.setSourcePath(cursor.getString(2));
        entry.setThumbPath(cursor.getString(3));
        entry.setResult(cursor.getString(4));
        entry.setSourceModified(Long.parseLong(cursor.getString(5)));

		Log.d("getQRCode(" + id + ")", entry.toString());

        // 5. return qr code
        return entry;
	}
	
	// Get All codes
    public List<QRCodeEntry> getAllQRCodes() {
        List<QRCodeEntry> entries = new LinkedList<QRCodeEntry>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_QRS;
 
    	// 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
 
        // 3. go over each row, build qr code and add it to list
        QRCodeEntry entry;
        if (cursor.moveToFirst()) {
            do {
                entry = new QRCodeEntry();
                entry.setId(Integer.parseInt(cursor.getString(0)));
                entry.setTitle(cursor.getString(1));
                entry.setSourcePath(cursor.getString(2));
                entry.setThumbPath(cursor.getString(3));
                entry.setResult(cursor.getString(4));
                entry.setSourceModified(Long.parseLong(cursor.getString(5)));

                // Add qrCode to qrCodes
                entries.add(entry);
            } while (cursor.moveToNext());
        }

		Log.d("getAllQRCodes()", entries.toString());

        // return qrCodes
        return entries;
    }

    // Get All codes
    public Map<String, QRCodeEntry> getAllQRCodesBySourcePath() {
        Map<String, QRCodeEntry> entries = new HashMap<String, QRCodeEntry>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_QRS;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build qr code and add it to list
        QRCodeEntry entry;
        if (cursor.moveToFirst()) {
            do {
                entry = new QRCodeEntry();
                entry.setId(Integer.parseInt(cursor.getString(0)));
                entry.setTitle(cursor.getString(1));
                entry.setSourcePath(cursor.getString(2));
                entry.setThumbPath(cursor.getString(3));
                entry.setResult(cursor.getString(4));
                entry.setSourceModified(Long.parseLong(cursor.getString(5)));

                // Add qrCode to qrCodes
                entries.put(entry.getSourcePath(), entry);
            } while (cursor.moveToNext());
        }

        Log.d("getAllQRCodesBySourcePath()", entries.toString());

        // return qrCodes
        return entries;
    }

	 // Updating single qr code
    public int updateQRCode(QRCodeEntry entry) {

    	// 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
		// 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(QRS_KEY_TITLE, entry.getTitle()); // get title
        values.put(QRS_KEY_SOURCE_PATH, entry.getSourcePath()); // get source path
        values.put(QRS_KEY_THUMB_PATH, entry.getThumbPath()); // get thumb path
        values.put(QRS_KEY_RESULT, entry.getResult()); // get result
        values.put(QRS_KEY_SOURCE_MODIFIED, entry.getSourceModified());

        // 3. updating row
        int i = db.update(TABLE_QRS, //table
        		values, // column/value
        		QRS_KEY_ID +" = ?", // selections
                new String[] { String.valueOf(entry.getId()) }); //selection args
        
        // 4. close
        db.close();
        
        return i;
    }

    // Deleting single qr code
    public void deleteQRCode(QRCodeEntry entry) {

    	// 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        
        // 2. delete
        db.delete(TABLE_QRS,
        		QRS_KEY_ID +" = ?",
                new String[] { String.valueOf(entry.getId()) });
        
        // 3. close
        db.close();
        
		Log.d("deleteQRCode", entry.toString());

    }

    /**
     * File CRUD operations (create "add", read "get", update, delete) file + get all files + delete all files
     */

    // files table name
    private static final String TABLE_FILES = "files";

    // files Table Columns names
    private static final String FILES_KEY_ID = "id";
    private static final String FILES_KEY_PATH = "path";
    private static final String FILES_KEY_LAST_MODIFIED = "lastModified";

    private static final String[] FILES_COLUMNS = {
            FILES_KEY_ID,
            FILES_KEY_PATH,
            FILES_KEY_LAST_MODIFIED
    };

    public void addFile(FileEntry entry){
        Log.d("addFile", entry.toString());

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(FILES_KEY_PATH, entry.getPath()); // get path
        values.put(FILES_KEY_LAST_MODIFIED, entry.getLastModified()); // get modified

        // 3. insert
        db.insert(TABLE_FILES, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public FileEntry getFile(int id){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        // 2. build query
        Cursor cursor =
                db.query(TABLE_FILES, // a. table
                        FILES_COLUMNS, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        // 3. if we got results get the first one
        if (cursor != null)
            cursor.moveToFirst();

        // 4. build file object
        FileEntry entry = new FileEntry();
        entry.setId(Integer.parseInt(cursor.getString(0)));
        entry.setPath(cursor.getString(1));
        entry.setLastModified(Long.parseLong(cursor.getString(2)));

        Log.d("getFile(" + id + ")", entry.toString());

        // 5. return file
        return entry;
    }

    // Get All files
    public List<FileEntry> getAllFiles() {
        List<FileEntry> entries = new LinkedList<FileEntry>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_FILES;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build file and add it to list
        FileEntry entry;
        if (cursor.moveToFirst()) {
            do {
                entry = new FileEntry();
                entry.setId(Integer.parseInt(cursor.getString(0)));
                entry.setPath(cursor.getString(1));
                entry.setLastModified(Long.parseLong(cursor.getString(5)));

                // Add file to files
                entries.add(entry);
            } while (cursor.moveToNext());
        }

        Log.d("getAllFiles()", entries.toString());

        // return files
        return entries;
    }

    // Get All files
    public Map<String, FileEntry> getAllFilesByPath() {
        Map<String, FileEntry> entries = new HashMap<String, FileEntry>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_FILES;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        // 3. go over each row, build file and add it to list
        FileEntry entry;
        if (cursor.moveToFirst()) {
            do {
                entry = new FileEntry();
                entry.setId(Integer.parseInt(cursor.getString(0)));
                entry.setPath(cursor.getString(1));
                entry.setLastModified(Long.parseLong(cursor.getString(5)));

                // Add file to files
                entries.put(entry.getPath(), entry);
            } while (cursor.moveToNext());
        }

        Log.d("getAllFilesByPath()", entries.toString());

        // return files
        return entries;
    }

    // Updating single file
    public int updateFile(FileEntry entry) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(FILES_KEY_PATH, entry.getPath()); // get path
        values.put(FILES_KEY_LAST_MODIFIED, entry.getLastModified()); // get modified

        // 3. updating row
        int i = db.update(TABLE_FILES, //table
                values, // column/value
                FILES_KEY_ID +" = ?", // selections
                new String[] { String.valueOf(entry.getId()) }); //selection args

        // 4. close
        db.close();

        return i;
    }

    // Deleting single file
    public void deleteFile(FileEntry entry) {

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. delete
        db.delete(TABLE_FILES,
                FILES_KEY_ID +" = ?",
                new String[] { String.valueOf(entry.getId()) });

        // 3. close
        db.close();

        Log.d("deleteFile", entry.toString());

    }
}
