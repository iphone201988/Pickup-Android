package com.pickup.sports.ui.home_modules.edit_profile

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.data.model.GetProfileApiResponse
import com.pickup.sports.databinding.ActivityEditProfileBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.account_module.AccountActivity
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pickup.sports.BR
import com.pickup.sports.data.model.GetAllSportsApiResponse
import com.pickup.sports.databinding.BottomsheetMultiSelectSportsBinding
import com.pickup.sports.databinding.ItemLayoutMultiSeletSportsBinding
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.game_details.GameDetailsActivity
import com.pickup.sports.ui.home_modules.location_module.LocationActivity
import com.pickup.sports.utils.BaseCustomBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class EditProfileActivity : BaseActivity<ActivityEditProfileBinding>() ,BaseCustomBottomSheet.Listener {

    private val viewModel : EditProfileActivityVm by viewModels()
    private var imageUri : Uri ?= null
    private var countryCode: String? = null
    private var selectedValue: Int = 0
    private lateinit var selectSportsBottomSheet : BaseCustomBottomSheet<BottomsheetMultiSelectSportsBinding>
    private lateinit var sportsAdapter : SimpleRecyclerViewAdapter<GetAllSportsApiResponse.Sport, ItemLayoutMultiSeletSportsBinding>
    private var selectedSportNames: String = ""
    private var selectedSportsId: String = ""

    companion object{
        var locationAddress: String? = null
        var searchLat = 0.0
        var searchLong = 0.0
    }
    override fun getLayoutResource(): Int {
       return R.layout.activity_edit_profile
    }

    override fun getViewModel(): BaseViewModel {
         return viewModel
    }

    override fun onCreateView() {
       initOnClick()
        viewModel.getProfile(Constants.GET_PROFILE)
        viewModel.getAllSports(Constants.GET_ALL_SPORTS)

        check()
        initBottomSheet()
        initAdapter()
        countryCodePicker()
        initObserver()

    }

    private fun initBottomSheet() {
        selectSportsBottomSheet = BaseCustomBottomSheet(this , R.layout.bottomsheet_multi_select_sports,this)


        selectSportsBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        selectSportsBottomSheet.behavior.isDraggable = true
        
    }

    private fun countryCodePicker() {

        binding.countyCodePicker.setOnCountryChangeListener {
            countryCode = binding.countyCodePicker.selectedCountryCodeWithPlus
            Log.i("dsdsad", "countryCodePicker: $countryCode")

        }
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
                                    binding.bean = myDataModel.user
                                    countryCode = myDataModel.user?.countryCode
                                    val code = myDataModel.user?.countryCode?.replace("+", "")?.toIntOrNull()
                                    code?.let {
                                        binding.countyCodePicker.setCountryForPhoneCode(it)
                                    }

                                }
                            }
                        }
                        "editProfile" ->{
                            val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast(it.message.toString())
                                val intent = Intent(this , AccountActivity :: class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            }
                        }
                        "getAllSports" ->{
                            try {
                                val myDataModel : GetAllSportsApiResponse ? = ImageUtils.parseJson(it.data.toString())
                                if (!myDataModel?.sports.isNullOrEmpty()) {
                                    val allSports = GetAllSportsApiResponse.Sport(
                                        _id = "", // Assign a unique ID
                                        name = "All Sports",
                                        isSelected = false
                                    )

                                    val updatedList = mutableListOf(allSports) // Add "All Sports" at the beginning

                                    myDataModel?.sports?.filterNotNull()
                                        ?.let { it1 -> updatedList.addAll(it1) } // Add the remaining sports

                                    sportsAdapter.list= updatedList
                                    Log.i("dsadsad", "initObserver: $updatedList ")
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
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

    private fun check() {
        binding.etFirstName.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                selection(1)
            }
        }
        binding.etLastName.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                selection(2)
            }
        }
        binding.etEmail.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus){
                selection(3)
            }
        }
    }


    private fun initAdapter() {
        sportsAdapter   = SimpleRecyclerViewAdapter(R.layout.item_layout_multi_selet_sports,
            BR.bean){ v, m, pos  ->
            when(v.id){
                R.id.consMain ->{
                    m.isSelected = !m.isSelected
                    sportsAdapter.notifyItemChanged(pos)

                    // Update global selected names
                    selectedSportNames = sportsAdapter.getList()
                        .filter { it.isSelected }
                        .joinToString(", ") { it.name.orEmpty() }

                    selectedSportsId = sportsAdapter.getList()
                        .filter { it.isSelected }
                        .joinToString(",") { "${it._id}" }

                    // Update UI
                  binding.etSelectSport.setText(selectedSportNames)
                }
            }

        }
        selectSportsBottomSheet.binding.rvSelectSports.adapter = sportsAdapter
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }

                R.id.tvSaveChanges ->{
                    if (isEmptyField()){
                        val multipartImage = imageUri?.let { convertImageToMultipart(it) }

                            val data = HashMap<String, RequestBody>()
                            data["firstName"] = binding.etFirstName.text.toString().trim().toRequestBody()
                            data["lastName"]  = binding.etLastName.text.toString().trim().toRequestBody()
                            data["email"] = binding.etEmail.text.toString().trim().toRequestBody()
                            data["countryCode"] = countryCode.toString().toRequestBody()
                            data["phone"] = binding.etPhoneNumber.text.toString().trim().toRequestBody()
                            data["city"] = binding.etCity.text.toString().trim().toRequestBody()
                            data["latitude"] = searchLat.toString().toRequestBody()
                            data["longitude"] = searchLong.toString().toRequestBody()
                            data["sportId"] = selectedSportsId.toRequestBody()
                            viewModel.editProfile(multipartImage,data,Constants.UPDATE_PROFILE)
//                        }else{
//                            val data = HashMap<String, RequestBody>()
//                            data["firstName"] = binding.etFirstName.text.toString().trim().toRequestBody()
//                            data["lastName"]  = binding.etLastName.text.toString().trim().toRequestBody()
//                            data["email"] = binding.etEmail.text.toString().trim().toRequestBody()
//                            viewModel.editProfile(null,data,Constants.UPDATE_PROFILE)
//                        }

//                        val intent = Intent(this ,AccountActivity::class.java)
//                        startActivity(intent)
                    }
                }
                R.id.profileImage -> {
                    ImagePicker.with(this)
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent { intent ->
                            startForImageResult.launch(intent)
                            binding.progressBar.visibility = View.VISIBLE  // Show loader
                        }
                }
                R.id.etSelectSport ->{
                    selectSportsBottomSheet.show()
                }
                R.id.etCity ->{
                    LocationActivity.activity = "EditProfile"
                    val intent = Intent(this , LocationActivity:: class.java )
                    startActivity(intent)
                }

            }
        })
    }
    private val startForImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        try {
            val resultCode = result.resultCode
            val data = result.data
            if (resultCode == RESULT_OK) {
                val fileUri = data?.data
                imageUri = fileUri
                binding.profileImage.setImageURI(fileUri)
                binding.progressBar.visibility = View.GONE

              //  Log.i("dasd", ": $imageUri")
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "Task canceled", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            binding.progressBar.visibility = View.GONE
            e.printStackTrace()
        }
    }
    private fun selection(selectedValue :Int) {
        when(selectedValue){
            1->{
                binding.etFirstName.setBackgroundResource(R.drawable.bg_selected_edit_text)
                binding.etLastName.setBackgroundResource(R.drawable.bg_edit_text)
                binding.etEmail.setBackgroundResource(R.drawable.bg_edit_text)
            }
            2->{
                binding.etLastName.setBackgroundResource(R.drawable.bg_selected_edit_text)
                binding.etFirstName.setBackgroundResource(R.drawable.bg_edit_text)
                binding.etEmail.setBackgroundResource(R.drawable.bg_edit_text)
            }
            3->{
                binding.etFirstName.setBackgroundResource(R.drawable.bg_edit_text)
                binding.etLastName.setBackgroundResource(R.drawable.bg_edit_text)
                binding.etEmail.setBackgroundResource(R.drawable.bg_selected_edit_text)
            }

        }

    }


    private fun isEmptyField(): Boolean{
        if (TextUtils.isEmpty(binding.etFirstName.text.toString().trim())){
            showToast("Please enter first name")
            return false
        }
        if (TextUtils.isEmpty(binding.etLastName.text.toString().trim())){
            showToast("Please enter last name")
            return false
        }
        if (TextUtils.isEmpty(binding.etEmail.text.toString().trim())){
            showToast("Please enter email")
            return false
        }
        return true
    }

    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(this, imageUri)
        return MultipartBody.Part.createFormData(
            "profileImage",
            file!!.name,
            file.asRequestBody("image/*".toMediaTypeOrNull())
        )
    }

    override fun onViewClick(view: View?) {

    }

    override fun onResume() {
        if (locationAddress !=null){
            binding.etCity.setText(locationAddress)
        }

        super.onResume()
    }

}