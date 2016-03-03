package com.luminous.dsys.youthconnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.pojo.Answer;
import com.luminous.dsys.youthconnect.pojo.Comment;
import com.luminous.dsys.youthconnect.pojo.FileToUpload;
import com.luminous.dsys.youthconnect.pojo.PendingFileToUpload;
import com.luminous.dsys.youthconnect.pojo.QuestionAndAnswer;
import com.luminous.dsys.youthconnect.qa.QAUtil;
import com.luminous.dsys.youthconnect.qa.QaListAdapter;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luminousinfoways on 18/12/15.
 */
public class QAAnsweredActivity extends BaseActivity implements
        QaListAdapter.OnDeleteClickListener, QaListAdapter.OnUnPublishClickListenr,
        QaListAdapter.OnEditQuestionClickListenr, QaListAdapter.OnAnswerClickListenr,
        QaListAdapter.OnEditAnswerClickListenr, QaListAdapter.OnCommentClickListenr,
        QaListAdapter.OnPublishClickListenr,
        Replication.ChangeListener{

    private static final String TAG = "QAAnsweredActivity";
    private ListView mListView = null;
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
                mAdapter = new QaListAdapter(this, application.getQAAnsweredForAdminQuery
                        (application.getDatabase()).toLiveQuery(),
                        this, this, this, this, this, this, this, false, false, true);
                mListView.setAdapter(mAdapter);
            }
        } else{
            //User is nodal Officer
            if(application.getQAAnsweredForNodalQuery(application.getDatabase()) != null) {
                mAdapter = new QaListAdapter(this, application.getQAAnsweredForNodalQuery
                        (application.getDatabase()).toLiveQuery(),
                        this, this, this, this, this, this, this, false, false, true);
                mListView.setAdapter(mAdapter);
            }
        }
    }

    @Override
    public void onUnPublishClick(final Document _qaDoc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
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

                AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
                        R.style.AppCompatAlertDialogStyle);
                builder.setTitle("Un-Publish Question");
                builder.setMessage("Done successfully.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Intent intent = new Intent(QAAnsweredActivity.this, QAAnsweredActivity.class);
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

    @Override
    public void onDeleteClick(final Document _qaDoc) {
        AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
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

                AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
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

    @Override
    public void onEditQuestionClick(final Document qaDoc) {

        String description = (String) qaDoc.getProperty(BuildConfigYouthConnect.QA_DESC);

        LayoutInflater li = LayoutInflater.from(QAAnsweredActivity.this);
        View promptsView = li.inflate(R.layout.alert_dialog_with_input_text, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                QAAnsweredActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInputTitle = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInputQTitle);
        final EditText userInputDesc = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInputQDescription);
        TextView textView1 = (TextView) promptsView.findViewById(R.id.textView1);
        TextView questionDescription = (TextView) promptsView.findViewById(R.id.textViewQuestion);
        questionDescription.setText(description);
        userInputTitle.setHint("Question Title");
        userInputDesc.setHint("Question Description");

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

                                final String mInputedStringForEditQuestionTitle = userInputTitle.getText().toString().trim();
                                final String mInputedStringForEditQuestionDescription = userInputDesc.getText().toString().trim();

                                if (mInputedStringForEditQuestionTitle == null
                                        || mInputedStringForEditQuestionTitle.trim().length() <= 0) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
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

                                    AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
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
    }

    @Override
    public void onAnswerClick(final Document qaDoc) {
        String description = (String) qaDoc.getProperty(BuildConfigYouthConnect.QA_DESC);
        LayoutInflater li1 = LayoutInflater.from(QAAnsweredActivity.this);
        View promptsView1 = li1.inflate(R.layout.alert_dialog_with_input_text, null);

        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                QAAnsweredActivity.this);

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

                                final String mInputedStringForAnswer = userInputTitle1.getText().toString().trim();

                                if (mInputedStringForAnswer == null
                                        || mInputedStringForAnswer.trim().length() <= 0) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
                                            R.style.AppCompatAlertDialogStyle);
                                    builder.setTitle("Answer to Question");
                                    builder.setMessage("Done successfully.");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();

                                            Intent intent = new Intent(QAAnsweredActivity.this, QAAnsweredActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();

                                            return;
                                        }
                                    });
                                    builder.show();
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
    }

    @Override
    public void onEditAnswerClick(final Document _qaDoc) {

        QuestionAndAnswer questionAndAnswer = QAUtil.getQAFromDocument(_qaDoc);
        List<Answer> previousData = questionAndAnswer.getAnswerList();

        String answer = "";
        if(previousData != null && previousData.size() > 0
                && previousData.get(0) != null
                && previousData.get(0).getQadmin_description() != null) {
            answer = previousData.get(0).getQadmin_description();
        } else{
            answer = "Not answered yet.";
        }
        LayoutInflater li1 = LayoutInflater.from(QAAnsweredActivity.this);
        View promptsView1 = li1.inflate(R.layout.alert_dialog_with_input_text, null);

        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                QAAnsweredActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder1.setView(promptsView1);

        final EditText userInputTitle1 = (EditText) promptsView1
                .findViewById(R.id.editTextDialogUserInputQTitle);
        final EditText userInputDesc1 = (EditText) promptsView1
                .findViewById(R.id.editTextDialogUserInputQDescription);
        TextView textView11 = (TextView) promptsView1.findViewById(R.id.textView1);
        TextView questionDescription11 = (TextView) promptsView1.findViewById(R.id.textViewQuestion);
        questionDescription11.setText(answer);

        questionDescription11.setVisibility(View.VISIBLE);
        textView11.setText("Edit Answer.");
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

                                String new_comment = userInputTitle1.getText().toString().trim();

                                if (new_comment == null
                                        || new_comment.trim().length() <= 0) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
                                            R.style.AppCompatAlertDialogStyle);
                                    builder.setTitle("Edit Answer");
                                    builder.setMessage("Edit your answer.");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            return;
                                        }
                                    });
                                    builder.show();
                                } else {

                                    int answer_by_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                            .getInt(Constants.SP_USER_ID, 0);
                                    String answer_by_user_name = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                            .getString(Constants.SP_USER_NAME, "");

                                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    Calendar calendar = GregorianCalendar.getInstance();
                                    String currentTimeString = dateFormatter.format(calendar.getTime());

                                    List<Answer> answers = new ArrayList<Answer>();
                                    Answer answer1 = new Answer(Parcel.obtain());
                                    answer1.setQadmin_description(new_comment);
                                    answer1.setAnswer_by_user_id(answer_by_user_id);
                                    answer1.setAnswer_by_user_name(answer_by_user_name);
                                    answer1.setCreated(currentTimeString);
                                    answers.add(answer1);

                                    try {
                                        // Update the document with more data
                                        Map<String, Object> updatedProperties = new HashMap<String, Object>();
                                        updatedProperties.putAll(_qaDoc.getProperties());
                                        updatedProperties.put(BuildConfigYouthConnect.QA_ANSWER, answers);
                                        _qaDoc.putProperties(updatedProperties);
                                    } catch (CouchbaseLiteException e) {
                                        Log.e(TAG, "Error putting", e);
                                    }

                                    AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
                                            R.style.AppCompatAlertDialogStyle);
                                    builder.setTitle("Edit Answer");
                                    builder.setMessage("Done successfully.");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            return;
                                        }
                                    });
                                    builder.show();
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
    }

    @Override
    public void onCommentClick(final Document _qaDoc) {
        LayoutInflater li1 = LayoutInflater.from(QAAnsweredActivity.this);
        View promptsView1 = li1.inflate(R.layout.alert_dialog_with_input_text, null);

        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                QAAnsweredActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder1.setView(promptsView1);

        final EditText userInputTitle1 = (EditText) promptsView1
                .findViewById(R.id.editTextDialogUserInputQTitle);
        final EditText userInputDesc1 = (EditText) promptsView1
                .findViewById(R.id.editTextDialogUserInputQDescription);
        TextView textView11 = (TextView) promptsView1.findViewById(R.id.textView1);
        TextView questionDescription11 = (TextView) promptsView1.findViewById(R.id.textViewQuestion);
        questionDescription11.setText("");

        questionDescription11.setVisibility(View.VISIBLE);
        textView11.setText("Post your comment.");
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

                                String new_comment = userInputTitle1.getText().toString().trim();

                                if (new_comment == null
                                        || new_comment.trim().length() <= 0) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
                                            R.style.AppCompatAlertDialogStyle);
                                    builder.setTitle("Comment Question");
                                    builder.setMessage("Comment to this question.");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            return;
                                        }
                                    });
                                    builder.show();
                                } else {
                                    QuestionAndAnswer questionAndAnswer = QAUtil.getQAFromDocument(_qaDoc);
                                    List<Comment> previousData = questionAndAnswer.getCommentList();

                                    if(previousData == null) {
                                        previousData = new ArrayList<Comment>();
                                    }

                                    int comment_by_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                            .getInt(Constants.SP_USER_ID, 0);
                                    String comment_by_user_name = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                            .getString(Constants.SP_USER_NAME, "");

                                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    Calendar calendar = GregorianCalendar.getInstance();
                                    String currentTimeString = dateFormatter.format(calendar.getTime());

                                    Comment comment = new Comment(Parcel.obtain());
                                    comment.setComment_description(new_comment);
                                    comment.setComment_by_user_name(comment_by_user_name);
                                    comment.setComment_by_user_id(comment_by_user_id);
                                    String timestamp = currentTimeString;
                                    comment.setCreated(timestamp);
                                    previousData.add(comment);

                                    try {
                                        // Update the document with more data
                                        Map<String, Object> updatedProperties = new HashMap<String, Object>();
                                        updatedProperties.putAll(_qaDoc.getProperties());
                                        updatedProperties.put(BuildConfigYouthConnect.QA_COMMENT, previousData);
                                        _qaDoc.putProperties(updatedProperties);
                                    } catch (CouchbaseLiteException e) {
                                        Log.e(TAG, "Error putting", e);
                                    }

                                    Toast.makeText(QAAnsweredActivity.this, "Posted", Toast.LENGTH_SHORT).show();
                                    /*AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
                                            R.style.AppCompatAlertDialogStyle);
                                    builder.setTitle("Comment on QA");
                                    builder.setMessage("Done successfully.");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            return;
                                        }
                                    });
                                    builder.show();*/
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
    }

    @Override
    public void onPublishClick(final Document _qaDoc) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(QAAnsweredActivity.this,
                R.style.AppCompatAlertDialogStyle);
        builder1.setTitle("Publish Question");
        builder1.setMessage("Are you sure want to publish this question?");
        builder1.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                try {
                    // Update the document with more data
                    Map<String, Object> updatedProperties = new HashMap<String, Object>();
                    updatedProperties.putAll(_qaDoc.getProperties());
                    updatedProperties.put(BuildConfigYouthConnect.QA_IS_PUBLISHED, 1);
                    _qaDoc.putProperties(updatedProperties);
                } catch (CouchbaseLiteException e) {
                    Log.e(TAG, "Error putting", e);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(QAAnsweredActivity.this,
                        R.style.AppCompatAlertDialogStyle);
                builder.setTitle("Publish Question");
                builder.setMessage("Done successfully.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Intent intent = new Intent(QAAnsweredActivity.this, QAPublishedActivity.class);
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
        builder1.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                return;
            }
        });
        builder1.show();
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