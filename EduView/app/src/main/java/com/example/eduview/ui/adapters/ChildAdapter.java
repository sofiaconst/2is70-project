package com.example.eduview.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduview.R;
import com.example.eduview.data.model.Classroom;
import com.example.eduview.data.model.Student;
import com.example.eduview.data.repository.ClassroomRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of children (students) in a RecyclerView.
 * Each item shows the student's name, username, and classroom information.
 */
public class ChildAdapter extends RecyclerView.Adapter<ChildAdapter.ChildViewHolder> {

    private List<Student> children = new ArrayList<>();
    private ClassroomRepository classroomRepository;

    /**
     * Default constructor using a new ClassroomRepository instance.
     */
    public ChildAdapter() {
        this.classroomRepository = new ClassroomRepository();
    }

    /**
     * Constructor for testing.
     */
    public ChildAdapter(ClassroomRepository repository) {
        this.classroomRepository = repository;
    }

    /**
     * Updates the list of children displayed in the adapter. Triggers a UI refresh after updating the data.
     *
     * @param children list of students to display
     */
    public void setChildren(List<Student> children) {
        this.children = (children != null) ? children : new ArrayList<>();

        // Skip notifying in unit tests
        try {
            notifyDataSetChanged();
        } catch (Exception ignored) {
            // Ignore
        }
    }

    /**
     * Creates a new ViewHolder for a child item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return new ViewHolder instance
     */
    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_child, parent, false);
        return new ChildViewHolder(view);
    }

    /**
     * Binds a student to the ViewHolder at the given position.
     * @param holder ViewHolder for the child
     * @param position position of the child in the list of children ID's
     */
    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        Student child = children.get(position);
        holder.bind(child, classroomRepository);
    }

    /**
     * Returns the amount of children in the list.
     * @return amount of children in the list
     */
    @Override
    public int getItemCount() {
        return children.size();
    }

    /**
     * ViewHolder representing a single child item in the list.
     */
    public static class ChildViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvClass;
        private final TextView tvUsername;

        /**
         * Initializes UI components for a child item.
         *
         * @param itemView the layout view for the item
         */
        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvChildName);
            tvClass = itemView.findViewById(R.id.tvChildClass);
            tvUsername = itemView.findViewById(R.id.tvChildUsername);
        }

        /**
         * Binds student data to the UI.
         *
         * @param child the student to display
         * @param classroomRepository repository used to fetch classroom data
         */
        public void bind(Student child, ClassroomRepository classroomRepository) {
            tvName.setText(child.getFirstName() + " " + child.getLastName());
            
            // Extract username from email (e.g., "john.doe@eduview.com" -> "john.doe")
            String email = child.getEmail();
            if (email != null && email.contains("@")) {
                String username = email.split("@")[0];
                tvUsername.setText(username);
            } else {
                tvUsername.setText(email != null ? email : "N/A");
            }

            // Gets the class ID of the child for assigning the feed item to the class.
            String classId = child.getClassId();
            if (classId == null || classId.isEmpty()) {
                tvClass.setText("Not registered to a class");
            } else {
                classroomRepository.getClassroomById(classId, new ClassroomRepository.ClassroomCallback<Classroom>() {
                    @Override
                    public void onSuccess(Classroom classroom) {
                        tvClass.setText("Class: " + classroom.getName());
                    }

                    @Override
                    public void onError(Exception e) {
                        tvClass.setText("Class: " + classId);
                    }
                });
            }
        }
    }
}
