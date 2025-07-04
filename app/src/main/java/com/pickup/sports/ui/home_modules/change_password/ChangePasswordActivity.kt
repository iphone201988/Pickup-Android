package com.pickup.sports.ui.home_modules.change_password

import android.text.InputType
import android.text.TextUtils
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.databinding.ActivityChangePasswordBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : BaseActivity<ActivityChangePasswordBinding>() {

    private val viewModel :  ChangePasswordActivityVm by viewModels()

    override fun getLayoutResource(): Int {
       return R.layout.activity_change_password
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initOnClick()
        initObserver()
        check()
    }

    private fun initObserver() {
        viewModel.obrCommon.observe(this , Observer {
            when(it?.status) {
                Status.LOADING ->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS  ->{
                    hideLoading()
                    val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                    if (myDataModel != null){
                        showToast(it.message.toString())
                        onBackPressedDispatcher.onBackPressed()
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

    private fun check() {
        binding.etOldPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                selection(1)
            }
        }
        binding.etNewPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                selection(2)
            }
        }
        binding.etConfirmNewPassword.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                selection(3)
            }
        }
    }
    private fun selection(selectedValue :Int) {
        when(selectedValue){
            1->{
                binding.etOldPassword.setBackgroundResource(R.drawable.bg_selected_edit_text)
                binding.etNewPassword.setBackgroundResource(R.drawable.bg_edit_text)
                binding.etConfirmNewPassword.setBackgroundResource(R.drawable.bg_edit_text)
            }
            2->{
                binding.etNewPassword.setBackgroundResource(R.drawable.bg_selected_edit_text)
                binding.etConfirmNewPassword.setBackgroundResource(R.drawable.bg_edit_text)
                binding.etOldPassword.setBackgroundResource(R.drawable.bg_edit_text)
            }
            3->{
                binding.etNewPassword.setBackgroundResource(R.drawable.bg_edit_text)
                binding.etOldPassword.setBackgroundResource(R.drawable.bg_edit_text)
                binding.etConfirmNewPassword.setBackgroundResource(R.drawable.bg_selected_edit_text)
            }

        }

    }
    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }
                R.id.tvSaveChanges ->{
                  if (isEmptyField()){
                      val data = HashMap<String,Any>()
                      data["oldPassword"] = binding.etOldPassword.text.toString().trim()
                      data["newPassword"] = binding.etNewPassword.text.toString().trim()

                      viewModel.changePassword(data,Constants.CHANGE_PASSWORD)
                  }

                }
                R.id.showOldPassword ->{
                    showOrHidePassword()
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
    private fun showOrHidePassword() {
        if (binding.etOldPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.showOldPassword.setImageResource(R.drawable.iv_eye)
            binding.etOldPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.showOldPassword.setImageResource(R.drawable.iv_show_password)
            binding.etOldPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etOldPassword.setSelection(binding.etOldPassword.length())
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
        if (binding.etConfirmNewPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.showConfirmPassword.setImageResource(R.drawable.iv_eye)
            binding.etConfirmNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.showConfirmPassword.setImageResource(R.drawable.iv_show_password)
            binding.etConfirmNewPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        binding.etConfirmNewPassword.setSelection(binding.etConfirmNewPassword.length())
    }
    private fun isEmptyField(): Boolean {
        val password = binding.etNewPassword.text.toString().trim()
        if (TextUtils.isEmpty(binding.etOldPassword.text.toString().trim())) {
            showToast("Please enter old password")
            return false
        }
        if (TextUtils.isEmpty(password)){
            showToast("Please enter new password")
            return false
        }
        if (TextUtils.isEmpty(binding.etConfirmNewPassword.text.toString().trim())) {
            showToast("Please enter confirm new password")
            return false
        }
        if (password != binding.etConfirmNewPassword.text.toString().trim()){
            showToast("New password  and confirm new password not matched")
            return false
        }
//        if (password.length < 8) {
//            showToast("Password must be 8 characters or more")
//            return false
//        }

        return true
    }
}