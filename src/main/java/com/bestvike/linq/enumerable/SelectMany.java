package com.bestvike.linq.enumerable;

import com.bestvike.function.Func1;
import com.bestvike.function.Func2;
import com.bestvike.function.IndexFunc2;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.exception.ExceptionArgument;
import com.bestvike.linq.exception.ThrowHelper;
import com.bestvike.linq.util.ListUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 许崇雷 on 2018-05-04.
 */
public final class SelectMany {
    private SelectMany() {
    }

    public static <TSource, TResult> IEnumerable<TResult> selectMany(IEnumerable<TSource> source, Func1<TSource, IEnumerable<TResult>> selector) {
        if (source == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.source);
        if (selector == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.selector);

        return new SelectManyIterator<>(source, selector);
    }

    public static <TSource, TResult> IEnumerable<TResult> selectMany(IEnumerable<TSource> source, IndexFunc2<TSource, IEnumerable<TResult>> selector) {
        if (source == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.source);
        if (selector == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.selector);

        return new SelectManyIterator2<>(source, selector);
    }

    public static <TSource, TCollection, TResult> IEnumerable<TResult> selectMany(IEnumerable<TSource> source, Func1<TSource, IEnumerable<TCollection>> collectionSelector, Func2<TSource, TCollection, TResult> resultSelector) {
        if (source == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.source);
        if (collectionSelector == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.collectionSelector);
        if (resultSelector == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.resultSelector);

        return new SelectManyResultIterator<>(source, collectionSelector, resultSelector);
    }

    public static <TSource, TCollection, TResult> IEnumerable<TResult> selectMany(IEnumerable<TSource> source, IndexFunc2<TSource, IEnumerable<TCollection>> collectionSelector, Func2<TSource, TCollection, TResult> resultSelector) {
        if (source == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.source);
        if (collectionSelector == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.collectionSelector);
        if (resultSelector == null)
            ThrowHelper.throwArgumentNullException(ExceptionArgument.resultSelector);

        return new SelectManyResultIterator2<>(source, collectionSelector, resultSelector);
    }
}


final class SelectManyIterator<TSource, TResult> extends Iterator<TResult> implements IIListProvider<TResult> {
    private final IEnumerable<TSource> source;
    private final Func1<TSource, IEnumerable<TResult>> selector;
    private IEnumerator<TSource> enumerator;
    private IEnumerator<TResult> subEnumerator;

    SelectManyIterator(IEnumerable<TSource> source, Func1<TSource, IEnumerable<TResult>> selector) {
        assert source != null;
        assert selector != null;

        this.source = source;
        this.selector = selector;
    }

    @Override
    public AbstractIterator<TResult> clone() {
        return new SelectManyIterator<>(this.source, this.selector);
    }

    @Override
    public boolean moveNext() {
        do {
            switch (this.state) {
                case 1:
                    this.enumerator = this.source.enumerator();
                    this.state = 2;
                case 2:
                    if (this.enumerator.moveNext()) {
                        TSource element = this.enumerator.current();
                        this.subEnumerator = this.selector.apply(element).enumerator();
                        this.state = 3;
                        break;
                    }
                    this.close();
                    return false;
                case 3:
                    if (this.subEnumerator.moveNext()) {
                        this.current = this.subEnumerator.current();
                        return true;
                    }
                    this.subEnumerator.close();
                    this.subEnumerator = null;
                    this.state = 2;
                    break;
                default:
                    return false;
            }
        } while (true);
    }

    @Override
    public void close() {
        if (this.enumerator != null) {
            this.enumerator.close();
            this.enumerator = null;
        }
        if (this.subEnumerator != null) {
            this.subEnumerator.close();
            this.subEnumerator = null;
        }
        super.close();
    }

    @Override
    public TResult[] _toArray(Class<TResult> clazz) {
        SparseArrayBuilder<TResult> builder = new SparseArrayBuilder<>();
        ArrayBuilder<IEnumerable<TResult>> deferredCopies = new ArrayBuilder<>();
        try (IEnumerator<TSource> e = this.source.enumerator()) {
            while (e.moveNext()) {
                IEnumerable<TResult> enumerable = this.selector.apply(e.current());
                if (builder.reserveOrAdd(enumerable))
                    deferredCopies.add(enumerable);
            }
        }

        TResult[] array = builder.toArray(clazz);
        ArrayBuilder<Marker> markers = builder.getMarkers();
        for (int i = 0; i < markers.getCount(); i++) {
            Marker marker = markers.get(i);
            IEnumerable<TResult> enumerable = deferredCopies.get(i);
            EnumerableHelpers.copy(enumerable, array, marker.getIndex(), marker.getCount());
        }

        return array;
    }

    @Override
    public Object[] _toArray() {
        SparseArrayBuilder<TResult> builder = new SparseArrayBuilder<>();
        ArrayBuilder<IEnumerable<TResult>> deferredCopies = new ArrayBuilder<>();
        try (IEnumerator<TSource> e = this.source.enumerator()) {
            while (e.moveNext()) {
                IEnumerable<TResult> enumerable = this.selector.apply(e.current());
                if (builder.reserveOrAdd(enumerable))
                    deferredCopies.add(enumerable);
            }
        }

        Object[] array = builder.toArray();
        ArrayBuilder<Marker> markers = builder.getMarkers();
        for (int i = 0; i < markers.getCount(); i++) {
            Marker marker = markers.get(i);
            IEnumerable<TResult> enumerable = deferredCopies.get(i);
            EnumerableHelpers.copy(enumerable, array, marker.getIndex(), marker.getCount());
        }

        return array;
    }

    @Override
    public List<TResult> _toList() {
        List<TResult> list = new ArrayList<>();
        try (IEnumerator<TSource> e = this.source.enumerator()) {
            while (e.moveNext())
                ListUtils.addRange(list, this.selector.apply(e.current()));
        }

        return list;
    }

    @Override
    public int _getCount(boolean onlyIfCheap) {
        if (onlyIfCheap)
            return -1;

        int count = 0;
        try (IEnumerator<TSource> e = this.source.enumerator()) {
            while (e.moveNext())
                count = Math.addExact(count, this.selector.apply(e.current()).count());
        }

        return count;
    }
}


final class SelectManyIterator2<TSource, TResult> extends Iterator<TResult> implements IIListProvider<TResult> {
    private final IEnumerable<TSource> source;
    private final IndexFunc2<TSource, IEnumerable<TResult>> selector;
    private IEnumerator<TSource> enumerator;
    private IEnumerator<TResult> subEnumerator;
    private int index;

    SelectManyIterator2(IEnumerable<TSource> source, IndexFunc2<TSource, IEnumerable<TResult>> selector) {
        assert source != null;
        assert selector != null;

        this.source = source;
        this.selector = selector;
    }

    @Override
    public AbstractIterator<TResult> clone() {
        return new SelectManyIterator2<>(this.source, this.selector);
    }

    @Override
    public boolean moveNext() {
        do {
            switch (this.state) {
                case 1:
                    this.index = -1;
                    this.enumerator = this.source.enumerator();
                    this.state = 2;
                case 2:
                    if (this.enumerator.moveNext()) {
                        TSource item = this.enumerator.current();
                        this.index = Math.addExact(this.index, 1);
                        this.subEnumerator = this.selector.apply(item, this.index).enumerator();
                        this.state = 3;
                        break;
                    }
                    this.close();
                    return false;
                case 3:
                    if (this.subEnumerator.moveNext()) {
                        this.current = this.subEnumerator.current();
                        return true;
                    }
                    this.subEnumerator.close();
                    this.subEnumerator = null;
                    this.state = 2;
                    break;
                default:
                    return false;
            }
        } while (true);
    }

    @Override
    public void close() {
        if (this.enumerator != null) {
            this.enumerator.close();
            this.enumerator = null;
        }
        if (this.subEnumerator != null) {
            this.subEnumerator.close();
            this.subEnumerator = null;
        }
        super.close();
    }

    @Override
    public TResult[] _toArray(Class<TResult> clazz) {
        SparseArrayBuilder<TResult> builder = new SparseArrayBuilder<>();
        ArrayBuilder<IEnumerable<TResult>> deferredCopies = new ArrayBuilder<>();
        int index = 0;
        try (IEnumerator<TSource> e = this.source.enumerator()) {
            while (e.moveNext()) {
                IEnumerable<TResult> enumerable = this.selector.apply(e.current(), index);
                index = Math.addExact(index, 1);
                if (builder.reserveOrAdd(enumerable))
                    deferredCopies.add(enumerable);
            }
        }

        TResult[] array = builder.toArray(clazz);
        ArrayBuilder<Marker> markers = builder.getMarkers();
        for (int i = 0; i < markers.getCount(); i++) {
            Marker marker = markers.get(i);
            IEnumerable<TResult> enumerable = deferredCopies.get(i);
            EnumerableHelpers.copy(enumerable, array, marker.getIndex(), marker.getCount());
        }

        return array;
    }

    @Override
    public Object[] _toArray() {
        SparseArrayBuilder<TResult> builder = new SparseArrayBuilder<>();
        ArrayBuilder<IEnumerable<TResult>> deferredCopies = new ArrayBuilder<>();
        int index = 0;
        try (IEnumerator<TSource> e = this.source.enumerator()) {
            while (e.moveNext()) {
                IEnumerable<TResult> enumerable = this.selector.apply(e.current(), index);
                index = Math.addExact(index, 1);
                if (builder.reserveOrAdd(enumerable))
                    deferredCopies.add(enumerable);
            }
        }

        Object[] array = builder.toArray();
        ArrayBuilder<Marker> markers = builder.getMarkers();
        for (int i = 0; i < markers.getCount(); i++) {
            Marker marker = markers.get(i);
            IEnumerable<TResult> enumerable = deferredCopies.get(i);
            EnumerableHelpers.copy(enumerable, array, marker.getIndex(), marker.getCount());
        }

        return array;
    }

    @Override
    public List<TResult> _toList() {
        List<TResult> list = new ArrayList<>();
        int index = 0;
        try (IEnumerator<TSource> e = this.source.enumerator()) {
            while (e.moveNext()) {
                ListUtils.addRange(list, this.selector.apply(e.current(), index));
                index = Math.addExact(index, 1);
            }
        }

        return list;
    }

    @Override
    public int _getCount(boolean onlyIfCheap) {
        if (onlyIfCheap)
            return -1;

        int count = 0;
        int index = 0;
        try (IEnumerator<TSource> e = this.source.enumerator()) {
            while (e.moveNext()) {
                count = Math.addExact(count, this.selector.apply(e.current(), index).count());
                index = Math.addExact(index, 1);
            }
        }

        return count;
    }
}


final class SelectManyResultIterator<TSource, TCollection, TResult> extends Iterator<TResult> {
    private final IEnumerable<TSource> source;
    private final Func1<TSource, IEnumerable<TCollection>> collectionSelector;
    private final Func2<TSource, TCollection, TResult> resultSelector;
    private IEnumerator<TSource> enumerator;
    private IEnumerator<TCollection> subEnumerator;
    private TSource element;

    SelectManyResultIterator(IEnumerable<TSource> source, Func1<TSource, IEnumerable<TCollection>> collectionSelector, Func2<TSource, TCollection, TResult> resultSelector) {
        assert source != null;
        assert collectionSelector != null;
        assert resultSelector != null;

        this.source = source;
        this.collectionSelector = collectionSelector;
        this.resultSelector = resultSelector;
    }

    @Override
    public AbstractIterator<TResult> clone() {
        return new SelectManyResultIterator<>(this.source, this.collectionSelector, this.resultSelector);
    }

    @Override
    public boolean moveNext() {
        do {
            switch (this.state) {
                case 1:
                    this.enumerator = this.source.enumerator();
                    this.state = 2;
                case 2:
                    if (this.enumerator.moveNext()) {
                        this.element = this.enumerator.current();
                        this.subEnumerator = this.collectionSelector.apply(this.element).enumerator();
                        this.state = 3;
                        break;
                    }
                    this.close();
                    return false;
                case 3:
                    if (this.subEnumerator.moveNext()) {
                        TCollection item = this.subEnumerator.current();
                        this.current = this.resultSelector.apply(this.element, item);
                        return true;
                    }
                    this.subEnumerator.close();
                    this.subEnumerator = null;
                    this.state = 2;
                    break;
                default:
                    return false;
            }
        } while (true);
    }

    @Override
    public void close() {
        if (this.enumerator != null) {
            this.enumerator.close();
            this.enumerator = null;
            this.element = null;
        }
        if (this.subEnumerator != null) {
            this.subEnumerator.close();
            this.subEnumerator = null;
        }
        super.close();
    }
}


final class SelectManyResultIterator2<TSource, TCollection, TResult> extends Iterator<TResult> {
    private final IEnumerable<TSource> source;
    private final IndexFunc2<TSource, IEnumerable<TCollection>> collectionSelector;
    private final Func2<TSource, TCollection, TResult> resultSelector;
    private IEnumerator<TSource> enumerator;
    private IEnumerator<TCollection> subEnumerator;
    private TSource element;
    private int index;


    SelectManyResultIterator2(IEnumerable<TSource> source, IndexFunc2<TSource, IEnumerable<TCollection>> collectionSelector, Func2<TSource, TCollection, TResult> resultSelector) {
        assert source != null;
        assert collectionSelector != null;
        assert resultSelector != null;

        this.source = source;
        this.collectionSelector = collectionSelector;
        this.resultSelector = resultSelector;
    }

    @Override
    public AbstractIterator<TResult> clone() {
        return new SelectManyResultIterator2<>(this.source, this.collectionSelector, this.resultSelector);
    }

    @Override
    public boolean moveNext() {
        do {
            switch (this.state) {
                case 1:
                    this.index = -1;
                    this.enumerator = this.source.enumerator();
                    this.state = 2;
                case 2:
                    if (this.enumerator.moveNext()) {
                        this.element = this.enumerator.current();
                        this.index = Math.addExact(this.index, 1);
                        this.subEnumerator = this.collectionSelector.apply(this.element, this.index).enumerator();
                        this.state = 3;
                        break;
                    }
                    this.close();
                    return false;
                case 3:
                    if (this.subEnumerator.moveNext()) {
                        TCollection item = this.subEnumerator.current();
                        this.current = this.resultSelector.apply(this.element, item);
                        return true;
                    }
                    this.subEnumerator.close();
                    this.subEnumerator = null;
                    this.state = 2;
                    break;
                default:
                    return false;
            }
        } while (true);
    }

    @Override
    public void close() {
        if (this.enumerator != null) {
            this.enumerator.close();
            this.enumerator = null;
            this.element = null;
        }
        if (this.subEnumerator != null) {
            this.subEnumerator.close();
            this.subEnumerator = null;
        }
        super.close();
    }
}
