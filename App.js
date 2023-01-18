import { StatusBar } from 'expo-status-bar';
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import {Periodic} from "./Periodic";
import * as Location from "expo-location";
import notifee from '@notifee/react-native';

export default function App() {
  
  React.useEffect(() => {
    (async () => {
      await notifee.onBackgroundEvent(async () => {});
      await notifee.requestPermission();
      await notifee.createChannel({
        id: 'PeriodicService',
        vibration: false,
        name: 'App status notification'
      });
      await Location.requestForegroundPermissionsAsync();
      await Location.requestBackgroundPermissionsAsync();
      Periodic.stop();
      Periodic.start(async () => {
        Periodic.telegram('Getting location');
        const position = await Location.getCurrentPositionAsync({
          accuracy: Location.Accuracy.Highest,
          enableHighAccuracy: true
        });
        Periodic.telegram('Got location: ' + JSON.stringify(position));
      })?.then();
    })().then();
  }, []);
  
  return (
    <View style={styles.container}>
      <Text>Open up App.js to start working on your app!</Text>
      <StatusBar style="auto" />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
