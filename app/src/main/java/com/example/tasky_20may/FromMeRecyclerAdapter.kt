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
import com.example.tasky_20may.databinding.ItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FromMeRecyclerAdapter(private val context: Context): RecyclerView.Adapter<FromMeRecyclerAdapter.ItemHolder>() {
   private val db = Firebase.firestore
   private val st = FirebaseStorage.getInstance()

   private val list = ArrayList<Task>()

   /*interface OnTaskClickListener {
      fun onTaskClick(taskUpd: Task)
   }

   private var listener: OnTaskClickListener? = null

   fun setOnTaskClickListener(listener: OnTaskClickListener) {
      this.listener = listener
   }*/

   class ItemHolder(itemXml: View, private val context: Context): RecyclerView.ViewHolder(itemXml) {
      val b = ItemBinding.bind(itemView)

      @SuppressLint("ClickableViewAccessibility")
      fun bind(task: Task, db: FirebaseFirestore, st: FirebaseStorage){
         b.back.setBackgroundResource(R.drawable.item_color_none) //может добавлю цвет, если успею
         b.itemTxt.text = task.datataskTitle
         b.itemDate.text = formatToString(task.datataskDateTime)

         db.collection("Accounts").document(task.datataskFor).get().addOnSuccessListener { document ->
            val accountNameFor = document.getString("accountName") //ПЕРВОЕ ДОБАВЛЕНИЕ ИМЕНИ В RECYCLER
            b.itemAccountName.text = "$accountNameFor >"

            val accountImageUrl = document.getString("accountImageUrl")
            if (accountImageUrl != null && accountImageUrl != "") {
               st.getReferenceFromUrl(accountImageUrl).getBytes(Long.MAX_VALUE).addOnSuccessListener {
                  val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                  b.itemAvatar.setImageBitmap(bitmap)
                  cropAvatar(Uri.parse(accountImageUrl), b.itemAvatar)
               }
            }
            b.itemAvatar.visibility = View.VISIBLE

            /*b.root.setOnTouchListener { v, e ->
               when (e.action) {
                  MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
                  MotionEvent.ACTION_UP -> { v.alpha = 1.0f
                     listener.onTaskClick(task)
                  }
               }
               true
            }*/
         }
      }

      fun formatToString(dateTime: Date): String {
         val formatter = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())
         return formatter.format(dateTime)
      }

      fun cropAvatar(uri: Uri, v: ImageView) {
         Glide.with(context)
            .load(uri)
            .apply(RequestOptions.circleCropTransform()) // Применение обрезки в круглую форму
            .into(v)
      }
   }

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
      return ItemHolder(view, context)
   }

   override fun onBindViewHolder(h: ItemHolder, position: Int) {
      h.bind(list[position], db, st) //, listener!!
   }

   override fun getItemCount(): Int = list.size

   fun addTaskData(task: Task){
      list.add(task)
      notifyDataSetChanged()
   }

   fun addTaskDataOnStartApp(task: List<Task>){
      list.clear()
      list.addAll(task) //почему то при выводе задач из firebase карточки в списке выводятся правильно сверху сниз :)
      notifyDataSetChanged()
   }

   fun removeTask(task: Task) {
      val position = list.indexOf(task)
      if (position != -1) {
         list.removeAt(position)
         notifyItemRemoved(position)
         notifyItemRangeChanged(position, itemCount)
      }
   }

   fun updateTask(task: Task) {
      val position = list.indexOf(task)
      if(position != -1){
         list[position] = task
         notifyItemChanged(position)
         Log.d("rrr", task.datataskTitle)
      }
      else Log.d("ddd", "провал")
   }
}