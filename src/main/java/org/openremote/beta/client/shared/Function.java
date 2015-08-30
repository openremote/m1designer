package org.openremote.beta.client.shared;

public interface Function<T, R> {
    R apply(T t);
}
