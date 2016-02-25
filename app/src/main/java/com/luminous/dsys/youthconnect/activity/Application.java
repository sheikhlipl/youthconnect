/**
 * Created by Pasin Suriyentrakorn <pasin@couchbase.com> on 2/26/14.
 */

package com.luminous.dsys.youthconnect.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Manager;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
import com.crashlytics.android.Crashlytics;
import com.google.api.client.http.HttpResponseException;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Observable;

import io.fabric.sdk.android.Fabric;

public class Application extends android.app.Application {

    public static final String TAG = "Application";
    private static final String DATABASE_NAME = "youth_connect";
    private static final String SYNC_URL_HTTP = BuildConfigYouthConnect.SYNC_URL_HTTP;
    private static final String SYNC_URL_HTTPS = BuildConfigYouthConnect.SYNC_URL_HTTPS;
    private static final String SYNC_URL = SYNC_URL_HTTP;

    private Manager manager;
    private Database database;
    private Synchronize sync;

    private OnSyncProgressChangeObservable onSyncProgressChangeObservable;
    private OnSyncUnauthorizedObservable onSyncUnauthorizedObservable;

    private com.couchbase.lite.View qaView = null;
    private com.couchbase.lite.View docView = null;

    private static final String QA_VIEW_NAME = "qalists";
    private static final String DOC_VIEW_NAME = "doclists";

    public enum AuthenticationType { FACEBOOK, ALL }

    // By default, this should be set to FACEBOOK.  To test "custom cookie" auth,
    // or basic auth change it to ALL. And run the app against your local sync gateway
    // you have control over to create custom cookies and users via the admin port.
    public static AuthenticationType authenticationType = AuthenticationType.FACEBOOK;

    private void initDatabase() {
        try {

            Manager.enableLogging(TAG, Log.VERBOSE);
            Manager.enableLogging(Log.TAG, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_SYNC_ASYNC_TASK, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_SYNC, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_QUERY, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_VIEW, Log.VERBOSE);
            Manager.enableLogging(Log.TAG_DATABASE, Log.VERBOSE);

            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            Log.e(TAG, "Cannot create Manager object", e);
            return;
        }

        try {
            database = manager.getDatabase(DATABASE_NAME);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot get Database", e);
            return;
        }
    }

    private void initObservable() {
        onSyncProgressChangeObservable = new OnSyncProgressChangeObservable();
        onSyncUnauthorizedObservable = new OnSyncUnauthorizedObservable();
    }

    private synchronized void updateSyncProgress(int completedCount, int totalCount, Replication.ReplicationStatus status) {
        onSyncProgressChangeObservable.notifyChanges(completedCount, totalCount, status);
    }

    public void startReplicationSync() {

        sync = new Synchronize.Builder(getDatabase(), SYNC_URL, true)
                .addChangeListener(getReplicationChangeListener())
                .build();
        sync.start();
    }

    public void stopSync() {
        sync.destroyReplications();
        sync = null;
    }

    public void logoutUser() {
        sync.destroyReplications();
        sync = null;
    }

    public void pushLocalNotification(String title, String notificationText){

        NotificationManager notificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //use the flag FLAG_UPDATE_CURRENT to override any notification already there
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification(R.mipmap.ic_launcher, notificationText, System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;

        //notification.setLatestEventInfo(this, title, notificationText, contentIntent);

        //10 is a random number I chose to act as the id for this notification
        notificationManager.notify(10, notification);

    }

    private Replication.ChangeListener getReplicationChangeListener() {
        return new Replication.ChangeListener() {

            @Override
            public void changed(Replication.ChangeEvent event) {
                Replication replication = event.getSource();
                if (event.getError() != null) {
                    Throwable lastError = event.getError();
                    if (lastError.getMessage().contains("existing change tracker")) {
                        pushLocalNotification("Replication Event", String.format("Sync error: %s:", lastError.getMessage()));
                    }
                    if (lastError instanceof HttpResponseException) {
                        HttpResponseException responseException = (HttpResponseException) lastError;
                        if (responseException.getStatusCode() == 401) {
                            onSyncUnauthorizedObservable.notifyChanges();
                        }
                    }
                }
                Log.d(TAG, event.toString());
                updateSyncProgress(
                        replication.getCompletedChangesCount(),
                        replication.getChangesCount(),
                        replication.getStatus()
                );
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        migrateOldVersion();

        initDatabase();
        initObservable();

        qaView = database.getView(QA_VIEW_NAME);
        docView = database.getView(DOC_VIEW_NAME);

        if (qaView.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get("type");
                    if (BuildConfigYouthConnect.DOC_TYPE_FOR_QA.equals(type)) {
                        emitter.emit(document.get(BuildConfigYouthConnect.QA_UPDATED_TIMESTAMP), document);
                    }
                }
            };
            qaView.setMap(mapper, "1");
        }

        if (docView.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get("type");
                    if (BuildConfigYouthConnect.DOC_TYPE_FOR_DOCUMENT.equals(type)) {
                        emitter.emit(document.get(BuildConfigYouthConnect.DOC_CREATED), document);
                    }
                }
            };
            docView.setMap(mapper, "1");
        }

    }

    private void migrateOldVersion() {
        int v = 0;
        try {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Database getDatabase() {
        return this.database;
    }

    public Database setDatabaseForName(String name) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            byte[] inputBytes = name.getBytes();
            byte[] hashBytes = digest.digest(inputBytes);
            database = manager.getDatabase("db" + byteArrayToHex(hashBytes));
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot get Database", e);
        }
        return database;
    }

    public void setDatabase() {
        try {
            database = manager.getDatabase(DATABASE_NAME);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    public OnSyncProgressChangeObservable getOnSyncProgressChangeObservable() {
        return onSyncProgressChangeObservable;
    }

    public OnSyncUnauthorizedObservable getOnSyncUnauthorizedObservable() {
        return onSyncUnauthorizedObservable;
    }

    static class OnSyncProgressChangeObservable extends Observable {
        private void notifyChanges(int completedCount, int totalCount, Replication.ReplicationStatus status) {
            SyncProgress progress = new SyncProgress();
            progress.completedCount = completedCount;
            progress.totalCount = totalCount;
            progress.status = status;
            setChanged();
            notifyObservers(progress);
        }
    }

    static class OnSyncUnauthorizedObservable extends Observable {
        private void notifyChanges() {
            setChanged();
            notifyObservers();
        }
    }

    static class SyncProgress {
        public int completedCount;
        public int totalCount;
        public Replication.ReplicationStatus status;
    }

    public Query getQAQuery(Database database) {
        Query query = qaView.createQuery();
        query.setDescending(true);

        return query;
    }

    public Query getDOCQuery(Database database) {
        Query query = docView.createQuery();
        query.setDescending(true);

        return query;
    }

}
