package com.example.doctor_manage

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import io.flutter.embedding.android.FlutterActivity // Added this import
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
	private val AUDIO_CHANNEL = "ai_scribe_copilot/audio"
	private val DND_CHANNEL = "ai_scribe_copilot/dnd"
	private val INTERRUPTION_CHANNEL = "ai_scribe_copilot/interruption"

	override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
		super.configureFlutterEngine(flutterEngine)
		MethodChannel(flutterEngine.dartExecutor.binaryMessenger, AUDIO_CHANNEL).setMethodCallHandler { call, result ->
			when (call.method) {
				"startBackgroundService" -> {
					// TODO: Start foreground service for audio
					result.success(true)
				}
				"stopBackgroundService" -> {
					// TODO: Stop foreground service
					result.success(true)
				}
				"switchAudioRoute" -> {
					val route = call.argument<String>("route")
					// TODO: Switch audio route (Bluetooth/wired)
					result.success(true)
				}
				else -> result.notImplemented()
			}
		}

		MethodChannel(flutterEngine.dartExecutor.binaryMessenger, DND_CHANNEL).setMethodCallHandler { call, result ->
			if (call.method == "isDoNotDisturbEnabled") {
				val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
				val enabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					notificationManager.currentInterruptionFilter != android.app.NotificationManager.INTERRUPTION_FILTER_ALL
				} else {
					Settings.Global.getInt(contentResolver, "zen_mode", 0) != 0
				}
				result.success(enabled)
			} else {
				result.notImplemented()
			}
		}

		// TODO: Listen for phone call/interruption events and send to Flutter via INTERRUPTION_CHANNEL
	}
}
