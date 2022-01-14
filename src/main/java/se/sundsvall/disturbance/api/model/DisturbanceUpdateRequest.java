package se.sundsvall.disturbance.api.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import io.smallrye.common.constraint.Nullable;

@Schema(description = "Disturbance update request model")
public class DisturbanceUpdateRequest {

	@Schema(description = "Title", required = true, example = "Disturbance")
	@Size(max = 255)
	private String title;

	@Schema(description = "Description", example = "Major disturbance")
	@Size(max = 8192)
	private String description;

	@Schema(description = "Disturbance status")
	@Nullable
	private Status status;

	@Schema(description = "Planned start date for the disturbance")
	private OffsetDateTime plannedStartDate;

	@Schema(description = "Planned stop date for the disturbance")
	private OffsetDateTime plannedStopDate;

	@Schema(type = SchemaType.ARRAY, implementation = Affected.class)
	private List<@Valid Affected> affecteds;

	public static DisturbanceUpdateRequest create() {
		return new DisturbanceUpdateRequest();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public DisturbanceUpdateRequest withTitle(String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DisturbanceUpdateRequest withDescription(String description) {
		this.description = description;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public DisturbanceUpdateRequest withStatus(Status status) {
		this.status = status;
		return this;
	}

	public OffsetDateTime getPlannedStartDate() {
		return plannedStartDate;
	}

	public void setPlannedStartDate(OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
	}

	public DisturbanceUpdateRequest withPlannedStartDate(OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
		return this;
	}

	public OffsetDateTime getPlannedStopDate() {
		return plannedStopDate;
	}

	public void setPlannedStopDate(OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
	}

	public DisturbanceUpdateRequest withPlannedStopDate(OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
		return this;
	}

	public List<Affected> getAffecteds() {
		return affecteds;
	}

	public void setAffecteds(List<Affected> affecteds) {
		this.affecteds = affecteds;
	}

	public DisturbanceUpdateRequest withAffecteds(List<Affected> affecteds) {
		this.affecteds = affecteds;
		return this;
	}

	@Override
	public int hashCode() { return Objects.hash(description, affecteds, plannedStartDate, plannedStopDate, status, title); }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DisturbanceUpdateRequest other = (DisturbanceUpdateRequest) obj;
		return Objects.equals(description, other.description) && Objects.equals(affecteds, other.affecteds) && Objects.equals(plannedStartDate, other.plannedStartDate)
			&& Objects.equals(plannedStopDate, other.plannedStopDate) && status == other.status && Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DisturbanceUpdateRequest [title=").append(title).append(", description=").append(description).append(", status=").append(status)
			.append(", plannedStartDate=").append(plannedStartDate).append(", plannedStopDate=").append(plannedStopDate).append(", affecteds=").append(affecteds).append("]");
		return builder.toString();
	}
}
