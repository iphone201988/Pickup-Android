package com.pickup.sports.ui.home_modules.location_module

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.pickup.sports.R
import com.pickup.sports.data.model.PlaceDetails
import com.pickup.sports.databinding.ActivityLocationBinding
import com.pickup.sports.ui.base.BaseActivity
import com.pickup.sports.ui.base.BaseViewModel
import com.pickup.sports.ui.home_modules.create_host_game.PlaceAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.SphericalUtil
import com.pickup.sports.ui.home_modules.HomeDashBoardActivity
import com.pickup.sports.ui.home_modules.edit_profile.EditProfileActivity
import com.pickup.sports.ui.home_modules.game_details.GameDetailsActivity
import com.pickup.sports.ui.home_modules.home_fragment.HomeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationActivity : BaseActivity<ActivityLocationBinding>() ,OnMapReadyCallback {
    private val viewModel : LocationActivityVm by viewModels()
    private lateinit var mMap : GoogleMap
    private lateinit var apiKey: String
    private lateinit var placesClient: PlacesClient
    private lateinit var autocompleteSessionToken: AutocompleteSessionToken
    private lateinit var placeAdapter: PlaceAdapter
    private var location: LatLng? = null
    private var address : String ? = null
    private val places = ArrayList<PlaceDetails>()
    private var radiusCircle: Circle? = null
    private var centerMarker : Marker? = null
    private var radiusMeters : Double ? = null
    private var selectedLocation  = false

    companion object{
        var lat = 0.0
        var long = 0.0
        var  activity : String ? = null
    }
    override fun getLayoutResource(): Int {
      return R.layout.activity_location
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        apiKey = getString(R.string.api_key)
        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey)
        }
        placesClient = Places.createClient(this)
        autocompleteSessionToken = AutocompleteSessionToken.newInstance()
//        val data = HashMap<String, Any>()
//        data["sportId"] =""
//        viewModel.getGames(data, Constants.GET_ALL_GAMES)
        Log.i("dsadsa", "onCreateView: $lat, $long")
        locationAdapter()
        searchLocation()
         initOnClick()
        setupSeekBar()
    }

//    private fun seekbar() {
//        // Set a listener to update the text as the SeekBar's progress changes
//        binding.seekbar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                // Update the TextView with the progress
//                binding.miles.text = "$progress mi"
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                // Optional: Handle event when user starts touching the SeekBar
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                // Optional: Handle event when user stops touching the SeekBar
//            }
//        })
//    }
private fun setupSeekBar() {
    binding.seekbar1.max = 100
    binding.seekbar1.progress = 10 // Default radius 10 miles
    binding.miles.text = "10 mi"

    binding.seekbar1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            val radius = if (progress < 1) 1 else progress // Minimum 1 mile

            if (selectedLocation){
                binding.miles.text = "$radius mi"
                drawCircle(LatLng(location!!.latitude, location!!.longitude), radius)
                moveCameraToCircle(LatLng(location!!.latitude, location!!.longitude),radius)
            }
            else{
                binding.miles.text = "$radius mi"
                drawCircle(LatLng(lat, long), radius)
                moveCameraToCircle(LatLng(lat, long),radius)
            }

        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })
}


    private fun initOnClick() {
        viewModel.onClick.observe(this, Observer {
            when(it?.id) {
                R.id.ivBack ->{
                    onBackPressedDispatcher.onBackPressed()
                }
                R.id.tvSaveChanges ->{
                    when(activity){
                        "Home" ->{
                            HomeDashBoardActivity.locationAddress = address ?: ""
                            HomeDashBoardActivity.searchLat= location?.latitude ?: lat
                            HomeDashBoardActivity.searchLong = location?.longitude ?: long
                            onBackPressedDispatcher.onBackPressed()
                        }
                        "GameDetail" ->{
                            GameDetailsActivity.locationAddress = address ?: ""
                            GameDetailsActivity.searchLat= location?.latitude ?: lat
                            GameDetailsActivity.searchLong = location?.longitude ?: long
                            onBackPressedDispatcher.onBackPressed()
                        }
                        "EditProfile" ->{
                            EditProfileActivity.locationAddress = address ?: ""
                            EditProfileActivity.searchLat= location?.latitude ?: lat
                            EditProfileActivity.searchLong = location?.longitude ?: long
                            onBackPressedDispatcher.onBackPressed()
                        }
                        else ->{
                            HomeFragment.locationAddress = address ?: ""
                            HomeFragment.searchLat= location?.latitude ?: lat
                            HomeFragment.searchLong = location?.longitude ?: long
                            // Convert radius from meters to miles
                            val radiusMiles = (radiusMeters ?: 0.0) / 1609.34
                            HomeFragment.radius = String.format("%.2f", radiusMiles) // Rounded to 2 decimal places
                            HomeFragment.side = "selectLocation"
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }

                }
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        moveCameraToCurrentLocation()
        drawCircle(LatLng(lat, long),10)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap.isMyLocationEnabled = false
    }

    private fun moveCameraToCurrentLocation() {
        val location = LatLng(lat, long)
        if (location != null){
            moveCameraToCircle(location,10)
//            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 17f)
//            mMap.animateCamera(cameraUpdate)
        }

    }

    private fun drawCircle(center: LatLng, radiusMiles: Int) {
        radiusCircle?.remove() // Remove previous circle if it exists
        centerMarker?.remove()

         radiusMeters = radiusMiles * 1609.34 // Convert miles to meters

        val circleOptions = CircleOptions()
            .center(center)
            .radius(radiusMeters!!)
            .strokeWidth(2f)
            .strokeColor(ContextCompat.getColor(this, R.color.heading_color))
            .fillColor(0x300084d3) // Light Blue Transparent

        radiusCircle = mMap.addCircle(circleOptions)

        val markerOptions = MarkerOptions()
            .position(center)
            .icon(bitmapDescriptorFromVector(R.drawable.iv_blue_dot)) // Your custom image

        centerMarker = mMap.addMarker(markerOptions)
    }

    // Function to convert a vector drawable to BitmapDescriptor
    private fun bitmapDescriptorFromVector(@DrawableRes vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(this, vectorResId) ?: return BitmapDescriptorFactory.defaultMarker()

        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)

        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
//    private fun moveCameraToCircle(location: LatLng, radiusMiles: Int) {
//        val radiusMeters = radiusMiles * 1609.34 // Convert miles to meters
//
//        val bounds = LatLngBounds.builder()
//            .include(SphericalUtil.computeOffset(location, radiusMeters, 0.0))  // North
//            .include(SphericalUtil.computeOffset(location, radiusMeters, 90.0)) // East
//            .include(SphericalUtil.computeOffset(location, radiusMeters, 180.0)) // South
//            .include(SphericalUtil.computeOffset(location, radiusMeters, 270.0)) // West
//            .build()
//
//        // Ensure the map is fully loaded before moving the camera
//        mMap.setOnMapLoadedCallback {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)) // Adjust padding as needed
//        }
//    }


    private fun moveCameraToCircle(location: LatLng, radiusMiles: Int) {
        val radiusMeters = radiusMiles * 1609.34 // Convert miles to meters

        val bounds = LatLngBounds.builder()
            .include(SphericalUtil.computeOffset(location, radiusMeters, 0.0))  // North
            .include(SphericalUtil.computeOffset(location, radiusMeters, 90.0)) // East
            .include(SphericalUtil.computeOffset(location, radiusMeters, 180.0)) // South
            .include(SphericalUtil.computeOffset(location, radiusMeters, 270.0)) // West
            .build()

        mMap.setOnMapLoadedCallback {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)) // Ensure padding
        }
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

        binding.searchLocation.setOnFocusChangeListener { v, hasFocus ->
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

        binding.searchLocation.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    getPlacePredictions(s.toString())
                    binding.locationCard.visibility = View.VISIBLE
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

        binding.searchLocation.setText(combinedAddress)
        location = places.location
        address = combinedAddress

        Log.i("dasd", "setPlaceToEditText: ${places.address} ")

        val latLng = LatLng(places.location.latitude, places.location.longitude)

        // Draw the circle at the selected location
        drawCircle(latLng, 10)

        // Move camera to keep the selected location in the center and fit the circle
        moveCameraToCircle(latLng, 10)

        selectedLocation = true

        binding.locationCard.visibility = View.GONE
    }


}