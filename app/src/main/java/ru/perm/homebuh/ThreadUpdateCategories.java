package ru.perm.homebuh;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ThreadUpdateCategories extends Thread {

    Handler mHandler;
    Context mContext;
    final static int STATE_DONE = 0;
    final static int STATE_RUNNING = 1;
    int mState;

    String mSecretKey;

    ThreadUpdateCategories(Handler h, Context context) {
        mHandler = h;
        mContext = context;
    }

    public void run() {
        mState = STATE_RUNNING;

        Boolean lWasError;

        if (mState == STATE_RUNNING) {

            lWasError = false;

            String result = ";(";
            String url = "https://hb.perm.ru/android/getcategories/key/" + mSecretKey;
            //HttpClient httpClient = new DefaultHttpClient();
            HttpClient httpClient = DBHelper.createHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {
                HttpResponse response = httpClient.execute(httpGet);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + System.getProperty("line.separator"));
                    }
                    result = sb.toString();
                    result = result.replaceAll("(\\r|\\n)", "");
                    if (result.equalsIgnoreCase("WRONG_SECRET_KEY")) {
                        result = mContext.getResources().getString(R.string.err_wrong_key);
                        lWasError = true;
                    }
                }
            } catch (Exception e) {
                result = "EX6! " + e.toString() + " Message:" + e.getMessage();
                lWasError = true;
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
