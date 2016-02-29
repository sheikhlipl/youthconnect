package com.luminous.dsys.youthconnect.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.database.DBHelper;
import com.luminous.dsys.youthconnect.home.DashboardFragment;
import com.luminous.dsys.youthconnect.home.HomeRecyclerAdapter;
import com.luminous.dsys.youthconnect.login.LoginActivity;
import com.luminous.dsys.youthconnect.pojo.FileToUpload;
import com.luminous.dsys.youthconnect.pojo.PendingFileToUpload;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DashboardFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = LayoutInflater.from(this).inflate(R.layout.nav_header_main, null);
        navigationView.addHeaderView(header);
        String user_full_name = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                .getString(Constants.SP_USER_NAME, "");
        String user_email = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                .getString(Constants.SP_USER_EMAIL, "");
        TextView tvUserFullName = (TextView) header.findViewById(R.id.tvUserFullName);
        TextView tvUserEmail = (TextView) header.findViewById(R.id.tvUserEmail);
        tvUserFullName.setText(user_full_name);
        tvUserEmail.setText(user_email);

        initViewPagerSetUp();
    }

    @Override
    public void onDashboardFragmentInteraction(DashboardFragment dashboardFragment) {

    }

    private void initViewPagerSetUp(){
        HomeRecyclerAdapter recyclerAdapter = new HomeRecyclerAdapter(getSupportFragmentManager(),
                MainActivity.this);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(recyclerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabView);
        tabLayout.addTab(tabLayout.newTab().setText(Constants.TAB_TITLE_HOME_SHOWCASE));
        tabLayout.addTab(tabLayout.newTab().setText(Constants.TAB_TITLE_HOME_DASHBOARD));
        //tabLayout.addTab(tabLayout.newTab().setText(Constants.TAB_TITLE_HOME_REPORT));
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        switch (tab.getPosition()) {

                            case 0:

                                break;
                            case 1:

                                break;
                            default:
                                break;
                        }
                    }
                });

        viewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
            Intent intent = new Intent(MainActivity.this, AskQuestionActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_create_document) {
            Intent intent = new Intent(MainActivity.this, AttachFileActivity.class);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        item.setChecked(true);

        if (id == R.id.qa_forum) {
            // Handle the camera action
            Intent intent = new Intent(this, QAPublishedActivity.class);
            startActivity(intent);
        } else if (id == R.id.qa_pending) {
            Intent intent = new Intent(this, QAPendingActivity.class);
            startActivity(intent);
        } else if (id == R.id.qa_answered) {
            Intent intent = new Intent(this, QAAnsweredActivity.class);
            startActivity(intent);
        } else if (id == R.id.documentation) {
            Intent intent = new Intent(this, DocListActivity.class);
            startActivity(intent);
        }  else if (id == R.id.change_password) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.logout) {
            showAlertDialog("Are you sure want to logout?", "Logout", "Yes", "No");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * To Show Material Alert Dialog
     *
     * @param message
     * @param title
     * */
    private void showAlertDialog(String message, String title, String positiveButtonText,
                                 String negativeButtonText){

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DBHelper dbHelper = new DBHelper(MainActivity.this);
                dbHelper.deleteAllDataInDatabase();
                dbHelper.close();

                getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 2).edit().putInt(Constants.SP_LOGIN_STATUS, 0).commit();
                getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 2).edit().putInt(Constants.SP_USER_ID, 0).commit();
                getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 2).edit().putString(Constants.SP_USER_API_KEY, null).commit();
                getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 2).edit().putString(Constants.SP_USER_DESG_ID, null).commit();
                getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 2).edit().putString(Constants.SP_USER_NAME, null).commit();
                getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 2).edit().putString(Constants.SP_USER_EMAIL, null).commit();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}
