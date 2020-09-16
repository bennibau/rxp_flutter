package com.example.rxp_flutter

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.realexpayments.hpp.HPPError
import com.realexpayments.hpp.HPPManager
import com.realexpayments.hpp.HPPManagerListener

class PaymentActivity : FragmentActivity(), HPPManagerListener<Any> {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_activity)
        val bundle = intent.extras?.getBundle("hppData")
        val hppManager: HPPManager = HPPManager.createFromBundle(bundle)
        val hppFragment = hppManager.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.container, hppFragment, null).commit()
    }

    override fun hppManagerCompletedWithResult(t: Any?) {
        setResult(RESULT_OK)
        finish()
    }

    override fun hppManagerFailedWithError(error: HPPError?) {
        val result = Intent()
        result.putExtra("error", error?.localizedMessage)
        setResult(2, result)
        finish()
    }

    override fun hppManagerCancelled() {
        setResult(RESULT_CANCELED)
        finish()
    }
}
