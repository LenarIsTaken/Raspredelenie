package com.example.tasky_20may

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.tasky_20may.databinding.ItemBinding
import com.example.tasky_20may.databinding.TaskPopupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OwnRecyclerAdapter(private val context: Context): RecyclerView.Adapter<OwnRecyclerAdapter.ItemHolder>() {
   private val db = Firebase.firestore
   val dbTasks = db.collection("Tasks")

   private val list = ArrayList<Task>()

   interface OnTaskClickListener {
      fun onTaskClick(taskUpd: Task)
   }

   private var listener: OnTaskClickListener? = null

   fun setOnTaskClickListener(listener: OnTaskClickListener) {
      this.listener = listener
   }

   class ItemHolder(itemXml: View, private val context: Context) :
      RecyclerView.ViewHolder(itemXml) {
      val b = ItemBinding.bind(itemView)

      @SuppressLint("ClickableViewAccessibility")
      fun bind(task: Task, listener: OnTaskClickListener) {
         b.back.setBackgroundResource(R.drawable.item_color_none) //запись цвета в firestore и после выгрузка в элемент списка
         b.itemTxt.text = task.datataskTitle
         b.itemDate.text = formatToString(task.datataskDateTime)

         b.root.setOnTouchListener { v, e ->
            when (e.action) {
               MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
               MotionEvent.ACTION_UP -> { v.alpha = 1.0f
                  listener.onTaskClick(task)
               }
            }
            true
         }
      }

      fun formatToString(dateTime: Date): String {
         val formatter = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())
         return formatter.format(dateTime)
      }
   }

   override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
      Log.d("inAdapter", "холдер создался")
      return ItemHolder(view, context)
   }

   override fun onBindViewHolder(h: ItemHolder, position: Int) {
      h.bind(list[position], listener!!)
   }

   override fun getItemCount(): Int = list.size

   fun addTaskData(task: Task) {
      list.add(task)
      notifyDataSetChanged()
   }

   fun addTaskDataOnStartApp(task: List<Task>) {
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