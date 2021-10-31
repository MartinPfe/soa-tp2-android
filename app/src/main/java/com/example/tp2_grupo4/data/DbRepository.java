package com.example.tp2_grupo4.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DbRepository {
    DbHelper dbHelper;
    public DbRepository(Context context)
    {
        dbHelper = new DbHelper(context);
    }

    public long insertUser(String name, String pass)
    {
        SQLiteDatabase dbb = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Name", name);
        contentValues.put("Password", pass);
        long id = dbb.insert("User", null , contentValues);
        return id;
    }

    public String getUserName()
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {"Id","Name","Password"};
        Cursor cursor =db.query("User",columns,null,null,null,null,null);
        StringBuffer buffer= new StringBuffer();
        while (cursor.moveToNext())
        {
            @SuppressLint("Range") int cid = cursor.getInt(cursor.getColumnIndex("Id"));
            @SuppressLint("Range") String name =cursor.getString(cursor.getColumnIndex("Name"));
            @SuppressLint("Range") String  password =cursor.getString(cursor.getColumnIndex("Password"));
            buffer.append(cid+ "-" + name + "-" + password +" \n");
        }
        return buffer.toString();
    }

    public  int deleteUser(String uname)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] whereArgs ={uname};

        int count =db.delete("User" ,"Name"+" = ?",whereArgs);
        return  count;
    }

    public int updatePassword(String userName, String oldPassword , String newPassword)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Password",newPassword);
        String[] whereArgs= {userName, oldPassword};
        int count =db.update("User",contentValues, "Name = ? AND Password = ?",whereArgs );
        return count;
    }

//    CountryVisited
    public long insertCountryVisited(String countryName, String userId)
    {
        SQLiteDatabase dbb = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CountryName", countryName);
        contentValues.put("UserId", userId);
        long id = dbb.insert("CountriesVisited", null , contentValues);
        return id;
    }



    static class DbHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "db";    // Database Name
        private static final int DATABASE_Version = 1;    // Database Version

        private Context context;

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_Version);
            this.context=context;
        }

        public void onCreate(SQLiteDatabase db) {

            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS User (Id INTEGER PRIMARY KEY AUTOINCREMENT, Name VARCHAR(255) UNIQUE, Password VARCHAR(255));");
                db.execSQL("CREATE TABLE IF NOT EXISTS CountriesVisited (Id INTEGER PRIMARY KEY AUTOINCREMENT, CountryName VARCHAR(255), UserId INTEGER);");
                db.execSQL("CREATE TABLE IF NOT EXISTS ActivityLogin (Id INTEGER PRIMARY KEY AUTOINCREMENT, UserId INTEGER, LoginDate DATETIME);");
            } catch (Exception e) {
                Log.println(Log.ERROR, "ERROR", "Error OnCreate SQLite. ex: " + e.getMessage());
//                Message.message(context,""+e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
/*                Message.message(context,"OnUpgrade");*/
                db.execSQL("DROP TABLE IF EXISTS CountriesVisited;");
                db.execSQL("DROP TABLE IF EXISTS User;");
                db.execSQL("DROP TABLE IF EXISTS ActivityLogin;");
                onCreate(db);
            }catch (Exception e) {
//                Message.message(context,""+e);
            }
        }
    }
}