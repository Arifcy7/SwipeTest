import 'dart:typed_data';
import '../backend/backend_api_service.dart';
import 'audio_chunk_uploader.dart';

/// Example usage: wire this into AudioStreamingService to send chunks as they are recorded.
class AudioUploadManager {
  final String userId;
  final String baseUrl;
  late final BackendApiService apiService;
  late final AudioChunkUploader uploader;

  AudioUploadManager({required this.userId, required this.baseUrl}) {
    apiService = BackendApiService(baseUrl: baseUrl);
    uploader = AudioChunkUploader(apiService: apiService, userId: userId, baseUrl: baseUrl);
  }

  Future<void> start({
    required String patientId,
    required String patientName,
    required String templateId,
    required String templateTitle,
  }) async {
    // Use current time as ISO8601 string for startTime
    final startTime = DateTime.now().toUtc().toIso8601String();
    await uploader.startSession(
      patientId_: patientId,
      patientName_: patientName,
      templateId_: templateId,
      startTime_: startTime,
    );
    await uploader.recoverUnsentChunks();
  }

  Future<void> sendChunk(Uint8List chunk) async {
    await uploader.queueChunk(chunk);
  }
}
