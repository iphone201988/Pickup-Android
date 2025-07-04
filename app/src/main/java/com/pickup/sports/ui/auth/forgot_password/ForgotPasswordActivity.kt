package com.pickup.sports.ui.auth.forgot_password

import android.content.Intent
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.databinding.ActivityForgotPasswordBinding
import com.pickup.sports.ui.auth.verification.VerificationActivity
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotPasswordActivity : BaseActivity<ActivityForgotPasswordBinding>() {

    private val viewModel : ForgotPasswordActivityVm by viewModels()

    private var email : String ? = null

    override fun getLayoutResource(): Int {
    return R.layout.activity_forgot_password
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
      initOnClick()
        initObserver()
    }

    private fun initObserver() {
        viewModel.obrCommon.observe(this , Observer {
            when(it?.status){
                Status.LOADING ->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                    if (myDataModel != null){
                        showToast("OTP has been sent on your email")
                        val intent = Intent(this , VerificationActivity :: class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    }

                }
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else ->{

                }

            }
        })
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
            R.id.ivBack ->{
                onBackPressedDispatcher.onBackPressed()
            }
             R.id.tvContinue ->{
                 if (isEmptyField()){
                     email = binding.etEmail.text.toString().trim()
                     val data = HashMap<String,Any>()
                     data["email"] = email!!
                     viewModel.forgotPassword(data,Constants.FORGOT_PASSWORD)
                 }
             }
            }
        })
    }

    private fun isEmptyField(): Boolean {

        if (TextUtils.isEmpty(binding.etEmail.text.toString().trim())) {
            showToast("Please enter email ")
            return false
        }

        return true
    }
}