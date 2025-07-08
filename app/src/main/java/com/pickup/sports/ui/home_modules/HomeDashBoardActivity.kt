package com.pickup.sports.ui.home_modules

import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.pickup.sports.R
import com.pickup.sports.data.api.Constants
import com.pickup.sports.data.model.GetProfileApiResponse
import com.pickup.sports.data.model.LoginApiResponse
import com.pickup.sports.databinding.ActivityHomeDashBoardBinding
import com.pickup.sports.databinding.BottomSheetLoginBinding
import com.pickup.sports.databinding.BottomSheetSignUpBinding
import com.pickup.sports.ui.auth.forgot_password.ForgotPasswordActivity
import com.pickup.sports.ui.auth.login_signup_module.MainViewModel
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.earnings_module.EarningFragment
import com.pickup.sports.ui.home_modules.home_fragment.HomeFragment
import com.pickup.sports.ui.home_modules.host_games_module.HostGameFragment
import com.pickup.sports.ui.home_modules.my_games.MyGamesFragment
import com.pickup.sports.ui.home_modules.refer_friend.ReferFriendFragment
import com.pickup.sports.utils.BaseCustomBottomSheet
import com.pickup.sports.utils.ImageUtils
import com.pickup.sports.utils.Status
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.pickup.sports.BR
import com.pickup.sports.data.api.SimpleApiResponse
import com.pickup.sports.data.model.GetAllSportsApiResponse
import com.pickup.sports.data.model.GetGameByIdApiResponse
import com.pickup.sports.data.model.GetMyGamesApiResponse
import com.pickup.sports.data.model.PushNotificationModel
import com.pickup.sports.databinding.BottomSheetRatingBinding
import com.pickup.sports.databinding.BottomsheetMultiSelectSportsBinding
import com.pickup.sports.databinding.BottomsheetPastRatingBinding
import com.pickup.sports.databinding.ItemLayoutMultiSeletSportsBinding
import com.pickup.sports.databinding.ItemLayoutSelectSportsBinding
import com.pickup.sports.ui.base.SimpleRecyclerViewAdapter
import com.pickup.sports.ui.home_modules.account_module.AccountActivity
import com.pickup.sports.ui.home_modules.chat_module.ChatActivity
import com.pickup.sports.ui.home_modules.location_module.LocationActivity
import com.pickup.sports.ui.home_modules.messages_module.MessageFragment
import com.pickup.sports.ui.home_modules.messages_module.game_group_messages.GroupMessageFragment
import com.pickup.sports.utils.Resource
import com.pickup.sports.utils.ReviewUtils
import com.pickup.sports.utils.event.SingleRequestEvent
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class HomeDashBoardActivity : BaseActivity<ActivityHomeDashBoardBinding>() ,BaseCustomBottomSheet.Listener {
    private val viewModel : MainViewModel by viewModels()
    private lateinit var loginBottomSheet : BaseCustomBottomSheet<BottomSheetLoginBinding>
    private lateinit var signUpBottomSheet : BaseCustomBottomSheet<BottomSheetSignUpBinding>
    private lateinit var ratingBottomSheet : BaseCustomBottomSheet<BottomSheetRatingBinding>
    private lateinit var ratingPastBottomSheet : BaseCustomBottomSheet<BottomsheetPastRatingBinding>
    private lateinit var selectSportsBottomSheet : BaseCustomBottomSheet<BottomsheetMultiSelectSportsBinding>
    private lateinit var sportsAdapter : SimpleRecyclerViewAdapter<GetAllSportsApiResponse.Sport, ItemLayoutMultiSeletSportsBinding>
    private var city : String ? = null


    private var countryCode: String? = "+1"
    private var gameId : String ? = null
    private var hostID : String ? = null
    var fragmentExit = false
    private var ratingList = 0
    private lateinit var headerView: View
    private  var name : TextView ? = null
    private var selectedSportNames: String = ""
    private var selectedSportsId: String = ""

    var token = "123"

    companion object{
        var lat = 0.0
        var long = 0.0
        var searchLat = 0.0
        var searchLong = 0.0

        var refresh = SingleRequestEvent<Boolean>()
        var loginObr = SingleRequestEvent<Boolean>()
        var allGamesRefresh = SingleRequestEvent<Boolean>()
        var locationAddress: String? = null
    }

    override fun getLayoutResource(): Int {
         return R.layout.activity_home_dash_board
    }

    override fun getViewModel(): BaseViewModel {
       return viewModel
    }

    override fun onCreateView() {
        initView()

        val bundle = intent.extras
        Log.i("dfddfd", "onCreateView: $bundle")
        initBottomSheet()


        countryCodePicker()

        if (bundle != null) {
            val data =bundle.getParcelable<PushNotificationModel>("notificationData")
            Log.i("dfdfd", "onCreateView: $data")
            when(data?.Type){
                "1" ->{

                }
                "2" ->{
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("chatDAta", data)
                    startActivity(intent)
                }
                "6" ->{
                    ratingBottomSheet.show()
                    gameId = data.gameId.toString()
                    if (gameId != null){
                        val gameData = HashMap<String,Any>()
                        gameData["gameId"]  = gameId!!
                        viewModel.singleGameDetail(gameData , Constants.SINGLE_GAME_DETAIL)
                    }


                }

            }

        }
        sideHeader()

        // Get the referral code from the deep link
        val data: Uri? = intent?.data
        val referralCode = data?.getQueryParameter("referralCode") // Extract referral code

        if (!referralCode.isNullOrEmpty()) {
             signUpBottomSheet.show()
             signUpBottomSheet.binding.etReferralCode.setText(referralCode)
        }

        handleSideNavigation()

        initOnClick()

        displayFragment(HomeFragment())


        city = getCityFromLatLong(this, lat, long)
        Log.i("dsadsad", "onCreateView: $city ")


        viewModel.getAllSports(Constants.GET_ALL_SPORTS)
        if (sharedPrefManager.getCurrentUser() != null){
            viewModel.getProfile(Constants.GET_PROFILE)
        }

        initAdapter()
        initObserver()
        ImageUtils.rating.observe(this){
            when(it?.status){
                Status.LOADING ->{

                }Status.SUCCESS ->{
                ratingBottomSheet(it.data)


                }Status.ERROR ->{

                }
                else ->{

                }
            }
        }
        loginObr.observe(this){
            Log.i("fdfdsfs", "onResume:${loginObr} ")
            when(it?.status){
                Status.LOADING ->{

                }Status.SUCCESS ->{
                Log.i("fdfdsfs", "onResume:${loginObr} ")
                loginBottomSheet.show()

            }Status.ERROR ->{

            }
                else ->{

                }
            }
        }

        // Increment the launch count
        sharedPrefManager.incrementLaunchCount()

        // Check the launch count and request review if necessary
        val launchCount = sharedPrefManager.getLaunchCount()
        Log.i("shared", "onCreateView: $launchCount")

        if (launchCount == 2 || launchCount == 4) {
            Log.i("Review", "Requesting in-app review...")
            ReviewUtils.launchInAppReview(this)
        }else {
            Log.i("Review", "Launch count not matching for review.")
        }
    }

    private fun initAdapter() {
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

    private fun countryCodePicker() {
        signUpBottomSheet.binding.countyCodePicker.setDefaultCountryUsingPhoneCode(1)
        signUpBottomSheet.binding.countyCodePicker.resetToDefaultCountry()

        signUpBottomSheet.binding.countyCodePicker.setOnCountryChangeListener {
            countryCode =   signUpBottomSheet.binding.countyCodePicker.selectedCountryCodeWithPlus ?: "+1"
            Log.i("dsdsad", "countryCodePicker: $countryCode")

        }
    }

    private var ratingList1 = 0

    fun ratingBottomSheet( game :
    GetMyGamesApiResponse.Game?){

        ratingPastBottomSheet  = BaseCustomBottomSheet( this, R.layout.bottomsheet_past_rating){
            when(it.id){

                R.id.star1 ->{
                    ratingList1 = 1
                    ratingPastBottomSheet.binding.apply {
                     star1.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.black))
                     star2.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))
                     star3.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))
                     star4.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))
                     star5.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))
                    }


                }
                R.id.star2 ->{
                    ratingList1 = 2
                    ratingPastBottomSheet.binding.apply {
                       star1.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.black))
                       star2.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.black))
                       star3.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))

                       star4.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))

                       star5.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))
                    }



                }
                R.id.star3->{
                    ratingList1 = 3
                    ratingPastBottomSheet.binding.apply {
                        star3.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star1.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star2.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star4.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.slate_300
                            )
                        )

                        star5.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.slate_300
                            )
                        )
                    }
                }
                R.id.star4->{
                    ratingList1 = 4
                    ratingPastBottomSheet.binding.apply {
                        star3.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star1.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star2.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star4.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star5.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.slate_300
                            )
                        )
                    }
                }
                R.id.star5->{
                    ratingList1 = 5
                    ratingPastBottomSheet.binding.apply {
                        star3.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star1.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star2.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star4.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                        star5.setColorFilter(
                            ContextCompat.getColor(
                                this@HomeDashBoardActivity,
                                R.color.black
                            )
                        )
                    }
                }

                R.id.tvSubmit ->{
                    if (game?._id != null && game.hostData?._id != null){
                        val data = HashMap<String,Any>()
                        data["gameId"] = game._id!!
                        data["ratings"] = ratingList1
                        data["hostId"] = game.hostData?._id!!

                        viewModel.rating(data, Constants.GIVE_RATING)
                        ratingPastBottomSheet.dismiss()
                    }

                }
            }
        }

        ratingPastBottomSheet.show()
        ratingPastBottomSheet.binding. bean = game
    }


    private fun initBottomSheet() {
        loginBottomSheet = BaseCustomBottomSheet(this,R.layout.bottom_sheet_login,this)
        signUpBottomSheet = BaseCustomBottomSheet(this , R.layout.bottom_sheet_sign_up,this)
        ratingBottomSheet  = BaseCustomBottomSheet(this , R.layout.bottom_sheet_rating, this)
        selectSportsBottomSheet = BaseCustomBottomSheet(this , R.layout.bottomsheet_multi_select_sports,this)
        ratingBottomSheet.setCancelable(false)

        signUpBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        signUpBottomSheet.behavior.isDraggable = true

        loginBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        loginBottomSheet.behavior.isDraggable = true


        ratingBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        ratingBottomSheet.behavior.isDraggable = true

        selectSportsBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        selectSportsBottomSheet.behavior.isDraggable = true

    }

    private fun sideHeader() {
        val navigationView = findViewById<NavigationView>(R.id.navView)
        headerView = navigationView.getHeaderView(0)

        name = headerView.findViewById(R.id.profileName)

        if (name == null) {
            Log.e("sideHeader", "TextView `name` is null, not initialized")
        } else {
            Log.i("sideHeader", "TextView `name` is initialized successfully")
            name?.text = "Welcome"
        }

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
    private fun initObserver() {
        viewModel.obrCommon.observe(this, Observer {
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
                                if (myDataModel?.user != null) {

                                    val firstName = myDataModel.user?.firstName ?: ""
                                    val lastName = myDataModel.user?.lastName ?: "" // Default to empty string if null

                                    Log.i("fdsfds", "initObserver: $firstName , $lastName")

                                    name?.let { textView ->  // Only update text if `name` is not null
                                        textView.text = if (lastName.isNotEmpty()) "$firstName $lastName" else firstName
                                    } ?: Log.e("initObserver", "TextView `name` is null")
                                } else {
                                    Log.e("initObserver", "myDataModel.user is null")
                                }

                            }
                        }
                        "login" ->{
                            val myDataModel : LoginApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast("Login Successfully")
                                loginBottomSheet.dismiss()
                                sharedPrefManager.saveUser(myDataModel)
                                viewModel.getProfile(Constants.GET_PROFILE)
                                allGamesRefresh.postValue(Resource.success("refreshGames", true))


                            }
                        }
                        "signUp" ->{
                            val myDataModel : LoginApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                showToast("Register Successfully")
                                signUpBottomSheet.dismiss()
                                sharedPrefManager.saveUser(myDataModel)
                                viewModel.getProfile(Constants.GET_PROFILE)
                                allGamesRefresh.postValue(Resource.success("refreshGames", true))
                            }
                        }
                        "singleGameDetail" ->{
                            val myDataModel: GetGameByIdApiResponse? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                if (myDataModel.game != null){
                                    try {
                                        hostID = myDataModel?.game?.hostData?._id
                                        ratingBottomSheet.binding.bean = myDataModel.game
                                       // ratingBottomSheet.show()

                                    }catch (e : Exception){
                                        e.printStackTrace()
                                    }
                                }
                            }


                        }
                        "rating" ->{
                            val myDataModel : SimpleApiResponse ? = ImageUtils.parseJson(it.data.toString())
                            if (myDataModel != null){
                                ratingBottomSheet.dismiss()
                                refresh.postValue(Resource.success("refresh",true))
                                showToast("Rating Added Successfully")
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

    private fun initOnClick() {
        viewModel.onClick.observe(this, Observer {
            when(it?.id){
                R.id.actionToggleBtn ->{
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
                R.id.ivProfile ->{
                    if (sharedPrefManager.getCurrentUser() != null) {
                        val intent =
                            Intent(this, AccountActivity::class.java)
                        startActivity(intent)
                    } else {
                        loginBottomSheet.show()
                    }
                }
            }
        })
    }

    private fun handleSideNavigation() {
        // Handle Navigation Item Clicks
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
//                R.id.pickUpSports -> {
//                    fragmentExit = false
//                    clearBackStack()
//                      displayFragment(HomeFragment())
//                }
                R.id.myGames -> {
                    binding.ivProfile.visibility = View.GONE
                    fragmentExit = true
                    if (sharedPrefManager.getCurrentUser() != null){
                        // Handle Profile click
                        clearBackStack()
                        displayFragment(MyGamesFragment())
                    }
                    else{
                        loginBottomSheet.show()
                    }

                }

                R.id.hostGames -> {
                    // Handle Settings click

                    fragmentExit = true
                    if (sharedPrefManager.getCurrentUser() != null){
                        clearBackStack()
                        displayFragment(HostGameFragment())
                        binding.ivProfile.visibility = View.GONE
                    }
                    else{
                        loginBottomSheet.show()
                    }

                }
                R.id.referFriend -> {
                    fragmentExit = true
                    if (sharedPrefManager.getCurrentUser() != null) {
                        clearBackStack()
                        displayFragment(ReferFriendFragment())
                        binding.ivProfile.visibility = View.GONE
                    }else{
                        loginBottomSheet.show()
                    }
                }
                R.id.earnings -> {

                    fragmentExit = true

                    if (sharedPrefManager.getCurrentUser() != null){

                        clearBackStack()
                        displayFragment(EarningFragment())
                        binding.ivProfile.visibility = View.GONE
                    }
                    else{
                        loginBottomSheet.show()
                    }
                }

                R.id.messages -> {
                    fragmentExit = true
                    if (sharedPrefManager.getCurrentUser() != null) {
                        clearBackStack()
                        displayFragment(MessageFragment())
                        binding.ivProfile.visibility = View.GONE
                    }else{
                        loginBottomSheet.show()
                    }
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }


    // display fragment
    private fun displayFragment(fragment: Fragment) {

            val transaction = supportFragmentManager.beginTransaction()
            transaction.setReorderingAllowed(true)
            transaction.replace(R.id.frameLayout, fragment)
            transaction.addToBackStack(null)
            transaction.commit()

    }
    private fun clearBackStack() {
        val fragmentManager = supportFragmentManager
        val fragmentCount = fragmentManager.backStackEntryCount
        for (i in 0 until fragmentCount) {
            fragmentManager.popBackStack()
        }
    }




    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
            if (fragmentExit){
                displayFragment(HomeFragment())
                binding.ivProfile.visibility = View.VISIBLE
                fragmentExit = false
            }
            else{
                super.finish()
            }
    }



    override fun onViewClick(view: View?) {
        when(view?.id){
            R.id.SignUp ->{
                signUpBottomSheet.show()
                loginBottomSheet.dismiss()
            }
            R.id.forgotPassword ->{
                val intent = Intent(this ,ForgotPasswordActivity:: class.java )
                startActivity(intent)
            }
            R.id.etSelectSport ->{
                selectSportsBottomSheet.show()
            }
            R.id.etCity ->{
                LocationActivity.activity = "Home"
                val intent = Intent(this ,LocationActivity:: class.java )
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
                   data["deviceType"] = 2
                   data["deviceToken"] = token
                   data["latitude"] = searchLat
                   data["longitude"] = searchLong
                   data["referralCode"] = signUpBottomSheet.binding.etReferralCode.text.toString().trim()
                   data["countryCode"] = countryCode.toString()
                   data["city"] = signUpBottomSheet.binding.etCity.text.toString().trim()
                   data["sportId"] = selectedSportsId
                   data["phone"] = signUpBottomSheet.binding.etPhoneNumber.text.toString().trim()
                   viewModel.signUp(data,Constants.SIGNUP)
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

            R.id.tvEndUserLicence -> {
                val url = "http://18.235.152.238/terms-conditions/"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Ensures a new browser task is started
                intent.setPackage(null) // Clears any default app selection
                startActivity(intent)
            }

            R.id.star1 ->{
                ratingList = 1
                ratingBottomSheet.binding.apply {
                    star1.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.black))
                    star2.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))
                    star3.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))
                    star4.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))
                    star5.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))
                }


            }
            R.id.star2 ->{
                ratingList = 2
                ratingBottomSheet.binding.apply {
                    star1.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.black))
                    star2.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.black))
                    star3.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))

                    star4.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))

                    star5.setColorFilter(ContextCompat.getColor(this@HomeDashBoardActivity,R.color.slate_300))
                }



            }
            R.id.star3->{
                ratingList = 3
                ratingBottomSheet.binding.apply {
                    star3.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star1.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star2.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star4.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.slate_300
                        )
                    )

                    star5.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.slate_300
                        )
                    )
                }
            }
            R.id.star4->{
                ratingList = 4
                ratingBottomSheet.binding.apply {
                    star3.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star1.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star2.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star4.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star5.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.slate_300
                        )
                    )
                }
            }
            R.id.star5->{
                ratingList = 5
                ratingBottomSheet.binding.apply {
                    star3.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star1.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star2.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star4.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                    star5.setColorFilter(
                        ContextCompat.getColor(
                            this@HomeDashBoardActivity,
                            R.color.black
                        )
                    )
                }
            }

            R.id.tvSubmit ->{
                val data = HashMap<String,Any>()
                data["gameId"] = gameId.toString()
                data["ratings"] = ratingList
                data["hostId"] = hostID.toString()

                viewModel.rating(data,Constants.GIVE_RATING)
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
        if (sharedPrefManager.getCurrentUser() != null){
            viewModel.getProfile(Constants.GET_PROFILE)
        }
        if (locationAddress != null){
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
