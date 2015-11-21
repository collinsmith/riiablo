package com.google.collinsmith70.diablo.command;

import com.google.common.base.Preconditions;

public class OptionalParameterResolver<T extends ParameterResolver> implements ParameterResolver {

private final T RESOLVER;

public OptionalParameterResolver(T resolver) {
    this.RESOLVER = Preconditions.checkNotNull(resolver, "Passed resolver cannot be null.");
}

@Override
public String resolve(String str) {
    return RESOLVER.resolve(str);
}

@Override
public String toString() {
    return RESOLVER.toString();
}

}
