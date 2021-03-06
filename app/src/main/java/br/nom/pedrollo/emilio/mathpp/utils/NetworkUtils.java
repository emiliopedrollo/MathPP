package br.nom.pedrollo.emilio.mathpp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


import br.nom.pedrollo.emilio.mathpp.BuildConfig;
import br.nom.pedrollo.emilio.mathpp.R;


public class NetworkUtils {

    @SuppressWarnings("WeakerAccess")
    public final static int LONG_TIMEOUT;
    @SuppressWarnings("WeakerAccess")
    public final static int MEDIUM_TIMEOUT;
    @SuppressWarnings("WeakerAccess")
    public final static int SHORT_TIMEOUT;

    static {
        SHORT_TIMEOUT = 1000;
        MEDIUM_TIMEOUT = 5000;
        LONG_TIMEOUT = 10000;
    }

    public static int readTimeout = 10000;
    public static int connectionTimeout = 10000;

    public enum Method {
        GET("GET"), POST("POST"), PUT("PUT");

        private String value;

        Method(String s){
            this.value = s;
        }

        public String getValue(){
            return value;
        }
    }

    public static class ServerResponse {
        private int code;
        private String body;

        ServerResponse(String body, int code){
            this.body = body;
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public String getBody() {
            return body;
        }
    }

    @NonNull
    private static String readIt(InputStream is) throws IOException{
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                try {
                    is.close();
                } catch (IOException e){
                    Log.e("READ_INPUT_STREAM","Error while closing InputStream object: "+e.getMessage());
                }
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    @NonNull
    @SuppressWarnings("WeakerAccess")
    public static String getEncodedDataString(HashMap<String,String> params)
            throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first) first = false; else result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    @Nullable
    @SuppressWarnings("unused")
    public static ServerResponse getFromServer(Context context, String uri){
        return getFromServer(context,uri,Method.GET,new HashMap<String, String>());
    }

    @Nullable
    public static ServerResponse getFromServer(Context context, String uri, Method method,
                                       HashMap<String, String> params){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean dev = prefs.getBoolean(context.getString(R.string.pref_key_enable_dev_server),false);
        Boolean xdebug = prefs.getBoolean(context.getString(R.string.pref_key_enable_dev_server_xdebug),false);

        try {
            InputStream is = null;
            OutputStreamWriter osw;
            String host;
            URL url;

            if (dev) {
                if (xdebug){
                    host = context.getResources().getString(R.string.fetch_hostname_debug);
                } else{
                    host = context.getResources().getString(R.string.fetch_hostname_dev);
                }
            } else {
                host = context.getResources().getString(R.string.fetch_hostname_prod);
            }

            try {
                if (method == Method.GET && params.size() > 0) {
                    url = new URL(host + uri + "?" + getEncodedDataString(params));
                } else {
                    url = new URL(host + uri);
                }
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                assert conn != null;

                if (xdebug) {
                    final String COOKIES_HEADER = "Set-Cookie";
                    java.net.CookieManager cookieManager = new java.net.CookieManager();
                    cookieManager.getCookieStore().add(null, new HttpCookie("XDEBUG_SESSION", "PHPSTORM"));

                    if (cookieManager.getCookieStore().getCookies().size() > 0) {
                        // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                        conn.setRequestProperty("Cookie",
                                TextUtils.join(";", cookieManager.getCookieStore().getCookies()));
                    }
                    conn.setReadTimeout(600000); // 10 minute
                    conn.setConnectTimeout(600000); // 10 minute
                } else {
                    conn.setReadTimeout(readTimeout);
                    conn.setConnectTimeout(connectionTimeout);
                }


                conn.setRequestMethod(method.getValue());
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Accept-Language", "UTF-8");
                conn.setRequestProperty("Content-type", "application/json;charset=UTF-8");
                conn.setDoInput(true);
                switch (method) {
                    case PUT:
                    case POST:
                        conn.setDoOutput(true);
                        osw = new OutputStreamWriter(conn.getOutputStream());
                        osw.write(getEncodedDataString(params));
                        //osw.flush();
                        osw.close();
                        break;
                    case GET:
                    default:
                        break;
                }
                conn.connect();
                int code = conn.getResponseCode();
                Log.d("GET_FROM_SERVER", "The HTTP Response is: " + code);
                String response;
                try {
                    if (code == 200)
                        is = conn.getInputStream();
                    else
                        is = conn.getErrorStream();
                    response = readIt(is);
                } catch (IOException e){
                    response = null;
                }
                return new ServerResponse(response,code);

            } catch (AssertionError e) {
                Log.e("GET_FROM_SERVER", "Could not open connection");
            } finally {
                try {
                    if (is != null) is.close();
                } catch (IOException e){
                    Log.e("GET_FROM_SERVER","Error while closing InputStream object: "+
                            e.getMessage());
                }
            }

        } catch (IOException e){
            Log.e("GET_FROM_SERVER",e.getLocalizedMessage());
        }
        return null;
    }

    public static class OnGetJSONFromServerResponseEvents{
        public void onJsonObjectFound(JSONObject jsonObject){}
        public void onStatusIsAnError(JSONObject json){}
        public void onFail(){}
        public void onFinish(Boolean success){}
    }

    public static void getJSONObjectsFromServerResponse(@NotNull String serverResponse,
                                                        OnGetJSONFromServerResponseEvents onGetJSONFromServerResponseEvents){
        Boolean finishedSuccessfully = false;
        try{
            JSONObject jsonRoot = new JSONObject(serverResponse);
            if (jsonRoot.getString("status").equals("OK")){
                if (jsonRoot.has("result")){
                    JSONArray resultArray = jsonRoot.getJSONArray("result");
                    for (int i=0; i < resultArray.length(); i++) {
                        onGetJSONFromServerResponseEvents.onJsonObjectFound(resultArray.getJSONObject(i));
                    }
                }
                finishedSuccessfully = true;
            } else {
                onGetJSONFromServerResponseEvents.onStatusIsAnError(jsonRoot);
            }
        } catch (JSONException|NullPointerException e){
            onGetJSONFromServerResponseEvents.onFail();
            Log.e("PARSE_JSON",e.getLocalizedMessage());
        } finally {
            onGetJSONFromServerResponseEvents.onFinish(finishedSuccessfully);
        }


    }

    private final static char[] MULTIPART_CHARS =
            "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();

    public static String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 20; // a random size from 20 to 30
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }
}
