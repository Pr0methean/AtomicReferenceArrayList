package io.github.pr0methean.util;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.testng.annotations.Test;

/**
 * Based on http://gee.cs.oswego.edu/cgi-bin/viewcvs.cgi/jsr166/src/test/tck/AtomicReferenceArrayListTest.java?view=co
 */
public class AtomicReferenceArrayListTest extends JSR166TestCase {

    static AtomicReferenceArrayList<Integer> populatedArray(int n) {
        AtomicReferenceArrayList<Integer> a = new AtomicReferenceArrayList<>();
        assertTrue(a.isEmpty());
        for (int i = 0; i < n; i++) {
            System.out.println("Current list: " + a);
            a.add(i);
        }
        assertFalse(a.isEmpty());
        assertEquals(n, a.size());
        return a;
    }

    static AtomicReferenceArrayList<Integer> populatedArray(Integer[] elements) {
        AtomicReferenceArrayList<Integer> a = new AtomicReferenceArrayList<>();
        assertTrue(a.isEmpty());
        for (Integer element : elements)
            a.add(element);
        assertFalse(a.isEmpty());
        assertEquals(elements.length, a.size());
        return a;
    }

    /**
     * a new list is empty
     */
    @Test
    public void testConstructor() {
        AtomicReferenceArrayList a = new AtomicReferenceArrayList();
        assertTrue(a.isEmpty());
    }

    /**
     * new list contains all elements of initializing array
     */
    @Test
    public void testConstructor2() {
        Integer[] ints = new Integer[SIZE];
        for (int i = 0; i < SIZE - 1; ++i)
            ints[i] = new Integer(i);
        AtomicReferenceArrayList<? extends Object> a = new AtomicReferenceArrayList<>(ints);
        for (int i = 0; i < SIZE; ++i)
            assertEquals(ints[i], a.get(i));
    }

    /**
     * new list contains all elements of initializing collection
     */
    @Test
    public void testConstructor3() {
        Integer[] ints = new Integer[SIZE];
        for (int i = 0; i < SIZE - 1; ++i)
            ints[i] = new Integer(i);
        AtomicReferenceArrayList<?> a = new AtomicReferenceArrayList<>(Arrays.asList(ints));
        for (int i = 0; i < SIZE; ++i)
            assertEquals(ints[i], a.get(i));
    }

    /**
     * addAll adds each element from the given collection, including duplicates
     */
    @Test
    public void testAddAll() {
        AtomicReferenceArrayList<Integer> full = populatedArray(3);
        assertTrue(full.addAll(Arrays.asList(three, four, five)));
        assertEquals(6, full.size());
        assertTrue(full.addAll(Arrays.asList(three, four, five)));
        assertEquals(9, full.size());
    }

    /**
     * addAllAbsent adds each element from the given collection that did not
     * already exist in the List
     */
    @Test
    public void testAddAllAbsent() {
        AtomicReferenceArrayList<Integer> full = populatedArray(3);
        // "one" is duplicate and will not be added
        assertEquals(2, full.addAllAbsent(Arrays.asList(three, four, one)));
        assertEquals(5, full.size());
        assertEquals(0, full.addAllAbsent(Arrays.asList(three, four, one)));
        assertEquals(5, full.size());
    }

    /**
     * addIfAbsent will not add the element if it already exists in the list
     */
    @Test
    public void testAddIfAbsent() {
        AtomicReferenceArrayList<Integer> full = populatedArray(SIZE);
        full.addIfAbsent(one);
        assertEquals(SIZE, full.size());
    }

    /**
     * addIfAbsent adds the element when it does not exist in the list
     */
    @Test
    public void testAddIfAbsent2() {
        AtomicReferenceArrayList<Integer> full = populatedArray(SIZE);
        full.addIfAbsent(three);
        assertTrue(full.contains(three));
    }

    /**
     * clear removes all elements from the list
     */
    @Test
    public void testClear() {
        AtomicReferenceArrayList<? extends Integer> full = populatedArray(SIZE);
        full.clear();
        assertEquals(0, full.size());
    }

    /**
     * Cloned list is equal
     */
    @Test
    public void testClone() {
        AtomicReferenceArrayList<? extends Integer> l1 = populatedArray(SIZE);
        AtomicReferenceArrayList<? extends Integer> l2 = (l1.clone());
        assertEquals(l1, l2);
        l1.clear();
        assertFalse(l1.equals(l2));
    }

    /**
     * contains is true for added elements
     */
    @Test
    public void testContains() {
        AtomicReferenceArrayList<? extends Integer> full = populatedArray(3);
        assertTrue(full.contains(one));
        assertFalse(full.contains(five));
    }

    /**
     * adding at an index places it in the indicated index
     */
    @Test
    public void testAddIndex() {
        AtomicReferenceArrayList<Integer> full = populatedArray(3);
        full.add(0, m1);
        assertEquals(4, full.size());
        assertEquals(m1, full.get(0));
        assertEquals(zero, full.get(1));

        full.add(2, m2);
        assertEquals(5, full.size());
        assertEquals(m2, full.get(2));
        assertEquals(two, full.get(4));
    }

    /**
     * lists with same elements are equal and have same hashCode
     */
    @Test
    public void testEquals() {
        AtomicReferenceArrayList a = populatedArray(3);
        AtomicReferenceArrayList b = populatedArray(3);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.containsAll(b));
        assertTrue(b.containsAll(a));
        assertEquals(a.hashCode(), b.hashCode());
        a.add(m1);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        assertTrue(a.containsAll(b));
        assertFalse(b.containsAll(a));
        b.add(m1);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertTrue(a.containsAll(b));
        assertTrue(b.containsAll(a));
        assertEquals(a.hashCode(), b.hashCode());

        assertFalse(a.equals(null));
    }

    /**
     * containsAll returns true for collections with subset of elements
     */
    @Test
    public void testContainsAll() {
        AtomicReferenceArrayList<? extends Integer> full = populatedArray(3);
        assertTrue(full.containsAll(Arrays.asList()));
        assertTrue(full.containsAll(Arrays.asList(one)));
        assertTrue(full.containsAll(Arrays.asList(one, two)));
        assertFalse(full.containsAll(Arrays.asList(one, two, six)));
        assertFalse(full.containsAll(Arrays.asList(six)));

        try {
            full.containsAll(null);
            shouldThrow();
        } catch (NullPointerException success) {}
    }

    /**
     * get returns the value at the given index
     */
    @Test
    public void testGet() {
        AtomicReferenceArrayList<Integer> full = populatedArray(3);
        assertEquals((int) 0, (int) full.get(0));
    }

    /**
     * indexOf gives the index for the given object
     */
    @Test
    public void testIndexOf() {
        AtomicReferenceArrayList<? extends Integer> full = populatedArray(3);
        assertEquals(1, full.indexOf(one));
        assertEquals(-1, full.indexOf("puppies"));
    }

    /**
     * indexOf gives the index based on the given index
     * at which to start searching
     */
    @Test
    public void testIndexOf2() {
        AtomicReferenceArrayList<Integer> full = populatedArray(3);
        assertEquals(1, full.indexOf(one, 0));
        assertEquals(-1, full.indexOf(one, 2));
    }

    /**
     * isEmpty returns true when empty, else false
     */
    @Test
    public void testIsEmpty() {
        AtomicReferenceArrayList empty = new AtomicReferenceArrayList();
        AtomicReferenceArrayList<? extends Integer> full = populatedArray(SIZE);
        assertTrue(empty.isEmpty());
        assertFalse(full.isEmpty());
    }

    /**
     * iterator() returns an iterator containing the elements of the
     * list in insertion order
     */
    @Test
    public void testIterator() {
        Collection empty = new AtomicReferenceArrayList();
        assertFalse(empty.iterator().hasNext());
        try {
            empty.iterator().next();
            shouldThrow();
        } catch (NoSuchElementException success) {}

        Integer[] elements = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++)
            elements[i] = i;
        shuffle(elements);
        Collection<Integer> full = populatedArray(elements);

        Iterator it = full.iterator();
        for (int j = 0; j < SIZE; j++) {
            assertTrue(it.hasNext());
            assertEquals(elements[j], it.next());
        }
        assertIteratorExhausted(it);
    }

    private static void assertIteratorExhausted(Iterator it) {
        assertFalse(it.hasNext(), "Iterator not exhausted when expected");
    }

    /**
     * iterator of empty collection has no elements
     */
    @Test
    public void testEmptyIterator() {
        Collection c = new AtomicReferenceArrayList();
        assertIteratorExhausted(c.iterator());
    }

    /**
     * iterator.remove throws UnsupportedOperationException
     */
    @Test
    public void testIteratorRemove() {
        AtomicReferenceArrayList full = populatedArray(SIZE);
        Iterator it = full.iterator();
        it.next();
        try {
            it.remove();
            shouldThrow();
        } catch (UnsupportedOperationException success) {}
    }

    /**
     * toString contains toString of elements
     */
    @Test
    public void testToString() {
        assertEquals("[]", new AtomicReferenceArrayList().toString());
        AtomicReferenceArrayList<? extends Integer> full = populatedArray(3);
        String s = full.toString();
        for (int i = 0; i < 3; ++i)
            assertTrue(s.contains(String.valueOf(i)));
        assertEquals(new ArrayList(full).toString(),
                full.toString());
    }

    /**
     * lastIndexOf returns the index for the given object
     */
    @Test
    public void testLastIndexOf1() {
        AtomicReferenceArrayList<Integer> full = populatedArray(3);
        full.add(one);
        full.add(three);
        assertEquals(3, full.lastIndexOf(one));
        assertEquals(-1, full.lastIndexOf(six));
    }

    /**
     * lastIndexOf returns the index from the given starting point
     */
    @Test
    public void testLastIndexOf2() {
        AtomicReferenceArrayList<Integer> full = populatedArray(3);
        full.add(one);
        full.add(three);
        assertEquals(3, full.lastIndexOf(one, 4));
        assertEquals(-1, full.lastIndexOf(three, 3));
    }

    /**
     * listIterator traverses all elements
     */
    @Test
    public void testListIterator1() {
        AtomicReferenceArrayList full = populatedArray(SIZE);
        ListIterator i = full.listIterator();
        int j;
        for (j = 0; i.hasNext(); j++)
            assertEquals(j, i.next());
        assertEquals(SIZE, j);
    }

    /**
     * listIterator only returns those elements after the given index
     */
    @Test
    public void testListIterator2() {
        AtomicReferenceArrayList full = populatedArray(3);
        ListIterator i = full.listIterator(1);
        int j;
        for (j = 0; i.hasNext(); j++)
            assertEquals(j + 1, i.next());
        assertEquals(2, j);
    }

    /**
     * remove(int) removes and returns the object at the given index
     */
    @Test
    public void testRemove_int() {
        int SIZE = 3;
        for (int i = 0; i < SIZE; i++) {
            AtomicReferenceArrayList<Integer> full = populatedArray(SIZE);
            assertEquals((int) i, (int) full.remove(i));
            assertEquals(SIZE - 1, full.size());
            assertFalse(full.contains(new Integer(i)));
        }
    }

    /**
     * remove(Object) removes the object if found and returns true
     */
    @Test
    public void testRemove_Object() {
        int SIZE = 3;
        for (int i = 0; i < SIZE; i++) {
            AtomicReferenceArrayList<? extends Integer> full = populatedArray(SIZE);
            assertFalse(full.remove(new Integer(-42)));
            assertTrue(full.remove(new Integer(i)));
            assertEquals(SIZE - 1, full.size());
            assertFalse(full.contains(new Integer(i)));
        }
        AtomicReferenceArrayList<? extends List<Integer>> x = new AtomicReferenceArrayList<>(Arrays.asList(4, 5, 6));
        assertTrue(x.remove(new Integer(6)));
        assertEquals(x, Arrays.asList(4, 5));
        assertTrue(x.remove(new Integer(4)));
        assertEquals(x, Arrays.asList(5));
        assertTrue(x.remove(new Integer(5)));
        assertEquals(x, Arrays.asList());
        assertFalse(x.remove(new Integer(5)));
    }

    /**
     * removeAll removes all elements from the given collection
     */
    @Test
    public void testRemoveAll() {
        AtomicReferenceArrayList<? extends Integer> full = populatedArray(3);
        assertTrue(full.removeAll(Arrays.asList(one, two)));
        assertEquals(1, full.size());
        assertFalse(full.removeAll(Arrays.asList(one, two)));
        assertEquals(1, full.size());
    }

    /**
     * set changes the element at the given index
     */
    @Test
    public void testSet() {
        AtomicReferenceArrayList<Integer> full = populatedArray(3);
        assertEquals(2, (int) full.set(2, four));
        assertEquals(4, (int) full.get(2));
    }

    /**
     * size returns the number of elements
     */
    @Test
    public void testSize() {
        AtomicReferenceArrayList empty = new AtomicReferenceArrayList();
        AtomicReferenceArrayList<? extends Integer> full = populatedArray(SIZE);
        assertEquals(SIZE, full.size());
        assertEquals(0, empty.size());
    }

    /**
     * toArray() returns an Object array containing all elements from
     * the list in insertion order
     */
    @Test
    public void testToArray() {
        Object[] a = new AtomicReferenceArrayList().toArray();
        assertTrue(Arrays.equals(new Object[0], a));
        assertSame(Object[].class, a.getClass());

        Integer[] elements = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++)
            elements[i] = i;
        shuffle(elements);
        Collection<Integer> full = populatedArray(elements);

        assertTrue(Arrays.equals(elements, full.toArray()));
        assertSame(Object[].class, full.toArray().getClass());
    }

    private static <T> void shuffle(T[] elements) {
        Collections.shuffle(Arrays.asList(elements));
    }

    /**
     * toArray(Integer array) returns an Integer array containing all
     * elements from the list in insertion order
     */
    @Test
    public void testToArray2() {
        Collection empty = new AtomicReferenceArrayList();
        Integer[] a;

        a = new Integer[0];
        assertSame(a, empty.toArray(a));

        a = new Integer[SIZE / 2];
        Arrays.fill(a, 42);
        assertSame(a, empty.toArray(a));
        assertNull(a[0]);
        for (int i = 1; i < a.length; i++)
            assertEquals(42, (int) a[i]);

        Integer[] elements = new Integer[SIZE];
        for (int i = 0; i < SIZE; i++)
            elements[i] = i;
        shuffle(elements);
        Collection<Integer> full = populatedArray(elements);

        Arrays.fill(a, 42);
        assertTrue(Arrays.equals(elements, full.toArray(a)));
        for (int i = 0; i < a.length; i++)
            assertEquals(42, (int) a[i]);
        assertSame(Integer[].class, full.toArray(a).getClass());

        a = new Integer[SIZE];
        Arrays.fill(a, 42);
        assertSame(a, full.toArray(a));
        assertTrue(Arrays.equals(elements, a));

        a = new Integer[2 * SIZE];
        Arrays.fill(a, 42);
        assertSame(a, full.toArray(a));
        assertTrue(Arrays.equals(elements, Arrays.copyOf(a, SIZE)));
        assertNull(a[SIZE]);
        for (int i = SIZE + 1; i < a.length; i++)
            assertEquals(42, (int) a[i]);
    }

    /**
     * sublists contains elements at indexes offset from their base
     */
    @Test
    public void testSubList() {
        AtomicReferenceArrayList<Integer> a = populatedArray(10);
        assertTrue(a.subList(1,1).isEmpty());
        for (int j = 0; j < 9; ++j) {
            for (int i = j ; i < 10; ++i) {
                List<Integer> b = a.subList(j,i);
                for (int k = j; k < i; ++k) {
                    assertEquals(new Integer(k), b.get(k-j));
                }
            }
        }

        List<Integer> s = a.subList(2, 5);
        assertEquals(3, s.size());
        s.set(2, m1);
        assertEquals(a.get(4), m1);
        s.clear();
        assertEquals(7, a.size());
    }

    // Exception tests

    /**
     * toArray throws an ArrayStoreException when the given array
     * can not store the objects inside the list
     */
    @Test
    public void testToArray_ArrayStoreException() {
        AtomicReferenceArrayList<String> c = new AtomicReferenceArrayList<>();
        c.add("zfasdfsdf");
        c.add("asdadasd");
        try {
            c.toArray(new Long[5]);
            shouldThrow();
        } catch (ArrayStoreException success) {}
    }

    /**
     * get throws an IndexOutOfBoundsException on a negative index
     */
    @Test
    public void testGet1_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List<? extends Object> list : lists) {
            try {
                list.get(-1);
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * get throws an IndexOutOfBoundsException on a too high index
     */
    @Test
    public void testGet2_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List<? extends Object> list : lists) {
            try {
                list.get(list.size());
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * set throws an IndexOutOfBoundsException on a negative index
     */
    @Test
    public void testSet1_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List list : lists) {
            try {
                list.set(-1, "qwerty");
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * set throws an IndexOutOfBoundsException on a too high index
     */
    @Test
    public void testSet2() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List list : lists) {
            try {
                list.set(list.size(), "qwerty");
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * add throws an IndexOutOfBoundsException on a negative index
     */
    @Test
    public void testAdd1_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List list : lists) {
            try {
                list.add(-1, "qwerty");
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * add throws an IndexOutOfBoundsException on a too high index
     */
    @Test
    public void testAdd2_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List list : lists) {
            try {
                list.add(list.size() + 1, "qwerty");
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * remove throws an IndexOutOfBoundsException on a negative index
     */
    @Test
    public void testRemove1_IndexOutOfBounds() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List<? extends Object> list : lists) {
            try {
                list.remove(-1);
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * remove throws an IndexOutOfBoundsException on a too high index
     */
    @Test
    public void testRemove2_IndexOutOfBounds() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List<? extends Object> list : lists) {
            try {
                list.remove(list.size());
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * addAll throws an IndexOutOfBoundsException on a negative index
     */
    @Test
    public void testAddAll1_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List list : lists) {
            try {
                list.addAll(-1, new LinkedList());
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * addAll throws an IndexOutOfBoundsException on a too high index
     */
    @Test
    public void testAddAll2_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List list : lists) {
            try {
                list.addAll(list.size() + 1, new LinkedList());
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * listIterator throws an IndexOutOfBoundsException on a negative index
     */
    @Test
    public void testListIterator1_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List<? extends Object> list : lists) {
            try {
                list.listIterator(-1);
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * listIterator throws an IndexOutOfBoundsException on a too high index
     */
    @Test
    public void testListIterator2_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List<? extends Object> list : lists) {
            try {
                list.listIterator(list.size() + 1);
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * subList throws an IndexOutOfBoundsException on a negative index
     */
    @Test
    public void testSubList1_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List<? extends Object> list : lists) {
            try {
                list.subList(-1, list.size());
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * subList throws an IndexOutOfBoundsException on a too high index
     */
    @Test
    public void testSubList2_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List<? extends Object> list : lists) {
            try {
                list.subList(0, list.size() + 1);
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * subList throws IndexOutOfBoundsException when the second index
     * is lower then the first
     */
    @Test
    public void testSubList3_IndexOutOfBoundsException() {
        AtomicReferenceArrayList<? extends Integer> c = populatedArray(5);
        List[] lists = { c, c.subList(1, c.size() - 1) };
        for (List<? extends Object> list : lists) {
            try {
                list.subList(list.size() - 1, 1);
                shouldThrow();
            } catch (IndexOutOfBoundsException success) {}
        }
    }

    /**
     * a deserialized/reserialized list equals original
     */
    @Test
    public void testSerialization() throws Exception {
        List<Integer> x = populatedArray(SIZE);
        List<Integer> y = serialClone(x);

        assertNotSame(x, y);
        assertEquals(x.size(), y.size());
        assertEquals(x.toString(), y.toString());
        assertTrue(Arrays.equals(x.toArray(), y.toArray()));
        assertEquals(x, y);
        assertEquals(y, x);
        while (!x.isEmpty()) {
            assertFalse(y.isEmpty());
            assertEquals(x.remove(0), y.remove(0));
        }
        assertTrue(y.isEmpty());
    }
    
}