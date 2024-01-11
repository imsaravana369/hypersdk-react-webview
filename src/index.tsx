import { NativeModules, Platform } from 'react-native';
import * as React from 'react';
import { WebView, type WebViewMessageEvent } from 'react-native-webview';
import { decode } from 'base-64';

const LINKING_ERROR =
  `The package 'hypersdk-react-webview' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

export const HypersdkReactWebview = NativeModules.HypersdkReactWebview
  ? NativeModules.HypersdkReactWebview
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const attachJuspayWebview = (webViewRef: React.RefObject<WebView>) => {
  const polyfillJS = ` window.HyperWebViewBridge = {}
          window.HyperWebViewBridge.postMessage =  window.ReactNativeWebView.postMessage.bind(window.ReactNativeWebView);
        `;
  webViewRef.current?.injectJavaScript(polyfillJS);
};
export const handleJuspayEvents =
  (webViewRef: React.RefObject<WebView>) => (event: WebViewMessageEvent) => {
    const { UPIModule } = NativeModules;
    const allowedMethods = ['findApps', 'openApp', 'getResourceByName'];

    const data = JSON.parse(event.nativeEvent.data);
    const fnName = data.fnName as string;
    const args = data.args;

    const returnResultInWebview = (
      requestCode: number,
      resultCode: number | null,
      result: any
    ) => {
      const cmd = `window.onActivityResult('${requestCode}' , '${resultCode}', '${JSON.stringify(
        result
      )}')`;
      webViewRef.current?.injectJavaScript(cmd);
    };

    if (!allowedMethods.includes(fnName)) {
      return;
    }
    switch (fnName) {
      case 'findApps':
        UPIModule.findApps(
          args[0],
          (requestCode: number, resultCode: number | null, res: any) => {
            returnResultInWebview(requestCode, resultCode, res);
          }
        );
        break;
      case 'getResourceByName':
        UPIModule.getWebviewVersion(
          (requestCode: number, resultCode: number | null, res?: string) => {
            returnResultInWebview(requestCode, resultCode, res);
          }
        );

        break;
      case 'openApp':
        UPIModule.openApp(
          args[0],
          args[1],
          args[2],
          args[3],
          (requestCode: number, resultCode: number | null, res: string) => {
            returnResultInWebview(requestCode, resultCode, decode(res));
          }
        );
        break;

      default:
        console.log('not a allowed method');
    }
  };
