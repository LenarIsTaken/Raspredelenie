package com.example.tasky_20may

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tasky_20may.databinding.ActivityRegistrationBinding
import com.example.tasky_20may.databinding.PopUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class RegistrationActivity : AppCompatActivity() {

   lateinit var b: ActivityRegistrationBinding
   lateinit var auth: FirebaseAuth
   private val db = Firebase.firestore
   private val st = FirebaseStorage.getInstance()

   val dbAccounts = db.collection("Accounts")

   private val PICK_IMAGE_REQUEST = 1
   private var selectedImageUri: Uri? = null

   @SuppressLint("ClickableViewAccessibility")
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      b = ActivityRegistrationBinding.inflate(layoutInflater)
      setContentView(b.root)
      auth = Firebase.auth

      plainStatesSetup()
      b.regAvatar.setOnTouchListener { v, e ->
         when (e.action) {
            MotionEvent.ACTION_DOWN -> v.alpha = 0.5f
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               openGallery()
            }
         }
         true
      }
      b.regSwitch.setOnTouchListener { v, e ->
         when (e.action) {
            MotionEvent.ACTION_DOWN -> { v.alpha = 0.3f
               startActivity(Intent(this, LoginActivity::class.java))
               finish()
            }
            MotionEvent.ACTION_UP -> v.alpha = 1.0f
         }
         true
      }

      b.regBtn.setOnTouchListener { v, e ->
         when (e.action) {
            MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               val name = b.regName.text.toString().trim()
               val post = b.regPost.text.toString().trim()
               val email = b.regEmail.text.toString().trim()
               val phone = b.regPhone.text.toString().trim()
               val password = b.regPassword.text.toString().trim()
               val confirmpassword = b.regConfirmPassword.text.toString().trim()

               if (isValidData(name, post, email, phone, password, confirmpassword)){
                  btnLoadingMod(true)
                  auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                     if (it.isSuccessful){
                        val ref = dbAccounts.document(auth.currentUser!!.uid)
                        val account = Account(auth.currentUser!!.uid, "", name, post, email, phone, password)
                        ref.set(account).addOnCompleteListener {
                           if (selectedImageUri != null) saveImageToFirestore(selectedImageUri!!)
                           else popUp()
                        }.addOnFailureListener { b.regEmail.setError("Ошибка загрузки данных"); btnLoadingMod(false) }
                     }
                     else b.regEmail.setError("Email уже зарегистрирован"); btnLoadingMod(false)
                  }
               }
            }
         }
         true
      }
   }

   fun isValidData(name: String, post: String, email: String, phone: String, password: String, confirmpassword: String): Boolean {
      val regexPost = Regex("\\d")
      val regexPhone = Regex("^(\\+7|8)\\d{10}\$")

      if (name.length > 30 || name.isEmpty()){
         b.regName.setError("Нужно 30 или меньше символов")
         return false
      }
      if (post.length < 3 || post.any { it.isDigit() } || post.isEmpty()){
         b.regPost.setError("Нужно 3 и больше символов без цифр")
         return false
      }
      if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()) {
         b.regEmail.setError("Неправильный формат")
         return false
      }
      if (!regexPhone.matches(phone) || phone.isEmpty()){
         b.regPhone.setError("Неправильный формат")
         return false
      }
      if (password.length < 6 || password.isEmpty()){
         b.regPassword.setError("Нужно 6 или больше символов")
         return false
      }
      if (password != confirmpassword || confirmpassword.isEmpty()){
         b.regConfirmPassword.setError("Поля не совпадают")
         return false
      }
      return true
   }

   @SuppressLint("ClickableViewAccessibility")
   fun popUp(){
      val b : PopUpBinding = PopUpBinding.inflate(layoutInflater)
      val dialog = Dialog(this)
      dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
      dialog.setCancelable(false)
      dialog.setContentView(b.root)
      dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

      b.popupBtn.setOnTouchListener { v, e ->
         when (e.action){
            MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               auth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
                  if (it.isSuccessful){
                     auth.signOut()
                     auth.addAuthStateListener {
                        if (it.currentUser == null) startActivity(Intent(this, LoginActivity::class.java))
                     }
                  }
                  else mess("Не удалось отправить письмо"); btnLoadingMod(false)
               }
            }
         }
         true
      }
      dialog.show()
      btnLoadingMod(false)
   }

   fun openGallery() {
      val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      startActivityForResult(intent, PICK_IMAGE_REQUEST)
   }

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(requestCode, resultCode, data)
      if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
         selectedImageUri = data.data!!
         cropAvatar(selectedImageUri!!, b.regAvatar)
      }
   }

   fun saveImageToFirestore(picUri: Uri) {
      val ref = st.reference.child("pics/${System.currentTimeMillis()}.png")

      ref.putFile(picUri).addOnSuccessListener { taskSnapshot ->
         ref.downloadUrl.addOnSuccessListener { url ->
            val urlToFirebase = hashMapOf("accountImageUrl" to url.toString())
            // Обновление конкретного поля "accountImageUrl" в документе "Accounts"
            dbAccounts.document(auth.currentUser!!.uid).update(urlToFirebase as Map<String, Any>)
               .addOnCompleteListener {
                  popUp()
                  Log.d("Pic", "картинка есть")
               }
               .addOnFailureListener { e ->
                  Log.e("Pic", "Ошибка при обновлении url: $e")
               }
         }.addOnFailureListener { e ->
            Log.e("Pic", "Ошибка при получении url: $e")
         }
      }.addOnFailureListener { e ->
         Log.e("Pic", "Ошибка при загрузке изображения: $e")
      }
   }

   fun mess(txt: String){
      Toast.makeText(this, txt, Toast.LENGTH_SHORT).show()
   }

   private fun cropAvatar(uri: Uri, v: ImageView) {
      Glide.with(this)
         .load(uri)
         .apply(RequestOptions.circleCropTransform()) // Применение обрезки в круглую форму
         .into(v)
   }

   fun btnLoadingMod(mode: Boolean){
      if (mode){
         b.regBtn.setTextColor(Color.TRANSPARENT)
         b.regBtn.isClickable = false
         b.regLoadingAni.visibility = View.VISIBLE
      }
      else{
         b.regBtn.setTextColor(Color.BLACK)
         b.regBtn.isClickable = true
         b.regLoadingAni.visibility = View.GONE
      }
   }

   fun plainStatesSetup(){
      b.regName.setOnFocusChangeListener { v, hasFocus ->
         if (hasFocus) v.setBackgroundResource(R.drawable.custom_button)
         else v.setBackgroundResource(R.drawable.custom_button_gray)
      }
      b.regPost.setOnFocusChangeListener { v, hasFocus ->
         if (hasFocus) v.setBackgroundResource(R.drawable.custom_button)
         else v.setBackgroundResource(R.drawable.custom_button_gray)
      }
      b.regEmail.setOnFocusChangeListener { v, hasFocus ->
         if (hasFocus) v.setBackgroundResource(R.drawable.custom_button)
         else v.setBackgroundResource(R.drawable.custom_button_gray)
      }
      b.regPhone.setOnFocusChangeListener { v, hasFocus ->
         if (hasFocus) v.setBackgroundResource(R.drawable.custom_button)
         else v.setBackgroundResource(R.drawable.custom_button_gray)
      }
      b.regPassword.setOnFocusChangeListener { v, hasFocus ->
         if (hasFocus) v.setBackgroundResource(R.drawable.custom_button)
         else v.setBackgroundResource(R.drawable.custom_button_gray)
      }
      b.regConfirmPassword.setOnFocusChangeListener { v, hasFocus ->
         if (hasFocus) v.setBackgroundResource(R.drawable.custom_button)
         else v.setBackgroundResource(R.drawable.custom_button_gray)
      }
   }
}