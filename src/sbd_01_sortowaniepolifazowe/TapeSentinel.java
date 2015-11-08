package sbd_01_sortowaniepolifazowe;

import java.util.Objects;
import java.util.function.Function;

public class TapeSentinel<T extends Comparable<T>> implements Comparable<TapeSentinel<T>> {
    private final T value;
    private final Long sentinel;
    
    public TapeSentinel(T value)
    {
        this.value = Objects.requireNonNull(value);
        this.sentinel = 0L;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.value);
        hash = 89 * hash + Objects.hashCode(this.sentinel);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TapeSentinel<?> other = (TapeSentinel<?>) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.sentinel, other.sentinel)) {
            return false;
        }
        return true;
    }
    
    public TapeSentinel(long sentinel)
    {
        this.value = null;
        this.sentinel = sentinel;
    }
    
    public T getValue()
    {
        return value;
    }
    
    public boolean isSentinel()
    {
        return value == null;
    }

    @Override
    public int compareTo(TapeSentinel<T> other) {
        if(!this.isSentinel() && !other.isSentinel())
            return this.value.compareTo(other.value);
        else
            return this.sentinel.compareTo(other.sentinel);
    }
    
    @Override
    public String toString()
    {
        Function<Object, String> getf = x -> String.format("%s(%s)", this.getClass().getSimpleName(), x);
        return this.isSentinel() ? getf.apply(sentinel) : getf.apply("\"" + value + "\"");
    }
    
    public static void main(String[] args)
    {
        System.out.println(new TapeSentinel<String>("asdf").compareTo(new TapeSentinel<String>(3)) < 0);
        System.out.println(new TapeSentinel<String>("aaaa").compareTo(new TapeSentinel<String>("bbbb")) < 0);
        System.out.println(new TapeSentinel<String>(-3).compareTo(new TapeSentinel<String>(3)) < 0);
        System.out.println(new TapeSentinel<String>(-3).compareTo(new TapeSentinel<String>("asdf")) < 0);
    }
}
