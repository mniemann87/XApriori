/*******************************************************************************
 * Copyright (c) 2013 Matthias Niemann.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * www.gnu.org/licenses/lgpl.txt
 * 
 * Contributors:
 *     Matthias Niemann - initial API and implementation
 ******************************************************************************/
package datatypes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Is a set of items, that occurs frequently.
 * 
 * @author mniemann
 * 
 * @param <T>	the type of items, the pattern stores
 */
public class Pattern<T extends Missable> implements Collection<T>, Iterable<T> {
	/** Set of items, that occurs frequently */
	protected Set<T> items;

	public Pattern() {
		items = new HashSet<T>();
	}

	public Pattern(Set<T> items) {
		this.items = items;
	}

	public Set<T> getItems() {
		return items;
	}

	@Override
	public boolean contains(Object ci) {
		return items.contains(ci);
	}

	@Override
	public Iterator<T> iterator() {
		return items.iterator();
	}

	public boolean add(T i) {
		return items.add(i);
	}

	public int getDimension() {
		return items.size();
	}

	@Override
	public String toString() {
		return "Pattern [items=" + items + "]";
	}

	public boolean containsAll(Pattern<T> p) {
		return items.containsAll(p.getItems());
	}

	public void remove(T item) {
		items.remove(item);

	}

	public Object[] toArray() {
		return items.toArray();
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		final int prime2 = 73;
		int result = 1;
		if (items != null){
			for (T item : items){
				result += prime * item.hashCode() + (item.hashCode()*prime2) % prime;	
			}	
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Pattern) {
			Pattern<T> src = (Pattern<T>) obj;
			if (src.getDimension() != items.size()) {
				return false;
			}
			for (T ci : items) {
				if (!src.contains(ci)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return items.addAll(c);
	}

	@Override
	public void clear() {
		items.clear();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return items.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public boolean remove(Object o) {
		return items.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return items.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return items.retainAll(c);
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return items.toArray(a);
	}

}
