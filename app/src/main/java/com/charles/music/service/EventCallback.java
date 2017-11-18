package com.charles.music.service;

public interface EventCallback<T> {
    void onEvent(T t);
}
