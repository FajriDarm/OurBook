package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var addButton: ImageView
    private lateinit var db: DatabaseHelper
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var recyclerView: RecyclerView

    private fun loadNotes() {
        val notes = db.getAllNotes() // Ambil semua catatan dari database
        notesAdapter.updateNotes(notes) // Perbarui adapter dengan catatan yang diambil
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)

        recyclerView = findViewById(R.id.notesVecycleView)

        // Ambil data dari database untuk inisialisasi adapter
        val initialNotes = db.getAllNotes() // Ambil semua catatan saat aplikasi dibuka
        if (initialNotes.isEmpty()) {
            Log.d("MainActivity", "No notes found in the database.")
        } else {
            Log.d("MainActivity", "Initial Notes: $initialNotes")
        }

        notesAdapter = NotesAdapter(initialNotes.toMutableList(), this) { noteId ->
            Toast.makeText(this, "Note with ID $noteId deleted", Toast.LENGTH_SHORT).show()
        }

        recyclerView.adapter = notesAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        addButton = findViewById(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, add_Note2::class.java) // Ganti dengan activity yang sesuai
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotes() // Memuat ulang data saat activity di-resume
    }
}
