/**
 * Created by Pasin Suriyentrakorn <pasin@couchbase.com> on 2/27/14.
 */

package com.luminous.dsys.youthconnect.helper;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.luminous.dsys.youthconnect.BuildConfig;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;

import java.util.ArrayList;
import java.util.List;

public class LiveQueryAdapter extends BaseAdapter {
    private LiveQuery query;
    private QueryEnumerator enumerator;
    private Context context;
    private String filterText;
    private List<Document> documentList;
    boolean isQa;

    public LiveQueryAdapter(Context context, LiveQuery query, final String filterText, final boolean isQa) {
        this.context = context;
        this.query = query;
        this.filterText = filterText;
        this.isQa = isQa;
        documentList = new ArrayList<Document>();

        query.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(final LiveQuery.ChangeEvent event) {
                ((Activity) LiveQueryAdapter.this.context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        enumerator = event.getRows();
                        documentList = new ArrayList<Document>();
                        for(enumerator.iterator(); enumerator.iterator().hasNext();){
                            QueryRow row = enumerator.iterator().next();
                            Document doc = row.getDocument();
                            if(filterText != null && filterText.trim().length() > 0) {
                                if (isQa) {
                                    String title = (String) doc.getProperty(BuildConfigYouthConnect.QA_TITLE);
                                    if (title.contains(filterText)) {
                                        documentList.add(doc);
                                    }
                                } else {
                                    String title = (String) doc.getProperty(BuildConfigYouthConnect.DOC_TITLE);
                                    if (title.contains(filterText)) {
                                        documentList.add(doc);
                                    }
                                }
                            } else{
                                documentList.add(doc);
                            }
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        });

        query.start();
    }

    @Override
    public int getCount() {
        return documentList != null ? documentList.size() : 0;
    }

    @Override
    public Object getItem(int i) {
        return documentList != null ? documentList.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public void invalidate() {
        if (query != null)
            query.stop();
    }

    /*
    Method called in the database change listener when a new change is detected.
    Because live queries do not trigger a change event when non current revisions are saved
    or pulled from a remote database.
     */
    public void updateQueryToShowConflictingRevisions(final Database.ChangeEvent event) {

        ((Activity) LiveQueryAdapter.this.context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                query.stop();
                enumerator = query.getRows();
                notifyDataSetChanged();
            }
        });

    }
}
