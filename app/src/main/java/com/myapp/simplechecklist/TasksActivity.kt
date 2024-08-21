package com.myapp.simplechecklist

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class TasksActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        val taskText: TextView = findViewById(R.id.textTasks)
        val buttonBack: Button = findViewById(R.id.buttonBack)
        val newTaskText = intent.getSerializableExtra("tasks_text")
        val buttonAddTask: Button = findViewById(R.id.buttonNewTask)
        taskText.text = newTaskText.toString()

        val list: ListView = findViewById(R.id.listView)
        val todos: MutableList<String> = mutableListOf()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, todos)
        list.adapter = adapter

        if (newTaskText == "Все задачи") {
            val db = DbHelper(this, null)
            for (i in 1..db.getRowCount()) {
                adapter.insert(db.getTaskByRowIndex(i-1)!!.name, 0)
            }
            db.close()
        }

        buttonBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        buttonAddTask.setOnClickListener {
            val intent = Intent(this, TaskCreateActivity::class.java)
            intent.putExtra("tasks_text", newTaskText)
            startActivity(intent)
        }
        
        list.setOnItemClickListener { _, _, i, _ ->
            val choose = arrayOf("Выполнено", "Удалить")
            val name = list.getItemAtPosition(i).toString()
            AlertDialog.Builder(this)
                .setTitle("Выберите действие")
                .setItems(choose) { _, which ->
                    when (which) {
                        0 -> {
                            val db = DbHelper(this, null)
                            val completedTask = db.getTaskByName(name)
                            completedTask!!.status = "completed"
                            db.saveTaskByName(name, completedTask)
                            db.close()
                            Toast.makeText(this, "Задание выполнено!", Toast.LENGTH_SHORT).show()
                        }
                        1 -> {
                            val db = DbHelper(this, null)
                            db.deleteTaskByName(name)
                            db.close()
                            Toast.makeText(this, "Задание удалено", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .show()
        }

        list.setOnItemLongClickListener { adapterView, view, i, l ->
            val isEditing = true
            val db = DbHelper(this, null)
            val editingTask = db.getTaskByName(list.getItemAtPosition(i).toString())
            db.close()
            val intent = Intent(this, TaskCreateActivity::class.java)
            intent.putExtra("is_editing", isEditing)
            intent.putExtra("editingTaskName", editingTask!!.name)
            intent.putExtra("tasks_text", newTaskText)
            startActivity(intent)
            true
        }
    }
}