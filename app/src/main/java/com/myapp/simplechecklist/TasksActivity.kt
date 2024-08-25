package com.myapp.simplechecklist

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
fun updateListData(taskText: String, adapter: TaskAdapter, db: DbHelper) {
    adapter.clear()
    when (taskText) {
        "Сегодня" -> {
            //Today tasks
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
}

class TasksActivity : AppCompatActivity() {
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasks)

        val taskText: TextView = findViewById(R.id.textTasks)
        var newTaskText = intent.getSerializableExtra("tasks_text")
        val textSort: TextView = findViewById(R.id.textSort)
        if (newTaskText == null)
            newTaskText = "Сегодня"
        taskText.text = newTaskText.toString()
        val buttonAddTask: Button = findViewById(R.id.buttonNewTask)
        val list: ListView = findViewById(R.id.listView)
        val todos: MutableList<Task> = mutableListOf()
        val adapter = TaskAdapter(this, todos)
        list.adapter = adapter

        textSort.setOnClickListener {
            val sorts = arrayOf("Дата и время", "Цвет", "Приоритет")
            AlertDialog.Builder(this)
                .setTitle("Выберите сортировку")
                .setItems(sorts) {_, which ->
                    textSort.text = sorts[which]
                }
                .show()
        }

        taskText.setOnClickListener {
            val filters = arrayOf("Сегодня", "Завтра", "Следующие 7 дней", "Выполненные", "Все задачи")
            AlertDialog.Builder(this)
                .setTitle("Выберите фильтр")
                .setItems(filters) {_, which ->
                    newTaskText = filters[which]
                    val db = DbHelper(this, null)
                    updateListData(newTaskText.toString(), adapter, db)
                    db.close()
                    taskText.text = newTaskText.toString()
                }
                .show()
        }

        if (true) {
            val db = DbHelper(this, null)
            updateListData(newTaskText.toString(), adapter, db)
            db.close()
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
                                        if (completedTask.time != "Нет") {
                                            if (!isDateTimeExpired(completedTask.date + " " + completedTask.time)) {
                                                completedTask.status = ""
                                            }
                                        } else {
                                            if (!isDateExpired(completedTask.date)) {
                                                completedTask.status = ""
                                            }
                                        }
                                    }
                                    "Каждую неделю" -> {
                                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                        val parsedDate = LocalDate.parse(completedTask.date, formatter)
                                        completedTask.date = parsedDate.plusWeeks(1).format(formatter).toString()
                                        if (completedTask.time != "Нет") {
                                            if (!isDateTimeExpired(completedTask.date + " " + completedTask.time)) {
                                                completedTask.status = ""
                                            }
                                        } else {
                                            if (!isDateExpired(completedTask.date)) {
                                                completedTask.status = ""
                                            }
                                        }
                                    }
                                    "Каждый месяц" -> {
                                        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                        val parsedDate = LocalDate.parse(completedTask.date, formatter)
                                        completedTask.date = parsedDate.plusMonths(1).format(formatter).toString()
                                        if (completedTask.time != "Нет") {
                                            if (!isDateTimeExpired(completedTask.date + " " + completedTask.time)) {
                                                completedTask.status = ""
                                            }
                                        } else {
                                            if (!isDateExpired(completedTask.date)) {
                                                completedTask.status = ""
                                            }
                                        }
                                    }
                                }
                                db.saveTaskByName(name, completedTask)
                                Toast.makeText(this, "Задание выполнено!", Toast.LENGTH_SHORT).show()
                                updateListData(newTaskText.toString(), adapter, db)
                            } else {
                                Toast.makeText(this, "Ошибка: задача не найдена", Toast.LENGTH_SHORT).show()
                                updateListData(newTaskText.toString(), adapter, db)
                            }
                        }
                        1 -> {
                            //Delete
                            db.deleteTaskByName(name)
                            Toast.makeText(this, "Задание удалено", Toast.LENGTH_SHORT).show()
                            updateListData(newTaskText.toString(), adapter, db)
                        }
                    }
                    db.close()
                }
                .show()
        }

        list.setOnItemLongClickListener { _, _, i, _ ->
            val item = list.getItemAtPosition(i) as Task
            val db = DbHelper(this, null)
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
