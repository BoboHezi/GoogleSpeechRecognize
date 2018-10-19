package eli.google.recognize.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanbo.zhang on 2018/9/14.
 */

public class Stack<T> {

    private List<T> array;

    private int size;

    private int currentNumber = 0;

    public Stack(int size) {
        this.size = size;
        if (size >= 0) {
            array = new ArrayList(size);
        }
        array.toArray();
    }

    public void addElement(T t) {
        if (currentNumber < size) {
            array.add(currentNumber, t);
            currentNumber ++;
        } else {
            popFirst();
            array.set(currentNumber - 1, t);
        }
    }

    public T getFirst() {
        return array.get(0);
    }

    public T getLast() {
        return array.get(currentNumber - 1);
    }

    public T getData(int position) {
        if (position < 0 || position > currentNumber) {
            return null;
        }

        return array.get(position);
    }

    public int currentSize() {
        return currentNumber;
    }

    private void popFirst() {
        if (size > 1 && currentNumber == size && array != null) {
            for (int i = 1; i < array.size() ; i ++) {
                array.set(i - 1, array.get(i));
            }
        }
    }
}
