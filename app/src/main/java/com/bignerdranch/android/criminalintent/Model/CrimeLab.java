package com.bignerdranch.android.criminalintent.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
//    private List<Crime> mCrimes;
    private Context context;
    private SQLiteDatabase database;

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }

        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        context=context.getApplicationContext();
        database=new CrimeBaseHelper(context).getWritableDatabase();
//        mCrimes = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            Crime crime = new Crime();
//            crime.setTitle("Crime #" + i);
//            crime.setSolved(i % 2 == 0);
//            mCrimes.add(crime);
//        }
    }

    public void addCrime(Crime crime)
    {
//        mCrimes.add(crime);
        ContentValues values=getContentvalues(crime);
        database.insert(CrimeDbSchema.CrimeTable.NAME,null,values);
    }
    public void updateCrime(Crime crime)
    {
        String uuidString=crime.getId().toString();
        ContentValues values=getContentvalues(crime);
        database.update(CrimeDbSchema.CrimeTable.NAME,values, CrimeDbSchema.CrimeTable.Cols.UUID+"=?"
                ,new String[]{uuidString});
    }
    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs)
    {
        Cursor cursor=database.query(CrimeDbSchema.CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,null,null);
        return new CrimeCursorWrapper(cursor);
    }
    public List<Crime> getCrimes() {
        //return mCrimes;
        List<Crime> crimes=new ArrayList<>();
        CrimeCursorWrapper cursor=queryCrimes(null,null);
        if(cursor!=null) {
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    crimes.add(cursor.getCrime());
                    cursor.moveToNext();
                }
            } finally {
                cursor.close();
            }
        }
        return crimes;
    }

    public Crime getCrime(UUID id) {
//        for (Crime crime : mCrimes) {
//            if (crime.getId().equals(id)) {
//                return crime;
//            }
//        }
    CrimeCursorWrapper cursor=queryCrimes(CrimeDbSchema.CrimeTable.Cols.UUID+"=?",
                                            new String[]{id.toString()});
        try {
            if(cursor.getCount()==0)
            {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getCrime();
        }
        finally {
            cursor.close();
        }
    }
    private static ContentValues getContentvalues(Crime crime)
    {
        ContentValues values=new ContentValues();
        values.put(CrimeDbSchema.CrimeTable.Cols.UUID,crime.getId().toString());
        values.put(CrimeDbSchema.CrimeTable.Cols.TITLE,crime.getTitle());
        values.put(CrimeDbSchema.CrimeTable.Cols.DATE,crime.getDate().getTime());
        values.put(CrimeDbSchema.CrimeTable.Cols.SOLVED,crime.isSolved()?1:0);
        values.put(CrimeDbSchema.CrimeTable.Cols.SUSPECT,crime.getSuspect());
        return values;
    }
}
