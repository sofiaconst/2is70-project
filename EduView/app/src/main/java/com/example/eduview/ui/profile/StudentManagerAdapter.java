package com.example.eduview.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.data.model.Student;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class StudentManagerAdapter extends RecyclerView.Adapter<StudentManagerAdapter.StudentViewHolder> {

    public interface OnRemoveClickListener {
        void onRemoveClick(Student student);
    }

    private final List<Student> students = new ArrayList<>();
    private final OnRemoveClickListener onRemoveClickListener;

    public StudentManagerAdapter(OnRemoveClickListener onRemoveClickListener) {
        this.onRemoveClickListener = onRemoveClickListener;
    }

    public void submitList(List<Student> newStudents) {
        students.clear();
        if (newStudents != null) {
            students.addAll(newStudents);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_manage, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(students.get(position));
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    class StudentViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvStudentName;
        private final MaterialButton btnRemoveStudent;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            btnRemoveStudent = itemView.findViewById(R.id.btnRemoveStudent);
        }

        void bind(Student student) {
            tvStudentName.setText(student.getFirstName() + " " + student.getLastName());
            btnRemoveStudent.setOnClickListener(v -> onRemoveClickListener.onRemoveClick(student));
        }
    }
}
