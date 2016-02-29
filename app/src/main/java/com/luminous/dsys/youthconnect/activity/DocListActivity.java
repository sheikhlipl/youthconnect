package com.luminous.dsys.youthconnect.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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
import android.widget.RelativeLayout;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.replicator.Replication;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.document.DocumentListAdapter1;
import com.luminous.dsys.youthconnect.pojo.AssignedToUSer;
import com.luminous.dsys.youthconnect.pojo.Doc;
import com.luminous.dsys.youthconnect.pojo.FileToUpload;
import com.luminous.dsys.youthconnect.pojo.PendingFileToUpload;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by luminousinfoways on 18/12/15.
 */
public class DocListActivity extends BaseActivity implements
        DocumentListAdapter1.OnDeleteClickListener,
        DocumentListAdapter1.OnUpdateClickListenr,
        Replication.ChangeListener {

    private ListView mListView = null;
    private DocumentListAdapter1 mAdapter = null;
    private Menu menu;
    private int nr = 0;

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
                mAdapter = new DocumentListAdapter1(this, application.getDocForAdminQuery
                        (application.getDatabase()).toLiveQuery(),
                        this, this);
                mListView.setAdapter(mAdapter);
            }
        } else{
            //User is nodal Officer
            if(application.getDocForNodalQuery(application.getDatabase()) != null) {
                mAdapter = new DocumentListAdapter1(this, application.getDocForNodalQuery
                        (application.getDatabase()).toLiveQuery(),
                        this, this);
                mListView.setAdapter(mAdapter);
            }
        }

        mListView.setAdapter(mAdapter);

        int user_type_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1).getInt(Constants.SP_USER_TYPE, 0);
        if(user_type_id == 1) {

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
                    inflater.inflate(R.menu.contextual_menu, menu);
                    DocListActivity.this.menu = menu;

                    int user_type_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1).getInt(Constants.SP_USER_TYPE, 0);
                    if (user_type_id == 2) {
                        menu.getItem(0).setVisible(false);
                        menu.getItem(1).setVisible(false);
                        menu.getItem(2).setVisible(false);
                    }

                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    // TODO Auto-generated method stub
                    switch (item.getItemId()) {

                        case R.id.item_delete:
                            mAdapter.deleteDocuments();
                            mode.finish();
                            break;

                        case R.id.item_publish_unpublish:
                            mAdapter.publishDocuments();
                            mode.finish();
                            break;

                        case R.id.item_send_to_nodal:
                            mAdapter.sendToNodalOfficers();
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
        }

        /*mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            View v = mAdapter.getView(position, view, parent);
            RelativeLayout layoutFileList = (RelativeLayout) v.findViewById(R.id.layoutFileList);
            if(layoutFileList.getVisibility() == View.VISIBLE){
                layoutFileList.setVisibility(View.GONE);
            } else{
                layoutFileList.setVisibility(View.VISIBLE);
            }
            }
        });*/
    }

    private void changeAndInflate(){
        /*if(nr > 1) {
            menu.getItem(0).setVisible(false);
        } else{
            menu.getItem(0).setVisible(true);
        }*/
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
    public void onUpdateClick(Document student) {

    }

    @Override
    public void onDeleteClick(Document student) {

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