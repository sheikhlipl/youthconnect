package com.luminous.dsys.youthconnect.asynctask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.luminous.dsys.youthconnect.R;
import com.luminous.dsys.youthconnect.util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Random;

/**
 * Created by Android Luminous on 3/4/2016.
 */
public class PushToAllDeviceAsyncTask extends AsyncTask<Integer, Void, Void> {

    private static final String TAG = "PushToAllDeviceAsyncTask";
    private Context mContext;
    private ProgressDialog progressDialog;

    public PushToAllDeviceAsyncTask(Context context){
        this.mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(mContext, "Push", "Message");
    }

    @Override
    protected Void doInBackground(Integer... params) {

        try {
            int currently_logged_in_user_id = mContext.getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1).getInt(Constants.SP_USER_ID, 0);
            String push_bots_app_id = mContext.getResources().getString(R.string.pb_appid);
            String push_bots_secret_id = mContext.getResources().getString(R.string.pb_secret_id);

            InputStream in = null;
            int resCode = -1;

            String link = Constants.PUSH_REST_API_PUSH_MESSAGE_TO_ALL_DEVICE;
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", "application/json");
            conn.setRequestProperty("X-PUSHBOTS-APPID", push_bots_app_id);
            conn.setRequestProperty("X-PUSHBOTS-SECRET", push_bots_secret_id);
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects(true);

            Random random = new Random();
            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
            osw.write(String.format("{ \"platform\" : 1  , " +
                            "\"alias\" : \"" + "1" + " }",
                    random.nextInt(30), random.nextInt(20)));
            osw.flush();
            osw.close();

            System.err.println(conn.getResponseCode());
            resCode = conn.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                in = conn.getInputStream();
            }
            if(in == null){
                return null;
            }
            BufferedReader reader =new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String response = "",data="";

            while ((data = reader.readLine()) != null){
                response += data + "\n";
            }

            Log.i(TAG, "Response : " + response);

            if(response != null && response.length() > 0){

            }
        } catch(SocketTimeoutException exception){
            Log.e(TAG, "LoginAsync : doInBackground", exception);
        } catch(ConnectException exception){
            Log.e(TAG, "LoginAsync : doInBackground", exception);
        } catch(MalformedURLException exception){
            Log.e(TAG, "LoginAsync : doInBackground", exception);
        } catch (IOException exception){
            Log.e(TAG, "LoginAsync : doInBackground", exception);
        } catch(Exception exception){
            Log.e(TAG, "LoginAsync : doInBackground", exception);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
