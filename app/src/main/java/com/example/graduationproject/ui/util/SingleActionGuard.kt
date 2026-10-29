package com.example.graduationproject.ui.util

/**
 * Synchronous, action-scoped guard for operations that must not overlap.
 *
 * The guard is deliberately independent of Compose recomposition: the first handler acquires it
 * before starting navigation or asynchronous work, so another event delivered in the same frame
 * is rejected as well.
 */
internal class SingleActionGuard {
    private var inProgress = false

    @Synchronized
    fun tryStart(): Boolean {
        if (inProgress) return false
        inProgress = true
        return true
    }

    @Synchronized
    fun finish() {
        inProgress = false
    }
}
