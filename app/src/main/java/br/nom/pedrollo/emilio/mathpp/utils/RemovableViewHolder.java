package br.nom.pedrollo.emilio.mathpp.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class RemovableViewHolder extends RecyclerView.ViewHolder {
    private View removableView;

    public RemovableViewHolder(final View itemView, int removableViewId) {
        super(itemView);
        removableView = itemView.findViewById(removableViewId);
    }

    public View getSwipableView() {
        return removableView;
    }

}
