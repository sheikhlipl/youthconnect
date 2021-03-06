package com.luminous.dsys.youthconnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.replicator.Replication;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.document.DocumentListAdapter;
import com.luminous.dsys.youthconnect.pojo.AssignedToUSer;
import com.luminous.dsys.youthconnect.pojo.Doc;
import com.luminous.dsys.youthconnect.pojo.FileToUpload;
import com.luminous.dsys.youthconnect.pojo.PendingFileToUpload;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by luminousinfoways on 18/12/15.
 */
public class DocListActivity extends BaseActivity implements
        DocumentListAdapter.OnDeleteClickListener,
        DocumentListAdapter.OnPublishClickListenr,
        DocumentListAdapter.OnUnPublishClickListenr,
        Replication.ChangeListener {

    private ListView mListView = null;
    private DocumentListAdapter mAdapter = null;
    private Menu menu;
    private int nr = 0;
    public static boolean isActive = false;

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    private static final String TAG = "DocListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_list1);

        if (null != toolbar) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

            toolbar.setTitle(getResources().getString(R.string.activity_doc_list_title));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(DocListActivity.this);
                }
            });
        }

        mListView = (ListView) findViewById(R.id.listView);

        try {
            showListInListView();
        } catch (CouchbaseLiteException exception){
            com.couchbase.lite.util.Log.e(TAG, "onCreate()", exception);
        } catch(IOException exception){
            com.couchbase.lite.util.Log.e(TAG, "onCreate()", exception);
        } catch(Exception exception){
            com.couchbase.lite.util.Log.e(TAG, "onCreate()", exception);
        }
    }

    private void showListInListView() throws CouchbaseLiteException, IOException {
        mListView = (ListView) findViewById(R.id.listView);

        int currently_logged_in_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0)
                .getInt(Constants.SP_USER_ID, 0);

        if(currently_logged_in_user_id == 1){
            //User is Admin
            if(application.getDocForAdminQuery(application.getDatabase()) != null) {
                mAdapter = new DocumentListAdapter(this, application.getDocForAdminQuery
                        (application.getDatabase()).toLiveQuery(),
                        this, this, this);
                mListView.setAdapter(mAdapter);
            }
        } else{
            //User is nodal Officer
            if(application.getDocForNodalQuery(application.getDatabase()) != null) {
                mAdapter = new DocumentListAdapter(this, application.getDocForAdminQuery
                        (application.getDatabase()).toLiveQuery(),
                        this, this, this);
                mListView.setAdapter(mAdapter);
            }
        }

        mListView.setAdapter(mAdapter);
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
            Intent intent = new Intent(DocListActivity.this, AskQuestionActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_create_document) {
            Intent intent = new Intent(DocListActivity.this, AttachFileActivity.class);
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
    public void changed(Replication.ChangeEvent event) {
        try {
            showListInListView();
        } catch(CouchbaseLiteException exception){
            com.couchbase.lite.util.Log.e(TAG, "onCreate()", exception);
        } catch(IOException exception){
            com.couchbase.lite.util.Log.e(TAG, "onCreate()", exception);
        } catch(Exception exception){
            com.couchbase.lite.util.Log.e(TAG, "onCreate()", exception);
        }
    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
    }

    @Override
    public void onDeleteClick(final Document doc) {

        AlertDialog.Builder builder = new AlertDialog.Builder(DocListActivity.this,
                R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Document Delete");
        builder.setMessage("Are you sure want to delete this document?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int is_delete = (Integer) doc.getProperty(BuildConfigYouthConnect.DOC_IS_DELETE);
                if(is_delete == 0) {
                    try {
                        // Update the document with more data
                        Map<String, Object> updatedProperties = new HashMap<String, Object>();
                        updatedProperties.putAll(doc.getProperties());
                        updatedProperties.put(BuildConfigYouthConnect.DOC_IS_DELETE, 1);
                        doc.putProperties(updatedProperties);
                    } catch (CouchbaseLiteException e) {
                        com.couchbase.lite.util.Log.e(TAG, "Error putting", e);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(DocListActivity.this,
                            R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("Delete Document");
                    builder.setMessage("Done successfully.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            return;
                        }
                    });
                } else{

                }

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onPublishClick(final Document doc) {
        final int is_publish = (Integer) doc.getProperty(BuildConfigYouthConnect.DOC_IS_PUBLISHED);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(DocListActivity.this,
                R.style.AppCompatAlertDialogStyle);
        builder1.setTitle("Document Publish");
        builder1.setMessage("Are you sure want to publish this document?");
        builder1.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int is_delete = (Integer) doc.getProperty(BuildConfigYouthConnect.DOC_IS_DELETE);

                if (is_delete == 0) {
                    if(is_publish == 0) {
                        //Publis document
                        try {
                            // Update the document with more data
                            Map<String, Object> updatedProperties = new HashMap<String, Object>();
                            updatedProperties.putAll(doc.getProperties());
                            updatedProperties.put(BuildConfigYouthConnect.DOC_IS_PUBLISHED, 1);
                            doc.putProperties(updatedProperties);
                        } catch (CouchbaseLiteException e) {
                            com.couchbase.lite.util.Log.e(TAG, "Error putting", e);
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(DocListActivity.this,
                                R.style.AppCompatAlertDialogStyle);
                        builder.setTitle("Publish Document");
                        builder.setMessage("Done successfully.");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                return;
                            }
                        });
                        builder.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DocListActivity.this,
                                R.style.AppCompatAlertDialogStyle);
                        builder.setTitle("Publish Document");
                        builder.setMessage("This document is already published.");
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

                dialog.dismiss();
            }
        });
        builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder1.show();
    }

    @Override
    public void onUnPublishClick(final Document doc) {
        final int is_publish = (Integer) doc.getProperty(BuildConfigYouthConnect.DOC_IS_PUBLISHED);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(DocListActivity.this,
                R.style.AppCompatAlertDialogStyle);
        builder1.setTitle("Document Un-Publish");
        builder1.setMessage("Are you sure want to un-publish this document?");
        builder1.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                int is_delete = (Integer) doc.getProperty(BuildConfigYouthConnect.DOC_IS_DELETE);

                if (is_delete == 0) {
                    if(is_publish == 1) {
                        //Publis document
                        try {
                            // Update the document with more data
                            Map<String, Object> updatedProperties = new HashMap<String, Object>();
                            updatedProperties.putAll(doc.getProperties());
                            updatedProperties.put(BuildConfigYouthConnect.DOC_IS_PUBLISHED, 0);
                            doc.putProperties(updatedProperties);
                        } catch (CouchbaseLiteException e) {
                            com.couchbase.lite.util.Log.e(TAG, "Error putting", e);
                        }
                        AlertDialog.Builder builder = new AlertDialog.Builder(DocListActivity.this,
                                R.style.AppCompatAlertDialogStyle);
                        builder.setTitle("Un-Publish Document");
                        builder.setMessage("Done successfully.");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                return;
                            }
                        });
                        builder.show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DocListActivity.this,
                                R.style.AppCompatAlertDialogStyle);
                        builder.setTitle("Un-Publish Document");
                        builder.setMessage("This document is already unpublished.");
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

                dialog.dismiss();
            }
        });
        builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder1.show();
    }

    public static Doc getDocFromDocument(Document document){

        try {

            String doc_id = document.getId();

            String title = (String) document.getProperty(BuildConfigYouthConnect.DOC_TITLE);
            String docPurpose = (String) document.getProperty(BuildConfigYouthConnect.DOC_PURPOSE);
            String updated_time_stamp = (String) document.getProperty(BuildConfigYouthConnect.DOC_CREATED);

            String postDate = updated_time_stamp;
            String user_name = (String) document.getProperty(BuildConfigYouthConnect.DOC_CREATED_BY_USER_NAME);
            int created_by_user_id = (Integer) document.getProperty(BuildConfigYouthConnect.DOC_CREATED_BY_USER_ID);
            int is_uploaded = (Integer) document.getProperty(BuildConfigYouthConnect.DOC_IS_UPLOADED);
            int is_published = (Integer) document.getProperty(BuildConfigYouthConnect.DOC_IS_PUBLISHED);

            ArrayList<String> fileList = (ArrayList<String>) document.getProperty(BuildConfigYouthConnect.DOC_FILES);
            ArrayList<LinkedHashMap<String, Object>> assigned_user_ids = (ArrayList<LinkedHashMap<String, Object>>)
                    document.getProperty(BuildConfigYouthConnect.DOC_ASSIGNED_TO_USER_IDS);

            ArrayList<FileToUpload> files = new ArrayList<FileToUpload>();
            if(fileList != null && fileList.size() > 0){
                for(int i = 0; i <fileList.size(); i++){
                    String file_name =  fileList.get(i);
                    if (file_name != null){

                        /*
                        * url to download file :
                        * http://192.168.1.107:4984/youth_connect/{doc_id}/{file_name}
                        * */
                        String download_link = BuildConfigYouthConnect.SYNC_URL_HTTP  + "/" + doc_id + "/" + file_name;
                        FileToUpload fileToUpload = new FileToUpload();
                        fileToUpload.setDownload_link_url(download_link);
                        fileToUpload.setFile_name(file_name);
                        files.add(fileToUpload);
                    }
                }
            }

            ArrayList<AssignedToUSer> ausers = new ArrayList<AssignedToUSer>();
            if(assigned_user_ids != null && assigned_user_ids.size() > 0){
                for(int i = 0; i <assigned_user_ids.size(); i++){
                    LinkedHashMap<String, Object> users = (LinkedHashMap<String, Object>) (assigned_user_ids.get(i));
                    if((users.get("user_name") != null)
                            && (users.get("user_id") != null)){
                        AssignedToUSer answer = new AssignedToUSer();
                        answer.setUser_name((String)users.get("user_name"));
                        answer.setUser_id((Integer)users.get("user_id"));
                        ausers.add(answer);
                    }
                }
            }

            Doc doc = new Doc();
            doc.setDoc_id(doc_id);
            doc.setDoc_title(title);
            doc.setCreated(postDate);
            doc.setDoc_purpose(docPurpose);
            doc.setIs_published(is_published);
            doc.setCreated_by_user_name(user_name);
            doc.setCreated_by_user_id(created_by_user_id);
            doc.setIs_uploaded(is_uploaded);
            doc.setFileToUploads(files);
            doc.setDoc_assigned_to_user_ids(ausers);

            return doc;
        } catch(Exception exception){
            Log.e("QAUtil", "getQAFromDocument()", exception);
        }
        return null;
    }
}