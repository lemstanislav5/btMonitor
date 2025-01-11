package com.example.btapp.dbHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int version = 1;
    public  static String dbName="db.db";
    public static final String TABLE_NAME ="keys";
    public static final String COL1 = "id";
    public static final String COL2 = "keyString";
    public static final String COL3 = "address";
    private static final String CREATE_TABLE = "create table if not exists "+ TABLE_NAME + "(" + COL1 + " INTEGER PRIMARY KEY AUTOINCREMENT,"+COL2+" TEXT NOT NULL," + COL3 + " TEXT);";
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

    public boolean InsertKeyString(String key)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL2,key);
        cv.put(COL3,"не задано");

        long result = db.insert(TABLE_NAME,null,cv);
        return result != -1;

    }
}