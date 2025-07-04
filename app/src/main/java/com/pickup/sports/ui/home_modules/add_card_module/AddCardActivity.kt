package com.pickup.sports.ui.home_modules.add_card_module

import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.AddCustomerCardResponse
import com.pickup.sports.data.model.CardDatas
import com.pickup.sports.databinding.ActivityAddCardBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.regex.Pattern

@AndroidEntryPoint
class AddCardActivity : BaseActivity<ActivityAddCardBinding>() {

    private val viewModel : AddCardActivityVm  by viewModels()
    private var month: String? = null
    private var year: String? = null

    private var stripeID: String? = null
    private var isDefault : Boolean = false
    private lateinit var stripe: Stripe

    override fun getLayoutResource(): Int {
        return R.layout.activity_add_card
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initOnClick()
        PaymentConfiguration.init(applicationContext, "pk_test_51QCFKSCZS1FBnKe4CXXm7DSGr2HZhoFdd4aP0J6mZh3yVmMTsTWvOrz6vW8orDRULnInIB9BH3ZNcc98yq7kAQsc00jQuBsT98")
        stripe = Stripe(
            applicationContext,
            PaymentConfiguration.getInstance(applicationContext).publishableKey
        )
        textWatcher()

        binding.setDefault.setOnCheckedChangeListener { _, isChecked ->
            isDefault = isChecked
            // isChecked will be true if the switch is on, false if it's off
        }

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
                    when(it.message) {
                        "addCard" ->{
                            val myDataModel : AddCustomerCardResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel !=  null){
                                showToast("Card added successfully")
                                onBackPressedDispatcher.onBackPressed()
                            }
                        }
                    }
                }
                Status.ERROR  ->{
                    hideLoading()
                    showToast(it.message.toString())
                }
                else->{

                }
            }
        })
    }

    private fun textWatcher() {
        binding.etCardNumber.filters =
            arrayOf<InputFilter>(InputFilter.LengthFilter(19)) // Limit input to 19 characters

        binding.etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                val formattedInput = formatCardNumber(input)
                if (input != formattedInput) {
                    binding.etCardNumber.removeTextChangedListener(this) // Prevent infinite loop
                    binding.etCardNumber.setText(formattedInput)
                    binding.etCardNumber.setSelection(formattedInput.length) // Set cursor position to the end
                    binding.etCardNumber.addTextChangedListener(this)
                }
            }
        })


        binding.etExpirationDate.filters =
            arrayOf<InputFilter>(InputFilter.LengthFilter(7)) // Limit input to 5 characters

        binding.etExpirationDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                val formattedInput = formatExpiryDate(input)
                if (input != formattedInput) {
                    binding.etExpirationDate.removeTextChangedListener(this) // Prevent infinite loop
                    binding.etExpirationDate.setText(formattedInput)
                    binding.etExpirationDate.setSelection(formattedInput.length) // Set cursor position to the end
                    binding.etExpirationDate.addTextChangedListener(this)
                }

                val monthYear = extractExpiryMonthAndYear(input)
                if (monthYear != null) {
                    val (extractedMonth, extractedYear) = monthYear
                    month = extractedMonth.toString().padStart(2, '0')
                    year = extractedYear.toString().takeLast(4)
                    Log.i("Extracted", "Month: $month, Year: $year")
                } else {
                    month = null
                    year = null
                }
            }
        })
    }
    private fun formatCardNumber(input: String): String {
        val formatted = StringBuilder()
        var count = 0
        for (char in input) {
            if (char.isDigit()) {
                if (count > 0 && count % 4 == 0) {
                    formatted.append("-")
                }
                formatted.append(char)
                count++
            }
        }
        return formatted.toString()
    }

    private fun formatExpiryDate(input: String): String {
        val formatted = StringBuilder()
        var count = 0
        for (char in input) {
            if (char.isDigit()) {
                if (count == 2) {
                    formatted.append('/')
                }
                formatted.append(char)
                count++
            }
        }
        return formatted.toString()
    }


    private fun extractExpiryMonthAndYear(expiryDate: String): Pair<Int, Int>? {
        val regex = "^(0[1-9]|1[0-2])/([0-9]{2}|[0-9]{4})$"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(expiryDate)

        return if (matcher.find()) {
            val monthString = matcher.group(1)
            val yearString = matcher.group(2)

            val month = monthString?.toIntOrNull()
            val year = yearString?.toIntOrNull()

            if (month != null && year != null) {
                if (yearString.length == 2) {
                    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                    val century = currentYear / 100
                    val fullYear = century * 100 + year
                    Pair(month, fullYear)
                } else {
                    Pair(month, year)
                }
            } else {
                null
            }
        } else {
            null
        }
        Log.i("dsad", "extractExpiryMonthAndYear: $month ,$year")
    }

    private fun isValidMonthAndYear(month: String, year: String): Boolean {
        Log.i("Validation", "Input Month: $month, Input Year: $year")

        val currentCalendar = Calendar.getInstance()
        val currentYearFull = currentCalendar.get(Calendar.YEAR) // Full 4-digit year (e.g., 2024)
        val currentYearShort = currentYearFull % 100 // Last two digits (e.g., 24)
        val currentMonth = currentCalendar.get(Calendar.MONTH) + 1 // Months are 0-based

        if (month.isEmpty() || year.isEmpty()) {
            showToast("Please enter a valid month and year.")
            return false
        }

        val inputMonth = month.toIntOrNull()
        val inputYear = year.toIntOrNull()

        if (inputMonth == null || inputMonth !in 1..12) {
            showToast("Invalid month format. Please enter a month between 01 and 12.")
            return false
        }

        if (inputYear == null) {
            showToast("Invalid year format. Please enter a 2-digit or 4-digit year.")
            return false
        }

        val fullYear = when (year.length) {
            2 -> {
                val century = if (inputYear >= currentYearShort) 2000 else 2100
                century + inputYear
            }
            4 -> inputYear
            else -> {
                showToast("Invalid year format. Please enter a 2-digit or 4-digit year.")
                return false
            }
        }

        return isValidFullYear(inputMonth, fullYear, currentYearFull, currentMonth)
    }




    private fun isValidFullYear(inputMonth: Int, inputYear: Int, currentYear: Int, currentMonth: Int): Boolean {
        if (inputYear < currentYear || (inputYear == currentYear && inputMonth < currentMonth)) {
            showToast("Card expiration date is invalid or expired.")
            return false
        }
        return true
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.tvContinue ->{
                    if (isEmptyField()){
                        val cardNumber = binding.etCardNumber.text.toString()
                        val cvc = binding.etSecurityCode.text.toString()
                        val monthStr = month.toString()
                        val yearStr = year.toString()
                        val zipCode = binding.etZipCode.text.toString()
                        if (isValidMonthAndYear(month.toString(), year.toString())) {
                            val cardData =
                                CardDatas(cardNumber, monthStr.toInt(), yearStr.toInt(), cvc,zipCode )
                            Log.i("cardData", "initOnClick: $cardData ")
                           initiatePaymentProcess(cardData)
                        }
                    }
                }
                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

    }

    private fun isEmptyField(): Boolean {

        if (TextUtils.isEmpty(binding.etCardNumber.text.toString().trim())) {
            showToast("Please enter card number")
            return false
        }
        if (TextUtils.isEmpty(binding.etExpirationDate.text.toString().trim())) {
            showToast("Please enter expiry date")
            return false
        }
        if (TextUtils.isEmpty(binding.etSecurityCode.text.toString().trim())) {
            showToast("Please enter security code ")
            return false
        }
        if (TextUtils.isEmpty(binding.etZipCode.text.toString().trim())) {
            showToast("Please enter zip code")
            return false
        }
        return true
    }



    private fun initiatePaymentProcess(cardData: CardDatas) {
        val cardParams = createCardParams(
            cardData.cardNumber,
            cardData.expiryMonth,
            cardData.expiryYear,
            cardData.cvv,
            cardData.zipCode
        )

        createPaymentMethodAndFetchStripeId(cardParams, cardData.zipCode) { stripeId ->


            if (stripeId != null) {
                val data = HashMap<String, Any>()
                data["cardId"] = stripeId.toString()
                data["isPrimary"] = isDefault

                Log.i("payment", "initiatePaymentProcess: ")
                viewModel.addCard(data, Constants.ADD_CARD)

            } else {
                runOnUiThread {
                    showToast("Invalid card details")
                }
            }

        }
    }

    private fun createPaymentMethodAndFetchStripeId(
        cardParams: PaymentMethodCreateParams.Card,
        zipCode : String, // Add card name as a parameter
        completion: (String?) -> Unit
    ) {
        // Add card name to the BillingDetails
        val billingDetails = PaymentMethod.BillingDetails.Builder()
            .setName(zipCode)
            .build()

        // Create PaymentMethod with cardParams and billingDetails
        val paymentMethodParams = PaymentMethodCreateParams.create(cardParams, billingDetails)

        stripe.createPaymentMethod(paymentMethodParams, callback = object :
            ApiResultCallback<PaymentMethod> {
            override fun onSuccess(result: PaymentMethod) {
                stripeID = result.id
                Log.i("stripeId", "onSuccess: $stripeID   , $isDefault ")
                completion(stripeID)
            }

            override fun onError(e: Exception) {
                e.printStackTrace()
                completion(null)
            }
        })
    }


    private fun createCardParams(
        cardNumber: String,
        expMonth: Int,
        expYear: Int,
        cvc: String,
        zipCode: String
    ): PaymentMethodCreateParams.Card {
        return PaymentMethodCreateParams.Card.Builder()
            .setNumber(cardNumber)
            .setExpiryMonth(expMonth)
            .setExpiryYear(expYear)
            .setCvc(cvc)
            .build()


    }


}