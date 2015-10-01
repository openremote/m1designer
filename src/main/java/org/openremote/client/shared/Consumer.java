package org.openremote.client.shared;

public interface Consumer<T> {
    void accept(T t);
}
