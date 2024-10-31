package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar

class add_Note2 : AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var nickname: EditText
    private lateinit var email: EditText
    private lateinit var address: EditText
    private lateinit var birthDate: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var saveButton: FloatingActionButton
    private lateinit var photo: ImageView

    private val REQUEST_GALLERY = 100
    private val REQUEST_CAMERA = 101
    private val STORAGE_PERMISSION = 102
    private val CAMERA_REQUEST = 103
    private var photoUri: Uri? = null
    private val REQUEST_IMAGE_PICK = 1
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book2)

        // Initialize views with IDs from XML layout
        name = findViewById(R.id.addNama)
        nickname = findViewById(R.id.addNP)
        email = findViewById(R.id.addEmail)
        address = findViewById(R.id.addAlamat)
        birthDate = findViewById(R.id.addTglLahir)
        phoneNumber = findViewById(R.id.addHP)
        saveButton = findViewById(R.id.doneButton)
        photo = findViewById(R.id.addPhoto)

        // Initialize database helper
        databaseHelper = DatabaseHelper(this)

        // Set up Date Picker for birth date
        birthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    birthDate.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // Set up photo selection
        photo.setOnClickListener {
            val options = arrayOf("Ambil Foto", "Pilih dari Galeri")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pilih sumber foto")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            builder.show()
        }

        // Save button with confirmation dialog
        saveButton.setOnClickListener {
            val nameInput = name.text.toString()
            val nicknameInput = nickname.text.toString()
            val emailInput = email.text.toString()
            val addressInput = address.text.toString()
            val birthDateInput = birthDate.text.toString()
            val phoneInput = phoneNumber.text.toString()

            if (nameInput.isNotEmpty() && nicknameInput.isNotEmpty() && emailInput.isNotEmpty() &&
                birthDateInput.isNotEmpty() && phoneInput.isNotEmpty()
            ) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Confirmation")
                builder.setMessage("Are you sure you want to save this data?")
                builder.setPositiveButton("Yes") { _, _ ->
                    // Save data to database
                    databaseHelper.addNote(
                        nameInput,
                        nicknameInput,
                        emailInput,
                        addressInput,
                        birthDateInput,
                        phoneInput,
                        photoUri?.toString()
                    )

                    // Navigate back to the main page
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            } else {
                Toast.makeText(this, "Please fill in all required fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        if (checkStoragePermission()) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_GALLERY)
        } else {
            requestStoragePermission()
        }
    }

    private fun openCamera() {
        if (checkCameraPermission()) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_CAMERA)
        } else {
            requestCameraPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_GALLERY -> {
                    val selectedImage: Uri? = data?.data
                    if (selectedImage != null) {
                        photo.setImageURI(selectedImage)
                        photoUri = selectedImage
                    } else {
                        Toast.makeText(this, "Gambar tidak valid", Toast.LENGTH_SHORT).show()
                    }
                }

                REQUEST_CAMERA -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    if (imageBitmap != null) {
                        photo.setImageBitmap(imageBitmap)

                        // Simpan gambar ke galeri dan dapatkan URI
                        val imageUri = saveImageToGallery(imageBitmap)
                        photoUri = imageUri
                    } else {
                        Toast.makeText(this, "Gambar tidak valid", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val savedImageURL: String = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Captured Image",
            "Image from camera"
        )
        return Uri.parse(savedImageURL)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION
        )
    }

    private fun checkStoragePermission(): Boolean {
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return readPermission && writePermission
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            CAMERA_REQUEST
        )
    }

    private fun checkCameraPermission(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return cameraPermission && writePermission
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Enable Camera and Storage Permissions", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Enable Storage Permission", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}