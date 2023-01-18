import {NativeEventEmitter, NativeModules} from 'react-native';

let mod = {
	start: () => {},
	stop: () => {},
	telegram: () => {}
}

if(NativeModules.Periodic) {
	const PeriodicEmitter = new NativeEventEmitter();
	const removeListeners = () => {
		PeriodicEmitter.removeAllListeners('heartbeat');
	}
	mod = {
		start: async (onHeartbeat) => {
			console.log("Periodic.start");
			removeListeners();
			PeriodicEmitter.addListener('heartbeat', onHeartbeat);
			console.log(`NativeModules.Periodic.start`);
			NativeModules.Periodic.start();
		},
		stop: () => {
			removeListeners();
			console.log('NativeModules.Periodic.stop');
			NativeModules.Periodic.stop();
		},
		telegram: text => NativeModules.Periodic.telegram(text)
	};
} else {
	console.log("Periodic not implemented");
}

export const Periodic = mod;