package com.pickup.sports.ui.auth.verification

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.databinding.ActivityVerificationBinding
import com.pickup.sports.ui.auth.reset_password.ResetPasswordActivity
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationActivity : BaseActivity<ActivityVerificationBinding>() {

    private val viewModel : VerificationActivityVm by viewModels()
    private lateinit var otpETs: Array<EditText?>
    var isOtpComplete = false
    private var email : String ? = null
    override fun getLayoutResource(): Int {
        return  R.layout.activity_verification
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        getData()
       initView()
        initOnClick()
        initObserver()
    }

    private fun initObserver() {
        viewModel.obrCommon.observe(this, Observer {
            when(it?.status){

                Status.LOADING ->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                      "verification" ->{
                          val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                          if (myDataModel != null){
                               val intent = Intent(this , ResetPasswordActivity:: class.java)
                                intent.putExtra("email", email)
                               startActivity(intent)
                          }
                      }
                        "forgotPassword" ->{
                            val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){

                            }
                        }
                    }
                }
                Status.ERROR ->{

                }
                else ->{

                }
            }
        })
    }

    private fun getData() {
        email = intent.getStringExtra("email")
        if (email != null){
            binding.description.text = "We've send you the verification code on your e-mail($email)"
        }
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }
                R.id.tvContinue ->{
                    val otpData =
                        "${binding.otpET1.text}" + "${binding.otpET2.text}" + "${binding.otpET3.text}" + "${binding.otpET4.text}" + "${binding.otpET5.text}" + "${binding.otpET6.text}"
                    if (otpData.isNotEmpty()) {
                        val data = HashMap<String, Any>()
                        data["otp"] = otpData
                        data["email"] = email.toString()
                        viewModel.verification(data, Constants.VERIFY_OTP)

                    }else {
                        showToast("Please enter valid otp")
                    }
                }
                R.id.tvResendCode ->{
                    val data = HashMap<String,Any>()
                    data["email"] = email.toString()
                    viewModel.forgotPassword(data,Constants.FORGOT_PASSWORD)
                }
            }
        })
    }

    private fun initView() {
        otpETs = arrayOf(
            binding.otpET1, binding.otpET2, binding.otpET3, binding.otpET4, binding.otpET5,binding.otpET6
        )
        otpETs.forEachIndexed { index, editText ->
            editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrEmpty() && index != otpETs.size - 1) {
                        otpETs[index + 1]?.requestFocus()
                    }

                    // Check if all OTP fields are filled
                    isOtpComplete = otpETs.all { it!!.text.isNotEmpty() }

                }
            })

            editText?.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index != 0) {
                        otpETs[index - 1]?.apply {
                            text?.clear()  // Clear the previous EditText before focusing
                            requestFocus()
                        }
                    }
                }
                // Check if all OTP fields are filled
                isOtpComplete = otpETs.all { it!!.text.isNotEmpty() }

                false
            }
        }
    }
}