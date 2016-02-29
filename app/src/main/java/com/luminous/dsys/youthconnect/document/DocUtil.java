package com.luminous.dsys.youthconnect.document;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.luminous.dsys.youthconnect.pojo.FileToUpload;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Android Luminous on 2/29/2016.
 */
public class DocUtil {

    public static final String TAG = "DocUtil";

    public static void addAttachmentToDoc(Document document,
                                          List<FileToUpload> fileToUploadList, Context context){
        try {
            // Add or update an image to a document as a JPEG attachment:
            UnsavedRevision newRev = document.getCurrentRevision().createRevision();
            if(fileToUploadList != null && fileToUploadList.size() > 0){
                for(int i = 0; i < fileToUploadList.size(); i++){
                    FileToUpload fileToUpload = fileToUploadList.get(i);
                    Uri uri = fileToUpload.getInputStream();
                    String file_name = fileToUpload.getFile_name();
                    String mime_type = fileToUpload.getMime_type();
                    InputStream stream = context.getContentResolver().openInputStream(uri);
                    newRev.setAttachment(file_name, mime_type, stream);
                }
            }
            newRev.save();
        } catch(FileNotFoundException exception){
            Log.e(TAG, "addAttachmentToDoc()", exception);
        } catch (CouchbaseLiteException exception){
            Log.e(TAG, "addAttachmentToDoc()", exception);
        } catch(Exception exception){
            Log.e(TAG, "addAttachmentToDoc()", exception);
        }
    }

    public static void removeAttachmentFromDoc(Document document, String file_name){
        try {
            UnsavedRevision newRev = document.getCurrentRevision().createRevision();
            newRev.removeAttachment(file_name);
            // (You could also update newRev.properties while you're here)
            newRev.save();
        } catch (CouchbaseLiteException exception){
            Log.e(TAG, "removeAttachmentFromDoc()", exception);
        } catch(Exception exception){
            Log.e(TAG, "removeAttachmentFromDoc()", exception);
        }
    }
}