package com.example.tp2_grupo4.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.tp2_grupo4.data.model.LoggedUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DbRepository {
    DbHelper dbHelper;
    public DbRepository(Context context)
    {
        dbHelper = new DbHelper(context);
    }

    public long insertUser(String email, String refreshToken, String accessToken)
    {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("Email", email);
        contentValues.put("RefreshToken", refreshToken);
        contentValues.put("AccessToken", accessToken);
        contentValues.put("LastRefresh", dateFormat.format(date));
        contentValues.put("LastLogin", dateFormat.format(date));

        long id = db.insert("User", null , contentValues);

        return id;
    }

    @SuppressLint("Range")
    public boolean existUser()
    {
        Cursor cursor = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            cursor = db.rawQuery("SELECT * FROM User ORDER BY LastLogin desc LIMIT 1 ",null);
            if(cursor.getCount() > 0) {
                return true;
            }
        }finally {
            cursor.close();
        }
        return false;
    }

    public int updateLoggedUser(String email, String refreshToken, String accessToken)
    {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("RefreshToken",refreshToken);
        contentValues.put("AccessToken",accessToken);
        contentValues.put("LastRefresh",dateFormat.format(date));
        contentValues.put("LastLogin",dateFormat.format(date));

        String[] whereArgs= {email};
        int count = db.update("User",contentValues, "Email = ?", whereArgs);

        return count;
    }

//    public LoggedUser getUser()
//    {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        String[] columns = {"Id","Name","Password"};
//        Cursor cursor =db.query("User",columns,null,null,null,null,null);
//        StringBuffer buffer= new StringBuffer();
//        while (cursor.moveToNext())
//        {
//            @SuppressLint("Range") int cid = cursor.getInt(cursor.getColumnIndex("Id"));
//            @SuppressLint("Range") String name =cursor.getString(cursor.getColumnIndex("Name"));
//            @SuppressLint("Range") String  password =cursor.getString(cursor.getColumnIndex("Password"));
//            buffer.append(cid+ "-" + name + "-" + password +" \n");
//        }
//        return buffer.toString();
//    }

    @SuppressLint("Range")
    public LoggedUser getLoggedUser()
    {
        Cursor cursor = null;
        LoggedUser loggedUser = new LoggedUser();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            cursor = db.rawQuery("SELECT * FROM User ORDER BY LastLogin desc LIMIT 1 ",null);
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                loggedUser.userId = cursor.getInt(cursor.getColumnIndex("Id"));
                loggedUser.email = cursor.getString(cursor.getColumnIndex("Email"));
                loggedUser.refreshToken = cursor.getString(cursor.getColumnIndex("RefreshToken"));
                loggedUser.accessToken = cursor.getString(cursor.getColumnIndex("AccessToken"));
                loggedUser.lastRefresh = cursor.getLong(cursor.getColumnIndex("LastRefresh"));
                loggedUser.lastLogin = cursor.getLong(cursor.getColumnIndex("LastLogin"));
            }
        }finally {
            cursor.close();
        }

        return loggedUser;
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

   //CountryVisited
    public long insertCountryVisited(String countryName, String userId)
    {
        SQLiteDatabase dbb = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CountryName", countryName);
        contentValues.put("UserId", userId);
        long id = dbb.insert("CountriesVisited", null , contentValues);
        return id;
    }

    public List<String> getCountryMoreVisited()
    {
        List<String> array = new ArrayList<String>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {"Id","Name","Password"};
        Cursor cursor =db.rawQuery("SELECT CountryName, COUNT(*) as Counter FROM ActivityCountrySituation GROUP BY CountryName ORDER BY Counter desc LIMIT 5 ",null);

        while(cursor.moveToNext()){
            @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("CountryName")) + " (" + cursor.getString(cursor.getColumnIndex("Counter"))+ ")" ;
            array.add(data);
        }
        return array;
    }

    public List<String> getCountriesLessInfected()
    {
        List<String> array = new ArrayList<String>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {"Id","Name","Password"};
        Cursor cursor =db.rawQuery("SELECT CountryName, InfectedQty FROM CountryVisited ORDER BY InfectedQty desc LIMIT 5 ",null);

        while(cursor.moveToNext()){
            @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("CountryName")) + " (" + cursor.getString(cursor.getColumnIndex("InfectedQty"))+ ")" ;
            array.add(data);
        }
        return array;
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
                db.execSQL("CREATE TABLE IF NOT EXISTS User (Id INTEGER PRIMARY KEY AUTOINCREMENT, Email VARCHAR(255) UNIQUE, RefreshToken VARCHAR(255), AccessToken VARCHAR(255), LastRefresh DATETIME, LastLogin DATETIME);");
                db.execSQL("CREATE TABLE IF NOT EXISTS CountriesVisited (Id INTEGER PRIMARY KEY AUTOINCREMENT, CountryName VARCHAR(255), InfectedQty INTEGER, InsDate DATETIME);");
                db.execSQL("CREATE TABLE IF NOT EXISTS ActivityCountrySituation (Id INTEGER PRIMARY KEY AUTOINCREMENT, UserId INTEGER, CountryName VARCHAR(255), InfectedQty INTEGER);");
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
                db.execSQL("DROP TABLE IF EXISTS ActivityCountrySituation;");
                onCreate(db);
            }catch (Exception e) {
//                Message.message(context,""+e);
            }
        }
    }
}