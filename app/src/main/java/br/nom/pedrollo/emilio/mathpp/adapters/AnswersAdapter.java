package br.nom.pedrollo.emilio.mathpp.adapters;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.nom.pedrollo.emilio.mathpp.R;
import br.nom.pedrollo.emilio.mathpp.entities.Answer;

public class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.ViewHolder> {


    public List<Answer> answers;
    private OnBindViewHolder onBindViewHolder;

    public AnswersAdapter(){
        this.answers = new ArrayList<>();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout answerItem;
        public LinearLayout answerBody;
        public ImageView answerAuthorIcon;
        public TextView answerTitle;
        public TextView answerAuthor;
        public TextView answerScore;
        public ImageButton thumbsUp;
        public ImageButton thumbsDown;

        ViewHolder(View itemView) {
            super(itemView);
            answerItem = (FrameLayout) itemView.findViewById(R.id.answer_item_background);

            answerBody = (LinearLayout) answerItem.findViewById(R.id.answer_item_body);
            answerAuthorIcon = (ImageView) answerItem.findViewById(R.id.answer_item_author_icon);
            answerTitle = (TextView) answerItem.findViewById(R.id.answer_item_title);
            answerAuthor = (TextView) answerItem.findViewById(R.id.answer_item_author);
            answerScore = (TextView) answerItem.findViewById(R.id.answer_item_score);
            thumbsUp = (ImageButton) answerItem.findViewById(R.id.thumbs_up_vote);
            thumbsDown = (ImageButton) answerItem.findViewById(R.id.thumbs_down_vote);
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.answer_item,parent,false);
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

        String userRespondsString = holder.itemView.getContext().getResources().getString(R.string.user_responded);

        Answer answer = answers.get(position);

        holder.answerTitle.setText( answer.getTitle() );
        holder.answerAuthor.setText(String.format(userRespondsString,answer.getAuthor()));
        holder.answerScore.setText( String.format(Locale.getDefault(),"%d",answer.getScore()) );

        onBindViewHolder.onBindViewHolder(holder,position);
    }

    @Override
    public int getItemCount() {
        return answers.size();
    }

}
