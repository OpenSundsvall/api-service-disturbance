package se.sundsvall.disturbance.api.model;

import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import se.sundsvall.disturbance.api.validation.ValidUuid;

@Schema(description = "Affected persons and/or organizations model")
public class Affected {

	@Schema(description = "PartyId (e.g. a personId or an organizationId)", required = true, example = "81471222-5798-11e9-ae24-57fa13b361e1")
	@ValidUuid
	private String partyId;

	@Schema(description = "Reference information", example = "Streetname 123")
	@NotNull
	@Size(max = 512)
	private String reference;

	public static Affected create() {
		return new Affected();
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public Affected withPartyId(String partyId) {
		this.partyId = partyId;
		return this;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public Affected withReference(String reference) {
		this.reference = reference;
		return this;
	}

	@Override
	public int hashCode() { return Objects.hash(partyId, reference); }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Affected other = (Affected) obj;
		return Objects.equals(partyId, other.partyId) && Objects.equals(reference, other.reference);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Affected [partyId=").append(partyId).append(", reference=").append(reference).append("]");
		return builder.toString();
	}
}
