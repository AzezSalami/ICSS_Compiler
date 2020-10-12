package nl.han.ica.datastructures;


import java.util.LinkedList;
import java.util.NoSuchElementException;

public class HANStack<T> implements IHANStack<T> {

    private int size;
    private HANLinkedList<T> linkedStack;

    public HANStack() {
        size = 0;
        linkedStack = new HANLinkedList<T>();
    }

    @Override
    public void push(T value) {
        linkedStack.insert(0, value);
        size = linkedStack.getSize();
    }

    @Override
    public T pop() {
        T t;
        if (linkedStack.getSize() > 0) {
            t = linkedStack.get(0);
            linkedStack.delete(0);
            size = linkedStack.getSize();
            return t;
        }
        else
            throw new IndexOutOfBoundsException();
    }

    @Override
    public T peek() {
        if (linkedStack.getFirst() == null)
        {
            throw new NoSuchElementException();
        }
        else
        {
            return linkedStack.getFirst();
        }
    }

    public int getSize() {
        return size;
    }
}
