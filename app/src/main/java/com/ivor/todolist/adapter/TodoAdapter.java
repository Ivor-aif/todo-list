package com.ivor.todolist.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.ivor.todolist.R;
import com.ivor.todolist.model.Todo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private Context context;
    private List<Todo> todoList;
    private List<Todo> filteredTodoList;
    private OnTodoItemClickListener listener;
    private SimpleDateFormat dateTimeFormat;
    
    public interface OnTodoItemClickListener {
        void onTodoClick(Todo todo);
        void onTodoLongClick(Todo todo);
        void onCheckboxClick(Todo todo, boolean isChecked);
        void onMoreClick(Todo todo, View view);
    }
    
    public TodoAdapter(Context context) {
        this.context = context;
        this.todoList = new ArrayList<>();
        this.filteredTodoList = new ArrayList<>();
        this.dateTimeFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    }
    
    public void setOnTodoItemClickListener(OnTodoItemClickListener listener) {
        this.listener = listener;
    }
    
    public void setTodoList(List<Todo> todoList) {
        this.todoList = todoList;
        this.filteredTodoList = new ArrayList<>(todoList);
        notifyDataSetChanged();
    }
    
    public void filterTodos(FilterType filterType) {
        filteredTodoList.clear();
        switch (filterType) {
            case ALL:
                filteredTodoList.addAll(todoList);
                break;
            case INCOMPLETE:
                for (Todo todo : todoList) {
                    if (!todo.isCompleted()) {
                        filteredTodoList.add(todo);
                    }
                }
                break;
            case COMPLETED:
                for (Todo todo : todoList) {
                    if (todo.isCompleted()) {
                        filteredTodoList.add(todo);
                    }
                }
                break;
        }
        notifyDataSetChanged();
    }
    
    public enum FilterType {
        ALL, INCOMPLETE, COMPLETED
    }
    
    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = filteredTodoList.get(position);
        holder.bind(todo);
    }
    
    @Override
    public int getItemCount() {
        return filteredTodoList.size();
    }
    
    public class TodoViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBoxCompleted;
        private TextView textViewTitle;
        private TextView textViewDescription;
        private Chip chipPriority;
        private TextView textViewDueDate;
        private TextView textViewOverdue;
        private ImageButton btnMore;
        
        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            chipPriority = itemView.findViewById(R.id.chipPriority);
            textViewDueDate = itemView.findViewById(R.id.textViewDueDate);
            textViewOverdue = itemView.findViewById(R.id.textViewOverdue);
            btnMore = itemView.findViewById(R.id.btnMore);
            
            setupClickListeners();
        }
        
        private void setupClickListeners() {
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTodoClick(filteredTodoList.get(position));
                    }
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTodoLongClick(filteredTodoList.get(position));
                        return true;
                    }
                }
                return false;
            });
            
            checkBoxCompleted.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCheckboxClick(filteredTodoList.get(position), checkBoxCompleted.isChecked());
                    }
                }
            });
            
            btnMore.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMoreClick(filteredTodoList.get(position), v);
                    }
                }
            });
        }
        
        public void bind(Todo todo) {
            // 设置标题
            textViewTitle.setText(todo.getTitle());
            
            // 设置描述
            if (!TextUtils.isEmpty(todo.getDescription())) {
                textViewDescription.setText(todo.getDescription());
                textViewDescription.setVisibility(View.VISIBLE);
            } else {
                textViewDescription.setVisibility(View.GONE);
            }
            
            // 设置完成状态
            checkBoxCompleted.setChecked(todo.isCompleted());
            
            // 根据完成状态设置文本样式
            if (todo.isCompleted()) {
                textViewTitle.setPaintFlags(textViewTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textViewTitle.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            } else {
                textViewTitle.setPaintFlags(textViewTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                textViewTitle.setTextColor(context.getResources().getColor(android.R.color.black));
            }
            
            // 设置优先级
            chipPriority.setText(todo.getPriorityText());
            switch (todo.getPriority()) {
                case 1: // 高优先级
                    chipPriority.setChipBackgroundColorResource(android.R.color.holo_red_light);
                    break;
                case 2: // 中优先级
                    chipPriority.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                    break;
                case 3: // 低优先级
                    chipPriority.setChipBackgroundColorResource(android.R.color.holo_green_light);
                    break;
            }
            
            // 设置截止时间
            if (todo.getDueDate() != null) {
                textViewDueDate.setText(dateTimeFormat.format(todo.getDueDate()));
                textViewDueDate.setVisibility(View.VISIBLE);
                
                // 检查是否逾期
                if (todo.isOverdue()) {
                    textViewOverdue.setVisibility(View.VISIBLE);
                    textViewDueDate.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    textViewOverdue.setVisibility(View.GONE);
                    textViewDueDate.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                }
            } else {
                textViewDueDate.setVisibility(View.GONE);
                textViewOverdue.setVisibility(View.GONE);
            }
        }
    }
    
    public void updateTodo(Todo updatedTodo) {
        for (int i = 0; i < todoList.size(); i++) {
            if (todoList.get(i).getId() == updatedTodo.getId()) {
                todoList.set(i, updatedTodo);
                break;
            }
        }
        
        for (int i = 0; i < filteredTodoList.size(); i++) {
            if (filteredTodoList.get(i).getId() == updatedTodo.getId()) {
                filteredTodoList.set(i, updatedTodo);
                notifyItemChanged(i);
                break;
            }
        }
    }
    
    public void removeTodo(Todo todo) {
        int originalPosition = -1;
        for (int i = 0; i < todoList.size(); i++) {
            if (todoList.get(i).getId() == todo.getId()) {
                todoList.remove(i);
                originalPosition = i;
                break;
            }
        }
        
        int filteredPosition = -1;
        for (int i = 0; i < filteredTodoList.size(); i++) {
            if (filteredTodoList.get(i).getId() == todo.getId()) {
                filteredTodoList.remove(i);
                filteredPosition = i;
                break;
            }
        }
        
        if (filteredPosition != -1) {
            notifyItemRemoved(filteredPosition);
        }
    }
    
    public boolean isEmpty() {
        return filteredTodoList.isEmpty();
    }
}