package com.pickup.sports.ui.home_modules.setting_module

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.databinding.ActivitySettingBinding
import com.pickup.sports.databinding.ItemLayoutDeleteAccountBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.utils.BaseCustomDialog
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.SocketHandler
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingActivity : BaseActivity<ActivitySettingBinding>() , BaseCustomDialog.Listener{

    private val viewModel : SettingActivityVm by viewModels()
    private lateinit var deletePopup : BaseCustomDialog<ItemLayoutDeleteAccountBinding>

    override fun getLayoutResource(): Int {
      return R.layout.activity_setting
    }

    override fun getViewModel(): BaseViewModel {
         return viewModel
    }

    override fun onCreateView() {
      initOnClick()
        initPopup()
        initObserver()
    }

    private fun initPopup() {
        deletePopup = BaseCustomDialog(this , R.layout.item_layout_delete_account, this)
    }

    private fun initObserver() {
        viewModel.obrCommon.observe(this, Observer {
            when(it?.status){
                Status.LOADING ->{
                    progressDialogAvl.isLoading(false)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                    if (myDataModel != null){
                        showToast(it.message.toString())
                        deletePopup.dismiss()
                        sharedPrefManager.clear()
                        SocketHandler.closeConnection()
                        ImageUtils.userId = ""
                        val intent = Intent(this , HomeDashBoardActivity::class.java)
                        startActivity(intent)
                    }
                }
                Status.ERROR ->{
                    hideLoading()
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
                R.id.consMain ->{
                  deletePopup.show()
                }
            }
        })
    }

    override fun onViewClick(view: View?) {
        when(view?.id){
            R.id.tvYes ->{
                viewModel.deleteAccount(Constants.DELETE_ACCOUNT)
            }
            R.id.tvNo ->{
                deletePopup.dismiss()
            }
        }
    }
}