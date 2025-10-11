import 'package:flutter/services.dart';

class HapticFeedbackHelper {
  static void vibrate() {
    HapticFeedback.mediumImpact();
  }

  static void success() {
    HapticFeedback.lightImpact();
  }

  static void error() {
    HapticFeedback.heavyImpact();
  }
}
