@file:Suppress("NOTHING_TO_INLINE")

package me.tatarka.fragstack.ktx

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import me.tatarka.fragstack.AnimationOptions
import me.tatarka.fragstack.FragmentBackStack
import me.tatarka.fragstack.PopAnimationOptions

inline val FragmentActivity.backStack
    get(): FragmentBackStack =
        FragmentBackStack.of(supportFragmentManager)

inline val Fragment.backStack get(): FragmentBackStack = FragmentBackStack.of(childFragmentManager)

inline val Fragment.parentBackStack get(): FragmentBackStack = FragmentBackStack.of(fragmentManager)

inline fun FragmentBackStack.push(
    fragment: Fragment,
    animationOptions: AnimationOptions.() -> Unit
): FragmentBackStack = push(fragment, null, AnimationOptions().apply(animationOptions))

inline fun FragmentBackStack.push(
    fragment: Fragment,
    tag: String?,
    animationOptions: AnimationOptions.() -> Unit
): FragmentBackStack = push(fragment, tag, AnimationOptions().apply(animationOptions))

inline fun FragmentBackStack.pop(animationOptions: PopAnimationOptions.() -> Unit): FragmentBackStack =
    pop(PopAnimationOptions().apply(animationOptions))

inline fun FragmentBackStack.popImmediate(animationOptions: PopAnimationOptions.() -> Unit): Boolean =
    popImmediate(PopAnimationOptions().apply(animationOptions))
