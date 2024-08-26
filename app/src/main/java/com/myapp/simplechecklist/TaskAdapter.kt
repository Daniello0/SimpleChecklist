package com.myapp.simplechecklist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(context: Context, tasks: MutableList<Task>) : ArrayAdapter<Task>(context, 0, tasks) {

    private val taskList: MutableList<Task> = tasks

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val task = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.simple_checklist_list_view, parent, false)

        val nameTextView: TextView = view.findViewById(R.id.textListName)
        val dateTextView: TextView = view.findViewById(R.id.textListDate)
        val timeTextView: TextView = view.findViewById(R.id.textListTime)
        val priorityTextView: TextView = view.findViewById(R.id.textListPriority)
        val repeatTextView: TextView = view.findViewById(R.id.textListRepeat)
        val statusTextView: TextView = view.findViewById(R.id.textListStatus)

        nameTextView.text = task?.name
        dateTextView.text = task?.date
        timeTextView.text = task?.time
        priorityTextView.text = "Приоритет: ${task?.priority}"
        repeatTextView.text =  "Повтор: ${task?.repeat}"
        statusTextView.text = task?.status

        nameTextView.setTextColor(Color.parseColor(task?.color))

        when (statusTextView.text) {
            "" -> {
                view.setBackgroundColor(Color.argb(77, 255, 255, 0))
                nameTextView.paintFlags = nameTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            "Просрочено" -> {
                view.setBackgroundColor(Color.argb(77, 255, 0, 0))
                nameTextView.paintFlags = nameTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                statusTextView.setTextColor(Color.RED)
            }
            "Выполнено" -> {
                view.setBackgroundColor(Color.argb(77, 0, 255, 0))
                nameTextView.paintFlags = nameTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                statusTextView.setTextColor(Color.GREEN)
            }
        }

        return view
    }

    fun addTask(task: Task) {
        taskList.add(task)
        notifyDataSetChanged()
    }

    fun sortTasksByDateTime() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US)

        taskList.sortWith(compareBy(
            { task ->
                // Сортировка по наличию даты: задачи с датой идут выше задач без даты
                if (task.date != "Нет") 0 else 1
            },
            { task ->
                // Сортировка по наличию времени: задачи с временем выше задач только с датой
                if (task.time != "Нет") 0 else 1
            },
            { task ->
                // Сортировка по дате (и времени, если есть)
                try {
                    when {
                        task.date != "Нет" && task.time != "Нет" -> dateTimeFormat.parse("${task.date} ${task.time}")
                        task.date != "Нет" -> dateFormat.parse(task.date)
                        else -> Date(Long.MAX_VALUE) // Максимальная дата для задач без даты
                    }
                } catch (e: Exception) {
                    Date(Long.MAX_VALUE) // Максимальная дата в случае ошибки парсинга
                }
            }
        ))
        notifyDataSetChanged()
    }




    fun sortTasksByPriority() {
        taskList.sortWith(compareByDescending { task ->
            when (task.priority) {
                "Высокий" -> 3
                "Средний" -> 2
                "Низкий" -> 1
                else -> 0
            }
        })
        notifyDataSetChanged()
    }

    fun sortTasksByColor() {
        taskList.sortWith(compareByDescending { task ->
            when (task.color) {
                "Red" -> 3
                "Blue" -> 2
                "Green" -> 1
                else -> 0
            }
        })
        notifyDataSetChanged()
    }
}
