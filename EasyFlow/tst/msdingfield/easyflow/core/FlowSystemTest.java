package msdingfield.easyflow.core;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import msdingfield.easyflow.core.FlowContext;
import msdingfield.easyflow.core.FlowEvaluator;
import msdingfield.easyflow.core.FlowOperation;
import msdingfield.easyflow.core.FlowSystem;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class FlowSystemTest {
	
	final FlowOperation a = AddOperation.builder()
		.setName("a")
		.setConstant(2)
		.addInput("input", Integer.class)
		.addOutput("a", Integer.class)
		.newOperation();
	final FlowOperation b1 = AddOperation.builder()
		.setName("b.1")
		.setConstant(4)
		.addInput("a", Integer.class)
		.addOutput("b.1", Integer.class)
		.newOperation();
	final FlowOperation b2 = AddOperation.builder()
		.setName("b.2")
		.setConstant(8)
		.addInput("a", Integer.class)
		.addOutput("b.2", Integer.class)
		.newOperation();
	final FlowOperation c = AddOperation.builder()
		.setName("c")
		.setConstant(16)
		.addInput("b.1", Integer.class)
		.addInput("b.2", Integer.class)
		.addOutput("c", Integer.class)
		.newOperation();
	
	@Before
	public void setup() {
		
	}
	
	@Test
	public void testSubGraph() throws InterruptedException, ExecutionException {
		final FlowSystem original = new FlowSystem(a, b1, b2, c);
		final FlowSystem partial = original.getSystemForOutputs(Sets.newHashSet("b.1", "a"));

		FlowContext context = new FlowContext();
		context.putAttribute("input", (Integer)1);
		
		FlowEvaluator.evaluate(partial, context).get();
		
		assertEquals((Integer)3, context.getAttribute("a"));
		assertEquals((Integer)7, context.getAttribute("b.1"));
		assertFalse(context.hasAttribute("b.2"));
		assertFalse(context.hasAttribute("c"));
	}
	
	@Test
	public void testDiamondWithResume() throws InterruptedException, ExecutionException {
		
		System.out.println("Beginning.");
		final FlowSystem sys = new FlowSystem(a, b1, b2, c);
		
		FlowContext context = new FlowContext();
		context.putAttribute("input", (Integer)1);
		
		final Future<?> future = FlowEvaluator.evaluate(sys, context);
		future.get();
		assertEquals((Integer)3, context.getAttribute("a"));
		assertEquals((Integer)7, context.getAttribute("b.1"));
		assertEquals((Integer)11, context.getAttribute("b.2"));
		assertEquals((Integer)34, context.getAttribute("c"));
		final long pause = (long) context.getAttribute("b.2.pausems");
		assertTrue( 4900L <= pause && pause <= 5100 );
		
		for (Object k : context.getAttributeKeys()) {
			java.lang.System.out.printf("%s = %s\n", k, context.getAttribute(k).toString());
		}
		System.out.println("Ending");
		
	}

	private static final class AddOperation extends FlowOperation {
		private final String name;
		private final int constant;
		private boolean hasResumed = false;
		
		public AddOperation(final Builder builder) {
			super(builder);
			this.name = builder.name;
			this.constant = builder.constant;
		}

		@Override
		public void execute(final FlowContext context) {
			
			if (name.equals("b.2") && !hasResumed) {
				System.out.printf("%s pausing\n", name);
				hasResumed = true;
				final ListenableFuture<Long> t = getTimer(5000);
				context.putAttribute("mytimer", t);
				FlowEvaluator.resumeOn(t, this);
				return;
			} else if (name.equals("b.2") && hasResumed) {
				final ListenableFuture<Long> t = (ListenableFuture<Long>) context.getAttribute("mytimer");
				try {
					context.putAttribute("b.2.pausems", t.get());
					System.out.printf("%s waited %d\n", name, t.get()); 
				} catch(Exception e) {}
			}
			
			try {
				System.out.printf("start %s\n", name);
				int acc = constant;
				for (final Map.Entry<String,Class<?>> input : getInputs().entrySet()) {
					if (!context.hasAttribute(input.getKey())) {
						throw new RuntimeException("Missing input");
					}
					final Object ob = context.getAttribute(input.getKey());
					if (!input.getValue().isInstance(ob)) {
						throw new RuntimeException("Bad types");
					}
					
					if (ob instanceof Integer) {
						acc += (Integer)ob;
					}
				}
				
				for (final Map.Entry<String, Class<?>> output : getOutputs().entrySet()) {
					if (Integer.class == output.getValue()) {
						context.putAttribute(output.getKey(), (Integer)acc);
					}
				}
				try { Thread.sleep(100L); } catch (Exception e) {}
			} finally {
				System.out.printf("end %s\n", name);
			}
		}
		
		public static Builder builder() {
			return new Builder();
		}
		
		public static class Builder extends FlowOperation.BuilderBase {
			private String name = "";
			private int constant = 0;
			
			public Builder setName(final String name) {
				this.name = name;
				return this;
			}
			
			public Builder setConstant(final int constant) {
				this.constant = constant;
				return this;
			}
			
			public Builder addInput(final String name, final Class<?> type) {
				super.addInput(name, type);
				return this;
			}
			
			public Builder addOutput(final String name, final Class<?> type) {
				super.addOutput(name, type);
				return this;
			}
			
			public AddOperation newOperation() {
				return new AddOperation(this);
			}
		}
	}
	
	private static Timer timer = new Timer(true);
	private static ListenableFuture<Long> getTimer(final long ms) {
		final SettableFuture<Long> f = SettableFuture.create();
		final long start = System.currentTimeMillis();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				f.set(System.currentTimeMillis() - start);
			}}, ms);
		return f;
	}
}
