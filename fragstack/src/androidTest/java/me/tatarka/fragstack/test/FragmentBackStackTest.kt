package me.tatarka.fragstack.test

import android.support.test.InstrumentationRegistry
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import me.tatarka.fragstack.FragmentBackStack
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentBackStackTest {

    @get:Rule
    val activityTestRule = ActivityTestRule(EmptyActivity::class.java)

    @Test
    fun starts_with_initial_fragment() {
        val activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("first", currentFragment.name)
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart
                ), firstFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun replaces_initial_fragment_with_pushed_fragment() {
        val activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")
        val secondFragment = EmptyFragment.newInstance("second")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .push(secondFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("second", currentFragment.name)
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnSaveInstanceState(savedState = "first_state"),
                    LifecycleEvent.OnDestroy
                ), firstFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart
                ), secondFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun starts_with_immediately_pushed_fragment() {
        val activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")
        val secondFragment = EmptyFragment.newInstance("second")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
                .push(secondFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("second", currentFragment.name)
            assertEquals(emptyList<LifecycleEvent>(), firstFragment.lifecycleEvents)
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart
                ), secondFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun pops_back_to_initial_fragment_and_restores_its_state() {
        val activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")
        val secondFragment = EmptyFragment.newInstance("second")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .push(secondFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .pop()
        }

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("first", currentFragment.name)
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnSaveInstanceState(savedState = "first_state"),
                    LifecycleEvent.OnDestroy
                ), firstFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnDestroy
                ), secondFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = "first_state"),
                    LifecycleEvent.OnStart
                ), currentFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun pops_back_to_initial_fragment_that_was_immediately_pushed_onto() {
        val activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")
        val secondFragment = EmptyFragment.newInstance("second")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
                .push(secondFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .pop()
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("first", currentFragment.name)
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart
                ), currentFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnDestroy
                ), secondFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun pushes_two_fragments_onto_initial_and_pops_them() {
        val activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")
        val secondFragment = EmptyFragment.newInstance("second")
        val thirdFragment = EmptyFragment.newInstance("third")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
                .push(secondFragment)
                .push(thirdFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .pop()
                .pop()
        }

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("first", currentFragment.name)
            assertEquals(
                emptyList<LifecycleEvent>(), firstFragment.lifecycleEvents
            )
            assertEquals(
                emptyList<LifecycleEvent>(), secondFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnDestroy
                ), thirdFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart
                ), currentFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun pushes_two_fragments_after_initial() {
        val activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")
        val secondFragment = EmptyFragment.newInstance("second")
        val thirdFragment = EmptyFragment.newInstance("third")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .push(secondFragment)
                .push(thirdFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .pop()
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("second", currentFragment.name)
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnSaveInstanceState(savedState = "first_state"),
                    LifecycleEvent.OnDestroy
                ), firstFragment.lifecycleEvents
            )
            assertEquals(
                emptyList<LifecycleEvent>(), secondFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnDestroy
                ), thirdFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart
                ), currentFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun pops_correctly_after_config_change() {
        var activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")
        val secondFragment = EmptyFragment.newInstance("second")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .push(secondFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            activity.recreate()
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        var newSecondFragment: EmptyFragment? = null
        activityTestRule.runOnUiThread {
            activity = activityTestRule.activity
            // Fragment was recreated so obtain the new one
            newSecondFragment =
                    activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment?
            FragmentBackStack.of(activity.supportFragmentManager)
                .pop()
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("first", currentFragment.name)
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnSaveInstanceState(savedState = "first_state"),
                    LifecycleEvent.OnDestroy
                ), firstFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnSaveInstanceState(savedState = "second_state"),
                    LifecycleEvent.OnDestroy
                ), secondFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = "second_state"),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnDestroy
                ), newSecondFragment!!.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = "first_state"),
                    LifecycleEvent.OnStart
                ), currentFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun pops_to_tag_inclusive() {
        val activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")
        val secondFragment = EmptyFragment.newInstance("second")
        val thirdFragment = EmptyFragment.newInstance("third")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
                .push(secondFragment, "second_tag")
                .push(thirdFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .popInclusive("second_tag")
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("first", currentFragment.name)
            assertEquals(
                emptyList<LifecycleEvent>(), secondFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnDestroy
                ), thirdFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart
                ), currentFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun pops_to_tag_exclusive() {
        val activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")
        val secondFragment = EmptyFragment.newInstance("second")
        val thirdFragment = EmptyFragment.newInstance("third")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
                .push(secondFragment, "second_tag")
                .push(thirdFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .popExclusive("second_tag")
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("second", currentFragment.name)
            assertEquals(
                emptyList<LifecycleEvent>(), firstFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnDestroy
                ), thirdFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart
                ), currentFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun back_press_performs_immediate_pop() {
        val activity = activityTestRule.activity

        val firstFragment = EmptyFragment.newInstance("first")
        val secondFragment = EmptyFragment.newInstance("second")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, firstFragment)
                .push(secondFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            activity.onBackPressed()
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            val currentFragment =
                activity.supportFragmentManager.findFragmentById(android.R.id.content) as EmptyFragment
            assertEquals("first", currentFragment.name)
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnDestroy
                ), secondFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart
                ), currentFragment.lifecycleEvents
            )
        }
    }

    @Test
    fun back_press_delegates_to_primary_child_fragment_back_stack() {
        val activity = activityTestRule.activity

        val parentFragment = EmptyFragment.newInstance("parent")
        val firstChildFragment = EmptyFragment.newInstance("firstChild")
        val secondChildFragment = EmptyFragment.newInstance("secondChild")

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(activity.supportFragmentManager)
                .startWith(android.R.id.content, parentFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            FragmentBackStack.of(parentFragment.childFragmentManager)
                .startWith(R.id.container, firstChildFragment)
                .push(secondChildFragment)
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            activity.onBackPressed()
        }

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        activityTestRule.runOnUiThread {
            val currentChildFragment =
                parentFragment.childFragmentManager.findFragmentById(R.id.container) as EmptyFragment
            assertEquals("firstChild", currentChildFragment.name)
            assertEquals(listOf(
                LifecycleEvent.OnCreate(savedState = null),
                LifecycleEvent.OnStart
            ), parentFragment.lifecycleEvents)
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart,
                    LifecycleEvent.OnStop,
                    LifecycleEvent.OnDestroy
                ), secondChildFragment.lifecycleEvents
            )
            assertEquals(
                listOf(
                    LifecycleEvent.OnCreate(savedState = null),
                    LifecycleEvent.OnStart
                ), currentChildFragment.lifecycleEvents
            )
        }
    }
}
