package se.sundsvall.disturbance.api.exception.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechnicalDetails implements Serializable {

	private static final long serialVersionUID = 7323068963781021771L;

	private int rootCode;
	private String rootCause;
	private String serviceId;
	private List<String> details;

	public static TechnicalDetails create() {
		return new TechnicalDetails();
	}

	public int getRootCode() {
		return rootCode;
	}

	public void setRootCode(int rootCode) {
		this.rootCode = rootCode;
	}

	public TechnicalDetails withRootCode(int rootCode) {
		this.rootCode = rootCode;
		return this;
	}

	public String getRootCause() {
		return rootCause;
	}

	public void setRootCause(String rootCause) {
		this.rootCause = rootCause;
	}

	public TechnicalDetails withRootCause(String rootCause) {
		this.rootCause = rootCause;
		return this;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public TechnicalDetails withServiceId(String serviceId) {
		this.serviceId = serviceId;
		return this;
	}

	public List<String> getDetails() {
		return details;
	}

	public void setDetails(List<String> details) {
		this.details = details;
	}

	public TechnicalDetails withDetails(List<String> details) {
		this.details = details;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(details, rootCause, rootCode, serviceId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TechnicalDetails other = (TechnicalDetails) obj;
		return Objects.equals(details, other.details) && Objects.equals(rootCause, other.rootCause)
			&& rootCode == other.rootCode && Objects.equals(serviceId, other.serviceId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TechnicalDetails [rootCode=").append(rootCode).append(", rootCause=").append(rootCause)
			.append(", serviceId=").append(serviceId).append(", details=").append(details).append("]");
		return builder.toString();
	}
}
