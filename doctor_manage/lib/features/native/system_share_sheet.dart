import 'package:share_plus/share_plus.dart';

class SystemShareSheet {
  static Future<void> shareText(String text) async {
    await Share.share(text);
  }

  static Future<void> shareFile(String filePath, {String? text}) async {
    await Share.shareXFiles([XFile(filePath)], text: text);
  }
}
