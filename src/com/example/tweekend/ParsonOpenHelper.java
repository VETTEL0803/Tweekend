package com.example.tweekend;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ParsonOpenHelper extends SQLiteOpenHelper{
	final static private int DB_VERSION = 1;

    public ParsonOpenHelper(Context context) {
        super(context, "tweetlog.db", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // table create
        db.execSQL(
            "create table twit_log("+
            "   status_id INTEGER PRIMARY KEY,"+
            "   user_id TEXT NOT NULL,"+
            "	tweet TEXT NOT NULL,"+
            "	tweet_time TEXT NOT NULL," +
            "	photo1 BLOB," +
            "	photo2 BLOB," +
            "	photo3 BLOB," +
            "	photo4 BLOB" +
            ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // データベースの変更が生じた場合は、ここに処理を記述する。
    }
}