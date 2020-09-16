package com.example.rxp_flutter

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.NonNull
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.realexpayments.hpp.HPPManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** RxpFlutterPlugin */
class RxpFlutterPlugin : FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var result: Result

    companion object {
        const val PAYMENT_REQUEST_CODE = 1337
        const val RESULT_ERROR = 2
        const val RESULT_FIELD_SUCCESS = "success"
        const val RESULT_FIELD_CODE = "code"
        const val RESULT_FIELD_RESULT = "result"
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "rxp_flutter")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        this.result = result
        when (call.method) {
            "showPaymentWindow" -> {
                val arguments: ArrayList<Any> = call.arguments<ArrayList<Any>>()
                val data = arguments.asPayloadData()

                // validate
                if (data == null
                        || data.hppURL.isEmpty()
                        || data.hppRequestConsumerURL.isEmpty()
                        || data.hppRequestProducerURL.isEmpty()) {
                    result.success(mutableMapOf(RESULT_FIELD_SUCCESS to false, RESULT_FIELD_RESULT to "No or invalid configuration provided", RESULT_FIELD_CODE to 9999))
                    return
                }

                val intent = Intent(context, PaymentActivity::class.java)
                val bundle = Bundle()
                bundle.putString(HPPManager.HPPREQUEST_PRODUCER_URL, data.hppRequestProducerURL)
                bundle.putString(HPPManager.HPPRESPONSE_CONSUMER_URL, data.hppRequestConsumerURL)
                bundle.putString(HPPManager.HPPURL, data.hppURL)
                data.merchantId?.let { bundle.putString(HPPManager.MERCHANT_ID, it) }
                data.amount?.let { bundle.putString(HPPManager.AMOUNT, it.toString()) }
                data.account?.let { bundle.putString(HPPManager.ACCOUNT, it) }
                data.currency?.let { bundle.putString(HPPManager.CURRENCY, it) }
                data.productId?.let { bundle.putString(HPPManager.PROD_ID, it) }
                data.supplementaryData?.let { bundle.putSerializable(HPPManager.SUPPLEMENTARYDATA, it) }
                intent.putExtra("hppData", bundle)
                activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE)
            }
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        //no-op
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        //no-op
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return when (requestCode) {
            PAYMENT_REQUEST_CODE -> handleResult(resultCode, data)
            else -> return false
        }
    }

    private fun handleResult(resultCode: Int, data: Intent?): Boolean {
        when (resultCode) {
            RESULT_OK -> result.success(mutableMapOf(RESULT_FIELD_SUCCESS to true, RESULT_FIELD_RESULT to "successful transaction", RESULT_FIELD_CODE to 200))
            RESULT_CANCELED -> result.success(mutableMapOf(RESULT_FIELD_SUCCESS to false, RESULT_FIELD_RESULT to "Cancelled", RESULT_FIELD_CODE to 9998))
            RESULT_ERROR -> result.success(mutableMapOf(RESULT_FIELD_SUCCESS to false, RESULT_FIELD_RESULT to data?.extras?.get("error"), RESULT_FIELD_CODE to 9999))
        }
        return true
    }
}

data class PayloadData(@SerializedName("HPPRequestProducerURL") val hppRequestProducerURL: String,
                       @SerializedName("HPPResponseConsumerURL") val hppRequestConsumerURL: String,
                       @SerializedName("HPPURL") val hppURL: String,
                       @SerializedName("merchantId") val merchantId: String?,
                       @SerializedName("account") val account: String?,
                       @SerializedName("amount") val amount: Int?,
                       @SerializedName("currency") val currency: String?,
                       @SerializedName("productId") val productId: String?,
                       @SerializedName("supplementaryData") val supplementaryData: HashMap<String, String>?
)

fun ArrayList<Any>.asPayloadData(): PayloadData? {
    val data: HashMap<String, Any>? = this[0] as HashMap<String, Any>
    return data?.toDataClass<PayloadData>()
}

val gson = Gson()

//convert a data class to a map
fun <T> T.serializeToMap(): Map<String, Any> {
    return convert()
}

//convert a map to a data class
inline fun <reified T> Map<String, Any>.toDataClass(): T {
    return convert()
}

//convert an object of type I to type O
inline fun <I, reified O> I.convert(): O {
    val json = gson.toJson(this)
    return gson.fromJson(json, object : TypeToken<O>() {}.type)
}
