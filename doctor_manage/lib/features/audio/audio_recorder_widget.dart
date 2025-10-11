import 'package:flutter/material.dart';
import 'dart:async';
import 'audio_streaming_service.dart';
import 'audio_level_visualizer.dart';
import '../native/interruption_handler.dart';
import '../native/patient_id_camera.dart';
import '../native/system_share_sheet.dart';
import '../native/system_notifications.dart';
import '../native/haptic_feedback_helper.dart';
import '../native/do_not_disturb_helper.dart';
import 'package:camera/camera.dart';
import 'dart:io';


class AudioRecorderWidget extends StatefulWidget {
  final String userId;
  final String baseUrl;
  final Map<String, dynamic>? patient;
  final Map<String, dynamic>? template;
  const AudioRecorderWidget({super.key, required this.userId, required this.baseUrl, this.patient, this.template});

  @override
  State<AudioRecorderWidget> createState() => _AudioRecorderWidgetState();
}

class _AudioRecorderWidgetState extends State<AudioRecorderWidget> {
  final AudioStreamingService _audioService = AudioStreamingService();
  double _gain = 1.0;
  StreamSubscription<String>? _interruptionSub;

  @override
  void initState() {
    super.initState();
    InterruptionHandler.initialize();
    _interruptionSub = InterruptionHandler.events.listen((event) async {
      if (event == 'phone_call') {
        if (_audioService.isRecording) await _audioService.pauseRecording();
      } else if (event == 'app_switch') {
        // Do nothing, recording should continue
      } else if (event == 'app_killed' || event == 'phone_restart') {
        // Attempt to recover state (auto-resume if needed)
        if (!_audioService.isRecording) {
          await _startRecordingWithContext();
        }
      }
    });
  }

  Future<void> _startRecordingWithContext() async {
    if (widget.patient == null || widget.template == null) return;
    await _audioService.startRecording(
      gain: _gain,
      userId: widget.userId,
      baseUrl: widget.baseUrl,
      patientId: widget.patient!['id'],
      patientName: widget.patient!['name'],
      templateId: widget.template!['id'],
      templateTitle: widget.template!['title'],
    );
  }

  @override
  void dispose() {
    _interruptionSub?.cancel();
    InterruptionHandler.dispose();
    _audioService.dispose();
    super.dispose();
  }

  XFile? _patientPhoto;
  bool _dndEnabled = false;

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          AudioLevelVisualizer(audioService: _audioService),
          Row(
            children: [
              const Text('Gain'),
              Expanded(
                child: Slider(
                  value: _gain,
                  min: 0.5,
                  max: 2.0,
                  divisions: 15,
                  label: _gain.toStringAsFixed(2),
                  onChanged: (v) {
                    setState(() => _gain = v);
                  },
                ),
              ),
            ],
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: _audioService.isRecording || widget.patient == null || widget.template == null
                    ? null
                    : _startRecordingWithContext,
                child: const Text('Start'),
              ),
              const SizedBox(width: 16),
              ElevatedButton(
                onPressed: _audioService.isRecording
                    ? () => _audioService.stopRecording()
                    : null,
                child: const Text('Stop'),
              ),
            ],
          ),
          const SizedBox(height: 24),
          // Camera
          Text('Patient ID Photo:'),
          if (_patientPhoto != null)
            Image.file(
              File(_patientPhoto!.path),
              width: 120,
              height: 120,
              fit: BoxFit.cover,
            ),
          PatientIdCamera(
            onPhotoTaken: (photo) {
              setState(() => _patientPhoto = photo);
              HapticFeedbackHelper.success();
            },
          ),
          const SizedBox(height: 16),
          // System Share
          ElevatedButton(
            onPressed: _patientPhoto != null
                ? () => SystemShareSheet.shareFile(_patientPhoto!.path, text: 'Patient ID Photo')
                : null,
            child: const Text('Share Patient Photo'),
          ),
          const SizedBox(height: 16),
          // System Notification
          ElevatedButton(
            onPressed: () async {
              await SystemNotifications.initialize();
              await SystemNotifications.showNotification(
                id: 1,
                title: 'AI Scribe Copilot',
                body: 'This is a test notification.',
              );
              HapticFeedbackHelper.vibrate();
            },
            child: const Text('Show Test Notification'),
          ),
          const SizedBox(height: 16),
          // Do Not Disturb
          ElevatedButton(
            onPressed: () async {
              final enabled = await DoNotDisturbHelper.isDoNotDisturbEnabled();
              setState(() => _dndEnabled = enabled);
            },
            child: const Text('Check Do Not Disturb'),
          ),
          Text(_dndEnabled ? 'DND is ON' : 'DND is OFF'),
        ],
      ),
    );
  }
}
