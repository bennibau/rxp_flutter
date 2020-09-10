import 'package:flutter/material.dart';
import 'package:rxp_flutter/rxp_flutter.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('RXP Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              SizedBox(height: 50),
              RaisedButton(
                onPressed: () async {
                  var result = await RxpFlutter.showPaymentWindow(
                      hppRequestProducerURL:
                          'https://myserver.com/hppRequestProducer',
                      hppResponseConsumerURL:
                          'https://myserver.com/hppResponseConsumer',
                      hppURL: 'https://pay.sandbox.realexpayments.com/pay',
                      merchantId: 'realexsandbox',
                      account: 'internet',
                      amount: 100,
                      currency: 'EUR',
                      supplementaryData: {'UNKNOWN_1': 'Unknown value 1'});
                  print(result.success);
                  print(result.result);
                  print(result.code);
                },
                child: Text('Show Payment Window'),
              )
            ],
          ),
        ),
      ),
    );
  }
}
