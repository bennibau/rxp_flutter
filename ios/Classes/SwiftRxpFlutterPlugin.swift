import Flutter
import UIKit
import RXPiOS


public class SwiftRxpFlutterPlugin: NSObject, FlutterPlugin {
    
    private var delegate: ElavonDelegate?
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "rxp_flutter", binaryMessenger: registrar.messenger())
        let instance = SwiftRxpFlutterPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
        if (call.method == "showPaymentWindow") {
            
                
                guard let payload = call.arguments as? Array<Any>,
                    let contentDict = payload[0] as? Dictionary<String, Any>,
                    let hppRequestProducerURL = URL(string: contentDict["HPPRequestProducerURL"] as? String ?? ""),
                    let hppRequestConsumerURL = URL(string: contentDict["HPPResponseConsumerURL"] as? String ?? ""),
                    let hppURL = URL(string: contentDict["HPPURL"] as? String ?? "")
                    else {
                    result(["success": false,
                    "result": "No or invalid configuration provided",
                    "code": 9999])
                    return
                }
                
                let appDelegate = UIApplication.shared.delegate as! FlutterAppDelegate
                let controller : FlutterViewController =  appDelegate.window!.rootViewController as! FlutterViewController
                
                let hppManager = HPPManager()
                hppManager.HPPRequestProducerURL = hppRequestProducerURL
                hppManager.HPPURL = hppURL
                hppManager.HPPResponseConsumerURL = hppRequestConsumerURL
                
                // add custom fields if present
                if let merchantId = contentDict["merchantId"] as? String {
                    hppManager.merchantId = merchantId
                }
                if let account = contentDict["account"] as? String {
                    hppManager.account = account
                }
                if let amount = contentDict["amount"] as? String {
                    hppManager.amount = amount
                }
                if let currency = contentDict["currency"] as? String {
                    hppManager.currency = currency
                }
                if let supplementaryData = contentDict["supplementaryData"] as? Dictionary<String, String> {
                    supplementaryData.forEach { (key, value) in
                        hppManager.supplementaryData[key] = value
                    }
                }
                DispatchQueue.main.async {
                // present view controller and init and set delegate
                hppManager.presentViewInViewController(controller)
                self.delegate = ElavonDelegate(result: result)
                hppManager.delegate = self.delegate
                
            }
        }else {
            result("Unimplemented")
        }
    }
}


class ElavonDelegate: HPPManagerDelegate {
    
    let result: FlutterResult
    
    init(result: @escaping FlutterResult) {
        self.result = result
    }
    
    func HPPManagerCancelled() {
        self.result(["success": false,
        "result": "Cancelled",
        "code": 9998])
    }
    func HPPManagerFailedWithError(_ error: NSError?) {
        self.result(["success": false,
                     "result": error?.localizedDescription ?? " ",
                     "code": error?.code ?? 9999])
    }
    func HPPManagerCompletedWithResult(_ result: Dictionary<String, String>) {
        self.result(["success": true,
                     "result": "successful transaction",
                     "code": 200])
    }
    
}
