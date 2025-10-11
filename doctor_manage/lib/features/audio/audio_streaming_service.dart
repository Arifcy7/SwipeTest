import 'dart:async';
import 'dart:typed_data';
import 'audio_upload_manager.dart';
import 'package:flutter_sound/flutter_sound.dart';
import 'package:path_provider/path_provider.dart';
import 'dart:io';

/// Handles real-time audio recording, chunking, and streaming.
/// Platform-specific background/foreground and headset switching will be handled via platform channels.
class AudioStreamingService {
  // Singleton pattern
  static final AudioStreamingService _instance = AudioStreamingService._internal();
  factory AudioStreamingService() => _instance;
  AudioStreamingService._internal();

  // Stream controller for audio level visualization
  final StreamController<double> _audioLevelController = StreamController.broadcast();
  Stream<double> get audioLevelStream => _audioLevelController.stream;

  // Stream controller for outgoing audio chunks
  final StreamController<Uint8List> _audioChunkController = StreamController.broadcast();
  Stream<Uint8List> get audioChunkStream => _audioChunkController.stream;

  bool _isRecording = false;
  bool get isRecording => _isRecording;

  // Upload manager instance (should be initialized with userId/baseUrl before use)
  AudioUploadManager? uploadManager;

  FlutterSoundRecorder? _recorder;
  StreamSubscription? _recorderSubscription;
  Timer? _chunkTimer;
  double _gain = 1.0;
  File? _recordFile;
  int _lastReadPos = 0;

  // Start recording and streaming audio in chunks
  Future<void> startRecording({
    double gain = 1.0,
    required String userId,
    required String baseUrl,
    required String patientId,
    required String patientName,
    required String templateId,
    required String templateTitle,
  }) async {
    uploadManager ??= AudioUploadManager(userId: userId, baseUrl: baseUrl);
    await uploadManager!.start(
      patientId: patientId,
      patientName: patientName,
      templateId: templateId,
      templateTitle: templateTitle,
    );
    _isRecording = true;
    _gain = gain;
    _recorder ??= FlutterSoundRecorder();
    await _recorder!.openRecorder();
    final dir = await getTemporaryDirectory();
    _recordFile = File('${dir.path}/aiscribe_recording.pcm');
    if (_recordFile!.existsSync()) _recordFile!.deleteSync();
    await _recorder!.startRecorder(
      toFile: _recordFile!.path,
      codec: Codec.pcm16,
      numChannels: 1,
      sampleRate: 16000,
    );
    // Audio level visualization
    _recorderSubscription = _recorder!.onProgress?.listen((event) {
      final level = event.decibels != null ? (event.decibels! + 60) / 60 : 0.0;
      _audioLevelController.add(level.clamp(0.0, 1.0));
    });
    // Periodically read new data and send as chunks
    _lastReadPos = 0;
    _chunkTimer = Timer.periodic(const Duration(milliseconds: 800), (_) async {
      if (!_isRecording || _recordFile == null || !(_recordFile!.existsSync())) return;
      final bytes = await _recordFile!.readAsBytes();
      if (bytes.length > _lastReadPos) {
        final chunk = bytes.sublist(_lastReadPos);
        final amplified = _applyGain(Uint8List.fromList(chunk), _gain);
        _audioChunkController.add(amplified);
        await uploadManager!.sendChunk(amplified);
        _lastReadPos = bytes.length;
      }
    });
  }

  // Stop recording
  Future<void> stopRecording() async {
    _isRecording = false;
    await _recorderSubscription?.cancel();
    _recorderSubscription = null;
  _chunkTimer?.cancel();
    _chunkTimer = null;
    await _recorder?.stopRecorder();
    await _recorder?.closeRecorder();
    _recorder = null;
    _recordFile = null;
    _lastReadPos = 0;
  }

  // Pause recording (for phone call interruptions)
  Future<void> pauseRecording() async {
    // TODO: Implement pause
    // ...existing code...
  }

  // Resume recording
  Future<void> resumeRecording() async {
    // TODO: Implement resume
    // ...existing code...
  }

  // Dispose controllers
  void dispose() {
    _audioLevelController.close();
    _audioChunkController.close();
    uploadManager = null;
    _recorderSubscription?.cancel();
    _chunkTimer?.cancel();
    _recorder?.closeRecorder();
    _recorder = null;
    _recordFile = null;
    _lastReadPos = 0;
  }

  // Apply gain to PCM16 audio data
  Uint8List _applyGain(Uint8List pcm, double gain) {
    final buffer = ByteData.sublistView(pcm);
    for (int i = 0; i < buffer.lengthInBytes; i += 2) {
      int sample = buffer.getInt16(i, Endian.little);
      sample = (sample * gain).clamp(-32768, 32767).toInt();
      buffer.setInt16(i, sample, Endian.little);
    }
    return pcm;
  }
}
