import 'package:flutter/material.dart';
import '../../services/api_service.dart';
import 'templates_repository.dart';

class TemplateSelectorWidget extends StatefulWidget {
  final String userId;
  final void Function(Map<String, dynamic> template) onSelected;
  const TemplateSelectorWidget({super.key, required this.userId, required this.onSelected});

  @override
  State<TemplateSelectorWidget> createState() => _TemplateSelectorWidgetState();
}

class _TemplateSelectorWidgetState extends State<TemplateSelectorWidget> {
  late final TemplatesRepository repo;
  List<Map<String, dynamic>> templates = [];
  bool loading = true;
  @override
  void initState() {
    super.initState();
    repo = TemplatesRepository(ApiService());
    _load();
  }
  Future<void> _load() async {
    setState(() => loading = true);
    try {
      templates = await repo.getTemplates(widget.userId);
    } catch (_) {}
    setState(() => loading = false);
  }
  @override
  Widget build(BuildContext context) {
    if (loading) return const CircularProgressIndicator();
    return DropdownButton<Map<String, dynamic>>(
      hint: const Text('Select Template'),
      value: null,
      items: templates.map((t) => DropdownMenuItem(
        value: t,
        child: Text(t['title'] ?? ''),
      )).toList(),
      onChanged: (t) {
        if (t != null) widget.onSelected(t);
      },
    );
  }
}
