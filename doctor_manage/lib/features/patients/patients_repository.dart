import 'dart:convert';
import '../../services/api_service.dart';

class PatientsRepository {
  final ApiService api;
  PatientsRepository(this.api);

  Future<List<Map<String, dynamic>>> getPatients(String userId) async {
    final res = await api.get('/v1/patients', params: {'userId': userId});
    if (res.statusCode == 200) {
      final data = res.body;
      return List<Map<String, dynamic>>.from(jsonDecode(data)['patients']);
    }
    throw Exception('Failed to fetch patients');
  }

  Future<Map<String, dynamic>> addPatient(String name, String userId) async {
    final res = await api.post('/v1/add-patient-ext', body: {'name': name, 'userId': userId});
    if (res.statusCode == 201) {
      return jsonDecode(res.body)['patient'];
    }
    throw Exception('Failed to add patient');
  }

  Future<Map<String, dynamic>> getPatientDetails(String patientId) async {
    final res = await api.get('/v1/patient-details/$patientId');
    if (res.statusCode == 200) {
      return jsonDecode(res.body);
    }
    throw Exception('Failed to fetch patient details');
  }
}
