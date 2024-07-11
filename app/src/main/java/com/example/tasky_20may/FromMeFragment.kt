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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasky_20may.databinding.AccountsPopupBinding
import com.example.tasky_20may.databinding.FragmentFromMeBinding
import com.example.tasky_20may.databinding.TaskPopupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class FromMeFragment : Fragment(), AccountsRecyclerAdapter.OnAccountClickListener{ //, FromMeRecyclerAdapter.OnTaskClickListener
   lateinit var b: FragmentFromMeBinding

   lateinit var auth: FirebaseAuth
   private val db = Firebase.firestore
   private val st = FirebaseStorage.getInstance()
   lateinit var adapter: FromMeRecyclerAdapter
   lateinit var adapterAccs: AccountsRecyclerAdapter

   val dbTasks = db.collection("Tasks")
   val dbAccounts = db.collection("Accounts")

   @SuppressLint("ClickableViewAccessibility")
   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View {
      b = FragmentFromMeBinding.inflate(inflater, container, false)
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
                  .whereNotEqualTo("datataskFor", auth.currentUser!!.uid)
                  .get()
                  .addOnSuccessListener {
                     val taskList: List<Task> = it.toObjects(Task::class.java)
                     adapter.addTaskDataOnStartApp(taskList)
                  }
            }
         }
      } //слушатель изменений

      b.fromMeFragBtn.setOnTouchListener { v, e ->
         when (e.action){
            MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               accountsPopUpShow()
            }
         }
         true
      }

      return b.root
   }

   @SuppressLint("ClickableViewAccessibility")
   fun taskPopUpShow(account: Account, taskUpd: Task){ //пока вроде не нужно: accountToId: String, accountToName: String, ownTask: Boolean
      val b = TaskPopupBinding.inflate(layoutInflater)
      val taskdialog = Dialog(requireContext())
      taskdialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
      taskdialog.setContentView(b.root)
      taskdialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

      //adapter.setOnTaskClickListener(this)

      /*if(account.accountId != ""){
         b.taskPopUpAccountName.text = account.accountName
      }
      else{
         dbAccounts.document(taskUpd.datataskFor).get().addOnCompleteListener{
            val account = it.result.getString("accountName")
            b.taskPopUpAccountName.text = account
         }
      }*/

      b.taskPopUpBtn.setOnTouchListener { v, e ->
         when (e.action){
            MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               val title = b.taskPopupName.text.toString().trim()
               val desc = b.taskPopupDesc.text.toString().trim()

               /*if(account.accountId != ""){
                  Log.d("fromTask", "добавление")

                  /*val ref = dbTasks.document()
                  val refId = ref.id
                  val task = Task(refId, auth.currentUser!!.uid, account.accountId, title, desc,
                     "",
                     "",
                     "")

                  ref.set(task)
                  adapter.addTaskData(task)*/
               }
               else{
                  Log.d("fromTask", "обновление")

                  /*val task = Task(taskUpd.datataskId, taskUpd.datataskBy, taskUpd.datataskFor, title, desc,
                     "",
                     "",
                     "")
                  dbTasks.document(taskUpd.datataskId).set(task)
                  adapter.updateTask(task)*/
               }*/

               val ref = dbTasks.document()
               val refId = ref.id
               val task = Task(refId, auth.currentUser!!.uid, account.accountId, title, desc,
                  "",
                  "",
                  "")

               ref.set(task)
               adapter.addTaskData(task)

               taskdialog.cancel()
            }
         }
         true
      }

      /*b.taskPopUpDelete.setOnTouchListener { v, e ->
         when (e.action){
            MotionEvent.ACTION_DOWN -> v.alpha = 0.3f
            MotionEvent.ACTION_UP -> { v.alpha = 1.0f
               dbTasks.document(taskUpd.datataskId).delete()
               adapter.removeTask(taskUpd)
               taskdialog.cancel()
            }
         }
         true
      }*/

      taskdialog.show()
   }

   @SuppressLint("ClickableViewAccessibility")
   fun accountsPopUpShow(){
      val b = AccountsPopupBinding.inflate(layoutInflater)
      val accountsdialog = Dialog(requireContext())
      accountsdialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
      accountsdialog.setContentView(b.root)
      accountsdialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

      adapterAccs = AccountsRecyclerAdapter(requireContext())
      b.recycler.layoutManager = LinearLayoutManager(requireContext())
      b.recycler.adapter = adapterAccs
      adapterAccs.setOnAccountClickListener(this)

      dbAccounts.get().addOnCompleteListener { task ->
         val reseivedAccounts = task.result
         val filteredAccounts = reseivedAccounts?.filter { it.id != auth.currentUser!!.uid }
         val accountList = filteredAccounts?.mapNotNull { it.toObject(Account::class.java) }
         adapterAccs.addAccountsInPopUp(accountList ?: listOf())
      }

      accountsdialog.show()
   }

   override fun onAccountClick(account: Account) {
      val taskBlank = Task("")
      taskPopUpShow(account, taskBlank)
   }

   /*override fun onTaskClick(taskUpd: Task) {
      val accountBlack = Account("")
      taskPopUpShow(accountBlack, taskUpd)
   }*/

   fun recyclerSetup(){ //настройка recycler view
      context?.let {
         adapter = FromMeRecyclerAdapter(it)
         b.recycler.layoutManager = LinearLayoutManager(context)
         b.recycler.adapter = adapter
      } ?: throw IllegalStateException("Fragment not attached to a context.")
   }

   fun tasksSetup(){
      dbTasks
         .whereEqualTo("datataskBy", auth.currentUser!!.uid)
         .whereNotEqualTo("datataskFor", auth.currentUser!!.uid)
         .get()
         .addOnSuccessListener {
            val taskList: List<Task> = it.toObjects(Task::class.java)
            adapter.addTaskDataOnStartApp(taskList)
         }
   }
}