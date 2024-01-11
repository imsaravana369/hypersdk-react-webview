import * as React from 'react';

import {
  attachJuspayWebview,
  handleJuspayEvents,
} from 'hypersdk-react-webview';

import WebView, { type WebViewMessageEvent } from 'react-native-webview';
import { useEffect } from 'react';

export default function App() {
  const webViewRef = React.useRef<WebView | null>(null);
  useEffect(() => {
    console.log('girija attaching Juspaywebview exampleapp');
    attachJuspayWebview(webViewRef);
    return () => {};
  });
  const handleMessage = (ev: WebViewMessageEvent) => {
    console.log('My onMessage');
    handleJuspayEvents(webViewRef)(ev);
  };
  return (
    <WebView
      ref={webViewRef}
      source={{ uri: 'http://localhost:4200' }}
      onMessage={handleMessage}
      onNavigationStateChange={() => attachJuspayWebview(webViewRef)}
    />
  );
}
