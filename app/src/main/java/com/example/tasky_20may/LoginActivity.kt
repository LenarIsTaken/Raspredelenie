package com.example.tasky_20may

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.tasky_20may.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
   lateinit var b: ActivityLoginBinding
   lateinit var auth: FirebaseAuth

   @SuppressLint("ClickableViewAccessibility")
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      Thread.sleep(500)
      installSplashScreen()
      b = ActivityLoginBinding.inflate(layoutInflater)
      setContentView(b.root)
      auth = Firebase.auth

      if (auth.currentUser != null) startActivity(Intent(this, MainActivity::class.java))

      plainStatesSetup()

      b.loginBtn.setOnTouchListener { v, e ->
         when (e.action) {
            MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               val email = b.loginEmail.text.toString().trim()
               val password = b.loginPassword.text.toString().trim()

               if (isValidData(email, password)){
                  btnLoadingMod(true)
                  auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) {
                     if (it.isSuccessful) {
                        if (auth.currentUser?.isEmailVerified == true) {
                           startActivity(Intent(this, MainActivity::class.java))
                        } else b.loginPassword.setError("Аккаунт не подтвержден"); btnLoadingMod(false)
                     } else b.loginPassword.setError("Неверный Email или пароль"); btnLoadingMod(false)
                  }
               }
            }
         }
         true
      }

      b.loginSwitch.setOnTouchListener { v, e ->
         when (e.action) {
            MotionEvent.ACTION_DOWN -> { v.alpha = 0.3f
               startActivity(Intent(this, RegistrationActivity::class.java))
               finish()
            }
            MotionEvent.ACTION_UP -> v.alpha = 1.0f
         }
         true
      }
   }

   fun isValidData(email: String, password: String): Boolean {
      if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
         b.loginEmail.setError("Неправильный формат Email")
         return false
      }
      if (password.length < 6 || password.isEmpty()){
         b.loginPassword.setError("Нужно 6 или больше символов")
         return false
      }
      return true
   }

   fun btnLoadingMod(mode: Boolean){
      if (mode){
         b.loginBtn.setTextColor(Color.TRANSPARENT)
         b.loginBtn.isClickable = false
         b.loginLoadingAni.visibility = View.VISIBLE
      }
      else{
         b.loginBtn.setTextColor(Color.BLACK)
         b.loginBtn.isClickable = true
         b.loginLoadingAni.visibility = View.GONE
      }
   }

   fun plainStatesSetup(){
      b.loginEmail.setOnFocusChangeListener { v, hasFocus ->
         if (hasFocus) v.setBackgroundResource(R.drawable.custom_button)
         else v.setBackgroundResource(R.drawable.custom_button_gray)
      }
      b.loginPassword.setOnFocusChangeListener { v, hasFocus ->
         if (hasFocus) v.setBackgroundResource(R.drawable.custom_button)
         else v.setBackgroundResource(R.drawable.custom_button_gray)
      }
   }
}