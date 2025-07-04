package com.pickup.sports.ui.auth.reset_password

import android.content.Intent
import android.text.InputType
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.databinding.ActivityResetPasswordBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordActivity : BaseActivity<ActivityResetPasswordBinding>() {

    private val viewModel : ResetPasswordActivityVm  by viewModels()

    private var email : String ? = null

    override fun getLayoutResource(): Int {
       return R.layout.activity_reset_password
    }

    override fun getViewModel(): BaseViewModel {
       return viewModel
    }

    override fun onCreateView() {
        getData()
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
                    val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                    if (myDataModel != null){
                        val intent = Intent(this ,HomeDashBoardActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
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
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.tvConfirm ->{
                    if (isEmptyField()){
                        if (email != null){
                            val data = HashMap<String, Any>()
                            data["email"] = email.toString()
                            data["password"] = binding.etNewPassword.text.toString().trim()

                            viewModel.resetPassword(data,Constants.RESET_PASSWORD)
                        }

                    }
                }
                R.id.showNewPassword ->{
                    showOrHideNewPassword()
                }
                R.id.showConfirmPassword ->{
                    showOrHideConfirmPassword()
                }
            }
        })

    }

    private fun showOrHideNewPassword() {
        if (binding.etNewPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.showNewPassword.setImageResource(R.drawable.iv_eye)
            binding.etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.showNewPassword.setImageResource(R.drawable.iv_show_password)
            binding.etNewPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etNewPassword.setSelection(binding.etNewPassword.length())
    }


    private fun showOrHideConfirmPassword() {
        if (binding.etConfirmPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.showConfirmPassword.setImageResource(R.drawable.iv_eye)
            binding.etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.showConfirmPassword.setImageResource(R.drawable.iv_show_password)
            binding.etConfirmPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
    }
    private fun isEmptyField(): Boolean {

        val password = binding.etNewPassword.text.toString().trim()

        if (TextUtils.isEmpty(binding.etNewPassword.text.toString().trim())) {
            showToast("Please enter new password ")
            return false
        }
        if (TextUtils.isEmpty(binding.etConfirmPassword.text.toString().trim())){
            showToast("Please  enter confirm password")
            return false
        }
        if (password != binding.etConfirmPassword.text.toString().trim()){
            showToast("New password  and confirm new password not matched")
            return false
        }

        return true
    }
}