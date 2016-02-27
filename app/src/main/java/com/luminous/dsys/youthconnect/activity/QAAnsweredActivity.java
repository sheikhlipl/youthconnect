package com.luminous.dsys.youthconnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.luminous.dsys.youthconnect.BuildConfig;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.pojo.Answer;
import com.luminous.dsys.youthconnect.pojo.Comment;
import com.luminous.dsys.youthconnect.pojo.FileToUpload;
import com.luminous.dsys.youthconnect.pojo.PendingFileToUpload;
import com.luminous.dsys.youthconnect.pojo.Question;
import com.luminous.dsys.youthconnect.pojo.QuestionAndAnswer;
import com.luminous.dsys.youthconnect.qa.QaListAdapter;
import com.luminous.dsys.youthconnect.qa.QaListAdapter1;
import com.luminous.dsys.youthconnect.swipemenu.SwipeMenu;
import com.luminous.dsys.youthconnect.swipemenu.SwipeMenuCreator;
import com.luminous.dsys.youthconnect.swipemenu.SwipeMenuItem;
import com.luminous.dsys.youthconnect.swipemenu.SwipeMenuListView;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luminousinfoways on 18/12/15.
 */
public class QAAnsweredActivity extends BaseActivity implements
        QaListAdapter1.OnDeleteClickListener, QaListAdapter1.OnUpdateClickListenr,
        Replication.ChangeListener{

    private static final String TAG = "QAPendingActivity";
    private ListView mListView = null;
    private QaListAdapter1 mAdapter = null;
    private Menu menu;
    private int nr = 0;

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

        mListView = (ListView) findViewById(R.id.listView);

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

        final int user_type_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getInt(Constants.SP_USER_TYPE, 0);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                if(user_type_id == 1){
                    // create "delete" item
                    SwipeMenuItem postComment = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    postComment.setBackground(new ColorDrawable(Color.rgb(0xF9,
                            0x5F, 0x25)));
                    // set item width
                    postComment.setWidth(Util.dp2px(90, QAAnsweredActivity.this));
                    // set a icon
                    postComment.setIcon(R.drawable.ic_comment_white);
                    // add to menu
                    menu.addMenuItem(postComment);

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

                    // create "answer" item
                    SwipeMenuItem answerItem = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    answerItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xCE,
                            0xC9)));
                    // set item width
                    answerItem.setWidth(Util.dp2px(90, QAAnsweredActivity.this));
                    // set item title
                    answerItem.setTitle("Publish");
                    // set item title fontsize
                    answerItem.setTitleSize(18);
                    // set item title font color
                    answerItem.setTitleColor(Color.WHITE);
                    // add to menu
                    menu.addMenuItem(answerItem);
                } else {
                    // create "delete" item
                    SwipeMenuItem postComment = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    postComment.setBackground(new ColorDrawable(Color.rgb(0xF9,
                            0x5F, 0x25)));
                    // set item width
                    postComment.setWidth(Util.dp2px(90, QAAnsweredActivity.this));
                    // set a icon
                    postComment.setIcon(R.drawable.ic_comment_white);
                    // add to menu
                    menu.addMenuItem(postComment);

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
            }
        };
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
            Intent intent = new Intent(QAAnsweredActivity.this, AttachFileActivity.class);
            if (AttachFileActivity.fileUploadList != null) {
                AttachFileActivity.fileUploadList.clear();
            } else {
                AttachFileActivity.fileUploadList = new ArrayList<PendingFileToUpload>();
            }
            if (AttachFileActivity.fileToUploads != null) {
                AttachFileActivity.fileToUploads.clear();
            } else {
                AttachFileActivity.fileToUploads = new ArrayList<FileToUpload>();
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
        mListView = (ListView) findViewById(R.id.listView);

        int currently_logged_in_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0)
                .getInt(Constants.SP_USER_ID, 0);

        if(currently_logged_in_user_id == 1){
            //User is Admin
            if(application.getQAAnsweredForAdminQuery(application.getDatabase()) != null) {
                mAdapter = new QaListAdapter1(this, application.getQAAnsweredForAdminQuery
                        (application.getDatabase()).toLiveQuery(),
                        this, this, false, false, true);
                mListView.setAdapter(mAdapter);
            }
        } else{
            //User is nodal Officer
            if(application.getQAAnsweredForNodalQuery(application.getDatabase()) != null) {
                mAdapter = new QaListAdapter1(this, application.getQAAnsweredForNodalQuery
                        (application.getDatabase()).toLiveQuery(),
                        this, this, false, false, true);
                mListView.setAdapter(mAdapter);
            }
        }

        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                mAdapter.clearSelection();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub

                nr = 0;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.contextual_menu_qa_answered, menu);
                QAAnsweredActivity.this.menu = menu;

                int user_type_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1).getInt(Constants.SP_USER_TYPE, 0);
                if(user_type_id == 2){
                    menu.getItem(0).setVisible(false);
                    menu.getItem(2).setVisible(false);
                }

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO Auto-generated method stub
                switch (item.getItemId()) {

                    case R.id.item_delete:
                        mAdapter.deleteQA();
                        mode.finish();
                        break;

                    case R.id.item_publish_unpublish:
                        mAdapter.publishQA();
                        mode.finish();
                        break;

                    case R.id.item_comment:
                        mAdapter.postComment();
                        mode.finish();
                        break;
                }
                return true;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                // TODO Auto-generated method stub
                if (checked) {
                    nr++;
                    mAdapter.setNewSelection(position, checked);
                } else {
                    nr--;
                    mAdapter.removeSelection(position);
                }
                mode.setTitle(nr + " selected");
                changeAndInflate();
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                // TODO Auto-generated method stub

                mListView.setItemChecked(position, !mAdapter.isPositionChecked(position));
                return false;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View v = mAdapter.getView(position, view, parent);
                RelativeLayout layoutFileList = (RelativeLayout) v.findViewById(R.id.layoutFileList);
                if (layoutFileList.getVisibility() == View.VISIBLE) {
                    layoutFileList.setVisibility(View.GONE);
                } else {
                    layoutFileList.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void changeAndInflate(){
        if(nr > 1) {
            menu.getItem(2).setVisible(false);
        } else{
            menu.getItem(2).setVisible(true);
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

    public static QuestionAndAnswer getQAFromDocument(Document document){

        try {
            String title = (String) document.getProperty(BuildConfigYouthConnect.QA_TITLE);
            String description = (String) document.getProperty(BuildConfigYouthConnect.QA_DESC);
            String updated_time_stamp = (String) document.getProperty(BuildConfigYouthConnect.QA_UPDATED_TIMESTAMP);

            String postDate = updated_time_stamp;
            String user_name = (String) document.getProperty(BuildConfigYouthConnect.QA_ASKED_BY_USER_NAME);
            int is_answered = (Integer) document.getProperty(BuildConfigYouthConnect.QA_IS_ANSWERED);
            int is_published = (Integer) document.getProperty(BuildConfigYouthConnect.QA_IS_PUBLISHED);
            int is_uploaded = (Integer) document.getProperty(BuildConfigYouthConnect.QA_IS_UPLOADED);
            int asked_by_user_id = (Integer) document.getProperty(BuildConfigYouthConnect.QA_ASKED_BY_USER_ID);

            ArrayList<LinkedHashMap<String, String>> answerJson = (ArrayList<LinkedHashMap<String, String>>)
                    document.getProperty(BuildConfigYouthConnect.QA_ANSWER);
            ArrayList<LinkedHashMap<String, String>> commentJson = (ArrayList<LinkedHashMap<String, String>>)
                    document.getProperty(BuildConfigYouthConnect.QA_COMMENT);

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            if(answerJson != null && answerJson.size() > 0){
                for(int i = 0; i <answerJson.size(); i++){
                    LinkedHashMap<String, String> answers = (LinkedHashMap<String, String>) (answerJson.get(i));
                    if((answers.get("qadmin_description") != null)
                            && (answers.get("answer_by_user_name") != null)
                            && (answers.get("answer_by_user_name") != null)) {
                        Answer answer = new Answer(Parcel.obtain());
                        answer.setQadmin_description(answers.get("qadmin_description"));
                        answer.setAnswer_by_user_name(answers.get("answer_by_user_name"));
                        answer.setCreated(answers.get("created"));
                        answerArrayList.add(answer);
                    }
                }
            }

            ArrayList<Comment> commentArrayList = new ArrayList<Comment>();
            if(commentJson != null && commentJson.size() > 0){
                for(int i = 0; i <commentJson.size(); i++){
                    LinkedHashMap<String, String> comments = (LinkedHashMap<String, String>) (commentJson.get(i));
                    if((comments.get("comment_description") != null)
                            && (comments.get("comment_by_user_name") != null)
                            && (comments.get("created") != null)) {
                        Comment answer = new Comment(Parcel.obtain());
                        answer.setComment_description(comments.get("comment_description"));
                        answer.setComment_by_user_name(comments.get("comment_by_user_name"));
                        answer.setCreated(comments.get("created"));
                        commentArrayList.add(answer);
                    }
                }
            }

            String id = document.getId();

            Question question = new Question(Parcel.obtain());
            question.setQa_id(id);
            question.setQa_title(title);
            question.setQa_description(description);
            question.setAskedBy(user_name);
            question.setIs_answer(is_answered);
            question.setPost_date(postDate);
            question.setQus_asked_by_user_id(asked_by_user_id);
            question.setIs_uploaded(is_uploaded);

            QuestionAndAnswer questionAndAnswer = new QuestionAndAnswer(Parcel.obtain());
            questionAndAnswer.setQuestion(question);
            questionAndAnswer.setAnswerList(answerArrayList);
            questionAndAnswer.setCommentList(commentArrayList);
            questionAndAnswer.setIs_published(is_published);
            questionAndAnswer.setQid(id);

            return questionAndAnswer;
        } catch(Exception exception){
            android.util.Log.e("QAUtil", "getQAFromDocument()", exception);
        }
        return null;
    }
}