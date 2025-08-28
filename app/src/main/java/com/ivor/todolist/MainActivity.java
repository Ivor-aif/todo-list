package com.ivor.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ivor.todolist.adapter.TodoAdapter;
import com.ivor.todolist.database.TodoDAO;
import com.ivor.todolist.model.Todo;
import com.ivor.todolist.notification.NotificationHelper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TodoAdapter.OnTodoItemClickListener {
    private static final int REQUEST_ADD_TODO = 1;
    private static final int REQUEST_EDIT_TODO = 2;
    
    private RecyclerView recyclerViewTodos;
    private TodoAdapter todoAdapter;
    private TextView textViewEmpty;
    private ChipGroup chipGroupFilter;
    private TodoDAO todoDAO;
    private NotificationHelper notificationHelper;
    private List<Todo> allTodos;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilterChips();
        setupFab();
        
        todoDAO = new TodoDAO(this);
        notificationHelper = new NotificationHelper(this);
        loadTodos();
    }
    
    private void initViews() {
        recyclerViewTodos = findViewById(R.id.recyclerViewTodos);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Todo List");
        }
    }
    
    private void setupRecyclerView() {
        todoAdapter = new TodoAdapter(this);
        todoAdapter.setOnTodoItemClickListener(this);
        recyclerViewTodos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTodos.setAdapter(todoAdapter);
    }
    
    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipAll) {
                    todoAdapter.filterTodos(TodoAdapter.FilterType.ALL);
                } else if (checkedId == R.id.chipIncomplete) {
                    todoAdapter.filterTodos(TodoAdapter.FilterType.INCOMPLETE);
                } else if (checkedId == R.id.chipCompleted) {
                    todoAdapter.filterTodos(TodoAdapter.FilterType.COMPLETED);
                }
                updateEmptyView();
            }
        });
        
        // 默认选中"全部"
        ((Chip) findViewById(R.id.chipAll)).setChecked(true);
    }
    
    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fabAddTodo);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
            startActivityForResult(intent, REQUEST_ADD_TODO);
        });
    }
    
    private void loadTodos() {
        todoDAO.open();
        allTodos = todoDAO.getAllTodos();
        todoAdapter.setTodoList(allTodos);
        updateEmptyView();
    }
    
    private void updateEmptyView() {
        if (todoAdapter.isEmpty()) {
            textViewEmpty.setVisibility(View.VISIBLE);
            recyclerViewTodos.setVisibility(View.GONE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
            recyclerViewTodos.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            showSortMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showSortMenu() {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_sort));
        popupMenu.getMenuInflater().inflate(R.menu.menu_sort, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.sort_by_date_created) {
                sortTodos(Comparator.comparing(Todo::getCreatedAt));
            } else if (itemId == R.id.sort_by_due_date) {
                sortTodos(Comparator.comparing(Todo::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
            } else if (itemId == R.id.sort_by_priority) {
                sortTodos(Comparator.comparing(Todo::getPriority));
            } else if (itemId == R.id.sort_by_title) {
                sortTodos(Comparator.comparing(Todo::getTitle, String.CASE_INSENSITIVE_ORDER));
            }
            return true;
        });
        
        popupMenu.show();
    }
    
    private void sortTodos(Comparator<Todo> comparator) {
        Collections.sort(allTodos, comparator);
        todoAdapter.setTodoList(allTodos);
        updateEmptyView();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_ADD_TODO || requestCode == REQUEST_EDIT_TODO) {
                loadTodos(); // 重新加载数据
            }
        }
    }
    
    // TodoAdapter.OnTodoItemClickListener 接口实现
    @Override
    public void onTodoClick(Todo todo) {
        // 点击todo项，进入编辑界面
        Intent intent = new Intent(this, EditTodoActivity.class);
        intent.putExtra("todo_id", todo.getId());
        startActivityForResult(intent, REQUEST_EDIT_TODO);
    }
    
    @Override
    public void onTodoLongClick(Todo todo) {
        // 长按todo项，可以显示更多选项
        showTodoOptionsMenu(todo);
    }
    
    @Override
    public void onCheckboxClick(Todo todo, boolean isChecked) {
        // 更新完成状态
        todo.setCompleted(isChecked);
        todoDAO.open();
        todoDAO.updateTodo(todo);
        todoAdapter.updateTodo(todo);
        
        // 更新通知提醒
        notificationHelper.updateNotification(todo);
    }
    
    @Override
    public void onMoreClick(Todo todo, View view) {
        // 显示更多选项菜单
        showTodoOptionsMenu(todo, view);
    }
    
    private void showTodoOptionsMenu(Todo todo) {
        showTodoOptionsMenu(todo, null);
    }
    
    private void showTodoOptionsMenu(Todo todo, View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView != null ? anchorView : recyclerViewTodos);
        popupMenu.getMenuInflater().inflate(R.menu.menu_todo_options, popupMenu.getMenu());
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                 // 编辑todo
                 Intent intent = new Intent(this, EditTodoActivity.class);
                 intent.putExtra("todo_id", todo.getId());
                 startActivityForResult(intent, REQUEST_EDIT_TODO);
            } else if (itemId == R.id.action_delete) {
                // 删除todo
                deleteTodo(todo);
            } else if (itemId == R.id.action_toggle_complete) {
                // 切换完成状态
                todo.setCompleted(!todo.isCompleted());
                todoDAO.open();
                todoDAO.updateTodo(todo);
                todoAdapter.updateTodo(todo);
                
                // 更新通知提醒
                notificationHelper.updateNotification(todo);
            }
            return true;
        });
        
        popupMenu.show();
    }
    
    private void deleteTodo(Todo todo) {
        // 取消通知提醒
        notificationHelper.cancelNotification(todo.getId());
        
        todoDAO.open();
        todoDAO.deleteTodo(todo.getId());
        todoAdapter.removeTodo(todo);
        updateEmptyView();
    }
}