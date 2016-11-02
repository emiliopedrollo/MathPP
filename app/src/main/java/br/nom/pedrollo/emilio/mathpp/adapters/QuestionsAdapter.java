package br.nom.pedrollo.emilio.mathpp.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import br.nom.pedrollo.emilio.mathpp.R;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ViewHolder> {

    private final List<String> questions;
    private final List<String> authors;
    private final List<Integer> answers;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;

    public QuestionsAdapter(List<String> questions, List<String> authors, List<Integer> answers){
        this.questions = questions;
        this.authors = authors;
        this.answers = answers;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout questionListItem;
        TextView questionTitle;
        TextView numAnswers;
        TextView questionAuthor;

        ViewHolder(View itemView) {
            //super(itemView,removableViewId);
            super(itemView);
            questionListItem = (FrameLayout) itemView;

            questionTitle = (TextView) questionListItem.findViewById(R.id.question_title);
            numAnswers = (TextView) questionListItem.findViewById(R.id.question_answer_number);
            questionAuthor = (TextView) questionListItem.findViewById(R.id.question_author);

        }
    }

    public void setOnItemClickListener(View.OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setOnItemLongClickListener(View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_list_item,parent,false);
        return (new ViewHolder(view));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.questionListItem.setLongClickable(true);
        holder.questionListItem.setClickable(true);

        if (clickListener != null)
            holder.questionListItem.setOnClickListener(clickListener);

        if (longClickListener != null)
            holder.questionListItem.setOnLongClickListener(longClickListener);

        String userAskString = holder.itemView.getContext().getResources().getString(R.string.user_asked);

        holder.questionTitle.setText(questions.get(position));
        holder.questionAuthor.setText(String.format(userAskString,authors.get(position)));
        //holder.questionAuthor.setText(String.format(userAskString,authors.get((new Random()).nextInt(authors.size()))));
        holder.numAnswers.setText(String.valueOf(answers.get(position)));

    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public List<String> getQuestions() {
        return questions;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public List<Integer> getAnswers() {
        return answers;
    }
}
