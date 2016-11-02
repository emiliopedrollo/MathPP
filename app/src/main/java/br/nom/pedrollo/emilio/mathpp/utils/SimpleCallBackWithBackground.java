package br.nom.pedrollo.emilio.mathpp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.view.View;


public abstract class SimpleCallBackWithBackground extends ItemTouchHelper.SimpleCallback {
    private Context context;
    private Bitmap leftSwipeIcon;
    private Bitmap rightSwipeIcon;
    private int leftSwipeBackground;
    private int rightSwipeBackground;
    private int leftIconTint;
    private int rightIconTint;
    private Paint paint;
    private float faintSpeed = (float) 0;
    private int dpOffset = 16;

    public SimpleCallBackWithBackground(Context context, int dragDirs, int swipeDirs){
        super(dragDirs,swipeDirs);
        this.context = context;
        paint = new Paint();
    }


    private int dpToPx(int dp){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public SimpleCallBackWithBackground setDpOffset(int dpOffset) {
        this.dpOffset = dpOffset;
        return this;
    }

    public SimpleCallBackWithBackground setFaintSpeed(float faintSpeed) {
        this.faintSpeed = faintSpeed;
        return this;
    }

    public SimpleCallBackWithBackground setSwipeBackground(int swipeBackground){
        this.rightSwipeBackground = swipeBackground;
        this.leftSwipeBackground = swipeBackground;
        return this;
    }

    public SimpleCallBackWithBackground setSwipeIcon(Bitmap swipeIcon){
        this.rightSwipeIcon = swipeIcon;
        this.leftSwipeIcon = swipeIcon;
        return this;
    }

    public SimpleCallBackWithBackground setIconTint(int iconTint) {
        this.leftIconTint = iconTint;
        this.rightIconTint = iconTint;
        return this;
    }

    public SimpleCallBackWithBackground setRightSwipeBackground(int rightSwipeBackground) {
        this.rightSwipeBackground = rightSwipeBackground;
        return this;
    }

    public SimpleCallBackWithBackground setLeftSwipeBackground(int leftSwipeBackground) {
        this.leftSwipeBackground = leftSwipeBackground;
        return this;
    }

    public SimpleCallBackWithBackground setRightSwipeIcon(Bitmap rightSwipeIcon) {
        this.rightSwipeIcon = rightSwipeIcon;
        return this;
    }

    public SimpleCallBackWithBackground setLeftSwipeIcon(Bitmap leftSwipeIcon) {
        this.leftSwipeIcon = leftSwipeIcon;
        return this;
    }

    public SimpleCallBackWithBackground setRightIconTint(int rightIconTint) {
        this.rightIconTint = rightIconTint;
        return this;
    }

    public SimpleCallBackWithBackground setLeftIconTint(int leftIconTint) {
        this.leftIconTint = leftIconTint;
        return this;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;

            //Paint paint = new Paint();
            if (dX > 0 && (leftSwipeBackground != 0 || leftSwipeIcon != null)) {

                if (leftSwipeBackground != 0){
                    paint.setColor( leftSwipeBackground );
                    c.drawRect((float) itemView.getLeft(), (float) itemView.getTop(), dX,
                            (float) itemView.getBottom(), paint);
                }

                if (leftSwipeIcon != null){
                    if (leftIconTint != 0){
                        paint.setColorFilter(new PorterDuffColorFilter(leftIconTint, PorterDuff.Mode.SRC_IN));
                    }
                    c.drawBitmap(leftSwipeIcon,
                            new Rect(0,0,
                                    Math.min(leftSwipeIcon.getWidth(),Math.max(0, (Math.round(dX) - (itemView.getLeft() + dpToPx(dpOffset))) )),
                                    leftSwipeIcon.getHeight()),
                            new Rect(
                                    itemView.getLeft() + dpToPx(dpOffset),
                                    itemView.getTop() + (itemView.getBottom() - itemView.getTop() - leftSwipeIcon.getHeight())/2,
                                    itemView.getLeft() + dpToPx(dpOffset) + Math.min(leftSwipeIcon.getWidth(),Math.max(0, (Math.round(dX) - (itemView.getLeft() + dpToPx(dpOffset))) )),
                                    (itemView.getTop() + (itemView.getBottom() - itemView.getTop() - leftSwipeIcon.getHeight())/2) + leftSwipeIcon.getHeight()
                            ),
                            paint);
                    paint.setColorFilter(null);
                }

            } else if (dX < 0 && (rightSwipeBackground != 0 || rightSwipeIcon != null)) {

                if (rightSwipeBackground != 0){
                    paint.setColor( rightSwipeBackground );
                    c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                            (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                }

                if (rightSwipeIcon != null){
                    if (rightIconTint != 0){
                        paint.setColorFilter(new PorterDuffColorFilter(rightIconTint, PorterDuff.Mode.SRC_IN));
                    }
                    c.drawBitmap(rightSwipeIcon,
                            new Rect(Math.min(rightSwipeIcon.getWidth(),Math.max(0,
                                    rightSwipeIcon.getWidth() + (Math.round(dX) + dpToPx(dpOffset))
                            )),
                                    0,rightSwipeIcon.getWidth(),
                                    rightSwipeIcon.getHeight()),
                            new Rect(
                                    itemView.getRight() + Math.max( Math.round(dX) , -(rightSwipeIcon.getWidth() + dpToPx(dpOffset))),
                                    itemView.getTop() + (itemView.getBottom() - itemView.getTop() - rightSwipeIcon.getHeight())/2,
                                    itemView.getRight() - dpToPx(dpOffset),
                                    (itemView.getTop() + (itemView.getBottom() - itemView.getTop() - rightSwipeIcon.getHeight())/2) + rightSwipeIcon.getHeight()
                            ),
                            paint);
                    paint.setColorFilter(null);
                }
            }
            // Fade out the view as it is swiped out of the parent's bounds
            final float alpha = 1 - Math.min(1,(Math.abs(dX) / (float) viewHolder.itemView.getWidth()) * faintSpeed);
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}
