package com.ivor.todolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.ivor.todolist.database.TodoDAO;
import com.ivor.todolist.model.Todo;
import com.ivor.todolist.notification.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditTodoActivity extends AppCompatActivity {
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextCategory;
    private ChipGroup chipGroupPriority;
    private Button btnSelectDate;
    private Button btnSelectTime;
    private Button btnClearDateTime;
    private Button btnCancel;
    private Button btnSave;
    private TextView textViewSelectedDateTime;
    
    private TodoDAO todoDAO;
    private NotificationHelper notificationHelper;
    private Todo currentTodo;
    private Calendar selectedDateTime;
    private SimpleDateFormat dateTimeFormat;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_todo);
        
        initViews();
        setupToolbar();
        setupClickListeners();
        
        todoDAO = new TodoDAO(this);
        notificationHelper = new NotificationHelper(this);
        dateTimeFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());
        selectedDateTime = Calendar.getInstance();
        
        loadTodoData();
    }
    
    private void initViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextCategory = findViewById(R.id.editTextCategory);
        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnClearDateTime = findViewById(R.id.btnClearDateTime);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        textViewSelectedDateTime = findViewById(R.id.textViewSelectedDateTime);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("编辑任务");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnClearDateTime.setOnClickListener(v -> clearDateTime());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTodo());
    }
    
    private void loadTodoData() {
        long todoId = getIntent().getLongExtra("todo_id", -1);
        if (todoId != -1) {
            todoDAO.open();
            currentTodo = todoDAO.getTodoById(todoId);
            if (currentTodo != null) {
                populateFields();
            } else {
                Toast.makeText(this, "无法加载任务数据", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "无效的任务ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void populateFields() {
        editTextTitle.setText(currentTodo.getTitle());
        editTextDescription.setText(currentTodo.getDescription());
        editTextCategory.setText(currentTodo.getCategory());
        
        // 设置优先级
        switch (currentTodo.getPriority()) {
            case 1:
                ((Chip) findViewById(R.id.chipHigh)).setChecked(true);
                break;
            case 2:
                ((Chip) findViewById(R.id.chipMedium)).setChecked(true);
                break;
            case 3:
                ((Chip) findViewById(R.id.chipLow)).setChecked(true);
                break;
        }
        
        // 设置截止时间
        if (currentTodo.getDueDate() != null) {
            selectedDateTime.setTime(currentTodo.getDueDate());
            updateDateTimeDisplay();
        } else {
            textViewSelectedDateTime.setText("未设置截止时间");
        }
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateTimeDisplay();
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                selectedDateTime.set(Calendar.SECOND, 0);
                updateDateTimeDisplay();
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }
    
    private void updateDateTimeDisplay() {
        String dateTimeText = dateTimeFormat.format(selectedDateTime.getTime());
        textViewSelectedDateTime.setText("截止时间: " + dateTimeText);
    }
    
    private void clearDateTime() {
        selectedDateTime = null;
        textViewSelectedDateTime.setText("未设置截止时间");
    }
    
    private int getSelectedPriority() {
        int checkedChipId = chipGroupPriority.getCheckedChipId();
        if (checkedChipId == R.id.chipHigh) {
            return 1;
        } else if (checkedChipId == R.id.chipMedium) {
            return 2;
        } else if (checkedChipId == R.id.chipLow) {
            return 3;
        }
        return 2; // 默认中优先级
    }
    
    private void saveTodo() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String category = editTextCategory.getText().toString().trim();
        
        if (TextUtils.isEmpty(title)) {
            editTextTitle.setError("请输入任务标题");
            editTextTitle.requestFocus();
            return;
        }
        
        // 更新Todo对象
        currentTodo.setTitle(title);
        currentTodo.setDescription(description);
        currentTodo.setCategory(category);
        currentTodo.setPriority(getSelectedPriority());
        
        if (selectedDateTime != null) {
            currentTodo.setDueDate(selectedDateTime.getTime());
        } else {
            currentTodo.setDueDate(null);
        }
        
        // 保存到数据库
        todoDAO.open();
        int rowsAffected = todoDAO.updateTodo(currentTodo);
        
        if (rowsAffected > 0) {
            // 更新通知提醒
            notificationHelper.updateNotification(currentTodo);
            
            Toast.makeText(this, "任务已更新", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "更新失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}