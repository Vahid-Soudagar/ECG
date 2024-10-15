package com.vcreate.ecg.bluetooth.data;

public class PersistentCircularBuffer {
    private byte[] buffer;
    private int head;
    private int tail;
    private int capacity;

    public PersistentCircularBuffer(int size) {
        capacity = size;
        buffer = new byte[capacity];
        head = 0;
        tail = 0;
    }

    public synchronized void write(byte[] data, int length) {
        for (int i = 0; i < length; i++) {
            buffer[head] = data[i];
            head = (head + 1) % capacity;
            if (head == tail) {
                tail = (tail + 1) % capacity;
            }
        }
    }

    public synchronized int read(byte[] data, int length) {
        int bytesRead = 0;
        while (bytesRead < length && tail != head) {
            data[bytesRead++] = buffer[tail];
            tail = (tail + 1) % capacity;
        }
        return bytesRead;
    }
}
