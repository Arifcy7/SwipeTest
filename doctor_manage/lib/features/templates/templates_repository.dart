import 'dart:convert';
import '../../services/api_service.dart';

class TemplatesRepository {
  final ApiService api;
  TemplatesRepository(this.api);

  Future<List<Map<String, dynamic>>> getTemplates(String userId) async {
    final res = await api.get('/v1/fetch-default-template-ext', params: {'userId': userId});
    if (res.statusCode == 200) {
      return List<Map<String, dynamic>>.from(jsonDecode(res.body)['data']);
    }
    throw Exception('Failed to fetch templates');
  }
}
