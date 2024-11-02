import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-nexgo' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const Nexgo = NativeModules.Nexgo
  ? NativeModules.Nexgo
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export function init(): Promise<boolean> {
  return Nexgo.init();
}

export function printText(
  text: string,
  fontSize: number,
  alignment: number,
  something: boolean,
): Promise<any> {
  return Nexgo.printText(text, fontSize, alignment, something);
}

export function printSpacedAround(
  textLeft: string,
  textRight: string,
  fontSize: number,
): Promise<any> {
  return Nexgo.printSpacedAround(textLeft, textRight, fontSize);
}

export function printQR(
  text: string,
  size: number,
  moduleSize: number,
): Promise<any> {
  return Nexgo.printQR(text, size, moduleSize);
}

export function execute(): Promise<boolean> {
  return Nexgo.execute();
}

