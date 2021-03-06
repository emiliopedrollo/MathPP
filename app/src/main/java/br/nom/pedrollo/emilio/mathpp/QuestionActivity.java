package br.nom.pedrollo.emilio.mathpp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.nom.pedrollo.emilio.mathpp.adapters.AnswersAdapter;
import br.nom.pedrollo.emilio.mathpp.contracts.VotesContract;
import br.nom.pedrollo.emilio.mathpp.contracts.VotesContract.VoteEntry;
import br.nom.pedrollo.emilio.mathpp.entities.Answer;
import br.nom.pedrollo.emilio.mathpp.utils.NetworkUtils;
import br.nom.pedrollo.emilio.mathpp.utils.transitions.ExpandTransition;

public class QuestionActivity extends AppCompatActivity {


    public static final String MESSAGE_QUESTION_ID = "QUESTION_ID";

    public static final String MESSAGE_NEW_QUESTION = "NEW_QUESTION";
    public static final String MESSAGE_QUESTION_TITLE = "QUESTION_TITLE";
    public static final String MESSAGE_QUESTION_BODY = "QUESTION_TEXT";
    public static final String MESSAGE_QUESTION_AUTHOR = "QUESTION_AUTHOR";
    public static final String MESSAGE_QUESTION_AUTHOR_TYPE = "QUESTION_AUTHOR_TYPE";
    public static final String MESSAGE_QUESTION_N_ANSWERS = "QUESTION_N_ANSWERS";

    Boolean alreadyLoadedCategoriesOnce = false;
    int questionId;

    String TAG = "==XX==";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.question);
        setSupportActionBar(toolbar);

        final Activity activity = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),WritePostActivity.class);
                intent.putExtra(WritePostActivity.INTENT_KEY_QUESTION,questionId);
                intent.putExtra(WritePostActivity.INTENT_KEY_POST_TYPE,WritePostActivity.POST_TYPE_ANSWER);
                ActivityCompat.startActivityForResult(activity,intent,1,null);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        handleIntent(getIntent());

        loadAnswers();

        //setupTransition();


        //TransitionHelper.fixSharedElementTransitionForStatusAndNavigationBar(this);
        //TransitionHelper.setSharedElementEnterTransition(this, R.transition.expand);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == WritePostActivity.POST_RESULT_SUCCESSFUL){
            loadAnswers();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupTransition() {
        TransitionSet transitionSet = new TransitionSet();
        transitionSet.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);

        ExpandTransition elevation = new ExpandTransition();
        elevation.addTarget(R.id.content_area);
        elevation.setDuration(2000);

        // FIXME: Por alguma razão as proximas 2 linhas são ignoradas pelo android.
        transitionSet.addTransition(elevation);
        transitionSet.addTransition(new ChangeBounds());

        transitionSet.excludeTarget(android.R.id.statusBarBackground, true);
        transitionSet.excludeTarget(android.R.id.navigationBarBackground, true);
        transitionSet.excludeTarget(R.id.toolbar,true);

        getWindow().setEnterTransition(transitionSet);
        getWindow().setSharedElementsUseOverlay(false);

    }

    private class getAnswersTask extends AsyncTask<Void,Void,String> {
        @SuppressWarnings("ThrowFromFinallyBlock")
        @Nullable
        @Override
        protected String doInBackground(Void... params) {
            HashMap<String, String> getParams = new HashMap<>();

            NetworkUtils.readTimeout = NetworkUtils.MEDIUM_TIMEOUT;
            NetworkUtils.connectionTimeout = NetworkUtils.MEDIUM_TIMEOUT;

            String uri = getResources().getString(R.string.answers_fetch_uri) +
                    "/" + Integer.toString(questionId);

            NetworkUtils.ServerResponse response = NetworkUtils.getFromServer(getBaseContext(), uri,
                    NetworkUtils.Method.GET, getParams);

            return (response != null) ? response.getBody() : null;

        }
    }

    private void loadAnswers() {

        final RecyclerView answerList = (RecyclerView) findViewById(R.id.question_answers);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        answerList.setLayoutManager(linearLayoutManager);
        answerList.setAdapter(new AnswersAdapter());

        answerList.setNestedScrollingEnabled(false);

        final RelativeLayout questionsAnswersPlaceholder = (RelativeLayout) findViewById(R.id.question_answers_placeholder);
        final TextView questionsAnswersPlaceholderTextView = (TextView) findViewById(R.id.question_answers_placeholder_text_view);

        ImageButton star = (ImageButton) findViewById(R.id.question_star);

        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag() == TAG) {
                    ((ImageButton) v).setImageResource(R.drawable.ic_star_border_black_36dp);
                    v.setTag(null);
                } else {
                    ((ImageButton) v).setImageResource(R.drawable.ic_star_black_36dp);
                    v.setTag(TAG);
                }
            }
        });

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            questionsAnswersPlaceholder.setVisibility(View.VISIBLE);
            questionsAnswersPlaceholderTextView.setText(getResources().getString(R.string.loading_answers));

            new getAnswersTask(){
                @Override
                protected void onPostExecute(@Nullable String serverResponse) {
                    super.onPostExecute(serverResponse);

                    if (serverResponse == null){
                        Log.e("QUESTION_ACTIVITY","serverResponse is null at onPostExecute");
                        return;
                    }

                    NetworkUtils.getJSONObjectsFromServerResponse(serverResponse, new NetworkUtils.OnGetJSONFromServerResponseEvents() {
                        @Override
                        public void onJsonObjectFound(JSONObject jsonObject) {
                            try{

                                Boolean foundExisting = false;
                                Integer id = jsonObject.getInt("id");
                                String title = jsonObject.getString("title");
                                String text = jsonObject.getString("text");
                                String author = jsonObject.getString("author");
                                String authorType = jsonObject.getString("authorType");
                                int score = jsonObject.getInt("score");

                                Answer answer = new Answer(id,title,text,author,authorType,score);
                                AnswersAdapter adapter = (AnswersAdapter) answerList.getAdapter();

                                for (int i=0; i < adapter.answers.size(); i++){
                                    if (adapter.answers.get(i).getId() ==  answer.getId() ){
                                        foundExisting = true;
                                        adapter.answers.set(i,answer);
                                        break;
                                    }
                                }
                                if (!foundExisting){
                                    adapter.answers.add(answer);
                                }

                            } catch (JSONException e){
                                Log.e("PARSE_JSON",e.getLocalizedMessage());
                            }
                        }

                        @Override
                        public void onFinish(Boolean success) {
                            AnswersAdapter adapter = (AnswersAdapter) answerList.getAdapter();

                            if (success){
                                if (adapter.answers.size() > 0){
                                    alreadyLoadedCategoriesOnce = true;
                                    questionsAnswersPlaceholder.setVisibility(View.GONE);
                                    answerList.setVisibility(View.VISIBLE);
                                } else {
                                    answerList.setVisibility(View.GONE);
                                    questionsAnswersPlaceholder.setVisibility(View.VISIBLE);
                                    questionsAnswersPlaceholderTextView.setText(getResources().getString(R.string.no_answers_available));
                                }

                                Collections.sort(adapter.answers, new Comparator<Answer>() {
                                    @Override
                                    public int compare(Answer o1, Answer o2) {
                                        if (o1.getScore() < o2.getScore()){
                                            return 1;
                                        } else if (o1.getScore() > o2.getScore()){
                                            return -1;
                                        } else {
                                            return (o1.getId() < o2.getId())?1:-1;
                                        }
                                    }
                                });

                                adapter.notifyDataSetChanged();
                            } else {
                                answerList.setVisibility(View.GONE);
                                questionsAnswersPlaceholder.setVisibility(View.VISIBLE);
                                questionsAnswersPlaceholderTextView.setText(getResources().getString(R.string.unable_retrieve_answers));
                            }
                        }
                    });
                }
            }.execute();


        } else {
            questionsAnswersPlaceholderTextView.setText(getResources().getString(R.string.no_internet_connection));
            questionsAnswersPlaceholder.setVisibility(View.VISIBLE);
        }

        ((AnswersAdapter)answerList.getAdapter()).setOnBindViewHolder(new AnswersAdapter.OnBindViewHolder() {
            @Override
            public void onBindViewHolder(AnswersAdapter.ViewHolder holder, int position) {

                final AnswersAdapter.ViewHolder viewHolder = holder;

                AnswersAdapter adapter = (AnswersAdapter) answerList.getAdapter();

                final Answer answer = adapter.answers.get(position);

                createBody(holder.answerBody,answer.getText(),BodyType.ANSWER);

                final OnVoteInterface onVoteInterface = new OnVoteInterface() {
                    @Override
                    public void onVoteButtonClick(int value, ImageButton button) {
                        if (button.getColorFilter() != null){
                            button.setColorFilter(null);
                            vote(0);
                        } else {
                            viewHolder.thumbsUp.setColorFilter(null);
                            viewHolder.thumbsDown.setColorFilter(null);
                            button.setColorFilter(ContextCompat
                                    .getColor(getApplicationContext(),R.color.colorAccent));
                            vote(value);
                        }
                    }
                    private void vote(final int value){
                        (new AsyncTask<Void,Void,String>(){
                            @Override
                            protected String doInBackground(Void... params) {

                                VotesContract.VotesDbHelper dbHelper =
                                        new VotesContract.VotesDbHelper(getBaseContext());
                                SQLiteDatabase db = dbHelper.getWritableDatabase();

                                // Query existent entry
                                String[] projection = {
                                        VoteEntry._ID
                                };

                                String selection = VoteEntry.COLUMN_NAME_ANSWER_ID + " = ?";
                                String[] selectionArgs = { String.valueOf(answer.getId()) };
                                Cursor cursor = db.query(
                                        VoteEntry.TABLE_NAME,                     // The table to query
                                        projection,                               // The columns to return
                                        selection,                                // The columns for the WHERE clause
                                        selectionArgs,                            // The values for the WHERE clause
                                        null,                                     // don't group the rows
                                        null,                                     // don't filter by row groups
                                        null                                      // The sort order
                                );

                                // Delete existing entries
                                if (cursor.getCount() > 0){
                                    String deletion = VoteEntry.COLUMN_NAME_ANSWER_ID + " = ?";
                                    String[] deletionArgs = { String.valueOf(answer.getId()) };
                                    db.delete(VoteEntry.TABLE_NAME, selection, deletionArgs);
                                }

                                cursor.close();


                                // Add new Entry
                                ContentValues values = new ContentValues();
                                values.put(VoteEntry.COLUMN_NAME_ANSWER_ID, answer.getId());
                                values.put(VoteEntry.COLUMN_NAME_VALUE, value);
                                db.insert(VoteEntry.TABLE_NAME,null,values);

                                return null;
                            }
                        }).execute();
                    }
                };

                View.OnClickListener onUpVote = new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        onVoteInterface.onVoteButtonClick(1,(ImageButton)v);
                    }
                };
                View.OnClickListener onDownVote = new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        onVoteInterface.onVoteButtonClick(-1,(ImageButton)v);
                    }
                };


                VotesContract.VotesDbHelper dbHelper =
                        new VotesContract.VotesDbHelper(getBaseContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                // Query existent entry
                String[] projection = {
                        VoteEntry.COLUMN_NAME_VALUE
                };

                String selection = VoteEntry.COLUMN_NAME_ANSWER_ID + " = ?";
                String[] selectionArgs = { String.valueOf(answer.getId()) };
                Cursor cursor = db.query(
                        VoteEntry.TABLE_NAME,                     // The table to query
                        projection,                               // The columns to return
                        selection,                                // The columns for the WHERE clause
                        selectionArgs,                            // The values for the WHERE clause
                        null,                                     // don't group the rows
                        null,                                     // don't filter by row groups
                        null                                      // The sort order
                );

                if (cursor.getCount() > 0){
                    cursor.moveToFirst();
                    int vote = cursor.getInt(cursor.getColumnIndexOrThrow(VoteEntry.COLUMN_NAME_VALUE));

                    if (vote > 0){
                        holder.thumbsUp.setColorFilter(ContextCompat
                                .getColor(getApplicationContext(),R.color.colorAccent));
                    } else if (vote < 0) {
                        holder.thumbsDown.setColorFilter(ContextCompat
                                .getColor(getApplicationContext(),R.color.colorAccent));
                    }
                }
                cursor.close();


                holder.thumbsUp.setOnClickListener(onUpVote);
                holder.thumbsDown.setOnClickListener(onDownVote);

            }
        });

    }

    private interface OnVoteInterface{
        void onVoteButtonClick(int value, ImageButton button);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public enum BodyPartType{
        NONE(""), TEXT("Text"), IMAGE("Image");

        private String value;

        BodyPartType(String value){
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public class BodyPart{
        private final BodyPartType type;
        private final String content;

        BodyPart(BodyPartType type, String content){
            this.type = type;
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public BodyPartType getType() {
            return type;
        }
    }

    public enum BodyType{
        QUESTION, ANSWER
    }

    @Nullable
    private ArrayList<BodyPart> parseBody(String body){
        BufferedReader reader = new BufferedReader(new StringReader(body));
        Pattern boundaryPattern = Pattern.compile("Boundary: (.*)");
        Pattern typePattern = Pattern.compile("Type: (.*)");
        Matcher matcher;
        String boundary;
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        BodyPartType currentPartType = BodyPartType.NONE;
        Boolean contentHasStarted = false;

        ArrayList<BodyPart> parts = new ArrayList<>();

        try{
            line = reader.readLine();
            if (line.startsWith("Boundary:")){
                matcher = boundaryPattern.matcher(line);
                if (matcher.find()){
                    boundary = matcher.group(1);
                } else {
                    throw new Exception("Malformed Body");
                }
            } else {
                parts.add(new BodyPart(BodyPartType.TEXT,body));
                return parts;
            }

            stringBuilder.setLength(0);

            while ((line = reader.readLine()) != null) {
                if (line.equals(boundary)){
                    if (stringBuilder.length() > 0){
                        parts.add(new BodyPart(currentPartType,stringBuilder.toString()));
                        stringBuilder.setLength(0);
                        contentHasStarted = false;
                    }
                } else {
                    if (!contentHasStarted){
                        if (line.startsWith("Type:")){
                            matcher = typePattern.matcher(line);
                            if (matcher.find()){
                                switch (matcher.group(1)) {
                                    case "Text":
                                        currentPartType = BodyPartType.TEXT;
                                        break;
                                    case "Image":
                                        currentPartType = BodyPartType.IMAGE;
                                        break;
                                }
                            }
                        } else if (line.equals("")){
                            contentHasStarted = true;
                        }
                    } else {
                        stringBuilder.append(line);
                    }
                }
            }

            return parts;
        } catch (Exception e) {
            Log.e("PARSE_POST_TEXT",e.getMessage());
            return null;
        }
    }

    private void createBody(ViewGroup container, String body, BodyType bodyType){
        ArrayList<BodyPart> parts = parseBody(body);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean dev = prefs.getBoolean(this.getString(R.string.pref_key_enable_dev_server),false);

        float scale = getResources().getDisplayMetrics().density;

        assert parts != null;
        for(BodyPart entry : parts){
            switch (entry.getType()){
                case TEXT:
                    TextView textView = new TextView(getBaseContext());
                    textView.setText(entry.getContent());

                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

                    if (bodyType == BodyType.ANSWER) {
                        layoutParams.setMargins(0, (int) (8 * scale + 0.5f), 0, 0);
                    }

                    textView.setLayoutParams(layoutParams);

                    textView.setTextSize(14);
                    textView.setLineSpacing(0f,1.1f);

                    textView.setTypeface(Typeface.SANS_SERIF, Typeface.NORMAL);

                    if (bodyType == BodyType.QUESTION){
                        textView.setTextColor(ContextCompat.getColor(
                                getApplicationContext(),android.R.color.tertiary_text_light));
                    } else {
                        textView.setTextColor(ContextCompat.getColor(
                                getApplicationContext(),android.R.color.secondary_text_light));
                        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);

                    }

                    container.addView(textView);
                    break;
                case IMAGE:
                    StringBuilder stringBuilder = new StringBuilder();
                    ImageView imageView = new ImageView(getBaseContext());

                    imageView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));

                    int padding = (int) (8*scale + 0.5f);
                    imageView.setPadding(padding,padding,padding,padding);
                    imageView.setAdjustViewBounds(true);

                    container.addView(imageView);

                    final ViewGroup viewGroup = container;

                    if (dev){
                        stringBuilder.append(getResources().getString(R.string.fetch_hostname_dev));
                    } else {
                        stringBuilder.append(getResources().getString(R.string.fetch_hostname_prod));
                    }
                    stringBuilder.append(entry.getContent());

                    Picasso.with(getBaseContext()).load(stringBuilder.toString())
                        .transform(new Transformation() {
                            @Override
                            public Bitmap transform(Bitmap source) {

                                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                                int targetWidth = viewGroup.getWidth();

                                while (targetWidth == 0){
                                    synchronized (viewGroup){
                                        targetWidth = viewGroup.getWidth();
                                    }
                                }

                                int targetHeight = (int) (targetWidth * aspectRatio);

//                                if (targetHeight <= 0 || targetWidth <= 0) return null;

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
                        }).into(imageView);
                    break;
            }
        }
    }

    private void handleIntent(Intent intent){
        TextView questionAuthor = (TextView) findViewById(R.id.question_author);
        TextView questionTitle = (TextView) findViewById(R.id.post_title);
        TextView questionAnswerNumber = (TextView) findViewById(R.id.answer_item_score);

        questionId = intent.getIntExtra(MESSAGE_QUESTION_ID,-1);

        LinearLayout questionBody = (LinearLayout) findViewById(R.id.question_body);


        switch (intent.getStringExtra(MESSAGE_QUESTION_AUTHOR_TYPE)){
            case "student":
                questionAuthor.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                                getBaseContext(),R.drawable.ic_account_outline),
                        null,null,null);
                break;
            case "monitor":
                questionAuthor.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                                getBaseContext(),R.drawable.ic_account),
                        null,null,null);
                break;
            case "teacher":
                questionAuthor.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                                getBaseContext(),R.drawable.ic_school),
                        null,null,null);
                break;
        }

        String userAskedString = getResources().getString(R.string.user_asked);

        questionAuthor.setText( String.format(userAskedString,intent.getStringExtra(MESSAGE_QUESTION_AUTHOR)) );
        questionTitle.setText( intent.getStringExtra(MESSAGE_QUESTION_TITLE) );
        questionAnswerNumber.setText( String.format(Locale.getDefault(),"%d",intent.getIntExtra(MESSAGE_QUESTION_N_ANSWERS,0)) );

        createBody(questionBody,intent.getStringExtra(MESSAGE_QUESTION_BODY),BodyType.QUESTION);

    }

}
