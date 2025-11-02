package com.nutrehogar.sistemacontable.application.config;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Context {
    private final Map<Class<?>, Object> beans = new HashMap<>();

    public <T> void registerBean(Class<T> type, T instance) {
        beans.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        return (T) beans.get(type);
    }
}