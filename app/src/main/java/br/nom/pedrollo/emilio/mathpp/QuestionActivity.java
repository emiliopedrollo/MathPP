package br.nom.pedrollo.emilio.mathpp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.nom.pedrollo.emilio.mathpp.entities.Answer;
import br.nom.pedrollo.emilio.mathpp.utils.transitions.ExpandTransition;

public class QuestionActivity extends AppCompatActivity {


    public static final String MESSAGE_NEW_QUESTION = "NEW_QUESTION";
    public static final String MESSAGE_QUESTION_TITLE = "QUESTION_TITLE";
    public static final String MESSAGE_QUESTION_AUTHOR = "QUESTION_AUTHOR";
    public static final String MESSAGE_QUESTION_N_ANSWERS = "QUESTION_N_ANSWERS";

    String TAG = "==XX==";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", this).show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        populateAnswers();

        handleIntent(getIntent());

        setupTransition();


        //TransitionHelper.fixSharedElementTransitionForStatusAndNavigationBar(this);
        //TransitionHelper.setSharedElementEnterTransition(this, R.transition.expand);

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

    private void populateAnswers() {
        RecyclerView answerList = (RecyclerView) findViewById(R.id.question_answers);
        answerList.setLayoutManager(new LinearLayoutManager(this));
        answerList.setNestedScrollingEnabled(false);

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


        final List<Answer> answers = new ArrayList<>();
        answers.add(new Answer("Emílio","This is a stub Answer 1",getResources().getString(R.string.lipsum)));
        answers.add(new Answer("Emílio","This is a stub Answer 2",getResources().getString(R.string.lipsum)));
        answers.add(new Answer("Emílio","This is a stub Answer 3",getResources().getString(R.string.lipsum)));
        answers.add(new Answer("Emílio","This is a stub Answer 4",getResources().getString(R.string.lipsum)));
        answers.add(new Answer("Emílio","This is a stub Answer 5",getResources().getString(R.string.lipsum)));

        answerList.setAdapter(new RecyclerView.Adapter<AnswerViewHolder>(){

            @Override
            public AnswerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.answer_item,parent,false);
                return (new AnswerViewHolder(view));
            }

            @Override
            public void onBindViewHolder(AnswerViewHolder holder, int position) {
                /*holder.answerItem.setLongClickable(true);
                holder.answerItem.setClickable(true);

                if (clickListener != null)
                    holder.answerItem.setOnClickListener(clickListener);

                if (longClickListener != null)
                    holder.answerItem.setOnLongClickListener(longClickListener);*/

                String userRespondsString = getResources().getString(R.string.user_responded);

                Answer answer = answers.get(position);

                holder.answerTitle.setText( answer.getTitle() );
                holder.answerAuthor.setText(String.format(userRespondsString,answer.getAuthor()));
                holder.answerText.setText( answer.getText() );

                ImageButton thumbsUp = (ImageButton) holder.itemView.findViewById(R.id.thumbs_up_vote);
                ImageButton thumbsDown = (ImageButton) holder.itemView.findViewById(R.id.thumbs_down_vote);

                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        if (v.getTag() == TAG) {
                            ((ImageButton) v).setColorFilter(null);
                            v.setTag(null);
                        } else {
                            ((ImageButton) v).setColorFilter(ContextCompat.getColor(getApplicationContext(),R.color.colorAccent));
                            v.setTag(TAG);
                        }
                    }
                };

                thumbsUp.setOnClickListener(onClickListener);
                thumbsDown.setOnClickListener(onClickListener);
            }

            @Override
            public int getItemCount() {
                return answers.size();
            }
        });
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

    private static class AnswerViewHolder extends RecyclerView.ViewHolder{
        FrameLayout answerItem;
        TextView answerTitle;
        TextView answerAuthor;
        TextView answerText;

        AnswerViewHolder(View itemView) {
            super(itemView);
            answerItem = (FrameLayout) itemView.findViewById(R.id.answer_item_background);

            answerTitle = (TextView) answerItem.findViewById(R.id.answer_item_title);
            answerAuthor = (TextView) answerItem.findViewById(R.id.answer_item_author);
            answerText = (TextView) answerItem.findViewById(R.id.answer_item_text);
        }

    }

    private void handleIntent(Intent intent){
        TextView questionAuthor = (TextView) findViewById(R.id.question_author);
        TextView questionTitle = (TextView) findViewById(R.id.question_title);
        TextView questionAnswerNumber = (TextView) findViewById(R.id.question_answer_number);

        questionAuthor.setText(intent.getStringExtra(MESSAGE_QUESTION_AUTHOR));
        questionTitle.setText(intent.getStringExtra(MESSAGE_QUESTION_TITLE));
        questionAnswerNumber.setText(intent.getStringExtra(MESSAGE_QUESTION_N_ANSWERS));
    }

}
