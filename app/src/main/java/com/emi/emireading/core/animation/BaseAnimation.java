package com.emi.emireading.core.animation;

import android.animation.Animator;
import android.view.View;


public interface  BaseAnimation {
    Animator[] getAnimators(View view);
}
