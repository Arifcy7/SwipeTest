import 'package:flutter/material.dart';
import 'audio_streaming_service.dart';

class AudioLevelVisualizer extends StatelessWidget {
  final AudioStreamingService audioService;
  const AudioLevelVisualizer({super.key, required this.audioService});

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<double>(
      stream: audioService.audioLevelStream,
      builder: (context, snapshot) {
        final level = snapshot.data ?? 0.0;
        return LinearProgressIndicator(
          value: level.clamp(0.0, 1.0),
          minHeight: 10,
          backgroundColor: Colors.grey[300],
          color: Colors.blue,
        );
      },
    );
  }
}
