package com.luminous.dsys.youthconnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.database.DBHelper;
import com.luminous.dsys.youthconnect.document.NodalOfficerListViewAdapter;
import com.luminous.dsys.youthconnect.pojo.Answer;
import com.luminous.dsys.youthconnect.pojo.AssignedToUSer;
import com.luminous.dsys.youthconnect.pojo.Comment;
import com.luminous.dsys.youthconnect.pojo.Doc;
import com.luminous.dsys.youthconnect.pojo.NodalUser;
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
public class NodalOfficerActivity extends BaseActivity
        implements AdapterView.OnItemClickListener{

    private static final String TAG = "NodalOfficerActivity";

    private TextView tvEmptyView;
    //private RecyclerView listView;
    private ListView listView;
    private NodalOfficerListViewAdapter mAdapter;

    private List<NodalUser> nodalOfficers;
    public static List<NodalUser> selectedNodalOfficers;

    private SearchView search;
    protected Handler handler;

    private Doc document = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nodal_officer);

        if (null != toolbar) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

            toolbar.setTitle(getResources().getString(R.string.activity_ask_a_question_title));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(NodalOfficerActivity.this);
                }
            });

            // Inflate a menu to be displayed in the toolbar
            //toolbar.inflateMenu(R.menu.settings);
        }

        search = (SearchView) findViewById(R.id.searchView1);
        search.setQueryHint("SearchView");

        //*** setOnQueryTextFocusChangeListener ***
        search.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

            }
        });

        //*** setOnQueryTextListener ***
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                setNodalOfficersList(newText);
                return false;
            }
        });

        tvEmptyView = (TextView) findViewById(R.id.tvNoRecordFoundText);
        listView = (ListView) findViewById(R.id.distList);
        listView.setOnItemClickListener(this);
        if(nodalOfficers == null) {
            nodalOfficers = new ArrayList<NodalUser>();
        }

        try {
            DBHelper dbHelper = new DBHelper(NodalOfficerActivity.this);
            List<NodalUser> nodalOfficerUsers = dbHelper.getAllNodalUsers();
            dbHelper.close();
            if (nodalOfficerUsers != null
                    && nodalOfficerUsers.size() > 0) {
                nodalOfficers.clear();
                nodalOfficers.addAll(nodalOfficerUsers);
            }
        } catch(Exception exception){
            Log.e(TAG, "error", exception);
        }
        handler = new Handler();

        // create an Object for Adapter
        mAdapter = new NodalOfficerListViewAdapter(nodalOfficers, NodalOfficerActivity.this);

        // set the adapter object to the Recyclerview
        listView.setAdapter(mAdapter);
        //  mAdapter.notifyDataSetChanged();

        if (nodalOfficers.isEmpty()) {
            listView.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }

        mAdapter.notifyDataSetChanged();
        selectedNodalOfficers = new ArrayList<NodalUser>();

        if(getIntent().getExtras() != null){
            document = (Doc) getIntent().getExtras().getSerializable(Constants.INTENT_KEY_DOCUMENT);
        }
    }

    public void onItemClickOfListView(int position, boolean isChecked){
        if(selectedNodalOfficers == null){
            selectedNodalOfficers = new ArrayList<NodalUser>();
        }

        NodalUser nodalUser = nodalOfficers.get(position);
        if(isChecked) {
            selectedNodalOfficers.add(nodalUser);
        } else{
            for(int i = 0; i < selectedNodalOfficers.size(); i++){
                NodalUser user = selectedNodalOfficers.get(i);
                if(user.getUser_id() == nodalUser.getUser_id()){
                    selectedNodalOfficers.remove(i);
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

    }

    private void setNodalOfficersList(String filterText){

        final List<NodalUser> nodalOfficerList = new ArrayList<NodalUser>();
        if(filterText != null && filterText.trim().length() > 0){
            for(int i = 0; i < nodalOfficers.size(); i++){
                String m_district = nodalOfficers.get(i).getFull_name();
                if(m_district != null && filterText != null &&
                        m_district.toLowerCase().contains(filterText.toLowerCase())){
                    nodalOfficerList.add(nodalOfficers.get(i));
                }
            }
        } else{
            nodalOfficerList.addAll(nodalOfficers);
        }

        // create an Object for Adapter
        mAdapter = new NodalOfficerListViewAdapter(nodalOfficerList, NodalOfficerActivity.this);

        // set the adapter object to the Recyclerview
        listView.setAdapter(mAdapter);
        //  mAdapter.notifyDataSetChanged();


        if (nodalOfficerList.isEmpty()) {
            listView.setVisibility(View.GONE);
            tvEmptyView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            tvEmptyView.setVisibility(View.GONE);
        }

        mAdapter.notifyDataSetChanged();
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
        getMenuInflater().inflate(R.menu.nodal_officer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == android.R.id.home){
            onBackPressed();
        } else if(id == R.id.action_send) {

            if(selectedNodalOfficers != null && selectedNodalOfficers.size() > 0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                builder.setMessage("Are you sure want to send document to selected nodal officers?");
                builder.setTitle("Document assign");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendDocumnetToNodalOfficers();
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
            } else{
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                builder.setMessage("Select atlest one officer to assign this document.");
                builder.setTitle("Document assign");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendDocumnetToNodalOfficers(){
        if(selectedNodalOfficers != null && selectedNodalOfficers.size() > 0){
            List<AssignedToUSer> assignedToUSers = document.getDoc_assigned_to_user_ids();
            if(assignedToUSers == null){
                assignedToUSers = new ArrayList<AssignedToUSer>();
            }
            for(int i = 0; i < selectedNodalOfficers.size(); i++){
                NodalUser user = selectedNodalOfficers.get(i);
                String user_name = user.getFull_name();
                int user_id = user.getUser_id();

                AssignedToUSer assignedToUSer = new AssignedToUSer();
                assignedToUSer.setUser_name(user_name);
                assignedToUSer.setUser_id(user_id);

                assignedToUSers.add(assignedToUSer);
            }

            if(document != null){
                String doc_id = document.getDoc_id();
                try {

                    Document document = application.getDatabase().getDocument(doc_id);
                    try {
                        // Update the document with more data

                        Map<String, Object> updatedProperties = new HashMap<String, Object>();
                        updatedProperties.putAll(document.getProperties());
                        updatedProperties.put(BuildConfigYouthConnect.DOC_ASSIGNED_TO_USER_IDS, assignedToUSers);
                        // Save to the Couchbase local Couchbase Lite DB
                        document.putProperties(updatedProperties);
                    } catch (CouchbaseLiteException e) {
                        com.couchbase.lite.util.Log.e("DocUtil", "Error putting", e);
                    } catch(Exception exception){
                        Log.e("DocUtil", "updateDocument()", exception);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("Doc assignment");
                    builder.setMessage("Done.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    builder.show();
                } catch(Exception exception){
                    Log.e(TAG, "OnClick()", exception);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
    }
}