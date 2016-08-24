package undertow;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class MyLinkedHashMap<K,V> extends LinkedHashMap<K,V>{
    private static final int CACHE_SIZE = 100000;

    /**
     * @param initialCapacity
     * @param loadFactor
     * @param accessOrder
     */
    public MyLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
     */
    @Override
    protected boolean removeEldestEntry(Entry eldest) {
        return this.size()>CACHE_SIZE;
    }
    
}
