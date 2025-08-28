package com.ivor.todolist.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TodoDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "todo_database.db";
    private static final int DATABASE_VERSION = 1;

    // 表名
    public static final String TABLE_TODOS = "todos";

    // 列名
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_IS_COMPLETED = "is_completed";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_DUE_DATE = "due_date";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_CATEGORY = "category";

    // 创建表的SQL语句
    private static final String CREATE_TABLE_TODOS = "CREATE TABLE " + TABLE_TODOS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0, " +
            COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
            COLUMN_DUE_DATE + " INTEGER, " +
            COLUMN_PRIORITY + " INTEGER DEFAULT 2, " +
            COLUMN_CATEGORY + " TEXT" +
            ")";

    private static TodoDatabaseHelper instance;

    public static synchronized TodoDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TodoDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private TodoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TODOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 简单的升级策略：删除旧表，创建新表
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}