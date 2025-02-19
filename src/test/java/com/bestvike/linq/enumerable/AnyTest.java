package com.bestvike.linq.enumerable;

import com.bestvike.TestCase;
import com.bestvike.collections.generic.Array;
import com.bestvike.function.Predicate1;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.Linq;
import com.bestvike.linq.exception.ArgumentNullException;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by 许崇雷 on 2018-05-10.
 */
public class AnyTest extends TestCase {
    @Test
    public void SameResultsRepeatCallsIntQuery() {
        IEnumerable<Integer> q = Linq.of(9999, 0, 888, -1, 66, -777, 1, 2, -12345)
                .where(x -> x > Integer.MIN_VALUE);

        Predicate1<Integer> predicate = TestCase::IsEven;
        assertEquals(q.any(predicate), q.any(predicate));
    }

    @Test
    public void SameResultsRepeatCallsStringQuery() {
        IEnumerable<String> q = Linq.of("!@#$%^", "C", "AAA", "", "Calling Twice", "SoS", Empty);

        Predicate1<String> predicate = TestCase::IsNullOrEmpty;
        assertEquals(q.any(predicate), q.any(predicate));
    }

    @Test
    public void Any() {
        this.Any(Linq.empty(), null, false);
        this.Any(Linq.singleton(3), null, true);

        Predicate1<Integer> isEvenFunc = TestCase::IsEven;
        this.Any(Linq.empty(), isEvenFunc, false);
        this.Any(Linq.singleton(4), isEvenFunc, true);
        this.Any(Linq.singleton(5), isEvenFunc, false);
        this.Any(Linq.of(5, 9, 3, 7, 4), isEvenFunc, true);
        this.Any(Linq.of(5, 8, 9, 3, 7, 11), isEvenFunc, true);

        Array<Integer> range = Linq.range(1, 10).toArray();
        this.Any(range, i -> i > 10, false);
        for (int j = 0; j <= 9; j++) {
            int k = j; // Local copy for iterator
            this.Any(range, i -> i > k, true);
        }
    }

    private void Any(IEnumerable<Integer> source, Predicate1<Integer> predicate, boolean expected) {
        if (predicate == null)
            assertEquals(expected, source.any());
        else
            assertEquals(expected, source.any(predicate));
    }

    @Test
    public void AnyRunOnce() {
        this.AnyRunOnce(Linq.empty(), null, false);
        this.AnyRunOnce(Linq.singleton(3), null, true);

        Predicate1<Integer> isEvenFunc = TestCase::IsEven;
        this.AnyRunOnce(Linq.empty(), isEvenFunc, false);
        this.AnyRunOnce(Linq.singleton(4), isEvenFunc, true);
        this.AnyRunOnce(Linq.singleton(5), isEvenFunc, false);
        this.AnyRunOnce(Linq.of(5, 9, 3, 7, 4), isEvenFunc, true);
        this.AnyRunOnce(Linq.of(5, 8, 9, 3, 7, 11), isEvenFunc, true);

        Array<Integer> range = Linq.range(1, 10).toArray();
        this.AnyRunOnce(range, i -> i > 10, false);
        for (int j = 0; j <= 9; j++) {
            int k = j; // Local copy for iterator
            this.AnyRunOnce(range, i -> i > k, true);
        }
    }

    private void AnyRunOnce(IEnumerable<Integer> source, Predicate1<Integer> predicate, boolean expected) {
        if (predicate == null)
            assertEquals(expected, source.runOnce().any());
        else
            assertEquals(expected, source.runOnce().any(predicate));
    }

    @Test
    public void NullObjectsInArray_Included() {
        IEnumerable<Integer> source = Linq.of(null, null, null, null);
        assertTrue(source.any());
    }

    @Test
    public void NullSource_ThrowsArgumentNullException() {
        assertThrows(NullPointerException.class, () -> ((IEnumerable<Integer>) null).any());
        assertThrows(NullPointerException.class, () -> ((IEnumerable<Integer>) null).any(i -> i != 0));
    }

    @Test
    public void NullPredicate_ThrowsArgumentNullException() {
        Predicate1<Integer> predicate = null;
        assertThrows(ArgumentNullException.class, () -> Linq.range(0, 3).any(predicate));
    }

    @Test
    public void testAny() {
        assertFalse(Linq.of(Collections.emptyList()).any());
        assertTrue(Linq.of(emps).any());
    }

    @Test
    public void testAnyPredicate() {
        assertFalse(Linq.of(depts).any(dept -> dept.name != null && dept.name.equals("IT")));
        assertTrue(Linq.of(depts).any(dept -> dept.name != null && dept.name.equals("Sales")));
    }
}
