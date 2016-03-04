package com.luminous.dsys.youthconnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.luminous.dsys.youthconnect.BuildConfig;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.asynctask.PushToAllDeviceAsyncTask;
import com.luminous.dsys.youthconnect.pojo.Answer;
import com.luminous.dsys.youthconnect.pojo.Comment;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luminousinfoways on 18/12/15.
 */
public class AskQuestionActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "AskQuestionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_a_question);

        if (null != toolbar) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

            toolbar.setTitle(getResources().getString(R.string.activity_ask_a_question_title));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(AskQuestionActivity.this);
                }
            });

            // Inflate a menu to be displayed in the toolbar
            //toolbar.inflateMenu(R.menu.settings);

            TextView tvPost = (TextView) findViewById(R.id.tvPostQus);
            tvPost.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch(id){
            case R.id.tvPostQus:
                EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
                final String title= editTextTitle.getText().toString().trim();
                if(title == null || title.length() <= 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("Post Question");
                    builder.setMessage("Provide title for question.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            return;
                        }
                    });
                    builder.show();
                }

                EditText editTextDescription = (EditText) findViewById(R.id.editTextDescription);
                final String description= editTextDescription.getText().toString().trim();
                if(description == null || description.length() <= 0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("Post Question");
                    builder.setMessage("Provide description for question.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            return;
                        }
                    });
                    builder.show();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle("Post Question");
                builder.setMessage("Are you sure want to post this question?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {

                            createDocument(application.getDatabase(), title, description);
                            AlertDialog.Builder builder12 = new AlertDialog.Builder(AskQuestionActivity.this, R.style.AppCompatAlertDialogStyle);
                            builder12.setTitle("Post Question");
                            builder12.setMessage("Done.");
                            builder12.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    Intent intent = new Intent(AskQuestionActivity.this, QAPendingActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                                    finish();
                                }
                            });
                            builder12.show();
                        } catch(Exception exception){
                            Log.e(TAG, "on Add Click", exception);
                        }
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();


                break;
            default:
                break;
        }
    }

    private String createDocument(Database database, String title, String description) {

        int asked_by_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getInt(Constants.SP_USER_ID, 0);
        String user_name = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getString(Constants.SP_USER_NAME, "");

        // Create a new document and add data
        Document document = database.createDocument();
        String documentId = document.getId();
        Map<String, Object> map = new HashMap<String, Object>();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        map.put(BuildConfigYouthConnect.QA_TITLE, title);
        map.put("type", BuildConfigYouthConnect.DOC_TYPE_FOR_QA);
        map.put(BuildConfigYouthConnect.QA_DESC, description);
        map.put(BuildConfigYouthConnect.QA_UPDATED_TIMESTAMP, currentTimeString);
        map.put(BuildConfigYouthConnect.QA_ASKED_BY_USER_NAME, user_name);
        map.put(BuildConfigYouthConnect.QA_ASKED_BY_USER_ID, asked_by_user_id);
        map.put(BuildConfigYouthConnect.QA_IS_ANSWERED, 0);
        map.put(BuildConfigYouthConnect.QA_IS_PUBLISHED, 0);
        map.put(BuildConfigYouthConnect.QA_IS_UPLOADED, 0);
        map.put(BuildConfigYouthConnect.QA_IS_READ, 0);
        map.put(BuildConfigYouthConnect.QA_IS_DELETE, 0);
        map.put(BuildConfigYouthConnect.QA_ANSWER, new ArrayList<Answer>());
        map.put(BuildConfigYouthConnect.QA_COMMENT, new ArrayList<Comment>());
        try {
            // Save the properties to the document
            document.putProperties(map);
            com.couchbase.lite.util.Log.i(TAG, "Document created.");
        } catch (CouchbaseLiteException e) {
            com.couchbase.lite.util.Log.e(TAG, "Error putting", e);
        }

        if( Util.getNetworkConnectivityStatus(AskQuestionActivity.this) ) {
            PushToAllDeviceAsyncTask pushToAllDeviceAsyncTask
                    = new PushToAllDeviceAsyncTask(AskQuestionActivity.this);
            pushToAllDeviceAsyncTask.execute();
        }

        return documentId;
    }

    /**
     * When touch on screen outside the keyboard, the input keyboard will hide automatically
     * */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null &&
                (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) &&
                v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit."))
        {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                Util.hideKeyboard(this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ask_question_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
    }
}