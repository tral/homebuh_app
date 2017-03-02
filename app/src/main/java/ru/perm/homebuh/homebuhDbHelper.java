package ru.perm.homebuh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.Calendar;
import java.util.Random;

class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "homebuhDB", null, 2);
    }

    // Вставляет расход (трату) денег
    public long insertExpense(long cat_id, int val, String date_, String comment) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH) + 1; // с нуля месяцы
        int day = c.get(Calendar.DAY_OF_MONTH);
        int min = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        int hour = c.get(Calendar.HOUR_OF_DAY);

        //cv.put("_id", 1);
        cv.put("date_", date_);
        cv.put("enter_time", String.format("%02d", day) + "." + String.format("%02d", month) + "." + year + " " + hour + ":" + min + ":" + sec);
        cv.put("comment", comment);
        cv.put("cat_id", cat_id);

        cv.put("val", val);
        long rowID = db.insert("data_", null, cv);

        // hash
        cv.clear();
        String lHash = Long.toString(rowID) + "_" + randomString();
        cv.put("hash", lHash);

        db.update("data_", cv, "_id = ?", new String[]{Long.toString(rowID)});

        return rowID;

    }

    public long insertCategory(int _id, int parent_id, String name, String pe) {
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
        return db.insert("category", null, cv);

    }

    public long deleteCategories() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("category", null, null);
    }

    private static String randomString() {

        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefjhijklmnopqrstuvwxyz1234567890!@#$%_)(*^;:+-[],.";
        final int N = alphabet.length();
        Random rd = new Random();
        int iLength = 32;
        StringBuilder sb = new StringBuilder(iLength);
        for (int i = 0; i < iLength; i++) {
            sb.append(alphabet.charAt(rd.nextInt(N)));
        }
        return sb.toString();
    }

    public void deleteExpenses() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("data_", null, null);
    }

    public String getSecretKey() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = db.query("keys", null, "_id=1", null, null, null, null);

        String key = "";
        if (c.moveToFirst()) {
            int idx = c.getColumnIndex("key_val");
            key = c.getString(idx);
        }
        c.close();
        return key;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

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


        // секретный ключ синхронизации
        db.execSQL("create table keys ("
                + "_id integer primary key,"
                + "key_val text"
                + ");");

        // Договорились, что ключ хранится в таблице с _id=1
        cv.put("_id", 1);
        cv.put("key_val", "fake_secret_key");
        db.insert("keys", null, cv);

        Upgrade_1_to_2(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 1) {
            Upgrade_1_to_2(db);
        }
    }

    void Upgrade_1_to_2(SQLiteDatabase db) {
        String upgradeQuery = "ALTER TABLE data_ ADD COLUMN hash TEXT";
        db.execSQL(upgradeQuery);
    }

}