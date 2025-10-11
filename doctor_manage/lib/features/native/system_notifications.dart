import 'package:flutter_local_notifications/flutter_local_notifications.dart';

class SystemNotifications {
  static final FlutterLocalNotificationsPlugin _plugin = FlutterLocalNotificationsPlugin();

  static Future<void> initialize() async {
    const AndroidInitializationSettings androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');
    const InitializationSettings settings = InitializationSettings(android: androidSettings);
    await _plugin.initialize(settings);
  }

  static Future<void> showNotification({required int id, required String title, required String body, List<AndroidNotificationAction>? actions}) async {
    final androidDetails = AndroidNotificationDetails(
      'ai_scribe_channel',
      'AI Scribe Notifications',
      channelDescription: 'Notifications for AI Scribe Copilot',
      importance: Importance.max,
      priority: Priority.high,
      actions: actions,
    );
    final details = NotificationDetails(android: androidDetails);
    await _plugin.show(id, title, body, details);
  }
}
