package se.sundsvall.disturbance.integration.db.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "affected", indexes = {
	@Index(name = "party_id_index", columnList = "party_id")
})
public class AffectedEntity implements Serializable {

	private static final long serialVersionUID = 8835799401886595749L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private long id;

	@Column(name = "party_id")
	private String partyId;

	@Column(name = "reference", length = 512)
	private String reference;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id", nullable = false, foreignKey = @ForeignKey(name = "fk_affected_parent_id_disturbance_id"))
	private DisturbanceEntity disturbanceEntity;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}

	public DisturbanceEntity getDisturbanceEntity() {
		return disturbanceEntity;
	}

	public void setDisturbanceEntity(DisturbanceEntity disturbanceEntity) {
		this.disturbanceEntity = disturbanceEntity;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		AffectedEntity affectedEntity = (AffectedEntity) o;
		return Objects.equals(id, affectedEntity.id) && Objects.equals(partyId, affectedEntity.partyId) && Objects.equals(reference, affectedEntity.reference);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, partyId, reference);
	}

	@Override
	public String toString() {
		long disturbanceId = disturbanceEntity == null ? 0L : disturbanceEntity.getId();
		StringBuilder builder = new StringBuilder();
		builder.append("AffectedEntity [id=").append(id).append(", partyId=").append(partyId).append(", reference=").append(reference).append(", disturbanceEntity.id=")
			.append(disturbanceId).append("]");
		return builder.toString();
	}
}
