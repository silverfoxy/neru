package com.y3owk1n.neru.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Entry point for a Key Mapper or shell broadcast activation shortcut. */
class NeruCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NeruAccessibilityService.ACTION_TOGGLE_GRID) {
            NeruAccessibilityService.toggleFromExternalShortcut()
        }
    }
}
