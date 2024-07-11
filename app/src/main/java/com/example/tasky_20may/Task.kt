package com.example.tasky_20may

import java.util.Date

data class Task(
   var datataskId: String = "",
   var datataskBy: String = "",
   var datataskFor: String = "",
   var datataskTitle: String = "",
   var datataskDesc: String = "",
   var datataskTimeToDo: String = "",
   var datataskStatus: String = "",
   var datataskColor: String = "",
   var datataskDateTime: Date = Date()
)


