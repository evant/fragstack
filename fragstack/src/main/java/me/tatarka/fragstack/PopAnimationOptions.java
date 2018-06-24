package me.tatarka.fragstack;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;
import android.view.View;

/**
 * Adds animations to a pop operation. This is here for shared element transitions, as the fragment
 * manager sees a 'pop' as a replace and will run the animation in the <em>new</em> fragment instead
 * of the old one.
 */
public class PopAnimationOptions {
    @Nullable
    SimpleArrayMap<View, String> sharedElements;

    /**
     * Used with custom Transitions to map a View from a removed or hidden Fragment to a View from a
     * shown or added Fragment. sharedElement must have a unique transitionName in the View hierarchy.
     *
     * @see android.support.v4.app.FragmentTransaction#addSharedElement(View, String)
     */
    @NonNull
    public PopAnimationOptions addSharedElement(View sharedElement, String name) {
        if (sharedElements == null) {
            sharedElements = new SimpleArrayMap<>();
        }
        sharedElements.put(sharedElement, name);
        return this;
    }
}
