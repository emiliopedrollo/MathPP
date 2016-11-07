package br.nom.pedrollo.emilio.mathpp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.nom.pedrollo.emilio.mathpp.entities.Answer;
import br.nom.pedrollo.emilio.mathpp.entities.Question;
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

    public final static String POST_NEW_ID = "NEW_ID";

    private ProgressDialog progress;

    int categoryId;
    int questionId;
    String postType;

    Intent returnIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

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

    private class putQuestionsTask extends AsyncTask<Question,Void,String> {
        @SuppressWarnings("ThrowFromFinallyBlock")
        @Override
        protected String doInBackground(Question... question) {
            HashMap<String, String> putArgs = new HashMap<>();
            putArgs.put("title", question[0].getTitle());
            putArgs.put("text", question[0].getText());
            putArgs.put("author", question[0].getAuthor());
            putArgs.put("author_type", question[0].getAuthorType());
            putArgs.put("author_imei", question[0].getAuthorIMEI());
            putArgs.put("categories", "["+ Integer.toString(categoryId) +"]");

            NetworkUtils.readTimeout = NetworkUtils.MEDIUM_TIMEOUT;
            NetworkUtils.connectionTimeout = NetworkUtils.MEDIUM_TIMEOUT;

            return NetworkUtils.getFromServer(getBaseContext(), getResources().getString(R.string.question_input_uri),
                    NetworkUtils.Method.PUT, putArgs);
        }
    }

    private class putAnswerTask extends AsyncTask<Answer,Void,String> {
        @SuppressWarnings("ThrowFromFinallyBlock")
        @Override
        protected String doInBackground(Answer... answer) {
            HashMap<String, String> putArgs = new HashMap<>();
            putArgs.put("title", answer[0].getTitle());
            putArgs.put("text", answer[0].getText());
            putArgs.put("author", answer[0].getAuthor());
            putArgs.put("author_type", answer[0].getAuthorType());
            putArgs.put("author_imei", answer[0].getAuthorIMEI());
            putArgs.put("question", Integer.toString(questionId));

            NetworkUtils.readTimeout = NetworkUtils.MEDIUM_TIMEOUT;
            NetworkUtils.connectionTimeout = NetworkUtils.MEDIUM_TIMEOUT;

            return NetworkUtils.getFromServer(getBaseContext(), getResources().getString(R.string.answer_input_uri),
                    NetworkUtils.Method.PUT, putArgs);
        }
    }

    private void sendQuestion(){
        ConnectivityManager connMgr = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


        if (networkInfo != null && networkInfo.isConnected()) {

            EditText titleEdit = (EditText) findViewById(R.id.question_title);
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

            // GENERATE BODY STRING
            String boundary = "----"+NetworkUtils.generateBoundary();

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("Boundary: ").append(boundary).append("\r\n");
            stringBuilder.append(boundary).append("\r\n");
            stringBuilder.append("Type: Text").append("\r\n").append("\r\n");
            stringBuilder.append(textEdit.getText().toString()).append("\r\n");
            stringBuilder.append(boundary);

            //TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

            // Todo: Create a common parent class to avoid this redundant mess!

            if (postType.equals(POST_TYPE_QUESTION)){
                final Question question = new Question();
                question.setTitle(  titleEdit.getText().toString()  );
                question.setAuthor( prefs.getString("display_name","Anonymous") );
                question.setAuthorType( prefs.getString("user_category","student") );
                question.setAuthorIMEI( Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) );
                question.setText( stringBuilder.toString() );
                new putQuestionsTask(){
                    @Override
                    protected void onPostExecute(String serverResponse) {
                        super.onPostExecute(serverResponse);
                        returnIntent.putExtra("Title",question.getTitle());
                        returnIntent.putExtra("Body",question.getText());
                        returnIntent.putExtra("Author",question.getAuthor());
                        returnIntent.putExtra("AuthorType",question.getAuthorType());
                        onPostPutExecute(serverResponse);
                    }
                }.execute(question);
            } else {
                Answer answer = new Answer();
                answer.setTitle(  titleEdit.getText().toString()  );
                answer.setAuthor( prefs.getString("display_name","Anonymous") );
                answer.setAuthorType( prefs.getString("user_category","student") );
                answer.setAuthorIMEI( Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) );
                answer.setText( stringBuilder.toString() );
                new putAnswerTask(){
                    @Override
                    protected void onPostExecute(String serverResponse) {
                        super.onPostExecute(serverResponse);
                        onPostPutExecute(serverResponse);
                    }
                }.execute(answer);
            }

        } else {
            Toast.makeText(getBaseContext(),R.string.no_internet_connection,Toast.LENGTH_SHORT).show();
        }
    }

    private void onPostPutExecute(String serverResponse){
        Boolean success = false;
        try{
            JSONObject jsonRoot = new JSONObject(serverResponse);
            if (jsonRoot.getString("status").equals("OK")){
                int newId = jsonRoot.getInt("id");
                progress.dismiss();

                returnIntent.putExtra(POST_NEW_ID,newId);
                success = true;
            }
        } catch (JSONException e){
            Log.e("PARSE_JSON",e.getLocalizedMessage());
            success = false;
        }

        setResult((success)?POST_RESULT_SUCCESSFUL:POST_RESULT_FAILED,returnIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.send_new_post) {
            sendQuestion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
