package br.nom.pedrollo.emilio.mathpp.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import br.nom.pedrollo.emilio.mathpp.MainActivity;
import br.nom.pedrollo.emilio.mathpp.QuestionActivity;
import br.nom.pedrollo.emilio.mathpp.R;
import br.nom.pedrollo.emilio.mathpp.adapters.QuestionsAdapter;
import br.nom.pedrollo.emilio.mathpp.utils.SimpleCallBackWithBackground;
import br.nom.pedrollo.emilio.mathpp.utils.TransitionHelper;

public class QuestionsListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    //private ListView listView;

    static final String[] questions = new String[]{
            "Quanto é 2+2?",
            "Conjectura de Hodge",
            "Hipótese de Riemann",
            "6÷2(1+2)=X",
            "Existência de Yang-Mills e intervalo de massa",
            "Existência e suavidade de Navier-Stokes",
            "Monty Hall: Mudar ou não mudar de porta? Por quê?",
            "Conjectura de Birch e Swinnerton-Dyer",
            "P x NP",
            "Problemas de Hilbert",
            "Problemas de Landau",
            "Problemas de Smale",
            "O quê f(x) = x^2 + c é inteiro e seu módulo é diferente de 0, 1 ou 2 tem de especial?",
            "O número 1 é primo?",
            "Qual a forma mais eficiente de embaralhar um baralho?",
            "O que é um nó?",
            "O que é uma garrafa de Klein?",
            "Numeros de Leyland",
            "O que é maior, 1 Gogol ou o numero de Graham?",
            "Teoria Fundamental da Algebra",
            "Quantos infinitos existem?",
            "Como se usa o Google?",
            "Pi = 3.2?",
            "O conjugado da razão entre o número complexo z = 4 - 8i e o número complexo de argumento igual a o = 180° e módulo igual a 4",
            "15 – {–10 – [–8 + ( 5 – 12 )] – 20}",
            "Considere a igualdade x + (4 + y) . i = (6 - x) + 2yi , em que x e y são números reais e i é a unidade imaginária. Qual é o módulo do número complexo z = x + yi",
            "df/dx = d(f(-x))/dx",
            "The graph of f has two minimums, one at x = -2 and one at x = 4, and one maximum at x = 1. Therefore f '(x) = 0 for x = -2, x = 1 and x = 4",
            "Meu colega me disse que a raiz quadrada de 2 é 1. Ta serto isso?",
            "O que é um tesseract? Que gosto tem?",
            "Quais são as propriedades matemáticas de um Nó de trevo?",
            "Alguém tem a prova da professóra Enésima sobre Geometria Analítica?",
            "Alguém consegue me explicar o que diabos é um Cardioide?",
    };

    static final String[] authors = new String[]{
            "Bruno",
            "Isabel",
            "Emílio",
            "Francielle",
            "Fábio",
            "Guilherme",
            "Andréia",
            "Patrícia",
            "Fabiana"
    };

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView questionsList;

    private void shuffleArray(String[] array){
        Random random  = new Random();
        for (int i = array.length - 1; i > 0; i--){
            int index = random.nextInt(i+1);
            String tmp = array[index];
            array[index] = array[i];
            array[i] = tmp;
        }
    }

    @Override
    public void onRefresh() {
        Log.i("QUESTION_LIST_REFRESH", "onRefresh called from SwipeRefreshLayout");
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 4000);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        questionsList = (RecyclerView) view.findViewById(R.id.questions_list);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_question_list);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(view.getContext(),R.color.colorPrimaryDark),
                ContextCompat.getColor(view.getContext(),R.color.colorAccent));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        List<String> questions = new ArrayList<>(Arrays.asList(this.questions));
        List<String> authors = new ArrayList<>();
        List<Integer> answers = new ArrayList<>();
        for (int i = 0; i< questions.size();i++){
            authors.add(this.authors[(new Random()).nextInt(this.authors.length)]);
            answers.add((new Random()).nextInt(16));
        }


        final View view = inflater.inflate(R.layout.fragment_question_list, container, false);
        final QuestionsAdapter questionsAdapter = new QuestionsAdapter(questions,authors,answers);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(container.getContext());

        questionsList = (RecyclerView) view.findViewById(R.id.questions_list);
        questionsList.setHasFixedSize(true);
        questionsList.setLayoutManager(layoutManager);

        questionsList.setItemAnimator(new DefaultItemAnimator());
        //questionsList.getItemAnimator().setRemoveDuration(500);


        // Workaround to enable overscroll effect in the bottom of RecyclerView
        fixOverScroll((LinearLayoutManager) layoutManager);

        //Handle Swipe
        handleSwipe(container, questionsAdapter);


        questionsList.setAdapter(questionsAdapter);


        ((QuestionsAdapter)questionsList.getAdapter()).setOnItemLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(view.getContext(),((TextView) view.findViewById(R.id.question_title)).getText(),Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        ((QuestionsAdapter)questionsList.getAdapter()).setOnItemClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                goToQuestionDetail(view);
            }
        });

        return view;
    }

    private void goToQuestionDetail(View view) {
        Intent intent = new Intent(view.getContext(),QuestionActivity.class);
        Bundle bundle;

        intent.putExtra(QuestionActivity.MESSAGE_QUESTION_TITLE,((TextView) view.findViewById(R.id.question_title)).getText());
        intent.putExtra(QuestionActivity.MESSAGE_QUESTION_AUTHOR,((TextView) view.findViewById(R.id.question_author)).getText());
        intent.putExtra(QuestionActivity.MESSAGE_QUESTION_N_ANSWERS,((TextView) view.findViewById(R.id.question_answer_number)).getText());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Pair<View, String>[] transitionPairs = TransitionHelper.createSafeTransitionParticipants(getActivity(),true,

                    Pair.create( ((MainActivity) getActivity()).getToolbarView() ,"toolbar"),

                    Pair.create(view.findViewById(R.id.question_item_scene_root),"content_area"),
                    Pair.create(view.findViewById(R.id.question_header),"question_header"),
                    Pair.create(view.findViewById(R.id.question_title),"question_title"),
                    Pair.create(view.findViewById(R.id.question_author),"question_author"),
                    Pair.create(view.findViewById(R.id.question_star),"question_star"),
                    Pair.create(view.findViewById(R.id.question_answer_number),"question_answer_number"),

                    // Adicionar o toolbar não funcionou muito bem. A intenção era acabar com o flicker ocasional.
                    Pair.create(getActivity().getWindow().getDecorView().findViewById(R.id.toolbar),"toolbar")

            );

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),transitionPairs);

            bundle = options.toBundle();
        } else {
            bundle = new Bundle();
        }

        ActivityCompat.startActivity(getActivity(),intent,bundle);
    }

    private void handleSwipe(final ViewGroup container, final QuestionsAdapter questionsAdapter) {
        //ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        SimpleCallBackWithBackground simpleItemTouchCallback = new SimpleCallBackWithBackground(container.getContext(),0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
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

                        List<String> questions = questionsAdapter.getQuestions();
                        List<String> authors = questionsAdapter.getAuthors();
                        List<Integer> answers = questionsAdapter.getAnswers();

                        questions.remove(itemPosition);
                        authors.remove(itemPosition);
                        answers.remove(itemPosition);
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

//            @Override
//            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//                if (viewHolder instanceof RemovableViewHolder) {
//                    int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
//                    return makeMovementFlags(0, swipeFlags);
//                } else
//                    return 0;
//            }
//
//            @Override
//            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//                getDefaultUIUtil().clearView(((RemovableViewHolder) viewHolder).getSwipableView());
//            }
//
//            @Override
//            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//                if (viewHolder != null) {
//                    getDefaultUIUtil().onSelected(((RemovableViewHolder) viewHolder).getSwipableView());
//                }
//            }
//
//            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                getDefaultUIUtil().onDraw(c, recyclerView, ((RemovableViewHolder) viewHolder).getSwipableView(), dX, dY,    actionState, isCurrentlyActive);
//            }
//
//            public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//                getDefaultUIUtil().onDrawOver(c, recyclerView, ((RemovableViewHolder) viewHolder).getSwipableView(), dX, dY,    actionState, isCurrentlyActive);
//            }

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
