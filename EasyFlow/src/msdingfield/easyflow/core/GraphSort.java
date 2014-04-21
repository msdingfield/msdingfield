package msdingfield.easyflow.core;

import java.util.List;
import java.util.Map;

import msdingfield.easyflow.support.FlowCyclicDependencyException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public final class GraphSort {
	
	public static List<FlowOperation> sort(final FlowSystem system) {
		return new GraphSort(system).sortInternal();
	}
	
	private final List<FlowOperation> schedule = Lists.newArrayList();
	private final Map<FlowOperation,Void> scheduling = Maps.newIdentityHashMap();
	private final Map<FlowOperation,Void> scheduled = Maps.newIdentityHashMap();
	
	private final FlowSystem sys;
	
	private GraphSort(final FlowSystem sys) {
		this.sys = sys;
	}
	
	private List<FlowOperation> sortInternal() {
		for (final FlowOperation op : sys.getAllOperations()) {
			scheduleInternal(op);
		}
		return schedule;
	}

	private void scheduleInternal(final FlowOperation op) {
		if (scheduled.containsKey(op)) {
			return;
		}
		
		if (scheduling.containsKey(op)) {
			throw new FlowCyclicDependencyException();
		}
		
		try {
			scheduling.put(op, null);
			
			for (final FlowOperation pred : sys.getDirectPredecessors(op)) {
				scheduleInternal(pred);
			}
			
			schedule.add(op);
			scheduled.put(op, null);
		} finally {
			scheduling.remove(op);
		}
	}
}