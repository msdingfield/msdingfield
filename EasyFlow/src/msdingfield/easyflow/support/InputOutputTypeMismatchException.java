package msdingfield.easyflow.support;

/** Exception throw when one or more inputs cannot receive the type of the
 * corresponding output.
 * 
 * @author Matt
 *
 */
public class InputOutputTypeMismatchException extends RuntimeException {

	private static final long serialVersionUID = -1458508441925527581L;

	public InputOutputTypeMismatchException() {
		/* empty */
	}

	public InputOutputTypeMismatchException(final String message) {
		super(message);
	}

	public InputOutputTypeMismatchException(final Throwable cause) {
		super(cause);
	}

	public InputOutputTypeMismatchException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
