package ru.perm.homebuh;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
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
    
	CheckBox cb;
	AutoCompleteTextView mAutoComplete;
	final String[] mContacts = {
            "Jacob Anderson", "Emily Duncan", "Michael Fuller", 
            "Emma Greenman", "Joshua Harrison", "Madison Johnson",
            "Matthew Cotman", "Olivia Lawson", "Andrew Chapman", 
            "Michael Honeyman", "Isabella Jackson", "William Patterson", 
            "Joseph Godwin", "Samantha Bush", "Christopher Gateman"};
	
	static String[] accountData = { "Наличные", "Связной", "ПКБ" };
	
	final String[] mCategories = {
	            "Авто", "Досуг и отдых", "Еда", 
	            "Здоровье и красота", "Иммиграция", "Квартира",
	            "Корректировки", "Кредиты", "Одежда", "Подарки и праздники", 
	            "Прочее", "Путешествие", "Счета", "Транспорт", "Электроника"};
	
	//test commit 9
	//Date
    private Button mPickDate;
    private int mYear;
    private int mMonth;
    private int mDay;
    
    // Dialogs
    static final int DATE_DIALOG_ID = 0;
	
    // Menu
    public static final int IDM_UPDATE_CATEGORIES = 101;
    
    // Database
    DBHelper dbHelper;
    SimpleCursorAdapter scAdapter;
    
    // Categories 
    Spinner spn1;
    Spinner spn2;
    
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
        
        // --- Database ---
        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);
        
    	// создаем объект для данных
		ContentValues cv = new ContentValues();

		// подключаемся к БД
		SQLiteDatabase db = dbHelper.getWritableDatabase();
        
		/*
		cv.put("_id", 7);
		cv.put("parent_id", 5);
		cv.put("name", "3_я_запись_ид7");
		cv.put("pe", "profit");
		long rowID = db.insert("category", null, cv);
		*/
		
	    String[] queryCols=new String[]{"_id", "name"};
	    String[] adapterCols=new String[]{"name"};
	    int[] adapterRowViews=new int[]{android.R.id.text1};
	    Cursor mycursor=db.query(true,"category", queryCols,null,null,null,null,null,null);
	    //this.startManagingCursor(mycursor);
	    SimpleCursorAdapter sca=new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, mycursor, adapterCols, adapterRowViews,0);
	    sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spn1.setAdapter(sca);
        
		// закрываем подключение к БД
		dbHelper.close();
        
		
		
		
        // ---------------------------
		
		TextView text = (TextView)findViewById(R.id.textView1);
		text.setText(R.string.button3);
		
		// For Toggle Button
		ToggleButton tb = (ToggleButton)findViewById(R.id.toggleButton2);
		tb.setOnCheckedChangeListener(this);
		 
		// For CheckBox
		cb = (CheckBox)findViewById(R.id.checkBox1);
		cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				TextView tv = (TextView)findViewById(R.id.textView1);
				
				 
				if (isChecked) {
					tv.setText("checked");
				}
				else {
					tv.setText("NOT checked");
				}
				
				tv.setText("! " + spn1.getSelectedItemId());
				
			}
			
		}
		);
	
	}
	

	// Categories 1
    public void onItemSelected(
            AdapterView<?> parent, View v, int position, long id) {
            //mLabel.setText(mContacts[position]);
    	
    	Toast toast = Toast.makeText(this, id + " !", Toast.LENGTH_SHORT);
        //toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // Categories 1        
    public void onNothingSelected(AdapterView<?> parent) {
            //mLabel.setText("");
    }
	
	// Date
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
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
		 return(super.onCreateOptionsMenu(menu));
	}

	// Menu
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        CharSequence message;
	        switch (item.getItemId()) {
	            case IDM_UPDATE_CATEGORIES:
	            	message = "!!!";       
	                break;
	            default:
	                return false;
	        }
	        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
	        //toast.setGravity(Gravity.CENTER, 0, 0);
	        toast.show();
	        return true;
	    }
	
	// For Toggle Button
	@Override
	public void onCheckedChanged(CompoundButton buttonview, boolean isChecked) {
		
		EditText editText = (EditText) findViewById(R.id.editText1);
		String message = editText.getText().toString();
		TextView tv = (TextView)findViewById(R.id.textView1);
        
        if (isChecked)
        	tv.setText("1");
        else
        	tv.setText("0");
        
        Toast.makeText(getApplicationContext(), "Color: " + mPickDate.getText()
                , Toast.LENGTH_SHORT).show();
        
	}
	
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        //Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText1);
        String message = editText.getText().toString();
        
        TextView tv = (TextView)findViewById(R.id.textView1);
        
        //intent.putExtra(EXTRA_MESSAGE, message);
        //startActivity(intent);
        
        
        /*
        String result = ";(";
        String url = "http://beta.finefin.ru/api/auth/";
        
        try {
            result = HttpConnect.sendGet(url);
           } catch (Exception e) {
            result = e.toString(); 
           }
        
        */
        /*
        
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
         }
        }
        catch (Exception e) {
         result = e.toString() +" Message:" +e.getMessage(); 
        }
        */
        //tv.setText(result);
        
        
    }


	
}
