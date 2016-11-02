package br.nom.pedrollo.emilio.mathpp.utils.transitions;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ExpandTransition extends Transition {

    private static final String PROP_NAME_ELEVATION = "br.nom.pedrollo.emilio.mathpp.utils.transitions:ExpandTransition:elevation";

    public ExpandTransition() {
    }

    public ExpandTransition(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        Float elevation = transitionValues.view.getElevation();
        transitionValues.values.put(PROP_NAME_ELEVATION, elevation);
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }

        Float startVal = (Float) startValues.values.get(PROP_NAME_ELEVATION);
        Float endVal = (Float) endValues.values.get(PROP_NAME_ELEVATION);
        if (startVal == null || endVal == null || startVal.floatValue() == endVal.floatValue()) {
            return null;
        }

        final View view = endValues.view;
        ValueAnimator a = ValueAnimator.ofFloat(startVal, endVal);
        a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setElevation((float)animation.getAnimatedValue());
            }
        });

        return a;
    }

}
