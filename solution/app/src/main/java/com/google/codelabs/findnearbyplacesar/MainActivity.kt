// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelabs.findnearbyplacesar

import android.app.ActivityManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.birjuvachhani.locus.Locus
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.codelabs.findnearbyplacesar.api.PlacesService
import com.google.codelabs.findnearbyplacesar.ar.PlaceNode
import com.google.codelabs.findnearbyplacesar.ar.PlacesArFragment
import com.google.codelabs.findnearbyplacesar.model.Geometry
import com.google.codelabs.findnearbyplacesar.model.GeometryLocation
import com.google.codelabs.findnearbyplacesar.model.NearByListResponse
import com.google.codelabs.findnearbyplacesar.model.Place
import com.google.codelabs.findnearbyplacesar.model.getPositionVector
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity(), SensorEventListener {

    private val TAG = "MainActivity"

    private lateinit var placesService: PlacesService
    private lateinit var arFragment: PlacesArFragment
//    private lateinit var mapFragment: SupportMapFragment

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Sensor
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var anchorNode: AnchorNode? = null
    private var markers: MutableList<Marker> = emptyList<Marker>().toMutableList()
    private var places: List<Place>? = null
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isSupportedDevice()) {
            return
        }
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as PlacesArFragment

        sensorManager = getSystemService()!!
        placesService = PlacesService.getMyBase(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        setUpAr()
        setUpMaps()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

//    private fun setUpAr() {
//        val test = arFragment.arSceneView.arFrame?.hitTest(
//            arFragment.arSceneView.width / 2f,
//            arFragment.arSceneView.height / 2f
//        )
//        if (test?.isNotEmpty() == true) {
//            val hitResult = test.first()
//            val anchor = hitResult.createAnchor()
//            val anchorNode = AnchorNode(anchor)
//            anchorNode.setParent(arFragment.arSceneView.scene)
//            addPlaces(anchorNode)
//        }
//        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
//            // Create anchor
//            val anchor = hitResult.createAnchor()
//            anchorNode = AnchorNode(anchor)
//            anchorNode?.setParent(arFragment.arSceneView.scene)
//            addPlaces(anchorNode!!)
//        }
//    }

    private fun addPlaces(anchorNode: AnchorNode) {
        val currentLocation = currentLocation
        if (currentLocation == null) {
            Log.w(TAG, "Location has not been determined yet")
            return
        }

        val places = places
        if (places == null) {
            Log.w(TAG, "No places to put")
            return
        }

        for (place in places) {
            // Add the place in AR
            val placeNode = PlaceNode(this, place)
            placeNode.setParent(anchorNode)
            placeNode.localPosition =
                place.getPositionVector(orientationAngles[0], currentLocation.latLng)
            placeNode.updateMarkerOrientation(anchorPose = anchorNode.anchor?.pose)
            placeNode.setOnTapListener { _, _ ->
                showInfoWindow(place)
            }
        }
    }

    private fun showInfoWindow(place: Place) {
        // Show in AR
        val matchingPlaceNode = anchorNode?.children?.filter {
            it is PlaceNode
        }?.first {
            val otherPlace = (it as PlaceNode).place ?: return@first false
            return@first otherPlace == place
        } as? PlaceNode
        matchingPlaceNode?.showInfoWindow()

        // Show as marker
        val matchingMarker = markers.firstOrNull {
            val placeTag = (it.tag as? Place) ?: return@firstOrNull false
            return@firstOrNull placeTag == place
        }
        matchingMarker?.showInfoWindow()
    }


    private fun setUpMaps() {
        getCurrentLocation {
            getNearbyPlaces(it)
        }
    }

    private fun getCurrentLocation(onSuccess: (Location) -> Unit) {
        Locus.getCurrentLocation(this) {
            currentLocation = it.location
            onSuccess(it.location!!)
        }
    }

    private fun getNearbyPlaces(location: Location) {
//        val apiKey = this.getString(R.string.google_maps_key)
        val ACCESS_TOKEN = "rkaIJn2ay03j7IOvrVELu9OKb4G0RtmC2RRB2cXCKnMyahtjZwwJhsmCLHt9sRqp"
        placesService.getNearByPlaces(
            accessToken = ACCESS_TOKEN,
            type = "bank",
            latitude = location.latitude,
            longitude = location.longitude
        ).enqueue(
            object : Callback<NearByListResponse> {
                override fun onResponse(
                    call: Call<NearByListResponse>,
                    response: Response<NearByListResponse>
                ) {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Failed to get nearby places")
                        return
                    }

                    val locationList = response.body()?.locationList ?: emptyList()
                    val places = mutableListOf<Place>()
                    locationList.forEachIndexed { index, nearPlacePlace ->
                        places.add(
                            Place(
                                id = "$index",
                                icon = nearPlacePlace.photos?.getOrNull(0)?.image ?: "",
                                name = nearPlacePlace.name,
                                geometry = Geometry(
                                    location = GeometryLocation(
                                        lat = nearPlacePlace.geometry.lat ?: 0.0,
                                        lng = nearPlacePlace.geometry.lng ?: 0.0
                                    )
                                )
                            )
                        )
                    }
                    this@MainActivity.places = places
                    addData(location)
                }

                override fun onFailure(call: Call<NearByListResponse>, t: Throwable) {
                    Log.e(TAG, "Failed to get nearby places", t)
                }
            }
        )
    }

    private fun addData(location: Location) {
        val latitude = location.latitude.toFloat()
        val longitude = location.longitude.toFloat()
        val altitude = location.altitude.toFloat()

        val desiredPose = Pose.makeTranslation(longitude, altitude, -latitude)
        val anchor = arFragment.arSceneView.session?.createAnchor(desiredPose)

        anchorNode = AnchorNode(anchor)
        anchorNode?.setParent(arFragment.arSceneView.scene)
        addPlaces(anchorNode!!)
    }


    private fun isSupportedDevice(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val openGlVersionString = activityManager.deviceConfigurationInfo.glEsVersion
        if (openGlVersionString.toDouble() < 3.0) {
            Toast.makeText(this, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            finish()
            return false
        }
        return true
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)
    }
}

val Location.latLng: LatLng
    get() = LatLng(this.latitude, this.longitude)

