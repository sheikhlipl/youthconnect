package com.luminous.dsys.youthconnect.qa;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.helper.LiveQueryAdapter;
import com.luminous.dsys.youthconnect.pojo.Answer;
import com.luminous.dsys.youthconnect.pojo.Comment;
import com.luminous.dsys.youthconnect.pojo.QuestionAndAnswer;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Android Luminous on 2/13/2016.
 */
public class QaListAdapter extends LiveQueryAdapter {

    private LiveQuery liveQuery;
    private Context context;

    private OnDeleteClickListener onDeleteClickListener;
    private OnUnPublishClickListenr onUnPublishClickListenr;
    private OnPublishClickListenr onPublishClickListenr;
    private OnAnswerClickListenr onAnswerClickListenr;
    private OnEditAnswerClickListenr onEditAnswerClickListenr;
    private OnEditQuestionClickListenr onEditQuestionClickListenr;
    private OnCommentClickListenr onCommentClickListenr;

    private boolean isFromPublished;
    private boolean isFromUnAnswered;
    private boolean isFromAnswered;
    private HashMap<Integer, Boolean> mSelection = new HashMap<Integer, Boolean>();
    private static final String TAG = "QaListAdapter";

    public QaListAdapter(Context context, LiveQuery liveQuery,
                         OnDeleteClickListener onDeleteClickListener,
                         OnUnPublishClickListenr OnUnPublishClickListenr,
                         OnPublishClickListenr onPublishClickListenr,
                         OnAnswerClickListenr onAnswerClickListenr,
                         OnEditAnswerClickListenr onEditAnswerClickListenr,
                         OnEditQuestionClickListenr onEditQuestionClickListenr,
                         OnCommentClickListenr onCommentClickListenr,
                         boolean isFromPublished, boolean isFromUnAnswered, boolean isFromAnswered){
        super(context, liveQuery);
        this.context = context;
        this.liveQuery = liveQuery;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onUnPublishClickListenr = OnUnPublishClickListenr;
        this.isFromAnswered = isFromAnswered;
        this.isFromPublished = isFromPublished;
        this.isFromUnAnswered = isFromUnAnswered;
        this.onPublishClickListenr = onPublishClickListenr;
        this.onAnswerClickListenr = onAnswerClickListenr;
        this.onEditAnswerClickListenr = onEditAnswerClickListenr;
        this.onEditQuestionClickListenr = onEditQuestionClickListenr;
        this.onCommentClickListenr = onCommentClickListenr;
    }

    static class ViewHolder {
        private ImageView imgDownArrow;
        private LinearLayout layout_comment;
        private TextView tvFileTitle;
        private TextView tvQusByUserName;
        private TextView tvUserName;
        private TextView tvQuestionDesc;
        private TextView tvAnswerDescription;
        private LinearLayout layoutComments;
        private EditText editTextPostAnswerOrComment;
        private TextView tvEdit;
        private TextView tvDelete;
        private TextView tvPublish;
        private TextView tvUnpublish;
        private CardView card_view;
        private TextView noComments;
        //private ImageView imgSend;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View vi = convertView;             //trying to reuse a recycled view
        ViewHolder holder = null;

        if (vi == null) {
            //The view is not a recycled one: we have to inflate
            vi = LayoutInflater.from(context).inflate(R.layout.list_row_qa, parent, false);
            holder = new ViewHolder();
            holder.imgDownArrow = (ImageView) vi.findViewById(R.id.imgDownArrow);
            holder.layout_comment = (LinearLayout) vi.findViewById(R.id.layout_comment);
            holder.tvFileTitle = (TextView) vi.findViewById(R.id.tvFileTitle);
            holder.tvQusByUserName = (TextView) vi.findViewById(R.id.tvQusByUserName);
            holder.tvUserName = (TextView) vi.findViewById(R.id.tvUserName);
            holder.tvQuestionDesc = (TextView) vi.findViewById(R.id.tvQuestionDesc);
            holder.tvAnswerDescription = (TextView) vi.findViewById(R.id.tvAnswerDescription);
            holder.layoutComments = (LinearLayout) vi.findViewById(R.id.layoutComments);
            holder.editTextPostAnswerOrComment = (EditText) vi.findViewById(R.id.editTextPostAnswerOrComment);
            holder.tvEdit = (TextView) vi.findViewById(R.id.tvEdit);
            holder.tvDelete = (TextView) vi.findViewById(R.id.tvDelete);
            holder.tvPublish = (TextView) vi.findViewById(R.id.tvPublish);
            holder.tvUnpublish = (TextView) vi.findViewById(R.id.tvUnpublish);
            holder.card_view = (CardView) vi.findViewById(R.id.card_view);
            holder.noComments = (TextView) vi.findViewById(R.id.noComments);
            //holder.imgSend = (ImageView) vi.findViewById(R.id.imgSend);

            vi.setTag(holder);
        } else {
            holder = (ViewHolder) vi.getTag();
        }

        holder.imgDownArrow.setTag(holder);
        holder.layout_comment.setTag(position);
        holder.tvFileTitle.setTag(position);
        holder.tvQusByUserName.setTag(position);
        holder.tvUserName.setTag(position);
        holder.tvQuestionDesc.setTag(position);
        holder.tvAnswerDescription.setTag(position);
        holder.layoutComments.setTag(position);
        holder.editTextPostAnswerOrComment.setTag(position);
        holder.tvEdit.setTag(position);
        holder.tvDelete.setTag(position);
        holder.tvPublish.setTag(position);
        holder.tvUnpublish.setTag(position);
        holder.card_view.setTag(holder);
        holder.noComments.setTag(position);
        //holder.imgSend.setTag(position);

        holder.imgDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder holder = (ViewHolder) v.getTag();
                if(holder.layout_comment.getVisibility() == View.INVISIBLE ||
                        holder.layout_comment.getVisibility() == View.GONE ) {
                    holder.layout_comment.setVisibility(View.VISIBLE);
                } else{
                    holder.layout_comment.setVisibility(View.GONE);
                }
            }
        });

        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            if(holder.layout_comment.getVisibility() == View.VISIBLE) {
                holder.layout_comment.setVisibility(View.GONE);
            }
            }
        });

        final Document doc = (Document)getItem(position);
        String title = (String) doc.getProperty(BuildConfigYouthConnect.QA_TITLE);
        String description = (String) doc.getProperty(BuildConfigYouthConnect.QA_DESC);
        String userName = (String) doc.getProperty(BuildConfigYouthConnect.QA_ASKED_BY_USER_NAME);
        String timeStamp = (String) doc.getProperty(BuildConfigYouthConnect.QA_UPDATED_TIMESTAMP);
        String dateTime = "";
        try{
            dateTime = getTimeToShow(timeStamp);
        } catch(Exception exception){
            Log.e(TAG, "getView()", exception);
        }

        String answer = "";
        QuestionAndAnswer questionAndAnswer = QAUtil.getQAFromDocument(doc);
        if(questionAndAnswer != null) {
            List<Answer> answerList = questionAndAnswer.getAnswerList();
            if (answerList != null && answerList.size() > 0
                    && answerList.get(0) != null
                    && answerList.get(0).getQadmin_description() != null) {
                answer = answerList.get(0).getQadmin_description();
            }
        }
        if(answer != null && answer.trim().length() > 0) {
            holder.tvAnswerDescription.setText(answer);
        }

        List<Comment> commentList = new ArrayList<Comment>();
        if (questionAndAnswer != null) {
            commentList = questionAndAnswer.getCommentList();
        }
        Collections.reverse(commentList);
        if(commentList == null || commentList.size() <= 0){
            holder.noComments.setVisibility(View.VISIBLE);
        } else {
            holder.layoutComments.removeAllViews();
            holder.noComments.setVisibility(View.GONE);
        }
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
                        String _dateTime = getTimeToShow(updated_time_stamp);
                        TextView tvAnswerDateTime = (TextView) itemLayout.findViewById(R.id.tvAnswerDateTime);
                        tvAnswerDateTime.setText(_dateTime);
                    } catch (Exception e) {
                        Log.e("DataAdapter", "onBindViewHolder()", e);
                    }

                    holder.layoutComments.addView(itemLayout);
                }
            }
        }

        holder.tvFileTitle.setText(title);
        holder.tvQusByUserName.setText("Asked by " +userName);
        holder.tvUserName.setText(dateTime);
        holder.tvQuestionDesc.setText(description);

        holder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (Integer) v.getTag();
                Document document = (Document) getItem(pos);
                onDeleteClickListener.onDeleteClick(document);
            }
        });

        holder.tvPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (Integer) v.getTag();
                Document document = (Document) getItem(pos);
                onPublishClickListenr.onPublishClick(document);
            }
        });

        holder.tvUnpublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (Integer) v.getTag();
                Document document = (Document) getItem(pos);
                onUnPublishClickListenr.onUnPublishClick(document);
            }
        });

        if(isFromUnAnswered){
            holder.editTextPostAnswerOrComment.setHint("Post answer here");
            holder.tvEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();
                    Document document = (Document) getItem(pos);
                    onEditQuestionClickListenr.onEditQuestionClick(document);
                }
            });

            holder.editTextPostAnswerOrComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();
                    Document document = (Document) getItem(pos);
                    onAnswerClickListenr.onAnswerClick(document);
                }
            });

        } else {
            holder.editTextPostAnswerOrComment.setHint("Post comment here");
            holder.tvEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();
                    Document document = (Document) getItem(pos);
                    onEditAnswerClickListenr.onEditAnswerClick(document);
                }
            });

            holder.editTextPostAnswerOrComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();
                    Document document = (Document) getItem(pos);
                    onCommentClickListenr.onCommentClick(document);
                }
            });

        }

        int user_type_id = context.getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                .getInt(Constants.SP_USER_TYPE, 0);
        if(isFromPublished){
            if(user_type_id == 1){
                holder.tvDelete.setVisibility(View.VISIBLE);
                holder.tvEdit.setVisibility(View.GONE);
                holder.tvPublish.setVisibility(View.GONE);
                holder.tvUnpublish.setVisibility(View.VISIBLE);
                holder.imgDownArrow.setVisibility(View.VISIBLE);
                holder.editTextPostAnswerOrComment.setVisibility(View.VISIBLE);
            } else{
                holder.tvDelete.setVisibility(View.GONE);
                holder.tvEdit.setVisibility(View.GONE);
                holder.tvPublish.setVisibility(View.GONE);
                holder.tvUnpublish.setVisibility(View.GONE);
                holder.imgDownArrow.setVisibility(View.GONE);
                holder.editTextPostAnswerOrComment.setVisibility(View.VISIBLE);
            }
        }

        if(isFromAnswered){
            if(user_type_id == 1){
                holder.tvDelete.setVisibility(View.VISIBLE);
                holder.tvEdit.setVisibility(View.VISIBLE);
                holder.tvPublish.setVisibility(View.VISIBLE);
                holder.tvUnpublish.setVisibility(View.GONE);
                holder.imgDownArrow.setVisibility(View.VISIBLE);
                holder.editTextPostAnswerOrComment.setVisibility(View.VISIBLE);
            } else{
                holder.tvDelete.setVisibility(View.GONE);
                holder.tvEdit.setVisibility(View.GONE);
                holder.tvPublish.setVisibility(View.GONE);
                holder.tvUnpublish.setVisibility(View.GONE);
                holder.imgDownArrow.setVisibility(View.GONE);
                holder.editTextPostAnswerOrComment.setVisibility(View.VISIBLE);
            }
        }

        if(isFromUnAnswered){
            if(user_type_id == 1){
                holder.tvDelete.setVisibility(View.VISIBLE);
                holder.tvEdit.setVisibility(View.VISIBLE);
                holder.tvPublish.setVisibility(View.GONE);
                holder.tvUnpublish.setVisibility(View.GONE);
                holder.imgDownArrow.setVisibility(View.VISIBLE);
                holder.editTextPostAnswerOrComment.setVisibility(View.VISIBLE);
            } else{
                holder.tvDelete.setVisibility(View.VISIBLE);
                holder.tvEdit.setVisibility(View.VISIBLE);
                holder.tvPublish.setVisibility(View.GONE);
                holder.tvUnpublish.setVisibility(View.GONE);
                holder.imgDownArrow.setVisibility(View.VISIBLE);
                holder.editTextPostAnswerOrComment.setVisibility(View.GONE);
            }
        }

        return vi;
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
                        String finalStr = "at " + op[0] + ":" + op[1];
                        return finalStr;
                    }

                    return "";
                } else if(date != null &&
                        date.length() > 0){
                    String[] crdate = date.split("-");
                    String mon = crdate[1];
                    String dd = crdate[2];
                    String monName = Util.getMonInWord(mon);
                    String to_return = "on "+ monName + " " + dd;
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

    @Override
    public int getCount() {
        return super.getCount();
    }

    public interface OnDeleteClickListener{
        public void onDeleteClick(Document student);
    }

    public interface OnUnPublishClickListenr {
        public void onUnPublishClick(Document student);
    }

    public interface OnPublishClickListenr {
        public void onPublishClick(Document student);
    }

    public interface OnAnswerClickListenr {
        public void onAnswerClick(Document student);
    }

    public interface OnEditAnswerClickListenr {
        public void onEditAnswerClick(Document student);
    }

    public interface OnEditQuestionClickListenr {
        public void onEditQuestionClick(Document student);
    }

    public interface OnCommentClickListenr {
        public void onCommentClick(Document student);
    }
}