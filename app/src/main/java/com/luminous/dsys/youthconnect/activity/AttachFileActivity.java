package com.luminous.dsys.youthconnect.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.replicator.Replication;
import com.google.api.client.util.Base64;
import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.pojo.Answer;
import com.luminous.dsys.youthconnect.pojo.AssignedToUSer;
import com.luminous.dsys.youthconnect.pojo.Comment;
import com.luminous.dsys.youthconnect.pojo.FileToUpload;
import com.luminous.dsys.youthconnect.pojo.PendingFileToUpload;
import com.luminous.dsys.youthconnect.util.BuildConfigYouthConnect;
import com.luminous.dsys.youthconnect.util.Constants;
import com.luminous.dsys.youthconnect.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by luminousinfoways on 18/12/15.
 */
public class AttachFileActivity extends BaseActivity
        implements View.OnClickListener {

    private static final String TAG = "AttachFileActivity";

    private LinearLayout mRevealView;
    private boolean hidden = true;

    private static final int PICK_IMAGE_REQUEST = 134;
    private static final int PICK_VIDEO_REQUEST = 127;
    private static final int PICK_AUDIO_REQUEST = 115;
    private static final int PICK_DOC_REQUEST = 105;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 102;
    private static final int RQS_RECORDING = 144;

    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    public static List<PendingFileToUpload> fileUploadList = null;
    public static List<FileToUpload> fileToUploads = null;
    private boolean isFromActivityResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_attach);

        if (null != toolbar) {
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);

            toolbar.setTitle(getResources().getString(R.string.activity_ask_a_question_title));
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NavUtils.navigateUpFromSameTask(AttachFileActivity.this);
                }
            });

            // Inflate a menu to be displayed in the toolbar
            //toolbar.inflateMenu(R.menu.settings);
        }

        ImageView imgDone = (ImageView) findViewById(R.id.imgDone);
        imgDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onActionDoneClick();
            }
        });

        if(fileUploadList == null){
            fileUploadList = new ArrayList<PendingFileToUpload>();
        }

        if(fileToUploads == null){
            fileToUploads = new ArrayList<FileToUpload>();
        }

        mRevealView = (LinearLayout) findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.INVISIBLE);

        ImageView imgGallery = (ImageView) findViewById(R.id.imgGallery);
        imgGallery.setOnClickListener(this);
        ImageView imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
        imgPhoto.setOnClickListener(this);
        ImageView imgVideo = (ImageView) findViewById(R.id.imgVideo);
        imgVideo.setOnClickListener(this);
        ImageView imgAudio = (ImageView) findViewById(R.id.imgAudio);
        imgAudio.setOnClickListener(this);
        ImageView imgDoc = (ImageView) findViewById(R.id.imgDoc);
        imgDoc.setOnClickListener(this);
        ImageView imgMicrophone = (ImageView) findViewById(R.id.imgMicrophone);
        imgMicrophone.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isFromActivityResult == false){
            setPreviousDataToList();
        }
    }

    private void setPreviousDataToList(){

        LinearLayout layoutDoc = (LinearLayout) findViewById(R.id.layoutDoc);
        layoutDoc.removeAllViews();
        if(fileUploadList != null && fileUploadList.size() > 0){
            for(int i = 0; i < fileUploadList.size(); i++){
                int position = i;
                PendingFileToUpload fileUpload1 = fileUploadList.get(i);
                int file_type = fileUpload1.getFileType();
                String file_path = fileUpload1.getFilePath();
                int is_uploaded = fileUpload1.getIs_uploaded();

                if(fileUpload1 != null && file_type == Constants.DOC){
                    if (file_path == null || file_path.length() <= 0) {
                        return;
                    } else{
                        PendingFileToUpload pendingFileToUpload = new PendingFileToUpload(Parcel.obtain());
                        String FilePath = file_path;
                        pendingFileToUpload = getPendingFileToBeUploadFromData(FilePath, "", Constants.DOC, is_uploaded);

                        addViewToList(pendingFileToUpload, 1, position);
                    }
                }

                if(fileUpload1 != null
                        && fileUpload1.getFileUri() != null) {
                    Uri uri_path = Uri.parse(fileUpload1.getFileUri());
                    if (uri_path == null || file_type < 0) {
                        return;
                    }

                    if (file_type == Constants.AUDIO) {
                        PendingFileToUpload pendingFileToUpload = new PendingFileToUpload(Parcel.obtain());

                        Uri selectedAudioUri = uri_path;
                        //String selectedAudioPath = getPathForAudio(selectedAudioUri);
                        String selectedAudioPath = getRealPathFromURI(selectedAudioUri);
                        pendingFileToUpload = getPendingFileToBeUploadFromData(selectedAudioPath, selectedAudioUri.toString(), Constants.AUDIO, is_uploaded);

                        addViewToList(pendingFileToUpload, 1, position);
                    } else if (file_type == Constants.VIDEO) {
                        final Uri uri = uri_path;
                        PendingFileToUpload pendingFileToUpload = new PendingFileToUpload(Parcel.obtain());
                        Uri selectedVideoUri = uri;
                        //String selectedImagePath = getPathForVideo(selectedVideoUri);
                        String selectedImagePath = getRealPathFromURI(selectedVideoUri);
                        pendingFileToUpload = getPendingFileToBeUploadFromData(selectedImagePath, selectedVideoUri.toString(), Constants.VIDEO, is_uploaded);
                        addViewToList(pendingFileToUpload, 1, position);
                    } else if(file_type == Constants.IMAGE){
                        final Uri uri = uri_path;
                        String filePathForImage = getRealPathFromURI(uri_path);
                        Uri selectedImageUri = uri;
                        PendingFileToUpload pendingFileToUpload = new PendingFileToUpload(Parcel.obtain());
                        pendingFileToUpload = getPendingFileToBeUploadFromData(filePathForImage, selectedImageUri.toString(), Constants.IMAGE, is_uploaded);
                        addViewToList(pendingFileToUpload, 1, position);
                    }
                }
            }
        } else{
            layoutDoc.removeAllViews();
        }
    }

    private void addViewToList(PendingFileToUpload pendingFileToBeUpload, int is_new, final int position) {
        LinearLayout layoutDoc = (LinearLayout) findViewById(R.id.layoutDoc);

        String file_path = pendingFileToBeUpload.getFilePath();
        final String uri_path = pendingFileToBeUpload.getFileUri();
        int status = pendingFileToBeUpload.getIs_uploaded();
        int file_type = pendingFileToBeUpload.getFileType();

        if(is_new == 0){

            for(int i= 0; i < layoutDoc.getChildCount(); i++){
                RelativeLayout relativeLayout = (RelativeLayout) ((ViewGroup)layoutDoc.getChildAt(i));
                // new background because something has changed
                // check if it's not the imageView you just clicked because you don't want to change its background

                String file_path_tag = (String) relativeLayout.getTag();
                if(pendingFileToBeUpload != null){
                    String filePath =  pendingFileToBeUpload.getFilePath();
                    if(filePath != null && file_path.equalsIgnoreCase(file_path_tag)){
                        ((ProgressBar) relativeLayout.findViewById(R.id.pBar)).setVisibility(View.INVISIBLE);
                        if(status == 1){
                            //Sent failure
                            ImageView imgSent = (ImageView) relativeLayout.findViewById(R.id.imgSent);
                            imgSent.setVisibility(View.VISIBLE);
                            ImageView imgPending = (ImageView) relativeLayout.findViewById(R.id.imgPending);
                            imgPending.setVisibility(View.GONE);
                        } else {
                            //Sent successfully
                            ImageView imgSent = (ImageView) relativeLayout.findViewById(R.id.imgSent);
                            imgSent.setVisibility(View.GONE);
                            ImageView imgPending = (ImageView) relativeLayout.findViewById(R.id.imgPending);
                            imgPending.setVisibility(View.VISIBLE);
                        }
                    }
                    ImageView imgRemove = (ImageView) relativeLayout.findViewById(R.id.imgRemove);
                    imgRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fileUploadList.remove(position);
                            fileToUploads.remove(position);
                            setPreviousDataToList();
                        }
                    });
                }
            }
        } else{
            switch (file_type) {
                case Constants.AUDIO:

                    RelativeLayout layout_doc = (RelativeLayout) LayoutInflater
                            .from(AttachFileActivity.this)
                            .inflate(R.layout.layout_list_file_child_doc, null);

                    //final String _filePath = getPathForAudio(Uri.parse(uri_path));
                    final String _filePath = getRealPathFromURI(Uri.parse(uri_path));
                    if(_filePath != null) {
                        TextView textDocName = (TextView) layout_doc.findViewById(R.id.textDocName);
                        textDocName.setText(_filePath);
                    }

                    ((ProgressBar) layout_doc.findViewById(R.id.pBar)).setVisibility(View.INVISIBLE);
                    if(status == 1){
                        //Sent failure
                        ImageView imgSent = (ImageView) layout_doc.findViewById(R.id.imgSent);
                        imgSent.setVisibility(View.VISIBLE);
                        ImageView imgPending = (ImageView) layout_doc.findViewById(R.id.imgPending);
                        imgPending.setVisibility(View.GONE);
                    } else {
                        //Sent successfully
                        ImageView imgSent = (ImageView) layout_doc.findViewById(R.id.imgSent);
                        imgSent.setVisibility(View.GONE);
                        ImageView imgPending = (ImageView) layout_doc.findViewById(R.id.imgPending);
                        imgPending.setVisibility(View.VISIBLE);
                    }

                    ImageView imgRemove = (ImageView) layout_doc.findViewById(R.id.imgRemove);
                    imgRemove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fileUploadList.remove(position);
                            fileToUploads.remove(position);
                            setPreviousDataToList();
                        }
                    });

                    layoutDoc.addView(layout_doc);
                    layout_doc.setTag(_filePath);
                    layout_doc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            File file = new File(_filePath);
                            intent.setDataAndType(Uri.fromFile(file), "audio/*");
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            } else{
                                Snackbar snackbar = Snackbar.make(findViewById(R.id.layoutParent),
                                        "Your device does not support this file." ,
                                        Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                });
                                View snackbarView = snackbar.getView();
                                TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                                tv.setTextColor(Color.WHITE);
                                TextView tvAction = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_action);
                                tvAction.setTextColor(Color.CYAN);
                                snackbar.show();
                            }
                        }
                    });

                    break;
                case Constants.VIDEO:

                    RelativeLayout layoutVideoItem = (RelativeLayout) LayoutInflater
                            .from(AttachFileActivity.this)
                            .inflate(R.layout.layout_list_file_child_video, null);

                    ImageView imageThumbnail = (ImageView) layoutVideoItem.findViewById(R.id.imageThumbnail);
                    ImageView imgPlay = (ImageView) layoutVideoItem.findViewById(R.id.imgPlay);

                    imgPlay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(uri_path), "video/*");
                            startActivity(Intent.createChooser(intent, "Complete action using"));
                        }
                    });

                    //String selectedImagePath = getPathForVideo(Uri.parse(uri_path));
                    String selectedImagePath = getRealPathFromURI(Uri.parse(uri_path));
                    if(selectedImagePath != null && selectedImagePath.length() > 0) {
                        Bitmap bmThumbnail = ThumbnailUtils.createVideoThumbnail(selectedImagePath, MediaStore.Video.Thumbnails.MICRO_KIND);
                        imageThumbnail.setImageBitmap(bmThumbnail);
                    }

                    //String filePathForVideo = getPathForVideo(Uri.parse(uri_path));
                    String filePathForVideo = getRealPathFromURI(Uri.parse(uri_path));
                    layoutVideoItem.setTag(filePathForVideo);

                    ((ProgressBar) layoutVideoItem.findViewById(R.id.pBar)).setVisibility(View.INVISIBLE);
                    if(status == 1){
                        //Sent failure
                        ImageView imgSent = (ImageView) layoutVideoItem.findViewById(R.id.imgSent);
                        imgSent.setVisibility(View.VISIBLE);
                        ImageView imgPending = (ImageView) layoutVideoItem.findViewById(R.id.imgPending);
                        imgPending.setVisibility(View.GONE);
                    } else {
                        //Sent successfully
                        ImageView imgSent = (ImageView) layoutVideoItem.findViewById(R.id.imgSent);
                        imgSent.setVisibility(View.GONE);
                        ImageView imgPending = (ImageView) layoutVideoItem.findViewById(R.id.imgPending);
                        imgPending.setVisibility(View.VISIBLE);
                    }

                    ImageView jh = (ImageView) layoutVideoItem.findViewById(R.id.imgRemove);
                    jh.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fileUploadList.remove(position);
                            fileToUploads.remove(position);
                            setPreviousDataToList();
                        }
                    });

                    layoutDoc.addView(layoutVideoItem);

                    break;
                case Constants.IMAGE:

                    RelativeLayout layoutImageItem = (RelativeLayout) LayoutInflater
                            .from(AttachFileActivity.this)
                            .inflate(R.layout.layout_list_file_child_image, null);

                    ImageView img = (ImageView) layoutImageItem.findViewById(R.id.img);
                    String imagePath = getRealPathFromURI(Uri.parse(uri_path));
                    System.out.println("Image Path : " + imagePath);
                    img.setImageURI(Uri.parse(uri_path));

                    img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(uri_path), "image/*");
                            startActivity(Intent.createChooser(intent, "Complete action using"));
                        }
                    });

                    String filePathForImage = getRealPathFromURI(Uri.parse(uri_path));
                    layoutImageItem.setTag(filePathForImage);

                    ((ProgressBar) layoutImageItem.findViewById(R.id.pBar)).setVisibility(View.INVISIBLE);
                    if(status == 1){
                        //Sent failure
                        ImageView imgSent = (ImageView) layoutImageItem.findViewById(R.id.imgSent);
                        imgSent.setVisibility(View.VISIBLE);
                        ImageView imgPending = (ImageView) layoutImageItem.findViewById(R.id.imgPending);
                        imgPending.setVisibility(View.GONE);
                    } else {
                        //Sent successfully
                        ImageView imgSent = (ImageView) layoutImageItem.findViewById(R.id.imgSent);
                        imgSent.setVisibility(View.GONE);
                        ImageView imgPending = (ImageView) layoutImageItem.findViewById(R.id.imgPending);
                        imgPending.setVisibility(View.VISIBLE);
                    }

                    ImageView kjjk = (ImageView) layoutImageItem.findViewById(R.id.imgRemove);
                    kjjk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fileUploadList.remove(position);
                            fileToUploads.remove(position);
                            setPreviousDataToList();
                        }
                    });

                    layoutDoc.addView(layoutImageItem);

                    break;
                case Constants.DOC:

                    RelativeLayout _layout_doc = (RelativeLayout) LayoutInflater
                            .from(AttachFileActivity.this)
                            .inflate(R.layout.layout_list_file_child_doc, null);

                    if(file_path != null) {
                        TextView textDocName = (TextView) _layout_doc.findViewById(R.id.textDocName);
                        textDocName.setText(file_path);
                        _layout_doc.setTag(file_path);
                    }

                    ((ProgressBar) _layout_doc.findViewById(R.id.pBar)).setVisibility(View.INVISIBLE);
                    if(status == 1){
                        //Sent failure
                        ImageView imgSent = (ImageView) _layout_doc.findViewById(R.id.imgSent);
                        imgSent.setVisibility(View.VISIBLE);
                        ImageView imgPending = (ImageView) _layout_doc.findViewById(R.id.imgPending);
                        imgPending.setVisibility(View.GONE);
                    } else {
                        //Sent successfully
                        ImageView imgSent = (ImageView) _layout_doc.findViewById(R.id.imgSent);
                        imgSent.setVisibility(View.GONE);
                        ImageView imgPending = (ImageView) _layout_doc.findViewById(R.id.imgPending);
                        imgPending.setVisibility(View.VISIBLE);
                    }

                    ImageView jhh = (ImageView) _layout_doc.findViewById(R.id.imgRemove);
                    jhh.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fileUploadList.remove(position);
                            fileToUploads.remove(position);
                            setPreviousDataToList();
                        }
                    });

                    layoutDoc.addView(_layout_doc);

                    final String _file_path_ = file_path;
                    _layout_doc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (_file_path_ != null && _file_path_.length() > 0) {
                                openDocumentFile(_file_path_);
                            }
                        }
                    });

                    break;
                default:
                    break;
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
        if(appIntent != null) {
            startActivity(appIntent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(AttachFileActivity.this,
                    R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getResources().getString(R.string.no_app_found_title));
            builder.setMessage(getResources().getString(R.string.no_app_found_message));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.imgGallery:

                CharSequence colors[] = new CharSequence[] {"Videos", "Images"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose one");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        switch (which) {
                            case 0:
                                Intent intentVideo = new Intent();
                                intentVideo.setType("video/*");
                                intentVideo.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intentVideo, "Select video"), PICK_VIDEO_REQUEST);
                                break;
                            case 1:
                                Intent intentImage = new Intent();
                                intentImage.setType("image/*");
                                intentImage.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intentImage, "Select image"), PICK_IMAGE_REQUEST);
                                break;
                            default:
                                break;
                        }
                    }
                });
                builder.show();

                break;
            case R.id.imgPhoto:
                clickPic();
                break;
            case R.id.imgVideo:
                recordVideo();
                break;
            case R.id.imgAudio:
                Intent intentImage = new Intent();
                intentImage.setType("audio/*");
                intentImage.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intentImage, "Select audio"), PICK_AUDIO_REQUEST);
                break;
            case R.id.imgDoc:
                Intent intentDoc = new Intent();
                intentDoc.setType("file/*");
                intentDoc.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intentDoc, "Select document"), PICK_DOC_REQUEST);
                break;
            case R.id.imgMicrophone:
                Intent intent =
                        new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
                startActivityForResult(intent, RQS_RECORDING);
                break;
            default:
                break;
        }
    }

    private PendingFileToUpload getPendingFileToBeUploadFromData
            (String file_path, String uri_path, int file_type, int is_uploaded){

        String user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getInt(Constants.SP_USER_ID, 0)+"";
        int user_type_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getInt(Constants.SP_USER_TYPE, 0);
        String m_desg_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getString(Constants.SP_USER_DESG_ID, "");

        PendingFileToUpload pendingFileToUpload = new PendingFileToUpload(Parcel.obtain());
        pendingFileToUpload.setUser_id(user_id);
        pendingFileToUpload.setUser_type_id(user_type_id + "");
        pendingFileToUpload.setM_desg_id(m_desg_id);
        pendingFileToUpload.setFileUri(uri_path);
        pendingFileToUpload.setFileType(file_type);
        pendingFileToUpload.setIs_uploaded(is_uploaded);
        pendingFileToUpload.setFilePath(file_path);

        List<String> fileList =  new ArrayList<String>();
        fileList.add(file_path);

        return pendingFileToUpload;
    }

    private String getMimeTypeFromUri(Uri uri){
        ContentResolver cR = getContentResolver();
        String type = cR.getType(uri);
        return type;
    }

    private boolean fileSizeLessThanMaxFileSizeLimit(Intent data, boolean isCameraCaptureImageRequest){

        if(data == null){
            return false;
        }

        String fileSize = "";
        if(isCameraCaptureImageRequest){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Uri uri = getImageUri(this, photo);
            fileSize = calculateFileSize(uri);
        } else {
            Uri uri = data.getData();
            fileSize = calculateFileSize(uri);
        }
        Log.i(TAG, "FileSize : "+fileSize);
        if(fileSize != null && fileSize.length() > 0
                && TextUtils.isDigitsOnly(fileSize)){
            int size = Integer.parseInt(fileSize);
            if(size < 6){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String calculateFileSize(Uri filepath)
    {
        //String filepathstr=filepath.toString();
        String file_path = getRealPathFromURI(filepath);
        File file = new File(file_path);

        // Get length of file in bytes
        long fileSizeInBytes = file.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;

        String calString=Long.toString(fileSizeInMB);
        return calString;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mRevealView = (LinearLayout) findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.INVISIBLE);
        isFromActivityResult = true;

        boolean isCameraCaptureImageRequest = false;
        if(requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE){
            isCameraCaptureImageRequest = true;
        }
        if(fileSizeLessThanMaxFileSizeLimit(data, isCameraCaptureImageRequest) == false){

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("File Upload");
            builder.setMessage("Sorry, exceeds file size limit.");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();

            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("File Upload");
        builder.setMessage("Are you sure want to upload this file?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                PendingFileToUpload pendingFileToUpload = new PendingFileToUpload(Parcel.obtain());
                FileToUpload fileToUpload = new FileToUpload();

                if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

                    Uri selectedImageUri = data.getData();
                    String filePathForImage = getRealPathFromURI(selectedImageUri);
                    pendingFileToUpload = getPendingFileToBeUploadFromData(filePathForImage, selectedImageUri.toString(), Constants.IMAGE, 0);
                    fileToUpload.setInputStream(selectedImageUri);
                    String filename = filePathForImage.substring(filePathForImage.lastIndexOf("/") + 1);
                    fileToUpload.setFile_name(filename);
                    fileToUpload.setMime_type(getMimeTypeFromUri(selectedImageUri));
                } else if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

                    Uri selectedVideoUri = data.getData();
                    //String selectedImagePath = getPathForVideo(selectedVideoUri);
                    String selectedImagePath = getRealPathFromURI(selectedVideoUri);
                    pendingFileToUpload = getPendingFileToBeUploadFromData(selectedImagePath, selectedVideoUri.toString(), Constants.VIDEO, 0);
                    fileToUpload.setInputStream(selectedVideoUri);
                    String filename = selectedImagePath.substring(selectedImagePath.lastIndexOf("/") + 1);
                    fileToUpload.setFile_name(filename);
                    fileToUpload.setMime_type(getMimeTypeFromUri(selectedVideoUri));

                } else if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK
                        && data != null && data.getData() != null) {
                    Uri selectedAudioUri = data.getData();
                    String selectedAudioPath = getRealPathFromURI(selectedAudioUri);
                    pendingFileToUpload = getPendingFileToBeUploadFromData(selectedAudioPath, selectedAudioUri.toString(), Constants.AUDIO, 0);
                    fileToUpload.setInputStream(selectedAudioUri);
                    String filename = selectedAudioPath.substring(selectedAudioPath.lastIndexOf("/") + 1);
                    fileToUpload.setFile_name(filename);
                    fileToUpload.setMime_type(getMimeTypeFromUri(selectedAudioUri));

                } else if (requestCode == PICK_DOC_REQUEST && resultCode == RESULT_OK
                        && data != null && data.getData() != null) {

                    String FilePath = data.getData().getPath();
                    Uri geturi = data.getData();
                    pendingFileToUpload = getPendingFileToBeUploadFromData(FilePath, "", Constants.DOC, 0);
                    fileToUpload.setInputStream(geturi);
                    String filename = FilePath.substring(FilePath.lastIndexOf("/") + 1);
                    fileToUpload.setFile_name(filename);
                    String type = null;
                    String extension = MimeTypeMap.getFileExtensionFromUrl(filename);
                    if (extension != null) {
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    }
                    fileToUpload.setMime_type(type);
                    Log.i("asdfsdf", "asdfsadf");

                } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == RESULT_OK
                        && data != null && data.getExtras() != null) {

                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    Uri tempUri = getImageUri(getApplicationContext(), photo);
                    File finalFile = new File(getRealPathFromURI(tempUri));
                    if (finalFile != null) {
                        System.out.println(finalFile.getPath());
                    }
                    String filePathForImage = getRealPathFromURI(tempUri);
                    pendingFileToUpload = getPendingFileToBeUploadFromData(filePathForImage, tempUri.toString(), Constants.IMAGE, 0);
                    fileToUpload.setInputStream(tempUri);
                    String filename = filePathForImage.substring(filePathForImage.lastIndexOf("/") + 1);
                    fileToUpload.setFile_name(filename);
                    fileToUpload.setMime_type(getMimeTypeFromUri(tempUri));
                } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE && resultCode == RESULT_OK
                        && data != null && data.getData() != null) {
                    Uri selectedVideoUri = data.getData();
                    //String selectedImagePath = getPathForVideo(selectedVideoUri);
                    String selectedImagePath = getRealPathFromURI(selectedVideoUri);
                    pendingFileToUpload = getPendingFileToBeUploadFromData(selectedImagePath, selectedVideoUri.toString(), Constants.VIDEO, 0);
                    fileToUpload.setInputStream(selectedVideoUri);
                    String filename = selectedImagePath.substring(selectedImagePath.lastIndexOf("/") + 1);
                    fileToUpload.setFile_name(filename);
                    fileToUpload.setMime_type(getMimeTypeFromUri(selectedVideoUri));
                } else if (requestCode == RQS_RECORDING && resultCode == RESULT_OK
                        && data != null && data.getData() != null) {
                    Uri selectedAudioUri = data.getData();
                    String selectedAudioPath = getRealPathFromURI(selectedAudioUri);
                    pendingFileToUpload = getPendingFileToBeUploadFromData(selectedAudioPath, selectedAudioUri.toString(), Constants.AUDIO, 0);
                    fileToUpload.setInputStream(selectedAudioUri);
                    String filename = selectedAudioPath.substring(selectedAudioPath.lastIndexOf("/") + 1);
                    fileToUpload.setFile_name(filename);
                    fileToUpload.setMime_type(getMimeTypeFromUri(selectedAudioUri));
                }

                int pos = fileUploadList.size();
                addViewToList(pendingFileToUpload, 1, pos);
                fileUploadList.add(pendingFileToUpload);
                fileToUploads.add(fileToUpload);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if(resultCode == RESULT_OK) {
            builder.show();
        }
    }

    private String createDocument(Database database, String doc_title,
                                  String doc_purpose, String doc_created_by_user_name,
                                  List<String> fileToUploads,List<FileToUpload> fileToUploadList,
                                  int doc_created_by_user_id,
                                  List<AssignedToUSer> assigned_to_user_ids) {
        // Create a new document and add data
        Document document = database.createDocument();
        String documentId = document.getId();

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimestamp = dateFormatter.format(calendar.getTime());

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("type", BuildConfigYouthConnect.DOC_TYPE_FOR_DOCUMENT);
        map.put(BuildConfigYouthConnect.DOC_TITLE, doc_title);
        map.put(BuildConfigYouthConnect.DOC_PURPOSE, doc_purpose);
        map.put(BuildConfigYouthConnect.DOC_CREATED_BY_USER_NAME, doc_created_by_user_name);
        map.put(BuildConfigYouthConnect.DOC_CREATED_BY_USER_ID, doc_created_by_user_id);
        map.put(BuildConfigYouthConnect.DOC_FILES, fileToUploads);
        map.put(BuildConfigYouthConnect.DOC_IS_UPLOADED, 0);
        map.put(BuildConfigYouthConnect.DOC_IS_PUBLISHED, 0);
        map.put(BuildConfigYouthConnect.DOC_IS_READ, 0);
        map.put(BuildConfigYouthConnect.DOC_IS_DELETE, 0);
        map.put(BuildConfigYouthConnect.DOC_CREATED, currentTimestamp);
        map.put(BuildConfigYouthConnect.DOC_ASSIGNED_TO_USER_IDS, assigned_to_user_ids);
        try {
            // Save the properties to the document
            document.putProperties(map);
            Log.i(TAG, "Document created.");
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }

        try {
            // Add or update an image to a document as a JPEG attachment:
            UnsavedRevision newRev = document.getCurrentRevision().createRevision();
            if(fileToUploadList != null && fileToUploadList.size() > 0){
                for(int i = 0; i < fileToUploadList.size(); i++){
                    FileToUpload fileToUpload = fileToUploadList.get(i);
                    Uri uri = fileToUpload.getInputStream();
                    String file_name = fileToUpload.getFile_name();
                    String mime_type = fileToUpload.getMime_type();
                    InputStream stream = getContentResolver().openInputStream(uri);
                    newRev.setAttachment(file_name, mime_type, stream);
                }
            }
            newRev.save();
        } catch(FileNotFoundException exception){
            Log.e("FileChooserMultiple", "onActivityResult", exception);
        } catch (CouchbaseLiteException exception){
            Log.e("FileChooserMultiple", "onActivityResult", exception);
        } catch(Exception exception){
            Log.e("FileChooserMultiple", "onActivityResult", exception);
        }

        return documentId;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(final Uri uri) {

        Context context = AttachFileActivity.this;
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    private void clickPic() {
        // Check Camera
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        } else {
            Toast.makeText(getApplication(), "Camera not supported", Toast.LENGTH_LONG).show();
        }
    }

    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    /**
     * To Show Material Alert Dialog
     *
     * @param message
     * @param title
     * */
    private void showAlertDialog(String message, String title, String positiveButtonText, final boolean isSuccess){
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (isSuccess) {
                        finish();
                    }
                }
            });
            if (!((Activity) AttachFileActivity.this).isFinishing()) {
                builder.show();
            }
        } catch (WindowManager.BadTokenException exception){
            Log.e("FileChooserMultiple","showAlertDialog()",exception);
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constants.INTENT_KEY_FILE_PATH, "");
        setResult(RESULT_OK, returnIntent);
        finish();
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
        getMenuInflater().inflate(R.menu.attach_file_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_attach) {
            int cx = (mRevealView.getLeft() + mRevealView.getRight());
//                int cy = (mRevealView.getTop() + mRevealView.getBottom())/2;
            int cy = mRevealView.getTop();

            int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {

                SupportAnimator animator =
                        ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(800);

                SupportAnimator animator_reverse = animator.reverse();

                if (hidden) {
                    mRevealView.setVisibility(View.VISIBLE);
                    animator.start();
                    hidden = false;
                } else {
                    animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
                        @Override
                        public void onAnimationStart() {

                        }

                        @Override
                        public void onAnimationEnd() {
                            mRevealView.setVisibility(View.INVISIBLE);
                            hidden = true;

                        }

                        @Override
                        public void onAnimationCancel() {

                        }

                        @Override
                        public void onAnimationRepeat() {

                        }
                    });
                    animator_reverse.start();

                }
            } else {
                if (hidden) {
                    Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                    mRevealView.setVisibility(View.VISIBLE);
                    anim.start();
                    hidden = false;

                } else {
                    Animator anim = android.view.ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, radius, 0);
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mRevealView.setVisibility(View.INVISIBLE);
                            hidden = true;
                        }
                    });
                    anim.start();

                }
            }

            return true;
        } else if(id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void onActionDoneClick(){

        EditText editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        final String title = editTextTitle.getText().toString().trim();

        if(title == null || title.trim().length() <= 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Attach File");
            builder.setMessage("Provide title for document.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    return;
                }
            });
            builder.show();
            return;
        }

        final String doc_created_by_user_name = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getString(Constants.SP_USER_NAME, "");
        final int doc_created_by_user_id = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getInt(Constants.SP_USER_ID, 0);

        if(fileToUploads == null || fileToUploads.size() <= 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Attach File");
            builder.setMessage("Attach atleast one file.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    return;
                }
            });
            builder.show();
            return;
        }

        new AsyncTask<Void, Void, Void>(){
            private ProgressDialog progressDialog = null;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(AttachFileActivity.this, "File Attachment", "Attaching files...");
            }

            @Override
            protected Void doInBackground(Void... params) {

                List<String> fileNames = new ArrayList<String>();
                if(fileToUploads != null && fileToUploads.size() > 0) {
                    for (FileToUpload fileToUpload : fileToUploads) {
                        if (fileToUpload.getFile_name() != null) {
                            fileNames.add(fileToUpload.getFile_name());
                        }
                    }
                }

                List<AssignedToUSer> assigned_to_user_list = new ArrayList<AssignedToUSer>();

                int userid = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getInt(Constants.SP_USER_ID, 0);
                String username = getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 0).getString(Constants.SP_USER_NAME, "");

                AssignedToUSer assignedToUSer = new AssignedToUSer();
                assignedToUSer.setUser_id(userid);
                assignedToUSer.setUser_name(username);
                assigned_to_user_list.add(assignedToUSer);

                try {
                    createDocument(application.getDatabase(),
                            title, "", doc_created_by_user_name, fileNames, fileToUploads, doc_created_by_user_id, assigned_to_user_list);
                } catch(Exception exception){
                    Log.e(TAG, "onActivityResult()", exception);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(AttachFileActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle("File attachment");
                builder.setMessage("Done successfully.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                builder.show();
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        System.gc();
        super.onDestroy();
    }
}