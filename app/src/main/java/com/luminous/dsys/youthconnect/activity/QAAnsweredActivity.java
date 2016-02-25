package com.luminous.dsys.youthconnect.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.qa.QaListAdapter;
import com.luminous.dsys.youthconnect.util.Util;

import java.io.IOException;

/**
 * Created by luminousinfoways on 18/12/15.
 */
public class QAAnsweredActivity extends BaseActivity implements
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
                    NavUtils.navigateUpFromSameTask(QAAnsweredActivity.this);
                }
            });
        }

        mListView = (SwipeMenuListView) findViewById(R.id.listView);
        init();
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // open
                        break;
                    case 1:
                        // delete
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

    private void init(){
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(Util.dp2px(90, QAAnsweredActivity.this));
                // set item title
                openItem.setTitle("Open");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(Util.dp2px(90, QAAnsweredActivity.this));
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
            Intent intent = new Intent(QAAnsweredActivity.this, AskQuestionActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_create_document) {
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
        if(application.getQAQuery(application.getDatabase()) != null) {
            mAdapter = new QaListAdapter(this, application.getQAQuery(application.getDatabase()).toLiveQuery(),
                    this, this, false, false, true);
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