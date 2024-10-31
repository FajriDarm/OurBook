package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class NotesAdapter(
    private var notes: MutableList<Note>,
    private val context: Context,
    private val onNoteDeleted: (Int) -> Unit // Listener untuk hapus
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.txtNama)
        val nickname: TextView = itemView.findViewById(R.id.txtNamaPanggilan)
        val email: TextView = itemView.findViewById(R.id.txtEmail)
        val address: TextView = itemView.findViewById(R.id.txtAlamat)
        val birthDate: TextView = itemView.findViewById(R.id.txtTglLahir)
        val phoneNumber: TextView = itemView.findViewById(R.id.txtHP)
        val updateButton: ImageView = itemView.findViewById(R.id.btnEdit)
        val deleteButton: ImageView = itemView.findViewById(R.id.btnDelete)
        val photo: ImageView = itemView.findViewById(R.id.Photo) // Pastikan ID ini sesuai dengan XML Anda
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.name.text = note.name
        holder.nickname.text = note.nickname
        holder.email.text = note.email
        holder.address.text = note.address
        holder.birthDate.text = note.birthDate
        holder.phoneNumber.text = note.phone

        // Load the image if photoUri is not null
        if (note.photoUri != null) {
            holder.photo.setImageURI(Uri.parse(note.photoUri))
        } else {
            holder.photo.setImageResource(R.drawable.baseline_insert_photo_24) // Set a default image if no photo is available
        }

        // Handle Update Button Click
        holder.updateButton.setOnClickListener {
            if (context is MainActivity) {
                val intent = Intent(context, UpdateActivity::class.java).apply {
                    putExtra("noteId", note.id)
                    putExtra("noteTitle", note.name)
                    putExtra("noteNickname", note.nickname)
                    putExtra("noteEmail", note.email)
                    putExtra("noteAddress", note.address)
                    putExtra("noteBirthDate", note.birthDate)
                    putExtra("notePhone", note.phone)
                    putExtra("notePhotoUri", note.photoUri) // Pass the photo URI to the update activity
                }
                context.startActivity(intent)
            }
        }

        // Handle Delete Button Click
        holder.deleteButton.setOnClickListener {
            val notesDatabaseHelper = DatabaseHelper(context)
            val rowsDeleted = notesDatabaseHelper.deleteNoteById(note.id)

            if (rowsDeleted > 0) {
                notes.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, notes.size)

                Toast.makeText(context, "Catatan berhasil dihapus", Toast.LENGTH_SHORT).show()

                // Call the delete listener in the Activity
                onNoteDeleted(note.id)
            } else {
                Toast.makeText(context, "Gagal menghapus catatan", Toast.LENGTH_SHORT).show()
            }
            notesDatabaseHelper.close()
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    // Fungsi untuk update list notes
    fun updateNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }
}