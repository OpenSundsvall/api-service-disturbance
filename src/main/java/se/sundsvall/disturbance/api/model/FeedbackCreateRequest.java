package se.sundsvall.disturbance.api.model;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import se.sundsvall.disturbance.api.validation.ValidUuid;

@Schema(description = "Feedback request model")
public class FeedbackCreateRequest {

	@ValidUuid
	@NotNull
	@Schema(description = "PartyId (e.g. a personId or an organizationId)", required = true, example = "81471222-5798-11e9-ae24-57fa13b361e1")
	private String partyId;

	public static FeedbackCreateRequest create() {
		return new FeedbackCreateRequest();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public FeedbackCreateRequest withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(partyId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FeedbackCreateRequest other = (FeedbackCreateRequest) obj;
		return Objects.equals(partyId, other.partyId);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ContinuousFeedbackCreateRequest [partyId=")
			.append(partyId).append("]");
		return builder.toString();
	}
}
