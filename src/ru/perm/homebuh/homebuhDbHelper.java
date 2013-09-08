package ru.perm.homebuh;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

  class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
      // конструктор суперкласса
      super(context, "homebuhDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      //Log.d(LOG_TAG, "--- onCreate database ---");
      // создаем таблицу с полями
      db.execSQL("create table category ("
          + "_id integer primary key," 
          + "name text,"
          + "parent_id integer, "
          + "pe text"
          + ");");

      // секретный ключ синхронизации
      db.execSQL("create table keys ("
              + "_id integer primary key," 
              + "key_val text"
              + ");");
      
      ContentValues cv = new ContentValues();
      
      // Договорились, что ключ хранится в таблице с _id=1
      cv.put("_id", 1);
      cv.put("key_val", "Gyfhr76yr89");
      db.insert("keys", null, cv);  
      
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
  }