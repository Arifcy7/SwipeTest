import 'dart:async';
import 'dart:typed_data';
import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:io';
import '../backend/backend_api_service.dart';

/// Handles chunk queueing, retry, and recovery for audio uploads.
class AudioChunkUploader {
  final BackendApiService apiService;
  final String userId;
  final String baseUrl;
  String? sessionId;
  int chunkNumber = 0;
  final List<_QueuedChunk> _queue = [];
  bool _uploading = false;
  String? patientId;
  String? patientName;
  String? templateId;
  String? startTime;

  AudioChunkUploader({required this.apiService, required this.userId, required this.baseUrl});

  Future<void> startSession({
    required String patientId_,
    required String patientName_,
    required String templateId_,
    required String startTime_,
  }) async {
    patientId = patientId_;
    patientName = patientName_;
    templateId = templateId_;
    startTime = startTime_;
    sessionId = await apiService.startUploadSession(
      patientId: patientId!,
      userId: userId,
      patientName: patientName!,
      templateId: templateId!,
      startTime: startTime!,
    );
    chunkNumber = 0;
  }

  Future<void> queueChunk(Uint8List chunk) async {
    final tempDir = await getTemporaryDirectory();
    final file = File('${tempDir.path}/audio_chunk_${DateTime.now().millisecondsSinceEpoch}.wav');
    await file.writeAsBytes(chunk);
    _queue.add(_QueuedChunk(file.path, chunkNumber));
    chunkNumber++;
    _tryUpload();
  }

  Future<void> _tryUpload() async {
    if (_uploading || _queue.isEmpty || sessionId == null) return;
    _uploading = true;
    while (_queue.isNotEmpty) {
      final chunk = _queue.first;
      if (!await _isNetworkAvailable()) break;
      try {
        final presigned = await apiService.getPresignedUrl(
          sessionId: sessionId!,
          chunkNumber: chunk.index,
          mimeType: 'audio/wav',
        );
        await apiService.uploadAudioChunk(
          presigned['url'],
          await File(chunk.path).readAsBytes(),
          mimeType: 'audio/wav',
        );
        await apiService.notifyChunkUploaded(
          sessionId: sessionId!,
          gcsPath: presigned['gcsPath'],
          chunkNumber: chunk.index,
          isLast: false, // Set true for last chunk
          totalChunksClient: chunkNumber,
          publicUrl: presigned['publicUrl'],
          mimeType: 'audio/wav',
          selectedTemplate: templateId ?? '',
          selectedTemplateId: templateId ?? '',
        );
        await File(chunk.path).delete();
        _queue.removeAt(0);
      } catch (e) {
        // Retry on next network available
        break;
      }
    }
    _uploading = false;
  }

  Future<void> recoverUnsentChunks() async {
    final tempDir = await getTemporaryDirectory();
    final files = tempDir.listSync().whereType<File>().where((f) => f.path.contains('audio_chunk_'));
    for (final file in files) {
      final idx = int.tryParse(file.path.split('_').last.split('.').first) ?? chunkNumber;
      _queue.add(_QueuedChunk(file.path, idx));
    }
    _tryUpload();
  }

  Future<bool> _isNetworkAvailable() async {
    final result = await Connectivity().checkConnectivity();
    return result != ConnectivityResult.none;
  }
}

class _QueuedChunk {
  final String path;
  final int index;
  _QueuedChunk(this.path, this.index);
}
