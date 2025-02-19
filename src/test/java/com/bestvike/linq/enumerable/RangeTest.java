package com.bestvike.linq.enumerable;

import com.bestvike.TestCase;
import com.bestvike.collections.generic.Array;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.Linq;
import com.bestvike.linq.exception.ArgumentOutOfRangeException;
import org.junit.Test;

import java.util.List;

/**
 * Created by 许崇雷 on 2019-05-30.
 */
public class RangeTest extends TestCase {
    @Test
    public void Range_ProduceCorrectSequence() {
        IEnumerable<Integer> rangeSequence = Linq.range(1, 100);
        int expected = 0;
        for (int val : rangeSequence) {
            expected++;
            assertEquals(expected, val);
        }

        assertEquals(100, expected);
    }

    @Test
    public void Range_ToArray_ProduceCorrectResult() {
        Array<Integer> array = Linq.range(1, 100).toArray();
        assertEquals(array._getCount(), 100);
        for (int i = 0; i < array._getCount(); i++)
            assertEquals(i + 1, array.get(i));
    }

    @Test
    public void Range_ToList_ProduceCorrectResult() {
        List<Integer> list = Linq.range(1, 100).toList();
        assertEquals(list.size(), 100);
        for (int i = 0; i < list.size(); i++)
            assertEquals(i + 1, list.get(i));
    }

    @Test
    public void Range_ZeroCountLeadToEmptySequence() {
        Array<Integer> array = Linq.range(1, 0).toArray();
        Array<Integer> array2 = Linq.range(Integer.MIN_VALUE, 0).toArray();
        Array<Integer> array3 = Linq.range(Integer.MAX_VALUE, 0).toArray();
        assertEquals(array._getCount(), 0);
        assertEquals(array2._getCount(), 0);
        assertEquals(array3._getCount(), 0);
    }

    @Test
    public void Range_ThrowExceptionOnNegativeCount() {
        assertThrows(ArgumentOutOfRangeException.class, () -> Linq.range(1, -1));
        assertThrows(ArgumentOutOfRangeException.class, () -> Linq.range(1, Integer.MIN_VALUE));
    }

    @Test
    public void Range_ThrowExceptionOnOverflow() {
        assertThrows(ArgumentOutOfRangeException.class, () -> Linq.range(1000, Integer.MAX_VALUE));
        assertThrows(ArgumentOutOfRangeException.class, () -> Linq.range(Integer.MAX_VALUE, 1000));
        assertThrows(ArgumentOutOfRangeException.class, () -> Linq.range(Integer.MAX_VALUE - 10, 20));
    }

    @Test
    public void Range_NotEnumerateAfterEnd() {
        try (IEnumerator<Integer> rangeEnum = Linq.range(1, 1).enumerator()) {
            assertTrue(rangeEnum.moveNext());
            assertFalse(rangeEnum.moveNext());
            assertFalse(rangeEnum.moveNext());
        }
    }

    @Test
    public void Range_EnumerableAndEnumeratorAreSame() {
        IEnumerable<Integer> rangeEnumerable = Linq.range(1, 1);
        try (IEnumerator<Integer> rangeEnumerator = rangeEnumerable.enumerator()) {
            assertSame(rangeEnumerable, rangeEnumerator);
        }
    }

    @Test
    public void Range_GetEnumeratorReturnUniqueInstances() {
        IEnumerable<Integer> rangeEnumerable = Linq.range(1, 1);
        try (IEnumerator<Integer> enum1 = rangeEnumerable.enumerator();
             IEnumerator<Integer> enum2 = rangeEnumerable.enumerator()) {
            assertNotSame(enum1, enum2);
        }
    }

    @Test
    public void Range_ToInt32MaxValue() {
        int from = Integer.MAX_VALUE - 3;
        int count = 4;
        IEnumerable<Integer> rangeEnumerable = Linq.range(from, count);

        assertEquals(count, rangeEnumerable.count());

        int[] expected = {Integer.MAX_VALUE - 3, Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE};
        assertEquals(Linq.of(expected), rangeEnumerable);
    }

    @Test
    public void RepeatedCallsSameResults() {
        assertEquals(Linq.range(-1, 2), Linq.range(-1, 2));
        assertEquals(Linq.range(0, 0), Linq.range(0, 0));
    }

    @Test
    public void NegativeStart() {
        int start = -5;
        int count = 1;
        int[] expected = {-5};

        assertEquals(Linq.of(expected), Linq.range(start, count));
    }

    @Test
    public void ArbitraryStart() {
        int start = 12;
        int count = 6;
        int[] expected = {12, 13, 14, 15, 16, 17};

        assertEquals(Linq.of(expected), Linq.range(start, count));
    }

    @Test
    public void Take() {
        assertEquals(Linq.range(0, 10), Linq.range(0, 20).take(10));
    }

    @Test
    public void TakeExcessive() {
        assertEquals(Linq.range(0, 10), Linq.range(0, 10).take(Integer.MAX_VALUE));
    }

    @Test
    public void Skip() {
        assertEquals(Linq.range(10, 10), Linq.range(0, 20).skip(10));
    }

    @Test
    public void SkipExcessive() {
        assertEmpty(Linq.range(10, 10).skip(20));
    }

    @Test
    public void SkipTakeCanOnlyBeOne() {
        assertEquals(Linq.of(new int[]{1}), Linq.range(1, 10).take(1));
        assertEquals(Linq.of(new int[]{2}), Linq.range(1, 10).skip(1).take(1));
        assertEquals(Linq.of(new int[]{3}), Linq.range(1, 10).take(3).skip(2));
        assertEquals(Linq.of(new int[]{1}), Linq.range(1, 10).take(3).take(1));
    }

    @Test
    public void ElementAt() {
        assertEquals(4, Linq.range(0, 10).elementAt(4));
    }

    @Test
    public void ElementAtExcessiveThrows() {
        assertThrows(ArgumentOutOfRangeException.class, () -> Linq.range(0, 10).elementAt(100));
    }

    @Test
    public void ElementAtOrDefault() {
        assertEquals(4, Linq.range(0, 10).elementAtOrDefault(4));
    }

    @Test
    public void ElementAtOrDefaultExcessiveIsDefault() {
        assertEquals(null, Linq.range(52, 10).elementAtOrDefault(100));
    }

    @Test
    public void First() {
        assertEquals(57, Linq.range(57, 1000000000).first());
    }

    @Test
    public void FirstOrDefault() {
        assertEquals(-100, Linq.range(-100, Integer.MAX_VALUE).firstOrDefault());
    }

    @Test
    public void Last() {
        assertEquals(1000000056, Linq.range(57, 1000000000).last());
    }

    @Test
    public void LastOrDefault() {
        assertEquals(Integer.MAX_VALUE - 101, Linq.range(-100, Integer.MAX_VALUE).lastOrDefault());
    }
}
