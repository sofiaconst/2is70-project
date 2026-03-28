package com.example.eduview.ui.adapters;

import android.util.Log;
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

/**
 * Adapter for displaying and managing students in a teacher's classroom.
 * Provides functionality to remove students via a callback listener.
 */
public class StudentManagerAdapter extends RecyclerView.Adapter<StudentManagerAdapter.StudentViewHolder> {

    /**
     * Listener for handling student removal.
     */
    public interface OnRemoveClickListener {

        /**
         * Called when the remove button for a student is clicked.
         *
         * @param student the student to be removed
         */
        void onRemoveClick(Student student);
    }

    private final List<Student> students = new ArrayList<>();
    private final OnRemoveClickListener onRemoveClickListener;

    /**
     * Creates the adapter with a removal callback.
     *
     * @param onRemoveClickListener listener triggered when a student is removed
     */
    public StudentManagerAdapter(OnRemoveClickListener onRemoveClickListener) {
        this.onRemoveClickListener = onRemoveClickListener;
    }

    /**
     * Updates the list of students displayed in the adapter.
     *
     * @param newStudents new list of students (can be null)
     */
    public void submitList(List<Student> newStudents) {
        students.clear();
        if (newStudents != null) {
            students.addAll(newStudents);
        }
        // Refresh the RecyclerView after updating data
        notifyDataSetChanged();
    }

    /**
     * Inflates the student item layout.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return ViewHolder for a student item
     */
    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_manage, parent, false);
        return new StudentViewHolder(view);
    }

    /**
     * Binds a student to the ViewHolder.
     *
     * @param holder ViewHolder instance
     * @param position position in the list
     */
    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(students.get(position));
    }

    /**
     * Returns number of students.
     *
     * @return size of student list
     */
    @Override
    public int getItemCount() {
        return students.size();
    }

    /**
     * ViewHolder representing a single student row.
     */
    class StudentViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvStudentName;
        private final MaterialButton btnRemoveStudent;

        /**
         * Initializes views for a student item.
         *
         * @param itemView item layout view
         */
        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            btnRemoveStudent = itemView.findViewById(R.id.btnRemoveStudent);
        }

        /**
         * Binds student data and sets up remove button behavior.
         *
         * @param student student to display
         */
        void bind(Student student) {
            // Display student's full name
            tvStudentName.setText(student.getFirstName() + " " + student.getLastName());

            // Trigger removal callback when button is clicked
            btnRemoveStudent.setOnClickListener(v ->
                    onRemoveClickListener.onRemoveClick(student)
            );
        }
    }
}