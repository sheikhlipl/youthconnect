package com.luminous.dsys.youthconnect.qa;

import android.os.Parcel;

import com.couchbase.lite.Document;
import com.luminous.dsys.youthconnect.pojo.Answer;
import com.luminous.dsys.youthconnect.pojo.Comment;
import com.luminous.dsys.youthconnect.pojo.Question;
import com.luminous.dsys.youthconnect.pojo.QuestionAndAnswer;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Android Luminous on 2/28/2016.
 */
public class QAUtil {

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

            ArrayList<Object> answerJson = (ArrayList<Object>)
                    document.getProperty(BuildConfigYouthConnect.QA_ANSWER);
            ArrayList<Object> commentJson = (ArrayList<Object>)
                    document.getProperty(BuildConfigYouthConnect.QA_COMMENT);

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            if(answerJson != null && answerJson.size() > 0){
                for(int i = 0; i <answerJson.size(); i++){

                    if(answerJson.get(i) != null && answerJson.get(i) instanceof Answer){
                        answerArrayList.add((Answer)answerJson.get(i));
                    } else {
                        LinkedHashMap<String, String> answers = (LinkedHashMap<String, String>) (answerJson.get(i));
                        if ((answers.get("qadmin_description") != null)
                                && (answers.get("answer_by_user_name") != null)
                                && (answers.get("answer_by_user_name") != null)) {
                            Answer answer = new Answer(Parcel.obtain());
                            answer.setQadmin_description(answers.get("qadmin_description"));
                            answer.setAnswer_by_user_name(answers.get("answer_by_user_name"));
                            //answer.setCreated(answers.get("created"));
                            answerArrayList.add(answer);
                        }
                    }
                }
            }

            ArrayList<Comment> commentArrayList = new ArrayList<Comment>();
            if(commentJson != null && commentJson.size() > 0){
                for(int i = 0; i <commentJson.size(); i++) {
                    if (commentJson.get(i) != null && commentJson.get(i) instanceof Comment) {
                        commentArrayList.add((Comment) commentJson.get(i));
                    } else {
                        LinkedHashMap<String, String> comments = (LinkedHashMap<String, String>) (commentJson.get(i));
                        if ((comments.get("comment_description") != null)
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
