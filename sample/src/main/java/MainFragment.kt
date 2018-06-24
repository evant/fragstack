package me.tatarka.fragstack.sample

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.transition.ChangeBounds
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.tatarka.fragstack.ktx.parentBackStack
import me.tatarka.fragstack.ktx.pop
import me.tatarka.fragstack.ktx.push

private const val ARG_COUNT = "count"

const val USE_NEW = true

class MainFragment : Fragment() {

    companion object {
        fun newInstance(count: Int) = MainFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_COUNT, count)
            }
        }
    }

    var count: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        count = arguments!!.getInt(ARG_COUNT)

        sharedElementEnterTransition = ChangeBounds()
        if (!USE_NEW) {
            sharedElementReturnTransition = ChangeBounds()
        }

        Log.d("LIFECYCLE", "${toString()} onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val countView = view.findViewById<TextView>(R.id.count)
        countView.text = count.toString()

        ViewCompat.setTransitionName(countView, "count")

        (countView.layoutParams as ConstraintLayout.LayoutParams).horizontalBias =
                if (count % 2 == 0) 0f else 1f

        view.findViewById<View>(R.id.push).setOnClickListener {
            if (USE_NEW) {
                pushNew(view)
            } else {
                pushOld(view)
            }
        }
        view.findViewById<View>(R.id.pop).setOnClickListener {
            if (USE_NEW) {
                popNew(view)
            } else {
                popOld(view)
            }
        }
    }

    private fun pushOld(view: View) {
        val countView = view.findViewById<View>(R.id.count)
        val sharedTransition = (activity as MainActivity).sharedElementTransition
        fragmentManager!!.beginTransaction()
            .apply {
                if (sharedTransition) {
                    addSharedElement(countView, "count")
                } else {
                    setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                }
            }
            .replace(R.id.content, MainFragment.newInstance(count + 1))
            .addToBackStack(null)
            .setReorderingAllowed(true)
            .commit()
    }

    private fun pushNew(view: View) {
        val countView = view.findViewById<View>(R.id.count)
        val sharedTransition = (activity as MainActivity).sharedElementTransition
        parentBackStack.push(MainFragment.newInstance(count + 1)) {
            if (sharedTransition) {
                addSharedElement(countView, "count")
            } else {
                setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
            }
        }
    }

    private fun popOld(view: View) {
        fragmentManager!!.popBackStack()
    }

    private fun popNew(view: View) {
        val countView = view.findViewById<View>(R.id.count)
        parentBackStack.pop {
            addSharedElement(countView, "count")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("LIFECYCLE", "${toString()} onStart")
    }

    override fun onStop() {
        super.onStop()
        Log.d("LIFECYCLE", "${toString()} onStop")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("LIFECYCLE", "${toString()} onSaveInstanceState")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LIFECYCLE", "${toString()} onDestroy")
    }

    override fun toString(): String {
        return "${super.toString()} ($count)"
    }
}
