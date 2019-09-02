package com.example.gambling_blocker;

import java.util.LinkedHashMap;

public class LRUCache<K, V> extends LinkedHashMap<K, V>
{
    /*
    The LRU cache is used to remove
    the least recently used resource
    when the cache is full
     */
    private int Capacity;
    private CleanupCallback callback;

    public LRUCache(int capacity, CleanupCallback callback)
    {
        super(capacity + 1, 1, true);
        this.Capacity = capacity;
        this.callback = callback;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest)
    {
        if (size() > Capacity)
        {
            callback.cleanup(eldest); // remove the eldest element in the linked hash map
            return true;
        }
        return false;
    }

    public static interface CleanupCallback<K, V>
    {
        public void cleanup(Entry<K, V> eldest);
    }
}

