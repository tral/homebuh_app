package ru.perm.homebuh;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ThreadSyncExpenses extends Thread {

    Handler mHandler;
    Context mContext;
    final static int STATE_DONE = 0;
    final static int STATE_RUNNING = 1;
    int mState;

    String mSecretKey;

    DBHelper dbHelper;

    ThreadSyncExpenses(Handler h, Context context) {
        mHandler = h;
        mContext = context;
    }

    public void run() {

        mState = STATE_RUNNING;

        Boolean lWasError = false;

        if (mState == STATE_RUNNING) {

            String result = "";

            try {

                dbHelper = new DBHelper(mContext);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                Cursor mCur = db.query("data_", null, null, null, null, null, "_id");

                if (mCur.moveToFirst()) {

                    int idColIndex = mCur.getColumnIndex("_id");
                    int dateColIndex = mCur.getColumnIndex("date_");
                    int etColIndex = mCur.getColumnIndex("enter_time");
                    int commentColIndex = mCur.getColumnIndex("comment");
                    int catColIndex = mCur.getColumnIndex("cat_id");
                    int valColIndex = mCur.getColumnIndex("val");
                    int hashColIndex = mCur.getColumnIndex("hash");

                    String url = "http://hb.perm.ru/android/saveexpense/key/" + mSecretKey;

                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                    do {
                        nameValuePairs.add(new BasicNameValuePair("date[]", mCur.getString(dateColIndex)));
                        nameValuePairs.add(new BasicNameValuePair("et[]", mCur.getString(etColIndex)));
                        nameValuePairs.add(new BasicNameValuePair("comment[]", mCur.getString(commentColIndex)));
                        nameValuePairs.add(new BasicNameValuePair("cat[]", Integer.toString(mCur.getInt(catColIndex))));
                        nameValuePairs.add(new BasicNameValuePair("val[]", Integer.toString(mCur.getInt(valColIndex))));
                        nameValuePairs.add(new BasicNameValuePair("hash[]", mCur.getString(hashColIndex)));
                    } while (mCur.moveToNext());

                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
                    HttpResponse response = httpClient.execute(httppost);

                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        StringBuilder sb = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + System.getProperty("line.separator"));
                        }
                        String answer = sb.toString();
                        answer = answer.replaceAll("(\\r|\\n)", "");

                        if (answer.equalsIgnoreCase("ok")) {
                            dbHelper.deleteExpenses();
                            result = "Синхронизация успешна";
                        } else if (answer.equalsIgnoreCase("WRONG_SECRET_KEY")) {
                            result = mContext.getResources().getString(R.string.err_wrong_key);
                        }
                    }
                }

                mCur.close();
                dbHelper.close();

            } catch (Exception e) {
                lWasError = true;
                result = "EX1! " + e.toString() + " Message:" + e.getMessage();
            }

            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("rsp", result);
            b.putBoolean("wasError", lWasError);
            msg.setData(b);
            mHandler.sendMessage(msg);

        }

    }


    public void setState(int state) {
        mState = state;
    }

    public void setSecretKey(String key) {
        mSecretKey = key;
    }

}
