package com.bestvike.linq.enumerable;

import com.bestvike.TestCase;
import com.bestvike.collections.generic.Array;
import com.bestvike.function.Predicate1;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.Linq;
import com.bestvike.linq.exception.ArgumentNullException;
import org.junit.Test;

/**
 * Created by 许崇雷 on 2019-06-17.
 */
public class FindLastIndexTest extends TestCase {
    @Test
    public void SameResultsRepeatCallsIntQuery() {
        IEnumerable<Integer> q = Linq.of(9999, 0, 888, -1, 66, -777, 1, 2, -12345)
                .where(x -> x > Integer.MIN_VALUE);

        Predicate1<Integer> predicate = TestCase::IsEven;
        assertEquals(q.findLastIndex(predicate), q.findLastIndex(predicate));
    }

    @Test
    public void SameResultsRepeatCallsStringQuery() {
        IEnumerable<String> q = Linq.of("!@#$%^", "C", "AAA", "", "Calling Twice", "SoS", Empty);

        Predicate1<String> predicate = TestCase::IsNullOrEmpty;
        assertEquals(q.findLastIndex(predicate), q.findLastIndex(predicate));
    }

    @Test
    public void FindLastIndex() {
        Predicate1<Integer> isEvenFunc = TestCase::IsEven;
        this.FindLastIndex(Linq.empty(), isEvenFunc, -1);
        this.FindLastIndex(Linq.singleton(4), isEvenFunc, 0);
        this.FindLastIndex(Linq.singleton(5), isEvenFunc, -1);
        this.FindLastIndex(Linq.of(5, 9, 3, 7, 4), isEvenFunc, 4);
        this.FindLastIndex(Linq.of(5, 8, 9, 3, 7, 11), isEvenFunc, 1);

        Array<Integer> range = Linq.range(1, 10).toArray();
        this.FindLastIndex(range, i -> i > 10, -1);
        for (int j = 0; j <= 9; j++) {
            int k = j; // Local copy for iterator
            this.FindLastIndex(range, i -> i > k, 9);
        }
    }

    private void FindLastIndex(IEnumerable<Integer> source, Predicate1<Integer> predicate, int expected) {
        assertEquals(expected, source.findLastIndex(predicate));
    }

    @Test
    public void FindLastIndexRunOnce() {
        Predicate1<Integer> isEvenFunc = TestCase::IsEven;
        this.FindLastIndexRunOnce(Linq.empty(), isEvenFunc, -1);
        this.FindLastIndexRunOnce(Linq.singleton(4), isEvenFunc, 0);
        this.FindLastIndexRunOnce(Linq.singleton(5), isEvenFunc, -1);
        this.FindLastIndexRunOnce(Linq.of(5, 9, 3, 7, 4), isEvenFunc, 4);
        this.FindLastIndexRunOnce(Linq.of(5, 8, 9, 3, 7, 11), isEvenFunc, 1);

        Array<Integer> range = Linq.range(1, 10).toArray();
        this.FindLastIndexRunOnce(range, i -> i > 10, -1);
        for (int j = 0; j <= 9; j++) {
            int k = j; // Local copy for iterator
            this.FindLastIndexRunOnce(range, i -> i > k, 9);
        }
    }

    private void FindLastIndexRunOnce(IEnumerable<Integer> source, Predicate1<Integer> predicate, int expected) {
        assertEquals(expected, source.runOnce().findLastIndex(predicate));
    }

    @Test
    public void NullSource_ThrowsArgumentNullException2() {
        assertThrows(NullPointerException.class, () -> ((IEnumerable<Integer>) null).findLastIndex(i -> i != 0));
    }

    @Test
    public void NullPredicate_ThrowsArgumentNullException() {
        Predicate1<Integer> predicate = null;
        assertThrows(ArgumentNullException.class, () -> Linq.range(0, 3).findLastIndex(predicate));
    }

    @Test
    public void testFindLastIndexPredicate() {
        assertEquals(-1, Linq.of(depts).findLastIndex(dept -> dept.name != null && dept.name.equals("IT")));
        assertEquals(0, Linq.of(depts).findLastIndex(dept -> dept.name != null && dept.name.equals("Sales")));
    }
}
