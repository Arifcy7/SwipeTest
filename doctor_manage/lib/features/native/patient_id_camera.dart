import 'package:camera/camera.dart';
import 'package:flutter/material.dart';

class PatientIdCamera extends StatefulWidget {
  final void Function(XFile photo) onPhotoTaken;
  const PatientIdCamera({super.key, required this.onPhotoTaken});

  @override
  State<PatientIdCamera> createState() => _PatientIdCameraState();
}

class _PatientIdCameraState extends State<PatientIdCamera> {
  CameraController? _controller;
  List<CameraDescription>? _cameras;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _initCamera();
  }

  Future<void> _initCamera() async {
    _cameras = await availableCameras();
    _controller = CameraController(_cameras!.first, ResolutionPreset.medium);
    await _controller!.initialize();
    setState(() => _loading = false);
  }

  @override
  void dispose() {
    _controller?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_loading || _controller == null) {
      return const Center(child: CircularProgressIndicator());
    }
    return Column(
      children: [
        AspectRatio(
          aspectRatio: _controller!.value.aspectRatio,
          child: CameraPreview(_controller!),
        ),
        ElevatedButton(
          onPressed: () async {
            final photo = await _controller!.takePicture();
            widget.onPhotoTaken(photo);
          },
          child: const Text('Take Patient ID Photo'),
        ),
      ],
    );
  }
}
