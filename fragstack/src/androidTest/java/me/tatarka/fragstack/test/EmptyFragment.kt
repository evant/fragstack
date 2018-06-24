package me.tatarka.fragstack.test

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.tatarka.fragstack.test.R

private const val STATE = "state"

class EmptyFragment : Fragment() {

    val name: String
        get() = arguments!!.getString("name")

    val lifecycleEvents = mutableListOf<LifecycleEvent>()

    companion object {
        fun newInstance(name: String) = EmptyFragment().apply {
            arguments = Bundle().apply {
                putString("name", name)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleEvents += LifecycleEvent.OnCreate(savedState = savedInstanceState?.getString(STATE))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.empty_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        lifecycleEvents += LifecycleEvent.OnStart
    }

    override fun onStop() {
        super.onStop()
        lifecycleEvents += LifecycleEvent.OnStop
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE, "${name}_state")
        lifecycleEvents += LifecycleEvent.OnSaveInstanceState(savedState = "${name}_state")
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleEvents += LifecycleEvent.OnDestroy
    }

    override fun toString(): String {
        return "${super.toString()} ($name)"
    }
}