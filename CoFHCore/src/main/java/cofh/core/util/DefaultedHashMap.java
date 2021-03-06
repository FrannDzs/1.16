package cofh.core.util;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A simple HashMap implementation to return a default value under a specific condition.
 * Default condition is if the element is null.
 *
 * @author covers1624
 */
public class DefaultedHashMap<K, V> extends HashMap<K, V> {

    private final V defaultValue;
    private final Predicate<V> useDefaultPredicate;

    public DefaultedHashMap(V defaultValue) {

        this(defaultValue, Objects::isNull);
    }

    public DefaultedHashMap(V defaultValue, Predicate<V> useDefaultPredicate) {

        this.defaultValue = defaultValue;
        this.useDefaultPredicate = useDefaultPredicate;
    }

    @Override
    public V get(Object key) {

        V value = super.get(key);
        return useDefaultPredicate.test(value) ? defaultValue : value;
    }

}
