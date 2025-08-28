package com.ivor.todolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
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

public class AddTodoActivity extends AppCompatActivity {
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextCategory;
    private ChipGroup chipGroupPriority;
    private Button btnSelectDate;
    private Button btnSelectTime;
    private TextView textViewSelectedDateTime;
    private Button btnCancel;
    private Button btnSave;
    
    private TodoDAO todoDAO;
    private NotificationHelper notificationHelper;
    private Calendar selectedDateTime;
    private SimpleDateFormat dateTimeFormat;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);
        
        initViews();
        setupToolbar();
        setupClickListeners();
        
        todoDAO = new TodoDAO(this);
        notificationHelper = new NotificationHelper(this);
        selectedDateTime = Calendar.getInstance();
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }
    
    private void initViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextCategory = findViewById(R.id.editTextCategory);
        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        textViewSelectedDateTime = findViewById(R.id.textViewSelectedDateTime);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupClickListeners() {
        btnSelectDate.setOnClickListener(v -> showDatePicker());
        btnSelectTime.setOnClickListener(v -> showTimePicker());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveTodo());
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);
                    updateDateTimeDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }
    
    private void updateDateTimeDisplay() {
        String dateTimeText = "截止时间: " + dateTimeFormat.format(selectedDateTime.getTime());
        textViewSelectedDateTime.setText(dateTimeText);
        textViewSelectedDateTime.setTextColor(getResources().getColor(android.R.color.black));
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
        
        int priority = getSelectedPriority();
        
        Todo todo = new Todo(title, description);
        todo.setPriority(priority);
        todo.setCategory(category);
        
        // 检查是否设置了截止时间
        if (!textViewSelectedDateTime.getText().toString().equals("未设置截止时间")) {
            todo.setDueDate(selectedDateTime.getTime());
        }
        
        todoDAO.open();
        long result = todoDAO.insertTodo(todo);
        todoDAO.close();
        
        if (result != -1) {
            // 设置通知提醒
            todo.setId(result);
            if (todo.getDueDate() != null) {
                notificationHelper.scheduleNotification(todo);
            }
            
            Toast.makeText(this, "任务添加成功", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "任务添加失败", Toast.LENGTH_SHORT).show();
        }
    }
    
    private int getSelectedPriority() {
        int checkedChipId = chipGroupPriority.getCheckedChipId();
        if (checkedChipId == R.id.chipHighPriority) {
            return 1; // 高优先级
        } else if (checkedChipId == R.id.chipMediumPriority) {
            return 2; // 中优先级
        } else if (checkedChipId == R.id.chipLowPriority) {
            return 3; // 低优先级
        }
        return 2; // 默认中优先级
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