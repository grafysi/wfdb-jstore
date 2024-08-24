package io.graphys.wfdbjstore.engine.metadataquery;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface MetadataRepository<T extends Metadata> {
    // find by name set
    default List<T> findByName(String name) {
        return findByName(name, Integer.MAX_VALUE);
    }

    default List<T> findByName(String name, int limit) {
        var result = new ArrayList<T>();
        findByName(name, limit, result::add);
        return result;
    }

    default void findByName(String name, Consumer<T> consumer) {
        findByName(name, Integer.MAX_VALUE, consumer);
    }

    public void findByName(String name, int limit, Consumer<T> consumer);


    default List<T> findByFilter(Predicate<T> filter) {
        return findByFilter(filter, Integer.MAX_VALUE);
    }

    default List<T> findByFilter(Predicate<T> filter, int limit) {
        var result = new ArrayList<T>();
        findByFilter(filter, limit, result::add);
        return result;
    }

    default void findByFilter(Predicate<T> filter, Consumer<T> consumer) {
        findByFilter(filter, Integer.MAX_VALUE, consumer);
    }
    public void findByFilter(Predicate<T> filter, int limit, Consumer<T> consumer);


    default List<T> findAll() {
        return findAll(Integer.MAX_VALUE);
    }

    default List<T> findAll(int limit) {
        var result = new ArrayList<T>();
        findAll(limit, result::add);
        return result;
    }

    default void findAll(Consumer<T> consumer) {
        findAll(Integer.MAX_VALUE, consumer);
    }

    public void findAll(int limit, Consumer<T> consumer);

    public Class<?> getResultClass();
}






















