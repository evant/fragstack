package me.tatarka.fragstack.test

import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import me.tatarka.fragstack.FragmentBackStack

class EmptyActivity : FragmentActivity() {

    override fun onStop() {
        super.onStop()
        Log.d(toString(), "onStop")
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Log.d(toString(), "onSaveInstanceState")
    }

    override fun onBackPressed() {
        if (!FragmentBackStack.of(supportFragmentManager).popImmediate()) {
            super.onBackPressed()
        }
    }
}