package undertow;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class MyLinkedHashMap<K,V> extends LinkedHashMap<K,V>{
    private static final int CACHE_SIZE = 1500000;

    /**
     * @param initialCapacity
     * @param loadFactor
     * @param accessOrder
     */
    public MyLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    /* (non-Javadoc)
     * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
     */
    @Override
    protected boolean removeEldestEntry(Entry eldest) {
        return this.size()>CACHE_SIZE;
    }
    
}
