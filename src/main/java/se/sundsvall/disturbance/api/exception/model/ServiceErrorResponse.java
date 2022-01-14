package se.sundsvall.disturbance.api.exception.model;

import static java.util.Optional.ofNullable;

import java.util.Objects;

import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.annotation.JsonInclude;

import se.sundsvall.disturbance.api.exception.ServiceException;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceErrorResponse {

	private int httpCode;
	private String message;
	private TechnicalDetails technicalDetails;

	public static ServiceErrorResponse create() {
		return new ServiceErrorResponse();
	}

	public static ServiceErrorResponse createFrom(final ServiceException e) {
		return create()
			.withHttpCode(ofNullable(e.getStatus())
				.map(Status::getStatusCode)
				.orElse(Status.INTERNAL_SERVER_ERROR.getStatusCode()))
			.withMessage(e.getMessage())
			.withTechnicalDetails(e.getTechnicalDetails()
				.withRootCode(e.getTechnicalDetails().getRootCode() == 0 ? e.getStatus().getStatusCode() : e.getTechnicalDetails().getRootCode()));
	}

	public int getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(int httpCode) {
		this.httpCode = httpCode;
	}

	public ServiceErrorResponse withHttpCode(int httpCode) {
		this.httpCode = httpCode;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ServiceErrorResponse withMessage(String message) {
		this.message = message;
		return this;
	}

	public TechnicalDetails getTechnicalDetails() {
		return technicalDetails;
	}

	public void setTechnicalDetails(TechnicalDetails technicalDetails) {
		this.technicalDetails = technicalDetails;
	}

	public ServiceErrorResponse withTechnicalDetails(TechnicalDetails technicalDetails) {
		this.technicalDetails = technicalDetails;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(httpCode, message, technicalDetails);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceErrorResponse other = (ServiceErrorResponse) obj;
		return httpCode == other.httpCode && Objects.equals(message, other.message)
			&& Objects.equals(technicalDetails, other.technicalDetails);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ServiceErrorResponse [httpCode=").append(httpCode).append(", message=").append(message)
			.append(", technicalDetails=").append(technicalDetails).append("]");
		return builder.toString();
	}
}
