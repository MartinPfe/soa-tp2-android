package com.example.tp2_grupo4.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.tp2_grupo4.data.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;


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
    public boolean existUser(String email)
    {
        Cursor cursor = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            String[] whereArgs = {email};
            cursor = db.rawQuery("SELECT * FROM User WHERE Email = ? ORDER BY LastLogin desc LIMIT 1 ", whereArgs);
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

    @SuppressLint("Range")
    public User getLoggedUser()
    {
        Cursor cursor = null;
        User user = new User();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            cursor = db.rawQuery("SELECT * FROM User ORDER BY LastLogin desc LIMIT 1 ",null);
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                user.userId = cursor.getInt(cursor.getColumnIndex("Id"));
                user.email = cursor.getString(cursor.getColumnIndex("Email"));
                user.refreshToken = cursor.getString(cursor.getColumnIndex("RefreshToken"));
                user.accessToken = cursor.getString(cursor.getColumnIndex("AccessToken"));
                //DATES
                String lastRefreshString = cursor.getString(cursor.getColumnIndex("LastRefresh"));
                Calendar lastRefreshDate = new GregorianCalendar();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                lastRefreshDate.setTime(sdf.parse(lastRefreshString));
                user.lastRefresh = lastRefreshDate.getTimeInMillis();

                String lastLoginString = cursor.getString(cursor.getColumnIndex("LastRefresh"));
                Calendar lastLoginDate = new GregorianCalendar();
                lastLoginDate.setTime(sdf.parse(lastLoginString));
                user.lastLogin = lastLoginDate.getTimeInMillis();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return user;
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


    public long insertLocalCountry(String countryName, int infectedQty)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CountryName", countryName);
        contentValues.put("InfectedQty", infectedQty);
        long id = db.insert("LocalCountry", null , contentValues);
        return id;
    }

    public long getLocalCountry(String countryName, int infectedQty)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("CountryName", countryName);
        contentValues.put("InfectedQty", infectedQty);
        long id = db.insert("LocalCountry", null , contentValues);
        return id;
    }

    public long insertCountryInfection(int userId, String countryName, int infectedQty)
    {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("UserId", userId);
        contentValues.put("CountryName", countryName);
        contentValues.put("InfectedQty", infectedQty);
        contentValues.put("InsDate",dateFormat.format(date));

        long id = db.insert("CountriesInfection", null , contentValues);
        return id;
    }

    public List<String> getCountryMoreVisited()
    {

        List<String> array = new ArrayList<String>();
        try {

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT CountryName, COUNT(*) as Counter FROM CountriesInfection GROUP BY CountryName ORDER BY COUNT(*) desc LIMIT 5 ", null);

            while (cursor.moveToNext()) {
                @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex("CountryName")) + " (" + cursor.getString(cursor.getColumnIndex("Counter")) + ")";
                array.add(data);
            }
        }
        catch (Exception ex){
            Log.println(Log.ERROR,"Error",ex.getMessage());
        }
        return array;
    }

    public List<String> getCountriesLessInfected()
    {
        List<String> array = new ArrayList<String>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor =db.rawQuery("SELECT * FROM (SELECT DISTINCT CountryName, InfectedQty FROM CountriesInfection ORDER BY InsDate DESC) ORDER BY InfectedQty asc LIMIT 5 ",null);

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
                db.execSQL("CREATE TABLE IF NOT EXISTS LocalCountry (Id INTEGER PRIMARY KEY AUTOINCREMENT, CountryName VARCHAR(255) UNIQUE, InfectedQty INTEGER);");
                db.execSQL("CREATE TABLE IF NOT EXISTS CountriesInfection (Id INTEGER PRIMARY KEY AUTOINCREMENT, UserId INTEGER, CountryName VARCHAR(255), InfectedQty INTEGER, InsDate DATETIME);");
            } catch (Exception e) {
                Log.println(Log.ERROR, "ERROR", "Error OnCreate SQLite. ex: " + e.getMessage());
//                Message.message(context,""+e);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
/*                Message.message(context,"OnUpgrade");*/
                db.execSQL("DROP TABLE IF EXISTS User;");
                db.execSQL("DROP TABLE IF EXISTS LocalCountry;");
                db.execSQL("DROP TABLE IF EXISTS CountriesInfection;");
                onCreate(db);
            }catch (Exception e) {
//                Message.message(context,""+e);
            }
        }
    }
}