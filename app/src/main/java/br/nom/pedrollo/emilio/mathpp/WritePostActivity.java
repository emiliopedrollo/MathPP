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
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.nom.pedrollo.emilio.mathpp.entities.Question;
import br.nom.pedrollo.emilio.mathpp.fragments.QuestionsListFragment;
import br.nom.pedrollo.emilio.mathpp.utils.NetworkUtils;

public class WritePostActivity extends AppCompatActivity {

    private ProgressDialog progress;

    int categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.new_question);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        categoryId = intent.getIntExtra("Category",0);


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.question_actions, menu);

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

    private void sendQuestion(){


        ConnectivityManager connMgr = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();


        if (networkInfo != null && networkInfo.isConnected()) {

            EditText titleEdit = (EditText) findViewById(R.id.question_title);
            EditText textEdit = (EditText) findViewById(R.id.question_text);

            if (titleEdit.getText().toString().isEmpty()) {
                Toast.makeText(getBaseContext(),"Please enter question title...",Toast.LENGTH_SHORT).show();
                return;
            }

            if (textEdit.getText().toString().isEmpty()) {
                Toast.makeText(getBaseContext(),"Please enter question...",Toast.LENGTH_SHORT).show();
                return;
            }

            progress = ProgressDialog.show(this, getString(R.string.sending_question_dialog_title),
                    getString(R.string.sending_question_dialog_message), true);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

            Question question = new Question();
            question.setTitle(  titleEdit.getText().toString()  );
            question.setAuthor( prefs.getString("display_name","Anonymous") );

            switch (prefs.getString("user_category","0")){
                case "0":
                    question.setAuthorType( "student" );
                    break;
                case "1":
                    question.setAuthorType( "monitor" );
                    break;
                case "2":
                    question.setAuthorType( "teacher" );
                    break;
            }

            question.setAuthorIMEI( Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID) );

            String boundary = "----"+NetworkUtils.generateBoundary();

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("Boundary: ").append(boundary).append("\r\n");
            stringBuilder.append(boundary).append("\r\n");
            stringBuilder.append("Type: Text").append("\r\n").append("\r\n");
            stringBuilder.append(textEdit.getText().toString()).append("\r\n");
            stringBuilder.append(boundary);

            question.setText( stringBuilder.toString() );

            new putQuestionsTask(){
                @Override
                protected void onPostExecute(String serverResponse) {
                    super.onPostExecute(serverResponse);

                    try{
                        JSONObject jsonRoot = new JSONObject(serverResponse);
                        if (jsonRoot.getString("status").equals("OK")){
                            int newId = jsonRoot.getInt("id");
                            progress.dismiss();
                        }
                    } catch (JSONException e){
                        Log.e("PARSE_JSON",e.getLocalizedMessage());
                    }

                }
            }.execute(question);

        } else {
            Toast.makeText(getBaseContext(),R.string.no_internet_connection,Toast.LENGTH_SHORT).show();
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.send_new_question) {
            sendQuestion();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
