package com.gmail.collinsmith70.util;

public interface Validator<T> {

static Validator ACCEPT_ALL = new Validator() {
    @Override
    public boolean isValid(Object obj) {
        return true;
    }
};

boolean isValid(Object obj);

}
