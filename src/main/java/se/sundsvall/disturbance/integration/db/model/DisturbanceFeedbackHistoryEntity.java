package se.sundsvall.disturbance.integration.db.model;

import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "disturbance_feedback_history")
public class DisturbanceFeedbackHistoryEntity implements Serializable {

	private static final long serialVersionUID = -7915948637114509762L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "disturbance_id", nullable = false)
	private String disturbanceId;

	@Column(name = "party_id", nullable = false)
	private String partyId;

	@Column(name = "category", nullable = false)
	private String category;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "created")
	private OffsetDateTime created;

	@PrePersist
	void prePersist() {
		created = now().truncatedTo(MILLIS);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDisturbanceId() {
		return disturbanceId;
	}

	public void setDisturbanceId(String disturbanceId) {
		this.disturbanceId = disturbanceId;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DisturbanceFeedbackHistoryEntity that = (DisturbanceFeedbackHistoryEntity) o;
		return Objects.equals(id, that.id) && Objects.equals(disturbanceId, that.disturbanceId) && Objects.equals(partyId, that.partyId)
			&& Objects.equals(category, that.category) && Objects.equals(status, that.status) && Objects.equals(created, that.created);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, disturbanceId, partyId, category, status, created);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DisturbanceFeedbackHistoryEntity [id=").append(id).append(", disturbanceId=").append(disturbanceId).append(", partyId=").append(partyId)
			.append(", category=").append(category).append(", status=").append(status).append(", created=").append(created).append("]");
		return builder.toString();
	}
}
