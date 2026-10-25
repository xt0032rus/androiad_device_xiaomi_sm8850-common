/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.nullcode.misound.xiaomi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "XiaomiMiSound-Boot"

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        when (intent.action) {
            // Start as early as possible (Direct Boot) so boot sounds and the
            // lock screen already get MiSound/EQ; BOOT_COMPLETED is the
            // fallback when the first attempt failed.
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_BOOT_COMPLETED -> start(context)
        }
    }

    private fun start(context: Context) {
        if (!started.compareAndSet(false, true)) {
            Log.d(TAG, "already started, ignoring")
            return
        }
        try {
            Log.i(TAG, "Starting MiSound controller")
            val controller = MiSoundController.getInstance(context)
            controller.onBootCompleted()
            controller.registerSettingsObserver()

            Log.i(TAG, "Starting earpiece EQ controller")
            val eqController = MiSoundEqController.getInstance(context)
            eqController.onBootCompleted()
        } catch (t: Throwable) {
            // Allow the next boot broadcast to retry.
            started.set(false)
            Log.e(TAG, "start failed", t)
        }
    }

    companion object {
        private val started = AtomicBoolean(false)
    }
}
