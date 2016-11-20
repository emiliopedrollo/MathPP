package br.nom.pedrollo.emilio.mathpp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import br.nom.pedrollo.emilio.mathpp.entities.Answer;
import br.nom.pedrollo.emilio.mathpp.entities.Post;
import br.nom.pedrollo.emilio.mathpp.entities.Question;
import br.nom.pedrollo.emilio.mathpp.exceptions.ServerFaultException;
import br.nom.pedrollo.emilio.mathpp.utils.NetworkUtils;

public class WritePostActivity extends AppCompatActivity {

    public final static String INTENT_KEY_CATEGORY = "CATEGORY";
    public final static String INTENT_KEY_QUESTION = "QUESTION";
    public final static String INTENT_KEY_POST_TYPE = "POST_TYPE";

    public final static String POST_TYPE_QUESTION = "QUESTION";
    public final static String POST_TYPE_ANSWER = "ANSWER";

    public final static int POST_RESULT_CANCELED = 0;
    public final static int POST_RESULT_SUCCESSFUL = 1;
    public final static int POST_RESULT_FAILED = 2;

    private final int RESULT_CODE_CAMERA = 1;
    private final int RESULT_CODE_GALLERY = 2;

    private final int GRANT_REQUEST_READ_EXTERNAL_STORAGE_FOR_IMAGE = 1;

    public final static String POST_NEW_ID = "NEW_ID";

    private ProgressDialog progress;

    Activity activity;

    FloatingActionsMenu fam;
    FloatingActionButton fab_add_text;
    FloatingActionButton fab_add_image;
    FloatingActionButton fab_add_tex;

    int categoryId;
    int questionId;
    String postType;

    Intent returnIntent;

    LinearLayout postBody;

    String imageFile;

    List<Uri> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        images = new ArrayList<>();

        Intent intent = getIntent();
        postType = intent.getStringExtra(INTENT_KEY_POST_TYPE);
        switch (postType){
            case POST_TYPE_QUESTION:
                categoryId = intent.getIntExtra(INTENT_KEY_CATEGORY, 0);
                break;
            case POST_TYPE_ANSWER:
                questionId = intent.getIntExtra(INTENT_KEY_QUESTION, 0);
                break;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle((postType.equals(POST_TYPE_QUESTION))?
                R.string.new_question:R.string.new_answer);
        setSupportActionBar(toolbar);

        returnIntent = new Intent();

        setupHints();

        activity = this;

        fam = (FloatingActionsMenu) findViewById(R.id.fab_menu);

        fab_add_text = (FloatingActionButton) findViewById(R.id.fab_add_text);
        fab_add_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity,R.string.not_available_yet,Toast.LENGTH_SHORT).show();
                fam.collapse();
            }
        });

        fab_add_tex = (FloatingActionButton) findViewById(R.id.fab_add_tex);
        fab_add_tex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(activity,R.string.not_available_yet,Toast.LENGTH_SHORT).show();
                fam.collapse();
            }
        });

        fab_add_image = (FloatingActionButton) findViewById(R.id.fab_add_image);
        fab_add_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    userLoadImage();
                } else {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                GRANT_REQUEST_READ_EXTERNAL_STORAGE_FOR_IMAGE);

                }
                fam.collapse();
            }
        });

        postBody = (LinearLayout) findViewById(R.id.post_body);

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.content_write_post);
        linearLayout.setClickable(true);
        linearLayout.setFocusable(true);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int childNumber = postBody.getChildCount();
                View lastView = postBody.getChildAt(childNumber-1);
                lastView.requestFocus();
                if (lastView instanceof EditText){
                    ((EditText)lastView).setSelection(((EditText)lastView).getText().length());
                }
            }
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

//        final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
//        //frameLayout.getBackground().setAlpha(0);
//        final FloatingActionsMenu fabMenu = (FloatingActionsMenu) findViewById(R.id.fab_menu);
//        fabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
//            @Override
//            public void onMenuExpanded() {
//                //frameLayout.getBackground().setAlpha(240);
//                frameLayout.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//                        fabMenu.collapse();
//                        return true;
//                    }
//                });
//            }
//
//            @Override
//            public void onMenuCollapsed() {
//                //frameLayout.getBackground().setAlpha(0);
//                frameLayout.setOnTouchListener(null);
//            }
//        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",getResources().getConfiguration().getLocales().get(0)).format(new Date());
        } else {
            timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",getResources().getConfiguration().locale).format(new Date());
        }
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Pictures");

        // Save a file: path for use with ACTION_VIEW intents
        // mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        imageFile = "file:"+image.getAbsolutePath();
        //image = file;

        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case GRANT_REQUEST_READ_EXTERNAL_STORAGE_FOR_IMAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    userLoadImage();

                }
                break;
        }
    }

    private void userLoadImage(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.pick_source)
                .setItems(R.array.image_sources, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                    File photoFile;
                                    Uri photoURI;

                                    try {
                                        photoFile = createImageFile();
                                    } catch (IOException e) {
                                        Log.e("POST_WRITER",e.getMessage());
                                        return;
                                    }
                                    assert photoFile != null;

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        photoURI = FileProvider.getUriForFile(getApplicationContext(),
                                                getApplicationContext().getPackageName()+".fileprovider", photoFile);
                                    } else {
                                        photoURI = Uri.fromFile(photoFile);
                                    }

                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(takePictureIntent, RESULT_CODE_CAMERA);
                                }
                                break;
                            case 1:
                                Intent pickImage = new Intent(Intent.ACTION_GET_CONTENT, null);
                                pickImage.setType("image/*");
                                pickImage.addCategory(Intent.CATEGORY_OPENABLE);

//                                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
//                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(Intent.createChooser(pickImage,getString(R.string.intent_select_image)) , RESULT_CODE_GALLERY);
                                break;
                        }
                    }
                });

        builder.create().show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case RESULT_CODE_CAMERA:
            case RESULT_CODE_GALLERY:
                if(resultCode == RESULT_OK){

                    Uri image;

                    //float scale = getResources().getDisplayMetrics().density;

                    final ImageView imageView = new ImageView(getBaseContext());

                    imageView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));


//                    int padding = (int) (8*scale + 0.5f);
//                    imageView.setPadding(padding,padding,padding,padding);
//                    imageView.setAdjustViewBounds(true);

                    postBody.addView(imageView);

                    imageView.setLongClickable(true);
                    imageView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setMessage(R.string.delete_image)
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            imageView.setVisibility(View.GONE);
                                            postBody.removeView(imageView);
                                            showFab(fab_add_image);
                                            images.remove((int)imageView.getTag());
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {}
                                    });
                            // Create the AlertDialog object and return it
                            builder.create().show();

                            return true;

                        }
                    });

                    if (requestCode == RESULT_CODE_GALLERY){
                        image = data.getData();
                    } else {
                        image = Uri.parse(imageFile);
                    }

                    images.add(image);
                    imageView.setTag(images.indexOf(image));

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
                        Drawable drawable = new BitmapDrawable(getResources(),bitmap);

                        double aspectRatio = (double) drawable.getIntrinsicHeight() / (double) drawable.getIntrinsicWidth();
                        final int targetWidth = ((LinearLayout)imageView.getParent()).getWidth();
                        final int targetHeight = (int) (targetWidth * aspectRatio);

                        imageView.setMinimumHeight(targetHeight);

                        Picasso.with(this).load(image)
                                .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                                .transform(new Transformation() {
                                    @Override
                                    public Bitmap transform(Bitmap source) {

                                        Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                                        if (result != source) {
                                            source.recycle();
                                        }

                                        return result;
                                    }

                                    @Override
                                    public String key() {
                                        return "transformation" + " desiredWidth";
                                    }
                                })
                                .into(imageView);

                        hideFab(fab_add_image);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }

                break;
        }
    }

    private void showAndHideFam(){
        if (fab_add_image.getVisibility() == View.GONE &&
                fab_add_tex.getVisibility() == View.GONE &&
                fab_add_text.getVisibility() == View.GONE) {
            fam.setVisibility(View.GONE);
        } else {
            fam.setVisibility(View.VISIBLE);
        }
    }

    private void showFab(FloatingActionButton fab){
        fab.setVisibility(View.VISIBLE);
        showAndHideFam();
    }

    private void hideFab(FloatingActionButton fab){
        fab.setVisibility(View.GONE);
        showAndHideFam();
    }

    private String createBody(){
        // GENERATE BODY STRING

        EditText textEdit = (EditText) findViewById(R.id.post_text);

        String boundary = "----"+NetworkUtils.generateBoundary();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Boundary: ").append(boundary).append("\r\n");

        // Text Part
        stringBuilder.append(boundary).append("\r\n");
        stringBuilder.append("Type: Text").append("\r\n").append("\r\n");
        stringBuilder.append(textEdit.getText().toString()).append("\r\n");


        // Image Part
        Bitmap bitmap;
        String mimeType;
        ByteArrayOutputStream byteArrayOutputStream;
        String encoded;
        byte[] byteArray;
        for (Uri image: images){
            try{
                bitmap = Picasso.with(this).load(image)
                        .memoryPolicy(MemoryPolicy.NO_CACHE,MemoryPolicy.NO_STORE)
                        .get();
                byteArrayOutputStream = new ByteArrayOutputStream();
                mimeType = GetMimeType(this,image);
                bitmap.compress(getCompressFormat(mimeType),90,byteArrayOutputStream);
                byteArray = byteArrayOutputStream .toByteArray();
                encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

                stringBuilder.append(boundary).append("\r\n");
                stringBuilder.append("Type: Image").append("\r\n");
                stringBuilder.append("Mime: ").append(mimeType).append("\r\n").append("\r\n");
                stringBuilder.append(encoded);

            } catch (IOException e){
                e.printStackTrace();
            }

        }

        stringBuilder.append(boundary);

        return stringBuilder.toString();
    }

    private static String GetMimeType(Context context, Uri uriImage)
    {
        String strMimeType = null;

        Cursor cursor = context.getContentResolver().query(uriImage,
                new String[] { MediaStore.MediaColumns.MIME_TYPE },
                null, null, null);

        if (cursor != null) {
            if (cursor.moveToNext()){
                strMimeType = cursor.getString(0);
            }
            cursor.close();
        }

        return strMimeType;
    }

    private static Bitmap.CompressFormat getCompressFormat(String mimeType){
        switch (mimeType){
            case "image/jpeg":
            case "image/jpg":
                return Bitmap.CompressFormat.JPEG;
            case "image/png":
                return Bitmap.CompressFormat.PNG;
            case "image/webp":
                return Bitmap.CompressFormat.WEBP;
            default:
                return Bitmap.CompressFormat.JPEG;
        }
    }

    private void setupHints(){
        EditText textEdit = (EditText) findViewById(R.id.post_text);
        switch (postType){
            case POST_TYPE_QUESTION:
                textEdit.setHint(R.string.white_question_body_hint);
                break;
            case POST_TYPE_ANSWER:
                textEdit.setHint(R.string.white_answer_body_hint);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post_writer_actions, menu);

        return true;
    }

    private class PutTaskResult{
        public NetworkUtils.ServerResponse response;
        public Post post;
    }

    private class PutTask extends AsyncTask<Post,Void,PutTaskResult> {
        @Override
        @Nullable
        protected PutTaskResult doInBackground(Post... posts) {
            Post post = posts[0];
            String serverUri;

            HashMap<String, String> putArgs = new HashMap<>();
            putArgs.put("title", post.getTitle());
            putArgs.put("text", post.getText());
            putArgs.put("author", post.getAuthor());
            putArgs.put("author_type", post.getAuthorType());
            putArgs.put("author_imei", post.getAuthorIMEI());


            if (post instanceof Question){
                serverUri = getResources().getString(R.string.question_input_uri);
                putArgs.put("categories", "["+ Integer.toString(categoryId) +"]");
            } else {
                serverUri = getResources().getString(R.string.answer_input_uri);
                putArgs.put("question", Integer.toString(questionId));
            }

            NetworkUtils.readTimeout = NetworkUtils.MEDIUM_TIMEOUT;
            NetworkUtils.connectionTimeout = NetworkUtils.MEDIUM_TIMEOUT;

            PutTaskResult putTaskResult = new PutTaskResult();




            putTaskResult.response = NetworkUtils.getFromServer(getBaseContext(), serverUri,
                    NetworkUtils.Method.PUT, putArgs);

            putTaskResult.post = post;

            return putTaskResult;
        }
    }

    private void sendPost(){
        ConnectivityManager connMgr = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


        if (networkInfo != null && networkInfo.isConnected()) {

            EditText titleEdit = (EditText) findViewById(R.id.post_title);
            EditText textEdit = (EditText) findViewById(R.id.post_text);

            if (titleEdit.getText().toString().isEmpty()) {
                Toast.makeText(getBaseContext(),
                        ((postType.equals(POST_TYPE_QUESTION))?
                                R.string.empty_question_title:R.string.empty_answer_title),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (textEdit.getText().toString().isEmpty()) {
                Toast.makeText(getBaseContext(),
                        ((postType.equals(POST_TYPE_QUESTION))?
                                R.string.empty_question_body:R.string.empty_answer_body),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            progress = ProgressDialog.show(this, getString(
                    ((postType.equals(POST_TYPE_QUESTION))?
                            R.string.sending_question_dialog_title:R.string.sending_answer_dialog_title)),
                    getString(R.string.sending_post_dialog_message), true);

            //TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

            final Post post;
            if (postType.equals(POST_TYPE_QUESTION)) {
                post = new Question();
            } else {
                post = new Answer();
            }

            post.setTitle(  titleEdit.getText().toString()  );
            post.setAuthor( prefs.getString("display_name","Anonymous") );
            post.setAuthorType( prefs.getString("user_category","student") );
            post.setAuthorIMEI( Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) );
//                question.setText( stringBuilder.toString() );
            new PutTask(){

                @Override
                protected void onPreExecute() {
                    post.setText(createBody());
                }

                @Override
                protected void onPostExecute(@Nullable PutTaskResult putTaskResult) {
                    super.onPostExecute(putTaskResult);

                    if (putTaskResult == null) {
                        onPostPutExecute(POST_RESULT_FAILED);
                        return;
                    }

                    if (putTaskResult.post instanceof Question){
                        returnIntent.putExtra("Title",putTaskResult.post.getTitle());
                        returnIntent.putExtra("Body",putTaskResult.post.getText());
                        returnIntent.putExtra("Author",putTaskResult.post.getAuthor());
                        returnIntent.putExtra("AuthorType",putTaskResult.post.getAuthorType());
                    }

                    try{
                        if (putTaskResult.response.getCode() != 200)
                            throw new ServerFaultException("Server has returned error code "+
                                            putTaskResult.response.getCode());

                        JSONObject jsonRoot = new JSONObject(putTaskResult.response.getBody());
                        if (jsonRoot.getString("status").equals("OK")){
                            int newId = jsonRoot.getInt("id");
                            progress.dismiss();

                            returnIntent.putExtra(POST_NEW_ID,newId);
                            onPostPutExecute(POST_RESULT_SUCCESSFUL);
                        }
                    } catch (JSONException|ServerFaultException e){
                        Log.e("PARSE_JSON",e.getMessage());
                        onPostPutExecute(POST_RESULT_FAILED);
                    }
                }

            }.execute(post);

        } else {
            Toast.makeText(getBaseContext(),R.string.no_internet_connection,Toast.LENGTH_SHORT).show();
        }
    }

    private void onPostPutExecute(int result){
        setResult(result,returnIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.send_new_post) {
            sendPost();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
