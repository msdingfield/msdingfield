package msdingfield.easyflow.support;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

public class IdentitySet<T> implements Set<T> {
	private final IdentityHashMap<T,T> map = new IdentityHashMap<T,T>();
	
	@Override
	public boolean add(T arg0) {
		map.put(arg0,arg0);
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		for (T t : arg0) {
			add(t);
		}
		return true;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean contains(Object arg0) {
		return map.containsKey(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		return map.keySet().containsAll(arg0);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return map.keySet().iterator();
	}

	@Override
	public boolean remove(Object arg0) {
		map.remove(arg0);
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		for (Object k : arg0) {
			remove(k);
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Object[] toArray() {
		return map.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		return map.keySet().toArray(arg0);
	}
	
}