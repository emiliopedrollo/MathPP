package br.nom.pedrollo.emilio.mathpp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class IOUtils {


    public static class CheckResourceExistsOnURITask extends AsyncTask<String,Void,Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                HttpsURLConnection.setFollowRedirects(false);
                // note : you may also need
                //        HttpURLConnection.setInstanceFollowRedirects(false)
                HttpsURLConnection con =
                        (HttpsURLConnection) new URL(params[0]).openConnection();
                con.setRequestMethod("HEAD");
                return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            }
            catch (IOException e) {
                Log.w("CHECK_RESOURCE_URI",e.getLocalizedMessage());
            }
            return false;

        }
    }

    public static class DownloadImageTask extends AsyncTask<String,Void,Bitmap>{
        ImageView imageView;

        public DownloadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String resourceURI = urls[0];
            Bitmap image = null;
            try {
                InputStream in = new java.net.URL(resourceURI).openStream();
                image = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

}
