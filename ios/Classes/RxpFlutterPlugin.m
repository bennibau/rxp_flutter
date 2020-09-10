#import "RxpFlutterPlugin.h"
#if __has_include(<rxp_flutter/rxp_flutter-Swift.h>)
#import <rxp_flutter/rxp_flutter-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "rxp_flutter-Swift.h"
#endif

@implementation RxpFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftRxpFlutterPlugin registerWithRegistrar:registrar];
}
@end
