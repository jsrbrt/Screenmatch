package com.example.Screenmatch.service;

public interface IntConverteDados {
    <T> T  obterDados(String json, Class<T> classe);
}
