package com.bignerdranch.android.criminalintent.Model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CrimeBaseHelper extends SQLiteOpenHelper {
    private static final int version=4;
    private static final String DATABASE_NAME="crimeBase.db";

    public CrimeBaseHelper(Context context) {
        super(context, DATABASE_NAME,null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table "+ CrimeDbSchema.CrimeTable.NAME+"("+
                "_id integer primary key autoincrement, "+
                 CrimeDbSchema.CrimeTable.Cols.UUID+", "+
                 CrimeDbSchema.CrimeTable.Cols.TITLE+", "+
                 CrimeDbSchema.CrimeTable.Cols.DATE+", "+
                 CrimeDbSchema.CrimeTable.Cols.SOLVED+", "+
                 CrimeDbSchema.CrimeTable.Cols.SUSPECT+")"
                );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //sqLiteDatabase.execSQL("Drop Table "+CrimeDbSchema.CrimeTable.NAME);
    }
}
