import { Alert, StyleSheet, Text, TouchableOpacity } from 'react-native';

// Capa 3 del protocolo de crisis: acceso directo a ayuda humana desde cualquier pantalla.
export function showHelpResources() {
  Alert.alert(
    "Centro de Seguridad",
    "Línea de Crisis Local: 135\nEmergencias Médicas: 911\n\nPor favor, busca apoyo profesional de inmediato si sientes que no puedes mantenerte a salvo.",
    [{ text: "Entendido", style: "cancel" }]
  );
}

export function HelpButton({ compact = false }: { compact?: boolean }) {
  return (
    <TouchableOpacity
      style={[styles.button, compact && styles.compact]}
      onPress={showHelpResources}
      accessibilityRole="button"
      accessibilityLabel="Necesito ayuda ahora">
      <Text style={styles.text}>{compact ? '🆘 Ayuda' : '🆘 Necesito ayuda'}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  button: {
    backgroundColor: '#FFE5E5',
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: '#FFCCCC',
  },
  compact: { paddingVertical: 5, paddingHorizontal: 10 },
  text: { color: '#D80000', fontWeight: '600', fontSize: 13 },
});
