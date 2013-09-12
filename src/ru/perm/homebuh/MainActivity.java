package ru.perm.homebuh;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class MainActivity extends Activity
// For Toggle Button
implements AdapterView.OnItemSelectedListener, // For Categories (spinner)
CompoundButton.OnCheckedChangeListener // For ToggleButtons
{
    
	ThreadUpdateCategories mThreadUpdateCategories;
	ThreadSyncExpenses mThreadSyncExpenses;
	
	//Date
    private Button mPickDate;
    private int mYear;
    private int mMonth;
    private int mDay;
    
    // Dialogs
    private static final int DATE_DIALOG_ID = 0;
    private final static int KEY_DIALOG_ID = 1;
    private final static int CAT_PROGRESS_DIALOG_ID = 2;
    private final static int SYNC_PROGRESS_DIALOG_ID = 3;
    
    // Menu
    public static final int IDM_UPDATE_CATEGORIES = 101;
    public static final int IDM_KEY_SYNC = 102;
    
    // Sum
    EditText mSumVal;
    
    // Database
    DBHelper dbHelper;
    SimpleCursorAdapter sca1;
    SimpleCursorAdapter sca2;
    
    // Categories 
    Spinner spn1;
    Spinner spn2;
    Cursor Cat1Cursor;
    Cursor Cat2Cursor;
    ProgressDialog mCatProgressDialog;
    ProgressDialog mSyncProgressDialog;
    ToggleButton tb1;
    ToggleButton tb2;
    ToggleButton tb3;
    ToggleButton tb4;
    
    // Buttons
    Button mSaveBtn;
    Button mSyncBtn;
    
    // Comment
    EditText mComment;
    TextView mLog;
    
    // ��������� ����� �� ������ � �����
    private void setLabelOnDateButton() {
    	mPickDate.setText(
                new StringBuilder()
                		.append(String.format("%02d", mDay)).append(".")
                        // Month is 0 based so add 1
                        .append(String.format("%02d", mMonth+1)).append(".")
                        .append(mYear).append(" "));
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// --- Date ---
		
		// get the current date
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        
        mPickDate = (Button) findViewById(R.id.dateBtn);
        this.setLabelOnDateButton();
        
        // add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
		
        // --- Categories ---
        tb1 = (ToggleButton)findViewById(R.id.toggleButton1);
        tb2 = (ToggleButton)findViewById(R.id.toggleButton2);
        tb3 = (ToggleButton)findViewById(R.id.toggleButton3);
        tb4 = (ToggleButton)findViewById(R.id.toggleButton4);
        tb1.setOnCheckedChangeListener(this);
        tb2.setOnCheckedChangeListener(this);
        tb3.setOnCheckedChangeListener(this);
        tb4.setOnCheckedChangeListener(this);
        spn1 = (Spinner)findViewById(R.id.spinnerCat1);
        spn1.setOnItemSelectedListener(this);
        spn2 = (Spinner)findViewById(R.id.spinnerCat2);
        spn2.setOnItemSelectedListener(this);
        this.loadCategoriesLevel1();
   
        // Sum
        mSumVal = (EditText)findViewById(R.id.editText1);
                
        //Comment
        mComment = (EditText)findViewById(R.id.editText2);
        
        mLog = (TextView)findViewById(R.id.textViewLog);
        this.updateLogLabel();
        
        // Buttons
        mSaveBtn = (Button) findViewById(R.id.saveBtn);
        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	int lSumVal;
            	if (mSumVal.getText().toString().equalsIgnoreCase("")) {
            		lSumVal = 0;
            	} else {
            		lSumVal = Integer.parseInt(mSumVal.getText().toString());	
            	}
            	
            	if (lSumVal < 1) 
            		Toast.makeText(MainActivity.this, "������� �����!", Toast.LENGTH_LONG).show();
            	 else 
            		if (spn2.getCount() < 1 && !tb1.isChecked() && !tb2.isChecked() && !tb3.isChecked() && !tb4.isChecked()) 
            			Toast.makeText(MainActivity.this, "��������� ���������!", Toast.LENGTH_LONG).show();
            			else {
            	
            				long cat_id = spn2.getSelectedItemId();
            				
            				if (tb1.isChecked()) cat_id = 53;
            				if (tb2.isChecked()) cat_id = 52;
            				if (tb3.isChecked()) cat_id = 88;
            				if (tb4.isChecked()) cat_id = 123;
            				
            				dbHelper = new DBHelper(MainActivity.this);
                        	
                        	long lastInsertId = dbHelper.insertExpense(cat_id, lSumVal, (String)mPickDate.getText(), mComment.getText().toString());

            	        	if (lastInsertId > 0) {
            	        		Toast.makeText(MainActivity.this, "C��������", Toast.LENGTH_SHORT).show();
            				}
            	        	
            	        	dbHelper.close();
            	        	MainActivity.this.updateSyncLabel();
            	        	mSumVal.setText("");
            	        	mComment.setText("");
            	        	MainActivity.this.nullToggles(-1);
            	        	mSumVal.requestFocus();
            	        	MainActivity.this.updateLogLabel();
            			}

            	//Toast.makeText(MainActivity.this, "!"+spn2.getCount(), Toast.LENGTH_SHORT).show();

            }
        });
        
        mSyncBtn = (Button) findViewById(R.id.syncBtn);
        this.updateSyncLabel();
        mSyncBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	dbHelper = new DBHelper(MainActivity.this);
            	SQLiteDatabase db = dbHelper.getWritableDatabase();
            	Cursor mCur = db.query("data_", null, null, null, null, null, "_id");
            	 
            	if (mCur.getCount() < 1) {
            		Toast.makeText(MainActivity.this, "��� �������������������� ��������!", Toast.LENGTH_LONG).show();            		
            	} else {
            		
            		showDialog(SYNC_PROGRESS_DIALOG_ID);
            		
	            	String[] s=new String[100];
	            	 
	            	String[] s_date = new String[100];
	            	String[] s_et = new String[100];
	            	String[] s_comment = new String[100];
	            	String[] s_cat = new String[100];
	            	String[] s_val = new String[100];
	            	 
	            	int i=0;
	            	 
	            	if (mCur.moveToFirst()) {
	
	            		int dateColIndex = mCur.getColumnIndex("date_");
	 	    	        int etColIndex = mCur.getColumnIndex("enter_time");
	 	    	        int commentColIndex = mCur.getColumnIndex("comment");
	 	    	        int catColIndex = mCur.getColumnIndex("cat_id");
	 	    	        int valColIndex = mCur.getColumnIndex("val");
	 	    	        
	 	    	        do {
	 	    	        	s_date[i] = mCur.getString(dateColIndex);
	 	    	        	s_et[i] = mCur.getString(etColIndex);
	 	    	        	s_comment[i] =mCur.getString(commentColIndex);
	 	    	        	s_cat[i]=Integer.toString(mCur.getInt(catColIndex));
	 	    	        	s_val[i]=Integer.toString(mCur.getInt(valColIndex));
	 	    	        	i++;
	 	    	        } while (mCur.moveToNext());
	            	 } 
	            	
					mThreadSyncExpenses = new ThreadSyncExpenses(handlerSync);
					mThreadSyncExpenses.setSecretKey(dbHelper.getSecretKey());
					mThreadSyncExpenses.setVars(s_date, s_et, s_comment, s_cat, s_val);
					mThreadSyncExpenses.setState(ThreadSyncExpenses.STATE_RUNNING);
					mThreadSyncExpenses.start();
            	}
				
				mCur.close();
	        	dbHelper.close();
            	
            }
        });
        
        // ����� , ����� �������� ����� ����������
        mSumVal.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        
	}
	
	protected void updateSyncLabel() {
		dbHelper = new DBHelper(this);
     	SQLiteDatabase db = dbHelper.getWritableDatabase();
     	Cursor mCur = db.query("data_", null, null, null, null, null, "_id");
		mSyncBtn.setText("������������� (" + Integer.toString(mCur.getCount()) + ")");
     	mCur.close();
		dbHelper.close();
	}
	
	protected void updateLogLabel() {
		
		mLog.setText("");
		
		dbHelper = new DBHelper(this);
     	SQLiteDatabase db = dbHelper.getWritableDatabase();
     	
     	String sqlQuery = "select d.val as val, c.name "
     	        + "from data_ as d "
     	        + "inner join category as c "
     	        + "on c._id = d.cat_id "
     	        + " ORDER BY d._id DESC";
     	
     	Cursor mCur = db.rawQuery(sqlQuery, null);
     	
     	int i=0;
     	if (mCur.moveToFirst()) {
     		
 	        int nameColIndex = mCur.getColumnIndex("name");
 	        int valColIndex = mCur.getColumnIndex("val");
 	        
 	        do {
 	        	if (i>0)
 	        		mLog.append(", ");
 	        	mLog.append(mCur.getString(nameColIndex) +" " + Integer.toString(mCur.getInt(valColIndex)));
 	        	i++;
 	        } while (mCur.moveToNext());
    	} 
     	
     	mCur.close();
		dbHelper.close();
	}
	
	protected void loadCategoriesLevel1() {
        dbHelper = new DBHelper(this);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
	    String[] queryCols=new String[]{"_id", "name"};
	    String[] adapterCols=new String[]{"name"};
	    int[] adapterRowViews=new int[]{android.R.id.text1};
	    Cat1Cursor=db.query(true,"category", queryCols, "parent_id is null" ,null,null,null,"name", null);
	    //this.startManagingCursor(Cat1Cursor); ������ ��� ������ �����
	    sca1=new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, Cat1Cursor, adapterCols, adapterRowViews,0);
	    sca1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spn1.setAdapter(sca1);
        
		dbHelper.close();
	}

	protected void loadCategoriesLevel2(long parent_id) {
        dbHelper = new DBHelper(this);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
	    String[] queryCols=new String[]{"_id", "name"};
	    String[] adapterCols=new String[]{"name"};
	    int[] adapterRowViews=new int[]{android.R.id.text1};
	    Cat2Cursor=db.query(true, "category", queryCols, "parent_id = " + parent_id ,null,null,null,"name", null);
	    //this.startManagingCursor(Cat1Cursor); ������ ��� ������ �����
	    sca2=new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, Cat2Cursor, adapterCols, adapterRowViews,0);
	    sca2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spn2.setAdapter(sca2);
        
		dbHelper.close();
	}
	
	// Categories 1 ����� ��������
    public void onItemSelected(
            AdapterView<?> parent, View v, int position, long id) {
    	
    	switch (parent.getId()) {
    	case R.id.spinnerCat1 :
    		this.loadCategoriesLevel2(id);
    		//Toast.makeText(this, "id = " + id, Toast.LENGTH_SHORT).show();
    		break;
    	case R.id.spinnerCat2 :
    		//Toast.makeText(this, "id = " + id, Toast.LENGTH_SHORT).show();
    		break;
    	}
    }

    // Categories 1 - ������ �� �������        
    public void onNothingSelected(AdapterView<?> parent) {
            //mLabel.setText("");
    }
	
	// �������
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case CAT_PROGRESS_DIALOG_ID:
        	  mCatProgressDialog = new ProgressDialog(MainActivity.this);
        	  //mCatProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        	  mCatProgressDialog.setCanceledOnTouchOutside(false);
        	  mCatProgressDialog.setCancelable(false);
        	  mCatProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        	  mCatProgressDialog.setMessage("�������� ���������...");
        	  return mCatProgressDialog;
        	  
        case SYNC_PROGRESS_DIALOG_ID:
        	mSyncProgressDialog = new ProgressDialog(MainActivity.this);
      	  //mCatProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        	mSyncProgressDialog.setCanceledOnTouchOutside(false);
        	mSyncProgressDialog.setCancelable(false);
        	mSyncProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        	mSyncProgressDialog.setMessage("������������� ��������...");
      	  return mSyncProgressDialog;
              
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
            
        case KEY_DIALOG_ID:
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.key_dialog, (ViewGroup)findViewById(R.id.key_dialog_layout));
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(layout);
            
            final EditText keyDlgEdit = (EditText) layout.findViewById(R.id.key_edit_text);
            
            builder.setMessage("������ �� ���� �������");
            
            builder.setPositiveButton("���������", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	
                	// update secret key
                	dbHelper = new DBHelper(MainActivity.this);
	        		SQLiteDatabase db = dbHelper.getWritableDatabase();
	        		ContentValues cv = new ContentValues();
	                cv.put("key_val", keyDlgEdit.getText().toString());
	                int updCount = db.update("keys", cv, "_id = ?", new String[] { "1" });
	                dbHelper.close();
	                
	                keyDlgEdit.setText(""); // ������ ����� �� ������ ����
	                
                    //MainActivity.this.finish();
                }
            });
            
            builder.setNegativeButton("����������", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    }
            });
            
            builder.setCancelable(false);
            return builder.create();

        }
        return null;
    }
	
    // Date
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    setLabelOnDateButton();
                }
            };
    
    // Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		//return true;
		
		 menu.add(Menu.NONE, IDM_UPDATE_CATEGORIES, Menu.NONE, "�������� ������ ���������");
		 menu.add(Menu.NONE, IDM_KEY_SYNC, Menu.NONE, "������ ���� �������������");
		 return(super.onCreateOptionsMenu(menu));
	}

	
    // Define the Handler that receives messages from the thread and update the progress
	// ����� ��� ���������� ��������� �� �����
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            String rsp = msg.getData().getString("rsp");
            Boolean lWasError = msg.getData().getBoolean("wasError");
            
            mThreadUpdateCategories.setState(ThreadUpdateCategories.STATE_DONE);
            
            if (lWasError) {
            	Toast.makeText(getApplicationContext(), "������ ���������� ���������: " + rsp, Toast.LENGTH_LONG).show();
            } else {
				
            	long lInsertedRows = 0;
                long lastInsertId;
                
                try {
                	// http://stackoverflow.com/questions/9605913/how-to-parse-json-in-android
					JSONObject jObject = new JSONObject(rsp);
					JSONArray jArray = jObject.getJSONArray("cats");
					
					dbHelper = new DBHelper(MainActivity.this);
					dbHelper.deleteCategories();
					for (int i=0; i < jArray.length(); i++)
					{
					        JSONObject oneObject = jArray.getJSONObject(i);
					        // Pulling items from the array
					        String json_id = oneObject.getString("id");
					        String json_name = oneObject.getString("name");
					        String json_parent_id = oneObject.getString("parent_id");
					        String json_pe = oneObject.getString("pe");
					        
					        try {
			    				
					        	lastInsertId = dbHelper.insertCategory(Integer.parseInt(json_id), Integer.parseInt(json_parent_id), json_name, json_pe);
					        	if (lastInsertId > 0) {
					        		lInsertedRows ++;
			    				}
			    				
			    			}
			    				catch (Exception e) {
			    					Toast.makeText(getApplicationContext(), "Database Exception" + e.getMessage(), Toast.LENGTH_LONG).show();
			    			}
    
					}

					MainActivity.this.loadCategoriesLevel1();
					
					dbHelper.close();
					
				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(), "JSON Exception: "+e.getMessage(), Toast.LENGTH_LONG).show();
				}
                
                
                Toast.makeText(getApplicationContext(), "��������� ���������, ��������� " + lInsertedRows + " �������", Toast.LENGTH_SHORT).show();
                
            } // was Inet Error ?
                
            dismissDialog(CAT_PROGRESS_DIALOG_ID);
        }
    };  
	

    // ����� ��� ������������� ��������
    final Handler handlerSync = new Handler() {
    	public void handleMessage(Message msg) {

    		try {
	    		String rsp = msg.getData().getString("rsp");
	            Boolean lWasError = msg.getData().getBoolean("wasError");
	              
	            mThreadSyncExpenses.setState(ThreadUpdateCategories.STATE_DONE);
	              
	            if (lWasError) {
	            	Toast.makeText(getApplicationContext(), "������ ������������� ��������: " + rsp, Toast.LENGTH_LONG).show();
	            } else {
	            	dbHelper = new DBHelper(MainActivity.this);
	            	dbHelper.deleteExpenses(); // ������� ����� ��������
	            	dbHelper.close();
	            	Toast.makeText(getApplicationContext(), "�������������� �������: " + rsp, Toast.LENGTH_SHORT).show();
	            }
	            MainActivity.this.updateSyncLabel();
	            MainActivity.this.updateLogLabel();
	            dismissDialog(SYNC_PROGRESS_DIALOG_ID);
	    	}
	        catch (Exception e) {
	        	Toast.makeText(getApplicationContext(), "handleMessage(Message msg): " + e.toString() +" Message:" +e.getMessage(), Toast.LENGTH_SHORT).show();
	        }
            
            
    	}
    };  
    
    
	// Menu
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        CharSequence message;
	        switch (item.getItemId()) {
	            case IDM_UPDATE_CATEGORIES:
	            	
	            	// ���� ��������� ��������� ������ ���� ��� �������������������� ��������
	              	dbHelper = new DBHelper(this);
	           		SQLiteDatabase db = dbHelper.getWritableDatabase();
	           		Cursor tmp = db.query(true,"data_", null,null,null,null,null,null,null);
	           	   
	           		if (tmp.getCount()>0 ) {
	           			Toast.makeText(this, "������� ��������������� �������!", Toast.LENGTH_LONG).show();
    				} else {
    					showDialog(CAT_PROGRESS_DIALOG_ID);
    					// ��������� ����� ����� ��� ����������� ��������� � ���������� �������
    					mThreadUpdateCategories = new ThreadUpdateCategories(handler);
    					mThreadUpdateCategories.setSecretKey(dbHelper.getSecretKey());
    					mThreadUpdateCategories.setState(ThreadUpdateCategories.STATE_RUNNING);
    				    mThreadUpdateCategories.start();
    				}
	           	    
	           		tmp.close();
	           		dbHelper.close();
	            	
	            	//Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

	                break;
	            case IDM_KEY_SYNC:
	            	showDialog(KEY_DIALOG_ID);
	                break;    
	                
	            default:
	                return false;
	        }
	        
	        return true;
	    }
	

	 public void nullToggles(int except_id) {
		if (except_id != tb1.getId())
			tb1.setChecked(false);
		if (except_id != tb2.getId())
			tb2.setChecked(false);
		if (except_id != tb3.getId())
			tb3.setChecked(false);
		if (except_id != tb4.getId())
			tb4.setChecked(false);
	 }
	 
	  @Override
	    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
	        if (isChecked) {
	        	this.nullToggles(button.getId());
	            //text.setText("Button checked");
	        }
	        else {
	           // text.setText("Button unchecked");
	        }
	    }
	 
	
}
