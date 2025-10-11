import 'dart:async';
import 'package:flutter/services.dart';

/// Handles interruption events from the native side (phone calls, app switching, etc).
class InterruptionHandler {
  static const MethodChannel _channel = MethodChannel('ai_scribe_copilot/interruption');
  static final StreamController<String> _eventController = StreamController.broadcast();
  static Stream<String> get events => _eventController.stream;

  static void initialize() {
    _channel.setMethodCallHandler((call) async {
      if (call.method == 'onPhoneCall') {
        _eventController.add('phone_call');
      } else if (call.method == 'onAppSwitch') {
        _eventController.add('app_switch');
      } else if (call.method == 'onAppKilled') {
        _eventController.add('app_killed');
      } else if (call.method == 'onPhoneRestart') {
        _eventController.add('phone_restart');
      }
    });
  }

  static void dispose() {
    _eventController.close();
  }
}
