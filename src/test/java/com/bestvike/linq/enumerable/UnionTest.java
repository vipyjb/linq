package com.bestvike.linq.enumerable;

import com.bestvike.TestCase;
import com.bestvike.collections.generic.EqualityComparer;
import com.bestvike.collections.generic.IEqualityComparer;
import com.bestvike.collections.generic.StringComparer;
import com.bestvike.linq.IEnumerable;
import com.bestvike.linq.IEnumerator;
import com.bestvike.linq.Linq;
import com.bestvike.linq.entity.Employee;
import com.bestvike.linq.exception.ArgumentNullException;
import com.bestvike.linq.util.HashSet;
import org.junit.Test;

import java.util.Objects;

/**
 * Created by 许崇雷 on 2018-05-10.
 */
public class UnionTest extends TestCase {
    @Test
    public void SameResultsRepeatCallsIntQuery() {
        IEnumerable<Integer> q1 = Linq.of(2, 3, null, 2, null, 4, 5);
        IEnumerable<Integer> q2 = Linq.of(1, 9, null, 4);

        assertEquals(q1.union(q2), q1.union(q2));
    }

    @Test
    public void SameResultsRepeatCallsStringQuery() {
        IEnumerable<String> q1 = Linq.of("AAA", Empty, "q", "C", "#", "!@#$%^", "0987654321", "Calling Twice");
        IEnumerable<String> q2 = Linq.of("!@#$%^", "C", "AAA", "", "Calling Twice", "SoS");

        assertEquals(q1.union(q2), q1.union(q2));
    }

    @Test
    public void SameResultsRepeatCallsMultipleUnions() {
        IEnumerable<Integer> q1 = Linq.of(2, 3, null, 2, null, 4, 5);
        IEnumerable<Integer> q2 = Linq.of(1, 9, null, 4);
        IEnumerable<Integer> q3 = Linq.of(null, 8, 2, 2, 3);

        assertEquals(q1.union(q2).union(q3), q1.union(q2).union(q3));
    }

    @Test
    public void BothEmpty() {
        int[] first = {};
        int[] second = {};
        assertEmpty(Linq.of(first).union(Linq.of(second)));
    }

    @Test
    public void ManyEmpty() {
        int[] first = {};
        int[] second = {};
        int[] third = {};
        int[] fourth = {};
        assertEmpty(Linq.of(first).union(Linq.of(second)).union(Linq.of(third)).union(Linq.of(fourth)));
    }

    @Test
    public void CustomComparer() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};
        String[] expected = {"Bob", "Robert", "Tim", "Matt", "Charlie"};

        AnagramEqualityComparer comparer = new AnagramEqualityComparer();
        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second), comparer), comparer);
    }

    @Test
    public void RunOnce() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};
        String[] expected = {"Bob", "Robert", "Tim", "Matt", "Charlie"};

        AnagramEqualityComparer comparer = new AnagramEqualityComparer();
        assertEquals(Linq.of(expected), Linq.of(first).runOnce().union(Linq.of(second).runOnce(), comparer), comparer);
    }

    @Test
    public void FirstNullCustomComparer() {
        String[] first = null;
        String[] second = {"ttaM", "Charlie", "Bbo"};

        assertThrows(ArgumentNullException.class, () -> Linq.of(first).union(Linq.of(second), new AnagramEqualityComparer()));
    }

    @Test
    public void SecondNullCustomComparer() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = null;

        assertThrows(ArgumentNullException.class, () -> Linq.of(first).union(Linq.of(second), new AnagramEqualityComparer()));
    }

    @Test
    public void FirstNullNoComparer() {
        String[] first = null;
        String[] second = {"ttaM", "Charlie", "Bbo"};

        assertThrows(ArgumentNullException.class, () -> Linq.of(first).union(Linq.of(second)));
    }

    @Test
    public void SecondNullNoComparer() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = null;

        assertThrows(ArgumentNullException.class, () -> Linq.of(first).union(Linq.of(second)));
    }

    @Test
    public void SingleNullWithEmpty() {
        String[] first = {null};
        String[] second = new String[0];
        String[] expected = {null};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second), EqualityComparer.Default()));
    }

    @Test
    public void NullEmptyStringMix() {
        String[] first = {null, null, Empty};
        String[] second = {null, null};
        String[] expected = {null, Empty};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second), EqualityComparer.Default()));
    }

    @Test
    public void DoubleNullWithEmpty() {
        String[] first = {null, null};
        String[] second = new String[0];
        String[] expected = {null};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second), EqualityComparer.Default()));
    }

    @Test
    public void EmptyWithNonEmpty() {
        int[] first = {};
        int[] second = {2, 4, 5, 3, 2, 3, 9};
        int[] expected = {2, 4, 5, 3, 9};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second)));
    }

    @Test
    public void NonEmptyWithEmpty() {
        int[] first = {2, 4, 5, 3, 2, 3, 9};
        int[] second = {};
        int[] expected = {2, 4, 5, 3, 9};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second)));
    }

    @Test
    public void CommonElementsShared() {
        int[] first = {1, 2, 3, 4, 5, 6};
        int[] second = {6, 7, 7, 7, 8, 1};
        int[] expected = {1, 2, 3, 4, 5, 6, 7, 8};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second)));
    }

    @Test
    public void SameElementRepeated() {
        int[] first = {1, 1, 1, 1, 1, 1};
        int[] second = {1, 1, 1, 1, 1, 1};
        int[] expected = {1};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second)));
    }

    @Test
    public void RepeatedElementsWithSingleElement() {
        int[] first = {1, 2, 3, 5, 3, 6};
        int[] second = {7};
        int[] expected = {1, 2, 3, 5, 6, 7};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second)));
    }

    @Test
    public void SingleWithAllUnique() {
        Integer[] first = {2};
        Integer[] second = {3, null, 4, 5};
        Integer[] expected = {2, 3, null, 4, 5};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second)));
    }

    @Test
    public void EachHasRepeatsBetweenAndAmongstThemselves() {
        Integer[] first = {1, 2, 3, 4, null, 5, 1};
        Integer[] second = {6, 2, 3, 4, 5, 6};
        Integer[] expected = {1, 2, 3, 4, null, 5, 6};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second)));
    }

    @Test
    public void EachHasRepeatsBetweenAndAmongstThemselvesMultipleUnions() {
        Integer[] first = {1, 2, 3, 4, null, 5, 1};
        Integer[] second = {6, 2, 3, 4, 5, 6};
        Integer[] third = {2, 8, 2, 3, 2, 8};
        Integer[] fourth = {null, 1, 7, 2, 7};
        Integer[] expected = {1, 2, 3, 4, null, 5, 6, 8, 7};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second)).union(Linq.of(third)).union(Linq.of(fourth)));
    }

    @Test
    public void MultipleUnionsCustomComparer() {
        Integer[] first = {1, 102, 903, 204, null, 5, 601};
        Integer[] second = {6, 202, 903, 204, 5, 106};
        Integer[] third = {2, 308, 2, 103, 802, 308};
        Integer[] fourth = {null, 101, 207, 202, 207};
        Integer[] expected = {1, 102, 903, 204, null, 5, 6, 308, 207};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second), new Modulo100EqualityComparer()).union(Linq.of(third), new Modulo100EqualityComparer()).union(Linq.of(fourth), new Modulo100EqualityComparer()));
    }

    @Test
    public void MultipleUnionsDifferentComparers() {
        String[] first = {"Alpha", "Bravo", "Charlie", "Bravo", "Delta", "atleD", "ovarB"};
        String[] second = {"Charlie", "Delta", "Echo", "Foxtrot", "Foxtrot", "choE"};
        String[] third = {"trotFox", "Golf", "Alpha", "choE", "Tango"};

        String[] plainThenAnagram = {"Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "Golf", "Tango"};
        String[] anagramThenPlain = {"Alpha", "Bravo", "Charlie", "Delta", "Echo", "Foxtrot", "trotFox", "Golf", "choE", "Tango"};

        assertEquals(Linq.of(plainThenAnagram), Linq.of(first).union(Linq.of(second)).union(Linq.of(third), new AnagramEqualityComparer()));
        assertEquals(Linq.of(anagramThenPlain), Linq.of(first).union(Linq.of(second), new AnagramEqualityComparer()).union(Linq.of(third)));
    }

    @Test
    public void NullEqualityComparer() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};
        String[] expected = {"Bob", "Robert", "Tim", "Matt", "miT", "ttaM", "Charlie", "Bbo"};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second), null));
    }

    @Test
    public void ForcedToEnumeratorDoesntEnumerate() {
        IEnumerable<Integer> iterator = NumberRangeGuaranteedNotCollectionType(0, 3).union(Linq.range(0, 3));
        // Don't insist on this behaviour, but check it's correct if it happens
        IEnumerator<Integer> en = (IEnumerator<Integer>) iterator;
        assertFalse(en != null && en.moveNext());
    }

    @Test
    public void ForcedToEnumeratorDoesntEnumerateMultipleUnions() {
        IEnumerable<Integer> iterator = NumberRangeGuaranteedNotCollectionType(0, 3).union(Linq.range(0, 3)).union(Linq.range(2, 4)).union(Linq.of(new int[]{
                9, 2, 4
        }));
        // Don't insist on this behaviour, but check it's correct if it happens
        IEnumerator<Integer> en = (IEnumerator<Integer>) iterator;
        assertFalse(en != null && en.moveNext());
    }

    @Test
    public void ToArray() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};
        String[] expected = {"Bob", "Robert", "Tim", "Matt", "miT", "ttaM", "Charlie", "Bbo"};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second)).toArray());
    }

    @Test
    public void ToArrayMultipleUnion() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};
        String[] third = {"Bob", "Albert", "Tim"};
        String[] expected = {"Bob", "Robert", "Tim", "Matt", "miT", "ttaM", "Charlie", "Bbo", "Albert"};

        assertEquals(Linq.of(expected), Linq.of(first).union(Linq.of(second)).union(Linq.of(third)).toArray());
    }

    @Test
    public void ToList() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};
        String[] expected = {"Bob", "Robert", "Tim", "Matt", "miT", "ttaM", "Charlie", "Bbo"};

        assertEquals(Linq.of(expected), Linq.of(Linq.of(first).union(Linq.of(second)).toList()));
    }

    @Test
    public void ToListMultipleUnion() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};
        String[] third = {"Bob", "Albert", "Tim"};
        String[] expected = {"Bob", "Robert", "Tim", "Matt", "miT", "ttaM", "Charlie", "Bbo", "Albert"};

        assertEquals(Linq.of(expected), Linq.of(Linq.of(first).union(Linq.of(second)).union(Linq.of(third)).toList()));
    }

    @Test
    public void Count() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};

        assertEquals(8, Linq.of(first).union(Linq.of(second)).count());
    }

    @Test
    public void CountMultipleUnion() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};
        String[] third = {"Bob", "Albert", "Tim"};

        assertEquals(9, Linq.of(first).union(Linq.of(second)).union(Linq.of(third)).count());
    }

    @Test
    public void RepeatEnumerating() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};

        IEnumerable<String> result = Linq.of(first).union(Linq.of(second));

        assertEquals(result, result);
    }

    @Test
    public void RepeatEnumeratingMultipleUnions() {
        String[] first = {"Bob", "Robert", "Tim", "Matt", "miT"};
        String[] second = {"ttaM", "Charlie", "Bbo"};
        String[] third = {"Matt", "Albert", "Ichabod"};

        IEnumerable<String> result = Linq.of(first).union(Linq.of(second)).union(Linq.of(third));
        assertEquals(result, result);
    }

    @Test
    public void HashSetWithBuiltInComparer_HashSetContainsNotUsed() {
        HashSet<String> set1 = new HashSet<>(StringComparer.OrdinalIgnoreCase);
        set1.add("a");
        IEnumerable<String> input1 = Linq.of(set1);
        IEnumerable<String> input2 = Linq.of(new String[]{"A"});

        assertEquals(Linq.of("a", "A"), input1.union(input2));
        assertEquals(Linq.of("a", "A"), input1.union(input2, null));
        assertEquals(Linq.of("a", "A"), input1.union(input2, EqualityComparer.Default()));
        assertEquals(Linq.of(new String[]{"a"}), input1.union(input2, StringComparer.OrdinalIgnoreCase));

        assertEquals(Linq.of("A", "a"), input2.union(input1));
        assertEquals(Linq.of("A", "a"), input2.union(input1, null));
        assertEquals(Linq.of("A", "a"), input2.union(input1, EqualityComparer.Default()));
        assertEquals(Linq.of(new String[]{"A"}), input2.union(input1, StringComparer.OrdinalIgnoreCase));
    }

    @Test
    public void testUnion() {
        assertEquals(6, Linq.of(emps)
                .union(Linq.of(badEmps))
                .union(Linq.of(emps))
                .count());
    }

    @Test
    public void testUnionWithComparer() {
        IEqualityComparer<Employee> comparer = new IEqualityComparer<Employee>() {
            @Override
            public boolean equals(Employee x, Employee y) {
                return Objects.equals(x.deptno, y.deptno);
            }

            @Override
            public int hashCode(Employee obj) {
                return obj.deptno == null ? 0 : obj.deptno;
            }
        };

        assertEquals(4, Linq.of(emps)
                .union(Linq.of(badEmps), comparer)
                .union(Linq.of(emps), comparer)
                .count());
    }


    private static final class Modulo100EqualityComparer implements IEqualityComparer<Integer> {
        @Override
        public boolean equals(Integer x, Integer y) {
            if (x == null)
                return y == null;
            if (y == null)
                return false;
            return x % 100 == y % 100;
        }

        @Override
        public int hashCode(Integer obj) {
            return obj != null ? obj % 100 + 1 : 0;
        }

        @Override
        public boolean equals(Object obj) {
            // Equal to all other instances.
            return obj instanceof Modulo100EqualityComparer;
        }

        @Override
        public int hashCode() {
            return 0xAFFAB1E; // Any number as long as it's constant.
        }
    }
}
