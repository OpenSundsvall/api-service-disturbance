package se.sundsvall.disturbance.api.exception;

/**
 * RuntimeException wrapper over ServiceException.
 */
public class ServiceRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -6395176060554455917L;

	public ServiceRuntimeException(ServiceException exception) {
		super(exception);
	}

	@Override
	public String getMessage() {
		return super.getCause().getMessage();
	}

	public ServiceException getTypedCause() {
		return (ServiceException) super.getCause();
	}
}
