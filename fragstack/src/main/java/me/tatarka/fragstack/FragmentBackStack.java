package me.tatarka.fragstack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import java.util.ArrayList;

/**
 * A better fragment back-stack™. The major difference from the built-in fragment back-stack is that
 * fragments in the back-stack on completely destroyed, not just their views. This removes the weird
 * extra {@link Fragment#onViewCreated(View, Bundle)}/{@link Fragment#onDestroyView()} lifecycle
 * that fragments have.
 * <p>
 * Transactions will automatically be optimised, so you are free to edit the
 * back-stack with multiple operations. (For example, to go from [A, B, C] to [A, B, D] you can do
 * <pre>{@code
 * backStack.pop().push(fragmentD);
 * }</pre>
 */
@SuppressLint("CommitTransaction")
public final class FragmentBackStack {

    /**
     * Obtain an instance of the back-stack. This class doesn't hold onto any state itself so it's
     * safe to call it multiple times on the same {@link FragmentManager}.
     */
    @NonNull
    public static FragmentBackStack of(FragmentManager fm) {
        return new FragmentBackStack(fm);
    }

    private final FragmentManager fm;
    @Nullable
    private BackStackTrackingFragment tf;

    FragmentBackStack(FragmentManager fm) {
        this.fm = fm;
    }

    /**
     * Starts the back-stack at the given container layout with the given fragment. If the fragment
     * is not already added, it will be. This must be called before any {@link #push(Fragment)} or
     * {@link #pop()} methods.
     */
    @NonNull
    public FragmentBackStack startWith(@IdRes int containerId, @NonNull Fragment fragment) {
        return startWith(containerId, fragment, null);
    }

    /**
     * Starts the back-stack at the given container layout with the given fragment. If the fragment
     * is not already added, it will be. This must be called before any {@link #push(Fragment)} or
     * {@link #pop()} methods.
     */
    @NonNull
    public FragmentBackStack startWith(@IdRes int containerId, @NonNull Fragment fragment, @Nullable String tag) {
        fm.executePendingTransactions();
        if (fm.findFragmentById(containerId) == null) {
            BackStackTrackingFragment tf = obtain();
            tf.setContainerId(containerId);
            tf.currentFragment = fragment;
            fm.beginTransaction()
                    .add(containerId, fragment, tag)
                    .setReorderingAllowed(true)
                    .setPrimaryNavigationFragment(fragment)
                    .commit();
        }
        return this;
    }

    /**
     * Pushes the given fragment onto the stack, replacing the current one.
     */
    @NonNull
    public FragmentBackStack push(@NonNull Fragment fragment) {
        return push(fragment, null, null);
    }

    /**
     * Pushes the given fragment onto the stack with the given tag.
     */
    @NonNull
    public FragmentBackStack push(@NonNull Fragment fragment, @Nullable String tag) {
        return push(fragment, tag, null);
    }

    /**
     * Pushes the given fragment onto the stack with the given animation options.
     *
     * @see AnimationOptions
     */
    @NonNull
    public FragmentBackStack push(@NonNull Fragment fragment, @Nullable AnimationOptions animationOptions) {
        return push(fragment, null, animationOptions);
    }

    /**
     * Pushes the given fragment onto the stack with the given tag and animation options.
     */
    @NonNull
    public FragmentBackStack push(@NonNull Fragment fragment, @Nullable String tag, @Nullable AnimationOptions animationOptions) {
        BackStackTrackingFragment tf = obtain();
        final Fragment currentFragment = getCurrentFragment(tf);
        if (currentFragment != null) {
            if (tf.backStack == null) {
                tf.backStack = new ArrayList<>();
            }
            final BackStackTrackingFragment.BackStackEntry entry = new BackStackTrackingFragment.BackStackEntry(currentFragment, animationOptions);
            tf.backStack.add(entry);
            if (isCurrentFragmentInFragmentManager(tf)) {
                // Detach, save state, then remove. This is to ensure onSaveInstanceState is called between onStop() and onDestroy().
                applyAnimations(fm.beginTransaction(), animationOptions)
                        .detach(currentFragment)
                        .setReorderingAllowed(true)
                        .runOnCommit(new Runnable() {
                            @Override
                            public void run() {
                                entry.savedState = fm.saveFragmentInstanceState(currentFragment);
                                fm.beginTransaction()
                                        .remove(currentFragment)
                                        .setReorderingAllowed(true)
                                        .commit();
                            }
                        })
                        .commit();
            }
        }
        FragmentTransaction transaction = applyAnimations(fm.beginTransaction(), animationOptions);
        transaction.replace(tf.getContainerId(), fragment, tag)
                .setReorderingAllowed(true)
                .setPrimaryNavigationFragment(fragment);
        transaction.commit();
        tf.currentFragment = fragment;
        return this;
    }

    /**
     * Pops the top fragment off the stack. If we are already at the first fragment given with
     * {@link #startWith(int, Fragment)} this will do nothing.
     */
    @NonNull
    public FragmentBackStack pop() {
        pop(null, false);
        return this;
    }

    /**
     * Pops the top fragment off the stack with the given animation options. If we are already at
     * the first fragment given with {@link #startWith(int, Fragment)} this will do nothing.
     */
    @NonNull
    public FragmentBackStack pop(@Nullable PopAnimationOptions animationOptions) {
        pop(animationOptions, false);
        return this;
    }

    /**
     * Immediately pops the top fragment off the stack. You should call this in your Activity's
     * {@link Activity#onBackPressed()} to handle the back button correctly. If we are already at
     * teh first fragment given with {@link #startWith(int, Fragment)} this will do nothing.
     *
     * @return true if a fragment was popped, false otherwise.
     */
    public boolean popImmediate() {
        return pop(null, true);
    }

    /**
     * Immediately pops the top fragment off the stack with the given animation options. You should call this in your Activity's
     * {@link Activity#onBackPressed()} to handle the back button correctly. If we are already at
     * teh first fragment given with {@link #startWith(int, Fragment)} this will do nothing.
     *
     * @return true if a fragment was popped, false otherwise.
     * @see PopAnimationOptions
     */
    public boolean popImmediate(@Nullable PopAnimationOptions animationOptions) {
        return pop(animationOptions, true);
    }

    private boolean pop(@Nullable PopAnimationOptions animationOptions, boolean immediate) {
        Fragment primaryNavFragment = fm.getPrimaryNavigationFragment();
        if (primaryNavFragment != null && FragmentBackStack.of(primaryNavFragment.getChildFragmentManager()).pop(null, immediate)) {
            return true;
        }
        BackStackTrackingFragment tf = obtain();
        if (tf.backStack != null && tf.backStack.size() > 0) {
            BackStackTrackingFragment.BackStackEntry lastEntry = tf.backStack.remove(tf.backStack.size() - 1);
            performPop(tf, lastEntry, animationOptions, immediate);
            return true;
        }
        return false;
    }

    /**
     * Pops up to but not including the fragment with the given tag. If the tag doesn't not exist in
     * the back-stack then nothing will happen.
     *
     * @see #pop()
     * @see #popExclusiveImmediate(String) (String)
     * @see #popInclusive(String)
     */
    @NonNull
    public FragmentBackStack popExclusive(@NonNull String tag) {
        pop(tag, false, false);
        return this;
    }

    /**
     * Pops up to and including the fragment with the given tag. If the tag doesn't not exist in
     * the back-stack then nothing will happen.
     *
     * @see #pop()
     * @see #popInclusiveImmediate(String)
     * @see #popExclusive(String)
     */
    @NonNull
    public FragmentBackStack popInclusive(@NonNull String tag) {
        pop(tag, true, false);
        return this;
    }

    /**
     * Immediately pops up to but not including the fragment with the given tag. If the tag doesn't
     * not exist in the back-stack then nothing will happen.
     *
     * @return true if a fragment was popped, false otherwise.
     * @see #pop()
     * @see #popInclusiveImmediate(String)
     * @see #popExclusive(String)
     */
    public boolean popExclusiveImmediate(@NonNull String tag) {
        return pop(tag, false, true);
    }

    /**
     * Immediately pops up to and including the fragment with the given tag. If the tag doesn't not
     * exist in the back-stack then nothing will happen.
     *
     * @return true if a fragment was popped, false otherwise.
     * @see #pop()
     * @see #popExclusiveImmediate(String)
     * @see #popInclusive(String)
     */
    public boolean popInclusiveImmediate(@NonNull String tag) {
        return pop(tag, true, true);
    }

    private boolean pop(@NonNull String tag, boolean inclusive, boolean immediate) {
        BackStackTrackingFragment tf = obtain();
        if (tf.backStack != null && tf.backStack.size() > 0) {
            BackStackTrackingFragment.BackStackEntry lastEntry = null;
            int index = -1;
            for (int i = tf.backStack.size() - 1; i >= 0; i--) {
                index = i;
                BackStackTrackingFragment.BackStackEntry entry = tf.backStack.get(i);
                if (tag.equals(entry.tag)) {
                    lastEntry = entry;
                    break;
                }
            }
            if (lastEntry != null) {
                for (int i = tf.backStack.size() - 1; i >= index; i--) {
                    tf.backStack.remove(i);
                }
                // If we are inclusive, pop one more.
                if (inclusive && tf.backStack.size() > 0) {
                    lastEntry = tf.backStack.remove(tf.backStack.size() - 1);
                }
                performPop(tf, lastEntry, null, immediate);
                return true;
            }
        }
        return false;
    }

    private void performPop(BackStackTrackingFragment tf, BackStackTrackingFragment.BackStackEntry entry, @Nullable PopAnimationOptions animationOptions, boolean immediate) {
        Fragment fragment = Fragment.instantiate(tf.getContext(), entry.name, entry.args);
        fragment.setInitialSavedState(entry.savedState);
        FragmentTransaction transaction = applyPopAnimations(fm.beginTransaction(), entry, animationOptions);
        transaction.replace(tf.getContainerId(), fragment, entry.tag)
                .setReorderingAllowed(true);
        if (immediate) {
            transaction.commitNow();
        } else {
            transaction.commit();
        }
    }

    private boolean isCurrentFragmentInFragmentManager(BackStackTrackingFragment tf) {
        return fm.findFragmentById(tf.getContainerId()) == getCurrentFragment(tf);
    }

    private BackStackTrackingFragment obtain() {
        if (tf == null) {
            tf = BackStackTrackingFragment.obtain(fm);
        }
        return tf;
    }

    @Nullable
    private Fragment getCurrentFragment(BackStackTrackingFragment tf) {
        if (tf.currentFragment == null) {
            tf.currentFragment = fm.findFragmentById(tf.getContainerId());
        }
        return tf.currentFragment;
    }

    private FragmentTransaction applyAnimations(FragmentTransaction transaction, @Nullable AnimationOptions options) {
        if (options != null) {
            transaction.setCustomAnimations(options.enter, options.exit);
            if (options.sharedElements != null) {
                for (int i = 0; i < options.sharedElements.size(); i++) {
                    View sharedElement = options.sharedElements.keyAt(i);
                    String name = options.sharedElements.valueAt(i);
                    transaction.addSharedElement(sharedElement, name);
                }
            }
        }
        return transaction;
    }

    private FragmentTransaction applyPopAnimations(FragmentTransaction transaction, BackStackTrackingFragment.BackStackEntry entry, @Nullable PopAnimationOptions options) {
        if (options != null) {
            transaction.setCustomAnimations(entry.popEnter, entry.popExit);
            if (options.sharedElements != null) {
                for (int i = 0; i < options.sharedElements.size(); i++) {
                    View sharedElement = options.sharedElements.keyAt(i);
                    String name = options.sharedElements.valueAt(i);
                    transaction.addSharedElement(sharedElement, name);
                }
            }
        }
        return transaction;
    }
}
