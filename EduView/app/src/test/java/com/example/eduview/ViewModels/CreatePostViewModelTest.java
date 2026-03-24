package com.example.eduview.ViewModels;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.eduview.data.model.PostType;
import com.example.eduview.data.repository.PostRepository;
import com.example.eduview.ui.createPost.CreatePostViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class CreatePostViewModelTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private CreatePostViewModel viewModel;

    @Mock
    private PostRepository mockRepository;

    @Mock
    private Observer<Boolean> postCreatedObserver;

    @Mock
    private Observer<String> errorObserver;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        viewModel = new CreatePostViewModel(mockRepository);

        // Attach observers so LiveData actually triggers
        viewModel.getPostCreated().observeForever(postCreatedObserver);
        viewModel.getErrorMessage().observeForever(errorObserver);
    }

    @Test
    public void setCaption_updatesLiveData() {
        // Tests if caption is stored

        viewModel.setCaption("Hello world");

        assertEquals("Hello world", viewModel.getCaption().getValue());
    }

    @Test
    public void setCaption_nullBecomesEmptyString() {
        // Null safety check

        viewModel.setCaption(null);

        assertEquals("", viewModel.getCaption().getValue());
    }

    @Test
    public void setImageUrl_updatesLiveData() {
        viewModel.setImageUrl("image.jpg");

        assertEquals("image.jpg", viewModel.getImageUrl().getValue());
    }

    @Test
    public void createPost_emptyInputs_showsError() {
        // No caption and no image, should fail

        viewModel.setCaption("");
        viewModel.setImageUrl("");

        viewModel.createPost(PostType.PENDING, "class1", "user1");

        verify(errorObserver).onChanged("Post must contain text or an image.");

        // Tests that repo is not called
        verify(mockRepository, never()).createPost(any(), any(), any(), any(), any());
    }

    @Test
    public void createPost_validInput_callsRepository() {
        // Valid caption, should go through

        viewModel.setCaption("Some text");

        viewModel.createPost(PostType.PENDING, "class1", "user1");

        verify(mockRepository, times(1))
                .createPost(eq(PostType.PENDING), eq("class1"), any(), any(), any());
    }

    @Test
    public void createPost_successCallback_setsPostCreatedTrue() {
        // Capture the success callback and trigger it manually

        viewModel.setCaption("Test");

        ArgumentCaptor<java.util.function.Consumer<String>> successCaptor = ArgumentCaptor.forClass(java.util.function.Consumer.class);
        ArgumentCaptor<java.util.function.Consumer<Exception>> errorCaptor = ArgumentCaptor.forClass(java.util.function.Consumer.class);

        viewModel.createPost(PostType.PENDING, "class1", "user1");

        verify(mockRepository).createPost(any(), any(), any(), successCaptor.capture(), errorCaptor.capture());

        // Simulate success
        successCaptor.getValue().accept("postId");

        verify(postCreatedObserver).onChanged(true);
    }

    @Test
    public void createPost_errorCallback_setsErrorMessage() {
        viewModel.setCaption("Test");

        ArgumentCaptor<java.util.function.Consumer<String>> successCaptor = ArgumentCaptor.forClass(java.util.function.Consumer.class);
        ArgumentCaptor<java.util.function.Consumer<Exception>> errorCaptor = ArgumentCaptor.forClass(java.util.function.Consumer.class);

        viewModel.createPost(PostType.PENDING, "class1", "user1");

        verify(mockRepository).createPost(any(), any(), any(), successCaptor.capture(), errorCaptor.capture());

        // Simulate failure
        errorCaptor.getValue().accept(new Exception("Something went wrong"));

        verify(errorObserver).onChanged("Something went wrong");
    }

    @Test
    public void clearPostCreatedFlag_resetsValue() {
        viewModel.clearPostCreatedFlag();

        assertEquals(false, viewModel.getPostCreated().getValue());
    }

    @Test
    public void clearErrorMessage_resetsValue() {
        viewModel.clearErrorMessage();

        assertNull(viewModel.getErrorMessage().getValue());
    }
}
