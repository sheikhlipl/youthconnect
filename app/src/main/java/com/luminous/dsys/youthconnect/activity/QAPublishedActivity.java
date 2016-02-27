package com.luminous.dsys.youthconnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.pojo.PendingFileToUpload;
import com.luminous.dsys.youthconnect.qa.QaListAdapter;
import com.luminous.dsys.youthconnect.swipemenu.SwipeMenu;
import com.luminous.dsys.youthconnect.swipemenu.SwipeMenuCreator;
import com.luminous.dsys.youthconnect.swipemenu.SwipeMenuItem;
import com.luminous.dsys.youthconnect.swipemenu.SwipeMenuListView;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by luminousinfoways on 18/12/15.
 */
public class QAPublishedActivity extends BaseActivity implements
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
                    NavUtils.navigateUpFromSameTask(QAPublishedActivity.this);
                }
            });
        }

        mListView = (SwipeMenuListView) findViewById(R.id.listView);
        int user_type_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0)
                .getInt(Constants.SP_USER_TYPE, 0);
        if(user_type_id == 1) {
            init();
            mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    int user_type_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0)
                            .getInt(Constants.SP_USER_TYPE, 0);
                    switch (index) {
                        case 0:
                            // un-publish
                            if (user_type_id == 1) {
                                final Document _qaDoc = (Document) mListView
                                        .getItemAtPosition(position);

                                AlertDialog.Builder builder = new AlertDialog.Builder(QAPublishedActivity.this,
                                        R.style.AppCompatAlertDialogStyle);
                                builder.setTitle("Un-Publish Question");
                                builder.setMessage("Are you sure want to un-publish this question?");
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                        try {
                                            // Update the document with more data
                                            Map<String, Object> updatedProperties = new HashMap<String, Object>();
                                            updatedProperties.putAll(_qaDoc.getProperties());
                                            updatedProperties.put(BuildConfigYouthConnect.QA_IS_PUBLISHED, 0);
                                            _qaDoc.putProperties(updatedProperties);
                                        } catch (CouchbaseLiteException e) {
                                            Log.e(TAG, "Error putting", e);
                                        }

                                        AlertDialog.Builder builder = new AlertDialog.Builder(QAPublishedActivity.this,
                                                R.style.AppCompatAlertDialogStyle);
                                        builder.setTitle("Un-Publish Question");
                                        builder.setMessage("Done successfully.");
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();

                                                Intent intent = new Intent(QAPublishedActivity.this, QAAnsweredActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();

                                                return;
                                            }
                                        });
                                        builder.show();

                                        return;
                                    }
                                });
                                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        return;
                                    }
                                });
                                builder.show();
                            }
                            break;
                        case 1:
                            // delete
                            if (user_type_id == 1) {
                                final Document _qaDoc = (Document) mListView
                                        .getItemAtPosition(position);

                                AlertDialog.Builder builder = new AlertDialog.Builder(QAPublishedActivity.this,
                                        R.style.AppCompatAlertDialogStyle);
                                builder.setTitle("Delete Question");
                                builder.setMessage("Are you sure want to delete this question?");
                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                        try {
                                            // Update the document with more data
                                            Map<String, Object> updatedProperties = new HashMap<String, Object>();
                                            updatedProperties.putAll(_qaDoc.getProperties());
                                            updatedProperties.put(BuildConfigYouthConnect.QA_IS_DELETE, 1);
                                            _qaDoc.putProperties(updatedProperties);
                                        } catch (CouchbaseLiteException e) {
                                            Log.e(TAG, "Error putting", e);
                                        }

                                        AlertDialog.Builder builder = new AlertDialog.Builder(QAPublishedActivity.this,
                                                R.style.AppCompatAlertDialogStyle);
                                        builder.setTitle("Delete Question");
                                        builder.setMessage("Done successfully.");
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                return;
                                            }
                                        });
                                        builder.show();

                                        return;
                                    }
                                });
                                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        return;
                                    }
                                });
                                builder.show();
                            }
                            break;
                    }
                    // false : close the menu; true : not close the menu
                    return false;
                }
            });
        }

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

    private void init(){
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem unpublishItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                unpublishItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                unpublishItem.setWidth(Util.dp2px(90, QAPublishedActivity.this));
                // set a icon
                unpublishItem.setIcon(R.drawable.ic_not_interested);
                // add to menu
                menu.addMenuItem(unpublishItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(Util.dp2px(90, QAPublishedActivity.this));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete_white);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        mListView.setMenuCreator(creator);
        // Right
        mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);

        // Left
        //mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);

        // Right
        mListView.setSwipeDirection(SwipeMenuListView.DIRECTION_RIGHT);

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
            Intent intent = new Intent(QAPublishedActivity.this, AskQuestionActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_create_document) {
            Intent intent = new Intent(QAPublishedActivity.this, AttachFileActivity.class);
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
        if(application.getQAPublishedForQuery(application.getDatabase()) != null) {
            mAdapter = new QaListAdapter(this, application.getQAPublishedForQuery
                    (application.getDatabase()).toLiveQuery(),
                    this, this, true, false, false);
            mListView.setAdapter(mAdapter);
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