package com.luminous.dsys.youthconnect.login;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.luminous.dsys.youthconnect.util.Constants;
import com.pushbots.push.Pushbots;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Android Luminous on 3/10/2016.
 */
public class UserUtil {

    private static final String TAG = "UserUtil";

    public static void registerPushBots(Context context, boolean isLogout){
        try {
            int currently_logged_in_user_id = context.getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1).getInt(Constants.SP_USER_ID, 0);
            String api_key = context.getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1).getString(Constants.SP_USER_API_KEY, "");
            String regId = Pushbots.sharedInstance().regID();
            Log.i(TAG, "Pushbots RegId : " + regId);

            InputStream in = null;
            int resCode = -1;

            String link = Constants.BASE_URL+Constants.REQUEST_URL_REGISTER_DEVICE_TOKEN_TO_SPORTS_SERVER;
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", api_key);
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects(true);

            int _isLogout = 1;
            if(isLogout){
                _isLogout = 0;
            }

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("device_id", regId)
                    .appendQueryParameter("is_register", _isLogout+"")
                    .appendQueryParameter("user_id", currently_logged_in_user_id+"");
            //.appendQueryParameter("deviceid", deviceid);
            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();
            resCode = conn.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                in = conn.getInputStream();
            }
            if(in == null){
                return;
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
    }

    /**
     * @param message_receiver_user_id user_ids with comma separated
     * */
    public static void pushMessageToSportsServer(Context context, String message_receiver_user_id,
                                                 String message, String activity, String fragment){
        try {
            int currently_logged_in_user_id = context.getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1).getInt(Constants.SP_USER_ID, 0);
            String api_key = context.getSharedPreferences(Constants.SHAREDPREFERENCE_KEY, 1).getString(Constants.SP_USER_API_KEY, "");
            String regId = Pushbots.sharedInstance().regID();
            Log.i(TAG, "Pushbots RegId : " + regId);

            InputStream in = null;
            int resCode = -1;

            String link = Constants.BASE_URL+Constants.REQUEST_URL_PUSH_MESSAGE_TO_SPORTS_SERVER;
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", api_key);
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("message", message)
                    .appendQueryParameter("user_id", message_receiver_user_id+"")
                    .appendQueryParameter("activity", activity)
            .appendQueryParameter("fragment", fragment);
            String query = builder.build().getEncodedQuery();

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            conn.connect();
            resCode = conn.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                in = conn.getInputStream();
            }
            if(in == null){
                return;
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
    }
}
