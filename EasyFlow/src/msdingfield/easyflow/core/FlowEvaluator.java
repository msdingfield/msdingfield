package msdingfield.easyflow.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/** Evaluates FlowOperations from a FlowSystem. */
public class FlowEvaluator {
	/** Executor for running operations.
	 * 
	 * Operations are not expected to block so we don't need more threads than we have processors.
	 */
	private static final ExecutorService executor = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors(), 
			new ThreadFactoryBuilder().setDaemon(true).setNameFormat("flow-pool-%d").build());

	/** The system of operations to evaluate. */
	private final FlowSystem system;
	
	/** The context in which to evaluate. */
	private final FlowContext context;
	
	/** A future for callers to block on. */
	private final SettableFuture<Void> evaluationFuture = SettableFuture.create();
	
	/** Runners which wrap each operation. */
	private final Map<FlowOperation, OperationRunner> runners = Maps.newIdentityHashMap();
	
	/** Running count of the number of scheduled & running operations.
	 * 
	 * This is the number of operations for this evaluation which have been
	 * passed to the executor service and which have not completed.  When this
	 * count returns to zero, the evaluation is complete.
	 */
	private final AtomicInteger runningCount = new AtomicInteger(0);
	
	/** Evaluates a system given a context.
	 * 
	 * @param system System of operations to evaluate.
	 * @param context The context in which to evaluate.
	 * @return A future which caller can block on.
	 */
	public static Future<Void> evaluate(final FlowSystem system, final FlowContext context) {
		return new FlowEvaluator(system, context).evaluateInternal();
	}
	
	private FlowEvaluator(final FlowSystem system, final FlowContext context) {
		this.system = system;
		this.context = context;

		// Initialize runners
		for (final FlowOperation op : system.getAllOperations()) {
			runners.put(op, new OperationRunner(op));
		}
	}
	
	private Future<Void> evaluateInternal() {
		/*
		 * All runners are configured to wait for at least 1 release before 
		 * running.  We go through and release each runner so those with no
		 * preceeders will start running.
		 */
		for (final OperationRunner op : runners.values()) {
			op.release();
		}
		
		/*
		 * We need to check the count here in case no operations could run.
		 */
		if (runningCount.get() == 0) {
			evaluationFuture.set(null);
		}
		
		return evaluationFuture;
	}
	
	private static final ThreadLocal<FlowEvaluator> flowEvaluatorLocal = new ThreadLocal<>();
	public static void resumeOn(final ListenableFuture<?> future, final FlowOperation operation) {
		if (flowEvaluatorLocal.get() == null) {
			throw new IllegalStateException("waitFor() must only be called inside runnable context.");
		}
		flowEvaluatorLocal.get().resumeOnInternal(future, operation);
	}

	private void resumeOnInternal(final ListenableFuture<?> future, final FlowOperation operation) {
		final OperationRunner runner = runners.get(operation);
		runner.resumeOn(future);
	}

	private final class OperationRunner implements Runnable {
		
		/** The operation being wrapped. */
		private final FlowOperation operation;
		
		/** Futures we will resume on. */
		private final Collection<ListenableFuture<?>> resumingFutures = new Vector<>();
		
		/** Number of preceeders which haven't finished.
		 * This starts as the number of input operations + 1.  The +1 is for 
		 * an imaginary input operation to all real operations.  This prevents
		 * operations from running too soon.
		 */
		private final AtomicInteger waitCount = new AtomicInteger();
		
		public OperationRunner(final FlowOperation operation) {
			this.operation = operation;

			// See waitCount for +1
			this.waitCount.set(system.getDirectPredecessors(operation).size() + 1);
		}
		
		public void resumeOn(final ListenableFuture<?> future) {
			resumingFutures.add(future);
		}

		/** Indicate completion of a preceeder. */
		public void release() {
			if (waitCount.decrementAndGet() == 0) {
				runningCount.incrementAndGet();
				executor.execute(this);
			}
		}

		@Override
		public void run() {
			// Execute the operation
			try {
				flowEvaluatorLocal.set(FlowEvaluator.this);
				operation.execute(context);
			} finally {
				flowEvaluatorLocal.remove();
			}
			
			if (resumingFutures.isEmpty()) {
			
				// Release followers to run
				for (final FlowOperation follower : system.getDirectSuccessors(operation)) {
					final OperationRunner runner = runners.get(follower);
					runner.release();
				}
				
				// Remove ourself from the running operation count
				if (runningCount.decrementAndGet() == 0) {
					// No more operations are running so signal that evaluation is complete
					// Unless we have unfinished futures
					final List<ListenableFuture<?>> pendingFutures = Lists.newArrayList();
					for (final String outputName : operation.getOutputs().keySet()) {
						final Object attr = context.getAttribute(outputName);
						if (attr instanceof ListenableFuture<?>) {
							final ListenableFuture<?> future = (ListenableFuture<?>) attr;
							if (!future.isDone()) {
								pendingFutures.add(future);
								future.addListener(new Runnable(){

									@Override
									public void run() {
										try {
											context.putAttribute(outputName, future.get());
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}}, executor);
							}
						}
					}
					if (pendingFutures.isEmpty()) {
						evaluationFuture.set(null);
					} else {
						Futures.successfulAsList(pendingFutures).addListener(new Runnable(){

							@Override
							public void run() {
								evaluationFuture.set(null);
							}}, executor);;
					}
				}
			} else {
				// We will be resumed later, so logically we're still running
				for (final ListenableFuture<?> f : resumingFutures) {
					f.addListener(this, executor);
				}
				resumingFutures.clear();
			}
		}
	}
	
}
