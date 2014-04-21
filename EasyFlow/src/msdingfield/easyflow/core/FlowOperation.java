package msdingfield.easyflow.core;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

/** An operation to be performed.
 * 
 * Operations have a set of inputs and a set of outputs.  All of the inputs 
 * must be available before the operation can execute.  Upon completion, all
 * outputs must have a value.
 * 
 * The inputs must be of a compatible type or evaluation will fail.
 * 
 * Not that this class is immutable.
 * 
 * @author Matt
 *
 */
public abstract class FlowOperation {
	
	/** Inputs to the operation.
	 * 
	 * Map from input name to input type.
	 */
	private final Map<String, Class<?>> inputs = Maps.newHashMap();
	
	/** Outputs from the operation.
	 * 
	 * Map from output name to output type.
	 */
	private final Map<String, Class<?>> outputs = Maps.newHashMap();

	protected FlowOperation(final Map<String, Class<?>> inputs, final Map<String, Class<?>> outputs) {
		this.inputs.putAll(inputs);
		this.outputs.putAll(outputs);
	}

	protected FlowOperation(final BuilderBase builder) {
		this(builder.inputs, builder.outputs);
	}
	
	public Map<String, Class<?>> getInputs() {
		return Collections.unmodifiableMap(inputs);
	}

	public Map<String, Class<?>> getOutputs() {
		return Collections.unmodifiableMap(outputs);
	}
	
	/** Base class for creating builders. */
	protected static class BuilderBase {
		private final Map<String, Class<?>> inputs = Maps.newHashMap();
		private final Map<String, Class<?>> outputs = Maps.newHashMap();
		
		public BuilderBase addInput(final String name, final Class<?> type) {
			inputs.put(name, type);
			return this;
		}
		
		public BuilderBase addOutput(final String name, final Class<?> type) {
			outputs.put(name, type);
			return this;
		}
	}

	/** Derived classes implement this to populate outputs.
	 * 
	 * This method should never be called until all required inputs have been
	 * set into the context.
	 * 
	 * @param context The context with inputs & outputs.
	 */
	abstract public void execute(final FlowContext context);
}
