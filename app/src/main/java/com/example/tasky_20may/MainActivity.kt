package com.example.tasky_20may

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tasky_20may.databinding.AccountsPopupBinding
import com.example.tasky_20may.databinding.ActivityMainBinding
import com.example.tasky_20may.databinding.TaskPopupBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity(), AccountsRecyclerAdapter.OnAccountClickListener {
   lateinit var b: ActivityMainBinding
   lateinit var auth: FirebaseAuth
   private val db = Firebase.firestore
   private val st = FirebaseStorage.getInstance()

   val dbAccounts = db.collection("Accounts")
   val dbTasks = db.collection("Tasks")

   lateinit var acAdapter: AccountsRecyclerAdapter
   lateinit var ownTasksAdapter: OwnRecyclerAdapter
   lateinit var fromMeAdapter: FromMeRecyclerAdapter

   lateinit var adapterMenu: AccountsRecyclerAdapter

   val tabIcons = arrayOf(
      R.drawable.house_pic,
      R.drawable.tome_pic,
      R.drawable.fromme_pic
   )

   @SuppressLint("ClickableViewAccessibility")
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      b = ActivityMainBinding.inflate(layoutInflater)
      setContentView(b.root)
      auth = Firebase.auth

      loadImageFromFirestore()
      dbAccounts.document(auth.currentUser!!.uid).get().addOnCompleteListener { document ->
         val accountName = document.result.getString("accountName")
         b.mainscreenTitle.text = accountName
      }

      ownTasksAdapter = OwnRecyclerAdapter(this)
      fromMeAdapter = FromMeRecyclerAdapter(this) //здесь инициализация адаптера в активности

      b.pager.adapter = ViewPagerAdapter(this) //здесь передача адаптера ownTasksAdapter в адаптер пейджера

      TabLayoutMediator(b.tab, b.pager) { tab, position ->
         tab.setIcon(tabIcons[position])
      }.attach()

      adapterMenu = AccountsRecyclerAdapter(this)
      b.menuRecycler.layoutManager = LinearLayoutManager(this)
      b.menuRecycler.adapter = adapterMenu
      adapterMenu.setOnAccountClickListener(this)

      dbAccounts.get().addOnCompleteListener { task ->
         if (task.isSuccessful) {
            val accountList = task.result?.toObjects(Account::class.java)
            adapterMenu.addAccountsInPopUp(accountList ?: listOf())
         } else {
            Log.e("Firestore", "Error getting documents: ", task.exception)
         }
      }

      b.threeDots.setOnTouchListener { v, e ->
         when (e.action){
            MotionEvent.ACTION_DOWN -> { v.alpha = 0.3f }
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               menuPopUpShow(v)
            }
         }
         true
      }

      b.lin.setOnTouchListener { v, e ->
         when (e.action){
            MotionEvent.ACTION_DOWN -> { v.alpha = 0.3f }
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               b.drawer.openDrawer(GravityCompat.START)
            }
         }
         true
      }
   }

   fun loadImageFromFirestore() { //когда мы загружаем картинку мы можем сразу записать в переменную ее url
                                  //и находить ее легко, удобно
      dbAccounts.document(auth.currentUser!!.uid).get().addOnSuccessListener { it ->
         val account = it.toObject(Account::class.java)
         if (account != null && !account.accountImageUrl.isNullOrEmpty()) {
            st.getReferenceFromUrl(account.accountImageUrl).getBytes(Long.MAX_VALUE).addOnSuccessListener {
               val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
               b.mainAvatar.setImageBitmap(bitmap)
               cropAvatar(Uri.parse(account.accountImageUrl), b.mainAvatar)
               Log.d("Pic", "Картинка в аватарке")
            }
         }
      }
   }

   fun menuPopUpShow(view: View) {
      val popupMenu = PopupMenu(this, view)
      popupMenu.menuInflater.inflate(R.menu.three_dots_menu, popupMenu.menu)

      popupMenu.setOnMenuItemClickListener { menuItem ->
         when (menuItem.itemId) {
            R.id.menu_logout -> {
               auth.signOut()
               startActivity(Intent(this, LoginActivity::class.java))
               true
            }
            R.id.menu_delete_accaunt -> {
               val user = auth.currentUser
               if (user != null) {
                  dbAccounts.document(auth.currentUser!!.uid).delete()
                  user.delete().addOnCompleteListener {
                     if(it.isSuccessful){
                        Log.d("deleteAccaunt", "вроде удален")
                     }
                     else Log.d("deleteAccaunt", "провал удаления")
                  }
               }
               true
            }
            else -> false
         }
      }
      popupMenu.show()
   }

   private fun cropAvatar(uri: Uri, v: ImageView) {
      Glide.with(this)
         .load(uri)
         .apply(RequestOptions.circleCropTransform()) // Применение обрезки в круглую форму
         .into(v)
   }

   override fun onAccountClick(account: Account) {
      Toast.makeText(this, "Не доделано", Toast.LENGTH_SHORT).show()
   }
}