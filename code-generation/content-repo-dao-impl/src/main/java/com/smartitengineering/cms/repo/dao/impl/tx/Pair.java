package com.smartitengineering.cms.repo.dao.impl.tx;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A thread-safe {@link Map.Entry} implementation.
 * @author imyousuf
 */
public class Pair<K, V> implements Map.Entry<K, V> {

  private final K key;
  private final AtomicReference<V> value;

  public Pair(K key,
              V value) {
    this.key = key;
    this.value = new AtomicReference<V>(value);
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return this.value.get();
  }

  public V setValue(V value) {
    return this.value.getAndSet(value);
  }
}
