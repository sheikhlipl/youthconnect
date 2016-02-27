package com.luminous.dsys.youthconnect.qa;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.activity.QAAnsweredActivity;
import com.luminous.dsys.youthconnect.helper.LiveQueryAdapter;
import com.luminous.dsys.youthconnect.pojo.Answer;
import com.luminous.dsys.youthconnect.pojo.Comment;
import com.luminous.dsys.youthconnect.pojo.QuestionAndAnswer;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Android Luminous on 2/13/2016.
 */
public class QaListAdapter extends LiveQueryAdapter {

    private LiveQuery liveQuery;
    private Context context;
    private OnDeleteClickListener onDeleteClickListener;
    private OnUpdateClickListenr onUpdateClickListenr;
    private boolean isFromPublished;
    private boolean isFromUnAnswered;
    private boolean isFromAnswered;

    public QaListAdapter(Context context, LiveQuery liveQuery,
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
            convertView = LayoutInflater.from(context).inflate(R.layout.list_row, null);
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
}
