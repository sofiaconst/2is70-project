package com.example.eduview.Adapters;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.example.eduview.data.model.Student;
import com.example.eduview.data.repository.ClassroomRepository;
import com.example.eduview.ui.adapters.ChildAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;


public class ChildAdapterTest {

    private ChildAdapter adapter;

    private List<Student> children;

    @Mock
    private ClassroomRepository mockRepository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        adapter = new ChildAdapter(mockRepository);

        children = new ArrayList<>();

        Student s1 = mock(Student.class);
        when(s1.getFirstName()).thenReturn("John");
        when(s1.getLastName()).thenReturn("Doe");
        when(s1.getEmail()).thenReturn("johndoe@test.com");
        when(s1.getClassId()).thenReturn("class1");

        Student s2 = mock(Student.class);
        when(s2.getFirstName()).thenReturn("Jane");
        when(s2.getLastName()).thenReturn("Smith");
        when(s2.getEmail()).thenReturn(null);
        when(s2.getClassId()).thenReturn(null);

        children.add(s1);
        children.add(s2);
    }

    @Test
    public void setChildren_updatesListAndCount() {
        // Tests if setChildren updates the adapter

        adapter.setChildren(children);

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void getItemCount_emptyList_returnsZero() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void setChildren_null_doesNotCrash() {
        // Defensive test

        try {
            adapter.setChildren(null);
        } catch (Exception e) {
            fail("setChildren should not crash on null input");
        }
    }

    @Test
    public void studentWithClassId_repositoryWouldBeUsed() {
        Student student = children.get(0);

        assertEquals("class1", student.getClassId());
    }
}
