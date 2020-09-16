import 'dart:async';

import 'package:flutter/services.dart';

class RxpResult {
  final bool success;
  final String result;
  final int code;

  RxpResult(this.success, this.result, this.code);
}

class RxpFlutter {
  static const MethodChannel _channel = const MethodChannel('rxp_flutter');

  static Future<RxpResult> showPaymentWindow(
      {String hppRequestProducerURL,
      String hppResponseConsumerURL,
      String hppURL,
      String merchantId,
      String account,
      int amount,
      String currency,
      String productId,
      Map<String, String> supplementaryData}) async {
    final payload = {
      'HPPRequestProducerURL': hppRequestProducerURL,
      'HPPResponseConsumerURL': hppResponseConsumerURL,
      'HPPURL': hppURL,
      'merchantId': merchantId,
      'account': account,
      'amount': amount,
      'currency': currency,
      'supplementaryData': supplementaryData,
      'productId': productId
    };

    final Map<dynamic, dynamic> result =
        await _channel.invokeMethod('showPaymentWindow', [payload]);
    var rxpResult =
        RxpResult(result['success'], result['result'], result['code']);
    return rxpResult;
  }
}
