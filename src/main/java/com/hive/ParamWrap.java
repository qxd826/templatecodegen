package com.hive;

public class ParamWrap <V> {

    private volatile V value;

    /**
     * Creates a new AtomicReference with the given initial value.
     *
     * @param initialValue the initial value
     */
    public ParamWrap(V initialValue) {
        value = initialValue;
    }

    /**
     * Creates a new AtomicReference with null initial value.
     */
    public ParamWrap() {
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public final V get() {
        return value;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    public final void set(V newValue) {
        value = newValue;
    }

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value.
     */
    public String toString() {
        return String.valueOf(get());
    }

}