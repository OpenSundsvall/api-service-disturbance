package se.sundsvall.disturbance.api.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Disturbance create request model")
public class DisturbanceCreateRequest {

	@Schema(description = "Disturbance ID", required = true, example = "435553")
	@NotNull
	@Size(max = 255)
	private String id;

	@Schema(description = "Disturbance category", required = true)
	@NotNull
	private Category category;

	@Schema(description = "Title", required = true, example = "Disturbance")
	@NotNull
	@Size(max = 255)
	private String title;

	@Schema(description = "Description", required = true, example = "Major disturbance")
	@NotNull
	@Size(max = 8192)
	private String description;

	@Schema(description = "Disturbance status", required = true)
	@NotNull
	private Status status;

	@Schema(description = "Planned start date for the disturbance")
	private OffsetDateTime plannedStartDate;

	@Schema(description = "Planned stop date for the disturbance")
	private OffsetDateTime plannedStopDate;

	@Schema(type = SchemaType.ARRAY, implementation = Affected.class)
	private List<@Valid Affected> affecteds;

	public static DisturbanceCreateRequest create() {
		return new DisturbanceCreateRequest();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DisturbanceCreateRequest withId(String id) {
		this.id = id;
		return this;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public DisturbanceCreateRequest withCategory(Category category) {
		this.category = category;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public DisturbanceCreateRequest withTitle(String title) {
		this.title = title;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DisturbanceCreateRequest withDescription(String description) {
		this.description = description;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public DisturbanceCreateRequest withStatus(Status status) {
		this.status = status;
		return this;
	}

	public OffsetDateTime getPlannedStartDate() {
		return plannedStartDate;
	}

	public void setPlannedStartDate(OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
	}

	public DisturbanceCreateRequest withPlannedStartDate(OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
		return this;
	}

	public OffsetDateTime getPlannedStopDate() {
		return plannedStopDate;
	}

	public void setPlannedStopDate(OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
	}

	public DisturbanceCreateRequest withPlannedStopDate(OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
		return this;
	}

	public List<Affected> getAffecteds() {
		return affecteds;
	}

	public void setAffecteds(List<Affected> affecteds) {
		this.affecteds = affecteds;
	}

	public DisturbanceCreateRequest withAffecteds(List<Affected> affecteds) {
		this.affecteds = affecteds;
		return this;
	}

	@Override
	public int hashCode() { return Objects.hash(category, description, id, affecteds, plannedStartDate, plannedStopDate, status, title); }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DisturbanceCreateRequest other = (DisturbanceCreateRequest) obj;
		return category == other.category && Objects.equals(description, other.description) && Objects.equals(id, other.id) && Objects.equals(affecteds, other.affecteds)
			&& Objects.equals(plannedStartDate, other.plannedStartDate) && Objects.equals(plannedStopDate, other.plannedStopDate) && status == other.status
			&& Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DisturbanceCreateRequest [id=").append(id).append(", category=").append(category).append(", title=").append(title).append(", description=")
			.append(description).append(", status=").append(status).append(", plannedStartDate=").append(plannedStartDate).append(", plannedStopDate=").append(plannedStopDate)
			.append(", affecteds=").append(affecteds).append("]");
		return builder.toString();
	}
}
