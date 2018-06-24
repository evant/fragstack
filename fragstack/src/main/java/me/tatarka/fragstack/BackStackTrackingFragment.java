package me.tatarka.fragstack;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;

/**
 * Internal fragment uses to save back-stack state.
 */
public final class BackStackTrackingFragment extends Fragment {
    static final String TAG = "me.tatarka.fragstack.BackStackTrackingFragment";
    static final String STATE_BACK_STACK = "backStack";
    static final String STATE_CONTAINER_ID = "containerId";

    static BackStackTrackingFragment obtain(FragmentManager fm) {
        BackStackTrackingFragment fragment = (BackStackTrackingFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new BackStackTrackingFragment();
            fm.beginTransaction().add(fragment, TAG).commitNow();
        }
        return fragment;
    }

    @Nullable
    Fragment currentFragment;
    private int containerId = -1;
    @Nullable
    ArrayList<BackStackEntry> backStack;

    void setContainerId(@IdRes int containerId) {
        this.containerId = containerId;
    }

    int getContainerId() {
        if (containerId == -1) {
            throw new IllegalStateException("Missing containerId, make sure you've initially called sFragmentBackStack#startWith(containerId, startingFragment) to setup the initial fragment.");
        }
        return containerId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            backStack = savedInstanceState.getParcelableArrayList(STATE_BACK_STACK);
            containerId = savedInstanceState.getInt(STATE_CONTAINER_ID);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (backStack != null) {
            outState.putParcelableArrayList(STATE_BACK_STACK, backStack);
            outState.putInt(STATE_CONTAINER_ID, containerId);
        }
    }

    public final static class BackStackEntry implements Parcelable {
        final String name;
        @Nullable
        final String tag;
        @Nullable
        final Bundle args;
        int popEnter;
        int popExit;
        @Nullable
        Fragment.SavedState savedState;

        BackStackEntry(Fragment fragment, @Nullable AnimationOptions animationOptions) {
            this.name = fragment.getClass().getName();
            this.tag = fragment.getTag();
            this.args = fragment.getArguments();
            if (animationOptions != null) {
                popEnter = animationOptions.popEnter;
                popExit = animationOptions.popExit;
            }
        }

        BackStackEntry(Parcel in) {
            name = in.readString();
            tag = in.readString();
            args = in.readBundle(getClass().getClassLoader());
            popEnter = in.readInt();
            popExit = in.readInt();
            savedState = in.readParcelable(SavedState.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeString(tag);
            dest.writeBundle(args);
            dest.writeInt(popEnter);
            dest.writeInt(popExit);
            dest.writeParcelable(savedState, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<BackStackEntry> CREATOR = new Creator<BackStackEntry>() {
            @Override
            public BackStackEntry createFromParcel(Parcel in) {
                return new BackStackEntry(in);
            }

            @Override
            public BackStackEntry[] newArray(int size) {
                return new BackStackEntry[size];
            }
        };
    }
}
