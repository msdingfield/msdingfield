package msdingfield.easyflow.support;

/** Exception thrown when a required output could not be found. */
public class NoMatchingOutputException extends RuntimeException {

	private static final long serialVersionUID = 5821094888654021383L;

	public NoMatchingOutputException() {
		/* empty */
	}

	public NoMatchingOutputException(final String msg) {
		super(msg);

	}

	public NoMatchingOutputException(final Throwable cause) {
		super(cause);

	}

	public NoMatchingOutputException(final String msg, final Throwable cause) {
		super(msg, cause);
	}

}
