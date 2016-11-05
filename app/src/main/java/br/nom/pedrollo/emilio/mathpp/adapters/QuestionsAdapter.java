package br.nom.pedrollo.emilio.mathpp.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.nom.pedrollo.emilio.mathpp.R;
import br.nom.pedrollo.emilio.mathpp.entities.Question;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ViewHolder> {

//    private View.OnClickListener clickListener;
//    private View.OnLongClickListener longClickListener;

    public List<Question> questions;
    private OnBindViewHolder onBindViewHolder;

    public QuestionsAdapter(){
        this.questions = new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        FrameLayout questionListItem;
        TextView questionTitle;
        TextView numAnswers;
        TextView questionAuthor;

        ViewHolder(View itemView) {
            //super(itemView,removableViewId);
            super(itemView);
            questionListItem = (FrameLayout) itemView;

            questionTitle = (TextView) questionListItem.findViewById(R.id.question_title);
            numAnswers = (TextView) questionListItem.findViewById(R.id.answer_item_score);
            questionAuthor = (TextView) questionListItem.findViewById(R.id.question_author);

        }
    }

//    public void setOnItemClickListener(View.OnClickListener clickListener) {
//        this.clickListener = clickListener;
//    }
//
//    public void setOnItemLongClickListener(View.OnLongClickListener longClickListener) {
//        this.longClickListener = longClickListener;
//    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.question_list_item,parent,false);
        return (new ViewHolder(view));
    }

    public interface OnBindViewHolder{
        void onBindViewHolder(ViewHolder holder, int position);
    }

    public void setOnBindViewHolder(OnBindViewHolder onBindViewHolder){
        this.onBindViewHolder = onBindViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        holder.questionListItem.setLongClickable(true);
//        holder.questionListItem.setClickable(true);

//        if (clickListener != null)
//            holder.questionListItem.setOnClickListener(clickListener);
//
//        if (longClickListener != null)
//            holder.questionListItem.setOnLongClickListener(longClickListener);

        String userAskString = holder.itemView.getContext().getResources().getString(R.string.user_asked);

        holder.questionTitle.setText(questions.get(position).getTitle());
        holder.questionAuthor.setText(String.format(userAskString,questions.get(position).getAuthor()));
        holder.numAnswers.setText(String.valueOf(questions.get(position).getAnswers()));

        switch (questions.get(position).getAuthorType()){
            case "student":
                holder.questionAuthor.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                                holder.itemView.getContext(),R.drawable.ic_person_outline_black_24dp),
                        null,null,null);
                break;
            case "monitor":
                holder.questionAuthor.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                                holder.itemView.getContext(),R.drawable.ic_person_black_24dp),
                        null,null,null);
                break;
            case "teacher":
                holder.questionAuthor.setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                                holder.itemView.getContext(),R.drawable.ic_school_black_24dp),
                        null,null,null);
                break;
        }

        onBindViewHolder.onBindViewHolder(holder,position);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

}
