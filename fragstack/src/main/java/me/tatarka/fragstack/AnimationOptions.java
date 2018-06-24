package me.tatarka.fragstack;

import android.support.annotation.AnimRes;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.view.View;

/**
 * Animation options for pushing a fragment.
 */
public class AnimationOptions {
    int enter;
    int exit;
    int popEnter;
    int popExit;
    @Nullable
    SimpleArrayMap<View, String> sharedElements;

    /**
     * Set specific animation resources to run for the fragments that are entering and exiting in
     * this transaction. These animations will not be played when popping the back stack.
     *
     * @see android.support.v4.app.FragmentTransaction#setCustomAnimations(int, int)
     */
    @NonNull
    public AnimationOptions setCustomAnimations(@AnimatorRes @AnimRes int enter, @AnimatorRes @AnimRes int exit) {
        this.enter = enter;
        this.exit = exit;
        return this;
    }

    /**
     * Set specific animation resources to run for the fragments that are entering and exiting in
     * this transaction. The popEnter and popExit animations will be played for enter/exit
     * operations specifically when popping the back stack.
     *
     * @see android.support.v4.app.FragmentTransaction#setCustomAnimations(int, int, int, int)
     */
    @NonNull
    public AnimationOptions setCustomAnimations(@AnimatorRes @AnimRes int enter, @AnimatorRes @AnimRes int exit, @AnimatorRes @AnimRes int popEnter, @AnimatorRes @AnimRes int popExit) {
        this.enter = enter;
        this.exit = exit;
        this.popEnter = popEnter;
        this.popExit = popExit;
        return this;
    }

    /**
     * Used with custom Transitions to map a View from a removed or hidden Fragment to a View from a
     * shown or added Fragment. sharedElement must have a unique transitionName in the View hierarchy.
     *
     * @see android.support.v4.app.FragmentTransaction#addSharedElement(View, String)
     */
    @NonNull
    public AnimationOptions addSharedElement(View sharedElement, String name) {
        if (sharedElements == null) {
            sharedElements = new SimpleArrayMap<>();
        }
        sharedElements.put(sharedElement, name);
        return this;
    }
}
