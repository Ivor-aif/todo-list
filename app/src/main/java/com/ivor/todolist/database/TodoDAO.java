package com.ivor.todolist.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ivor.todolist.model.Todo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TodoDAO {
    private TodoDatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public TodoDAO(Context context) {
        dbHelper = TodoDatabaseHelper.getInstance(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    // 确保数据库已打开
    private void ensureDatabaseOpen() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
    }

    // 插入新的Todo
    public long insertTodo(Todo todo) {
        ensureDatabaseOpen();
        ContentValues values = new ContentValues();
        values.put(TodoDatabaseHelper.COLUMN_TITLE, todo.getTitle());
        values.put(TodoDatabaseHelper.COLUMN_DESCRIPTION, todo.getDescription());
        values.put(TodoDatabaseHelper.COLUMN_IS_COMPLETED, todo.isCompleted() ? 1 : 0);
        values.put(TodoDatabaseHelper.COLUMN_CREATED_AT, todo.getCreatedAt().getTime());
        if (todo.getDueDate() != null) {
            values.put(TodoDatabaseHelper.COLUMN_DUE_DATE, todo.getDueDate().getTime());
        }
        values.put(TodoDatabaseHelper.COLUMN_PRIORITY, todo.getPriority());
        values.put(TodoDatabaseHelper.COLUMN_CATEGORY, todo.getCategory());

        long id = database.insert(TodoDatabaseHelper.TABLE_TODOS, null, values);
        todo.setId(id);
        return id;
    }

    // 更新Todo
    public int updateTodo(Todo todo) {
        ensureDatabaseOpen();
        ContentValues values = new ContentValues();
        values.put(TodoDatabaseHelper.COLUMN_TITLE, todo.getTitle());
        values.put(TodoDatabaseHelper.COLUMN_DESCRIPTION, todo.getDescription());
        values.put(TodoDatabaseHelper.COLUMN_IS_COMPLETED, todo.isCompleted() ? 1 : 0);
        values.put(TodoDatabaseHelper.COLUMN_CREATED_AT, todo.getCreatedAt().getTime());
        if (todo.getDueDate() != null) {
            values.put(TodoDatabaseHelper.COLUMN_DUE_DATE, todo.getDueDate().getTime());
        } else {
            values.putNull(TodoDatabaseHelper.COLUMN_DUE_DATE);
        }
        values.put(TodoDatabaseHelper.COLUMN_PRIORITY, todo.getPriority());
        values.put(TodoDatabaseHelper.COLUMN_CATEGORY, todo.getCategory());

        return database.update(TodoDatabaseHelper.TABLE_TODOS, values,
                TodoDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(todo.getId())});
    }

    // 删除Todo
    public int deleteTodo(long id) {
        ensureDatabaseOpen();
        return database.delete(TodoDatabaseHelper.TABLE_TODOS,
                TodoDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // 根据ID获取Todo
    public Todo getTodoById(long id) {
        ensureDatabaseOpen();
        Cursor cursor = database.query(TodoDatabaseHelper.TABLE_TODOS,
                null,
                TodoDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        Todo todo = null;
        if (cursor != null && cursor.moveToFirst()) {
            todo = cursorToTodo(cursor);
            cursor.close();
        }
        return todo;
    }

    // 获取所有Todo
    public List<Todo> getAllTodos() {
        ensureDatabaseOpen();
        List<Todo> todos = new ArrayList<>();
        Cursor cursor = database.query(TodoDatabaseHelper.TABLE_TODOS,
                null, null, null, null, null,
                TodoDatabaseHelper.COLUMN_CREATED_AT + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                todos.add(cursorToTodo(cursor));
            }
            cursor.close();
        }
        return todos;
    }

    // 获取未完成的Todo
    public List<Todo> getIncompleteTodos() {
        ensureDatabaseOpen();
        List<Todo> todos = new ArrayList<>();
        Cursor cursor = database.query(TodoDatabaseHelper.TABLE_TODOS,
                null,
                TodoDatabaseHelper.COLUMN_IS_COMPLETED + " = ?",
                new String[]{"0"},
                null, null,
                TodoDatabaseHelper.COLUMN_PRIORITY + " ASC, " + TodoDatabaseHelper.COLUMN_DUE_DATE + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                todos.add(cursorToTodo(cursor));
            }
            cursor.close();
        }
        return todos;
    }

    // 获取已完成的Todo
    public List<Todo> getCompletedTodos() {
        ensureDatabaseOpen();
        List<Todo> todos = new ArrayList<>();
        Cursor cursor = database.query(TodoDatabaseHelper.TABLE_TODOS,
                null,
                TodoDatabaseHelper.COLUMN_IS_COMPLETED + " = ?",
                new String[]{"1"},
                null, null,
                TodoDatabaseHelper.COLUMN_CREATED_AT + " DESC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                todos.add(cursorToTodo(cursor));
            }
            cursor.close();
        }
        return todos;
    }

    // 根据优先级获取Todo
    public List<Todo> getTodosByPriority(int priority) {
        ensureDatabaseOpen();
        List<Todo> todos = new ArrayList<>();
        Cursor cursor = database.query(TodoDatabaseHelper.TABLE_TODOS,
                null,
                TodoDatabaseHelper.COLUMN_PRIORITY + " = ?",
                new String[]{String.valueOf(priority)},
                null, null,
                TodoDatabaseHelper.COLUMN_DUE_DATE + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                todos.add(cursorToTodo(cursor));
            }
            cursor.close();
        }
        return todos;
    }

    // 标记Todo为完成
    public int markTodoAsCompleted(long id) {
        ensureDatabaseOpen();
        ContentValues values = new ContentValues();
        values.put(TodoDatabaseHelper.COLUMN_IS_COMPLETED, 1);
        return database.update(TodoDatabaseHelper.TABLE_TODOS, values,
                TodoDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // 标记Todo为未完成
    public int markTodoAsIncomplete(long id) {
        ensureDatabaseOpen();
        ContentValues values = new ContentValues();
        values.put(TodoDatabaseHelper.COLUMN_IS_COMPLETED, 0);
        return database.update(TodoDatabaseHelper.TABLE_TODOS, values,
                TodoDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // 将Cursor转换为Todo对象
    private Todo cursorToTodo(Cursor cursor) {
        Todo todo = new Todo();
        todo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_ID)));
        todo.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_TITLE)));
        todo.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_DESCRIPTION)));
        todo.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_IS_COMPLETED)) == 1);
        todo.setCreatedAt(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_CREATED_AT))));
        
        long dueDateLong = cursor.getLong(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_DUE_DATE));
        if (dueDateLong > 0) {
            todo.setDueDate(new Date(dueDateLong));
        }
        
        todo.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_PRIORITY)));
        todo.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(TodoDatabaseHelper.COLUMN_CATEGORY)));
        
        return todo;
    }
}