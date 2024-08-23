package com.myapp.simplechecklist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(context: Context, tasks: MutableList<Task>) : ArrayAdapter<Task>(context, 0, tasks) {

    // Список задач
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
                view.setBackgroundDrawable(ColorDrawable(Color.argb(38, 255, 255, 0)))            }
            "Просрочено" -> {
                view.setBackgroundDrawable(ColorDrawable(Color.argb(38, 255, 0, 0)))
                statusTextView.setTextColor(Color.RED)
            }
            "Выполнено" -> {
                view.setBackgroundDrawable(ColorDrawable(Color.argb(38, 0, 255, 0)))
                nameTextView.paintFlags = nameTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                statusTextView.setTextColor(Color.GREEN)
            }
        }

        return view
    }

    // Метод добавления задачи
    fun addTask(task: Task) {
        taskList.add(task)
        sortTasksByDateTime()
        notifyDataSetChanged() // Обновляем список
    }

    // Метод сортировки задач по дате и времени
    private fun sortTasksByDateTime() {
        val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        taskList.sortWith(compareBy {
            try {
                dateTimeFormat.parse("${it.date} ${it.time}") ?: Date(0)
            } catch (e: Exception) {
                Date(0)
            }
        })
    }
}
