package com.myapp.simplechecklist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TasksActivity : AppCompatActivity() {
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        val taskText: TextView = findViewById(R.id.textTasks)
        val buttonBack: Button = findViewById(R.id.buttonBack)
        val newTaskText = intent.getSerializableExtra("tasks_text")
        val buttonAddTask: Button = findViewById(R.id.buttonNewTask)
        taskText.text = newTaskText.toString()

        val list: ListView = findViewById(R.id.listView)
        val todos: MutableList<Task> = mutableListOf()
        val adapter = TaskAdapter(this, todos)
        list.adapter = adapter



        when (newTaskText) {
            "Сегодня" -> {
                //Today tasks
                val db = DbHelper(this, null)
                for (i in 1..db.getRowCount()) {
                    val task = db.getTaskByRowIndex(i - 1)
                    if (task.date == "Нет" && task.status == "") {
                        adapter.addTask(task)
                    } else if (task.date != "Нет" && task.time == "Нет" && task.status == "") {
                        if (isDateExpired(task.date)) {
                            task.status = "Просрочено"
                            db.saveTaskByName(task.name, task)
                        } else {
                            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            val parsedDate = LocalDate.parse(task.date, formatter)
                            val nowDate = LocalDate.now()
                            if (parsedDate.isEqual(nowDate)) {
                                adapter.addTask(task)
                            }
                        }
                    } else if (task.date != "Нет" && task.time != "Нет" && task.status == "") {
                        if (isDateTimeExpired(task.date + " " + task.time)) {
                            task.status = "Просрочено"
                            db.saveTaskByName(task.name, task)
                        } else {
                            val formatterTime = DateTimeFormatter.ofPattern("HH:mm")
                            val formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            val parsedTime = LocalTime.parse(task.time, formatterTime)
                            val parsedDate = LocalDate.parse(task.date, formatterDate)
                            val nowDate = LocalDate.now()
                            val nowTime = LocalTime.now()
                            if (parsedDate.isEqual(nowDate) && parsedTime.isAfter(nowTime)) {
                                adapter.addTask(task)
                            }
                        }
                    }
                }
                db.close()
            }
            "Завтра" -> {
                //Tomorrow tasks
                val db = DbHelper(this, null)
                for (i in 1..db.getRowCount()) {
                    val task = db.getTaskByRowIndex(i - 1)
                    if (task.date == "Нет" && task.status == "") {
                        adapter.addTask(task)
                    } else if (task.date != "Нет" && task.status == "") {
                        if (isDateExpired(task.date)) {
                            task.status = "Просрочено"
                            db.saveTaskByName(task.name, task)
                        } else {
                            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            val parsedDate = LocalDate.parse(task.date, formatter)
                            val tomorrowDate = LocalDate.now().plusDays(1)
                            if (parsedDate.isEqual(tomorrowDate)) {
                                adapter.addTask(task)
                            }
                        }
                    }
                }
                db.close()
            }
            "Следующие 7 дней" -> {
                //Next 7 days tasks
                val db = DbHelper(this, null)
                for (i in 1..db.getRowCount()) {
                    val task = db.getTaskByRowIndex(i - 1)
                    if (task.date == "Нет" && task.status == "") {
                        adapter.addTask(task)
                    } else if (task.date != "Нет" && task.status == "") {
                        if (isDateExpired(task.date)) {
                            task.status = "Просрочено"
                            db.saveTaskByName(task.name, task)
                        } else {
                            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            val parsedDate = LocalDate.parse(task.date, formatter)
                            val nowDate = LocalDate.now()
                            val nextWeekDate = LocalDate.now().plusWeeks(1)
                            if (parsedDate.isBefore(nextWeekDate) || parsedDate.isEqual(nextWeekDate) || parsedDate.isEqual(nowDate)) {
                                adapter.addTask(task)
                            }
                        }
                    }
                }
                db.close()
            }
            "Выполненные" -> {
                //Completed tasks
                val db = DbHelper(this, null)
                for (i in 1..db.getRowCount()) {
                    val task = db.getTaskByRowIndex(i - 1)
                    if (task.status == "Выполнено") {
                        adapter.addTask(task)
                    }
                }
                db.close()
            }
            "Все задачи" -> {
                //All tasks
                val db = DbHelper(this, null)
                for (i in 1..db.getRowCount()) {
                    val task = db.getTaskByRowIndex(i - 1)
                    if (task.date != "Нет" && task.time == "Нет") {
                        if (isDateExpired(task.date) && task.status != "Выполнено") {
                            task.status = "Просрочено"
                            db.saveTaskByName(task.name, task)
                        }
                    }
                    if (task.date != "Нет" && task.time != "Нет") {
                        if (isDateTimeExpired(task.date + " " + task.time) && task.status != "Выполнено") {
                            task.status = "Просрочено"
                            db.saveTaskByName(task.name, task)
                        }
                    }
                    adapter.addTask(task)
                }
                db.close()
            }
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
            val name = (list.getItemAtPosition(i) as Task).name
            AlertDialog.Builder(this)
                .setTitle("Выберите действие")
                .setItems(choose) { _, which ->
                    val db = DbHelper(this, null)
                    when (which) {
                        0 -> {
                            //Complete
                            val completedTask = db.getTaskByName(name)
                            if (completedTask != null) {
                                when (completedTask.repeat) {
                                    "Нет" -> {
                                        completedTask.status = "Выполнено"
                                    }
                                    "Каждый день" -> {
                                        val formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                        val parsedDate = LocalDate.parse(completedTask.date, formatterDate)
                                        completedTask.date = parsedDate.plusDays(1).format(formatterDate).toString()
                                        if (!isDateTimeExpired(completedTask.date + " " + completedTask.time)) {
                                            completedTask.status = ""
                                        }
                                    }
                                    "Каждую неделю" -> {
                                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                        val parsedDate = LocalDate.parse(completedTask.date, formatter)
                                        completedTask.date = parsedDate.plusWeeks(1).format(formatter).toString()
                                        if (!isDateTimeExpired(completedTask.date + " " + completedTask.time)) {
                                            completedTask.status = ""
                                        }
                                    }
                                    "Каждый месяц" -> {
                                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                        val parsedDate = LocalDate.parse(completedTask.date, formatter)
                                        completedTask.date = parsedDate.plusMonths(1).format(formatter).toString()
                                        if (!isDateTimeExpired(completedTask.date + " " + completedTask.time)) {
                                            completedTask.status = ""
                                        }
                                    }
                                }
                                db.saveTaskByName(name, completedTask)
                                Toast.makeText(this, "Задание выполнено!", Toast.LENGTH_SHORT).show()
                                recreate()
                            } else {
                                Toast.makeText(this, "Ошибка: задача не найдена", Toast.LENGTH_SHORT).show()
                                recreate()
                            }
                        }
                        1 -> {
                            //Expire
                            db.deleteTaskByName(name)
                            Toast.makeText(this, "Задание удалено", Toast.LENGTH_SHORT).show()
                            recreate()
                        }
                    }
                    db.close()
                }
                .show()
        }

        list.setOnItemLongClickListener { _, _, i, _ ->
            val db = DbHelper(this, null)
            val item = list.getItemAtPosition(i) as Task
            val editingTask = db.getTaskByName(item.name)
            db.close()
            if (editingTask != null) {
                val intent = Intent(this, TaskCreateActivity::class.java)
                intent.putExtra("is_editing", true)
                intent.putExtra("editingTaskName", editingTask.name)
                intent.putExtra("tasks_text", newTaskText)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Ошибка: задача не найдена", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }
}
