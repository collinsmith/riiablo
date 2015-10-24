package com.google.collinsmith70.util;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A <a href="https://en.wikipedia.org/wiki/Ternary_search_tree">ternary search trie</a>
 * is a {@link Trie} with a maximum of three child {@link Node}s at each Node.
 *
 * @param <V> type of the value to be stored at each node
 *
 * @author Collin Smith <strong>collinsmith70@gmail.com</strong>
 */
public class TernaryTrie<V> implements Trie<V> {
	/**
	 * This field represents the number of elements (key-value pairs) within
	 * this {@link TernaryTrie}.
	 */
	private int size;

	/**
	 * This field keeps a reference to the root {@link Node} of this
	 * {@link TernaryTrie}.
	 */
	private Node<V> root;

	/**
	 * Default constructor which constructs an empty {@link TernaryTrie}.
	 */
	public TernaryTrie() {
		clear();
	}

	/**
	 * Constructs a {@link TernaryTrie} and adds all elements from the
	 * specified map into that TernaryTrie.
	 *
	 * @param m {@link Map} of key-value of pairs which should be added into
	 *	the constructed {@link TernaryTrie}
	 */
	public TernaryTrie(Map<? extends String, ? extends V> m) {
		this();
		putAll(m);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return size;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V put(String key, V val) {
		key = Objects.requireNonNull(key);
		if (!containsKey(key)) {
			size++;
		}

		PutReturn<V> retValue = put(root, key, val, 0, new PutReturn<V>());
		root = retValue.node;
		return retValue.oldValue;
	}

	/**
	 * Puts a given value at a specified key by traversing this
	 * {@link TernaryTrie} until the specified location is found (or created).
	 *
	 * @param node root {@link Node} to search from
	 * @param key key to add (or change) within this {@link TernaryTrie}
	 * @param value value to add at that key position
	 * @param pos offset within the key that this call is examining
	 * @param retValue initial call will pass this reference forward and use
	 *	its members for return values
	 *
	 * @return a {@link PutReturn} object containing a reference to the new
	 *	root {@link Node} and previous value at the location modified, or
	 *	{@code null} if previously empty
	 */
	private PutReturn<V> put(Node<V> node, String key, V value, int pos, PutReturn<V> retValue) {
		assert key != null;
		assert retValue != null;

		char c = key.charAt(pos);
		if (node == null) {
			node = new Node<V>();
			node.c = c;
		}

		if (c < node.c) {
			node.left = put(node.left, key, value, pos, retValue).node;
		} else if (c > node.c) {
			node.right = put(node.right, key, value, pos, retValue).node;
		} else if (pos < key.length() - 1) {
			node.middle = put(node.middle, key, value, pos + 1, retValue).node;
		} else {
			retValue.oldValue = node.value;
			node.value = value;
		}

		retValue.node = node;
		return retValue;
	}

	/**
	 * A {@link PutReturn} represents the type of object that must be returned
	 * by a {@link #put(Node, String, Object, int, PutReturn)}
	 * call. Each member within this class stores valuable data about the
	 * put call.
	 *
	 * @param <V> type of the value object of this {@link TernaryTrie}
	 */
	private static class PutReturn<V> {
		/**
		 * This field represents the new root {@link Node} of the
		 * {@link TernaryTrie} after the {@link #put(Node, String, Object, int, PutReturn)}
		 * operation.
		 */
		private Node<V> node;

		/**
		 * This field represents the previously stored value at the key
		 * specified in the {@link #put(Node, String, Object, int, PutReturn)}
		 * call, or {@code null} if there was none.
		 */
		private V oldValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsKey(Object key) {
		return get(key.toString()) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V get(Object key) {
		String keyString = Objects.requireNonNull(key).toString();
		if (keyString.isEmpty()) {
			throw new IllegalArgumentException("key must have length >= 1");
		}

		Node<V> node = get(root, keyString, 0);
		if (node == null) {
			return null;
		}

		return node.value;
	}

	/**
	 * Retrieves the {@link Node} with a given key.
	 *
	 * @param node root {@link Node} to search from
	 * @param key key of the object to search for
	 * @param pos offset position within the key which is being examined
	 *
	 * @return a {@link Node} with the specified key, or {@code null} if
	 *	none can be found
	 */
	private Node<V> get(Node<V> node, String key, int pos) {
		assert key != null && !key.isEmpty();
		if (node == null) {
			return null;
		}

		char c = key.charAt(pos);
		if (c < node.c) {
			return get(node.left, key, pos);
		} else if (c > node.c) {
			return get(node.right, key, pos);
		} else if (pos < key.length() - 1) {
			return get(node.middle, key, pos + 1);
		} else {
			return node;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsValue(Object value) {
		for (V v : values()) {
			if (v.equals(value)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public V remove(Object key) {
		String keyString = Objects.requireNonNull(key).toString();
		V oldValue = put(keyString, null);
		return oldValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putAll(Map<? extends String, ? extends V> m) {
		for (Entry<? extends String, ? extends V> entry : m.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		root = null;
		size = 0;
	}

	/**
	 * Collects all keys within this {@link TernaryTrie} rooted at a specified
	 * {@link Node} and starting with the specified prefix value and returns
	 * them all as a {@link Set}.
	 *
	 * @param root root {@link Node} to search from
	 * @param prefix prefix value to search keys for
	 * @param collection reference to {@link Set} of which to place collected
	 *	values into
	 *
	 * @return a reference to a {@link Set} containing all collected values
	 *	with the specified prefix
	 */
	private Set<String> collect(Node<V> root, String prefix, Set<String> collection) {
		if (root == null) {
			return collection;
		}

		collect(root.left, prefix, collection);
		if (root.value != null) {
			collection.add(prefix + root.c);
		}

		collect(root.middle, prefix + root.c, collection);
		collect(root.right, prefix, collection);
		return collection;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterable<String> prefixMatch(String prefix) {
        if (prefix.isEmpty()) {
            return keySet();
        }

		Set<String> keySet = new HashSet<String>();
		Node<V> node = get(root, prefix, 0);
		if (node == null) {
			return keySet;
		}

		if (node.value != null) {
			keySet.add(prefix);
		}

		return collect(node.middle, prefix, keySet);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> keySet() {
		return collect(root, "", new HashSet<String>());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<V> values() {
		Set<V> c = new HashSet<V>();
		for (String s : keySet()) {
			c.add(get(s));
		}

		return c;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Entry<String, V>> entrySet() {
		Set<Entry<String, V>> entrySet = new HashSet<Entry<String, V>>();
		for (String s : keySet()) {
			entrySet.add(new AbstractMap.SimpleEntry<String, V>(s, get(s)));
		}

		return entrySet;
	}

	/**
	 * This class represents a {@link Node} of a {@link TernaryTrie}, which
	 * is represented by a character, a possibly non-null value, and three
	 * possible child nodes, each representing further {@link Node}s.
	 */
	private static class Node<V> {
		/**
		 * This field represents the character identifier of this
		 * {@link Node}.
		 */
		private char c;

		/**
		 * This field represents the value stored at this {@link Node} if
		 * it is a terminal node, or {@code null} otherwise.
		 */
		private V value;

		/**
		 * The left child is defined as having a {@link #c} value
		 * <strong>less than</strong> the {@link #c} value of this
		 * {@link Node}.
		 */
		private Node<V> left;

		/**
		 * The middle child is defined as having a {@link #c} value
		 * <strong>equal to</strong> the {@link #c} value of this
		 * {@link Node}.
		 */
		private Node<V> middle;

		/**
		 * The right child is defined as having a {@link #c} value
		 * <strong>greater than</strong> the {@link #c} value at this
		 * {@link Node}.
		 */
		private Node<V> right;
	}
}
