package com.example.smartattendanceapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartattendanceapp.databinding.ActivityAttendanceBinding
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class AttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAttendanceBinding

    private var photoTaken = false
    private var locationTaken = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🕒 WAKTU
        val time = SimpleDateFormat(
            "dd-MM-yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())
        binding.txtTime.text = "Waktu Absen: $time"

        // 📸 KAMERA
        binding.btnCamera.setOnClickListener {
            checkCameraPermission()
        }

        // 📍 LOKASI
        binding.btnLocation.setOnClickListener {
            checkLocationPermission()
        }

        // ✅ SELESAI
        binding.btnFinish.setOnClickListener {
            Toast.makeText(
                this,
                "Absensi berhasil (foto & lokasi valid)",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }

        // 🔙 KEMBALI
        binding.btnBack.setOnClickListener {
            finish()
        }

        updateFinishButton()
    }

    // ================= KAMERA =================

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                101
            )
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as Bitmap
                binding.imgPhoto.setImageBitmap(bitmap)

                photoTaken = true
                updateFinishButton()
            }
        }

    // ================= LOKASI =================

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                201
            )
        }
    }

    private fun getLocation() {
        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    binding.txtLocation.text =
                        "Lokasi: ${location.latitude}, ${location.longitude}"

                    locationTaken = true
                    updateFinishButton()
                } else {
                    Toast.makeText(
                        this,
                        "Lokasi belum tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    // ================= VALIDASI =================

    private fun updateFinishButton() {
        binding.btnFinish.isEnabled = photoTaken && locationTaken
    }
}
