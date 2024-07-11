package com.example.tasky_20may

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasky_20may.databinding.FragmentOwnTasksBinding
import com.example.tasky_20may.databinding.TaskPopupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class OwnTasksFragment : Fragment(), OwnRecyclerAdapter.OnTaskClickListener {
   lateinit var b: FragmentOwnTasksBinding

   lateinit var auth: FirebaseAuth
   private val db = Firebase.firestore
   private val st = FirebaseStorage.getInstance()
   lateinit var adapter: OwnRecyclerAdapter

   val dbTasks = db.collection("Tasks")
   val dbAccounts = db.collection("Accounts")

   @SuppressLint("ClickableViewAccessibility")
   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View {
      b = FragmentOwnTasksBinding.inflate(inflater, container, false)
      auth = FirebaseAuth.getInstance()

      recyclerSetup()
      tasksSetup()

      dbTasks.addSnapshotListener { snapshots, exception ->
         if (exception != null) {
            Log.e("FirestoreListener", "Listen failed!", exception)
            return@addSnapshotListener
         }

         if (snapshots != null) {
            for (documentChange in snapshots.documentChanges) {
               dbTasks
                  .whereEqualTo("datataskBy", auth.currentUser!!.uid)
                  .whereEqualTo("datataskFor", auth.currentUser!!.uid)
                  .get()
                  .addOnSuccessListener {
                     val taskList: List<Task> = it.toObjects(Task::class.java)
                     adapter.addTaskDataOnStartApp(taskList)
                     Log.d("inFragAddTask", "обновление работает")
                  }
            }
         }
      } //слушатель изменений

      b.ownFragBtn.setOnTouchListener { v, e ->
         when (e.action){
            MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               val blank = Task("")
               taskPopUpShow(blank)
            }
         }
         true
      }

      return b.root
   }

   @SuppressLint("ClickableViewAccessibility")
   fun taskPopUpShow(taskUpd: Task){ //пока вроде не нужно: accountToId: String, accountToName: String, ownTask: Boolean
      val b = TaskPopupBinding.inflate(layoutInflater)
      val taskdialog = Dialog(requireContext())
      taskdialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
      taskdialog.setContentView(b.root)
      taskdialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

      dbAccounts.document(auth.currentUser!!.uid).get().addOnCompleteListener { document ->
         val accountName = document.result.getString("accountName")
         b.taskPopUpAccountName.text = accountName
      }

      if (taskUpd.datataskId != ""){
         b.taskPopupName.setText(taskUpd.datataskTitle)
         if(taskUpd.datataskDesc.isNotEmpty()) b.taskPopupDesc.setText(taskUpd.datataskDesc)
         //еще свойства задачи
      }

      b.taskPopUpBtn.setOnTouchListener { v, e ->
         when (e.action){
            MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               val title = b.taskPopupName.text.toString().trim()
               val desc = b.taskPopupDesc.text.toString().trim()

               if(taskUpd.datataskId != ""){
                  val task = Task(taskUpd.datataskId, taskUpd.datataskBy, taskUpd.datataskFor, title, desc,
                     "",
                     "",
                     "")
                  dbTasks.document(taskUpd.datataskId).set(task)
                  adapter.updateTask(task)
               }
               else{
                  val ref = dbTasks.document()
                  val refId = ref.id
                  val task = Task(refId, auth.currentUser!!.uid, auth.currentUser!!.uid, title, desc,
                     "",
                     "",
                     "")
                  ref.set(task)
                  adapter.addTaskData(task)
               }

               taskdialog.cancel()
            }
         }
         true
      }

      b.taskPopUpDelete.setOnTouchListener { v, e ->
         when (e.action){
            MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               dbTasks.document(taskUpd.datataskId).delete()
               adapter.removeTask(taskUpd)
               taskdialog.cancel()
            }
         }
         true
      }

      taskdialog.show()
   }

   fun recyclerSetup(){ //настройка recycler view
      context?.let {
         adapter = OwnRecyclerAdapter(it)
         b.recycler.layoutManager = LinearLayoutManager(it)
         b.recycler.adapter = adapter
         adapter.setOnTaskClickListener(this)
      } ?: throw IllegalStateException("Fragment not attached to a context.")
   }

   fun tasksSetup(){ //вывод задач при запуске приложения
      dbTasks
         .whereEqualTo("datataskBy", auth.currentUser!!.uid)
         .whereEqualTo("datataskFor", auth.currentUser!!.uid)
         .get()
         .addOnSuccessListener {
            val taskList: List<Task> = it.toObjects(Task::class.java)
            adapter.addTaskDataOnStartApp(taskList)
            Log.d("inFragAddTask", "обновление работает")
         }
   }

   override fun onTaskClick(taskUpd: Task) {
      taskPopUpShow(taskUpd)
   }
}