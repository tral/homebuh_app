package ru.perm.homebuh;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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

import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class MainActivity extends ActionBarActivity
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
    ToggleButton tb5;
    ToggleButton tb6;
    Button tbMore;

    // Buttons
    int mNotifCount = 0;

    // Comment
    EditText mComment;
    TextView mLog;

    //private Drawer.Result drawerResult = null;

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    private void setTodayDate() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        this.setLabelOnDateButton();
    }

    // Установит лейбл на кнопке с датой
    private void setLabelOnDateButton() {
        mPickDate.setText(
                new StringBuilder()
                        .append(String.format("%02d", mDay)).append(".")
                        // Month is 0 based so add 1
                        .append(String.format("%02d", mMonth + 1)).append(".")
                        .append(mYear).append(" "));
    }

    public void saveItem(View v) {
        int lSumVal;
        if (mSumVal.getText().toString().equalsIgnoreCase("")) {
            lSumVal = 0;
        } else {
            lSumVal = Integer.parseInt(mSumVal.getText().toString());
        }

        if (lSumVal < 1)
            MainActivity.this.ShowToast(getResources().getString(R.string.err_enter_amount), Toast.LENGTH_LONG);
        else if (spn2.getCount() < 1)
            MainActivity.this.ShowToast(getResources().getString(R.string.err_load_cats), Toast.LENGTH_LONG);
        else if (spn2.getSelectedItemId() < 1 && !tb1.isChecked() && !tb2.isChecked() && !tb3.isChecked() && !tb4.isChecked() && !tb5.isChecked() && !tb6.isChecked())
            MainActivity.this.ShowToast(getResources().getString(R.string.err_choose_cat), Toast.LENGTH_LONG);
        else {

            long cat_id = spn2.getSelectedItemId();

            // TODO avoid hardcode
            if (tb1.isChecked()) cat_id = 53;
            if (tb2.isChecked()) cat_id = 52;
            if (tb3.isChecked()) cat_id = 88;
            if (tb4.isChecked()) cat_id = 123;
            if (tb5.isChecked()) cat_id = 87;
            if (tb6.isChecked()) cat_id = 69;

            dbHelper = new DBHelper(MainActivity.this);

            long lastInsertId = dbHelper.insertExpense(cat_id, lSumVal, (String) mPickDate.getText(), mComment.getText().toString());

            if (lastInsertId > 0) {
                MainActivity.this.showSaveResult(lastInsertId);
            }

            dbHelper.close();
            MainActivity.this.updateSyncLabel();
            mSumVal.setText("");
            mComment.setText("");
            MainActivity.this.nullToggles(-1);
            spn1.setSelection(0);
            spn2.setSelection(0);
            mSumVal.requestFocus();
            MainActivity.this.updateLogLabel();
        }
    }
/*
    @Override
    public void onBackPressed(){
        if(drawerResult.isDrawerOpen()){
            drawerResult.closeDrawer();
        }
        else{
            super.onBackPressed();
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
/*
        drawerResult = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_home).withBadge("99").withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_gamepad),
                        new PrimaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_eye).withBadge("6").withIdentifier(2),
                        new SectionDrawerItem().withName(R.string.drawer_item_home),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_question).setEnabled(false),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_home).withIcon(FontAwesome.Icon.faw_bullhorn)
                )

                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public boolean equals(Object o) {
                        return super.equals(o);
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        //Toast.makeText(MainActivity.this, "onDrawerOpened", Toast.LENGTH_SHORT).show();
                        hideSoftKeyboard(MainActivity.this);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        //Toast.makeText(MainActivity.this, "onDrawerClosed", Toast.LENGTH_SHORT).show();
                    }
                })
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof Nameable) {
                            Toast.makeText(MainActivity.this, MainActivity.this.getString(((Nameable) drawerItem).getNameRes()), Toast.LENGTH_SHORT).show();
                        }

                        if (drawerItem instanceof Badgeable) {
                            Badgeable badgeable = (Badgeable) drawerItem;
                            if (badgeable.getBadge() != null) {
                                //note don't do this if your badge contains a "+"
                                int badge = Integer.valueOf(badgeable.getBadge());
                                if (badge > 0) {
                                    drawerResult.updateBadge(String.valueOf(badge - 1), position);
                                }
                            }
                        }
                    }
                })
                .withOnDrawerItemLongClickListener(new Drawer.OnDrawerItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        if (drawerItem instanceof SecondaryDrawerItem) {
                            Toast.makeText(MainActivity.this, MainActivity.this.getString(((SecondaryDrawerItem) drawerItem).getNameRes()), Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                }).build();
*/
        // Date
        mPickDate = (Button) findViewById(R.id.dateBtn);
        this.setTodayDate();

        // add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        // Categories
        tb1 = (ToggleButton) findViewById(R.id.toggleButton1);
        tb2 = (ToggleButton) findViewById(R.id.toggleButton2);
        tb3 = (ToggleButton) findViewById(R.id.toggleButton3);
        tb4 = (ToggleButton) findViewById(R.id.toggleButton4);
        tb5 = (ToggleButton) findViewById(R.id.toggleButton5);
        tb6 = (ToggleButton) findViewById(R.id.toggleButton6);
        tbMore = (Button) findViewById(R.id.buttonMore);
        tb1.setOnCheckedChangeListener(this);
        tb2.setOnCheckedChangeListener(this);
        tb3.setOnCheckedChangeListener(this);
        tb4.setOnCheckedChangeListener(this);
        tb5.setOnCheckedChangeListener(this);
        tb6.setOnCheckedChangeListener(this);
        tbMore.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                spn1.performClick();
            }
        });

        spn1 = (Spinner) findViewById(R.id.spinnerCat1);
        spn2 = (Spinner) findViewById(R.id.spinnerCat2);

        spn1.setOnItemSelectedListener(this);
        spn2.setOnItemSelectedListener(this);
        this.loadCategoriesLevel1();



        // Sum
        mSumVal = (EditText) findViewById(R.id.editText1);

        //Comment
        mComment = (EditText) findViewById(R.id.editText2);

        mLog = (TextView) findViewById(R.id.textViewLog);
        this.updateLogLabel();
        this.updateSyncLabel();

        // Фокус, чтобы показать сразу клавиатуру
        mSumVal.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setTodayDate();
    }


    protected void ShowToast(String txt, int lng) {
        Toast toast = Toast.makeText(MainActivity.this, txt, lng);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    protected void updateSyncLabel() {
        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor mCur = db.query("data_", null, null, null, null, null, "_id");
//        mSyncBtn.setText(getResources().getString(R.string.sync_button) + " (" + Integer.toString(mCur.getCount()) + ")");
        setNotifCount(mCur.getCount());

        mCur.close();
        dbHelper.close();
    }

    protected void showSaveResult(long id) {

        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String sqlQuery = "select d.val as val, c.name "
                + "from data_ as d "
                + "inner join category as c "
                + "on c._id = d.cat_id "
                + " WHERE d._id = " + Long.toString(id);

        Cursor mCur = db.rawQuery(sqlQuery, null);

        if (mCur.moveToFirst()) {
            int nameColIndex = mCur.getColumnIndex("name");
            int valColIndex = mCur.getColumnIndex("val");
            MainActivity.this.ShowToast(Integer.toString(mCur.getInt(valColIndex)) + " " + mCur.getString(nameColIndex), Toast.LENGTH_SHORT);
        }

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

        int i = 0;
        if (mCur.moveToFirst()) {

            int nameColIndex = mCur.getColumnIndex("name");
            int valColIndex = mCur.getColumnIndex("val");

            do {
                if (i > 0)
                    mLog.append("\n");
                mLog.append(mCur.getString(nameColIndex) + " " + Integer.toString(mCur.getInt(valColIndex)));
                i++;
            } while (mCur.moveToNext());
        }

        mCur.close();
        dbHelper.close();
    }

    protected void loadCategoriesLevel1() {
        dbHelper = new DBHelper(this);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] queryCols = new String[]{"_id", "name"};
        String[] adapterCols = new String[]{"name"};
        int[] adapterRowViews = new int[]{android.R.id.text1};
        Cat1Cursor = db.query(true, "category", queryCols, "parent_id is null", null, null, null, "name", null);
        sca1 = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, Cat1Cursor, adapterCols, adapterRowViews, 0);
        sca1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn1.setAdapter(sca1);

        dbHelper.close();
    }



    protected void loadCategoriesLevel2(long parent_id) {
        dbHelper = new DBHelper(this);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] queryCols = new String[]{"_id", "name"};
        String[] adapterCols = new String[]{"name"};
        int[] adapterRowViews = new int[]{android.R.id.text1};
        Cat2Cursor = db.query(true, "category", queryCols, "parent_id = " + parent_id, null, null, null, "name", null);
        sca2 = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, Cat2Cursor, adapterCols, adapterRowViews, 0);
        sca2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn2.setAdapter(sca2);
        if (Cat2Cursor.getCount()>1) {spn2.performClick();}
        dbHelper.close();


    }

    // Categories 1 - select element
    public void onItemSelected(
            AdapterView<?> parent, View v, int position, long id) {

        switch (parent.getId()) {
            case R.id.spinnerCat1:
                this.loadCategoriesLevel2(id);
                mSumVal.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                break;
            case R.id.spinnerCat2:
                break;
        }
    }

    // Categories 1 - nothing selected
    public void onNothingSelected(AdapterView<?> parent) {
        //mLabel.setText("");
    }

    // Dialogs
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case CAT_PROGRESS_DIALOG_ID:
                mCatProgressDialog = new ProgressDialog(MainActivity.this);
                //mCatProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mCatProgressDialog.setCanceledOnTouchOutside(false);
                mCatProgressDialog.setCancelable(false);
                mCatProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mCatProgressDialog.setMessage(getResources().getString(R.string.info_loading_cats));
                return mCatProgressDialog;

            case SYNC_PROGRESS_DIALOG_ID:
                mSyncProgressDialog = new ProgressDialog(MainActivity.this);
                //mCatProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mSyncProgressDialog.setCanceledOnTouchOutside(false);
                mSyncProgressDialog.setCancelable(false);
                mSyncProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mSyncProgressDialog.setMessage(getResources().getString(R.string.info_syncing));
                return mSyncProgressDialog;

            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);

            case KEY_DIALOG_ID:
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.key_dialog, (ViewGroup) findViewById(R.id.key_dialog_layout));

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(layout);

                final EditText keyDlgEdit = (EditText) layout.findViewById(R.id.key_edit_text);

                builder.setMessage(getResources().getString(R.string.placeholder1));

                builder.setPositiveButton(getResources().getString(R.string.save_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        // update secret key
                        dbHelper = new DBHelper(MainActivity.this);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        ContentValues cv = new ContentValues();
                        cv.put("key_val", keyDlgEdit.getText().toString());
                        db.update("keys", cv, "_id = ?", new String[]{"1"});
                        dbHelper.close();

                        keyDlgEdit.setText(""); // Чистим чтобы не палить ключ

                        //MainActivity.this.finish();
                    }
                });

                builder.setNegativeButton(getResources().getString(R.string.cancel_button), new DialogInterface.OnClickListener() {
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

        getMenuInflater().inflate(R.menu.main, menu);

        if (mNotifCount > 0) {
            ActionItemBadge.update(this, menu.findItem(R.id.action_sync), new IconicsDrawable(this, GoogleMaterial.Icon.gmd_sync).color(Color.BLACK).actionBarSize(), ActionItemBadge.BadgeStyle.YELLOW, mNotifCount);
        } else {
            ActionItemBadge.hide(menu.findItem(R.id.action_sync));
        }

        menu.add(Menu.NONE, IDM_UPDATE_CATEGORIES, Menu.NONE, getResources().getString(R.string.menu_update_cats));
        menu.add(Menu.NONE, IDM_KEY_SYNC, Menu.NONE, getResources().getString(R.string.menu_enter_key));
        return super.onCreateOptionsMenu(menu);
    }

    private void setNotifCount(int count) {
        mNotifCount = count;
        invalidateOptionsMenu();
    }

    // Define the Handler that receives messages from the thread and update the progress
    // Поток для обновления категорий из инета
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {

            String rsp = msg.getData().getString("rsp");
            Boolean lWasError = msg.getData().getBoolean("wasError");

            mThreadUpdateCategories.setState(ThreadUpdateCategories.STATE_DONE);

            if (lWasError) {
                MainActivity.this.ShowToast(getResources().getString(R.string.err_update_cats) + ": " + rsp, Toast.LENGTH_LONG);
            } else {

                long lInsertedRows = 0;
                long lastInsertId;

                try {
                    // http://stackoverflow.com/questions/9605913/how-to-parse-json-in-android
                    JSONObject jObject = new JSONObject(rsp);
                    JSONArray jArray = jObject.getJSONArray("cats");

                    dbHelper = new DBHelper(MainActivity.this);
                    dbHelper.deleteCategories();

                    lastInsertId = dbHelper.insertCategory(0, -1, " —", "expense");
                    lastInsertId = dbHelper.insertCategory(-1, 0, " —", "expense");

                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject oneObject = jArray.getJSONObject(i);
                        // Pulling items from the array
                        String json_id = oneObject.getString("id");
                        String json_name = oneObject.getString("name");
                        String json_parent_id = oneObject.getString("parent_id");
                        String json_pe = oneObject.getString("pe");

                        try {

                            lastInsertId = dbHelper.insertCategory(Integer.parseInt(json_id), Integer.parseInt(json_parent_id), json_name, json_pe);
                            if (lastInsertId > 0) {
                                lInsertedRows++;
                            }

                        } catch (Exception e) {
                            //Toast.makeText(getApplicationContext(), "EX2! " + "Database Exception" + e.getMessage(), Toast.LENGTH_LONG).show();
                            MainActivity.this.ShowToast("EX2! " + "Database Exception" + e.getMessage(), Toast.LENGTH_LONG);
                        }

                    }

                    MainActivity.this.loadCategoriesLevel1();

                    dbHelper.close();

                } catch (JSONException e) {
                    //Toast.makeText(getApplicationContext(), "EX3! " + "JSON Exception: "+e.getMessage(), Toast.LENGTH_LONG).show();
                    MainActivity.this.ShowToast("EX3! " + "JSON Exception: " + e.getMessage(), Toast.LENGTH_LONG);
                }

                MainActivity.this.ShowToast(String.format(getResources().getString(R.string.info_cats_updated), lInsertedRows), Toast.LENGTH_SHORT);

            } // was Inet Error ?

            dismissDialog(CAT_PROGRESS_DIALOG_ID);
        }
    };


    // Поток для синхронизации расходов
    final Handler handlerSync = new Handler() {
        public void handleMessage(Message msg) {

            try {
                String rsp = msg.getData().getString("rsp");
                Boolean lWasError = msg.getData().getBoolean("wasError");

                mThreadSyncExpenses.setState(ThreadSyncExpenses.STATE_DONE);

                if (lWasError) {
                    MainActivity.this.ShowToast(getResources().getString(R.string.err_sync) + ": " + rsp, Toast.LENGTH_LONG);
                } else {
                    //dbHelper = new DBHelper(MainActivity.this);
                    //dbHelper.deleteExpenses(); // удаляем траты локально
                    //dbHelper.close();
                    MainActivity.this.ShowToast(rsp, Toast.LENGTH_SHORT);
                }
                MainActivity.this.updateSyncLabel();
                MainActivity.this.updateLogLabel();
                dismissDialog(SYNC_PROGRESS_DIALOG_ID);
            } catch (Exception e) {
                MainActivity.this.ShowToast("EX4! " + e.toString() + " Message:" + e.getMessage(), Toast.LENGTH_LONG);
            }


        }
    };


    // Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SQLiteDatabase db;
        switch (item.getItemId()) {
            case IDM_UPDATE_CATEGORIES:

                // Даем обновлять категории только если нет несинхронизированных расходов
                dbHelper = new DBHelper(this);
                db = dbHelper.getWritableDatabase();
                Cursor tmp = db.query(true, "data_", null, null, null, null, null, null, null);

                if (tmp.getCount() > 0) {
                    MainActivity.this.ShowToast(getResources().getString(R.string.err_sync_first) + "!", Toast.LENGTH_LONG);
                } else {
                    showDialog(CAT_PROGRESS_DIALOG_ID);
                    // Запускаем новый поток для вытягивания категорий с удаленного сервера
                    mThreadUpdateCategories = new ThreadUpdateCategories(handler, getApplicationContext());
                    mThreadUpdateCategories.setSecretKey(dbHelper.getSecretKey());
                    mThreadUpdateCategories.setState(ThreadUpdateCategories.STATE_RUNNING);
                    mThreadUpdateCategories.start();
                }

                tmp.close();
                dbHelper.close();

                break;
            case IDM_KEY_SYNC:
                showDialog(KEY_DIALOG_ID);
                break;
            case R.id.action_sync:
                dbHelper = new DBHelper(MainActivity.this);
                db = dbHelper.getWritableDatabase();
                Cursor mCur = db.query("data_", null, null, null, null, null, "_id");
                long lExCnt = mCur.getCount();
                mCur.close();
                dbHelper.close();
                if (lExCnt < 1) {
                    MainActivity.this.ShowToast(getResources().getString(R.string.err_nothing_to_sync), Toast.LENGTH_LONG);
                } else {
                    showDialog(SYNC_PROGRESS_DIALOG_ID);
                    mThreadSyncExpenses = new ThreadSyncExpenses(handlerSync, getApplicationContext());
                    mThreadSyncExpenses.setSecretKey(dbHelper.getSecretKey());
                    //mThreadSyncExpenses.setVars(s_date, s_et, s_comment, s_cat, s_val);
                    mThreadSyncExpenses.setState(ThreadSyncExpenses.STATE_RUNNING);
                    mThreadSyncExpenses.start();
                }
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
        if (except_id != tb5.getId())
            tb5.setChecked(false);
        if (except_id != tb6.getId())
            tb6.setChecked(false);
    }

    @Override
    public void onCheckedChanged(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            this.nullToggles(button.getId());
            //text.setText("Button checked");
        } else {
            // text.setText("Button unchecked");
        }
        mSumVal.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }


}
