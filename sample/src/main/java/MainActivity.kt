package me.tatarka.fragstack.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import me.tatarka.fragstack.ktx.backStack

class MainActivity : AppCompatActivity() {

    var sharedElementTransition = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<CheckBox>(R.id.shared_element).setOnCheckedChangeListener { _, checked ->
            sharedElementTransition = checked
        }
        backStack.startWith(R.id.content, MainFragment.newInstance(0))
    }

    override fun onStop() {
        super.onStop()
        Log.d("LIFECYCLE", "${toString()} onStop")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("LIFECYCLE", "${toString()} onSaveInstanceState")
    }

    override fun onBackPressed() {
        if (!backStack.popImmediate()) {
            super.onBackPressed()
        }
    }
}
