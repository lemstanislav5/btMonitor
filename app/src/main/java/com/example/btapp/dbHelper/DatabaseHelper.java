package com.example.btapp.dbHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int version = 1;
    public  static String dbName="db.db";
    public static final String TABLE_NAME ="keys";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_KEY_STRING = "keyString";
    public static final String COLUMN_ADDRESS = "address";
    private static final String CREATE_TABLE = "create table if not exists "+ TABLE_NAME + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"+COLUMN_KEY_STRING+" TEXT NOT NULL," + COLUMN_ADDRESS + " TEXT);";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS "+ TABLE_NAME;
    public DatabaseHelper(Context context) {
        super(context,dbName,null,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

//    public int checkKeyCursor(String key){
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues cv = new ContentValues();
//    }
    public boolean InsertKeyString(String key){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_KEY_STRING,key);
        cv.put(COLUMN_ADDRESS,"не задано");

        long result = db.insert(TABLE_NAME,null,cv);
        return result != -1;

    }
    public void update(String address, String key){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ADDRESS,address);
        db.update(TABLE_NAME, cv, COLUMN_KEY_STRING +  " = ?", new String[]{key});
    }
    public void delete(String key){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_KEY_STRING +  " = ?", new String[]{key});
    }
}