package me.tatarka.fragstack.test

sealed class LifecycleEvent {
    data class OnCreate(val savedState: String?) : LifecycleEvent()
    object OnStart : LifecycleEvent()
    object OnStop : LifecycleEvent()
    data class OnSaveInstanceState(val savedState: String?) : LifecycleEvent()
    object OnDestroy : LifecycleEvent()
}