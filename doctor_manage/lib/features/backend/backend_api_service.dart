import 'dart:convert';
import 'dart:typed_data';
import 'package:http/http.dart' as http;

class BackendApiService {
  final String baseUrl;
  BackendApiService({required this.baseUrl});

  Future<String> startUploadSession({
    required String patientId,
    required String userId,
    required String patientName,
    required String templateId,
    required String startTime,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/v1/upload-session'),
      body: jsonEncode({
        'patientId': patientId,
        'userId': userId,
        'patientName': patientName,
        'status': 'recording',
        'startTime': startTime,
        'templateId': templateId,
      }),
      headers: {'Content-Type': 'application/json'},
    );
    if (response.statusCode == 201) {
      final data = jsonDecode(response.body);
      return data['id'] as String;
    } else {
      throw Exception('Failed to start upload session');
    }
  }

  Future<Map<String, dynamic>> getPresignedUrl({
    required String sessionId,
    required int chunkNumber,
    String mimeType = 'audio/wav',
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/v1/get-presigned-url'),
      body: jsonEncode({
        'sessionId': sessionId,
        'chunkNumber': chunkNumber,
        'mimeType': mimeType,
      }),
      headers: {'Content-Type': 'application/json'},
    );
    if (response.statusCode == 200) {
      return jsonDecode(response.body) as Map<String, dynamic>;
    } else {
      throw Exception('Failed to get presigned URL');
    }
  }

  Future<void> uploadAudioChunk(String presignedUrl, Uint8List chunkData, {String mimeType = 'audio/wav', String? authToken}) async {
    final headers = <String, String>{'Content-Type': mimeType};
    if (authToken != null) headers['Authorization'] = 'Bearer $authToken';
    final response = await http.put(
      Uri.parse(presignedUrl),
      body: chunkData,
      headers: headers,
    );
    if (response.statusCode != 200 && response.statusCode != 201) {
      throw Exception('Failed to upload audio chunk');
    }
  }

  Future<void> notifyChunkUploaded({
    required String sessionId,
    required String gcsPath,
    required int chunkNumber,
    required bool isLast,
    required int totalChunksClient,
    required String publicUrl,
    required String mimeType,
    required String selectedTemplate,
    required String selectedTemplateId,
    String model = 'fast',
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/v1/notify-chunk-uploaded'),
      body: jsonEncode({
        'sessionId': sessionId,
        'gcsPath': gcsPath,
        'chunkNumber': chunkNumber,
        'isLast': isLast,
        'totalChunksClient': totalChunksClient,
        'publicUrl': publicUrl,
        'mimeType': mimeType,
        'selectedTemplate': selectedTemplate,
        'selectedTemplateId': selectedTemplateId,
        'model': model,
      }),
      headers: {'Content-Type': 'application/json'},
    );
    if (response.statusCode != 200) {
      throw Exception('Failed to notify chunk uploaded');
    }
  }

  Future<List<dynamic>> fetchPatients(String userId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/v1/patients?userId=$userId'),
    );
    if (response.statusCode == 200) {
      return jsonDecode(response.body) as List<dynamic>;
    } else {
      throw Exception('Failed to fetch patients');
    }
  }

  Future<void> addPatient(Map<String, dynamic> patientData) async {
    final response = await http.post(
      Uri.parse('$baseUrl/v1/add-patient-ext'),
      body: jsonEncode(patientData),
      headers: {'Content-Type': 'application/json'},
    );
    if (response.statusCode != 200) {
      throw Exception('Failed to add patient');
    }
  }

  Future<List<dynamic>> fetchSessionsByPatient(String patientId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/v1/fetch-session-by-patient/$patientId'),
    );
    if (response.statusCode == 200) {
      return jsonDecode(response.body) as List<dynamic>;
    } else {
      throw Exception('Failed to fetch sessions for patient');
    }
  }
}
