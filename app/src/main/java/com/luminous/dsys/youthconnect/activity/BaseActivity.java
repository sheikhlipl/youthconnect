package com.luminous.dsys.youthconnect.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.luminous.dsys.youthconnect.R;

import java.util.Observable;
import java.util.Observer;

public class BaseActivity extends AppCompatActivity {

    Toolbar toolbar;
    public Application application;
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (Application) getApplication();

        application.setDatabase();

        application.getOnSyncProgressChangeObservable().addObserver(new Observer() {
            @Override
            public void update(final Observable observable, final Object data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Application.SyncProgress progress = (Application.SyncProgress) data;
                        Log.d(TAG, "Sync progress changed.  Completed: %d Total: %d Status: %s", progress.completedCount, progress.totalCount, progress.status);

                        if (progress.status == Replication.ReplicationStatus.REPLICATION_ACTIVE) {
                            Log.d(TAG, "Turn on progress spinny");
                            setProgressBarIndeterminateVisibility(true);
                        } else {
                            Log.d(TAG, "Turn off progress spinny");
                            setProgressBarIndeterminateVisibility(false);
                        }
                    }
                });
            }
        });

        application.getOnSyncUnauthorizedObservable().addObserver(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d(Application.TAG, "OnSyncUnauthorizedObservable called, show toast");

                        // clear the saved user id, since our session is no longer valid
                        // and we want to show the login button

                        String msg = "Sync unable to continue due to invalid session/login";
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                    }
                });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        application.startReplicationSync();
    }

    @Override
    protected void onPause() {
        super.onPause();
        application.stopSync();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setupToolbar();
    }

    protected void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }
}
