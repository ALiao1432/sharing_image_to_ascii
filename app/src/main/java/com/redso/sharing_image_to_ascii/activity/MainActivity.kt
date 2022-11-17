package com.redso.sharing_image_to_ascii.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import com.redso.sharing_image_to_ascii.databinding.ActivityMainBinding
import com.redso.sharing_image_to_ascii.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class Resolution(val size: Size) {
    CUSTOM(Size(3, 4)),
    P_360(Size(480, 360)),
    HD(Size(1920, 1080)),
    QHD(Size(2560, 1440)),
    FOUR_K(Size(3840, 2160)),
}

class MainActivity : AppCompatActivity() {
    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.CAMERA
    ).toTypedArray()
    private val REQUEST_CODE_PERMISSIONS = 10
    private val targetResolution = Resolution.CUSTOM.size
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isAllPermissionGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            viewModel = ViewModelProvider(this@MainActivity)[MainViewModel::class.java].apply {
                handleShotButtonViewClicked = {
                    takePhoto()
                }
            }
            setContentView(root)
        }
        initViews()
    }

    private fun initViews() {
        binding.apply {
            shotButtonView.setOnClickListener { viewModel?.handleShotButtonViewClicked?.invoke() }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (isAllPermissionGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun isAllPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        if (cameraProvider == null) {
            return
        }
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(binding.previewView.surfaceProvider)
        }
        imageCapture = ImageCapture.Builder()
            .setTargetResolution(targetResolution)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
            .build()

        // Unbind use cases before rebinding
        cameraProvider.unbindAll()

        // Bind use cases to camera
        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
    }

    private fun takePhoto() {
        imageCapture.apply {
            takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = binding.viewModel?.getProcessBitmap(image) ?: return
                    val luminanceArray = binding.viewModel?.getLuminanceArray(bitmap) ?: return

                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.resultImageView.setImageBitmap(bitmap)
                        appendLuminanceToTextView(luminanceArray)
                    }
                    image.close()
                }
            })
        }
    }

    private fun appendLuminanceToTextView(luminanceArray: Array<IntArray>) {
        var resultText = ""
        luminanceArray.forEach { row ->
            val asciiArray = row.map { binding.viewModel?.getAsciiTextByLuminance(it) ?: " " }
                .reduce { acc, s -> "$acc$s " }
            resultText += asciiArray + "\n"
        }
        binding.resultTextView.text = resultText
    }
}