package nl.han.ica.datastructures;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    private Node<T> head, tail;
    private int size = 0;

    @Override
    public void clear() {
        size = 0;
        head = tail = null;
    }

    @Override
    public void insert(int index, T value) {
        if (index == 0) {
            addFirst(value);
        }
        else if (index >= size) {
            addLast(value);
        }
        else {
            Node<T> current = head;
            for (int i = 1; i < index; i++) {
                current = current.next;
            }
            Node<T> temp = current.next;
            current.next = new Node<>(value);
            (current.next).next = temp;
            size++;
        }
    }

    @Override
    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value);
        newNode.next = head;
        head = newNode;
        size++;

        if (tail == null)
            tail = head;
    }

    private void addLast(T value) {
        Node<T> newNode = new Node<>(value); // Create a new for element e

        if (tail == null) {
            head = tail = newNode; // The new node is the only node in list
        }
        else {
            tail.next = newNode; // Link the new with the last node
            tail = newNode; // tail now points to the last node
        }

        size++; // Increase size
    }

    @Override
    public void delete(int pos) {
        if (pos >= 0 && pos < size) {
            if (pos == 0) {
                removeFirst();
            } else if (pos == size - 1) {
                removeLast();
            } else {
                Node<T> previous = head;

                for (int i = 1; i < pos; i++) {
                    previous = previous.next;
                }

                Node<T> current = previous.next;
                previous.next = current.next;
                size--;
            }
        }
    }

    @Override
    public T get(int pos) {
        if (pos < 0 || pos >= size) {
            throw new IllegalArgumentException("index was out of bounds");
        }
        Node<T> current = head;
        for (int i = 0; i < pos; i++) {
            current = current.next;
        }
        return current.element;
    }

    @Override
    public void removeFirst() {
        if (size != 0) {
            T temp = head.element;
            head = head.next;
            size--;
            if (head == null) {
                tail = null;
            }
        }
    }

    public void removeLast() {
        if (size != 0) {

            if (size == 1) {
                T temp = head.element;
                head = tail = null;
                size = 0;
            } else {
                Node<T> current = head;

                for (int i = 0; i < size - 2; i++) {
                    current = current.next;
                }

                T temp = tail.element;
                tail = current;
                tail.next = null;
                size--;
            }
        }
    }

    @Override
    public T getFirst() {
        if (size == 0) {
            return null;
        } else {
            return head.element;
        }
    }

    @Override
    public int getSize() {
        return size;
    }
}
