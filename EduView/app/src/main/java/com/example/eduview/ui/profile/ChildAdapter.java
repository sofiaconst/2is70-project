package com.example.eduview.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.repository.ClassroomRepository;

import java.util.ArrayList;
import java.util.List;

public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private List<Student> children = new ArrayList<>();
    private final ClassroomRepository classroomRepository;

    public ChildAdapter(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    public void setChildren(List<Student> children) {
        this.children = children;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Student child = children.get(position);
        holder.bind(child, classroomRepository);
    }

    @Override
    public int getItemCount() {
        return children.size();
    }

    static class ChildViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvClass;
        private final TextView tvEmail;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChildName);
            tvClass = itemView.findViewById(R.id.tvChildClass);
            tvEmail = itemView.findViewById(R.id.tvChildEmail);
        }

        public void bind(Student child, ClassroomRepository classroomRepository) {
            tvName.setText(child.getFirstName() + " " + child.getLastName());
            tvEmail.setText(child.getEmail() != null ? child.getEmail() : "No email");

            String classId = child.getClassId();
            if (classId == null || classId.isEmpty()) {
                tvClass.setText("Not registered to a class");
            } else {
                classroomRepository.getClassroomName(classId, 
                    name -> tvClass.setText("Class " + name),
                    error -> tvClass.setText("Class " + classId)
                );
            }
        }
    }
}
