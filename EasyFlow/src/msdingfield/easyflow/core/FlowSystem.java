package msdingfield.easyflow.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import msdingfield.easyflow.support.DuplicateOutputsFoundException;
import msdingfield.easyflow.support.IdentitySet;
import msdingfield.easyflow.support.InputOutputTypeMismatchException;
import msdingfield.easyflow.support.NoMatchingOutputException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/** A system of operations.
 *
 * A FlowSystem instance contains all of the FlowOperations which will be
 * considered during evaluation.  Operation inputs are matched to operation
 * outputs within the context of a FlowSystem.  The inputs and outputs are
 * matched together by name resulting in a directed graph of operations.
 *
 * A given output name may only be produced by a single operation.  However,
 * many operations may consume a given output name ie. many operations may
 * declare a given name as an input.
 *
 * Every use of a name as input or output declares the expected type.  The
 * input types must be assignable from the output type or evaluation will fail.
 *
 * This class is immutable.
 *
 * @author Matt
 *
 */
public class FlowSystem {

	/** All operations in system. */
	private final Collection<FlowOperation> allOperations = new ArrayList<FlowOperation>();

	/** Map of all outputs to producing operation. */
	private final Map<String, FlowOperation> allOutputs = Maps.newHashMap();

	/** Map of all inputs to consuming operations. */
	private final Map<String, Collection<FlowOperation>> allInputs = Maps.newHashMap();

	/** Map of operation to its predecessor operations. */
	private final IdentityHashMap<FlowOperation, IdentitySet<FlowOperation>> directPredecessors = Maps.newIdentityHashMap();

	/** Map of operation to its successor operations. */
	private final IdentityHashMap<FlowOperation, IdentitySet<FlowOperation>> directSuccessors = Maps.newIdentityHashMap();

	public FlowSystem(final FlowOperation ...operations) {
		for (final FlowOperation op : operations) {
			this.allOperations.add(op);
		}
		init();
	}

	public FlowSystem(final Collection<FlowOperation> operations) {
		this.allOperations.addAll(operations);
		init();
	}

	/** Get all operations in this system. */
	public Collection<FlowOperation> getAllOperations() {
		return Collections.unmodifiableCollection(allOperations);
	}

	/** Get all direct predecessors of an operation. */
	public Collection<FlowOperation> getDirectPredecessors(final FlowOperation operation) {
		assert operation != null && directSuccessors.containsKey(operation);
		return Collections.unmodifiableSet(directPredecessors.get(operation));
	}

	/** Get all transitive predecessors of an operation. */
	public Collection<FlowOperation> getTransitivePredecessors(final FlowOperation operation) {
		assert operation != null && directSuccessors.containsKey(operation);
		final Collection<FlowOperation> predecessors = new IdentitySet<FlowOperation>();

		// We can do this because we know there are no cycles
		final List<FlowOperation> stack = Lists.newArrayList(operation);
		while (!stack.isEmpty()) {
			final FlowOperation current = stack.remove(stack.size()-1);
			predecessors.add(current);
			stack.addAll(getDirectPredecessors(current));
		}

		return predecessors;
	}

	/** Get all operations which are required to produce a given output. */
	public Collection<? extends FlowOperation> getTransitivePredecessors(final String outputName) {
		assert outputName != null;
		if (!allOutputs.containsKey(outputName)) {
			throw new NoMatchingOutputException("Could not find transitive predecssors for '" + outputName + "'.");
		}
		return getTransitivePredecessors(allOutputs.get(outputName));
	}

	/** Get all direct successors of an operation. */
	public Collection<FlowOperation> getDirectSuccessors(final FlowOperation operation) {
		assert operation != null && directSuccessors.containsKey(operation);
		return Collections.unmodifiableSet(directSuccessors.get(operation));
	}

	/** Creates a minimal system with the requested outputs. */
	public FlowSystem getSystemForOutputs(final Set<String> outputNames) {
		final Collection<FlowOperation> operations = new IdentitySet<>();
		for (final String outputName : outputNames) {
			operations.addAll(getTransitivePredecessors(outputName));
		}
		return new FlowSystem(operations);
	}

	private void init() {
		initInputOutputMaps();
		initGraph();
		checkCycles();
		checkTypes();
	}

	private void initInputOutputMaps() {
		for (final FlowOperation op : allOperations) {
			for (final String outputName : op.getOutputs().keySet()) {
				if (allOutputs.containsKey(outputName)) {
					throw new DuplicateOutputsFoundException("Duplicate output.");
				}
				allOutputs.put(outputName, op);
			}

			for (final String inputName : op.getInputs().keySet()) {
				if (!allInputs.containsKey(inputName)) {
					allInputs.put(inputName, Lists.<FlowOperation>newArrayList());
				}
				allInputs.get(inputName).add(op);
			}
		}
	}

	private void initGraph() {
		for (final FlowOperation op : allOperations) {
			directSuccessors.put(op, new IdentitySet<FlowOperation>());
			for (final String outputName : op.getOutputs().keySet()) {
				if (allInputs.containsKey(outputName)) {
					final Collection<FlowOperation> followers = allInputs.get(outputName);
					directSuccessors.get(op).addAll(followers);
				}
			}

			directPredecessors.put(op, new IdentitySet<FlowOperation>());
			for (final String inputName : op.getInputs().keySet()) {
				if (allOutputs.containsKey(inputName)) {
					directPredecessors.get(op).add(allOutputs.get(inputName));
				}
			}
		}
	}

	private void checkCycles() {
		GraphSort.sort(this);
	}

	private void checkTypes() {
		final Map<String,Class<?>> outputTypes = Maps.newHashMap();
		for (final FlowOperation operation : allOperations) {
			outputTypes.putAll(operation.getOutputs());
		}
		
		for (final FlowOperation operation : allOperations) {
			for (final Entry<String, Class<?>> entry : operation.getInputs().entrySet()) {
				final String name = entry.getKey();
				final Class<?> inputType = entry.getValue();
				final Class<?> outputType = outputTypes.get(name);
				if (outputType != null && !isConvertableTo(inputType, outputType)) {
					throw new InputOutputTypeMismatchException(String.format("%s: Cannot convert %s to %s.", name, outputType.getCanonicalName(), inputType.getCanonicalName()));
				}
			}
		}
	}

	private boolean isConvertableTo(final Class<?> inputType, final Class<?> outputType) {
		return inputType.isAssignableFrom(outputType);
	}
}
