package org.openremote.beta.client.shared;

public interface Consumer<T> {
    void accept(T t);
}
