package bithazard.util.collection;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class MergedImmutableList<E> implements List<E> {
    private static final Set<String> TRULY_IMMUTABLE_LIST_SUPERCLASSES = Set.of(
            "java.util.ImmutableCollections$AbstractImmutableList",
            "com.google.common.collect.ImmutableList",
            "com.google.common.collect.ImmutableAsList",
            "com.google.common.collect.RegularImmutableAsList"
    );
    private volatile Integer overallSize;
    private final List<E>[] lists;

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <E> List<E> of(List<E>... lists) {
        if (lists.length == 0) {
            return List.of();
        }
        List<E>[] defensiveCopy = new List[lists.length];
        System.arraycopy(lists, 0, defensiveCopy, 0, lists.length);
        for (List<E> list : defensiveCopy) {
            if (!TRULY_IMMUTABLE_LIST_SUPERCLASSES.contains(list.getClass().getSuperclass().getName())) {
                throw new IllegalArgumentException("At least one of the passed lists is of a type that is not known to be immutable: "
                        + list.getClass().getName());
            }
        }
        return new MergedImmutableList<>(defensiveCopy);
    }

    @SafeVarargs
    private MergedImmutableList(List<E>... lists) {
        this.lists = lists;
    }

    @Override
    @SuppressWarnings("NestedAssignment")
    public int size() {
        Integer tmp = overallSize;
        if (tmp == null) {
            overallSize = tmp = calculateSize();
        }
        return tmp;
    }

    private int calculateSize() {
        int size = 0;
        for (List<E> list : lists) {
            size += list.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public E get(int index) {
        for (List<E> list : lists) {
            int currentListSize = list.size();
            if (index < currentListSize) {
                return list.get(index);
            }
            index -= currentListSize;
        }
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public boolean contains(Object o) {
        for (List<E> list : lists) {
            if (list.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object element : c) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int indexOf(Object o) {
        int overallIndex = 0;
        for (List<E> list : lists) {
            int indexOfObject = list.indexOf(o);
            if (indexOfObject != -1) {
                return overallIndex + indexOfObject;
            }
            overallIndex += list.size();
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int overallSize = size();
        for (int i = lists.length - 1; i >= 0; i--) {
            List<E> currentList = lists[i];
            overallSize -= currentList.size();
            int lastIndexOfObject = currentList.lastIndexOf(o);
            if (lastIndexOfObject != -1) {
                return overallSize + lastIndexOfObject;
            }
        }
        return -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<E> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex == toIndex) {
            return List.of();
        }

        int overallIndex = 0;
        int listCounter = 0;
        List<E> listWithFromIndex = null;
        for (; listCounter < lists.length; listCounter++) {
            List<E> currentList = lists[listCounter];
            int currentListSize = currentList.size();
            if (overallIndex + currentListSize >= fromIndex) {
                listWithFromIndex = currentList;
                fromIndex -= overallIndex;
                break;
            }
            overallIndex += currentListSize;
        }
        if (listWithFromIndex == null) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        int fromListIndex = listCounter;

        List<E> listWithToIndex = null;
        for (; listCounter < lists.length; listCounter++) {
            List<E> currentList = lists[listCounter];
            int currentListSize = currentList.size();
            if (toIndex <= overallIndex + currentListSize) {
                listWithToIndex = currentList;
                toIndex -= overallIndex;
                break;
            }
            overallIndex += currentListSize;
        }
        if (listWithToIndex == null) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        int toListIndex = listCounter;
        if (listWithFromIndex == listWithToIndex) {
            return listWithFromIndex.subList(fromIndex, toIndex);
        }

        List<E>[] subLists = new List[toListIndex - fromListIndex + 1];
        subLists[0] = listWithFromIndex.subList(fromIndex, listWithFromIndex.size());
        int numberOfListsToCopy = toListIndex - fromListIndex - 1;
        if (numberOfListsToCopy > 0) {
            System.arraycopy(lists, fromListIndex + 1, subLists, 1, numberOfListsToCopy);
        }
        subLists[toListIndex - fromListIndex] = listWithToIndex.subList(0, toIndex);
        return new MergedImmutableList<E>(subLists);
    }

    @Override
    public Object[] toArray() {
        int overallSize = size();
        Object[] copy = new Object[overallSize];
        for (int i = 0, overallIndex = 0; i < lists.length; i++) {
            List<E> currentList = lists[i];
            int currentListSize = currentList.size();
            System.arraycopy(currentList.toArray(), 0, copy, overallIndex, currentListSize);
            overallIndex += currentListSize;
        }
        return copy;
    }

    @Override
    @SuppressWarnings({"unchecked", "SuspiciousToArrayCall"})
    public <T> T[] toArray(T[] a) {
        int overallSize = size();
        Class<?> componentType = a.getClass().getComponentType();
        T[] copy;
        if (a.length < overallSize) {
            copy = (T[]) Array.newInstance(componentType, overallSize);
        } else {
            copy = a;
            if (copy.length > overallSize) {
                copy[overallSize] = null;
            }
        }
        T[] typedEmptyArray = (T[])Array.newInstance(componentType, 0);
        for (int i = 0, overallIndex = 0; i < lists.length; i++) {
            List<E> currentList = lists[i];
            int currentListSize = currentList.size();
            System.arraycopy(currentList.toArray(typedEmptyArray), 0, copy, overallIndex, currentListSize);
            overallIndex += currentListSize;
        }
        return copy;
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        private final int size;
        protected int indexOfList;
        protected int cursorInList;
        protected int overallIndex;

        public Itr() {
            this(0, 0, 0);
        }

        public Itr(int indexOfList, int cursorInList, int overallIndex) {
            this.size = size();
            this.indexOfList = indexOfList;
            this.cursorInList = cursorInList;
            this.overallIndex = overallIndex;
        }

        @Override
        public boolean hasNext() {
            return overallIndex < size;
        }

        @Override
        public E next() {
            if (cursorInList < lists[indexOfList].size()) {
                overallIndex++;
                return lists[indexOfList].get(cursorInList++);
            }
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            do {
                indexOfList++;
            } while (lists[indexOfList].isEmpty());
            cursorInList = 1;
            overallIndex++;
            return lists[indexOfList].get(0);
        }
    }

    @Override
    public ListIterator<E> listIterator() {
        return new ListItr();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + " Size: " + size());
        }
        int listCounter = 0;
        int indexInList = index;
        for (; listCounter < lists.length; listCounter++) {
            int currentListSize = lists[listCounter].size();
            if (indexInList <= currentListSize) {
                return new ListItr(listCounter, indexInList, index);
            }
            indexInList -= currentListSize;
        }
        throw new IndexOutOfBoundsException("Index: " + index + " Size: " + size());
    }

    private class ListItr extends Itr implements ListIterator<E> {
        public ListItr() {
            super(0, 0, 0);
        }

        public ListItr(int indexOfList, int cursorInList, int overallIndex) {
            super(indexOfList, cursorInList, overallIndex);
        }

        @Override
        public boolean hasPrevious() {
            return overallIndex > 0;
        }

        @Override
        public E previous() {
            if (cursorInList > 0) {
                overallIndex--;
                return lists[indexOfList].get(--cursorInList);
            }
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            do {
                indexOfList--;
            } while (lists[indexOfList].isEmpty());
            cursorInList = lists[indexOfList].size() - 1;
            overallIndex--;
            return lists[indexOfList].get(cursorInList);
        }

        @Override
        public int nextIndex() {
            return overallIndex;
        }

        @Override
        public int previousIndex() {
            return overallIndex - 1;
        }

        @Override
        public void add(E e) {throw new UnsupportedOperationException();}
        @Override
        public void remove() {throw new UnsupportedOperationException();}
        @Override
        public void set(E e) {throw new UnsupportedOperationException();}
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (List<E> list : lists) {
            for (E element : list) {
                hashCode = 31 * hashCode + element.hashCode();
            }
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof List)) {
            return false;
        }
        List<?> otherList = (List<?>)obj;
        if (otherList.size() != size()) {
            return false;
        }
        Iterator<?> otherListItr = otherList.iterator();
        for (List<E> list : lists) {
            for (E element : list) {
                if (!element.equals(otherListItr.next())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<E> it = iterator();
        sb.append('[').append(it.next());
        while (it.hasNext()){
            sb.append(',').append(' ').append(it.next());
        }
        return sb.append(']').toString();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        for (List<E> list : lists) {
            for (E element : list) {
                action.accept(element);
            }
        }
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size(), Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE);
    }

    @Override
    public boolean add(E e)                                     {throw new UnsupportedOperationException();}
    @Override
    public void add(int index, E element)                       {throw new UnsupportedOperationException();}
    @Override
    public boolean addAll(Collection<? extends E> c)            {throw new UnsupportedOperationException();}
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {throw new UnsupportedOperationException();}
    @Override
    public E remove(int index)                                  {throw new UnsupportedOperationException();}
    @Override
    public boolean remove(Object o)                             {throw new UnsupportedOperationException();}
    @Override
    public boolean removeAll(Collection<?> c)                   {throw new UnsupportedOperationException();}
    @Override
    public boolean retainAll(Collection<?> c)                   {throw new UnsupportedOperationException();}
    @Override
    public void clear()                                         {throw new UnsupportedOperationException();}
    @Override
    public E set(int index, E element)                          {throw new UnsupportedOperationException();}
    @Override
    public void replaceAll(UnaryOperator<E> operator)           {throw new UnsupportedOperationException();}
    @Override
    public void sort(Comparator<? super E> c)                   {throw new UnsupportedOperationException();}
    @Override
    public boolean removeIf(Predicate<? super E> filter)        {throw new UnsupportedOperationException();}
}
