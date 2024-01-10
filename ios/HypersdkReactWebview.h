
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNHypersdkReactWebviewSpec.h"

@interface HypersdkReactWebview : NSObject <NativeHypersdkReactWebviewSpec>
#else
#import <React/RCTBridgeModule.h>

@interface HypersdkReactWebview : NSObject <RCTBridgeModule>
#endif

@end
