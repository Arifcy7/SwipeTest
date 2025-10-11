import 'dart:convert';
import 'package:http/http.dart' as http;

class ApiService {
  static const String baseUrl = 'https://app.scribehealth.ai/api';
  static const String backendUrl = 'https://medinote-backend-staging-616605604904.us-central1.run.app/api';



  Future<http.Response> get(String path, {Map<String, String>? params, bool backend = false}) async {
    final uri = Uri.parse((backend ? backendUrl : baseUrl) + path).replace(queryParameters: params);
    return http.get(uri, headers: {
      'Content-Type': 'application/json',
    });
  }

  Future<http.Response> post(String path, {Object? body, bool backend = false}) async {
    final uri = Uri.parse((backend ? backendUrl : baseUrl) + path);
    return http.post(uri, headers: {
      'Content-Type': 'application/json',
    }, body: jsonEncode(body));
  }

  Future<http.Response> putPresigned(String url, List<int> bytes, {String mimeType = 'audio/wav'}) async {
    return http.put(Uri.parse(url), headers: {
      'Content-Type': mimeType,
    }, body: bytes);
  }
}
