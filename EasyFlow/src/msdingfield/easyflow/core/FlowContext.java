package msdingfield.easyflow.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Contains state for the evaluation of a Tasks. */
public class FlowContext {
	private Map<Object, Object> attributes = new HashMap<Object, Object>();
	
	public FlowContext setInput(final Map<Object, Object> input) {
		for (Map.Entry<Object, Object> e : input.entrySet()) {
			putAttribute(e.getKey(), e.getValue());
		}
		return this;
	}
	
	public void putAttribute(final Object key, final Object value) {
		attributes.put(key, value);
	}
	
	public Object getAttribute(final Object key) {
		return attributes.get(key);
	}
	
	public boolean hasAttribute(final Object key) {
		return attributes.containsKey(key);
	}
	
	public Collection<Object> getAttributeKeys() {
		return attributes.keySet();
	}
	
	public String toString() {
		return attributes.toString();
	}
}
