package com.pickup.sports.ui.home_modules.game_details

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pickup.sports.BR
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.data.model.GetGameByIdApiResponse
import com.pickup.sports.data.model.Images
import com.pickup.sports.data.model.LoginApiResponse
import com.pickup.sports.databinding.ActivityGameDetailsBinding
import com.pickup.sports.databinding.BottomSheetCancelGameBinding
import com.pickup.sports.databinding.BottomSheetLeaveGameBinding
import com.pickup.sports.databinding.BottomSheetLoginBinding
import com.pickup.sports.databinding.BottomSheetSignUpBinding
import com.pickup.sports.databinding.BottomsheetPlayersListBinding
import com.pickup.sports.databinding.ItemLayoutPlayersBinding
import com.pickup.sports.ui.auth.forgot_password.ForgotPasswordActivity
import com.pickup.sports.ui.auth.login_signup_module.MainViewModel
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.chat_module.ChatActivity
import com.pickup.sports.ui.home_modules.payment_policy.PaymentPolicyActivity
import com.pickup.sports.utils.BaseCustomBottomSheet
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.SocketHandler
import com.pickup.sports.utils.Status
import com.google.firebase.messaging.FirebaseMessaging
import com.pickup.sports.data.model.GetAllSportsApiResponse
import com.pickup.sports.databinding.BottomsheetMultiSelectSportsBinding
import com.pickup.sports.databinding.ItemLayoutMultiSeletSportsBinding
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.ui.home_modules.edit_profile.EditProfileActivity
import com.pickup.sports.ui.home_modules.location_module.LocationActivity
import dagger.hilt.android.AndroidEntryPoint
import io.socket.client.Socket
import org.json.JSONObject
import java.util.Locale

@AndroidEntryPoint
class GameDetailsActivity : BaseActivity<ActivityGameDetailsBinding>() ,BaseCustomBottomSheet.Listener{
    private val viewModel : MainViewModel by viewModels()
    private lateinit var playerBottomSheet : BaseCustomBottomSheet<BottomsheetPlayersListBinding>
    private lateinit var cancelGameBottomSheet : BaseCustomBottomSheet<BottomSheetCancelGameBinding>
    private lateinit var  leaveGameBottomSheet : BaseCustomBottomSheet<BottomSheetLeaveGameBinding>
    private lateinit var loginBottomSheet : BaseCustomBottomSheet<BottomSheetLoginBinding>
    private lateinit var signUpBottomSheet : BaseCustomBottomSheet<BottomSheetSignUpBinding>
    private lateinit var selectSportsBottomSheet : BaseCustomBottomSheet<BottomsheetMultiSelectSportsBinding>
    private lateinit var sportsAdapter : SimpleRecyclerViewAdapter<GetAllSportsApiResponse.Sport, ItemLayoutMultiSeletSportsBinding>
    private lateinit var playerAdapter : SimpleRecyclerViewAdapter<GetGameByIdApiResponse.Game.SlotId,ItemLayoutPlayersBinding>
    private var gameDate : String ? = null
    private var countryCode: String? = "+1"
    private lateinit var mSocket: Socket
    private var imageList = arrayListOf<Images>()
    private var city : String ? = null
    private var gameDetail : GetGameByIdApiResponse.Game ? = null
    private var id : String ? = null

    private var selectedSportNames: String = ""
    private var selectedSportsId: String = ""
    var token = "123"

    companion object{
        var side : String ? = null
        var gameStatus : String ? =null
        var locationAddress: String? = null

            var lat = 0.0
            var long = 0.0
            var searchLat = 0.0
            var searchLong = 0.0
    }
    override fun getLayoutResource(): Int {
        return R.layout.activity_game_details
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        setData()
        getData()
        initView()
        socketHandler()
        initOnClick()
        initBottomSheet()
        countryCodePicker()
        //getList()
        initAdapter()
        initObserver()

        city = getCityFromLatLong(this,lat, long)

    }

    private fun countryCodePicker() {
        signUpBottomSheet.binding.countyCodePicker.setDefaultCountryUsingPhoneCode(1)
        signUpBottomSheet.binding.countyCodePicker.resetToDefaultCountry()

        signUpBottomSheet.binding.countyCodePicker.setOnCountryChangeListener {
            countryCode =   signUpBottomSheet.binding.countyCodePicker.selectedCountryCodeWithPlus ?: "+1"
            Log.i("dsdsad", "countryCodePicker: $countryCode")

        }
    }
    private fun socketHandler() {
        val token = sharedPrefManager.getCurrentUser()?.token
        try {
            if (!token.isNullOrEmpty()) {
                SocketHandler.setSocket(token)  // Establish socket connection with token
                SocketHandler.establishConnection()
                mSocket = SocketHandler.getSocket()!!
                Log.i("SocketHandler", "socketHandler: $mSocket")
                Log.e("SocketHandler", "Connection is established.")


            } else {
                Log.e("SocketHandler", "Token is missing! Cannot establish connection.")
            }
        }catch ( e: Exception){
            e.printStackTrace()
        }

    }

    private fun getData() {
        id = intent.getStringExtra("id")
        val game = intent.getStringExtra("game")


        if (id != null){
            if (sharedPrefManager.getCurrentUser() != null){
                val data = HashMap<String,Any>()
                data["gameId"]  = id.toString()
                data["userId"] = sharedPrefManager.getCurrentUser()?.userId.toString()
                viewModel.singleGameDetail(data , Constants.SINGLE_GAME_DETAIL)
            }
            else{
                val data = HashMap<String,Any>()
                data["gameId"]  = id.toString()
                viewModel.singleGameDetail(data , Constants.SINGLE_GAME_DETAIL)
            }

        }

        if (game == "joined"){
            binding.tvJoinGame.visibility = View.GONE
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
                        "login" ->{
                            val myDataModel : LoginApiResponse? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast("Login Successfully")
                                loginBottomSheet.dismiss()
                                sharedPrefManager.saveUser(myDataModel)
                                socketHandler()
                            }
                        }
                        "signUp" ->{
                            val myDataModel : LoginApiResponse? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast("Register Successfully")
                                signUpBottomSheet.dismiss()
                                sharedPrefManager.saveUser(myDataModel)
                                socketHandler()

                            }
                        }
                        "singleGameDetail" ->{
                            val myDataModel: GetGameByIdApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.game != null){
                                    try {
                                        val game = intent.getStringExtra("game")
                                        if (game?.contains("joined") == true){
                                            binding.tvJoinGame.visibility = View.GONE
                                        }
                                        binding.bean  = myDataModel.game
                                        gameDetail = myDataModel.game
                                        gameDate = myDataModel.game!!.hostTimestamp


                                        if (myDataModel.game!!.slotIds?.isNotEmpty() == true){
                                            binding.tvPlayer.visibility = View.VISIBLE
                                            playerAdapter.list = myDataModel.game!!.slotIds
                                            binding.groupChat.isFocusable = true
                                            binding.groupChat.isClickable = true
                                            binding.groupChat.setTextColor(ContextCompat.getColor(this, R.color.heading_color))
                                        }else{
                                            binding.tvPlayer.visibility = View.INVISIBLE
                                            binding.groupChat.isFocusable = false
                                            binding.groupChat.isClickable = false
                                            binding.groupChat.setTextColor(ContextCompat.getColor(this, R.color.grey))

                                        }
                                    }catch (e : Exception){
                                        e.printStackTrace()
                                    }
                                }
                                    }


                        }
                        "leaveGame" ->{
                            val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast("You left the game successfully")
                                onBackPressedDispatcher.onBackPressed()
                                leaveGameBottomSheet.dismiss()
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

    private fun initView() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            token = it.result
            Log.i("daadd", "initView: $token")
        }
    }

    private fun setData() {
        var status = intent.getIntExtra("status",0)

        when (side) {
             "My games" -> {
                 binding.tvJoinGame.backgroundTintList = ContextCompat.getColorStateList(this, R.color.pink)
                 binding.tvJoinGame.text = "Leave Game"

                 Log.i("dff", "setData: $gameStatus")
                 Log.i("dfdf", "setData: $status")
                 if (gameStatus == "Past"){
                     if (status == 4){
                         binding.groupChat.visibility = View.GONE
                         binding.messageHost.visibility = View.GONE
                         binding.tvJoinGame.visibility = View.GONE
                     }else{
                         binding.groupChat.visibility = View.VISIBLE
                         binding.messageHost.visibility = View.VISIBLE
                         binding.tvJoinGame.visibility = View.GONE
                     }

                 }else{
                     binding.groupChat.visibility = View.VISIBLE
                     binding.messageHost.visibility = View.VISIBLE
                     binding.tvJoinGame.visibility = View.VISIBLE
                 }
//                 binding.groupChat.visibility = View.INVISIBLE
//                 binding.messageHost.visibility = View.INVISIBLE
            }
            "Join Game" -> {
                binding.tvJoinGame.backgroundTintList = ContextCompat.getColorStateList(this, R.color.view_color)
                binding.tvJoinGame.text = "Join Game"
//                binding.groupChat.visibility = View.VISIBLE
//                binding.messageHost.visibility = View.VISIBLE
                if (sharedPrefManager.getCurrentUser() != null){
                    binding.groupChat.visibility = View.VISIBLE
                    binding.messageHost.visibility = View.VISIBLE
                    binding.tvJoinGame.visibility = View.VISIBLE
                }
                else{
                    binding.groupChat.visibility = View.GONE
                    binding.messageHost.visibility = View.GONE
                    binding.tvJoinGame.visibility = View.GONE
                }
            }
        }


    }


    private fun initAdapter() {
        playerAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_players,BR.bean){v,m,pos ->
            when(v.id){
                R.id.playerCons->{


                }
            }

        }
        playerBottomSheet.binding.rvPlayers.adapter =playerAdapter
        playerAdapter.notifyDataSetChanged()

        sportsAdapter   = SimpleRecyclerViewAdapter(R.layout.item_layout_multi_selet_sports,BR.bean){v,m,pos  ->
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


                    Log.i("sportsId", "initAdapter: $selectedSportsId")

                    // Update UI
                    signUpBottomSheet.binding.etSelectSport.setText(selectedSportNames)
                }
            }

        }
        selectSportsBottomSheet.binding.rvSelectSports.adapter = sportsAdapter
    }


    private fun initBottomSheet() {
        playerBottomSheet  = BaseCustomBottomSheet(this, R.layout.bottomsheet_players_list,this)
        cancelGameBottomSheet  = BaseCustomBottomSheet(this , R.layout.bottom_sheet_cancel_game,this)
        leaveGameBottomSheet = BaseCustomBottomSheet(this , R.layout.bottom_sheet_leave_game,this)
        loginBottomSheet = BaseCustomBottomSheet(this,R.layout.bottom_sheet_login,this)
        signUpBottomSheet = BaseCustomBottomSheet(this , R.layout.bottom_sheet_sign_up,this)
        selectSportsBottomSheet = BaseCustomBottomSheet(this , R.layout.bottomsheet_multi_select_sports,this)


        signUpBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        signUpBottomSheet.behavior.isDraggable = true

        loginBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        loginBottomSheet.behavior.isDraggable = true


        playerBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        playerBottomSheet.behavior.isDraggable = true

        cancelGameBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        cancelGameBottomSheet.behavior.isDraggable = true

        leaveGameBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        leaveGameBottomSheet.behavior.isDraggable = true

        selectSportsBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        selectSportsBottomSheet.behavior.isDraggable = true
    }

//    private fun getList() {
//        imageList.add(Images(R.drawable.unsplash_image))
//        imageList.add(Images(R.drawable.iv_dumm))
//        imageList.add(Images(R.drawable.unsplash_image))
//
//        binding.viewPager.adapter = MyPagerAdapter(imageList)
//
//        binding.indicatorView.apply {
//            setSliderColor(Color.GRAY, Color.WHITE) // Inactive and active colors
//            setSlideMode(IndicatorSlideMode.SCALE) // Enables scaling effect
//            setIndicatorStyle(IndicatorStyle.CIRCLE)
//
//            setSliderWidth(resources.getDimension(com.intuit.sdp.R.dimen._4sdp)) // Normal dot size
//            setSliderHeight(resources.getDimension(com.intuit.sdp.R.dimen._4sdp))
//
//            setCheckedSlideWidth(resources.getDimension(com.intuit.sdp.R.dimen._8sdp)) // Larger size for selected dot
//
//            setupWithViewPager(binding.viewPager)
//        }
//
//
//
//    }


    private fun initOnClick() {
        viewModel.onClick.observe(this , Observer {
            when(it?.id){
                R.id.tvJoinGame ->{

                        if(binding.tvJoinGame.text.equals("Leave Game")){
                            leaveGameBottomSheet.show()
                        }
                        else{
                            if (gameDetail != null){
                                val intent = Intent(this, PaymentPolicyActivity::class.java)
                                intent.putExtra("data", gameDetail)
                                startActivity(intent)
                            }

                        }
                    }

                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }
                R.id.groupChat ->{

                        mSocket.on(Socket.EVENT_CONNECT) {

                        }
                        if (gameDetail?.chatId != null){
                            try {
                                Log.i("SocketHandler", "Socket connected. Joining host room...")

                                val jsonData = JSONObject().apply {
                                    put("type", 2)
                                    put("chatId", gameDetail!!.chatId)

                                }
                                // Emit the joinRoom event with the prepared data
                                mSocket.emit("joinRoom", jsonData)
                                Log.i("SocketHandler", "Emitted joinRoom event: $jsonData")

                                // Listen for the response
                                mSocket.on("roomId") { args ->
                                    Log.i("SocketHandler", "Received join host Room response: $args")

                                    if (args.isNotEmpty()) {
                                        try {
                                            val jsonString = args[0].toString()  // Convert object to String
                                            val jsonObject = JSONObject(jsonString) // Parse it as JSON
                                            Log.i("SocketHandler", "Parsed JSON: $jsonObject")

                                            // Access specific values if needed
                                            val roomId = jsonObject.optString("roomId", "Unknown")
                                            val isBlocked = jsonObject.optBoolean("isBlock",false)
                                            Log.i("SocketHandler", "Extracted Room ID: $roomId, $isBlocked" )

                                            if (roomId != null){
                                                val intent = Intent(this, ChatActivity::class.java)
                                                intent.putExtra("data", gameDetail)
                                                intent.putExtra("roomId",roomId)
                                                intent.putExtra("isBlocked", isBlocked)
                                                intent.putExtra("side","Group")
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                startActivity(intent)
                                            }
                                            Log.i("SocketHandler", "Extracted Room ID: $roomId")
                                        } catch (e: Exception) {
                                            Log.e("SocketHandler", "Error parsing JSON: ${e.message}")
                                        }
                                    }
                                }

                            }catch (e : Exception){
                                e.printStackTrace()
                            }
                        }


                }
                R.id.messageHost -> {
                        mSocket.on(Socket.EVENT_CONNECT) {

                        }
                        if (gameDetail?._id != null && gameDetail!!.hostData?._id != null) {
                            try {
                                Log.i("SocketHandler", "Socket connected. Joining room...")

                                val jsonData = JSONObject().apply {
                                    if (gameDetail?.hostChatWithLoggedInUser != null){
                                        put("chatId", gameDetail?.hostChatWithLoggedInUser)

                                    }
                                    else{
                                        put("type", 1)
                                        put("gameId", gameDetail?._id.toString())
                                        put("to", gameDetail!!.hostData?._id.toString())
                                    }


                                }

                                // Emit the joinRoom event with the prepared data
                                mSocket.emit("joinRoom", jsonData)
                                Log.i("SocketHandler", "Emitted joinRoom event: $jsonData")

                                // Listen for the response
                                mSocket.on("roomId") { args ->
                                    Log.i("SocketHandler", "Received joinRoom response: $args")

                                    if (args.isNotEmpty()) {
                                        try {
                                            val jsonString = args[0].toString()  // Convert object to String
                                            val jsonObject = JSONObject(jsonString) // Parse it as JSON
                                            Log.i("SocketHandler", "Parsed JSON: $jsonObject")

                                            // Access specific values if needed
                                            val roomId = jsonObject.optString("roomId", "Unknown")
                                            val isBlocked = jsonObject.optBoolean("isBlock",false)

                                            if (roomId != null){
                                                val intent = Intent(this, ChatActivity::class.java)
                                                intent.putExtra("data", gameDetail)
                                                intent.putExtra("roomId",roomId)
                                                intent.putExtra("isBlocked", isBlocked)
                                                intent.putExtra("side","single")
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                startActivity(intent)
                                            }
                                            Log.i("SocketHandler", "Extracted Room ID: $roomId")
                                        } catch (e: Exception) {
                                            Log.e("SocketHandler", "Error parsing JSON: ${e.message}")
                                        }
                                    }
                                }
                            }catch (e : Exception){
                                e.printStackTrace()
                            }
                    }
                }


                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }
                R.id.tvPlayer ->{
                    playerBottomSheet.show()
                }
            }
        })
    }

    override fun onViewClick(view: View?) {
        when(view?.id){

            R.id.SignUp ->{
                signUpBottomSheet.show()
                loginBottomSheet.dismiss()
            }
            R.id.forgotPassword ->{
                val intent = Intent(this , ForgotPasswordActivity:: class.java )
                startActivity(intent)
            }
            R.id.etCity ->{
                LocationActivity.activity = "GameDetail"
                val intent = Intent(this , LocationActivity:: class.java )
                startActivity(intent)
            }

            R.id.tvSignIn ->{
                if (loginValidation()){
                    val data = HashMap<String, Any>()
                    data["email"] = loginBottomSheet.binding.etEmail.text.toString().trim()
                    data["password"] = loginBottomSheet.binding.etPassword.text.toString().trim()
                    data["deviceType"] = 2
                    data["deviceToken"] = token
                    data["latitude"] = lat
                    data["longitude"] = long
                    data["city"] = city.toString()
                    viewModel.login(data, Constants.LOGIN)
                }
            }
            R.id.tvSignUp ->{
                if (signUpValidation()){
                    val data = HashMap<String,Any>()
                    data["firstName"] = signUpBottomSheet.binding.etName.text.toString().trim()
                    data["email"] = signUpBottomSheet.binding.etEmail.text.toString().trim()
                    data["password"] = signUpBottomSheet.binding.etPassword.text.toString().trim()
                    data["referralCode"] = signUpBottomSheet.binding.etReferralCode.text.toString().trim()
                    data["deviceType"] = 2
                    data["deviceToken"] = token
                    data["latitude"] = searchLat
                    data["longitude"] = searchLong
                    data["countryCode"] = countryCode.toString()
                    data["city"] = signUpBottomSheet.binding.etCity.text.toString().trim()
                    data["sportId"] = selectedSportsId
                    data["phone"] = signUpBottomSheet.binding.etPhoneNumber.text.toString().trim()
                    viewModel.signUp(data, Constants.SIGNUP)
                }
            }
            R.id.showPassword ->{
                showOrHidePassword()
            }
            R.id.signUpShowPassword ->{
                signUpShowHidePassword()
            }
            R.id.SignIn ->{
                loginBottomSheet.show()
                signUpBottomSheet.dismiss()
            }
            R.id.leaveGame ->{
                viewModel.leaveGame(Constants.LEAVE_GAMES+id)
            }
            R.id.cancel ->{
                leaveGameBottomSheet.dismiss()
            }
        }

        }

    private fun loginValidation ():Boolean {
        if (TextUtils.isEmpty(loginBottomSheet.binding.etEmail.text.toString().trim())){
            showToast("Please enter email")
            return false
        }
        if (TextUtils.isEmpty(loginBottomSheet.binding.etPassword.text.toString().trim())){
            showToast("Please enter password")
            return false
        }
        return true
    }

    private fun signUpValidation ():Boolean {
        if (TextUtils.isEmpty(signUpBottomSheet.binding.etName.text.toString().trim())){
            showToast("Please enter name")
            return false
        }
        if (TextUtils.isEmpty(signUpBottomSheet.binding.etEmail.text.toString().trim())){
            showToast("Please enter email")
            return false
        }
        if (TextUtils.isEmpty(signUpBottomSheet.binding.etPassword.text.toString().trim())){
            showToast("Please enter password")
            return false
        }
        if (TextUtils.isEmpty(signUpBottomSheet.binding.etPhoneNumber.text.toString().trim())){
            showToast("Please enter phone number")
            return false
        }
        if (TextUtils.isEmpty(signUpBottomSheet.binding.etCity.text.toString().trim())){
            showToast("Please select city")
            return false
        }
        if (TextUtils.isEmpty(signUpBottomSheet.binding.etSelectSport.text.toString().trim())){
            showToast("Please select sports")
            return false
        }
        return true
    }

    private fun showOrHidePassword() {
        if (loginBottomSheet.binding.etPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            loginBottomSheet.binding.showPassword.setImageResource(R.drawable.iv_eye)
            loginBottomSheet.binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            loginBottomSheet.binding.showPassword.setImageResource(R.drawable.iv_show_password)
            loginBottomSheet.binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        loginBottomSheet.binding.etPassword.setSelection(loginBottomSheet.binding.etPassword.length())
    }

    private fun signUpShowHidePassword() {
        if (signUpBottomSheet.binding.etPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            signUpBottomSheet.binding.signUpShowPassword.setImageResource(R.drawable.iv_eye)
            signUpBottomSheet.binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            signUpBottomSheet.binding.signUpShowPassword.setImageResource(R.drawable.iv_show_password)
            signUpBottomSheet.binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        signUpBottomSheet.binding.etPassword.setSelection(signUpBottomSheet.binding.etPassword.length())
    }

    override fun onResume() {
        if (locationAddress !=null){
            signUpBottomSheet.binding.etCity.setText(locationAddress)
        }
        super.onResume()
    }

    fun getCityFromLatLong(context: Context, latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                addresses[0].locality  // This gives the city name
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    }


