package com.luminous.dsys.youthconnect;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.luminous.dsys.youthconnect.helper.BaseActivity;
import com.luminous.dsys.youthconnect.home.HomeRecyclerAdapter;
import com.luminous.dsys.youthconnect.util.Constants;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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
            return true;
        } else if (id == R.id.action_create_document) {
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
        } else if (id == R.id.qa_pending) {

        } else if (id == R.id.qa_answered) {

        } else if (id == R.id.documentation) {

        } else if (id == R.id.feedback) {

        } else if (id == R.id.change_password) {

        } else if (id == R.id.logout) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
