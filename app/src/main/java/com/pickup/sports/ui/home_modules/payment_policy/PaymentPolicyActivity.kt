package com.pickup.sports.ui.home_modules.payment_policy

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.data.model.GetGameByIdApiResponse
import com.pickup.sports.data.model.GetPaymentMethodApiResponse
import com.pickup.sports.databinding.ActivityPaymentPolicyBinding
import com.pickup.sports.databinding.ItemLayoutDeleteCardPopupBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.add_card_module.AddCardActivity
import com.pickup.sports.ui.home_modules.game_details.GameDetailsActivity
import com.pickup.sports.utils.BaseCustomDialog
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentPolicyActivity : BaseActivity<ActivityPaymentPolicyBinding>() , CustomPaymentAdapter.OnItemClickListener ,BaseCustomDialog.Listener {

    private val viewModel : PaymentPolicyActivityVm by viewModels()
    private lateinit var customPaymentAdapter: CustomPaymentAdapter
    private lateinit var deleteCardPopup : BaseCustomDialog<ItemLayoutDeleteCardPopupBinding>
    private var paymentCardId : String ? = null
    private var isSelected : Boolean = false
    private var gameId : String ? = null
    private var price : Int ? = null
    override fun getLayoutResource(): Int {
        return R.layout.activity_payment_policy
    }

    override fun getViewModel(): BaseViewModel {
        return  viewModel
    }

    override fun onCreateView() {
        initOnClick()
        getData()
        initPopup()
        viewModel.getPaymentMethod(Constants.GET_PAYMENT_METHOD)
        customPaymentAdapter = CustomPaymentAdapter(this)
        binding.rvCardDetails.adapter = customPaymentAdapter
        initObserver()

    }

    private fun initPopup() {
        deleteCardPopup = BaseCustomDialog(this , R.layout.item_layout_delete_card_popup, this)
    }


    private fun getData() {
        val gameDetail = intent.getParcelableExtra<GetGameByIdApiResponse.Game>("data")
        if (gameDetail != null){
            price = gameDetail.type
            binding.bean = gameDetail
            gameId = gameDetail._id
        }


    }

    private fun initObserver() {
        viewModel.obrCommon.observe(this, Observer {
            when(it?.status){
                Status.LOADING->{
                    progressDialogAvl.isLoading(true)
                }
                Status.SUCCESS ->{
                    hideLoading()
                    when(it.message){
                        "getPaymentMethod" ->{
                            val myDataModel : GetPaymentMethodApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                               try {
                                   myDataModel.paymentMethods?.paymentMethods?.let { methods ->
                                       if (methods.isNotEmpty()) {
                                           paymentCardId = methods[0]?.id ?: "" // Ensure it's not null
                                           customPaymentAdapter.setList(methods)
                                           customPaymentAdapter.notifyDataSetChanged()
                                       }
                                   }

                                   binding.account.text = "Account balance: $" + (myDataModel.credits ?: "0.00")
                               }catch ( e : Exception){
                                   e.printStackTrace()
                               }


                            }
                        }
                        "joinGame" ->{
                            val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast("You have joined the game successfully")
                                val intent = Intent(this, GameDetailsActivity::class.java)
                                intent.putExtra("id",gameId)
                                intent.putExtra("game", "joined")
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            }
                        }
                        "deleteCard" ->{
                            val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast("Card Deleted Successfully")
                                deleteCardPopup.dismiss()
                                viewModel.getPaymentMethod(Constants.GET_PAYMENT_METHOD)
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


    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.tvConfirmPay ->{
                    if (price == 2){
                        val data = HashMap<String,Any>()
                        data["gameId"]  = gameId.toString()
                        viewModel.joinGame(data,Constants.MAKE_PAYMENT)

                    }else{
                        if (paymentCardId != null){
                            val data = HashMap<String,Any>()
                            data["gameId"]  = gameId.toString()
                            data["deductCredits"] = isSelected
                            data["paymentMethodId"] = paymentCardId.toString()
                            viewModel.joinGame(data,Constants.MAKE_PAYMENT)
                        }else{
                            showToast("Please select card")
                        }
                    }

                }
                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }
                R.id.consCreditCard ->{
                    val intent = Intent(this , AddCardActivity::class.java)
                    startActivity(intent)
                }
                R.id.consAccountBalance ->{
                    selection()
                }
            }
        })
    }

    private fun selection() {
        Log.i("dsadsa", "initOnClick: $isSelected")

        isSelected = !isSelected // Toggle the selection state

        if (isSelected) {
            binding.consAccountBalance.setBackgroundResource(R.drawable.bg_selected_edit_text)
        } else {
            binding.consAccountBalance.setBackgroundResource(R.drawable.bg_edit_text)
        }
    }

    override fun onItemClick(
        view: View,
        item: GetPaymentMethodApiResponse.PaymentMethods.PaymentMethod?,
        position: Int
    ) {

        when(view.id){
            R.id.deleteCard ->{
                deleteCardPopup.show()
            }
            R.id.consMain -> {
                // Update selection in the adapter
                customPaymentAdapter.updateSelection(position)

                // Assign selected payment card ID
                if (item != null){
                    paymentCardId = item.id
                    Log.i("dfdsfdsf", "onItemClick: $paymentCardId")
                }

            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.getPaymentMethod(Constants.GET_PAYMENT_METHOD)
    }

    override fun onViewClick(view: View?) {
        when(view?.id){
            R.id.tvYes ->{
                viewModel.deleteCard(Constants.DELETE_CARD+paymentCardId)
            }

        }
    }
}