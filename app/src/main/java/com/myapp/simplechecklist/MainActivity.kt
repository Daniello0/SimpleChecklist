package com.myapp.simplechecklist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val today: Button = findViewById(R.id.buttonToday)
        val tomorrow: Button = findViewById(R.id.buttonTomorrow)
        val sevenDays: Button = findViewById(R.id.buttonSevenDays)
        val completed: Button = findViewById(R.id.buttonCompleted)
        val allTasks: Button = findViewById(R.id.buttonAllTasks)

        today.setOnClickListener {
            val newTaskText = today.text.toString()
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("tasks_text", newTaskText)
            startActivity(intent)
        }
        tomorrow.setOnClickListener {
            val newTaskText = tomorrow.text.toString()
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("tasks_text", newTaskText)
            startActivity(intent)
        }
        sevenDays.setOnClickListener {
            val newTaskText = sevenDays.text.toString()
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("tasks_text", newTaskText)
            startActivity(intent)
        }
        completed.setOnClickListener {
            val newTaskText = completed.text.toString()
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("tasks_text", newTaskText)
            startActivity(intent)
        }
        allTasks.setOnClickListener {
            val newTaskText = allTasks.text.toString()
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("tasks_text", newTaskText)
            startActivity(intent)
        }
    }
}