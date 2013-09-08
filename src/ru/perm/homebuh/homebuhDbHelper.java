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

    public long insertCategory(int _id,  int parent_id, String name, String pe) {
    	SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("_id", _id);
		if (parent_id>0) {
			cv.put("parent_id", parent_id);
		}
		cv.put("name", name);
		cv.put("pe", pe);
		long rowID = db.insert("category", null, cv);
		return rowID;
    			
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
      cv.put("key_val", "fake_secret_key");
      db.insert("keys", null, cv);  
      
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
  }