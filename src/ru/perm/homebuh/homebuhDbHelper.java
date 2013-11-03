package ru.perm.homebuh;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

  class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
      // конструктор суперкласса
      super(context, "homebuhDB", null, 1);
    }

    // Вставляет расход (трату) денег
    public long insertExpense(long cat_id, int val, String date_, String comment) {
    	
    	SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
    	
		Calendar c=Calendar.getInstance(); 
		int year=c.get(Calendar.YEAR); 
		int month=c.get(Calendar.MONTH)+1; // с нуля месяцы  
		int day=c.get(Calendar.DAY_OF_MONTH);
		int min=c.get(Calendar.MINUTE);
		int sec=c.get(Calendar.SECOND);
		int hour=c.get(Calendar.HOUR_OF_DAY);
		
        //cv.put("_id", 1);
        cv.put("date_", date_);
        cv.put("enter_time", String.format("%02d", day)+"."+String.format("%02d", month)+"."+year+" "+hour+":"+min+":"+sec);
        cv.put("comment", comment);
        cv.put("cat_id", cat_id);
        cv.put("val", val);
        long rowID = db.insert("data_", null, cv);
		
		return rowID;
    			
    }
    
    public long insertCategory(int _id,  int parent_id, String name, String pe) {
    	SQLiteDatabase db = this.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("_id", _id);
		if (parent_id >= 0) {
			cv.put("parent_id", parent_id);
		}
		cv.put("name", name);
		if (!pe.equalsIgnoreCase("-1")) {
			cv.put("pe", pe);
		}
		long rowID = db.insert("category", null, cv);
		return rowID;
    			
    }
    
    // Удаляет все категории
    public long deleteCategories() {
    	SQLiteDatabase db = this.getWritableDatabase();
		long delCount  = db.delete("category", null, null);
		return delCount;
    }
    
    // Удаляет все расходы
    public long deleteExpenses() {
    	SQLiteDatabase db = this.getWritableDatabase();
		long delCount  = db.delete("data_", null, null);
		return delCount;
    }
    
    public String getSecretKey() {
    	SQLiteDatabase db = this.getWritableDatabase();
    	Cursor c = db.query("keys", null, "_id=1", null, null, null, null);
    	
    	if (c.moveToFirst()) {
            int idx = c.getColumnIndex("key_val");
            String key = c.getString(idx);
            return  key;
		}
    	
    	return "";
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
      //Log.d(LOG_TAG, "--- onCreate database ---");
      
      db.execSQL("create table category ("
          + "_id integer primary key," 
          + "name text,"
          + "parent_id integer, "
          + "pe text"
          + ");");

      db.execSQL("create table data_ ("
              + "_id integer primary key autoincrement," 
              + "date_ text,"
              + "enter_time text,"
              + "cat_id integer, "
              + "val integer, "
              + "comment text"
              + ");");
      
      ContentValues cv = new ContentValues();
      
      // test del !!!
      /*
      cv.put("_id", 1);
      cv.put("date_", "---");
      cv.put("enter_time", "---");
      cv.put("comment", "---");
      cv.put("cat_id", 1);
      cv.put("val", 1);
      db.insert("data_", null, cv);
  */    
      // секретный ключ синхронизации
      db.execSQL("create table keys ("
              + "_id integer primary key," 
              + "key_val text"
              + ");");
      
      // Договорились, что ключ хранится в таблице с _id=1
      cv.put("_id", 1);
      cv.put("key_val", "fake_secret_key");
      db.insert("keys", null, cv);  
      
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
  }