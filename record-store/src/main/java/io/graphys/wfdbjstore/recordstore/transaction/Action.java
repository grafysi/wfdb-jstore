package io.graphys.wfdbjstore.recordstore.transaction;

@FunctionalInterface
public interface Action<R> {
    public R perform();
}
