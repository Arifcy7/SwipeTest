import 'dart:convert';
import '../../services/api_service.dart';

class SessionsRepository {
  final ApiService api;
  SessionsRepository(this.api);

  Future<List<Map<String, dynamic>>> getSessionsByPatient(String patientId) async {
    final res = await api.get('/v1/fetch-session-by-patient/$patientId');
    if (res.statusCode == 200) {
      return List<Map<String, dynamic>>.from(jsonDecode(res.body)['sessions']);
    }
    throw Exception('Failed to fetch sessions');
  }

  Future<List<Map<String, dynamic>>> getAllSessions(String userId) async {
    final res = await api.get('/v1/all-session', params: {'userId': userId});
    if (res.statusCode == 200) {
      return List<Map<String, dynamic>>.from(jsonDecode(res.body)['sessions']);
    }
    throw Exception('Failed to fetch all sessions');
  }

  Future<String> createSession({
    required String patientId,
    required String userId,
    required String patientName,
    required String templateId,
    required String startTime,
  }) async {
    final res = await api.post('/v1/upload-session', body: {
      'patientId': patientId,
      'userId': userId,
      'patientName': patientName,
      'status': 'recording',
      'startTime': startTime,
      'templateId': templateId,
    });
    if (res.statusCode == 201) {
      return jsonDecode(res.body)['id'];
    }
    throw Exception('Failed to create session');
  }
}
