package com.example.pocketsoccer;

public class Pair<T1, T2> {
    public T1 valueA;
    public T2 valueB;

    public Pair(T1 valueA, T2 valueB) {
        this.valueA = valueA;
        this.valueB = valueB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return valueA.equals(pair.valueA) &&
                valueB.equals(pair.valueB);
    }

}
