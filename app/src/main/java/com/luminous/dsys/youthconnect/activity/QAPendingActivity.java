package com.luminous.dsys.youthconnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.pojo.Answer;
import com.luminous.dsys.youthconnect.pojo.PendingFileToUpload;
import com.luminous.dsys.youthconnect.qa.QaListAdapter;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luminousinfoways on 18/12/15.
 */
public class QAPendingActivity extends BaseActivity implements
        QaListAdapter.OnDeleteClickListener, QaListAdapter.OnUpdateClickListenr,
        Replication.ChangeListener{

    private static final String TAG = "QAPendingActivity";
    private SwipeMenuListView mListView = null;
    private QaListAdapter mAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qa);

        if (null != toolbar) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

            toolbar.setTitle(getResources().getString(R.string.activity_pending_question_title));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(QAPendingActivity.this);
                }
            });
        }

        mListView = (SwipeMenuListView) findViewById(R.id.listView);
        init();
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                int user_type_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0)
                        .getInt(Constants.SP_USER_TYPE, 0);
                final Document qaDoc = (Document) mListView
                        .getItemAtPosition(position);
                String title = (String) qaDoc.getProperty(BuildConfigYouthConnect.QA_TITLE);
                final String description = (String) qaDoc.getProperty(BuildConfigYouthConnect.QA_DESC);
                switch (index) {
                    case 0:
                        // edit.

                        // get prompts.xml view
                        LayoutInflater li = LayoutInflater.from(QAPendingActivity.this);
                        View promptsView = li.inflate(R.layout.alert_dialog_with_input_text, null);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                QAPendingActivity.this);

                        // set prompts.xml to alertdialog builder
                        alertDialogBuilder.setView(promptsView);

                        final EditText userInputTitle = (EditText) promptsView
                                .findViewById(R.id.editTextDialogUserInputQTitle);
                        final EditText userInputDesc = (EditText) promptsView
                                .findViewById(R.id.editTextDialogUserInputQDescription);
                        TextView textView1 = (TextView) promptsView.findViewById(R.id.textView1);
                        TextView questionDescription = (TextView) promptsView.findViewById(R.id.textViewQuestion);
                        questionDescription.setText(description);

                        questionDescription.setVisibility(View.GONE);
                        textView1.setText("Edit this question.");
                        userInputDesc.setVisibility(View.VISIBLE);

                        // set dialog message
                        alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // get user input and set it to result
                                        // edit text
                                        dialog.dismiss();

                                        mInputedStringForEditQuestionTitle = userInputTitle.getText().toString().trim();
                                        mInputedStringForEditQuestionDescription = userInputDesc.getText().toString().trim();

                                        if (mInputedStringForEditQuestionTitle == null
                                                || mInputedStringForEditQuestionTitle.trim().length() <= 0) {

                                            AlertDialog.Builder builder = new AlertDialog.Builder(QAPendingActivity.this,
                                                    R.style.AppCompatAlertDialogStyle);
                                            builder.setTitle("Edit Question");
                                            builder.setMessage("Provide title for question.");
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    return;
                                                }
                                            });
                                            builder.show();
                                        } else if (mInputedStringForEditQuestionDescription == null
                                                || mInputedStringForEditQuestionDescription.trim().length() <= 0) {

                                            AlertDialog.Builder builder = new AlertDialog.Builder(QAPendingActivity.this,
                                                    R.style.AppCompatAlertDialogStyle);
                                            builder.setTitle("Edit Question");
                                            builder.setMessage("Provide description for question.");
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    return;
                                                }
                                            });
                                            builder.show();
                                        } else {

                                            try {
                                                // Update the document with more data
                                                Map<String, Object> updatedProperties = new HashMap<String, Object>();
                                                updatedProperties.putAll(qaDoc.getProperties());
                                                updatedProperties.put(BuildConfigYouthConnect.QA_TITLE, mInputedStringForEditQuestionTitle);
                                                updatedProperties.put(BuildConfigYouthConnect.QA_DESC, mInputedStringForEditQuestionDescription);
                                                qaDoc.putProperties(updatedProperties);
                                            } catch (CouchbaseLiteException e) {
                                                Log.e(TAG, "Error putting", e);
                                            }
                                        }
                                    }
                                })
                            .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();

                            // show it
                            alertDialog.show();

                        break;
                    case 1:
                        // delete only admin can do
                        if(user_type_id == 1){
                            final Document _qaDoc = (Document) mListView
                                    .getItemAtPosition(position);
                            try {
                                // Update the document with more data
                                Map<String, Object> updatedProperties = new HashMap<String, Object>();
                                updatedProperties.putAll(_qaDoc.getProperties());
                                updatedProperties.put(BuildConfigYouthConnect.QA_IS_DELETE, 1);
                                _qaDoc.putProperties(updatedProperties);
                            } catch (CouchbaseLiteException e) {
                                Log.e(TAG, "Error putting", e);
                            }
                        }
                        break;
                    case 2:
                        // answer only admin can do
                        if(user_type_id == 1){

                            // get prompts.xml view
                            LayoutInflater li1 = LayoutInflater.from(QAPendingActivity.this);
                            View promptsView1 = li1.inflate(R.layout.alert_dialog_with_input_text, null);

                            AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                                    QAPendingActivity.this);

                            // set prompts.xml to alertdialog builder
                            alertDialogBuilder1.setView(promptsView1);

                            final EditText userInputTitle1 = (EditText) promptsView1
                                    .findViewById(R.id.editTextDialogUserInputQTitle);
                            final EditText userInputDesc1 = (EditText) promptsView1
                                    .findViewById(R.id.editTextDialogUserInputQDescription);
                            TextView textView11 = (TextView) promptsView1.findViewById(R.id.textView1);
                            TextView questionDescription11 = (TextView) promptsView1.findViewById(R.id.textViewQuestion);
                            questionDescription11.setText(description);

                            questionDescription11.setVisibility(View.VISIBLE);
                            textView11.setText("Answer this question.");
                            userInputDesc1.setVisibility(View.GONE);

                            // set dialog message
                            alertDialogBuilder1
                                .setCancelable(false)
                                .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // get user input and set it to result
                                            // edit text
                                            dialog.dismiss();

                                            mInputedStringForAnswer = userInputTitle1.getText().toString().trim();

                                            if (mInputedStringForAnswer == null
                                                    || mInputedStringForAnswer.trim().length() <= 0) {

                                                AlertDialog.Builder builder = new AlertDialog.Builder(QAPendingActivity.this,
                                                        R.style.AppCompatAlertDialogStyle);
                                                builder.setTitle("Answer Question");
                                                builder.setMessage("Answer to this question.");
                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        return;
                                                    }
                                                });
                                                builder.show();
                                            } else {

                                                int currently_logged_in_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                                        .getInt(Constants.SP_USER_ID, 0);
                                                String currently_logged_in_user_name = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                                        .getString(Constants.SP_USER_NAME, "");
                                                Answer answer = new Answer(Parcel.obtain());
                                                answer.setAnswer_by_user_id(currently_logged_in_user_id);
                                                answer.setAnswer_by_user_name(currently_logged_in_user_name);
                                                answer.setQadmin_description(mInputedStringForAnswer);

                                                List<Answer> answerList = new ArrayList<Answer>();
                                                answerList.add(answer);

                                                try {
                                                    // Update the document with more data
                                                    Map<String, Object> updatedProperties = new HashMap<String, Object>();
                                                    updatedProperties.putAll(qaDoc.getProperties());
                                                    updatedProperties.put(BuildConfigYouthConnect.QA_ANSWER, answerList);
                                                    updatedProperties.put(BuildConfigYouthConnect.QA_IS_ANSWERED, 1);
                                                    qaDoc.putProperties(updatedProperties);
                                                } catch (CouchbaseLiteException e) {
                                                    Log.e(TAG, "Error putting", e);
                                                }
                                            }
                                        }
                                    })
                                .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                            // create alert dialog
                            AlertDialog alertDialog1 = alertDialogBuilder1.create();

                            // show it
                            alertDialog1.show();

                            return false;
                        }
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        try {
            showListInListView();
        } catch (CouchbaseLiteException exception){
            Log.e(TAG, "onCreate()", exception);
        } catch(IOException exception){
            Log.e(TAG, "onCreate()", exception);
        } catch(Exception exception){
            Log.e(TAG, "onCreate()", exception);
        }
    }

    private String mInputedStringForEditQuestionTitle = "";
    private String mInputedStringForEditQuestionDescription = "";
    private String mInputedStringForAnswer  = "";

    private void init(){

        final int user_type_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getInt(Constants.SP_USER_TYPE, 0);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                if(user_type_id == 1){
                    // create "edit" item
                    SwipeMenuItem editItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    editItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                            0xCE)));
                    // set item width
                    editItem.setWidth(Util.dp2px(90, QAPendingActivity.this));
                    // set item title
                    editItem.setTitle("Edit");
                    // set item title fontsize
                    editItem.setTitleSize(18);
                    // set item title font color
                    editItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(editItem);

                    // create "delete" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                            0x3F, 0x25)));
                    // set item width
                    deleteItem.setWidth(Util.dp2px(90, QAPendingActivity.this));
                    // set a icon
                    deleteItem.setIcon(R.drawable.ic_delete_white);
                    // add to menu
                    menu.addMenuItem(deleteItem);

                    // create "answer" item
                    SwipeMenuItem answerItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    answerItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xCE,
                            0xC9)));
                    // set item width
                    answerItem.setWidth(Util.dp2px(90, QAPendingActivity.this));
                    // set item title
                    answerItem.setTitle("Answer");
                    // set item title fontsize
                    answerItem.setTitleSize(18);
                    // set item title font color
                    answerItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(answerItem);
                } else {
                    // create "edit" item
                    SwipeMenuItem openItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                            0xCE)));
                    // set item width
                    openItem.setWidth(Util.dp2px(90, QAPendingActivity.this));
                    // set item title
                    openItem.setTitle("Edit");
                    // set item title fontsize
                    openItem.setTitleSize(18);
                    // set item title font color
                    openItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(openItem);
                }
            }
        };

        // set creator
        mListView.setMenuCreator(creator);
        // Left
        mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_ask_question) {
            Intent intent = new Intent(QAPendingActivity.this, AskQuestionActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_create_document) {
            Intent intent = new Intent(QAPendingActivity.this, AttachFileActivity.class);
            if (AttachFileActivity.fileUploadList != null) {
                AttachFileActivity.fileUploadList.clear();
            } else {
                AttachFileActivity.fileUploadList = new ArrayList<PendingFileToUpload>();
            }
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
    }

    private void showListInListView() throws CouchbaseLiteException, IOException {
        mListView = (SwipeMenuListView) findViewById(R.id.listView);

        int currently_logged_in_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0)
                .getInt(Constants.SP_USER_ID, 0);

        if(currently_logged_in_user_id == 1){
            //User is Admin
            if(application.getQAUnAnsweredForAdminQuery(application.getDatabase()) != null) {
                mAdapter = new QaListAdapter(this, application.getQAUnAnsweredForAdminQuery
                        (application.getDatabase()).toLiveQuery(),
                        this, this, false, true, false);
                mListView.setAdapter(mAdapter);
            }
        } else{
            //User is nodal Officer
            if(application.getQAUnAnsweredForNodalQuery(application.getDatabase()) != null) {
                mAdapter = new QaListAdapter(this, application.getQAUnAnsweredForNodalQuery
                        (application.getDatabase()).toLiveQuery(),
                        this, this, false, true, false);
                mListView.setAdapter(mAdapter);
            }
        }
    }

    @Override
    public void onUpdateClick(Document student) {

    }

    @Override
    public void onDeleteClick(Document student) {

    }

    @Override
    public void changed(Replication.ChangeEvent event) {
        try {
            showListInListView();
        } catch(CouchbaseLiteException exception){
            Log.e(TAG, "onCreate()", exception);
        } catch(IOException exception){
            Log.e(TAG, "onCreate()", exception);
        } catch(Exception exception){
            Log.e(TAG, "onCreate()", exception);
        }
    }
}