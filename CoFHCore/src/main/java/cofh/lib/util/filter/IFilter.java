package cofh.lib.util.filter;

import java.util.function.Predicate;

public interface IFilter<T> {

    Predicate<T> getRules();

}
