package se.dimovski.android.delugeremote.data.filters;

/**
 * Created by nihilist on 2014-10-04.
 */
public interface Predicate<T>
{
    boolean apply(T type);
}
