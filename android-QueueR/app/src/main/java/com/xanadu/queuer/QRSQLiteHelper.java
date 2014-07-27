package com.xanadu.queuer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

public class QRSQLiteHelper extends SQLiteOpenHelper {
	
	// Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "QrDb";
   
	public QRSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);	
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// SQL statement to create book table
		String CREATE_QR_TABLE = "CREATE TABLE qrs ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + 
				"title TEXT, "+
                "sourcePath TEXT, "+
				"thumbPath TEXT )";
		
		// create books table
		db.execSQL(CREATE_QR_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older qrs table if existed
        db.execSQL("DROP TABLE IF EXISTS qrs");
        
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
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_SOURCE_PATH = "sourcePath";
    private static final String KEY_THUMB_PATH = "thumbPath";

    private static final String[] COLUMNS = {KEY_ID,KEY_TITLE, KEY_SOURCE_PATH, KEY_THUMB_PATH};
    
	public void addQRCode(QRCodeEntry entry){
		Log.d("addQRCode", entry.toString());
		// 1. get reference to writable DB
		SQLiteDatabase db = this.getWritableDatabase();
		 
		// 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, entry.getTitle()); // get title
        values.put(KEY_SOURCE_PATH, entry.getSourcePath()); // get source path
        values.put(KEY_THUMB_PATH, entry.getThumbPath()); // get thumb path

        // 3. insert
        db.insert(TABLE_QRS, // table
        		null, //nullColumnHack
        		values); // key/value -> keys = column names/ values = column values
        
        // 4. close
        db.close(); 
	}
	
	public QRCodeEntry getBook(int id){

		// 1. get reference to readable DB
		SQLiteDatabase db = this.getReadableDatabase();
		 
		// 2. build query
        Cursor cursor = 
        		db.query(TABLE_QRS, // a. table
        		COLUMNS, // b. column names
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

		Log.d("getQRCode(" + id + ")", entry.toString());

        // 5. return qr code
        return entry;
	}
	
	// Get All Books
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

                // Add qrCode to qrCodes
                entries.add(entry);
            } while (cursor.moveToNext());
        }

		Log.d("getAllQRCodes()", entries.toString());

        // return qrCodes
        return entries;
    }
	
	 // Updating single qr code
    public int updateQRCode(QRCodeEntry entry) {

    	// 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
 
		// 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("title", entry.getTitle()); // get title
        values.put("sourcePath", entry.getSourcePath()); // get source path
        values.put("thumbPath", entry.getThumbPath()); // get thumb path

        // 3. updating row
        int i = db.update(TABLE_QRS, //table
        		values, // column/value
        		KEY_ID+" = ?", // selections
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
        		KEY_ID+" = ?",
                new String[] { String.valueOf(entry.getId()) });
        
        // 3. close
        db.close();
        
		Log.d("deleteQRCode", entry.toString());

    }
}
