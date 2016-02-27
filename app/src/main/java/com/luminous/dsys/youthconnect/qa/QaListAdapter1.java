package com.luminous.dsys.youthconnect.qa;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcel;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.activity.BaseActivity;
import com.luminous.dsys.youthconnect.activity.QAAnsweredActivity;
import com.luminous.dsys.youthconnect.helper.LiveQueryAdapter;
import com.luminous.dsys.youthconnect.pojo.Answer;
import com.luminous.dsys.youthconnect.pojo.Comment;
import com.luminous.dsys.youthconnect.pojo.Question;
import com.luminous.dsys.youthconnect.pojo.QuestionAndAnswer;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Android Luminous on 2/13/2016.
 */
public class QaListAdapter1 extends LiveQueryAdapter {

    private LiveQuery liveQuery;
    private Context context;
    private OnDeleteClickListener onDeleteClickListener;
    private OnUpdateClickListenr onUpdateClickListenr;
    private boolean isFromPublished;
    private boolean isFromUnAnswered;
    private boolean isFromAnswered;
    private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();
    private static final String TAG = "DocList";

    public QaListAdapter1(Context context, LiveQuery liveQuery,
                          OnDeleteClickListener onDeleteClickListener,
                          OnUpdateClickListenr onUpdateClickListenr,
                          boolean isFromPublished, boolean isFromUnAnswered, boolean isFromAnswered){
        super(context, liveQuery);
        this.context = context;
        this.liveQuery = liveQuery;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onUpdateClickListenr = onUpdateClickListenr;
        this.isFromAnswered = isFromAnswered;
        this.isFromPublished = isFromPublished;
        this.isFromUnAnswered = isFromUnAnswered;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_row_bk, null);
            convertView.setTag(position);
        }

        final Document task = (Document) getItem(position);

        int isAnswered = (Integer) task.getProperty(BuildConfigYouthConnect.QA_IS_ANSWERED);
        int isPublished = (Integer) task.getProperty(BuildConfigYouthConnect.QA_IS_PUBLISHED);

        if(isFromUnAnswered){
            if(isAnswered == 0){
                setData(convertView, task);
            }
        } else if(isFromAnswered){
            if(isAnswered == 1){
                setData(convertView, task);
            }
        } else if(isFromPublished){
            if(isPublished == 1){
                setData(convertView, task);
            }
        }

        return convertView;
    }

    private void setData(View convertView, Document task){
        TextView tvQusByUserName = (TextView) convertView.findViewById(R.id.tvQusByUserName);
        tvQusByUserName.setText((String) task.getProperty(BuildConfigYouthConnect.QA_ASKED_BY_USER_NAME));

        TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
        try {
            tvTime.setText(getTimeToShow((String) task.getProperty(BuildConfigYouthConnect.QA_UPDATED_TIMESTAMP)));
        } catch(Exception exception){
            Log.e("QAListAdapter", "error()", exception);
        }

        TextView tvUserNameShortCut = (TextView) convertView.findViewById(R.id.tvUserNameShortCut);
        String user_name = (String) task.getProperty(BuildConfigYouthConnect.QA_ASKED_BY_USER_NAME);
        String first_two_characters_of_name = user_name.substring(0, 1).toUpperCase();
        tvUserNameShortCut.setText(first_two_characters_of_name);

        TextView tvTextTitle = (TextView) convertView.findViewById(R.id.tvFileTitle);
        tvTextTitle.setText((String) task.getProperty(BuildConfigYouthConnect.QA_TITLE));

        int is_answered = (Integer) task.getProperty(BuildConfigYouthConnect.QA_IS_ANSWERED);
        RelativeLayout layoutAnswerAndComment = (RelativeLayout) convertView.findViewById(R.id.layoutAnswerAndComment);
        if(is_answered == 1){
            layoutAnswerAndComment.setVisibility(View.VISIBLE);

            LinearLayout layoutAnswerList = (LinearLayout) convertView.findViewById(R.id.layoutAnswerList);
            layoutAnswerList.removeAllViews();
            QuestionAndAnswer questionAndAnswer = QAAnsweredActivity.getQAFromDocument(task);
            if(questionAndAnswer != null) {
                List<Answer> answerList = questionAndAnswer.getAnswerList();
                if (answerList != null && answerList.size() > 0) {
                    for (int i = 0; i < answerList.size(); i++) {
                        if (answerList != null && answerList.size() > 0) {
                            RelativeLayout itemLayout = (RelativeLayout) LayoutInflater.from(context)
                                    .inflate(R.layout.answer_list_item, null);
                            TextView tvAnsContent = (TextView) itemLayout.findViewById(R.id.tvAnswerContent);
                            tvAnsContent.setText(answerList.get(i).getQadmin_description());

                            layoutAnswerList.addView(itemLayout);
                        }
                    }
                    //btnPostAnswer.setText("Edit your answer");
                }

                LinearLayout layoutCommentList = (LinearLayout)
                        convertView.findViewById(R.id.layoutCommentLsit);
                layoutCommentList.removeAllViews();
                List<Comment> commentList = new ArrayList<Comment>();
                if (questionAndAnswer != null) {
                    commentList = questionAndAnswer.getCommentList();
                }

                Collections.reverse(commentList);

                if (commentList != null && commentList.size() > 0) {
                    for (int i = 0; i < commentList.size(); i++) {
                        if (commentList != null && commentList.size() > 0) {
                            RelativeLayout itemLayout = (RelativeLayout) LayoutInflater.from(context)
                                    .inflate(R.layout.comment_list_item, null);
                            TextView tvCommentDesc = (TextView) itemLayout.findViewById(R.id.tvCommentDesc);
                            tvCommentDesc.setText(commentList.get(i).getComment_description().trim());

                            TextView tvAnswerBy = (TextView) itemLayout.findViewById(R.id.tvAnswerBy);
                            tvAnswerBy.setText(commentList.get(i).getComment_by_user_name());

                            try {
                                String updated_time_stamp = (String) commentList.get(i).getCreated();
                                String dateTime = getTimeToShow(updated_time_stamp);
                                TextView tvAnswerDateTime = (TextView) itemLayout.findViewById(R.id.tvAnswerDateTime);
                                tvAnswerDateTime.setText(dateTime);
                            } catch (Exception e) {
                                Log.e("DataAdapter", "onBindViewHolder()", e);
                            }

                            layoutCommentList.addView(itemLayout);
                        }
                    }
                }
            }

        } else{
            layoutAnswerAndComment.setVisibility(View.GONE);
        }
    }

    private String getTimeToShow(String time) throws Exception {
        // 2016-12-23T12:33:23.328Z
        String currentDate = Util.getCurrentDateTime();
        if(time != null && time.trim().length() > 0 && time.trim().contains("T")){
            String[] arr = time.trim().split("T");
            for(int i = 0; i < arr.length; i++){
                String date = arr[0];
                if(date != null &&
                        date.length() > 0 &&
                        currentDate != null &&
                        currentDate.trim().length() > 0 &&
                        date.trim().equalsIgnoreCase(currentDate.trim())){

                    String t = arr[1];
                    if(t.contains(":")){
                        String[] op = t.split(":");
                        String finalStr = op[0] + ":" + op[1];
                        return finalStr;
                    }

                    return "";
                } else if(date != null &&
                        date.length() > 0){
                    String[] crdate = date.split("-");
                    String mon = crdate[1];
                    String dd = crdate[2];
                    String monName = Util.getMonInWord(mon);
                    String to_return = monName + " " + dd;
                    return to_return;
                }
            }
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    public interface OnDeleteClickListener{
        public void onDeleteClick(Document student);
    }

    public interface OnUpdateClickListenr{
        public void onUpdateClick(Document student);
    }

    public void setNewSelection(int position, boolean value) {
        mSelection.put(position, value);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        Boolean result = mSelection.get(position);
        return result == null ? false : result;
    }

    public Set<Integer> getCurrentCheckedPosition() {
        return mSelection.keySet();
    }

    public void removeSelection(int position) {
        mSelection.remove(position);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        mSelection = new HashMap<Integer, Boolean>();
        notifyDataSetChanged();
    }

    public void deleteQA(){
        Iterator it = mSelection.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
            int position = (Integer) pair.getKey();

            Document doc = (Document) getItem(position);
            deleteDoc(doc);
        }
    }

    private void deleteDoc(final Document _qaDoc){
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
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
                    com.couchbase.lite.util.Log.e(TAG, "Error putting", e);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context,
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

    public void unPublishQA(){
        Iterator it = mSelection.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
            int position = (Integer) pair.getKey();

            Document doc = (Document) getItem(position);
            unPublishQAndA(doc);
        }
    }

    private void unPublishQAndA(final Document _qaDoc){
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
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
                    com.couchbase.lite.util.Log.e(TAG, "Error putting", e);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context,
                        R.style.AppCompatAlertDialogStyle);
                builder.setTitle("Un-Publish Question");
                builder.setMessage("Done successfully.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Intent intent = new Intent(context, QAAnsweredActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                        if (context instanceof BaseActivity) {
                            ((BaseActivity)context).finish();
                        }

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

    public void publishQA(){
        Iterator it = mSelection.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
            int position = (Integer) pair.getKey();

            Document doc = (Document) getItem(position);
            publishQAndA(doc);
        }
    }

    private void publishQAndA(final Document _qaDoc){
        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Publish Question");
        builder.setMessage("Are you sure want to publish this question?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
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
                    com.couchbase.lite.util.Log.e(TAG, "Error putting", e);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context,
                        R.style.AppCompatAlertDialogStyle);
                builder.setTitle("Publish Question");
                builder.setMessage("Done successfully.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Intent intent = new Intent(context, QAAnsweredActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                        if (context instanceof BaseActivity) {
                            ((BaseActivity)context).finish();
                        }

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

    public void postComment(){
        Iterator it = mSelection.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
            int position = (Integer) pair.getKey();

            Document doc = (Document) getItem(position);
            comment(doc);
        }
    }

    private void comment(final Document _qaDoc){

        // get prompts.xml view
        LayoutInflater li1 = LayoutInflater.from(context);
        View promptsView1 = li1.inflate(R.layout.alert_dialog_with_input_text, null);

        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                context);

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

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context,
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
                                    QuestionAndAnswer questionAndAnswer = getQAFromDocument(_qaDoc);
                                    List<Comment> previousData = questionAndAnswer.getCommentList();

                                    if(previousData == null) {
                                        previousData = new ArrayList<Comment>();
                                    }

                                    int comment_by_user_id = context.getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                            .getInt(Constants.SP_USER_ID, 0);
                                    String comment_by_user_name = context.getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
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
                                        com.couchbase.lite.util.Log.e(TAG, "Error putting", e);
                                    }

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context,
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

    public void editQuestion(){
        Iterator it = mSelection.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
            int position = (Integer) pair.getKey();

            Document doc = (Document) getItem(position);
            editQuestionForQA(doc);
        }
    }

    public void postAnswer(){
        Iterator it = mSelection.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
            int position = (Integer) pair.getKey();

            Document doc = (Document) getItem(position);
            postAnswerForQA(doc);
        }
    }

    private void editQuestionForQA(final Document qaDoc){
        String description = (String) qaDoc.getProperty(BuildConfigYouthConnect.QA_DESC);
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.alert_dialog_with_input_text, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

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

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context,
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

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context,
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
                                        com.couchbase.lite.util.Log.e(TAG, "Error putting", e);
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

    private void postAnswerForQA(final Document qaDoc){
        String description = (String) qaDoc.getProperty(BuildConfigYouthConnect.QA_DESC);
// get prompts.xml view
        LayoutInflater li1 = LayoutInflater.from(context);
        View promptsView1 = li1.inflate(R.layout.alert_dialog_with_input_text, null);

        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                context);

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

                                    AlertDialog.Builder builder = new AlertDialog.Builder(context,
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

                                    int currently_logged_in_user_id = context.getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                            .getInt(Constants.SP_USER_ID, 0);
                                    String currently_logged_in_user_name = context.getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
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
                                        com.couchbase.lite.util.Log.e(TAG, "Error putting", e);
                                    }
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context,
                                            R.style.AppCompatAlertDialogStyle);
                                    builder.setTitle("Answer to Question");
                                    builder.setMessage("Done successfully.");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();

                                            Intent intent = new Intent(context, QAAnsweredActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            context.startActivity(intent);
                                            if(context instanceof BaseActivity) {
                                                ((BaseActivity)context).finish();
                                            }

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
}