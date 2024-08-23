package com.myapp.simplechecklist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "tasks", factory, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE tasks (id INT PRIMARY KEY, name TEXT, date TEXT, time TEXT, " +
                "color TEXT, priority TEXT, repeat TEXT, description TEXT, status TEXT)"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS tasks")
        onCreate(db)
    }

    fun addTask(task: Task) {
        val values = ContentValues()
        values.put("name", task.name)
        values.put("date", task.date)
        values.put("time", task.time)
        values.put("color", task.color)
        values.put("priority", task.priority)
        values.put("repeat", task.repeat)
        values.put("description", task.description)
        values.put("status", task.status)

        val db = this.writableDatabase
        db.insert("tasks", null, values)

        db.close()
    }

    fun getRowCount(): Int {
        val db = this.readableDatabase
        val cursor = db.query("tasks", null, null, null, null, null, null)
        val count = cursor.count
        cursor.close()
        db.close()
        return count
    }

    fun getTaskByRowIndex(rowIndex: Int): Task {
        val db = this.readableDatabase
        val cursor = db.query(
            "tasks", null, null, null,
            null, null, null
        )

        if (cursor != null && cursor.moveToPosition(rowIndex)) {
            val task = Task(
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                time = cursor.getString(cursor.getColumnIndexOrThrow("time")),
                color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                priority = cursor.getString(cursor.getColumnIndexOrThrow("priority")),
                repeat = cursor.getString(cursor.getColumnIndexOrThrow("repeat")),
                description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            )
            cursor.close()
            db.close()
            return task
        } else {
            cursor?.close()
            db.close()
            throw IllegalArgumentException("Task not found at rowIndex: $rowIndex")
        }
    }


    fun getTaskByName(name: String): Task? {
        val db = this.readableDatabase
        val cursor = db.query(
            "tasks", null, "name = ?", arrayOf(name),
            null, null, null
        )

        return if (cursor != null && cursor.moveToFirst()) {
            val task = Task(
                name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                time = cursor.getString(cursor.getColumnIndexOrThrow("time")),
                color = cursor.getString(cursor.getColumnIndexOrThrow("color")),
                priority = cursor.getString(cursor.getColumnIndexOrThrow("priority")),
                repeat = cursor.getString(cursor.getColumnIndexOrThrow("repeat")),
                description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            )
            cursor.close()
            db.close()
            task
        } else {
            cursor?.close()
            db.close()
            null
        }
    }

    fun deleteTaskByName(name: String) {
        val db = this.writableDatabase
        db.delete("tasks", "name = ?", arrayOf(name))
        db.close()
    }

    fun saveTaskByName(name: String, updatedTask: Task) {
        val values = ContentValues()
        values.put("name", updatedTask.name)
        values.put("date", updatedTask.date)
        values.put("time", updatedTask.time)
        values.put("color", updatedTask.color)
        values.put("priority", updatedTask.priority)
        values.put("repeat", updatedTask.repeat)
        values.put("description", updatedTask.description)
        values.put("status", updatedTask.status)

        val db = this.writableDatabase
        db.update("tasks", values, "name = ?", arrayOf(name))
        db.close()
    }

}