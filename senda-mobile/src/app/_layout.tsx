import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { ActivityIndicator, View } from 'react-native';

import { useSession } from '@/hooks/use-session';

export default function RootLayout() {
  const { session, loading } = useSession();

  if (loading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#FDFBF7' }}>
        <ActivityIndicator color="#1B4965" />
      </View>
    );
  }

  return (
    <>
      <StatusBar style="dark" />
      <Stack>
        <Stack.Protected guard={!session}>
          <Stack.Screen name="login" options={{ headerShown: false }} />
        </Stack.Protected>

        <Stack.Protected guard={!!session}>
          <Stack.Screen name="index" options={{ headerShown: false }} />
          <Stack.Screen 
            name="chat" 
            options={{ 
              title: 'NOVA', 
              presentation: 'modal', 
              headerStyle: { backgroundColor: '#FDFBF7' },
              headerTintColor: '#1B4965'
            }} 
          />
          <Stack.Screen 
            name="journal" 
            options={{ 
              title: 'Diario', 
              headerStyle: { backgroundColor: '#FDFBF7' },
              headerTintColor: '#1B4965'
            }} 
          />
          <Stack.Screen 
            name="courses" 
            options={{ 
              title: 'Aprender', 
              headerStyle: { backgroundColor: '#FDFBF7' },
              headerTintColor: '#1B4965'
            }} 
          />
        </Stack.Protected>
      </Stack>
    </>
  );
}
