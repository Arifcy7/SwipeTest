import 'dart:convert';
import '../../services/api_service.dart';

class AudioChunkRepository {
  final ApiService api;
  AudioChunkRepository(this.api);

  Future<Map<String, dynamic>> getPresignedUrl(String sessionId, int chunkNumber, String mimeType) async {
    final res = await api.post('/v1/get-presigned-url', body: {
      'sessionId': sessionId,
      'chunkNumber': chunkNumber,
      'mimeType': mimeType,
    });
    if (res.statusCode == 200) {
      return jsonDecode(res.body);
    }
    throw Exception('Failed to get presigned URL');
  }

  Future<void> uploadChunk(String url, List<int> bytes, {String mimeType = 'audio/wav'}) async {
    final res = await api.putPresigned(url, bytes, mimeType: mimeType);
    if (res.statusCode != 200) {
      throw Exception('Failed to upload chunk');
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
    final res = await api.post('/v1/notify-chunk-uploaded', body: {
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
    });
    if (res.statusCode != 200) {
      throw Exception('Failed to notify chunk uploaded');
    }
  }
}
