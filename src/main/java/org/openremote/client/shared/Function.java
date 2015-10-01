package org.openremote.client.shared;

public interface Function<T, R> {
    R apply(T t);
}
