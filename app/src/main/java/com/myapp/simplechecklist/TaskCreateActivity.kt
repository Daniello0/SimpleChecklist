package com.myapp.simplechecklist

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun getColor(view: View): String? {
    val background = view.background
    if (background is ColorDrawable) {
        val colorInt = background.color

        val red = Color.red(colorInt)
        val green = Color.green(colorInt)
        val blue = Color.blue(colorInt)

        // Определяем, какая компонента преобладает
        return when {
            red > green && red > blue -> "Red"
            green > red && green > blue -> "Green"
            blue > red && blue > green -> "Blue"
            green > blue && red > blue || green == red -> "Yellow"
            else -> "Cannot determine"
        }
    }
    return null
}

@SuppressLint("NewApi")
fun isDateExpired(dateString: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val parsedDate = LocalDate.parse(dateString, formatter)
    val currentDate = LocalDate.now()
    return currentDate.isAfter(parsedDate)
}

@SuppressLint("NewApi")
fun isDateTimeExpired(dateTimeString: String): Boolean {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val parsedDateTime = LocalDateTime.parse(dateTimeString, formatter)
        val currentDateTime = LocalDateTime.now()
        return currentDateTime.isAfter(parsedDateTime)
}

class TaskCreateActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_create)

        val textDate: TextView = findViewById(R.id.textDatePicker)
        val textTime: TextView = findViewById(R.id.textTimePicker)
        val buttonBack: Button = findViewById(R.id.buttonBack)
        @Suppress("DEPRECATION") val newTaskText = intent.getSerializableExtra("tasks_text")
        val viewColor: View = findViewById(R.id.viewColor)
        viewColor.setBackgroundColor(Color.GREEN)
        val textPriority: TextView = findViewById(R.id.textPriorityPicker)
        val buttonAddTask: Button = findViewById(R.id.buttonAddTask)
        val description: EditText = findViewById(R.id.editMultiLineTextDescription)
        val textName: EditText = findViewById(R.id.textName)
        val textRepeat: TextView = findViewById(R.id.textRepeatPicker)
        @Suppress("DEPRECATION") val isEditing = intent.getSerializableExtra("is_editing")
        @Suppress("DEPRECATION") val editingTaskName = intent.getSerializableExtra("editingTaskName")

        val cal = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            textDate.text = sdf.format(cal.time)
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minute)

            val myFormat = "HH:mm"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            textTime.text = sdf.format(cal.time)
        }

        textDate.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        textDate.setOnLongClickListener {
            textDate.text = "Нет"
            true
        }

        textTime.setOnClickListener {
            TimePickerDialog(this, timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), true).show()
        }

        textTime.setOnLongClickListener {
            textTime.text = "Нет"
            true
        }

        viewColor.setOnClickListener {
            val colors = arrayOf("Зеленый", "Синий", "Желтый", "Красный")
            val colorValues = arrayOf(Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED)
            AlertDialog.Builder(this)
                .setTitle("Выберите цвет")
                .setItems(colors) { dialog, which ->
                    viewColor.setBackgroundColor(colorValues[which])
                }
                .show()

        }

        textPriority.setOnClickListener {
            val priorities = arrayOf("Сделать", "Запланировать", "Делегировать", "Удалить")
            AlertDialog.Builder(this)
                .setTitle("Выберите приоритет")
                .setItems(priorities) { dialog, which ->
                    textPriority.text = priorities[which]
                }
                .show()
        }

        textRepeat.setOnClickListener {
            val repeats = arrayOf("Нет", "Каждый день", "Каждую неделю", "Каждый месяц")
            AlertDialog.Builder(this)
                .setTitle("Выберите повтор")
                .setItems(repeats) { dialog, which ->
                    textRepeat.text = repeats[which]
                }
                .show()
        }

        if (isEditing != null) {
            val db = DbHelper(this, null)
            buttonAddTask.text = "Сохранить"
            val editingTask = db.getTaskByName(editingTaskName.toString())
            db.close()
            textName.setText(editingTask!!.name)
            textDate.text = editingTask.date
            textTime.text = editingTask.time
            viewColor.setBackgroundColor(editingTask.color.toColorInt())
            textPriority.text = editingTask.priority
            textRepeat.text = editingTask.repeat
            description.setText(editingTask.description)
        }

        buttonAddTask.setOnClickListener {
            val db = DbHelper(this, null)
            if (textName.text.toString() == "") {
                Toast.makeText(this, "Введите название задачи", Toast.LENGTH_SHORT).show()
            } else if (textDate.text.toString() == "Нет" && textTime.text.toString() != "Нет") {
                Toast.makeText(this, "Введите дату", Toast.LENGTH_SHORT).show()
            } else if (db.getTaskByName(textName.text.toString()) != null && isEditing == null) {
                Toast.makeText(this, "Задание с таким именем уже существует", Toast.LENGTH_SHORT).show()
            } else if (textRepeat.text != "Нет" && textDate.text == "Нет") {
                    Toast.makeText(this, "Для повтора задачи необходима дата", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this, TasksActivity::class.java)
                    intent.putExtra("tasks_text", newTaskText)

                    val task = Task(textName.text.toString().trim(), textDate.text.toString(),
                        textTime.text.toString(), getColor(viewColor).toString(), textPriority.text.toString(),
                        textRepeat.text.toString(), description.text.toString(), status = "")

                    if (textDate.text.toString() == "Нет" && textTime.text.toString() == "Нет") {
                        task.status = ""
                    } else if (textTime.text.toString() == "Нет" && textDate.text.toString() != "Нет") {
                        if (isDateExpired(textDate.text.toString()))
                            task.status = "Просрочено"
                        else task.status = ""
                    } else if (textDate.text.toString() != "Нет" && textTime.text.toString() != "Нет") {
                        if (isDateTimeExpired(textDate.text.toString() + " " + textTime.text.toString()))
                            task.status = "Просрочено"
                        else task.status = ""
                    }

                    if (isEditing != null) {
                        val db = DbHelper(this, null)
                        db.deleteTaskByName(editingTaskName.toString())
                        db.addTask(task)
                        db.close()

                        Toast.makeText(this, "Изменения сохранены", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                    } else {
                        val db = DbHelper(this, null)
                        db.addTask(task)
                        db.close()

                        Toast.makeText(this, "Задача добавлена", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                    }
                }
        }

        buttonBack.setOnClickListener {
            val intent = Intent(this, TasksActivity::class.java)
            intent.putExtra("tasks_text", newTaskText)
            startActivity(intent)
        }
    }
}