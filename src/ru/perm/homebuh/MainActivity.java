package ru.perm.homebuh;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
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
implements CompoundButton.OnCheckedChangeListener//, CompoundButton.
,AdapterView.OnItemSelectedListener // For Categories (spinner)
{
    
	ThreadUpdateCategories mThreadUpdateCategories;
	
	CheckBox cb;
	
	//Date
    private Button mPickDate;
    private int mYear;
    private int mMonth;
    private int mDay;
    
    // Dialogs
    private static final int DATE_DIALOG_ID = 0;
    private final static int KEY_DIALOG_ID = 1;
	
    // Menu
    public static final int IDM_UPDATE_CATEGORIES = 101;
    public static final int IDM_KEY_SYNC = 102;
    
    
    // Database
    DBHelper dbHelper;
    SimpleCursorAdapter sca;
    
    // Categories 
    Spinner spn1;
    Spinner spn2;
    
    Cursor Cat1Cursor;
    
    // Установит лейбл на кнопке с датой
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
        spn1 = (Spinner)findViewById(R.id.spinnerCat1);
        spn1.setOnItemSelectedListener(this);
        spn2 = (Spinner)findViewById(R.id.spinnerCat2);
        spn2.setOnItemSelectedListener(this);
        
        // создаем объект для создания и управления версиями БД и подключаемся к БД
        dbHelper = new DBHelper(this);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        
	    String[] queryCols=new String[]{"_id", "name"};
	    String[] adapterCols=new String[]{"name"};
	    int[] adapterRowViews=new int[]{android.R.id.text1};
	    Cat1Cursor=db.query(true,"category", queryCols,null,null,null,null,null,null);
	    //this.startManagingCursor(mycursor);
	    sca=new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, Cat1Cursor, adapterCols, adapterRowViews,0);
	    sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spn1.setAdapter(sca);
        
		// закрываем подключение к БД
		dbHelper.close();
        
		
		
		
        // ---------------------------
		
		//TextView txt = (TextView)findViewById(R.id.textView1);
		//text.setText(R.string.button3);
		
		// For Toggle Button
		ToggleButton tb = (ToggleButton)findViewById(R.id.toggleButton2);
		tb.setOnCheckedChangeListener(this);
		 
		// For CheckBox
		cb = (CheckBox)findViewById(R.id.checkBox1);
		cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				//TextView tv = (TextView)findViewById(R.id.textView1);
				/*
				 
				if (isChecked) {
					tv.setText("checked");
				}
				else {
					tv.setText("NOT checked");
				}
				
				tv.setText("! " + spn1.getSelectedItemId());
				*/
			}
			
		}
		);
	
	}
	

	// Categories 1
    public void onItemSelected(
            AdapterView<?> parent, View v, int position, long id) {
    	
    	Toast toast = Toast.makeText(this, id + " !", Toast.LENGTH_SHORT);
        //toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // Categories 1        
    public void onNothingSelected(AdapterView<?> parent) {
            //mLabel.setText("");
    }
	
	// Диалоги
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
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
            
            builder.setMessage("Защита от злых хакеров");
            
            builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                	
                	// update secret key
                	dbHelper = new DBHelper(MainActivity.this);
	        		SQLiteDatabase db = dbHelper.getWritableDatabase();
	        		ContentValues cv = new ContentValues();
	                cv.put("key_val", keyDlgEdit.getText().toString());
	                int updCount = db.update("keys", cv, "_id = ?", new String[] { "1" });
	                dbHelper.close();
	                
	                keyDlgEdit.setText(""); // Чистим чтобы не палить ключ
	                
                    //MainActivity.this.finish();
                }
            });
            
            builder.setNegativeButton("Обломиться", new DialogInterface.OnClickListener() {
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
		
		 menu.add(Menu.NONE, IDM_UPDATE_CATEGORIES, Menu.NONE, "Обновить список категорий");
		 menu.add(Menu.NONE, IDM_KEY_SYNC, Menu.NONE, "Ввести ключ синхронизации");
		 return(super.onCreateOptionsMenu(menu));
	}

	
    // Define the Handler that receives messages from the thread and update the progress
	// Поток для обновления категорий из инета
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            String rsp = msg.getData().getString("rsp");
            Boolean lWasError = msg.getData().getBoolean("wasError");
            
            mThreadUpdateCategories.setState(ThreadUpdateCategories.STATE_DONE);

            if (lWasError) {
            	Toast.makeText(getApplicationContext(), "Ошибка обновления категорий: " + rsp, Toast.LENGTH_LONG).show();
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
			    					Toast toast = Toast.makeText(getApplicationContext(), "Database Exception" + e.getMessage(), Toast.LENGTH_SHORT);
			    					toast.show();
			    			}
    
					}
					// Как же его обновить?
					//Cat1Cursor.requery();
					//sca.notifyDataSetChanged();
					//sca.changeCursor(Cat1Cursor);
					
					dbHelper.close();
					
				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(), "JSON Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
				}
                
                Toast.makeText(getApplicationContext(), "Вставлено " + lInsertedRows + " записей", Toast.LENGTH_SHORT).show();
                
            } // was Inet Error ?
                
                
        }
    };  
	
	// Menu
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        CharSequence message;
	        switch (item.getItemId()) {
	            case IDM_UPDATE_CATEGORIES:
	            	
	            	// Даем обновлять категории только если нет несинхронизированных расходов
	            	dbHelper = new DBHelper(this);
	         		SQLiteDatabase db = dbHelper.getWritableDatabase();
	         		Cursor tmp = db.query(true,"data_", null,null,null,null,null,null,null);
	         	   
	         		if (tmp.getCount()>0 ) {
	         			Toast toast = Toast.makeText(this, "Сначала синхронизируйте расходы!", Toast.LENGTH_SHORT);
						toast.show();     
					} else {
						// Запускаем новый поток для вытягивания категорий с удаленного сервера
						mThreadUpdateCategories = new ThreadUpdateCategories(handler);
					    mThreadUpdateCategories.start();
					}
	         	    
	         		tmp.close();
	         		dbHelper.close();
	                
	            	//Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
	    	        //toast.setGravity(Gravity.CENTER, 0, 0);
	    	        //toast.show();
	                break;
	            case IDM_KEY_SYNC:
	            	showDialog(KEY_DIALOG_ID);
	                break;    
	                
	            default:
	                return false;
	        }
	        
	        return true;
	    }
	
	// For Toggle Button
	@Override
	public void onCheckedChanged(CompoundButton buttonview, boolean isChecked) {
		
		Toast.makeText(getApplicationContext(), "Color: "
                , Toast.LENGTH_SHORT).show();
		
	//	EditText editText = (EditText) findViewById(R.id.editText1);
		//String message = editText.getText().toString();
		//TextView tv = (TextView)findViewById(R.id.textView1);
        /*
        if (isChecked)
        	tv.setText("1");
        else
        	tv.setText("0");
        */

        TextView tv3 = (TextView)findViewById(R.id.textView3);
        tv3.setText("");
        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);
    
		// подключаемся к БД
		SQLiteDatabase db = dbHelper.getWritableDatabase();
    
	     Cursor c = db.query("keys", null, null, null, null, null, null);

	      // ставим позицию курсора на первую строку выборки
	      // если в выборке нет строк, вернется false
	      if (c.moveToFirst()) {

	        // определяем номера столбцов по имени в выборке
	        int idColIndex = c.getColumnIndex("_id");
	        int keyColIndex = c.getColumnIndex("key_val");
	        

	        do {
	          // получаем значения по номерам столбцов и пишем все в лог
	        	tv3.append(" id: " + c.getInt(idColIndex) + " val: " + c.getString(keyColIndex));
	          /*Log.d(LOG_TAG,
	              "ID = " + c.getInt(idColIndex) + 
	              ", name = " + c.getString(nameColIndex) + 
	              ", email = " + c.getString(emailColIndex));
	              */
	          // переход на следующую строку 
	          // а если следующей нет (текущая - последняя), то false - выходим из цикла
	        } while (c.moveToNext());
	      } 
	      
	      c.close();
	
        
		// закрываем подключение к БД
		dbHelper.close();
        
	}
	
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        //Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText1);
        String message = editText.getText().toString();
    }


	
}
