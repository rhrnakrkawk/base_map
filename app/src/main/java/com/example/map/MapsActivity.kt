package com.example.map

import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.map.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    val PERM_FLAG = 99
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)

        if(isPermitted()){
            startProcess()
        } else{
            ActivityCompat.requestPermissions(this, permissions, PERM_FLAG)
        }


        //binding = ActivityMapsBinding.inflate(layoutInflater)
        //setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    fun isPermitted() : Boolean{
        for(perm in permissions){
            if(ContextCompat.checkSelfPermission(this, perm) != PERMISSION_GRANTED)
                return false
        }
        return true
    }

    fun startProcess(){
        val mapFragment = supportFragmentManager // 맵 프래그먼트 관리 변수 설정
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this) // 맵 등록 -> 띄우기
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setUpdateLocationListener()
    }
    lateinit var fusedLocationClient: FusedLocationProviderClient // 배터리 소모 좋아지고 정확한 좌표값을 가져옴
    lateinit var locationCallback: LocationCallback // 좌표 응답을 받아 처리

    @SuppressLint("MissingPermission") // 문법검사기
    fun setUpdateLocationListener(){
        val locationReqeust = LocationRequest.create()
        locationReqeust.run{
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // 정확도 업, 배터리 소모 업
            interval = 1000 // 1초에 한 번
        }

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let{
                    // 널이 아닌것은 it로 사용가능
                    for((i, location) in it.locations.withIndex()){
                    // 하나의 쌍으로 사용 가능 인덱스와 위치를 하나의 쌍으로
                        Log.d("로케이션", "$i ${location.latitude}, ${location.longitude}")
                        setLastLoction(location)
                    }
                }

            }
        }
        // 로케이션 요청 함수 호출(locationRequest, locatiobCallback을 담아 보낸다)
        fusedLocationClient.requestLocationUpdates(locationReqeust, locationCallback, Looper.myLooper())

    }
    fun setLastLoction(location : Location){
        val myLocation = LatLng(location.latitude, location.longitude)
        val marker = MarkerOptions()
            .position(myLocation)
            .title("I am here")
        val cameraOption = CameraPosition.Builder()
            .target(myLocation)
            .zoom(15.0f)
            .build()
        val camera = CameraUpdateFactory.newCameraPosition(cameraOption)
        mMap.clear()
        mMap.addMarker(marker)
        mMap.moveCamera(camera) // 카메라 위치 이동하는 함수
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERM_FLAG ->{
                var check = true
                for(grant in grantResults){
                    if(grant != PERMISSION_GRANTED){
                        check = false
                        break
                    }
                }
                if(check){
                    startProcess()
                } else{
                    Toast.makeText(this, "권한을 승인해야지만 앱을 사용할 수 있습니다.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }
}