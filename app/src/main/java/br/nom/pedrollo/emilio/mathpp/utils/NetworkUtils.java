package br.nom.pedrollo.emilio.mathpp.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


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

    @SuppressWarnings("WeakerAccess")
    @NonNull
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
    public static String getFromServer(Context context, String uri){
        return getFromServer(context,uri,Method.GET,new HashMap<String, String>());
    }

    @Nullable
    public static String getFromServer(Context context, String uri, Method method,
                                       HashMap<String, String> params){
        try{
            InputStream is = null;
            OutputStreamWriter osw;
            URL url;

            String host = context.getResources().getString(R.string.fetch_hostname);

            try{
                if (method == Method.GET && params.size() > 0){
                    url = new URL(host + uri + "?" + getEncodedDataString(params));
                } else {
                    url = new URL(host + uri);
                }
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                assert conn != null;
                conn.setReadTimeout(readTimeout);
                conn.setConnectTimeout(connectionTimeout);
                switch (method){
                    case PUT:
                    case POST:
                        conn.setRequestMethod("POST");
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
                conn.setRequestMethod(method.getValue());
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Accept-Language", "UTF-8");
                conn.setRequestProperty("Content-type", "application/json;charset=UTF-8");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d("GET_FROM_SERVER", "The HTTP Response is: "+response);
                is = conn.getInputStream();

                return readIt(is);

            } catch (AssertionError e){
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


}
