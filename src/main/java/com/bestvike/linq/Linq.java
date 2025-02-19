package com.bestvike.linq;

import com.bestvike.linq.enumerable.Enumerable;
import com.bestvike.linq.enumerable.Range;
import com.bestvike.linq.enumerable.Repeat;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-07-18.
 */
@SuppressWarnings("unchecked")
public final class Linq {
    private Linq() {
    }

    public static <TResult> IEnumerable<TResult> empty() {
        return Enumerable.empty();
    }

    public static <TSource> IEnumerable<TSource> singleton(TSource item) {
        return Enumerable.singleton(item);
    }

    public static IEnumerable<Boolean> of(boolean[] source) {
        return Enumerable.of(source);
    }

    public static IEnumerable<Byte> of(byte[] source) {
        return Enumerable.of(source);
    }

    public static IEnumerable<Short> of(short[] source) {
        return Enumerable.of(source);
    }

    public static IEnumerable<Integer> of(int[] source) {
        return Enumerable.of(source);
    }

    public static IEnumerable<Long> of(long[] source) {
        return Enumerable.of(source);
    }

    public static IEnumerable<Float> of(float[] source) {
        return Enumerable.of(source);
    }

    public static IEnumerable<Double> of(double[] source) {
        return Enumerable.of(source);
    }

    public static IEnumerable<Character> of(char[] source) {
        return Enumerable.of(source);
    }

    public static IEnumerable<Character> of(CharSequence source) {
        return Enumerable.of(source);
    }

    public static <TSource> IEnumerable<TSource> of(TSource... source) {
        return Enumerable.of(source);
    }

    public static <TSource> IEnumerable<TSource> of(IEnumerable<? extends TSource> source) {
        return Enumerable.of((IEnumerable<TSource>) source);
    }

    public static <TSource> IEnumerable<TSource> of(List<? extends TSource> source) {
        return Enumerable.of((List<TSource>) source);
    }

    public static <TSource> IEnumerable<TSource> of(Collection<? extends TSource> source) {
        return Enumerable.of((Collection<TSource>) source);
    }

    public static <TSource> IEnumerable<TSource> of(Iterable<? extends TSource> source) {
        return Enumerable.of((Iterable<TSource>) source);
    }

    public static <TSource> IEnumerable<TSource> of(Iterator<? extends TSource> source) {
        return Enumerable.of((Iterator<TSource>) source);
    }

    public static <TSource> IEnumerable<TSource> of(Enumeration<? extends TSource> source) {
        return Enumerable.of((Enumeration<TSource>) source);
    }

    public static <TKey, TValue> IEnumerable<Map.Entry<TKey, TValue>> of(Map<? extends TKey, ? extends TValue> source) {
        return Enumerable.of((Map<TKey, TValue>) source);
    }

    public static <TSource> IEnumerable<TSource> as(Object source) {
        return Enumerable.as(source);
    }

    public static IEnumerable<Integer> range(int start, int count) {
        return Range.range(start, count);
    }

    public static <TResult> IEnumerable<TResult> repeat(TResult element, int count) {
        return Repeat.repeat(element, count);
    }
}
