package com.pickup.sports.ui.home_modules.account_module

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.AccountModuleList
import com.pickup.sports.data.model.GetProfileApiResponse
import com.pickup.sports.databinding.ActivityAccountBinding
import com.pickup.sports.databinding.ItemLayoutAccountBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.ui.home_modules.change_password.ChangePasswordActivity
import com.pickup.sports.ui.home_modules.edit_profile.EditProfileActivity
import com.pickup.sports.ui.home_modules.help_module.HelpActivity
import com.pickup.sports.ui.home_modules.setting_module.SettingActivity
import com.pickup.sports.utils.BaseCustomDialog
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.SocketHandler
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountActivity : BaseActivity<ActivityAccountBinding>() , BaseCustomDialog.Listener {

    private val viewModel : AccountActivityVm  by viewModels()

    private lateinit var logoutPopup : BaseCustomDialog<ItemLayoutAccountBinding>
    private lateinit var accountAdapter : SimpleRecyclerViewAdapter<AccountModuleList, ItemLayoutAccountBinding>
    private var accountList = arrayListOf<AccountModuleList>()

    override fun getLayoutResource(): Int {
       return R.layout.activity_account
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initOnClick()
        initPopup()
        getList()
        viewModel.getProfile(Constants.GET_PROFILE)
        initObserver()
        initAdapter()
    }

    private fun initPopup() {
        logoutPopup = BaseCustomDialog(this , R.layout.item_layout_logout_popup,this )
    }

    private fun initObserver() {
        viewModel.obrCommon.observe(this , Observer {
            when(it?.status){
                Status.LOADING ->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getProfile" ->{
                            val myDataModel : GetProfileApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.user != null){
                                    binding.bean  = myDataModel.user
                                }
                            }
                        }
                        "logOut" ->{
                            sharedPrefManager.clear()
                            SocketHandler.closeConnection()
                            ImageUtils.userId = ""
                            logoutPopup.dismiss()
                            showToast("Logout successfully")
                            val intent = Intent(this , HomeDashBoardActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        }
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

    private fun initAdapter() {
        accountAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_account,BR.bean){v,m,pos ->
            when(v.id){
                R.id.title ->{
                    when(m.title){
                        "My Profile" ->{
                            val intent = Intent(this, EditProfileActivity::class.java)
                            startActivity(intent)
                        }
                        "Change Password" ->{
                            val intent = Intent(this, ChangePasswordActivity::class.java)
                            startActivity(intent)
                        }
                        "Settings" ->{
                            val intent = Intent(this, SettingActivity::class.java)
                            startActivity(intent)
                        }
                        "Help" ->{
                            val intent = Intent(this, HelpActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            }
        }
        binding.rvAccount.adapter = accountAdapter
        accountAdapter.list = accountList
        accountAdapter.notifyDataSetChanged()
    }

    private fun getList() {
        accountList.add(AccountModuleList("My Profile",R.drawable.iv_edit_profile))
        accountList.add(AccountModuleList("Change Password",R.drawable.iv_change_password))
        accountList.add(AccountModuleList("Settings",R.drawable.iv_settings))
        accountList.add(AccountModuleList("Help",R.drawable.iv_help))
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }

                R.id.logout ->{
                   logoutPopup.show()
                }
            }
        })


    }

    override fun onViewClick(view: View?) {
        when(view?.id){
            R.id.tvYes ->{
                viewModel.logOut(Constants.LOGOUT)
            }
            R.id.tvNo ->{
                logoutPopup.dismiss()
            }
        }
    }
}