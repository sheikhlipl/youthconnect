package com.luminous.dsys.youthconnect.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.activity.MainActivity;
import com.luminous.dsys.youthconnect.helper.ImageHelper;
import com.luminous.dsys.youthconnect.helper.LiveQueryAdapter;
import com.luminous.dsys.youthconnect.pojo.FileToUpload;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luminousinfoways on 04/01/16.
 */
public class ShowcaseDataAdapterExp extends LiveQueryAdapter {

    private LiveQuery liveQuery;
    private Context context;
    private static final String TAG = "ShowcaseDataAdapterExp";

    public ShowcaseDataAdapterExp(Context context, LiveQuery liveQuery) {
        super(context, liveQuery);
        this.liveQuery = liveQuery;
        this.context = context;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public Object getItem(int i) {
        return super.getItem(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.showcase_event_list_item, null);
        }

        final Document task = (Document) getItem(position);
        TextView tvActivityName = (TextView) view.findViewById(R.id.tvActivityName);
        tvActivityName.setText((String) task.getProperty(BuildConfigYouthConnect.DOC_TITLE));
        TextView tvActivityPurpose = (TextView) view.findViewById(R.id.tvActivityPurpose);
        tvActivityPurpose.setText((String) task.getProperty(BuildConfigYouthConnect.DOC_PURPOSE));

        String dateTime = ((String) task.getProperty(BuildConfigYouthConnect.DOC_CREATED));
        try {
            String _date_time = getTimeToShow(dateTime);
            TextView tvDate = (TextView) view.findViewById(R.id.tvDate);
            tvDate.setText(_date_time);
        } catch(Exception exception){
            Log.e("ShowcaseFragment", "getTimeToShow()", exception);
        }

        TextView tvPostedBy = (TextView) view.findViewById(R.id.tvPostedBy);
        tvPostedBy.setText((String) task.getProperty(BuildConfigYouthConnect.DOC_CREATED_BY_USER_NAME));

        String doc_id = task.getId();
        RecyclerView item_horizontal_list = (RecyclerView) view.findViewById(R.id.item_horizontal_list);
        item_horizontal_list.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
        HorizontalAdapter horizontalAdapter = new HorizontalAdapter(doc_id);
        item_horizontal_list.setAdapter(horizontalAdapter);

        List<String> fileNameList = (List<String>) task.getProperty(BuildConfigYouthConnect.DOC_FILES);
        if(fileNameList != null && fileNameList.size() > 0) {
            for (String fileName : fileNameList) {
                fileName.trim().replace(" ", "%20");
            }
            horizontalAdapter.setData(fileNameList, context, task);
            horizontalAdapter.setRowIndex(position);
        }

        ImageView imgUnPublish = (ImageView) view.findViewById(R.id.imgUnPublish);
        imgUnPublish.setTag(position);
        imgUnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = (Integer) v.getTag();
                Document task = (Document) getItem(position);
                unPublishDoc(task);
            }
        });

        return view;
    }

    private void unPublishDoc(final Document doc){
        final int is_publish = (Integer) doc.getProperty(BuildConfigYouthConnect.DOC_IS_PUBLISHED);
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context,
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(context,
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(context,
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

    private String getTimeToShow(String time) throws Exception {
        // 2016-12-23T12:33:23.328Z
        String currentDate = Util.getCurrentDateTime();
        if(time != null && time.trim().length() > 0 && time.trim().contains("T")){
            String[] arr = time.trim().split("T");
            for(int i = 0; i < arr.length; i++){
                String date = arr[0];
                if(date != null &&
                        date.length() > 0 &&
                        currentDate != null &&
                        currentDate.trim().length() > 0 &&
                        date.trim().equalsIgnoreCase(currentDate.trim())){

                    String t = arr[1];
                    if(t.contains(":")){
                        String[] op = t.split(":");
                        String finalStr = op[0] + ":" + op[1];
                        return finalStr;
                    }

                    return "";
                } else if(date != null &&
                        date.length() > 0){
                    String[] crdate = date.split("-");
                    String mon = crdate[1];
                    String dd = crdate[2];
                    String monName = Util.getMonInWord(mon);
                    String to_return = monName + " " + dd;
                    return to_return;
                }
            }
        }
        return null;
    }

    private static class HorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<String> mDataList;
        private int mRowIndex = -1;
        private Context context;
        private String doc_id;
        private Document document;

        public HorizontalAdapter(String doc_id) {
            this.doc_id = doc_id;
        }

        public void setData(List<String> data, Context context, Document document) {
            if (mDataList != data) {
                mDataList = data;
                notifyDataSetChanged();
            } else {
                mDataList = new ArrayList<String>();
            }
            this.context = context;
            this.document = document;
        }

        public void setRowIndex(int index) {
            mRowIndex = index;
        }

        private class ItemViewHolder extends RecyclerView.ViewHolder {

            private ImageView img;
            private ProgressBar progressBar;

            public ItemViewHolder(View itemView) {
                super(itemView);
                img = (ImageView) itemView.findViewById(R.id.imageView);
                progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
            ItemViewHolder holder = new ItemViewHolder(itemView);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder rawHolder, int position) {
            ItemViewHolder holder = (ItemViewHolder) rawHolder;

            //holder.img.setImageResource(R.drawable.ic_get_app_black);
            //holder.img.setBackgroundResource(R.drawable.transparent_body_blue_border_square);
            holder.img.setTag(position);
            holder.progressBar.setTag(position);
            holder.progressBar.setVisibility(View.GONE);

            String file_name = mDataList.get(position);
            /*Other files*/
            if (file_name != null && file_name.length() > 0){
                    /*&& ((file_name.contains("mp4")) ||
                    (file_name.contains("flv")) ||
                    (file_name.contains("mkv")) ||
                    (file_name.contains("mp3")) ||
                    (file_name.contains("3gp")) ||
                    (file_name.contains("avi")) ||
                    (file_name.contains("amr")))){*/

                String extension = Util.getFileExt(file_name);

                if(extension.equalsIgnoreCase("mp3")
                        || extension.equalsIgnoreCase("amr")
                        || extension.equalsIgnoreCase("aa")
                        || extension.equalsIgnoreCase("aac")
                        || extension.equalsIgnoreCase("aax")
                        || extension.equalsIgnoreCase("act")
                        || extension.equalsIgnoreCase("aiff")
                        || extension.equalsIgnoreCase("ape")
                        || extension.equalsIgnoreCase("au")
                        || extension.equalsIgnoreCase("awb")
                        || extension.equalsIgnoreCase("dct")
                        || extension.equalsIgnoreCase("dss")
                        || extension.equalsIgnoreCase("dvf")
                        || extension.equalsIgnoreCase("flac")
                        || extension.equalsIgnoreCase("gsm")
                        || extension.equalsIgnoreCase("iklax")
                        || extension.equalsIgnoreCase("ivs")
                        || extension.equalsIgnoreCase("m4a")
                        || extension.equalsIgnoreCase("m4b")
                        || extension.equalsIgnoreCase("m4p")
                        || extension.equalsIgnoreCase("mmf")
                        || extension.equalsIgnoreCase("mpc")
                        || extension.equalsIgnoreCase("msv")
                        || extension.equalsIgnoreCase("ogg")
                        || extension.equalsIgnoreCase("oga")
                        || extension.equalsIgnoreCase("wav")
                        || extension.equalsIgnoreCase("3gp")){

                    // audio
                    holder.img.setLayoutParams(new RelativeLayout.LayoutParams(Util.dp2px(70, context), Util.dp2px(70, context)));
                    holder.img.setPadding(10, 10, 10, 10);
                    holder.img.setImageResource(R.drawable.ic_headset_white);
                    holder.img.setBackgroundResource(R.drawable.circle_purple_for_audio_icon_bg);

                } else if (((extension.equalsIgnoreCase("mp4")) ||
                        (extension.equalsIgnoreCase("flv")) ||
                        (extension.equalsIgnoreCase("3gp")) ||
                        (extension.equalsIgnoreCase("avi")))){
                    // Video
                    holder.img.setLayoutParams(new RelativeLayout.LayoutParams(Util.dp2px(70, context), Util.dp2px(70, context)));
                    holder.img.setPadding(10, 10, 10, 10);
                    holder.img.setImageResource(R.drawable.ic_videocam_white);
                    holder.img.setBackgroundResource(R.drawable.circle_purple_for_record_icon_bg);
                } else if ( ((extension.equalsIgnoreCase("jpg")) ||
                        (extension.equalsIgnoreCase("jpeg")) ||
                        (extension.equalsIgnoreCase("bmp")) ||
                        (extension.equalsIgnoreCase("png")))) {
                    // Image
                    holder.img.setLayoutParams(new RelativeLayout.LayoutParams(Util.dp2px(70, context), Util.dp2px(70, context)));
                    holder.img.setPadding(10, 10, 10, 10);
                    holder.img.setBackgroundResource(R.drawable.transparent_body_blue_border_square);
                } else{
                    // Document
                    holder.img.setLayoutParams(new RelativeLayout.LayoutParams(Util.dp2px(70, context), Util.dp2px(70, context)));
                    holder.img.setPadding(10, 10, 10, 10);
                    holder.img.setImageResource(R.drawable.ic_insert_drive_file);
                    holder.img.setBackgroundResource(R.drawable.circle_purple_for_doc_icon_bg);
                }

                File file = getFileFromAttachment(document, file_name);
                if(file != null) {
                    holder.progressBar.setVisibility(View.GONE);
                    setImage(file, holder, position);
                } else{
                    if(Util.getNetworkConnectivityStatus(context)) {
                        BgAsync async = new BgAsync(position, holder, context, doc_id);
                        async.execute();
                    } else{
                        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            /* Image */
            if (file_name != null && file_name.length() > 0
                    && ((file_name.contains("jpg")) ||
                    (file_name.contains("jpeg")) ||
                    (file_name.contains("bmp")) ||
                    (file_name.contains("png")))) {
                Bitmap bitmap = getBitmapFromAttachment(document, position);
                if(bitmap != null){
                    holder.progressBar.setVisibility(View.GONE);
                    holder.img.setImageBitmap(bitmap);
                } else{
                    if(Util.getNetworkConnectivityStatus(context)) {
                        BgAsync async = new BgAsync(position, holder, context, doc_id);
                        async.execute();
                    } else{
                        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        private View.OnClickListener mItemClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (v == null) {
                    return;
                }

                int columnIndex = (int) v.getTag();
                int rowIndex = mRowIndex;

                String text = String.format("rowIndex:%d ,columnIndex:%d", rowIndex, columnIndex);
                Log.d("test", text);
            }
        };

        private class BgAsync extends AsyncTask<Void, Void, File> {

            private Bitmap bitmap = null;
            private int index = -1;
            private ItemViewHolder viewHolder = null;
            private Context context;
            private String doc_id;

            public BgAsync(int index, ItemViewHolder viewHolder, Context context, String doc_id) {
                BgAsync.this.index = index;
                BgAsync.this.viewHolder = viewHolder;
                BgAsync.this.context = context;
                this.doc_id = doc_id;
            }

            @Override
            protected File doInBackground(Void... params) {

                int a = index;
                List<String> _mDataList = mDataList;
                String df= mDataList.get(index);
                int sd = mDataList.get(index).length();

                if (index >= 0 && mDataList != null && mDataList.size() > 0
                        && index <= mDataList.size() - 1 &&
                        mDataList.get(index) != null &&
                        mDataList.get(index).length() > 0) {

                    int count;
                    try {

                        String fileName = mDataList.get(index);
                        fileName.trim().replace(" ", "%20");
                        String download_link = BuildConfigYouthConnect.SYNC_URL_HTTP + "/" + doc_id  + "/" + mDataList.get(index);

                        if (download_link == null || download_link.trim().length() <= 0) {
                            return null;
                        }

                        String req_url = download_link;

                        URL url = new URL(req_url);
                        URLConnection conection = url.openConnection();
                        conection.connect();
                        // getting file length
                        int lenghtOfFile = conection.getContentLength();

                        // input stream to read file - with 8k buffer
                        InputStream input = new BufferedInputStream(url.openStream(), 8192);

                        String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/youth_connect";
                        File dir = new File(fullPath);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        OutputStream fOut = null;
                        File file = new File(fullPath, fileName);
                        if (file.exists())
                            file.delete();
                        file.createNewFile();

                        // Output stream to write file
                        OutputStream output = new FileOutputStream(file);

                        byte data[] = new byte[1024];

                        long total = 0;

                        while ((count = input.read(data)) != -1) {
                            total += count;
                            // publishing the progress....
                            // After this onProgressUpdate will be called
                            //publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                            // writing data to file
                            output.write(data, 0, count);
                        }

                        // flushing output
                        output.flush();

                        // closing streams
                        output.close();
                        input.close();

                        return file;

                    } catch (SocketTimeoutException e) {
                        Log.e("Error: ", e.getMessage());
                    } catch (Exception e) {
                        Log.e("Error: ", e.getMessage());
                    }
                }

                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                viewHolder.progressBar.setVisibility(View.VISIBLE);
                viewHolder.img.setVisibility(View.INVISIBLE);
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);

                viewHolder.progressBar.setVisibility(View.INVISIBLE);
                viewHolder.img.setVisibility(View.VISIBLE);
                setImage(file, viewHolder, index);
            }
        }

        private void setImage(File file, ItemViewHolder viewHolder, int position) {
            if (file == null || file.getAbsolutePath() == null) {
                return;
            }

            final String filePath = file.getAbsolutePath();
            if (filePath != null && filePath.length() > 0
                    && ((filePath.contains("jpg")) ||
                    (filePath.contains("jpeg")) ||
                    (filePath.contains("bmp")) ||
                    (filePath.contains("png")))) {

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inDither = true;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

                if (bitmap != null) {
                    viewHolder.img.setImageBitmap(bitmap);

                    viewHolder.img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Open Image
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            File file = new File(filePath);
                            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
                            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                            if (extension.equalsIgnoreCase("") || mimetype == null) {
                                // if there is no extension or there is no definite mimetype, still try to open the file
                                intent.setDataAndType(Uri.fromFile(file), "image/*");
                            } else {
                                intent.setDataAndType(Uri.fromFile(file), mimetype);
                            }

                            // custom message for the intent
                            Intent appIntent = Intent.createChooser(intent, "Choose an Application:");
                            if (appIntent != null) {
                                context.startActivity(appIntent);
                            } else {
                                if (context != null && context instanceof MainActivity) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder((MainActivity)context, R.style.AppCompatAlertDialogStyle);
                                    builder.setTitle(context.getResources().getString(R.string.no_app_found_title));
                                    builder.setMessage(context.getResources().getString(R.string.no_app_found_message));
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                }
                            }
                        }
                    });
                } /*else {
                    viewHolder.img.setImageResource(R.drawable.ic_get_app_black);
                    viewHolder.img.setBackgroundResource(R.drawable.transparent_body_blue_border_square);
                    viewHolder.img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(Util.getNetworkConnectivityStatus(context)) {
                                int pos = (Integer) v.get;
                                BgAsync async = new BgAsync(pos, viewHolder, context, doc_id);
                                async.execute();
                            }
                        }
                    });
                }*/
            } else if (filePath != null && filePath.length() > 0
                    && ((filePath.contains("mp4")) ||
                    (filePath.contains("flv")) ||
                    (filePath.contains("3gp")) ||
                    (filePath.contains("avi")))) {
                //viewHolder.img.setImageResource(R.drawable.ic_play_circle_outline_black);
//                viewHolder.img.setBackgroundResource(R.drawable.transparent_body_blue_border_square);
//                viewHolder.img.setPadding(12, 12, 12, 12);

                viewHolder.img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Open Video
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        File file = new File(filePath);
                        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
                        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        if (extension.equalsIgnoreCase("") || mimetype == null) {
                            // if there is no extension or there is no definite mimetype, still try to open the file
                            intent.setDataAndType(Uri.fromFile(file), "video/*");
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), mimetype);
                        }

                        // custom message for the intent
                        Intent appIntent = Intent.createChooser(intent, "Choose an Application:");
                        if (appIntent != null) {
                            context.startActivity(appIntent);
                        } else {
                            if(context != null && context instanceof MainActivity) {
                                AlertDialog.Builder builder = new AlertDialog.Builder((MainActivity)context, R.style.AppCompatAlertDialogStyle);
                                builder.setTitle(context.getResources().getString(R.string.no_app_found_title));
                                builder.setMessage(context.getResources().getString(R.string.no_app_found_message));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        }
                    }
                });

            } else {

//                viewHolder.img.setImageResource(R.drawable.ic_open_in_new_black);
//                viewHolder.img.setBackgroundResource(R.drawable.transparent_body_blue_border_square);

                viewHolder.img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Open Document
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        File file = new File(filePath);
                        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
                        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                        if (extension.equalsIgnoreCase("") || mimetype == null) {
                            // if there is no extension or there is no definite mimetype, still try to open the file
                            intent.setDataAndType(Uri.fromFile(file), "text/*");
                        } else {
                            intent.setDataAndType(Uri.fromFile(file), mimetype);
                        }

                        // custom message for the intent
                        Intent appIntent = Intent.createChooser(intent, "Choose an Application:");
                        if (appIntent != null) {
                            context.startActivity(appIntent);
                        } else {
                            if(context != null && context instanceof MainActivity) {
                                AlertDialog.Builder builder = new AlertDialog.Builder((MainActivity)context, R.style.AppCompatAlertDialogStyle);
                                builder.setTitle(context.getResources().getString(R.string.no_app_found_title));
                                builder.setMessage(context.getResources().getString(R.string.no_app_found_message));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        }
                    }
                });
            }
        }
    }

    public void openDocumentFile(String name) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(name);
        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (extension.equalsIgnoreCase("") || mimetype == null) {
            // if there is no extension or there is no definite mimetype, still try to open the file
            intent.setDataAndType(Uri.fromFile(file), "text/*");
        } else {
            intent.setDataAndType(Uri.fromFile(file), mimetype);
        }

        // custom message for the intent
        Intent appIntent = Intent.createChooser(intent, "Choose an Application:");
        if(appIntent != null){
            context.startActivity(appIntent);
        } else{
            if(context != null && context instanceof MainActivity) {
                AlertDialog.Builder builder = new AlertDialog.Builder((MainActivity)context, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(context.getResources().getString(R.string.no_app_found_title));
                builder.setMessage(context.getResources().getString(R.string.no_app_found_message));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }
    }

    public void openVideoFile(String name) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File file = new File(name);
        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (extension.equalsIgnoreCase("") || mimetype == null) {
            // if there is no extension or there is no definite mimetype, still try to open the file
            intent.setDataAndType(Uri.fromFile(file), "video/mp4");
        } else {
            intent.setDataAndType(Uri.fromFile(file), mimetype);
        }

        // custom message for the intent
        Intent appIntent = Intent.createChooser(intent, "Choose an Application:");
        if(appIntent != null){
            context.startActivity(appIntent);
        } else{
            if(context != null && context instanceof MainActivity) {
                AlertDialog.Builder builder = new AlertDialog.Builder((MainActivity)context,
                        R.style.AppCompatAlertDialogStyle);
                builder.setTitle(context.getResources().getString(R.string.no_app_found_title));
                builder.setMessage(context.getResources().getString(R.string.no_app_found_message));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }
    }

    private static final int THUMBNAIL_SIZE_PX = 150;

    private static Bitmap getBitmapFromAttachment(Document document, int position){

        if(document == null){
            return null;
        }

        Bitmap thumbnail = null;
        List<Attachment> attachments = document.getCurrentRevision().getAttachments();
        if (attachments != null && attachments.size() > 0) {
            Attachment attachment = attachments.get(0);
            try {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(attachment.getContent(), null, options);
                options.inSampleSize = ImageHelper.calculateInSampleSize(
                        options, THUMBNAIL_SIZE_PX, THUMBNAIL_SIZE_PX);
                attachment.getContent().close();

                // Need to get a new attachment again as the FileInputStream
                // doesn't support mark and reset.
                attachments = document.getCurrentRevision().getAttachments();
                attachment = attachments.get(position);
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeStream(attachment.getContent(), null, options);
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                thumbnail = ThumbnailUtils.extractThumbnail(bitmap, THUMBNAIL_SIZE_PX, THUMBNAIL_SIZE_PX);
                attachment.getContent().close();
            } catch (Exception e) {
                com.couchbase.lite.util.Log.e(TAG, "Cannot decode the attached image", e);
            }
        }

        return thumbnail;
    }

    private static File getFileFromAttachment(Document document, String file_name){

        if(document == null || file_name == null){
            return null;
        }

        Bitmap thumbnail = null;
        File file = null;
        List<Attachment> attachments = document.getCurrentRevision().getAttachments();
        if (attachments != null && attachments.size() > 0) {
            Attachment attachment = attachments.get(0);
            try {
                InputStream inputStream = attachment.getContent();
                String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/youth_connect";
                file = new File(fullPath, file_name);
                copyInputStreamToFile(inputStream, file);
                attachment.getContent().close();
            } catch (Exception e) {
                com.couchbase.lite.util.Log.e(TAG, "Cannot decode the attached image", e);
            }
        }

        return file;
    }

    private static void copyInputStreamToFile( InputStream in, File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}