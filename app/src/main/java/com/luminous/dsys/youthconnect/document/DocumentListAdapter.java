package com.luminous.dsys.youthconnect.document;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.activity.MainActivity;
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
import java.util.List;

/**
 * Created by Android Luminous on 2/13/2016.
 */
public class DocumentListAdapter extends LiveQueryAdapter {

    private LiveQuery liveQuery;
    private Context context;
    private OnDeleteClickListener onDeleteClickListener;
    private OnUpdateClickListenr onUpdateClickListenr;

    public DocumentListAdapter(Context context, LiveQuery liveQuery,
                               OnDeleteClickListener onDeleteClickListener,
                               OnUpdateClickListenr onUpdateClickListenr){
        super(context, liveQuery);
        this.context = context;
        this.liveQuery = liveQuery;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onUpdateClickListenr = onUpdateClickListenr;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_row, null);
            convertView.setTag(position);
        }

        final Document task = (Document) getItem(position);

        setData(convertView, task, position);

        return convertView;
    }

    private void setData(View convertView, Document task, int position){
        TextView tvQusByUserName = (TextView) convertView.findViewById(R.id.tvQusByUserName);
        tvQusByUserName.setText((String) task.getProperty(BuildConfigYouthConnect.DOC_CREATED_BY_USER_NAME));

        TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
        try {
            tvTime.setText(getTimeToShow((String) task.getProperty(BuildConfigYouthConnect.DOC_CREATED)));
        } catch(Exception exception){
            Log.e("QAListAdapter", "error()", exception);
        }

        TextView tvUserNameShortCut = (TextView) convertView.findViewById(R.id.tvUserNameShortCut);
        String user_name = (String) task.getProperty(BuildConfigYouthConnect.DOC_CREATED_BY_USER_NAME);
        String first_two_characters_of_name = user_name.substring(0, 1).toUpperCase();
        tvUserNameShortCut.setText(first_two_characters_of_name);

        TextView tvTextTitle = (TextView) convertView.findViewById(R.id.tvFileTitle);
        tvTextTitle.setText((String) task.getProperty(BuildConfigYouthConnect.DOC_TITLE));

        final RelativeLayout layoutFileList = (RelativeLayout) convertView.findViewById(R.id.layoutFileList);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = layoutFileList.getVisibility();
                if (visibility == View.GONE || visibility == View.INVISIBLE) {
                    layoutFileList.setVisibility(View.VISIBLE);
                } else {
                    layoutFileList.setVisibility(View.GONE);
                }
            }
        });

        String doc_id = task.getId();
        RecyclerView item_horizontal_list = (RecyclerView) convertView.findViewById(R.id.item_horizontal_list);
        item_horizontal_list.setLayoutManager(new LinearLayoutManager(convertView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        HorizontalAdapter horizontalAdapter = new HorizontalAdapter(doc_id);
        item_horizontal_list.setAdapter(horizontalAdapter);

        List<String> fileNameList = (List<String>) task.getProperty(BuildConfigYouthConnect.DOC_FILES);
        if(fileNameList != null && fileNameList.size() > 0) {
            for (String fileName : fileNameList) {
                fileName.trim().replace(" ", "%20");
            }
            horizontalAdapter.setData(fileNameList, context);
            horizontalAdapter.setRowIndex(position);
        }
    }

    private static class HorizontalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<String> mDataList;
        private int mRowIndex = -1;
        private Context context;
        private String doc_id;

        public HorizontalAdapter(String doc_id) {
            this.doc_id = doc_id;
        }

        public void setData(List<String> data, Context context) {
            if (mDataList != data) {
                mDataList = data;
                notifyDataSetChanged();
            } else {
                mDataList = new ArrayList<String>();
            }
            this.context = context;
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

            holder.img.setImageResource(R.drawable.ic_menu_send);
            holder.img.setBackgroundResource(R.drawable.transparent_body_blue_border_square);
            holder.img.setTag(position);
            holder.progressBar.setTag(position);

            String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/youth_connect";
            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            OutputStream fOut = null;
            String filename = mDataList.get(position);
            filename = filename.trim().replace(" ", "%20");
            File _file = new File(fullPath, filename);
             if (_file.exists()) {
                holder.progressBar.setVisibility(View.INVISIBLE);
                holder.img.setVisibility(View.VISIBLE);
                setImage(_file, holder);
            } else {
                if(Util.getNetworkConnectivityStatus(context)) {
                    BgAsync async = new BgAsync(position, holder, context, doc_id);
                    async.execute();
                } else{

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
                setImage(file, viewHolder);
            }
        }

        private void setImage(File file, ItemViewHolder viewHolder) {
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
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

                if (bitmap != null) {
                    viewHolder.img.setImageBitmap(bitmap);
                } else {
                    viewHolder.img.setImageResource(R.drawable.ic_menu_send);
                    viewHolder.img.setBackgroundResource(R.drawable.transparent_body_blue_border_square);
                }

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
            } else if (filePath != null && filePath.length() > 0
                    && ((filePath.contains("mp4")) ||
                    (filePath.contains("flv")) ||
                    (filePath.contains("3gp")) ||
                    (filePath.contains("avi")))) {
                viewHolder.img.setImageResource(R.drawable.ic_action_play_over_video);
                viewHolder.img.setBackgroundResource(R.drawable.transparent_body_blue_border_square);
                viewHolder.img.setPadding(12, 12, 12, 12);

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

                viewHolder.img.setImageResource(R.drawable.ic_insert_drive_file);
                viewHolder.img.setBackgroundResource(R.color.blue);

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

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    public interface OnDeleteClickListener{
        public void onDeleteClick(Document student);
    }

    public interface OnUpdateClickListenr{
        public void onUpdateClick(Document student);
    }
}
