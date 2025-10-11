import 'package:flutter/material.dart';
import 'features/audio/audio_recorder_widget.dart';
import 'features/patients/patient_selector_widget.dart';
import 'features/templates/template_selector_widget.dart';

void main() {
  runApp(const AiScribeCopilotApp());
}

class AiScribeCopilotApp extends StatelessWidget {
  const AiScribeCopilotApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'doctor manage',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
        useMaterial3: true,
        scaffoldBackgroundColor: Colors.grey[50],
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.white,
          foregroundColor: Colors.teal,
          elevation: 1,
          centerTitle: true,
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.teal,
            foregroundColor: Colors.white,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(10),
            ),
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
            textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
        ),
      ),
      home: const HomeScreen(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});
  @override
  Widget build(BuildContext context) {
    return _HomeScreenBody();
  }
}

class _HomeScreenBody extends StatefulWidget {
  @override
  State<_HomeScreenBody> createState() => _HomeScreenBodyState();
}

class _HomeScreenBodyState extends State<_HomeScreenBody> {
  Map<String, dynamic>? selectedPatient;
  Map<String, dynamic>? selectedTemplate;
  final String userId = 'arif';
  final String baseUrl = 'https://app.scribehealth.ai/api';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('doctor manage', style: TextStyle(fontWeight: FontWeight.bold)),
        centerTitle: true,
      ),
      body: Center(
        child: Container(
          constraints: const BoxConstraints(maxWidth: 500),
          padding: const EdgeInsets.all(16),
          child: Card(
            elevation: 2,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  PatientSelectorWidget(
                    userId: userId,
                    onSelected: (p) => setState(() => selectedPatient = p),
                  ),
                  const SizedBox(height: 16),
                  TemplateSelectorWidget(
                    userId: userId,
                    onSelected: (t) => setState(() => selectedTemplate = t),
                  ),
                  const SizedBox(height: 24),
                  if (selectedPatient != null && selectedTemplate != null)
                    AudioRecorderWidget(
                      userId: userId,
                      baseUrl: baseUrl,
                      // You can pass patient/template info here as needed
                    ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  
  }
}
