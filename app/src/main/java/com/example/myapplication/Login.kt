package com.example.myapplication

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import android.content.Intent
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowInsetsCompat

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val nameInput: EditText = findViewById(R.id.Username)
        val passwordInput: EditText = findViewById(R.id.Password)
        val loginButton: Button = findViewById(R.id.login)

        loginButton.setOnClickListener {
            val name: String = nameInput.text.toString()
            val password: String = passwordInput.text.toString()

            if (name == "Jean" && password == "12345") {
                Toast.makeText(this, "Login Sukses", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val munculDialog = AlertDialog.Builder(this@Login)
                munculDialog.setTitle("Login Gagal!!")
                munculDialog.setMessage("Username atau Password Salah")
                munculDialog.setPositiveButton("Coba lagi") { dialog, which ->
                    dialog.dismiss()
                }
                munculDialog.show()
            }
        }


    }
}
