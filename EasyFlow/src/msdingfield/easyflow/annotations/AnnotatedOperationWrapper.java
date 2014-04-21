package msdingfield.easyflow.annotations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import msdingfield.easyflow.core.FlowContext;
import msdingfield.easyflow.core.FlowEvaluator;
import msdingfield.easyflow.core.FlowOperation;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;

public class AnnotatedOperationWrapper extends FlowOperation {

	private final Class<?> target;
	private final Constructor<?> constructor;
	private final Method operation;
	
	protected AnnotatedOperationWrapper(final Builder builder) {
		super(builder);
		this.target = builder.target;
		this.constructor = builder.constructor;
		this.operation = builder.operation;
	}

	@Override
	public void execute(final FlowContext context) {
		
			try {
				TaskHolder holder = null;
				if (!context.hasAttribute(target)) {
					final Object obj = constructor.newInstance();
					context.putAttribute(target, new TaskHolder(obj));
					for (final String inputName : getInputs().keySet()) {
						setInput(obj, inputName, context);
					}
				}
				holder = (TaskHolder) context.getAttribute(target);
				
				holder.invoke(context);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException
					| NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private void getOutput(Object obj, String outputName, FlowContext context) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Object attribute = target.getField(outputName).get(obj);
		context.putAttribute(outputName, attribute);
	}

	private void setInput(Object obj, String inputName, FlowContext context) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		assert context.hasAttribute(inputName);
		final Object attribute = context.getAttribute(inputName);
		target.getField(inputName).set(obj, attribute);
	}

	public static AnnotatedOperationWrapper of(final Class<?> clazz) {
		return new Builder(clazz).newAnnotatedOperationWrapper();
	}
	
	private class TaskHolder {
		public final Object task;
		
		public TaskHolder(final Object task) {
			this.task = task;
		}
		
		public void invoke(final FlowContext context) {

			try {
				operation.invoke(task);
				
				for (final String outputName : getOutputs().keySet()) {
					getOutput(task, outputName, context);
				}
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchFieldException
					| SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static class Builder extends BuilderBase {
		private Class<?> target = null;
		private Constructor<?> constructor = null;
		private Method operation = null;
		
		public Builder(final Class<?> target) {
			this.target = target;

			Field[] fields = target.getDeclaredFields();
			for (Field field : fields) {
				Input input = field.getAnnotation(Input.class);
				if (input != null) {
					Class<?> type = field.getType();
					String name = field.getName();
					addInput(name, type);
				}
				
				Output output = field.getAnnotation(Output.class);
				if (output != null) {
					Class<?> type = field.getType();
					String name = field.getName();
					addOutput(name, type);
				}
			}
			
			try {
				constructor = target.getConstructor();
			} catch (NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (Method method : target.getDeclaredMethods()) {
				Operation op = method.getAnnotation(Operation.class);
				if (op != null) {
					operation = method;
				}
			}
		}
		
		public AnnotatedOperationWrapper newAnnotatedOperationWrapper() {
			return new AnnotatedOperationWrapper(this);
		}
	}
}
