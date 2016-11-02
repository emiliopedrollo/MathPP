package br.nom.pedrollo.emilio.mathpp.utils.transitions;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ElevationTransition extends Transition {

    private static final String PROP_NAME_START = "elevation:start";
    private static final String PROP_NAME_END = "elevation:end";

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROP_NAME_START, transitionValues.view.getElevation());
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROP_NAME_END, transitionValues.view.getElevation());
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }

        Float startVal = (Float) startValues.values.get(PROP_NAME_START);
        Float endVal = (Float) endValues.values.get(PROP_NAME_END);
        if (startVal == null || endVal == null || startVal.floatValue() == endVal.floatValue()) {
            return null;
        }

        final View view = endValues.view;
        ValueAnimator animator = ValueAnimator.ofFloat(startVal, endVal);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setElevation((float)animation.getAnimatedValue());
            }
        });

        return animator;
    }

}
