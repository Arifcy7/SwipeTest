import 'package:flutter/services.dart';

/// Handles platform channel calls for native background/foreground audio recording and headset switching.
class NativeAudioPlatform {
  static const MethodChannel _channel = MethodChannel('ai_scribe_copilot/audio');

  /// Start background/foreground audio service (Android/iOS)
  static Future<void> startBackgroundService() async {
    await _channel.invokeMethod('startBackgroundService');
  }

  /// Stop background/foreground audio service
  static Future<void> stopBackgroundService() async {
    await _channel.invokeMethod('stopBackgroundService');
  }

  /// Switch audio route (Bluetooth/wired)
  static Future<void> switchAudioRoute(String route) async {
    await _channel.invokeMethod('switchAudioRoute', {'route': route});
  }

  /// Check if running in background
  static Future<bool> isInBackground() async {
    final result = await _channel.invokeMethod('isInBackground');
    return result == true;
  }
}
