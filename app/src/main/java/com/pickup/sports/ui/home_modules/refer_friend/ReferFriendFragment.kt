package com.pickup.sports.ui.home_modules.refer_friend

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.model.GetReferralCodeApiResponse
import com.pickup.sports.databinding.FragmentReferFriendBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReferFriendFragment : BaseFragment<FragmentReferFriendBinding>() {

    private val viewModel : ReferFriendFragmentVm by viewModels()
    private var referralCode : String ? = null

    override fun onCreateView(view: View) {
        initOnClick()
        viewModel.getReferralData(com.pickup.sports.data.api.Constants.GET_REFERRAL_CODE)
        initObserver()
    }

    private fun initObserver() {
        viewModel.obrCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING  ->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    val myDataModel : GetReferralCodeApiResponse ? = ImageUtils.parseJson(it.data.toString())
                    if (myDataModel != null){
                        binding.bean = myDataModel
                        referralCode = myDataModel.referralCode
                    }
                }
                Status.ERROR ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else -> {

                }
            }
        })
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.tvCopyLink ->{
                   shareReferralCode()
                }
            }
        })
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_refer_friend
    }

    override fun getViewModel(): BaseViewModel {
       return viewModel
    }



    private fun shareReferralCode() {
        if (referralCode.isNullOrBlank()) {
            showToast("No referral code available")
            return
        }


        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, referralCode)
        }

        val chooserIntent = Intent.createChooser(intent, "Share via")
        startActivity(chooserIntent)
    }


}