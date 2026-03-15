package com.example.eduview.data.repository;

import com.example.eduview.data.model.User;

public interface SessionCallback {
        void onSuccess(User user);
        void onError(Exception e);

}
