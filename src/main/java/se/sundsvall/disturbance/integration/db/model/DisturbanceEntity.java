package se.sundsvall.disturbance.integration.db.model;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "disturbance", indexes = {
	@Index(name = "disturbance_id_index", columnList = "disturbance_id"),
	@Index(name = "category_index", columnList = "category")
})
public class DisturbanceEntity implements Serializable {

	private static final long serialVersionUID = -4882470746578837725L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "disturbance_id", nullable = false)
	private String disturbanceId;

	@Column(name = "category", nullable = false)
	private String category;

	@Column(name = "title")
	private String title;

	@Column(name = "description", nullable = false, length = 8192)
	private String description;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "planned_start_date")
	private OffsetDateTime plannedStartDate;

	@Column(name = "planned_stop_date")
	private OffsetDateTime plannedStopDate;

	@Column(name = "created")
	private OffsetDateTime created;

	@Column(name = "updated")
	private OffsetDateTime updated;

	@Column(name = "deleted")
	private boolean deleted;

	@OneToMany(mappedBy = "disturbanceEntity", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AffectedEntity> affectedEntities;

	@PrePersist
	void prePersist() {
		created = now().truncatedTo(MILLIS);
	}

	@PreUpdate
	void preUpdate() {
		updated = now().truncatedTo(MILLIS);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDisturbanceId() {
		return disturbanceId;
	}

	public void setDisturbanceId(String disturbanceId) {
		this.disturbanceId = disturbanceId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public OffsetDateTime getPlannedStartDate() {
		return plannedStartDate;
	}

	public void setPlannedStartDate(OffsetDateTime plannedStartDate) {
		this.plannedStartDate = plannedStartDate;
	}

	public OffsetDateTime getPlannedStopDate() {
		return plannedStopDate;
	}

	public void setPlannedStopDate(OffsetDateTime plannedStopDate) {
		this.plannedStopDate = plannedStopDate;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(OffsetDateTime updated) {
		this.updated = updated;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public List<AffectedEntity> getAffectedEntities() {
		return affectedEntities;
	}

	public void setAffectedEntities(List<AffectedEntity> affectedEntities) {
		this.affectedEntities = affectedEntities;
	}

	public void addAffectedEntities(List<AffectedEntity> affectedEntities) {
		ofNullable(affectedEntities).ifPresent(entities -> {
			if (this.affectedEntities == null) {
				this.affectedEntities = new ArrayList<>();
			}
			entities.stream().forEach(e -> {
				e.setDisturbanceEntity(this);
				this.affectedEntities.add(e);
			});
		});
	}

	public void replaceAffectedEntities(List<AffectedEntity> affectedEntities) {
		if (nonNull(affectedEntities)) {
			if (this.affectedEntities == null) {
				this.affectedEntities = new ArrayList<>();
			}
			this.affectedEntities.clear();
			addAffectedEntities(affectedEntities);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(affectedEntities, category, created, description, disturbanceId, id, plannedStartDate, plannedStopDate, status, title, updated, deleted);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DisturbanceEntity other = (DisturbanceEntity) obj;
		return Objects.equals(affectedEntities, other.affectedEntities) && Objects.equals(category, other.category) && Objects.equals(created, other.created)
			&& Objects.equals(description, other.description) && Objects.equals(disturbanceId, other.disturbanceId) && Objects.equals(id, other.id)
			&& Objects.equals(plannedStartDate, other.plannedStartDate) && Objects.equals(plannedStopDate, other.plannedStopDate) && Objects.equals(status, other.status)
			&& Objects.equals(title, other.title) && Objects.equals(updated, other.updated) && Objects.equals(deleted, other.deleted);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DisturbanceEntity [id=").append(id).append(", disturbanceId=").append(disturbanceId).append(", category=").append(category).append(", title=").append(title)
			.append(", description=").append(description).append(", status=").append(status).append(", plannedStartDate=").append(plannedStartDate).append(", plannedStopDate=")
			.append(plannedStopDate).append(", created=").append(created).append(", updated=").append(updated).append(", deleted=").append(deleted).append(", affectedEntities=")
			.append(affectedEntities).append("]");
		return builder.toString();
	}
}
