package com.google.collinsmith70.diablo.command.resolver;

import com.google.collinsmith70.diablo.command.ParameterResolver;

public enum KeyAliasParameterResolver implements ParameterResolver {
INSTANCE;

@Override
public String resolve(String str) {
    return str;
}

@Override
public String toString() {
    return "alias";
}

}