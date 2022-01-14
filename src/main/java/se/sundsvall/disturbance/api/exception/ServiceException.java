package se.sundsvall.disturbance.api.exception;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import se.sundsvall.disturbance.api.exception.model.TechnicalDetails;

public class ServiceException extends Exception {

	private static final long serialVersionUID = 6852617653500403553L;

	private Status status;
	private TechnicalDetails technicalDetails;

	private ServiceException(final String message, final Throwable cause, final Status status) {
		super(message, cause);

		this.status = status;
		this.technicalDetails = TechnicalDetails.create();
	}

	public static ServiceException create(final String message) {
		return new ServiceException(message, null, null);
	}

	public static ServiceException create(final String message, final Throwable cause) {
		return new ServiceException(message, cause, null);
	}

	public static ServiceException create(final String message, final Status status) {
		return create(message, status, (Status) null);
	}

	public static ServiceException create(final String message, final Status status, final String... details) {
		return create(message, status, null, details);
	}

	public static ServiceException create(final String message, final Throwable cause, final Status status) {
		return new ServiceException(message, cause, status);
	}

	public static ServiceException create(final String message, final Status status, final Status rootStatus, final String... details) {
		return create(message, null, status).withTechnicalDetails(
			TechnicalDetails.create().withRootCode(rootStatus != null ? rootStatus.getStatusCode() : 0)
				.withRootCause(rootStatus != null ? rootStatus.getReasonPhrase() : null)
				.withDetails(details != null ? List.of(details) : List.of()));
	}

	public static ServiceException create(final String message, final String serviceId, final Status status, final Status rootStatus, final String... details) {
		return create(message, null, status).withTechnicalDetails(TechnicalDetails.create().withServiceId(serviceId)
			.withRootCode(rootStatus != null ? rootStatus.getStatusCode() : 0)
			.withRootCause(rootStatus != null ? rootStatus.getReasonPhrase() : null).withDetails(List.of(details)));

	}

	public static ServiceException create(final String message, final Throwable cause, final String serviceId, final Status status, final Status rootStatus,
		final String... details) {
		return create(message, cause, status).withTechnicalDetails(TechnicalDetails.create().withServiceId(serviceId)
			.withRootCode(rootStatus != null ? rootStatus.getStatusCode() : 0)
			.withRootCause(cause != null ? cause.getMessage() : null).withDetails(List.of(details)));
	}

	public Status getStatus() {
		return status;
	}

	public ServiceException withStatus(Status status) {
		this.status = status;
		return this;
	}

	public TechnicalDetails getTechnicalDetails() {
		return technicalDetails;
	}

	public ServiceException withTechnicalDetails(final TechnicalDetails technicalDetails) {
		this.technicalDetails = technicalDetails;
		return this;
	}

	public ServiceRuntimeException asRuntimeException() {
		return new ServiceRuntimeException(this);
	}
}
