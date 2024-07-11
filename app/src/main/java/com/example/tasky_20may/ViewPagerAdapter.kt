package com.example.tasky_20may

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) { //private val ownTasksAdapter: OwnRecyclerAdapter

   override fun getItemCount(): Int = 3 //private val taskActionsListener: OwnTasksFragment.TaskActionsListener

   override fun createFragment(position: Int): Fragment {
      return when (position) {
         0 -> OwnTasksFragment() //здесь передача адаптера во фрагмент //ownTasksAdapter
         1 -> ToMeFragment()
         2 -> FromMeFragment()
         else -> throw IllegalStateException("Unexpected position $position")
      }
   }
}