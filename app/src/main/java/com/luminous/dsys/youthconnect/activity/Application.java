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
import android.os.Build;

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
import com.localytics.android.LocalyticsActivityLifecycleCallbacks;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.pojo.AssignedToUSer;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.customHandler;
import com.pushbots.push.Pushbots;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

    private com.couchbase.lite.View qaAnsweredNodalView = null;
    private com.couchbase.lite.View qaUnAnsweredNodalView = null;

    private com.couchbase.lite.View qaAnsweredAdminView = null;
    private com.couchbase.lite.View qaUnAnsweredAdminView = null;
    private com.couchbase.lite.View qaPublishedView = null;

    private com.couchbase.lite.View docViewForAdmin = null;
    private com.couchbase.lite.View docViewForNodal = null;
    private com.couchbase.lite.View docViewPublished = null;

    private static final String QA_VIEW_NAME_ANSWERED_NODAL = "qalists_answered_nodal";
    private static final String QA_VIEW_NAME_UNANSWERED_NODAL = "qalists_unanswered_nodal";

    private static final String QA_VIEW_NAME_ANSWERED_ADMIN = "qalists_answered_admin";
    private static final String QA_VIEW_NAME_UNANSWERED_ADMIN = "qalists_unanswered_admin";
    private static final String QA_VIEW_NAME_PUBLISHED = "qalists_published";

    private static final String DOC_VIEW_NAME_ADMIN = "doclists_admin";
    private static final String DOC_VIEW_NAME_NODAL = "doclists_nodal";
    private static final String DOC_VIEW_NAME_PUBLISH = "doclists_publish";

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
        registerActivityLifecycleCallbacks(
                new LocalyticsActivityLifecycleCallbacks(this));
        Pushbots.sharedInstance().init(this, getResources().getString(R.string.pb_appid),
                getResources().getString(R.string.pb_senderid), getResources().getString(R.string.pb_logLevel));
        Pushbots.sharedInstance().setCustomHandler(customHandler.class);
        Pushbots.sharedInstance().debug(true);

        migrateOldVersion();

        initDatabase();
        initObservable();

        qaAnsweredAdminView = database.getView(QA_VIEW_NAME_ANSWERED_ADMIN);
        qaUnAnsweredAdminView = database.getView(QA_VIEW_NAME_UNANSWERED_ADMIN);

        qaAnsweredNodalView = database.getView(QA_VIEW_NAME_ANSWERED_NODAL);
        qaUnAnsweredNodalView = database.getView(QA_VIEW_NAME_UNANSWERED_NODAL);
        qaPublishedView = database.getView(QA_VIEW_NAME_PUBLISHED);

        docViewForAdmin = database.getView(DOC_VIEW_NAME_ADMIN);
        docViewForNodal = database.getView(DOC_VIEW_NAME_NODAL);
        docViewPublished = database.getView(DOC_VIEW_NAME_PUBLISH);

        if (qaAnsweredAdminView.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                String type = (String) document.get("type");
                if (BuildConfigYouthConnect.DOC_TYPE_FOR_QA.equals(type)){
                    int is_delete = (Integer) document.get(BuildConfigYouthConnect.QA_IS_DELETE);
                    int is_answered = (Integer) document.get(BuildConfigYouthConnect.QA_IS_ANSWERED);
                    if( is_delete == 0 && is_answered == 1) {
                        emitter.emit(document.get(BuildConfigYouthConnect.QA_UPDATED_TIMESTAMP), document);
                    }
                }
                }
            };
            qaAnsweredAdminView.setMap(mapper, "1");
        }

        if (qaAnsweredNodalView.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get("type");
                    if (BuildConfigYouthConnect.DOC_TYPE_FOR_QA.equals(type)){
                        int currently_logged_in_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                .getInt(Constants.SP_USER_ID, 0);
                        int is_delete = (Integer) document.get(BuildConfigYouthConnect.QA_IS_DELETE);
                        int is_answered = (Integer) document.get(BuildConfigYouthConnect.QA_IS_ANSWERED);
                        int asked_by_user_id = (Integer) document.get(BuildConfigYouthConnect.QA_ASKED_BY_USER_ID);
                        if(is_delete == 0 && is_answered == 1
                            && currently_logged_in_user_id == asked_by_user_id) {
                            emitter.emit(document.get(BuildConfigYouthConnect.QA_UPDATED_TIMESTAMP), document);
                        }
                    }
                }
            };
            qaAnsweredNodalView.setMap(mapper, "1.1");
        }

        if (qaUnAnsweredAdminView.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get("type");
                    if (BuildConfigYouthConnect.DOC_TYPE_FOR_QA.equals(type)){
                        int is_delete = (Integer) document.get(BuildConfigYouthConnect.QA_IS_DELETE);
                        int is_answered = (Integer) document.get(BuildConfigYouthConnect.QA_IS_ANSWERED);
                        if(is_delete == 0 && is_answered == 0) {
                            emitter.emit(document.get(BuildConfigYouthConnect.QA_UPDATED_TIMESTAMP), document);
                        }
                    }
                }
            };
            qaUnAnsweredAdminView.setMap(mapper, "1");
        }

        if (qaUnAnsweredNodalView.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get("type");
                    if (BuildConfigYouthConnect.DOC_TYPE_FOR_QA.equals(type)){
                        int currently_logged_in_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                .getInt(Constants.SP_USER_ID, 0);
                        int is_delete = (Integer) document.get(BuildConfigYouthConnect.QA_IS_DELETE);
                        int is_answered = (Integer) document.get(BuildConfigYouthConnect.QA_IS_ANSWERED);
                        int asked_by_user_id = (Integer) document.get(BuildConfigYouthConnect.QA_ASKED_BY_USER_ID);
                        if( is_delete == 0 && is_answered == 0
                            && currently_logged_in_user_id == asked_by_user_id) {
                            emitter.emit(document.get(BuildConfigYouthConnect.QA_UPDATED_TIMESTAMP), document);
                        }
                    }
                }
            };
            qaUnAnsweredNodalView.setMap(mapper, "1");
        }

        if (qaPublishedView.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get("type");
                    if (BuildConfigYouthConnect.DOC_TYPE_FOR_QA.equals(type)){
                        int is_delete = (Integer) document.get(BuildConfigYouthConnect.QA_IS_DELETE);
                        int is_published = (Integer) document.get(BuildConfigYouthConnect.QA_IS_PUBLISHED);
                        if(is_delete == 0 && is_published == 1){
                            emitter.emit(document.get(BuildConfigYouthConnect.QA_UPDATED_TIMESTAMP), document);
                        }
                    }
                }
            };
            qaPublishedView.setMap(mapper, "1");
        }

        if (docViewForAdmin.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get("type");
                    if (BuildConfigYouthConnect.DOC_TYPE_FOR_DOCUMENT.equals(type)) {
                        int is_delete = (Integer) document.get(BuildConfigYouthConnect.DOC_IS_DELETE);
                        if(is_delete == 0) {
                            emitter.emit(document.get(BuildConfigYouthConnect.DOC_CREATED), document);
                        }
                    }
                }
            };
            docViewForAdmin.setMap(mapper, "1");
        }

        if (docViewForNodal.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get("type");
                    if(BuildConfigYouthConnect.DOC_TYPE_FOR_DOCUMENT.equals(type)) {
                        int is_delete = (Integer) document.get(BuildConfigYouthConnect.DOC_IS_DELETE);
                        int currently_logged_in_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1)
                                .getInt(Constants.SP_USER_ID, 0);

                        ArrayList<LinkedHashMap<String, Object>> assignedToUserIds =
                                (ArrayList<LinkedHashMap<String, Object>>)
                                        document.get(BuildConfigYouthConnect.DOC_ASSIGNED_TO_USER_IDS);

                        int count = 0;

                        for (LinkedHashMap<String, Object> obj : assignedToUserIds) {
                            AssignedToUSer assignedToUSer = new AssignedToUSer();
                            assignedToUSer.setUser_name((String)obj.get("user_name"));
                            assignedToUSer.setUser_id((Integer)obj.get("user_id"));
                            if (assignedToUSer != null && assignedToUSer.getUser_id()
                                    == currently_logged_in_user_id) {
                                count++;
                            }
                        }

                        if (is_delete == 0
                                && count > 0 ){
                            emitter.emit(document.get(BuildConfigYouthConnect.DOC_CREATED), document);
                        }
                    }
                }
            };
            docViewForNodal.setMap(mapper, "1");
        }

        if (docViewPublished.getMap() == null) {
            Mapper mapper = new Mapper() {
                public void map(Map<String, Object> document, Emitter emitter) {
                    String type = (String) document.get("type");
                    if(BuildConfigYouthConnect.DOC_TYPE_FOR_DOCUMENT.equals(type)) {
                        int is_delete = (Integer) document.get(BuildConfigYouthConnect.DOC_IS_DELETE);
                        int is_publish = (Integer) document.get(BuildConfigYouthConnect.DOC_IS_PUBLISHED);
                        if (is_delete == 0 && is_publish == 1) {
                            emitter.emit(document.get(BuildConfigYouthConnect.DOC_CREATED), document);
                        }
                    }
                }
            };
            docViewPublished.setMap(mapper, "1");
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

    public Query getQAUnAnsweredForAdminQuery(Database database) {
        Query query = qaUnAnsweredAdminView.createQuery();
        query.setDescending(true);

        return query;
    }

    public Query getQAAnsweredForAdminQuery(Database database) {
        Query query = qaAnsweredAdminView.createQuery();
        query.setDescending(true);

        return query;
    }

    public Query getQAPublishedForQuery(Database database) {
        Query query = qaPublishedView.createQuery();
        query.setDescending(true);

        return query;
    }

    public Query getQAUnAnsweredForNodalQuery(Database database) {
        Query query = qaUnAnsweredNodalView.createQuery();
        query.setDescending(true);

        return query;
    }

    public Query getQAAnsweredForNodalQuery(Database database) {
        Query query = qaAnsweredNodalView.createQuery();
        query.setDescending(true);

        return query;
    }

    public Query getPublishedDocQuery(Database database) {
        Query query = docViewPublished.createQuery();
        query.setDescending(true);

        return query;
    }

    public Query getDocForAdminQuery(Database database) {
        Query query = docViewForAdmin.createQuery();
        query.setDescending(true);

        return query;
    }

    public Query getDocForNodalQuery(Database database) {
        Query query = docViewForNodal.createQuery();
        query.setDescending(true);

        return query;
    }

}
