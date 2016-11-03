package br.nom.pedrollo.emilio.mathpp.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.Contract;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.nom.pedrollo.emilio.mathpp.MainActivity;
import br.nom.pedrollo.emilio.mathpp.QuestionActivity;
import br.nom.pedrollo.emilio.mathpp.R;
import br.nom.pedrollo.emilio.mathpp.WritePostActivity;
import br.nom.pedrollo.emilio.mathpp.adapters.QuestionsAdapter;
import br.nom.pedrollo.emilio.mathpp.entities.Question;
import br.nom.pedrollo.emilio.mathpp.utils.NetworkUtils;
import br.nom.pedrollo.emilio.mathpp.utils.SimpleCallBackWithBackground;
import br.nom.pedrollo.emilio.mathpp.utils.TransitionHelper;

// TODO: Implement infinite scroll

// TODO: Implement and verify user type, using the correct icon

public class QuestionsListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public final static String CATEGORY = "CATEGORY";

    private Boolean alreadyLoadedCategoriesOnce = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView questionsList;
    private RelativeLayout questionsPlaceholder;

    private int categoryId;

    @Override
    public void onRefresh() {
        Log.i("QUESTION_LIST_REFRESH", "onRefresh called from SwipeRefreshLayout");
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 4000);
        //TODO: make it really reload
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle.containsKey(CATEGORY)){
            categoryId = bundle.getInt(CATEGORY);
        } else {
            categoryId = -1;
        }
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_question:
                Intent intent = new Intent(getActivity(),WritePostActivity.class);
                intent.putExtra("Category",categoryId);
                ActivityCompat.startActivity(getActivity(),intent,null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class CollectParams {
        public int offset;
        public int limit;

        CollectParams(){
            this.offset = 0;
            this.limit = 50;
        }

        CollectParams(int offset){
            this.offset = offset;
            this.limit = 10;
        }

        CollectParams(int offset, int limit){
            this.offset = offset;
            this.limit = limit;
        }
    }

    private class getQuestionsTask extends AsyncTask<CollectParams,Void,String> {
        @SuppressWarnings("ThrowFromFinallyBlock")
        @Override
        protected String doInBackground(CollectParams... params) {
            HashMap<String, String> getParams = new HashMap<>();
            getParams.put("offset",Integer.toString(params[0].offset));
            getParams.put("limit",Integer.toString(params[0].limit));

            NetworkUtils.readTimeout = NetworkUtils.MEDIUM_TIMEOUT;
            NetworkUtils.connectionTimeout = NetworkUtils.MEDIUM_TIMEOUT;

            StringBuilder uri = new StringBuilder();
            uri.append(getResources().getString(R.string.questions_fetch_uri));
            if (categoryId != -1){
                uri.append("/");
                uri.append(categoryId);
            }

            return NetworkUtils.getFromServer(getContext(),uri.toString(),
                    NetworkUtils.Method.GET, getParams);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_question_list, container, false);

        questionsPlaceholder = (RelativeLayout) view.findViewById(R.id.questions_placeholder);
        final ProgressBar questionsPlaceholderProgressbar = (ProgressBar) view.findViewById(R.id.questions_placeholder_progressbar);
        final TextView questionsPlaceholderTextView = (TextView) view.findViewById(R.id.questions_placeholder_text_view);

        questionsList = (RecyclerView) view.findViewById(R.id.questions_list);
        questionsList.setItemAnimator(new DefaultItemAnimator());
        questionsList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        questionsList.setAdapter(new QuestionsAdapter());
        questionsList.setHasFixedSize(true);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_question_list);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(view.getContext(),R.color.colorPrimaryDark),
                ContextCompat.getColor(view.getContext(),R.color.colorAccent));

        fixOverScroll((LinearLayoutManager) questionsList.getLayoutManager());
        handleSwipe(container, (QuestionsAdapter) questionsList.getAdapter());


        ConnectivityManager connMgr = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {

            questionsPlaceholder.setVisibility(View.VISIBLE);
            questionsPlaceholderProgressbar.setVisibility(View.VISIBLE);
            questionsPlaceholderTextView.setText(getResources().getString(R.string.loading_questions));

            new getQuestionsTask(){
                @Override
                protected void onPostExecute(String serverResponse) {
                    super.onPostExecute(serverResponse);

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
                                int answers = jsonObject.getInt("n_answers");

                                Question question = new Question(id,title,text,author,authorType,answers);
                                QuestionsAdapter adapter = (QuestionsAdapter) questionsList.getAdapter();

                                for (int i=0; i < adapter.questions.size(); i++){
                                    if (adapter.questions.get(i).getId() ==  question.getId() ){
                                        foundExisting = true;
                                        adapter.questions.set(i,question);
                                        break;
                                    }
                                }
                                if (!foundExisting){
                                    adapter.questions.add(question);
                                }

                            } catch (JSONException e){
                                Log.e("PARSE_JSON",e.getLocalizedMessage());
                            }
                        }

                        @Override
                        public void onFinish(Boolean success) {
                            QuestionsAdapter adapter = (QuestionsAdapter) questionsList.getAdapter();

                            ProgressBar questionsPlaceholderProgressbar =
                                    (ProgressBar)questionsPlaceholder.findViewById(R.id.questions_placeholder_progressbar);

                            TextView questionsPlaceholderTextView =
                                    (TextView) questionsPlaceholder.findViewById(R.id.questions_placeholder_text_view);

                            if (success){
                                if (adapter.questions.size() > 0){
                                    alreadyLoadedCategoriesOnce = true;
                                    questionsPlaceholder.setVisibility(View.GONE);
                                    questionsList.setVisibility(View.VISIBLE);
                                } else {
                                    questionsList.setVisibility(View.GONE);
                                    questionsPlaceholder.setVisibility(View.VISIBLE);
                                    questionsPlaceholderProgressbar.setVisibility(View.GONE);
                                    questionsPlaceholderTextView.setText(getResources().getString(R.string.no_categories_available));
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                questionsList.setVisibility(View.GONE);
                                questionsPlaceholder.setVisibility(View.VISIBLE);
                                questionsPlaceholderProgressbar.setVisibility(View.GONE);
                                questionsPlaceholderTextView.setText(getResources().getString(R.string.unable_retrieve_categories));
                            }
                        }
                    });

                }
            }.execute(new CollectParams());


        } else {
            questionsPlaceholderTextView.setText(getResources().getString(R.string.no_internet_connection));
            questionsPlaceholder.setVisibility(View.VISIBLE);
            questionsPlaceholderProgressbar.setVisibility(View.GONE);
        }

        ((QuestionsAdapter)questionsList.getAdapter()).setOnBindViewHolder(new QuestionsAdapter.OnBindViewHolder() {
            @Override
            public void onBindViewHolder(QuestionsAdapter.ViewHolder holder, final int position) {
                holder.itemView.setClickable(true);
                holder.itemView.setLongClickable(true);
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(v.getContext(),
                                ((TextView) v.findViewById(R.id.question_title)).getText(),
                                Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToQuestionDetail(view,
                                ((QuestionsAdapter)questionsList.getAdapter()).questions.get(position));
                    }
                });
            }
        });

        return view;
    }

    private void goToQuestionDetail(View view, Question question) {
        Intent intent = new Intent(view.getContext(),QuestionActivity.class);
        Bundle bundle = new Bundle();

        intent.putExtra(QuestionActivity.MESSAGE_QUESTION_ID,question.getId());
        intent.putExtra(QuestionActivity.MESSAGE_QUESTION_TITLE,question.getTitle());
        intent.putExtra(QuestionActivity.MESSAGE_QUESTION_BODY,question.getText());
        intent.putExtra(QuestionActivity.MESSAGE_QUESTION_AUTHOR,question.getAuthor());
        intent.putExtra(QuestionActivity.MESSAGE_QUESTION_AUTHOR_TYPE,question.getAuthorType());
        intent.putExtra(QuestionActivity.MESSAGE_QUESTION_N_ANSWERS,question.getAnswers());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && false) {

            Pair<View, String>[] transitionPairs = TransitionHelper.createSafeTransitionParticipants(getActivity(),true,

                    Pair.create( ((MainActivity) getActivity()).getToolbarView() ,"toolbar"),

                    Pair.create(view.findViewById(R.id.question_item_scene_root),"content_area"),
                    Pair.create(view.findViewById(R.id.question_header),"question_header"),
                    Pair.create(view.findViewById(R.id.question_title),"question_title"),
                    Pair.create(view.findViewById(R.id.question_author),"question_author"),
                    Pair.create(view.findViewById(R.id.question_star),"question_star"),
                    Pair.create(view.findViewById(R.id.answer_item_score),"question_answer_number"),

                    // Adicionar o toolbar não funcionou muito bem. A intenção era acabar com o flicker ocasional.
                    Pair.create(getActivity().getWindow().getDecorView().findViewById(R.id.toolbar),"toolbar")

            );

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),transitionPairs);

            bundle.putAll(options.toBundle());
        }

        ActivityCompat.startActivity(getActivity(),intent,bundle);
    }

    private void handleSwipe(final ViewGroup container, final QuestionsAdapter questionsAdapter) {
        //ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        SimpleCallBackWithBackground simpleItemTouchCallback = new SimpleCallBackWithBackground(container.getContext(),0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            @Contract(value = "_, _, _ -> false", pure = true)
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                final View itemView = viewHolder.itemView;

                viewHolder.setIsRecyclable(false);
                final int initialHeight = itemView.getMeasuredHeight();
                Animation a = new ScaleAnimation(itemView.getWidth(),itemView.getWidth(),itemView.getHeight(),0){
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        itemView.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                        itemView.requestLayout();
                        if (interpolatedTime == 1){
                            itemView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    @Contract(value = " -> true", pure = true)
                    public boolean willChangeBounds() {
                        return true;
                    }
                };
                int animationDuration = (int)(initialHeight/itemView.getContext().getResources().getDisplayMetrics().density) * 4;
                a.setDuration(animationDuration);
                itemView.startAnimation(a);


                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        int itemPosition = viewHolder.getAdapterPosition();
                        if (itemPosition == -1) return;

                        questionsAdapter.questions.remove(itemPosition);

                        questionsAdapter.notifyItemRemoved(itemPosition);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                                itemView.setVisibility(View.VISIBLE);
                                viewHolder.setIsRecyclable(true);
                            }
                        }, 100);

                    }
                }, animationDuration + 100 ); /*questionsList.getItemAnimator().getRemoveDuration());*/

                Snackbar.make(questionsList, getResources().getString(R.string.question_hidden), Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.undo).toUpperCase(), null).show();

                //TODO: Implement Undo Listener
            }

        };
        simpleItemTouchCallback
                .setSwipeBackground(Color.argb(255,244,67,54))
                .setSwipeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_block_black_24dp))
                .setIconTint(Color.WHITE);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(questionsList);
    }

    private void fixOverScroll(final LinearLayoutManager layoutManager) {
        questionsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                try {
                    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisiblePosition == 0)
                        swipeRefreshLayout.setEnabled(true);
                    else
                        swipeRefreshLayout.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
