package com.pickup.sports.ui.home_modules.create_host_game

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.data.model.PlaceDetails
import com.pickup.sports.databinding.ActivityCreateHostGameBinding
import com.pickup.sports.databinding.BottomSheetMinuteBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.ui.home_modules.repeat_module.RepeatActivity
import com.pickup.sports.ui.home_modules.select_sports.SelectSportsActivity
import com.pickup.sports.utils.BaseCustomBottomSheet
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.pickup.sports.data.model.GetGameByIdApiResponse
import com.pickup.sports.ui.home_modules.repeat_module.weekly_module.WeeklyFragment
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class CreateHostGameActivity : BaseActivity<ActivityCreateHostGameBinding>() {

    private val viewModel : CreateHostGameActivityVm by viewModels()
    private lateinit var apiKey: String
    private lateinit var placesClient: PlacesClient
    private lateinit var autocompleteSessionToken: AutocompleteSessionToken
    private lateinit var placeAdapter: PlaceAdapter
    private var gameData :  GetGameByIdApiResponse.Game ? = null
    private var gameId : String ? = null
    private var location: LatLng? = null
    private var imageUri : Uri ?= null
    private var state : String ? = null
    private var city  : String ? = null
    private var selectedDateTime: Calendar? = null
    private var selectedTimeInSeconds: Int? = null
    private var address : String ? = null
    private val places = ArrayList<PlaceDetails>()
    private var isFromEditGame = false // Flag to track navigation source
    private var side = 0
    private lateinit var timeSheet: BaseCustomBottomSheet<BottomSheetMinuteBinding>
    var selectDuration: Boolean = false
    var duration: Long? = null
    override fun getLayoutResource(): Int {
        return R.layout.activity_create_host_game
    }

    companion object{

        var id : String = ""
        var sportName : String ? = null
        var selectedSide : String ? = null
        var selectedDays : String  ? = null
        var repeatWeek : String = ""
        var selectedCustomDate: String = ""
    }
    override fun getViewModel(): BaseViewModel {
       return viewModel
    }

    override fun onCreateView() {
        initOnClick()
        Log.i("Dfdf", "onCreateView: ")

        apiKey = getString(R.string.api_key)
        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey)
        }
        placesClient = Places.createClient(this)
        autocompleteSessionToken = AutocompleteSessionToken.newInstance()

        locationAdapter()
        getGameData()
        searchLocation()
        initTimeSheet()


        initObserver()

    }

    private fun getGameData() {
         gameData = intent.getParcelableExtra<GetGameByIdApiResponse.Game>("gameData")
        isFromEditGame = intent.getBooleanExtra("isFromEditGame", false)


            binding.locationCard.visibility = View.GONE

        if (gameData != null){
            binding.tvPublishGame.text = "Update Game"
            binding.bean = gameData
            gameId = gameData?._id
            Log.i("Sdfdsf", "getGameData: $imageUri")
            WeeklyFragment.repeatDays = gameData?.repeatWeekDay
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
                        "createGame" -> {
                            val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast("Game created successfully")
                                val intent = Intent(this , HomeDashBoardActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
                            }
                        }
                        "updateGame" ->{
                            val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast("Game updated successfully")
                                val intent = Intent(this , HomeDashBoardActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(intent)
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


    private fun initTimeSheet() {
        timeSheet = BaseCustomBottomSheet(
            this, R.layout.bottom_sheet_minute
        ) {
            when (it?.id) {
                R.id.ivBack -> {
                    timeSheet.dismiss()
                }

                R.id.tvChangebtn -> {
                    selectDuration = true

                    Log.d(
                        "changeTime",
                        "initTimeSheet: hours : ${timeSheet.binding.numberPicker.value} minutes : ${timeSheet.binding.numberPickerMin.value}"
                    )

                    val hours: Int = timeSheet.binding.numberPicker.value // Get hours

                    val minuteIndex: Int = timeSheet.binding.numberPickerMin.value // Index of the selected minute
                    val minutesArray = Array(12) { (it * 5) } // [0, 5, 10, 15, ..., 55]
                    val minutes: Int = minutesArray[minuteIndex] // Get actual minute value

                    duration = hours * 3600L + minutes * 60L

                    Log.i("fdfd", "initTimeSheet: $duration")

                    // ✅ Fix: Use `minutes` instead of `numberPickerMin.value`
                    binding.etDuration.setText("$hours hours $minutes minutes")

                    timeSheet.dismiss()
                }

            }
        }
        initTimeDateData()
    }

    private fun initTimeDateData() {
        val hours = Array(24) { it.toString() }
        val minutes = Array(12) { (it * 5).toString() } // Generates [5, 10, 15, ..., 60]

        timeSheet.binding.numberPicker.minValue = 0
        timeSheet.binding.numberPicker.maxValue = hours.size - 1
        timeSheet.binding.numberPicker.displayedValues = hours

        timeSheet.binding.numberPickerMin.minValue = 0
        timeSheet.binding.numberPickerMin.maxValue = minutes.size - 1
        timeSheet.binding.numberPickerMin.displayedValues = minutes
    }

    private fun locationAdapter() {
        placeAdapter = PlaceAdapter(emptyList()) { places ->

            Log.i("dadadda", "initAdapter: $places ")

            setPlaceToEditText(places)

        }
        binding.rvEnterLocation.adapter = placeAdapter
        placeAdapter.notifyDataSetChanged()
    }

    private fun searchLocation() {

        binding.etLocation.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {

                if (places.isNotEmpty()) {
                        binding.locationCard.visibility = View.VISIBLE

                } else {
                    binding.locationCard.visibility = View.GONE
                }

            } else {
                binding.locationCard.visibility = View.GONE
            }

        }

        binding.etLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    getPlacePredictions(s.toString())
                    if (isFromEditGame){
                        binding.locationCard.visibility = View.GONE
                        isFromEditGame = false
                    }else{
                        binding.locationCard.visibility = View.VISIBLE
                    }

                } else {
                    placeAdapter.updatePlaces(emptyList())
                    binding.locationCard.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun getPlacePredictions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder().setQuery(query).build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val predictions = response.autocompletePredictions
            //  binding.cards.visibility = View.VISIBLE // Ensure card is visible when there's text
            fetchPlaceDetails(predictions)

        }.addOnFailureListener { exception ->
            exception.printStackTrace()
            Log.i("dsaas", "getPlacePredictions: $exception")
        }
    }
    // function to show full address details of location search

    private fun fetchPlaceDetails(predictions: List<AutocompletePrediction>) {
        val placeFields =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        places.clear()
        predictions.forEach { prediction ->
            val request = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()
            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val place = response.place

                val placeDetails = PlaceDetails(
                    name = place.name ?: "", address = place.address ?: "", location = place.latLng
                )
                places.add(placeDetails)


                if (places.size == predictions.size) {
                    placeAdapter.updatePlaces(places)
                }

                Log.i("places", "fetchPlaceDetails: ${placeDetails.location}")
            }.addOnFailureListener { exception ->
                exception.printStackTrace()
            }
        }
    }
    private fun setPlaceToEditText(places: PlaceDetails) {
        val name = places.name?.trim() ?: ""
        address = places.address?.trim() ?: ""

        // Avoid duplicate name in address
        val combinedAddress = if (address!!.contains(name, ignoreCase = true)) {
            address
        } else {
            "$name, $address"
        }
    //    val combinedAddress = "${places.name}, ${places.address}".trim(',').trim()
            binding.etLocation.setText(combinedAddress)
            location = places.location
            address = combinedAddress
            Log.i("dasd", "setPlaceToEditText: ${places.address} ")


        // Extract city & state
        val addressParts = places.address.split(",").map { it.trim() }
         city = if (addressParts.isNotEmpty()) addressParts[0] else ""  // First part is city
         state = if (addressParts.size > 1) addressParts[1] else ""  // Second part is state

        Log.i("AddressInfo", "City: $city, State: $state")
            binding.locationCard.visibility = View.GONE

    }
    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()

                }
                R.id.etDate, R.id.ivCalender ->{
                    calendarOpen()
                }
                R.id.etTime, R.id.ivTime ->{
                    timePickerOpen()
                }
                R.id.etDuration ->{
                    timeSheet.show()
                }
                R.id.etRepeat ->{

                    val intent  = Intent(this , RepeatActivity ::class.java)
                    startActivity(intent)
                }
                R.id.etSelectSport, R.id.sportDrop ->{
                    val intent = Intent(this , SelectSportsActivity::class.java)
                    intent.putExtra("side","CreateGame")
                    startActivity(intent)
                }
                R.id.tvPublishGame ->{
                    if (isEmptyField()) {
                        try {
                            // Helper function to use user input or fallback to gameData
                            fun String?.orGameData(value: String?): RequestBody =
                                this?.takeIf { it.isNotEmpty() }?.toRequestBody() ?: value?.toRequestBody() ?: "".toRequestBody()

                            Log.i("imageUri", "initOnClick: $imageUri")
                            val multipartImage = imageUri?.let { convertImageToMultipart(it) } // Can be null

                            val data = HashMap<String, RequestBody>()

                            data["sportsType"] = id.orGameData(gameData?.sportsData?._id)
                            data["title"] = binding.etTitle.text.toString().trim().orGameData(gameData?.title)
                            data["description"] = binding.etDescription.text.toString().trim().orGameData(gameData?.description)
                            data["address"] = address.orGameData(gameData?.address)
                            data["latitude"] = location?.latitude?.toString().orGameData(gameData?.latitude?.toString())
                            data["longitude"] = location?.longitude?.toString().orGameData(gameData?.longitude?.toString())
                            data["price"] = binding.etPrice.text.toString().trim().ifEmpty { "0" }.orGameData(gameData?.price.toString())
                            data["duration"] = duration?.toString().orGameData(gameData?.duration.toString())
                            data["state"] = state?.toString().orGameData(gameData?.state)

// Use formatted date if available, else fallback to gameData
                            data["hostTimestamp"] = getFormattedDateTime()?.toRequestBody() ?: gameData?.hostTimestamp?.toRequestBody() ?: "".toRequestBody()

                            data["minimumSlots"] = binding.etMinimumPlayer.text.toString().trim().orGameData(gameData?.minimumSlots.toString())
                            data["maximumSlots"] = binding.etMaximumPlayer.text.toString().trim().orGameData(gameData?.maximumSlots.toString())

// Determine repeatType
                            val repeatType = when (selectedSide) {
                                "Weekly" -> 2
                                "Custom" -> 3
                                else -> 1
                            }
                            data["repeatType"] = repeatType.toString().toRequestBody("text/plain".toMediaType())

// Handle repeatWeekDay and repeatCustomDate
                            repeatWeek?.toString()?.let { data["repeatWeekDay"] = it.toRequestBody() }
                                ?: gameData?.repeatWeekDay?.toString()?.let { data["repeatWeekDay"] = it.toRequestBody() }

                            selectedCustomDate?.toString()?.let { data["repeatCustomDate"] = it.toRequestBody() }
                                ?: gameData?.repeatCustomDate?.toString()?.let { data["repeatCustomDate"] = it.toRequestBody() }

// Check if updating or creating a game
                            if (binding.tvPublishGame.text == "Update Game") {
                                // Allow null multipartImage when updating
                                viewModel.updateGame(multipartImage, data, Constants.UPDATE_GAME + gameId)
                            } else {
                                // Ensure multipartImage is not null when creating a game
                                if (multipartImage != null) {
                                    viewModel.createGame(multipartImage, data, Constants.CREATE_GAME)
                                } else {
                                    Toast.makeText(this, "Please select an image to create a game", Toast.LENGTH_SHORT).show()
                                }
                            }


                        }catch (e : Exception){
                            e.printStackTrace()
                        }




                    }
                }
                R.id.ivUploadImage ->{
                    ImagePicker.with(this)
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent { intent ->
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                            startForImageResult.launch(intent)
                            binding.progressBar.visibility = View.VISIBLE  // Show loader
                        }

                }

            }
        })
    }


   //  permission required
//private val startForImageResult =
//    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
//        try {
//            val resultCode = result.resultCode
//            val data = result.data
//            if (resultCode == RESULT_OK) {
//                val fileUri = data?.data
//                if (fileUri != null) {
//                    // Try to take persistable permissions safely
//                    try {
//                        contentResolver.takePersistableUriPermission(
//                            fileUri,
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                        )
//                    } catch (e: SecurityException) {
//                        e.printStackTrace()  // Prevent crash if permission is denied
//                    }
//
//                    // Set the image and hide extra views
//                    binding.ivUploadImage.setImageURI(fileUri)
//                    binding.ivAddImage.visibility = View.GONE
//                    binding.tvUploadPhotos.visibility = View.GONE
//                    imageUri = fileUri
//                }
//            } else if (resultCode == ImagePicker.RESULT_ERROR) {
//                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
//                binding.ivAddImage.visibility = View.VISIBLE
//                binding.tvUploadPhotos.visibility = View.VISIBLE
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()  // Catch any unexpected errors to avoid crashing
//        }
//    }






    private fun calendarOpen() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this, R.style.DialogTheme, { _, selectedYear, selectedMonth, selectedDay ->
                if (selectedDateTime == null) selectedDateTime = Calendar.getInstance()
                selectedDateTime!!.set(selectedYear, selectedMonth, selectedDay)

                // Display selected date
                val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                binding.etDate.setText(dateFormat.format(selectedDateTime!!.time))
            }, year, month, day
        )

        datePickerDialog.show()
        val positiveColor = ContextCompat.getColor(this, R.color.colorPrimary)
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(positiveColor)
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
//        applyDialogButtonColor(datePickerDialog)
    }

    private val startForImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            try {
                val resultCode = result.resultCode
                val data = result.data

                when (resultCode) {
                    RESULT_OK -> {
                        val fileUri = data?.data
                        if (fileUri != null) {
                            try {
                                contentResolver.takePersistableUriPermission(
                                    fileUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                )
                            } catch (e: SecurityException) {
                                e.printStackTrace()
                            }

                            binding.ivUploadImage.post {
                                binding.ivUploadImage.setImageURI(fileUri)
                                binding.progressBar.visibility = View.GONE
                                binding.ivAddImage.visibility = View.GONE
                                binding.tvUploadPhotos.visibility = View.GONE
                                imageUri = fileUri
                            }
                        } else {
                            binding.progressBar.visibility = View.GONE  // Ensure loader hides if no file selected
                        }
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                        binding.ivAddImage.visibility = View.VISIBLE
                        binding.tvUploadPhotos.visibility = View.VISIBLE
                        binding.progressBar.visibility = View.GONE
                    }
                    RESULT_CANCELED -> {
                        binding.progressBar.visibility = View.GONE  // Hide loader when user cancels
                    }
                    else -> {
                        binding.progressBar.visibility = View.GONE  // Handle unexpected cases
                    }
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                e.printStackTrace()
            }
        }




    private fun timePickerOpen() {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this, R.style.DialogTheme, { _, hourOfDay, minuteOfHour ->
                if (selectedDateTime == null) selectedDateTime = Calendar.getInstance()
                selectedDateTime!!.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDateTime!!.set(Calendar.MINUTE, minuteOfHour)

                // Format time for display
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()) // Always AM/PM format
                val formattedTime = timeFormat.format(selectedDateTime!!.time)

                binding.etTime.setText(formattedTime)

            }, hour, minute, false
        )

        timePickerDialog.show()
        val positiveColor = ContextCompat.getColor(this, R.color.colorPrimary)
        timePickerDialog.getButton(TimePickerDialog.BUTTON_NEGATIVE).setTextColor(positiveColor)
        timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setTextColor(positiveColor)
    }




    private fun getFormattedDateTime(): String? {
        selectedDateTime?.let {
            val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault())
            return apiFormat.format(it.time)
        }
        return null
    }




    private fun isEmptyField(): Boolean {
        if (TextUtils.isEmpty(binding.etSelectSport.text.toString().trim())){
            showToast("Please select sport")
            return false
        }
        if (TextUtils.isEmpty(binding.etTitle.text.toString().trim())){
            showToast("Please enter title")
            return false
        }
        if (TextUtils.isEmpty(binding.etDescription.text.toString().trim())){
            showToast("Please enter description")
            return false
        }
        if (TextUtils.isEmpty(binding.etMinimumPlayer.text.toString().trim())){
            showToast("Please select minimum players")
            return false
        }
        if (TextUtils.isEmpty(binding.etMaximumPlayer.text.toString().trim())){
            showToast("Please select maximum players")
            return false
        }
        if (TextUtils.isEmpty(binding.etDate.text.toString().trim())){
            showToast("Please select date")
            return false
        }
        if (TextUtils.isEmpty(binding.etTime.text.toString().trim())){
            showToast("Please select time")
            return false
        }
        if (TextUtils.isEmpty(binding.etDuration.text.toString().trim())){
            showToast("Please set game duration")
            return false
        }
        // Convert minimum and maximum players to integers
        val minPlayers = binding.etMinimumPlayer.text.toString().trim().toIntOrNull() ?: 0
        val maxPlayers = binding.etMaximumPlayer.text.toString().trim().toIntOrNull() ?: 0

        // Validate minimum and maximum player count
        if (minPlayers < 2) {
            showToast("Minimum players should be at least 2")
            return false
        }
        if (maxPlayers > 100) {
            showToast("Maximum players should not be more than 100")
            return false
        }
        if (minPlayers > maxPlayers) {
            showToast("Minimum players cannot be greater than maximum players")
            return false
        }
        return true
    }

//    private fun convertImageToMultipart(imageUri: Uri): MultipartBody.Part {
//        val file = FileUtil.getTempFile(this, imageUri)
//        return MultipartBody.Part.createFormData(
//            "photos",
//            file?.name,
//            file!!.asRequestBody("image/*".toMediaTypeOrNull())
//        )
//    }

    private fun convertImageToMultipart(imageUri: Uri?): MultipartBody.Part? {
        if (imageUri == null) return null
        val file = FileUtil.getTempFile(this, imageUri) ?: return null

        return MultipartBody.Part.createFormData(
            "photos",
            file.name,
            file.asRequestBody("image/*".toMediaTypeOrNull())
        )
    }


    override fun onResume() {
        super.onResume()
        Log.i("Dfdf", "onResume: ")
        if (sportName != null){
            Log.i("fdsfdsf", "onResume: $sportName")
            binding.etSelectSport.setText(sportName)
        }

        Log.i("sdsadsa", "onCreateView: $repeatWeek")
        Log.i("sdsadsa", "onCreateView: $selectedCustomDate")
        if (selectedSide != null){
            when(selectedSide){
                "Weekly" -> binding.etRepeat.setText("Weekly")
                "Custom" ->  binding.etRepeat.setText("Custom")
                else ->{ binding.etRepeat.setText("Repeat")}
            }
        }

        if (selectedDays != null){
            binding.day.text = "Every $selectedDays"
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        selectedDays = ""
        selectedSide = ""

    }
}