package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notes.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "notes"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_NICKNAME = "nickname"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_BIRTH_DATE = "birth_date"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_PHOTO_URI = "photo_uri"
    }


    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_NAME TEXT," +
                "$COLUMN_NICKNAME TEXT," +
                "$COLUMN_EMAIL TEXT," +
                "$COLUMN_ADDRESS TEXT," +
                "$COLUMN_BIRTH_DATE TEXT," +
                "$COLUMN_PHONE TEXT," +
                "$COLUMN_PHOTO_URI BLOB" +
                ")")
        db.execSQL(createTable)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Method to add a new note
    fun addNote(
        name: String,
        nickname: String,
        email: String,
        address: String,
        birthDate: String,
        phone: String,
        photoUri: String?  // Add this parameter
    ) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_NICKNAME, nickname)
            put(COLUMN_EMAIL, email)
            put(COLUMN_ADDRESS, address)
            put(COLUMN_BIRTH_DATE, birthDate)
            put(COLUMN_PHONE, phone)
            put(COLUMN_PHOTO_URI, photoUri)  // Save the photo URI
        }
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    // Method to retrieve all notes
    fun getAllNotes(): List<Note> {
        val notesList = mutableListOf<Note>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val nickname = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NICKNAME))
                val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                val address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS))
                val birthDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTH_DATE))
                val phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE))
                val photoUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_URI))

                val note = Note(
                    id = id,
                    name = name,
                    nickname = nickname,
                    email = email,
                    address = address,
                    birthDate = birthDate,
                    phone = phone,
                    photoUri = photoUri
                )
                notesList.add(note)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return notesList
    }



    // Method to delete a note by ID
    fun deleteNoteById(id: Int): Int {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    // Method to update an existing note
    fun updateNote(
        id: Int,
        name: String,
        nickname: String,
        email: String,
        address: String,
        birthDate: String,
        phone: String,
        photoUri: String?
    ): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_NICKNAME, nickname)
            put(COLUMN_EMAIL, email)
            put(COLUMN_ADDRESS, address)
            put(COLUMN_BIRTH_DATE, birthDate)
            put(COLUMN_PHONE, phone)
            put(COLUMN_PHOTO_URI, photoUri)
        }
        val result = db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return result // Kembalikan jumlah baris yang diperbarui
    }
}