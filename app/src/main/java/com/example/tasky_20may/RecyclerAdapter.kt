package com.example.tasky_20may

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tasky_20may.databinding.ItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecyclerAdapter: RecyclerView.Adapter<RecyclerAdapter.ItemHolder>() {
   private val db = Firebase.firestore
   lateinit var auth: FirebaseAuth

   private val list = ArrayList<Task>()

   class ItemHolder(itemXml: View): RecyclerView.ViewHolder(itemXml) {
      val b = ItemBinding.bind(itemView)
      fun bind(task: Task){
         b.itemTxt.text = task.datataskTitle
         b.itemDate.text = formatToString(task.datataskDateTime)
      }
      fun formatToString(dateTime: Date): String {
         val formatter = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())
         return formatter.format(dateTime)
      }
   }

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
      return ItemHolder(view)
   }

   override fun onBindViewHolder(holder: ItemHolder, position: Int) {
      holder.bind(list[position])

      holder.b.root.setOnClickListener {
         db.collection("Tasks").document(auth.currentUser!!.uid).collection("tasks").document(list[position].datataskId).delete()
         list.removeAt(holder.adapterPosition)
         notifyItemRemoved(holder.adapterPosition)
         notifyItemRangeChanged(holder.adapterPosition, itemCount)
      }
   }

   override fun getItemCount() = list.size

   fun addTaskData(task: Task){
      list.add(task)
      notifyDataSetChanged()
   }

   fun addTaskDataOnStartApp(task: List<Task>){
      list.clear()
      list.addAll(task) //почему то при выводе задач из firebase карточки в списке выводятся правильно сверху сниз :)
      notifyDataSetChanged()
   }

   fun deleteTask(){

   }

   fun initAuth(auth: FirebaseAuth) {
      this.auth = auth
   }
}