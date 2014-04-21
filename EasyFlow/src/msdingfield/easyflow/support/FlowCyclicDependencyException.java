package msdingfield.easyflow.support;

/** Exception thrown if a cyclic dependency is detected. */
public class FlowCyclicDependencyException extends RuntimeException {

	private static final long serialVersionUID = -7458331831085619578L;

	public FlowCyclicDependencyException() {
		/* empty */
	}

	public FlowCyclicDependencyException(String msg) {
		super(msg);
	}

	public FlowCyclicDependencyException(Throwable cause) {
		super(cause);
	}

	public FlowCyclicDependencyException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
