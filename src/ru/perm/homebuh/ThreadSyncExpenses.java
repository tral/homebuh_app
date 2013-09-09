package ru.perm.homebuh;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ThreadSyncExpenses extends Thread {
	
    Handler mHandler;
    final static int STATE_DONE = 0;
    final static int STATE_RUNNING = 1;
    int mState;

    String mSecretKey;
   
    String[] mDate;
    String[] mEt;
    String[] mComment;
    String[] mCat;
    String[] mVal;
    
   
    ThreadSyncExpenses(Handler h) {
        mHandler = h;
    }
   
    public void run() {

    	mState = STATE_RUNNING;   

        Boolean lWasError = false;
        int rowsInserted = 0;
        
        
        if (mState == STATE_RUNNING) {

        	String result = "";
        	String tmp = "";
        	
        	try {
        		for (int k=0; k<=mVal.length-1 ;k++) {
        			if (mVal[k]!= null) {
        				tmp = this.Go(mDate[k], mEt[k],  mComment[k], mCat[k], mVal[k]);
        				
       					if (!tmp.equalsIgnoreCase("ok")) {
        						result = tmp;
        						break;
        				}
        				
        				rowsInserted++;
        			} else break;
        		}
        		
				if (!tmp.equalsIgnoreCase("ok")) { 
					lWasError = true;
				} else {
					result = Integer.toString(rowsInserted);
				}
        		
        	}
	        catch (Exception e) {
	        	lWasError = true;
	        	result = e.toString() +" Message:" +e.getMessage();
	        }
        	
	        Message msg = mHandler.obtainMessage();
	        Bundle b = new Bundle();
	        b.putString("rsp", result);
	        b.putBoolean("wasError", lWasError);
	        msg.setData(b);
	        mHandler.sendMessage(msg);
	        
        }
       
    }
    
    
    public String Go(String p_date, String p_et, String p_comment, String p_cat, String p_val) {
    	
    	String url = "http://hb.perm.ru/android/saveexpense/key/"+mSecretKey;
    	String answer = "";
    	
        try {
        HttpClient httpClient = new DefaultHttpClient();
        
        HttpPost httppost = new HttpPost(url);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);

        nameValuePairs.add(new BasicNameValuePair("date", p_date));
        nameValuePairs.add(new BasicNameValuePair("et", p_et));
        nameValuePairs.add(new BasicNameValuePair("comment", p_comment));
        nameValuePairs.add(new BasicNameValuePair("cat", p_cat));
        nameValuePairs.add(new BasicNameValuePair("val", p_val));
        
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));
        
        HttpResponse response = httpClient.execute(httppost);
        //HttpResponse response = httpClient.execute(httpGet);
         
         if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
          StringBuilder sb = new StringBuilder();
          String line = null;
          while ((line = reader.readLine()) != null) {
           sb.append(line + System.getProperty("line.separator"));
          }
          answer = sb.toString();
          answer = answer.replaceAll("(\\r|\\n)", "");
            if (answer.equalsIgnoreCase("WRONG_SECRET_KEY")) {
            	answer="Неверный секретный ключ синхронизации!";
	          }
         }
        }
        catch (Exception e) {
         answer = e.toString() +" Message:" +e.getMessage();
        }
        
       return answer;
    }
    
    
    
    public void setState(int state) {
        mState = state;
    }
    
    public void setSecretKey(String key) {
        mSecretKey = key;
    }
    
    public void setVars(String[] s_date, String[] s_et, String[] s_comment, String[] s_cat, String[] s_val) {
        mDate = s_date;
        mEt = s_et;
        mComment = s_comment;
        mCat = s_cat;
        mVal =s_val; 
    }
    
}
