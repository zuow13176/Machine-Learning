package com.example.falldetection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SQLiteDB extends SQLiteOpenHelper {
    public static final String CREATE_TABLE = "create table " + MyConstant.TABLE_NAME +
            " (" + MyConstant.COL_ID + " INTEGER, " + MyConstant.COL_X + " REAL, " + MyConstant.COL_Y +
            " REAL, " + MyConstant.COL_Z + " REAL, " + MyConstant.COL_CLASS + " varchar(10))";
    private SQLiteDatabase db;

    public SQLiteDB(@Nullable Context context) {
        super(context, MyConstant.DB_NAME, null, 1);
        db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatebaseHelper","Create dataset");
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
    }

    public long insertRecord(float x, float y, float z, String class_) {
        ContentValues values = new ContentValues();
        values.put(MyConstant.COL_X, x);
        values.put(MyConstant.COL_Y, y);
        values.put(MyConstant.COL_Z, z);
        values.put(MyConstant.COL_CLASS, class_);
        return db.insert(MyConstant.TABLE_NAME,null, values);
    }

    public List<DataBean> queryRecord() {
        //Cursor cursor = db.query(MyConstant.TABLE_NAME, null, null,null,
        //        null, null, null +" desc");
        Cursor cursor = db.rawQuery("SELECT * FROM " + MyConstant.TABLE_NAME, null);
        List<DataBean> recordBeanList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                recordBeanList.add(new DataBean(Float.parseFloat(cursor.getString(1)),
                        Float.parseFloat(cursor.getString(2)),
                        Float.parseFloat(cursor.getString(3)),
                        cursor.getString(4)));
            } while (cursor.moveToNext());
            // moving our cursor to next.
        }
        cursor.close();
        return recordBeanList;
    }
}
