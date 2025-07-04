package com.pickup.sports.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.pickup.sports.BR
import com.pickup.sports.data.local.SharedPrefManager
import com.pickup.sports.room_data.AppDb
import com.pickup.sports.utils.hideKeyboard
import javax.inject.Inject


abstract class BaseFragment<Binding : ViewDataBinding> : Fragment() {
    lateinit var binding: Binding

    @Inject
    lateinit var sharedPrefManager: SharedPrefManager
    lateinit var progressDialogAvl: ProgressDialogAvl
    val parentActivity: BaseActivity<*>?
        get() = activity as? BaseActivity<*>
    var db: AppDb? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onCreateView(view)
        progressDialogAvl = ProgressDialogAvl(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val layout: Int = getLayoutResource()
        binding = DataBindingUtil.inflate(layoutInflater, layout, container, false)
        val vm = getViewModel()
        binding.setVariable(BR.vm, vm)
        vm.onUnAuth.observe(viewLifecycleOwner) {
            val activity = requireActivity() as BaseActivity<*>
            activity.showUnauthorised()
        }
        //binding.setVariable(BR.vm, getViewModel())
        db = Room.databaseBuilder(
            requireActivity(), AppDb::class.java, requireActivity().applicationContext.packageName
        ).build()
        return binding.root
    }

    protected abstract fun getLayoutResource(): Int
    protected abstract fun getViewModel(): BaseViewModel
    protected abstract fun onCreateView(view: View)
    override fun onPause() {
        super.onPause()
        activity?.hideKeyboard()
    }

    fun showLoading(s: String?) {
        parentActivity?.showLoading(s)
    }

    fun showLoading(s: Int) {
        parentActivity?.showLoading(getString(s))
    }

    fun showLoading(){
        progressDialogAvl.isLoading(true)
    }
    fun hideLoading(){
        progressDialogAvl.isLoading(false)
        //   parentActivity?.hideLoading()
    }

    fun showToast(msg: String? = "Something went wrong !!") {
        Toast.makeText(requireContext(), msg ?: "Showed null value !!", Toast.LENGTH_SHORT).show()
    }

    open fun onBackPressed() {
        findNavController().popBackStack()
    }

}