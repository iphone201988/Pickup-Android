package com.pickup.sports.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.room.Room
import com.pickup.sports.App
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.local.SharedPrefManager
import com.pickup.sports.data.network.ErrorCodes
import com.pickup.sports.data.network.NetworkError
import com.pickup.sports.databinding.ViewProgressSheetBinding
import com.pickup.sports.ui.base.connectivity.ConnectivityProvider
import com.pickup.sports.utils.AlertManager
import com.pickup.sports.utils.event.NoInternetSheet
import com.pickup.sports.utils.hideKeyboard
import com.pickup.sports.room_data.AppDb
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject

abstract class BaseActivity<Binding : ViewDataBinding> : AppCompatActivity(),
    ConnectivityProvider.ConnectivityStateListener {
    var db: AppDb? = null
    lateinit var progressDialogAvl: ProgressDialogAvl
    private  var progressSheet: ProgressSheet?=null
    open val onRetry: (() -> Unit)? = null


    var onStartCount = 0
    lateinit var binding: Binding
    val app: App
        get() = application as App

    private lateinit var connectivityProvider: ConnectivityProvider
    private var noInternetSheet: NoInternetSheet? = null

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db=  Room.databaseBuilder(this, AppDb::class.java, applicationContext.packageName).build()
        /*onStartCount = 1
        if (savedInstanceState == null) // 1st time
        {
            this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
        } else // already created so reverse animation
        {
            onStartCount = 2
        }*/
        val layout: Int = getLayoutResource()
        binding = DataBindingUtil.setContentView(this, layout)
        val vm = getViewModel()
        binding.setVariable(BR.vm, vm)
        vm.onUnAuth.observe(this) {
            showUnauthorised()
        }
//        binding.setVariable(BR.vm, getViewModel())
        connectivityProvider = ConnectivityProvider.createProvider(this)
        connectivityProvider.addListener(this)
        progressDialogAvl = ProgressDialogAvl(this)

        onCreateView()
    }

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager

    protected abstract fun getLayoutResource(): Int
    protected abstract fun getViewModel(): BaseViewModel
    protected abstract fun onCreateView()

    fun showToast(msg: String? = "Something went wrong !!") {
        Toast.makeText(this, msg ?: "Showed null value !!", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    fun showLoading(s: String?) {
        progressSheet?.dismissAllowingStateLoss()
        progressSheet = ProgressSheet(object : ProgressSheet.BaseCallback {
            override fun onClick(view: View?) {}
            override fun onBind(bind: ViewProgressSheetBinding) {
                progressSheet?.showMessage(s);
            }
        })
        progressSheet?.isCancelable=false
        progressSheet?.show(supportFragmentManager, progressSheet?.tag)

    }
    fun showUnauthorised() {
        sharedPrefManager.clear()
        val intent = Intent(this, HomeDashBoardActivity::class.java)
        startActivity(intent)
        finishAffinity()
    }

    fun hideLoading() {
        progressDialogAvl.isLoading(false)
//        progressSheet?.dismissAllowingStateLoss()
    }

    fun onError(error: Throwable, showErrorView: Boolean) {
        if (error is NetworkError) {

            when (error.errorCode) {
                ErrorCodes.SESSION_EXPIRED -> {
                    showToast(getString(R.string.session_expired))
                    app.onLogout()
                }

                else -> AlertManager.showNegativeAlert(
                    this,
                    error.message,
                    getString(R.string.alert)
                )
            }
        } else {
            AlertManager.showNegativeAlert(
                this,
                getString(R.string.please_try_again),
                getString(R.string.alert)
            )
        }
    }

    override fun onDestroy() {

        connectivityProvider.removeListener(this)
        super.onDestroy()
    }


    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        if (supportFragmentManager.isStateSaved){
            return
        }
        if (noInternetSheet == null) {
            noInternetSheet = NoInternetSheet()
            noInternetSheet?.isCancelable = false
        }
        if (state.hasInternet()) {
            if (noInternetSheet?.isVisible == true)
                noInternetSheet?.dismiss()
        } else {
            if (noInternetSheet?.isVisible == false)
                noInternetSheet?.show(supportFragmentManager, noInternetSheet?.tag)
        }
    }

    private fun ConnectivityProvider.NetworkState.hasInternet(): Boolean {
        return (this as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
    }

    override fun onStart() {
        super.onStart()
        /*if (onStartCount > 1) {
            this.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        } else if (onStartCount == 1) {
            onStartCount++
        }*/
    }
    fun textToRequestBody(text: String?): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), text!!)
    }

    fun multipartImageBody(image: File): MultipartBody.Part {
        return MultipartBody.Part.createFormData(
            "profile",
//            "profileImage",
            image.name,
            RequestBody.create("image/png".toMediaTypeOrNull(), image)
        )
    }

    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(InputMethodManager::class.java)
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }


}