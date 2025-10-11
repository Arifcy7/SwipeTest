import 'package:flutter/services.dart';

class DoNotDisturbHelper {
  static const MethodChannel _channel = MethodChannel('ai_scribe_copilot/dnd');

  static Future<bool> isDoNotDisturbEnabled() async {
    final result = await _channel.invokeMethod('isDoNotDisturbEnabled');
    return result == true;
  }
}
