import 'package:flutter/material.dart';
import '../../services/api_service.dart';
import 'patients_repository.dart';

class PatientSelectorWidget extends StatefulWidget {
  final String userId;
  final void Function(Map<String, dynamic> patient) onSelected;
  const PatientSelectorWidget({super.key, required this.userId, required this.onSelected});

  @override
  State<PatientSelectorWidget> createState() => _PatientSelectorWidgetState();
}

class _PatientSelectorWidgetState extends State<PatientSelectorWidget> {
  late final PatientsRepository repo;
  List<Map<String, dynamic>> patients = [];
  bool loading = true;
  @override
  void initState() {
    super.initState();
    repo = PatientsRepository(ApiService());
    _load();
  }
  Future<void> _load() async {
    setState(() => loading = true);
    try {
      patients = await repo.getPatients(widget.userId);
    } catch (_) {}
    setState(() => loading = false);
  }
  @override
  Widget build(BuildContext context) {
    if (loading) return const CircularProgressIndicator();
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        DropdownButton<Map<String, dynamic>>(
          hint: const Text('Select Patient'),
          value: null,
          items: patients.map((p) => DropdownMenuItem(
            value: p,
            child: Text(p['name'] ?? ''),
          )).toList(),
          onChanged: (p) {
            if (p != null) widget.onSelected(p);
          },
        ),
        const SizedBox(height: 8),
        ElevatedButton.icon(
          icon: const Icon(Icons.person_add),
          label: const Text('Add Patient'),
          onPressed: () async {
            final name = await showDialog<String>(
              context: context,
              builder: (ctx) {
                String tempName = '';
                return AlertDialog(
                  title: const Text('Add Patient'),
                  content: TextField(
                    autofocus: true,
                    decoration: const InputDecoration(labelText: 'Patient Name'),
                    onChanged: (v) => tempName = v,
                  ),
                  actions: [
                    TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('Cancel')),
                    ElevatedButton(onPressed: () => Navigator.pop(ctx, tempName), child: const Text('Add')),
                  ],
                );
              },
            );
            if (name != null && name.trim().isNotEmpty) {
              setState(() => loading = true);
              try {
                await repo.addPatient(name.trim(), widget.userId);
                await _load();
              } catch (e) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(content: Text('Failed to add patient: $e')),
                );
              }
            }
          },
        ),
      ],
    );
  }
}
