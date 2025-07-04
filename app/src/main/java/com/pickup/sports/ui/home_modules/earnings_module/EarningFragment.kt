package com.pickup.sports.ui.home_modules.earnings_module

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.GetBalanceApiResponse
import com.pickup.sports.data.model.GetPaymentMethodApiResponse
import com.pickup.sports.data.model.StripeConnectApiResponse
import com.pickup.sports.databinding.FragmentEarningBinding
import com.pickup.sports.databinding.ItemLayoutCardBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.web_view_module.WebViewActivity
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EarningFragment : BaseFragment<FragmentEarningBinding>() {


    private val viewModel : EarningFragmentVm by viewModels()
    private var cardData: List<GetPaymentMethodApiResponse.PaymentMethods.ExternalAccount?>? = null

    private lateinit var cardAdapter : SimpleRecyclerViewAdapter<GetPaymentMethodApiResponse.PaymentMethods.ExternalAccount , ItemLayoutCardBinding>

    override fun onCreateView(view: View) {
        initOnClick()

        viewModel.getBalance(Constants.GET_BALANCE)
        viewModel.getPaymentMethod(Constants.GET_PAYMENT_METHOD)
        initAdapter()
        initObserver()
    }

    private fun initObserver() {
        viewModel.obrCommon.observe(viewLifecycleOwner, Observer {
            when(it?.status){
                Status.LOADING->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "stripeConnect" ->{
                            val myDataModel : StripeConnectApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.accountLink != null){

                                    val stripeLink = myDataModel.accountLink!!.url

                                    val intent = Intent(requireContext() , WebViewActivity ::class.java )
                                    intent.putExtra("url", stripeLink)
                                    startActivity(intent)

                                }
                            }
                        }
                        "getBalance" ->{
                            val myDataModel : GetBalanceApiResponse  ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                 binding.bean  = myDataModel
                            }
                        }
                        "getPaymentMethod" ->{
                            val myDataModel : GetPaymentMethodApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if(myDataModel.paymentMethods != null){
                                     cardData = myDataModel.paymentMethods!!.externalAccounts
                                    cardAdapter.list = myDataModel.paymentMethods!!.externalAccounts
                                    Log.i("fdfdf", "initObserver: $cardData")

                                }
                            }
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
        cardAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_card,BR.bean){v,m,pos ->

        }
        binding.rvCardList.adapter =cardAdapter
        cardAdapter.notifyDataSetChanged()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner , Observer {
            when(it?.id){

                R.id.consAddBankAccount ->{
                    if (cardData.isNullOrEmpty()) {
                        viewModel.stripeConnect(Constants.STRIPE_CONNECT)
                    }else{
                        showToast("Account already added")
                    }

//                    val intent = Intent(requireContext(), AddCardActivity::class.java)
//                    startActivity(intent)
                }
            }
        })

    }

    override fun getLayoutResource(): Int {

        return R.layout.fragment_earning
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

}