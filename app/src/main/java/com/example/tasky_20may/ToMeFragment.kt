package com.example.tasky_20may

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasky_20may.databinding.FragmentOwnTasksBinding
import com.example.tasky_20may.databinding.FragmentToMeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ToMeFragment : Fragment() {
   lateinit var b: FragmentToMeBinding

   lateinit var auth: FirebaseAuth
   private val db = Firebase.firestore
   private val st = FirebaseStorage.getInstance()
   lateinit var adapter: ToMeRecyclerAdapter

   val dbAccounts = db.collection("Accounts")
   val dbTasks = db.collection("Tasks")

   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View {
      b = FragmentToMeBinding.inflate(inflater, container, false)
      auth = FirebaseAuth.getInstance()

      recyclerSetup()
      tasksSetup3()

      return b.root
   }

   fun recyclerSetup(){ //настройка recycler view
      context?.let {
         adapter = ToMeRecyclerAdapter(it)
         b.recycler.layoutManager = LinearLayoutManager(context)
         b.recycler.adapter = adapter
      } ?: throw IllegalStateException("Fragment not attached to a context.")
   }

   fun tasksSetup3(){
      dbTasks
         .whereNotEqualTo("datataskBy", auth.currentUser!!.uid)
         .whereEqualTo("datataskFor", auth.currentUser!!.uid)
         .get()
         .addOnSuccessListener {
            val taskList: List<Task> = it.toObjects(Task::class.java)
            adapter.addTaskDataOnStartApp(taskList)
         }.addOnFailureListener {
            Log.e("Firestore", "Ошибка при получении данных", it)
         }
   }
}