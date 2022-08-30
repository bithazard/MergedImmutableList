package bithazard.util.collection;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Named.named;

@SuppressWarnings({"unchecked", "rawtypes"})
class MergedImmutableListTest {
    private static final String EMPTY_LIST_PROVIDER_FQN = "bithazard.util.collection.MergedImmutableListTest#emptyListImplementations";
    private static final String FILLED_LIST_PROVIDER_FQN = "bithazard.util.collection.MergedImmutableListTest#filledListImplementations";
    private static final String DUPLICATED_ENTRIES_LIST_PROVIDER_FQN = "bithazard.util.collection.MergedImmutableListTest#duplicatedEntriesListImplementations";
    //The lists are expected in these sizes and with these items in the tests
    private static final List<Object> REFERENCE_EMPTY_LIST = List.of();
    private static final List[] NO_LISTS = new List[0];
    private static final List[] ONE_EMPTY_LIST = {List.of()};
    private static final List<String> REFERENCE_FILLED_LIST = List.of("test1", "test2", "test3", "test4", "test5", "test6");
    private static final List[] SEVERAL_LISTS_ASC_SIZES = {List.of(), List.of("test1"), List.of("test2", "test3"), List.of("test4", "test5", "test6")};
    private static final List[] SEVERAL_LISTS_DESC_SIZES = {List.of("test1", "test2", "test3"), List.of("test4", "test5"), List.of("test6"), List.of()};
    private static final List<String> REFERENCE_DUPLICATED_ENTRIES_LIST = List.of("test1", "test2", "test1", "test4", "test5", "test2");
    private static final List[] DUPLICATED_ENTRIES_LISTS = {List.of("test1", "test2", "test1"), List.of("test4", "test5"), List.of("test2"), List.of()};

    static Stream<Arguments> emptyListImplementations() {
        return Stream.of(
                Arguments.of(named("MergedImmutableList from 0 Lists", MergedImmutableList.of(NO_LISTS))),
                Arguments.of(named("MergedImmutableList from 1 empty List", MergedImmutableList.of(ONE_EMPTY_LIST))),
                Arguments.of(named("Empty Java List", REFERENCE_EMPTY_LIST))
        );
    }

    static Stream<Arguments> filledListImplementations() {
        return Stream.of(
                Arguments.of(named("MergedImmutableList from 4 Lists of ascending sizes", MergedImmutableList.of(SEVERAL_LISTS_ASC_SIZES))),
                Arguments.of(named("MergedImmutableList from 4 Lists of descending sizes", MergedImmutableList.of(SEVERAL_LISTS_DESC_SIZES))),
                Arguments.of(named("Java List of 6 entries", REFERENCE_FILLED_LIST))
        );
    }

    static Stream<Arguments> duplicatedEntriesListImplementations() {
        return Stream.of(
                Arguments.of(named("MergedImmutableList from 4 Lists with partially duplicated entries", MergedImmutableList.of(DUPLICATED_ENTRIES_LISTS))),
                Arguments.of(named("Java List of 6 partially duplicated entries", REFERENCE_DUPLICATED_ENTRIES_LIST))
        );
    }

    @Nested
    class Size {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void calculatesCorrectSizeForFilledList(List<String> filledList) {
            assertEquals(6, filledList.size());
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void calculatesCorrectSizeForEmptyList(List<String> emptyList) {
            assertEquals(0, emptyList.size());
        }
    }

    @Nested
    class IsEmpty {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void determinesIfEmptyCorrectlyForFilledList(List<String> filledList) {
            assertFalse(filledList.isEmpty());
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void determinesIfEmptyCorrectlyForEmptyList(List<String> emptyList) {
            assertTrue(emptyList.isEmpty());
        }
    }

    @Nested
    @SuppressWarnings("ResultOfMethodCallIgnored")
    class Get {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void getWorksCorrectlyForFilledList(List<String> filledList) {
            assertEquals("test1", filledList.get(0));
            assertEquals("test4", filledList.get(3));
            assertEquals("test6", filledList.get(5));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void getThrowsExceptionWhenIndexExceedsListSizeForFilledList(List<String> filledList) {
            assertThrows(IndexOutOfBoundsException.class, () -> filledList.get(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> filledList.get(6));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void getThrowsExceptionWhenIndexExceedsListSizeForEmptyList(List<String> emptyList) {
            assertThrows(IndexOutOfBoundsException.class, () -> emptyList.get(0));
        }
    }

    @Nested
    class Contains {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void containsWorksCorrectlyForFilledList(List<String> filledList) {
            assertTrue(filledList.contains("test1"));
            assertTrue(filledList.contains("test4"));
            assertTrue(filledList.contains("test6"));
            assertFalse(filledList.contains("test23"));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void containsWorksCorrectlyForEmptyList(List<String> emptyList) {
            assertFalse(emptyList.contains("test1"));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        @SuppressWarnings("ResultOfMethodCallIgnored")
        <T>void containsNullThrowsNullPointerExceptionForEmptyList(List<String> emptyList) {
            assertThrows(NullPointerException.class, () -> emptyList.contains(null));
        }
    }

    @Nested
    @SuppressWarnings("RedundantCollectionOperation")
    class ContainAll {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void containsAllWorksCorrectlyWithListsForFilledList(List<String> filledList) {
            assertTrue(filledList.containsAll(List.of("test1")));
            assertFalse(filledList.containsAll(List.of("test23")));
            assertTrue(filledList.containsAll(List.of("test1", "test2", "test3", "test4", "test5", "test6")));
            assertTrue(filledList.containsAll(List.of("test1", "test1", "test1")));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void containsAllWorksCorrectlyWithSetsForFilledList(List<String> filledList) {
            assertTrue(filledList.containsAll(Set.of("test1")));
            assertFalse(filledList.containsAll(Set.of("test23")));
            assertTrue(filledList.containsAll(Set.of("test1", "test2", "test3", "test4", "test5", "test6")));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void containsAllWorksCorrectlyWithHashSetsForFilledList(List<String> filledList) {
            assertTrue(filledList.containsAll(new HashSet<>(List.of("test1"))));
            assertFalse(filledList.containsAll(new HashSet<>(List.of("test23"))));
            assertTrue(filledList.containsAll(new HashSet<>(Arrays.asList("test1", "test2", "test3", "test4", "test5", "test6"))));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void containsAllWorksCorrectlyWithLinkedHashSetsForFilledList(List<String> filledList) {
            assertTrue(filledList.containsAll(new LinkedHashSet<>(List.of("test1"))));
            assertFalse(filledList.containsAll(new LinkedHashSet<>(List.of("test23"))));
            assertTrue(filledList.containsAll(new LinkedHashSet<>(Arrays.asList("test1", "test2", "test3", "test4", "test5", "test6"))));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void containsAllWorksCorrectlyEmptyList(List<String> emptyList) {
            assertTrue(emptyList.containsAll(List.<String>of()));
            assertFalse(emptyList.containsAll(List.of("test1")));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
        <T>void containsAllNullThrowsNullPointerExceptionForEmptyList(List<String> emptyList) {
            assertThrows(NullPointerException.class, () -> emptyList.containsAll(null));
        }
    }

    @Nested
    class IndexOf {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void indexOfWorksCorrectlyForFilledList(List<String> filledList) {
            assertEquals(0, filledList.indexOf("test1"));
            assertEquals(3, filledList.indexOf("test4"));
            assertEquals(5, filledList.indexOf("test6"));
            assertEquals(-1, filledList.indexOf("test23"));
        }

        @ParameterizedTest
        @MethodSource(DUPLICATED_ENTRIES_LIST_PROVIDER_FQN)
        <T>void indexOfWorksCorrectlyForListsWithDuplicateEntries(List<String> duplicatedEntriesList) {
            assertEquals(0, duplicatedEntriesList.indexOf("test1"));
            assertEquals(1, duplicatedEntriesList.indexOf("test2"));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void indexOfWorksCorrectlyForEmptyList(List<String> emptyList) {
            assertEquals(-1, emptyList.indexOf("test1"));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void indexOfNullThrowsNullPointerExceptionForEmptyList(List<String> emptyList) {
            assertThrows(NullPointerException.class, () -> emptyList.indexOf(null));
        }
    }

    @Nested
    class LastIndexOf {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void lastIndexOfWorksCorrectlyForFilledList(List<String> filledList) {
            assertEquals(0, filledList.lastIndexOf("test1"));
            assertEquals(3, filledList.lastIndexOf("test4"));
            assertEquals(5, filledList.lastIndexOf("test6"));
            assertEquals(-1, filledList.lastIndexOf("test23"));
        }

        @ParameterizedTest
        @MethodSource(DUPLICATED_ENTRIES_LIST_PROVIDER_FQN)
        <T>void lastIndexOfWorksCorrectlyForListsWithDuplicateEntries(List<String> duplicatedEntriesList) {
            assertEquals(2, duplicatedEntriesList.lastIndexOf("test1"));
            assertEquals(5, duplicatedEntriesList.lastIndexOf("test2"));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void lastIndexOfWorksCorrectlyForEmptyList(List<String> emptyList) {
            assertEquals(-1, emptyList.lastIndexOf("test1"));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void lastIndexOfNullThrowsNullPointerExceptionForEmptyList(List<String> emptyList) {
            assertThrows(NullPointerException.class, () -> emptyList.lastIndexOf(null));
        }
    }

    @Nested
    class Sublist {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void subListWorksCorrectlyForPartOfFilledList(List<String> filledList) {
            List<String> subList = filledList.subList(2, 5);
            assertThat(subList).containsExactly("test3", "test4", "test5");
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void subListWorksCorrectlyForAllElementsOfFilledList(List<String> filledList) {
            List<String> subList = filledList.subList(0, 6);
            assertThat(subList).containsExactly("test1", "test2", "test3", "test4", "test5", "test6");
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void subListWorksCorrectlyForSingleElementOfFilledList(List<String> filledList) {
            List<String> subList = filledList.subList(0, 1);
            assertThat(subList).containsExactly("test1");

            List<String> anotherSubList = filledList.subList(5, 6);
            assertThat(anotherSubList).containsExactly("test6");
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void subListThrowsExceptionWhenIndexOutsideListSizeOfFilledList(List<String> filledList) {
            assertThrows(IndexOutOfBoundsException.class, () -> filledList.subList(6, 7));
            assertThrows(IndexOutOfBoundsException.class, () -> filledList.subList(7, 8));
            assertThrows(IndexOutOfBoundsException.class, () -> filledList.subList(-1, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> filledList.subList(-2, -1));
            assertThrows(IndexOutOfBoundsException.class, () -> filledList.subList(-1, 7));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void subListThrowsExceptionWhenFromIndexIsBiggerThanToIndex(List<String> filledList) {
            assertThrows(IllegalArgumentException.class, () -> filledList.subList(4, 3));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void zeroElementSubListWorksCorrectlyForEmptyList(List<String> emptyList) {
            assertEquals(0, emptyList.subList(0, 0).size());
        }
    }

    @Nested
    class ToArray {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void toArrayWorksCorrectlyForFilledList(List<String> filledList) {
            Object[] array = filledList.toArray();
            assertThat(array).containsExactly("test1", "test2", "test3", "test4", "test5", "test6");
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void toArrayWorksCorrectlyForEmptyList(List<String> emptyList) {
            assertEquals(0, emptyList.toArray().length);
        }
    }

    @Nested
    class ToArrayTyped {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void toArrayTypedWorksCorrectlyWithZeroLengthArrayForFilledList(List<String> filledList) {
            String[] zeroLengthArray = new String[0];
            String[] array = filledList.toArray(zeroLengthArray);
            assertNotSame(zeroLengthArray, array);
            assertThat(array).containsExactly("test1", "test2", "test3", "test4", "test5", "test6");
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void toArrayTypedWorksCorrectlyWithZeroLengthArrayForEmptyList(List<String> emptyList) {
            String[] zeroLengthArray = new String[0];
            String[] array = emptyList.toArray(zeroLengthArray);
            assertSame(zeroLengthArray, array);
            assertEquals(0, array.length);
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void toArrayTypedWorksCorrectlyWithOverSizedArrayForFilledList(List<String> filledList) {
            String[] overSizedArray = {"a", "b", "c", "d", "e", "f", "j"};
            String[] array = filledList.toArray(overSizedArray);
            assertSame(overSizedArray, array);
            assertThat(array).containsExactly("test1", "test2", "test3", "test4", "test5", "test6", null);
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void toArrayTypedWorksCorrectlyWithOverSizedArrayForEmptyList(List<String> emptyList) {
            String[] overSizedArray = new String[] {"a", "b"};
            String[] array = emptyList.toArray(overSizedArray);
            assertSame(overSizedArray, array);
            assertThat(array).containsExactly(null, "b");
        }
    }

    @Nested
    class Itr {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void iteratingOverAllElementsOfFilledListWorksCorrectly(List<String> filledList) {
            List<String> iterationResult = new ArrayList<>();
            ListIterator<String> iterator = filledList.listIterator();
            while (iterator.hasNext()) {
                String string = iterator.next();
                iterationResult.add(string);
            }
            assertThrows(NoSuchElementException.class, iterator::next);
            assertThat(iterationResult).containsExactly("test1", "test2", "test3", "test4", "test5", "test6");
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void removeOnIteratorIsUnsupported(List<String> filledList) {
            Iterator<String> iterator = filledList.iterator();
            assertTrue(iterator.hasNext());
            assertThrows(UnsupportedOperationException.class, iterator::remove);
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void forEachRemainingOnIteratorWorksCorrectly(List<String> filledList) {
            Iterator<String> iterator = filledList.iterator();
            for (int i = 0; i < 3; i++) {
                iterator.next();
            }
            List<String> forEachRemainingResult = new ArrayList<>();
            iterator.forEachRemaining(forEachRemainingResult::add);
            assertThat(forEachRemainingResult).containsExactly("test4", "test5", "test6");
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void nextThrowsExceptionOnIteratorOfEmptyList(List<String> emptyList) {
            Iterator<String> iterator = emptyList.iterator();
            assertThrows(NoSuchElementException.class, iterator::next);
        }
    }

    @Nested
    class ListItr {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void iteratingOverAllElementsOfFilledListWorksCorrectly(List<String> filledList) {
            List<String> iterationResult = new ArrayList<>();
            ListIterator<String> iterator = filledList.listIterator();
            while (iterator.hasNext()) {
                String string = iterator.next();
                iterationResult.add(string);
            }
            assertThrows(NoSuchElementException.class, iterator::next);
            assertThat(iterationResult).containsExactly("test1", "test2", "test3", "test4", "test5", "test6");
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        @SuppressWarnings("Convert2MethodRef")
        <T>void mutatingOperationsOfListIteratorAreUnsupported(List<String> filledList) {
            ListIterator<String> iterator = filledList.listIterator();
            assertTrue(iterator.hasNext());
            assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
            assertThrows(UnsupportedOperationException.class, () -> iterator.set("test"));
            assertThrows(UnsupportedOperationException.class, () -> iterator.add("test1.5"));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void forEachRemainingOnListIteratorWorksCorrectly(List<String> filledList) {
            ListIterator<String> iterator = filledList.listIterator();
            for (int i = 0; i < 3; i++) {
                iterator.next();
            }
            List<String> forEachRemainingResult = new ArrayList<>();
            iterator.forEachRemaining(forEachRemainingResult::add);
            assertThat(forEachRemainingResult).containsExactly("test4", "test5", "test6");
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void hasNextAndHasPreviousWorkCorrectlyForListIterator(List<String> filledList) {
            ListIterator<String> iterator = filledList.listIterator();
            assertFalse(iterator.hasPrevious());
            iterator.next();
            assertTrue(iterator.hasPrevious());
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void traversingAllElementsOfListIteratorForwardsAndBackwardsWorksCorrectly(List<String> filledList) {
            ListIterator<String> iterator = filledList.listIterator();
            for (int i = 0; i < 6; i++) {
                iterator.next();
            }
            assertThrows(NoSuchElementException.class, iterator::next);
            List<String> reversedList = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                reversedList.add(iterator.previous());
            }
            assertThrows(NoSuchElementException.class, iterator::previous);
            assertThat(reversedList).containsExactly("test6", "test5", "test4", "test3", "test2", "test1");
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void nextIndexAndPreviousIndexWorkCorrectlyWhenTraversingAllElementsOfListIterator(List<String> filledList) {
            ListIterator<String> iterator = filledList.listIterator();
            for (int i = 0; i < 6; i++) {
                assertEquals(i, iterator.nextIndex());
                assertEquals(i - 1, iterator.previousIndex());
                iterator.next();
            }
            for (int i = 5; i >= 0; i--) {
                assertEquals(i + 1, iterator.nextIndex());
                assertEquals(i, iterator.previousIndex());
                iterator.previous();
            }
            for (int i = 0; i < 6; i++) {
                assertEquals(i, iterator.nextIndex());
                assertEquals(i - 1, iterator.previousIndex());
                iterator.next();
            }
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void previousAndNextThrowExceptionOnListIteratorOfEmptyList(List<String> emptyList) {
            ListIterator<String> iterator7 = emptyList.listIterator();
            assertThrows(NoSuchElementException.class, iterator7::previous);
            assertThrows(NoSuchElementException.class, iterator7::next);
        }
    }

    @Nested
    class ListItrIndex {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void iteratingOverAllElementsFromIndexOfFilledListWorksCorrectly(List<String> filledList) {
            List<String> iterationResult = new ArrayList<>();
            ListIterator<String> iterator = filledList.listIterator(2);
            while (iterator.hasNext()) {
                String string = iterator.next();
                iterationResult.add(string);
            }
            assertThrows(NoSuchElementException.class, iterator::next);
            assertThat(iterationResult).containsExactly("test3", "test4", "test5", "test6");
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        @SuppressWarnings("Convert2MethodRef")
        <T>void mutatingOperationsOfListIteratorFromIndexAreUnsupported(List<String> filledList) {
            ListIterator<String> iterator = filledList.listIterator(5);
            assertTrue(iterator.hasNext());
            assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
            assertThrows(UnsupportedOperationException.class, () -> iterator.set("test"));
            assertThrows(UnsupportedOperationException.class, () -> iterator.add("test1.5"));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void forEachRemainingOnListIteratorFromIndexWorksCorrectly(List<String> filledList) {
            ListIterator<String> iterator = filledList.listIterator(2);
            for (int i = 0; i < 2; i++) {
                iterator.next();
            }
            List<String> forEachRemainingResult = new ArrayList<>();
            iterator.forEachRemaining(forEachRemainingResult::add);
            assertThat(forEachRemainingResult).containsExactly("test5", "test6");
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void hasNextAndHasPreviousWorkCorrectlyForListIteratorFromIndex(List<String> filledList) {
            ListIterator<String> iterator = filledList.listIterator(6);
            assertFalse(iterator.hasNext());
            assertTrue(iterator.hasPrevious());
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void previousWorksCorrectlyForListIteratorFromIndex(List<String> filledList) {
            ListIterator<String> iterator = filledList.listIterator(6);
            assertThrows(NoSuchElementException.class, iterator::next);
            List<String> reversedList = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                reversedList.add(iterator.previous());
            }
            assertThrows(NoSuchElementException.class, iterator::previous);
            assertThat(reversedList).containsExactly("test6", "test5", "test4", "test3", "test2", "test1");
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void nextIndexAndPreviousIndexWorkCorrectlyWhenTraversingAllElementsOfListIteratorFromIndex(List<String> filledList) {
            ListIterator<String> iterator = filledList.listIterator(6);
            for (int i = 5; i >= 0; i--) {
                assertEquals(i + 1, iterator.nextIndex());
                assertEquals(i, iterator.previousIndex());
                iterator.previous();
            }
            for (int i = 0; i < 6; i++) {
                assertEquals(i, iterator.nextIndex());
                assertEquals(i - 1, iterator.previousIndex());
                iterator.next();
            }
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void usingInvalidIndexValuesForListIteratorThrowsException(List<String> filledList) {
            assertThrows(IndexOutOfBoundsException.class, () -> filledList.listIterator(7));
            assertThrows(IndexOutOfBoundsException.class, () -> filledList.listIterator(-1));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void previousAndNextThrowExceptionOnListIteratorFromIndexOfEmptyList(List<String> emptyList) {
            ListIterator<String> iterator = emptyList.listIterator(0);
            assertThrows(NoSuchElementException.class, iterator::previous);
            assertThrows(NoSuchElementException.class, iterator::next);
        }
    }

    @Nested
    class HashCode {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void hashCodesForFilledListsWithEqualContentsAreEqual(List<String> filledList) {
            List<String> expected = List.of("test1", "test2", "test3", "test4", "test5", "test6");
            assertEquals(expected.hashCode(), filledList.hashCode());
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void hashCodesForFilledListsWithNonEqualContentsAreNotEqual(List<String> filledList) {
            //Elements 5 and 6 are flipped
            List<String> expected = List.of("test1", "test2", "test3", "test4", "test6", "test5");
            assertNotEquals(expected.hashCode(), filledList.hashCode());
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void hashCodesForEmptyListsAreEqual(List<String> emptyList) {
            List<String> expected = List.of();
            assertEquals(expected.hashCode(), emptyList.hashCode());
        }
    }

    @Nested
    @SuppressWarnings("SimplifiableAssertion")
    class Equals {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        @SuppressWarnings("EqualsWithItself")
        <T>void filledListEqualsItself(List<String> filledList) {
            assertTrue(filledList.equals(filledList));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        @SuppressWarnings("EqualsBetweenInconvertibleTypes")
        <T>void filledListIsNotEqualToDifferentTypes(List<String> filledList) {
            Set<String> filledSet = Set.copyOf(filledList);
            assertFalse(filledList.equals(filledSet));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void filledListIsNotEqualToListOfDifferentSize(List<String> filledList) {
            List<String> filledSubList = filledList.subList(0, 5);
            assertFalse(filledList.equals(filledSubList));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void filledListsWithEqualContentsAreEqual(List<String> filledList) {
            List<String> expected = List.of("test1", "test2", "test3", "test4", "test5", "test6");
            assertTrue(filledList.equals(expected));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void filledListsWithNonEqualContentsAreNotEqual(List<String> filledList) {
            //Elements 5 and 6 are flipped
            List<String> different = List.of("test1", "test2", "test3", "test4", "test6", "test5");
            assertFalse(filledList.equals(different));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void emptyListsAreEqual(List<String> emptyList) {
            List<String> expected = List.of();
            assertTrue(emptyList.equals(expected));
        }
    }

    @Nested
    class ToString {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void toStringWorksCorrectlyForFilledList(List<String> filledList) {
            String string = filledList.toString();
            assertThat(string).isEqualTo("[test1, test2, test3, test4, test5, test6]");
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void toStringWorksCorrectlyForEmptyList(List<String> emptyList) {
            String string = emptyList.toString();
            assertThat(string).isEqualTo("[]");
        }
    }

    @Nested
    class ForEach {
        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void nullActionThrowsNullPointerException(List<String> emptyList) {
            assertThrows(NullPointerException.class, () -> emptyList.forEach(null));
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void forEachWorksCorrectlyForFilledList(List<String> filledList) {
            StringBuilder stringBuilder = new StringBuilder();
            filledList.forEach(stringBuilder::append);
            assertThat(stringBuilder.toString()).isEqualTo("test1test2test3test4test5test6");
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void forEachWorksCorrectlyForEmptyList(List<String> emptyList) {
            StringBuilder stringBuilder = new StringBuilder();
            emptyList.forEach(stringBuilder::append);
            assertThat(stringBuilder.toString()).isEqualTo("");
        }
    }

    @Nested
    class StreamOperations {
        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void streamMapWorksCorrectlyForFilledList(List<String> filledList) {
            List<String> strings = filledList.stream()
                    .map(s -> s.substring(4))
                    .collect(Collectors.toList());
            assertThat(strings).containsExactly("1", "2", "3", "4", "5", "6");
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void streamMapWorksCorrectlyForEmptyList(List<String> emptyList) {
            List<String> strings = emptyList.stream()
                    .map(s -> s.substring(4))
                    .collect(Collectors.toList());
            assertThat(strings).isEmpty();
        }

        @ParameterizedTest
        @MethodSource(FILLED_LIST_PROVIDER_FQN)
        <T>void streamFilterWorksCorrectlyForFilledList(List<String> filledList) {
            List<String> strings = filledList.stream()
                    .filter(s -> s.endsWith("2") || s.endsWith("4") || s.endsWith("6"))
                    .collect(Collectors.toList());
            assertThat(strings).containsExactly("test2", "test4", "test6");
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void streamFilterWorksCorrectlyForEmptyList(List<String> emptyList) {
            List<String> strings = emptyList.stream()
                    .filter(s -> s.endsWith("2") || s.endsWith("4") || s.endsWith("6"))
                    .collect(Collectors.toList());
            assertThat(strings).isEmpty();
        }
    }

    @Nested
    class Immutability {
        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void addIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.add(""));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void removeIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.remove(""));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void addAllIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.addAll(List.of("")));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void addAllAtIndexIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.addAll(0, List.of("")));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void removeAllIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.removeAll(List.of("")));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void retainAllIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.retainAll(List.of("")));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        @SuppressWarnings("Convert2MethodRef")
        <T>void clearIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.clear());
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void setIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.set(0, ""));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void addAtIndexIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.add(0, ""));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void removeFromIndexIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.remove(0));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void sortIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.sort(String::compareTo));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void replaceAllIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.replaceAll(String::toString));
        }

        @ParameterizedTest
        @MethodSource(EMPTY_LIST_PROVIDER_FQN)
        <T>void removeIfIsUnsupported(List<String> emptyList) {
            assertThrows(UnsupportedOperationException.class, () -> emptyList.removeIf(String::isEmpty));
        }
    }
}
