package com.luminous.dsys.youthconnect.util;

/**
 * Created by Android Luminous on 2/24/2016.
 */
public class BuildConfigYouthConnect {

    public static final boolean DEBUG = Boolean.parseBoolean("true");
    public static final String APPLICATION_ID = "com.luminous.dsys.youthconnect";
    public static final String BUILD_TYPE = "debug";
    public static final String FLAVOR = "";
    public static final int VERSION_CODE = 110;
    public static final String VERSION_NAME = "1.1.0";
    // Fields from build type: debug

    private static final String LOCAL_SYNC_URL = "http://104.196.34.181";//"http://192.168.1.107:4984";
    private static final String LIVE_SYNC_URL = "http://104.196.34.181:4984";
    private static final String DATABASE_NAME = "youth_connect";
    public static final String SYNC_URL_HTTP = LIVE_SYNC_URL + "/" + DATABASE_NAME;
    public static final String SYNC_URL_HTTPS = "";
    private static final String TAG = "DatabaseUtil";

    public static final String DOC_TYPE_FOR_QA = "qa"; // This is the value for the key "type"
    public static final String TYPE_QA = "questionandanswer";
    public static final String QA_TITLE = "title";
    public static final String QA_DESC = "description";
    public static final String QA_UPDATED_TIMESTAMP = "updated_timestamp";
    public static final String QA_ANSWER = "answer";
    public static final String QA_COMMENT = "comment";
    public static final String QA_ASKED_BY_USER_NAME = "asked_by_user_name";
    public static final String QA_ASKED_BY_USER_ID = "asked_by_user_id";
    public static final String QA_IS_ANSWERED = "is_answered";
    public static final String QA_IS_PUBLISHED = "is_published";
    public static final String QA_IS_UPLOADED = "is_uploaded";
    public static final String QA_IS_READ = "is_qa_read";
    public static final String QA_IS_DELETE = "is_qa_delete";

    public static final String DISTRICTS = "districts";
    public static final String NODAL_OFFICERS = "users";

    public static final String DOC_TYPE_FOR_DOCUMENT = "document"; // This is the value for the key "type"
    public static final String DOC_TITLE = "doc_title";
    public static final String DOC_PURPOSE = "doc_purpose";
    public static final String DOC_FILES = "files";
    public static final String DOC_CREATED = "created";
    public static final String DOC_IS_PUBLISHED = "is_published";
    public static final String DOC_CREATED_BY_USER_NAME = "created_by_user_name";
    public static final String DOC_CREATED_BY_USER_ID = "created_by_user_id";
    public static final String DOC_ASSIGNED_TO_USER_IDS = "doc_assigned_to_user_ids";
    public static final String DOC_IS_UPLOADED = "is_uploaded";
    public static final String DOC_IS_READ = "is_doc_read";
    public static final String DOC_IS_DELETE = "is_doc_delete";
}