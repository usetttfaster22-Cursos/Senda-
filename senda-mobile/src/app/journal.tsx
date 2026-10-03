import React, { useState } from 'react';
import { StyleSheet, Text, View, TextInput, TouchableOpacity, ScrollView, SafeAreaView, Alert, ActivityIndicator } from 'react-native';

import { apiFetch } from '@/lib/api';

const QUESTIONS: { key: keyof JournalData; label: string }[] = [
  { key: 'emocion', label: '¿Qué siento ahora?' },
  { key: 'contexto', label: '¿Qué ocurrió antes de sentirme así?' },
  { key: 'pensamiento', label: '¿Qué pensamiento está ocupando más espacio?' },
  { key: 'control', label: '¿Qué parte depende de mí?' },
  { key: 'accion', label: 'Una acción pequeña para hoy' },
  { key: 'gratitud', label: 'Tres cosas por las que siento gratitud' },
];

const EMPTY_JOURNAL = { emocion: '', contexto: '', pensamiento: '', control: '', accion: '', gratitud: '' };
type JournalData = typeof EMPTY_JOURNAL;

export default function JournalScreen() {
  const [journalData, setJournalData] = useState<JournalData>(EMPTY_JOURNAL);
  const [saving, setSaving] = useState(false);

  const handleChange = (key: string, value: string) => {
    setJournalData({ ...journalData, [key]: value });
  };

  const saveJournal = async () => {
    // Se guardan solo las preguntas respondidas, como texto con su pregunta.
    const content = QUESTIONS
      .filter(({ key }) => journalData[key].trim() !== '')
      .map(({ key, label }) => `${label}\n${journalData[key].trim()}`)
      .join('\n\n');

    if (!content) {
      Alert.alert("Diario vacío", "Responde al menos una pregunta antes de guardar.");
      return;
    }

    setSaving(true);
    try {
      await apiFetch('/api/journal', { method: 'POST', body: JSON.stringify({ content }) });
      setJournalData(EMPTY_JOURNAL);
      Alert.alert("Diario guardado", "Tus reflexiones han sido guardadas de forma segura y privada.");
    } catch (error) {
      console.error(error);
      Alert.alert("No se pudo guardar", "Revisa tu conexión e inténtalo de nuevo. Tu texto sigue aquí.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.container}>
        <Text style={styles.headerTitle}>Diario Guiado</Text>
        <Text style={styles.subtitle}>Tómate un momento para organizar tus pensamientos de hoy.</Text>

        <View style={styles.card}>
          <Text style={styles.label}>¿Qué siento ahora?</Text>
          <TextInput
            style={styles.input}
            placeholder="Ej: Siento presión en el pecho, algo de ansiedad..."
            value={journalData.emocion}
            onChangeText={(t) => handleChange('emocion', t)}
            multiline
          />
        </View>

        <View style={styles.card}>
          <Text style={styles.label}>¿Qué ocurrió antes de sentirme así?</Text>
          <TextInput
            style={styles.input}
            placeholder="Describe brevemente la situación..."
            value={journalData.contexto}
            onChangeText={(t) => handleChange('contexto', t)}
            multiline
          />
        </View>

        <View style={styles.card}>
          <Text style={styles.label}>¿Qué pensamiento está ocupando más espacio?</Text>
          <TextInput
            style={styles.input}
            placeholder="Ej: 'No voy a llegar a tiempo con esto'..."
            value={journalData.pensamiento}
            onChangeText={(t) => handleChange('pensamiento', t)}
            multiline
          />
        </View>

        <View style={styles.card}>
          <Text style={styles.label}>¿Qué parte depende de mí?</Text>
          <TextInput
            style={styles.input}
            placeholder="Qué está bajo tu control..."
            value={journalData.control}
            onChangeText={(t) => handleChange('control', t)}
            multiline
          />
        </View>

        <View style={styles.card}>
          <Text style={styles.label}>Una acción pequeña para hoy</Text>
          <TextInput
            style={styles.input}
            placeholder="Ej: Voy a salir a caminar 5 minutos..."
            value={journalData.accion}
            onChangeText={(t) => handleChange('accion', t)}
            multiline
          />
        </View>

        <View style={styles.card}>
          <Text style={styles.label}>Tres cosas por las que siento gratitud</Text>
          <TextInput
            style={styles.input}
            placeholder="1. \n2. \n3. "
            value={journalData.gratitud}
            onChangeText={(t) => handleChange('gratitud', t)}
            multiline
          />
        </View>

        <TouchableOpacity style={styles.saveButton} onPress={saveJournal} disabled={saving}>
          {saving ? <ActivityIndicator color="#FFF" /> : <Text style={styles.saveButtonText}>Guardar Reflexión</Text>}
        </TouchableOpacity>

      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: { flex: 1, backgroundColor: '#FDFBF7' },
  container: { padding: 20 },
  headerTitle: { fontSize: 26, fontWeight: 'bold', color: '#1B4965', marginBottom: 5 },
  subtitle: { fontSize: 14, color: '#666', marginBottom: 25 },
  
  card: {
    backgroundColor: '#FFFFFF',
    padding: 15,
    borderRadius: 12,
    marginBottom: 15,
    borderWidth: 1,
    borderColor: '#E8F5E9',
    shadowColor: '#000',
    shadowOpacity: 0.03,
    shadowRadius: 5,
    elevation: 1,
  },
  label: { fontSize: 15, fontWeight: '600', color: '#2E7D32', marginBottom: 10 },
  input: {
    backgroundColor: '#F5F5F5',
    borderRadius: 8,
    padding: 12,
    fontSize: 14,
    minHeight: 60,
    textAlignVertical: 'top',
  },
  saveButton: {
    backgroundColor: '#1B4965', // Azul petróleo
    paddingVertical: 15,
    borderRadius: 25,
    alignItems: 'center',
    marginTop: 15,
    marginBottom: 30
  },
  saveButtonText: { color: '#FFF', fontWeight: 'bold', fontSize: 16 }
});
