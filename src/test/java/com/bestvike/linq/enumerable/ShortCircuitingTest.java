package com.bestvike.linq.enumerable;

import com.bestvike.TestCase;
import com.bestvike.function.Predicate1;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.Linq;
import com.bestvike.linq.exception.InvalidOperationException;
import org.junit.Test;

import java.util.List;

/**
 * Created by 许崇雷 on 2019-06-06.
 */
public class ShortCircuitingTest extends TestCase {
    @Test
    public void ListLastDoesntCheckAll() {
        List<Integer> source = Linq.range(0, 10).toList();
        CountedFunction<Integer> pred = new CountedFunction<>(i -> i < 7);
        assertEquals(6, Linq.of(source).last(pred.getFunc()));

        // .NET Core shortcircuits as an optimization.
        // See https://github.com/dotnet/corefx/pull/2350.
        assertEquals(4, pred.Calls);
    }

    @Test
    public void MinDoubleDoesntCheckAll() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Double> source = tracker.select(i -> i == 5 ? Double.NaN : (double) i);
        assertTrue(Double.isNaN(source.minDouble()));

        // .NET Core shortcircuits as an optimization.
        // See https://github.com/dotnet/corefx/pull/2350.
        assertEquals(5, tracker.Moves);
    }

    @Test
    public void MinNullableDoubleDoesntCheckAll() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Double> source = tracker.select(i -> i == 5 ? Double.NaN : (double) i);
        assertTrue(Double.isNaN(source.minDoubleNull()));

        // .NET Core shortcircuits as an optimization.
        // See https://github.com/dotnet/corefx/pull/2350.
        assertEquals(5, tracker.Moves);
    }

    @Test
    public void MinSingleDoesntCheckAll() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Float> source = tracker.select(i -> i == 5 ? Float.NaN : (float) i);
        assertTrue(Float.isNaN(source.minFloat()));

        // .NET Core shortcircuits as an optimization.
        // See https://github.com/dotnet/corefx/pull/2350.
        assertEquals(5, tracker.Moves);
    }

    @Test
    public void MinNullableSingleDoesntCheckAll() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Float> source = tracker.select(i -> i == 5 ? Float.NaN : (float) i);
        assertTrue(Float.isNaN(source.minFloatNull()));

        // .NET Core shortcircuits as an optimization.
        // See https://github.com/dotnet/corefx/pull/2350.
        assertEquals(5, tracker.Moves);
    }

    @Test
    public void MinDoubleDoesntCheckAllStartLeadingWithNaN() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Double> source = tracker.select(i -> i == 1 ? Double.NaN : (double) i);

        assertTrue(Double.isNaN(source.minDouble()));
        assertEquals(1, tracker.Moves);
    }

    @Test
    public void MinNullableDoubleDoesntCheckAllLeadingWithNaN() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Double> source = tracker.select(i -> i == 1 ? Double.NaN : (double) i);

        assertTrue(Double.isNaN(source.minDoubleNull()));
        assertEquals(1, tracker.Moves);
    }

    @Test
    public void MinSingleDoesntCheckAllLeadingWithNaN() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Float> source = tracker.select(i -> i == 1 ? Float.NaN : (float) i);

        assertTrue(Float.isNaN(source.minFloat()));
        assertEquals(1, tracker.Moves);
    }

    @Test
    public void MinNullableSingleDoesntCheckAllLeadingWithNaN() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Float> source = tracker.select(i -> i == 1 ? Float.NaN : (float) i);

        assertTrue(Float.isNaN(source.minFloatNull()));
        assertEquals(1, tracker.Moves);
    }

    @Test
    public void MinDoubleSelectorDoesntCheckAllStartLeadingWithNaN() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Double> source = tracker.select(i -> i == 1 ? Double.NaN : (double) i);

        assertTrue(Double.isNaN(source.minDouble(x -> x + 1d)));
        assertEquals(1, tracker.Moves);
    }

    @Test
    public void MinNullableDoubleSelectorDoesntCheckAllLeadingWithNaN() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Double> source = tracker.select(i -> i == 1 ? Double.NaN : (double) i);

        assertTrue(Double.isNaN(source.minDoubleNull(x -> x + 1d)));
        assertEquals(1, tracker.Moves);
    }

    @Test
    public void MinSingleSelectorDoesntCheckAllLeadingWithNaN() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Float> source = tracker.select(i -> i == 1 ? Float.NaN : (float) i);

        assertTrue(Float.isNaN(source.minFloat(x -> x + 1f)));
        assertEquals(1, tracker.Moves);
    }

    @Test
    public void MinNullableSingleSelectorDoesntCheckAllLeadingWithNaN() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        IEnumerable<Float> source = tracker.select(i -> i == 1 ? Float.NaN : (float) i);

        assertTrue(Float.isNaN(source.minFloatNull(x -> x + 1f)));
        assertEquals(1, tracker.Moves);
    }

    @Test
    public void SingleWithPredicateDoesntCheckAll() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        CountedFunction<Integer> pred = new CountedFunction<>(i -> i > 2);
        assertThrows(InvalidOperationException.class, () -> tracker.single(pred.getFunc()));

        // .NET Core shortcircuits as an optimization.
        // See https://github.com/dotnet/corefx/pull/2350.
        assertEquals(4, tracker.Moves);
        assertEquals(4, pred.Calls);
    }

    @Test
    public void SingleOrDefaultWithPredicateDoesntCheckAll() {
        TrackingEnumerable tracker = new TrackingEnumerable(10);
        CountedFunction<Integer> pred = new CountedFunction<>(i -> i > 2);
        assertThrows(InvalidOperationException.class, () -> tracker.singleOrDefault(pred.getFunc()));

        // .NET Core shortcircuits as an optimization.
        // See https://github.com/dotnet/corefx/pull/2350.
        assertEquals(4, tracker.Moves);
        assertEquals(4, pred.Calls);
    }

    @Test
    public void SingleWithPredicateWorksLikeWhereFollowedBySingle() {
        TrackingEnumerable tracker0 = new TrackingEnumerable(10);
        CountedFunction<Integer> pred0 = new CountedFunction<>(i -> i > 2);
        assertThrows(InvalidOperationException.class, () -> tracker0.single(pred0.getFunc()));
        TrackingEnumerable tracker1 = new TrackingEnumerable(10);
        CountedFunction<Integer> pred1 = new CountedFunction<>(i -> i > 2);
        assertThrows(InvalidOperationException.class, () -> tracker1.where(pred1.getFunc()).single());

        // .NET Core shortcircuits as an optimization.
        // See https://github.com/dotnet/corefx/pull/2350.
        assertEquals(tracker0.Moves, tracker1.Moves);
        assertEquals(pred0.Calls, pred1.Calls);
    }

    @Test
    public void SingleOrDefaultWithPredicateWorksLikeWhereFollowedBySingleOrDefault() {
        TrackingEnumerable tracker0 = new TrackingEnumerable(10);
        CountedFunction<Integer> pred0 = new CountedFunction<>(i -> i > 2);
        assertThrows(InvalidOperationException.class, () -> tracker0.singleOrDefault(pred0.getFunc()));
        TrackingEnumerable tracker1 = new TrackingEnumerable(10);
        CountedFunction<Integer> pred1 = new CountedFunction<>(i -> i > 2);
        assertThrows(InvalidOperationException.class, () -> tracker1.where(pred1.getFunc()).singleOrDefault());

        // .NET Core shortcircuits as an optimization.
        // See https://github.com/dotnet/corefx/pull/2350.
        assertEquals(tracker0.Moves, tracker1.Moves);
        assertEquals(pred0.Calls, pred1.Calls);
    }


    private static class TrackingEnumerable implements IEnumerable<Integer> {
        // Skipping tests of double calls on GetEnumerable. Just don't do them here!
        private final int count;
        int Moves;

        TrackingEnumerable(int count) {
            this.count = count;
        }

        public IEnumerator<Integer> enumerator() {
            return new AbstractEnumerator<Integer>() {
                @Override
                public boolean moveNext() {
                    if (this.state < 0)
                        return false;
                    if (this.state < TrackingEnumerable.this.count) {
                        this.state++;
                        this.current = ++TrackingEnumerable.this.Moves;
                        return true;
                    }
                    this.close();
                    return false;
                }
            };
        }
    }

    private static class CountedFunction<T> {
        private final Predicate1<T> basefunc;
        int Calls;

        CountedFunction(Predicate1<T> baseFunc) {
            this.basefunc = baseFunc;
        }

        Predicate1<T> getFunc() {
            return x -> {
                ++this.Calls;
                return this.basefunc.apply(x);
            };
        }
    }
}
