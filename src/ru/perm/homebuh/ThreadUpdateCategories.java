package ru.perm.homebuh;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ThreadUpdateCategories extends Thread {
	
    Handler mHandler;
    final static int STATE_DONE = 0;
    final static int STATE_RUNNING = 1;
    int mState;

    String mSecretKey;
   
    ThreadUpdateCategories(Handler h) {
        mHandler = h;
    }
   
    public void run() {
        mState = STATE_RUNNING;   
        
        Boolean lWasError;
        
        if (mState == STATE_RUNNING) {
	        
        	lWasError = false;
        	
	        String result = ";(";
	        String url = "http://hb.perm.ru/android/getcategories/key/"+mSecretKey;
	        HttpClient httpClient = new DefaultHttpClient();
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
	          String answer = sb.toString();
	          result = answer;
	          result = result.replaceAll("(\\r|\\n)", "");
	          if (result.equalsIgnoreCase("WRONG_SECRET_KEY")) {
	        	  result="Неверный секретный ключ синхронизации!";
	        	  lWasError = true;
	          }
	         }
	        }
	        catch (Exception e) {
	         result = "EX6! " +  e.toString() +" Message:" +e.getMessage();
	         lWasError = true;
	        }
	        
	        Message msg = mHandler.obtainMessage();
	        Bundle b = new Bundle();
	        //b.putInt("total", mTotal);
	        b.putString("rsp", result);
	        b.putBoolean("wasError", lWasError);
	        msg.setData(b);
	        mHandler.sendMessage(msg);
	        
        }
        /*
        while (mState == STATE_RUNNING) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Log.e("ERROR", "Thread Interrupted");
            }
            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putInt("total", mTotal);
            b.putString("rsp", result);
            msg.setData(b);
            mHandler.sendMessage(msg);
            mTotal++;
        }*/
    }
    
    public void setState(int state) {
        mState = state;
    }
    
    public void setSecretKey(String key) {
        mSecretKey = key;
    }
    
}
