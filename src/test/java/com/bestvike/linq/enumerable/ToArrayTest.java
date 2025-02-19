package com.bestvike.linq.enumerable;

import com.bestvike.TestCase;
import com.bestvike.collections.generic.Array;
import com.bestvike.collections.generic.ICollection;
import com.bestvike.function.Action1;
import com.bestvike.function.Action2;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.Linq;
import com.bestvike.linq.exception.ArgumentNullException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by 许崇雷 on 2019-06-06.
 */
public class ToArrayTest extends TestCase {
    private static IEnumerable<Object[]> JustBelowPowersOfTwoLengths() {
        return SmallPowersOfTwo().select(p -> new Object[]{p - 1});
    }

    private static IEnumerable<Object[]> PowersOfTwoLengths() {
        return SmallPowersOfTwo().select(p -> new Object[]{p});
    }

    private static IEnumerable<Object[]> JustAbovePowersOfTwoLengths() {
        return SmallPowersOfTwo().select(p -> new Object[]{p + 1});
    }

    private static IEnumerable<Integer> SmallPowersOfTwo() {
        // By N being "small" we mean that allocating an array of
        // size N doesn't come close to the risk of causing an OOME

        final int MaxPower = 19;
        return Linq.range(0, MaxPower)
                .select(i -> 1 << i);// equivalent to pow(2, i)
    }

    @Test
    public void ToArray_CreateACopyWhenNotEmpty() {
        Integer[] sourceArray = new Integer[]{1, 2, 3, 4, 5};
        Integer[] resultArray = Linq.of(sourceArray).toArray(Integer.class);

        assertNotSame(sourceArray, resultArray);
        assertEquals(Linq.of(sourceArray), Linq.of(resultArray));
    }

    @Test
    public void ToArray_UseArrayEmptyWhenEmpty() {
        IEnumerable<Integer> emptySourceArray = new Array<>(new Object[0]);

        // .NET Core returns the instance as an optimization.
        // see https://github.com/dotnet/corefx/pull/2401.
        Action2<Object, Object> assertSame = (objA, objB) -> assertEquals(true, objA == objB);

        assertSame.apply(emptySourceArray.toArray(), emptySourceArray.toArray());

        assertSame.apply(emptySourceArray.select(i -> i).toArray(), emptySourceArray.select(i -> i).toArray());
        assertSame.apply(Linq.of(emptySourceArray.toList()).select(i -> i).toArray(), Linq.of(emptySourceArray.toList()).select(i -> i).toArray());
        assertSame.apply(Linq.of(new ArrayList<>(emptySourceArray.toList())).select(i -> i).toArray(), Linq.of(new ArrayList<>(emptySourceArray.toList())).select(i -> i).toArray());
        assertSame.apply(emptySourceArray.orderBy(i -> i).toArray(), emptySourceArray.orderBy(i -> i).toArray());

        assertSame.apply(Linq.range(5, 0).toArray(), Linq.range(3, 0).toArray());
        assertSame.apply(Linq.range(5, 3).take(0).toArray(), Linq.range(3, 0).toArray());
        assertSame.apply(Linq.range(5, 3).skip(3).toArray(), Linq.range(3, 0).toArray());

        assertSame.apply(Linq.repeat(42, 0).toArray(), Linq.range(84, 0).toArray());
        assertSame.apply(Linq.repeat(42, 3).take(0).toArray(), Linq.range(84, 3).take(0).toArray());
        assertSame.apply(Linq.repeat(42, 3).skip(3).toArray(), Linq.range(84, 3).skip(3).toArray());
    }

    private <T> void RunToArrayOnAllCollectionTypes(T[] items, Action1<Array<T>> validation) {
        validation.apply(Linq.of(items).toArray());
        validation.apply(Linq.of(Arrays.asList(items)).toArray());
        validation.apply(new TestEnumerable<>(items).toArray());
        validation.apply(new TestReadOnlyCollection<>(items).toArray());
        validation.apply(new TestCollection<>(items).toArray());
    }

    @Test
    public void ToArray_WorkWithEmptyCollection() {
        this.RunToArrayOnAllCollectionTypes(new Integer[0], resultArray -> {
            assertNotNull(resultArray);
            assertEquals(0, resultArray._getCount());
        });
    }

    @Test
    public void ToArray_ProduceCorrectArray() {
        Integer[] sourceArray = new Integer[]{1, 2, 3, 4, 5, 6, 7};
        this.RunToArrayOnAllCollectionTypes(sourceArray, resultArray -> {
            assertEquals(sourceArray.length, resultArray._getCount());
            assertEquals(Linq.of(sourceArray), resultArray);
        });

        String[] sourceStringArray = new String[]{"1", "2", "3", "4", "5", "6", "7", "8"};
        this.RunToArrayOnAllCollectionTypes(sourceStringArray, resultStringArray -> {
            assertEquals(sourceStringArray.length, resultStringArray._getCount());
            for (int i = 0; i < sourceStringArray.length; i++)
                assertSame(sourceStringArray[i], resultStringArray.get(i));
        });
    }

    @Test
    public void RunOnce() {
        assertEquals(Linq.of(new int[]{1, 2, 3, 4, 5, 6, 7}), Linq.range(1, 7).runOnce().toArray());
        assertEquals(Linq.of("1", "2", "3", "4", "5", "6", "7", "8"),
                Linq.range(1, 8).select(i -> i.toString()).runOnce().toArray());
    }

    @Test
    public void ToArray_TouchCountWithICollection() {
        TestCollection<Integer> source = new TestCollection<>(new Integer[]{1, 2, 3, 4});
        Array<Integer> resultArray = source.toArray();

        assertEquals(source, resultArray);
        assertEquals(1, source.CountTouched);
    }

    @Test
    public void ToArray_ThrowArgumentNullExceptionWhenSourceIsNull() {
        int[] source = null;
        assertThrows(ArgumentNullException.class, () -> Linq.of(source).toArray());
    }

    // Generally the optimal approach. Anything that breaks this should be confirmed as not harming performance.
    @Test
    public void ToArray_UseCopyToWithICollection() {
        TestCollection<Integer> source = new TestCollection<>(new Integer[]{1, 2, 3, 4});
        Array<Integer> resultArray = source.toArray();

        assertEquals(source, resultArray);
        // assertEquals(1, source.CopyToTouched);
        assertEquals(1, source.ToArrayTouched); // This is different from C#
    }

    @Test
    public void ToArray_FailOnExtremelyLargeCollection() {
        IEnumerable<Byte> largeSeq = new FastInfiniteEnumerator<>();
        assertThrows(OutOfMemoryError.class, () -> largeSeq.toArray());
    }

    @Test
    public void ToArray_ArrayWhereSelect() {
        this.ToArray_ArrayWhereSelect(new int[]{}, new String[]{});
        this.ToArray_ArrayWhereSelect(new int[]{1}, new String[]{"1"});
        this.ToArray_ArrayWhereSelect(new int[]{1, 2, 3}, new String[]{"1", "2", "3"});
    }

    private void ToArray_ArrayWhereSelect(int[] sourceIntegers, String[] convertedStrings) {
        assertEquals(Linq.of(convertedStrings), Linq.of(sourceIntegers).select(i -> i.toString()).toArray());

        assertEquals(Linq.of(sourceIntegers), Linq.of(sourceIntegers).where(i -> true).toArray());
        assertEquals(Linq.empty(), Linq.of(sourceIntegers).where(i -> false).toArray());

        assertEquals(Linq.of(convertedStrings), Linq.of(sourceIntegers).where(i -> true).select(i -> i.toString()).toArray());
        assertEquals(Linq.empty(), Linq.of(sourceIntegers).where(i -> false).select(i -> i.toString()).toArray());

        assertEquals(Linq.of(convertedStrings), Linq.of(sourceIntegers).select(i -> i.toString()).where(s -> s != null).toArray());
        assertEquals(Linq.empty(), Linq.of(sourceIntegers).select(i -> i.toString()).where(s -> s == null).toArray());
    }

    @Test
    public void ToArray_ListWhereSelect() {
        this.ToArray_ListWhereSelect(new int[]{}, new String[]{});
        this.ToArray_ListWhereSelect(new int[]{1}, new String[]{"1"});
        this.ToArray_ListWhereSelect(new int[]{1, 2, 3}, new String[]{"1", "2", "3"});
    }

    private void ToArray_ListWhereSelect(int[] sourceIntegers, String[] convertedStrings) {
        List<Integer> sourceList = Linq.of(sourceIntegers).toList();

        assertEquals(Linq.of(convertedStrings), Linq.of(sourceList).select(i -> i.toString()).toArray());

        assertEquals(Linq.of(sourceList), Linq.of(sourceList).where(i -> true).toArray());
        assertEquals(Linq.empty(), Linq.of(sourceList).where(i -> false).toArray());

        assertEquals(Linq.of(convertedStrings), Linq.of(sourceList).where(i -> true).select(i -> i.toString()).toArray());
        assertEquals(Linq.empty(), Linq.of(sourceList).where(i -> false).select(i -> i.toString()).toArray());

        assertEquals(Linq.of(convertedStrings), Linq.of(sourceList).select(i -> i.toString()).where(s -> s != null).toArray());
        assertEquals(Linq.empty(), Linq.of(sourceList).select(i -> i.toString()).where(s -> s == null).toArray());
    }

    @Test
    public void SameResultsRepeatCallsFromWhereOnIntQuery() {
        IEnumerable<Integer> q = Linq.of(new int[]{9999, 0, 888, -1, 66, -777, 1, 2, -12345}).where(x -> x > Integer.MIN_VALUE);

        assertEquals(q.toArray(), q.toArray());
    }

    @Test
    public void SameResultsRepeatCallsFromWhereOnStringQuery() {
        IEnumerable<String> q = Linq.of("!@#$%^", "C", "AAA", "", "Calling Twice", "SoS", Empty).where(x -> !IsNullOrEmpty(x));

        assertEquals(q.toArray(), q.toArray());
    }

    @Test
    public void SameResultsButNotSameObject() {
        IEnumerable<Integer> qInt = Linq.of(new int[]{9999, 0, 888, -1, 66, -777, 1, 2, -12345}).where(x -> x > Integer.MIN_VALUE);
        IEnumerable<String> qString = Linq.of("!@#$%^", "C", "AAA", "", "Calling Twice", "SoS", Empty).where(x -> !IsNullOrEmpty(x));

        assertNotSame(qInt.toArray(), qInt.toArray());
        assertNotSame(qString.toArray(), qString.toArray());
        assertNotSame(qInt.toArray().getArray(), qInt.toArray().getArray());
        assertNotSame(qString.toArray().getArray(), qString.toArray().getArray());
    }

    @Test
    public void EmptyArraysSameObject() {
        // .NET Core returns the instance as an optimization.
        // see https://github.com/dotnet/corefx/pull/2401.
        assertSame(Linq.<Integer>empty().toArray(), Linq.<Integer>empty().toArray());

        Integer[] array = new Integer[0];
        assertNotSame(array, Linq.of(array).toArray().getArray());
    }

    @Test
    public void SourceIsEmptyICollectionT() {
        int[] source = {};

        ICollection<Integer> collection = Linq.of(source).toArray();

        assertEmpty(Linq.of(source).toArray());
        assertEmpty(collection.toArray());
    }

    @Test
    public void SourceIsICollectionTWithFewElements() {
        Integer[] source = {-5, null, 0, 10, 3, -1, null, 4, 9};
        Integer[] expected = {-5, null, 0, 10, 3, -1, null, 4, 9};

        ICollection<Integer> collection = Linq.of(source).toArray();

        assertEquals(Linq.of(expected), Linq.of(source).toArray());
        assertEquals(Linq.of(expected), collection.toArray());
    }

    @Test
    public void SourceNotICollectionAndIsEmpty() {
        IEnumerable<Integer> source = NumberRangeGuaranteedNotCollectionType(-4, 0);

        assertNull(as(source, ICollection.class));

        assertEmpty(source.toArray());
    }

    @Test
    public void SourceNotICollectionAndHasElements() {
        IEnumerable<Integer> source = NumberRangeGuaranteedNotCollectionType(-4, 10);
        int[] expected = {-4, -3, -2, -1, 0, 1, 2, 3, 4, 5};

        assertNull(as(source, ICollection.class));

        assertEquals(Linq.of(expected), source.toArray());
    }

    @Test
    public void SourceNotICollectionAndAllNull() {
        IEnumerable<Integer> source = RepeatedNullableNumberGuaranteedNotCollectionType(null, 5);
        Integer[] expected = {null, null, null, null, null};

        assertNull(as(source, ICollection.class));

        assertEquals(Linq.of(expected), source.toArray());
    }

    @Test
    public void ConstantTimeCountPartitionSelectSameTypeToArray() {
        IEnumerable<Integer> source = Linq.range(0, 100).select(i -> i * 2).skip(1).take(5);
        assertEquals(Linq.of(new int[]{2, 4, 6, 8, 10}), source.toArray());
    }

    @Test
    public void ConstantTimeCountPartitionSelectDiffTypeToArray() {
        IEnumerable<String> source = Linq.range(0, 100).select(i -> i.toString()).skip(1).take(5);
        assertEquals(Linq.of("1", "2", "3", "4", "5"), source.toArray());
    }

    @Test
    public void ConstantTimeCountEmptyPartitionSelectSameTypeToArray() {
        IEnumerable<Integer> source = Linq.range(0, 100).select(i -> i * 2).skip(1000);
        assertEmpty(source.toArray());
    }

    @Test
    public void ConstantTimeCountEmptyPartitionSelectDiffTypeToArray() {
        IEnumerable<String> source = Linq.range(0, 100).select(i -> i.toString()).skip(1000);
        assertEmpty(source.toArray());
    }

    @Test
    public void NonConstantTimeCountPartitionSelectSameTypeToArray() {
        IEnumerable<Integer> source = NumberRangeGuaranteedNotCollectionType(0, 100).orderBy(i -> i).select(i -> i * 2).skip(1).take(5);
        assertEquals(Linq.of(new int[]{2, 4, 6, 8, 10}), source.toArray());
    }

    @Test
    public void NonConstantTimeCountPartitionSelectDiffTypeToArray() {
        IEnumerable<String> source = NumberRangeGuaranteedNotCollectionType(0, 100).orderBy(i -> i).select(i -> i.toString()).skip(1).take(5);
        assertEquals(Linq.of("1", "2", "3", "4", "5"), source.toArray());
    }

    @Test
    public void NonConstantTimeCountEmptyPartitionSelectSameTypeToArray() {
        IEnumerable<Integer> source = NumberRangeGuaranteedNotCollectionType(0, 100).orderBy(i -> i).select(i -> i * 2).skip(1000);
        assertEmpty(source.toArray());
    }

    @Test
    public void NonConstantTimeCountEmptyPartitionSelectDiffTypeToArray() {
        IEnumerable<String> source = NumberRangeGuaranteedNotCollectionType(0, 100).orderBy(i -> i).select(i -> i.toString()).skip(1000);
        assertEmpty(source.toArray());
    }

    @Test
    public void ToArrayShouldWorkWithSpecialLengthLazyEnumerables() {
        for (Object[] objects : JustBelowPowersOfTwoLengths()) {
            this.ToArrayShouldWorkWithSpecialLengthLazyEnumerables((int) objects[0]);
        }
        for (Object[] objects : PowersOfTwoLengths()) {
            this.ToArrayShouldWorkWithSpecialLengthLazyEnumerables((int) objects[0]);
        }
        for (Object[] objects : JustAbovePowersOfTwoLengths()) {
            this.ToArrayShouldWorkWithSpecialLengthLazyEnumerables((int) objects[0]);
        }
    }

    private void ToArrayShouldWorkWithSpecialLengthLazyEnumerables(int length) {
        assertTrue(length >= 0);

        IEnumerable<Integer> range = Linq.range(0, length);
        IEnumerable<Integer> lazyEnumerable = ForceNotCollection(range); // We won't go down the IIListProvider path
        assertEquals(range, lazyEnumerable.toArray());
    }

    @Test
    public void ToArray_Cast() {
        Object[] source = {Enum0.First, Enum0.Second, Enum0.Third};
        IEnumerable<Enum0> cast = Linq.of(source).cast(Enum0.class);
        assertIsType(CastIterator.class, cast);
        Array<Enum0> castArray = cast.toArray();
        assertIsType(Array.class, castArray);
        Enum0[] castArray2 = cast.toArray(Enum0.class);
        assertIsType(Enum0[].class, castArray2);
        assertEquals(Linq.of(Enum0.First, Enum0.Second, Enum0.Third), castArray);
    }

    @Test
    public void testToArray() {
        Object[] source = {1, 2, 3};
        Object[] target = Linq.of(source).cast(Integer.class).toArray(Integer.class);
        assertEquals(3, target.length);
        assertTrue(Linq.of(source).sequenceEqual(Linq.of(target)));

        Set<Integer> source2 = new HashSet<>();
        source2.add(1);
        source2.add(2);
        source2.add(3);
        Integer[] target2 = Linq.of(source2).toArray(Integer.class);
        assertEquals(3, target2.length);
        assertTrue(Linq.of(target2).sequenceEqual(Linq.of(source2)));

        Character[] lst = Linq.of(Arrays.asList('h', 'e', 'l', 'l', 'o')).toArray(Character.class);
        assertEquals(5, lst.length);
        assertEquals("o", lst[4].toString());

        Character[] arrChar = {'h', 'e', 'l', 'l', 'o'};
        Character[] arr = Linq.of(arrChar).toArray(Character.class);
        assertEquals(5, arr.length);
        assertEquals("o", arr[4].toString());

        Character[] hello = Linq.of("hello").toArray(Character.class);
        assertEquals(5, hello.length);
        assertEquals("o", hello[4].toString());

        Character[] h = Linq.singleton('h').toArray(Character.class);
        assertEquals(1, h.length);
        assertEquals("h", h[0].toString());

        Character[] empty = Linq.<Character>empty().toArray(Character.class);
        assertEquals(0, empty.length);

        String[] array = {"a", "b"};
        Array<String> stringArray = Linq.of(array).toArray();
        stringArray.set(0, "c");
        assertEquals("a", array[0]);
        assertEquals("c", stringArray.get(0));

        assertEquals(Linq.of(true, false, false), Linq.of(new boolean[]{true, false, false}).toArray());
        assertEquals(Linq.of(true, false, false), Linq.of(Linq.of(new boolean[]{true, false, false}).toArray(Boolean.class)));

        assertEquals(Linq.of((byte) 1, (byte) 2, (byte) 3), Linq.of(new byte[]{1, 2, 3}).toArray());
        assertEquals(Linq.of((byte) 1, (byte) 2, (byte) 3), Linq.of(Linq.of(new byte[]{1, 2, 3}).toArray(Byte.class)));

        assertEquals(Linq.of((short) 1, (short) 2, (short) 3), Linq.of(new short[]{1, 2, 3}).toArray());
        assertEquals(Linq.of((short) 1, (short) 2, (short) 3), Linq.of(Linq.of(new short[]{1, 2, 3}).toArray(Short.class)));

        assertEquals(Linq.of(1, 2, 3), Linq.of(new int[]{1, 2, 3}).toArray());
        assertEquals(Linq.of(1, 2, 3), Linq.of(Linq.of(new int[]{1, 2, 3}).toArray(Integer.class)));

        assertEquals(Linq.of(1L, 2L, 3L), Linq.of(new long[]{1, 2, 3}).toArray());
        assertEquals(Linq.of(1L, 2L, 3L), Linq.of(Linq.of(new long[]{1, 2, 3}).toArray(Long.class)));

        assertEquals(Linq.of('a', 'b', 'c'), Linq.of(new char[]{'a', 'b', 'c'}).toArray());
        assertEquals(Linq.of('a', 'b', 'c'), Linq.of(Linq.of(new char[]{'a', 'b', 'c'}).toArray(Character.class)));

        assertEquals(Linq.of(1f, 2f, 3f), Linq.of(new float[]{1f, 2f, 3f}).toArray());
        assertEquals(Linq.of(1f, 2f, 3f), Linq.of(Linq.of(new float[]{1f, 2f, 3f}).toArray(Float.class)));

        assertEquals(Linq.of(1d, 2d, 3d), Linq.of(new double[]{1d, 2d, 3d}).toArray());
        assertEquals(Linq.of(1d, 2d, 3d), Linq.of(Linq.of(new double[]{1d, 2d, 3d}).toArray(Double.class)));
    }


    // Consider that two very similar enums is not unheard of, if e.g. two assemblies map the
    // same external source of numbers (codes, response codes, colour codes, etc.) to values.
    private enum Enum0 {
        First,
        Second,
        Third
    }
}
