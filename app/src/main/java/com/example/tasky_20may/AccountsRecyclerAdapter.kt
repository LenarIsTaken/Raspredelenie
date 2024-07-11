package com.example.tasky_20may

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.tasky_20may.databinding.AccountItemBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class AccountsRecyclerAdapter(private val context: Context): RecyclerView.Adapter<AccountsRecyclerAdapter.ItemHolder>() {
   private val db = Firebase.firestore
   private val st = FirebaseStorage.getInstance()

   private val list = ArrayList<Account>()

   interface OnAccountClickListener {
      fun onAccountClick(account: Account)
   }

   private var listener: OnAccountClickListener? = null

   fun setOnAccountClickListener(listener: OnAccountClickListener) {
      this.listener = listener
   }

   interface OnAccountInfoListener {
      fun onAccountInfo(account: Account)
   }

   private var listenerInfo: OnAccountInfoListener? = null

   fun setOnAccountInfoListener(listenerInf: OnAccountInfoListener) {
      this.listenerInfo = listenerInf
   }

   class ItemHolder(itemXml: View, private val context: Context): RecyclerView.ViewHolder(itemXml) {
      val b = AccountItemBinding.bind(itemView)

      @SuppressLint("ClickableViewAccessibility")
      fun bind(account: Account, db: FirebaseFirestore, st: FirebaseStorage, listener: OnAccountClickListener){//запись цвета в firestore и после выгрузка в элемент списка
         b.acitemName.text = account.accountName
         b.acitemEmail.text = account.accountEmail
         if (!account.accountImageUrl.isEmpty()) {
            st.getReferenceFromUrl(account.accountImageUrl).getBytes(Long.MAX_VALUE).addOnSuccessListener {
               val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
               b.acitemAvatar.setImageBitmap(bitmap)
               cropAvatar(Uri.parse(account.accountImageUrl), b.acitemAvatar)
            }
         }

         b.root.setOnTouchListener { v, e ->
            when (e.action){
               MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
               MotionEvent.ACTION_UP -> { v.alpha = 1.0f
                  val taskBlank = Task("")
                  listener.onAccountClick(account)
               }
            }
            true
         }
      }
      fun cropAvatar(uri: Uri, v: ImageView) {
         Glide.with(context)
            .load(uri)
            .apply(RequestOptions.circleCropTransform()) // Применение обрезки в круглую форму
            .into(v)
      }
   }

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.account_item, parent, false)
      return ItemHolder(view, context)
   }

   override fun onBindViewHolder(h: ItemHolder, position: Int) {
      h.bind(list[position], db, st, listener!!)
   }

   override fun getItemCount(): Int = list.size

   fun addAccountsInPopUp(accounts: List<Account>){
      list.clear()
      list.addAll(accounts) //почему то при выводе задач из firebase карточки в списке выводятся правильно сверху сниз :)
      notifyDataSetChanged()
   }
}