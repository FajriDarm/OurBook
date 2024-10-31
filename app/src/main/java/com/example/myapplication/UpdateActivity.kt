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

class UpdateActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editNickname: EditText
    private lateinit var editEmail: EditText
    private lateinit var editAddress: EditText
    private lateinit var editBirthDate: EditText
    private lateinit var editPhone: EditText
    private lateinit var saveButton: FloatingActionButton
    private lateinit var photo: ImageView
    private lateinit var notesDatabaseHelper: DatabaseHelper
    private var noteId: Int = -1
    private var photoUri: Uri? = null

    companion object {
        private const val REQUEST_GALLERY = 1
        private const val REQUEST_CAMERA = 2
        private const val STORAGE_PERMISSION = 3
        private const val CAMERA_PERMISSION = 4
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.book_update)

        // Inisialisasi komponen UI dari XML layout
        editName = findViewById(R.id.addNama)
        editNickname = findViewById(R.id.addNP)
        editEmail = findViewById(R.id.addEmail)
        editAddress = findViewById(R.id.addAlamat)
        editBirthDate = findViewById(R.id.addTglLahir)
        editPhone = findViewById(R.id.addHP)
        saveButton = findViewById(R.id.doneButton)
        photo = findViewById(R.id.addPhoto)

        // Inisialisasi database helper
        notesDatabaseHelper = DatabaseHelper(this)

        // Ambil data dari intent
        noteId = intent.getIntExtra("noteId", -1)
        val noteName = intent.getStringExtra("noteTitle")
        val noteNickname = intent.getStringExtra("noteNickname")
        val noteEmail = intent.getStringExtra("noteEmail")
        val noteAddress = intent.getStringExtra("noteAddress")
        val noteBirthDate = intent.getStringExtra("noteBirthDate")
        val notePhone = intent.getStringExtra("notePhone")
        val notePhoto = intent.getStringExtra("notePhotoUri")

        // Set nilai awal pada EditTexts
        editName.setText(noteName)
        editNickname.setText(noteNickname)
        editEmail.setText(noteEmail)
        editAddress.setText(noteAddress)
        editBirthDate.setText(noteBirthDate)
        editPhone.setText(notePhone)

        // Load existing photo if available
        if (notePhoto != null) {
            photoUri = Uri.parse(notePhoto)
            photo.setImageURI(photoUri)
        }

        // Set up Date Picker untuk tanggal lahir
        editBirthDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    editBirthDate.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        // Set listener untuk ImageView untuk memilih foto
        photo.setOnClickListener {
            // Menampilkan dialog untuk memilih antara galeri atau kamera
            val options = arrayOf("Ambil Foto", "Pilih dari Galeri")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pilih Foto")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera() // Ambil foto
                    1 -> openGallery() // Pilih dari galeri
                }
            }
            builder.show()
        }

        // Tambahkan listener untuk saveButton
        saveButton.setOnClickListener {
            // Ambil data yang sudah diedit dari EditTexts
            val updatedName = editName.text.toString()
            val updatedNickname = editNickname.text.toString()
            val updatedEmail = editEmail.text.toString()
            val updatedAddress = editAddress.text.toString()
            val updatedBirthDate = editBirthDate.text.toString()
            val updatedPhone = editPhone.text.toString()

            // Validasi noteId sebelum update
            if (noteId != -1) {
                val rowsUpdated = notesDatabaseHelper.updateNote(
                    noteId,
                    updatedName,
                    updatedNickname,
                    updatedEmail,
                    updatedAddress,
                    updatedBirthDate,
                    updatedPhone,
                    photoUri?.toString() // Simpan URI foto jika ada
                )

                if (rowsUpdated > 0) {
                    Toast.makeText(this, "Catatan berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke MainActivity setelah update
                } else {
                    Toast.makeText(this, "Gagal memperbarui catatan", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "ID catatan tidak valid", Toast.LENGTH_SHORT).show()
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
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    photo.setImageBitmap(imageBitmap)

                    // Simpan gambar ke galeri dan dapatkan URI
                    val imageUri = saveImageToGallery(imageBitmap)
                    photoUri = imageUri
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
            CAMERA_PERMISSION
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
            CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Izinkan akses kamera", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Izinkan akses penyimpanan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}