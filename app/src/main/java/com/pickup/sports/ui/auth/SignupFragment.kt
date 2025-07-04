package com.pickup.sports.ui.auth

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.databinding.FragmentSignupBinding
import com.pickup.sports.ui.base.BaseFragment
import com.pickup.sports.ui.base.BaseViewModel

class SignupFragment : BaseFragment<FragmentSignupBinding>() {
    private val viewModel: AuthCommonVM by viewModels()

    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initObserver()
    }

    private fun initObserver() {

    }

    private fun initOnClick() {

    }


    override fun getLayoutResource(): Int {
        return R.layout.fragment_signup
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initView() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when (it?.id) {
                R.id.buttonSignup -> {
                  onBackPressed()
                }
            }
        })
    }

}