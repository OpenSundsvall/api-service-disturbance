package se.sundsvall.disturbance.integration.db.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "schema_history")
public class SchemaHistoryEntity implements Serializable {

	private static final long serialVersionUID = 8141773118200476617L;

	@Id
	@Column(name = "schema_version", nullable = false)
	private String schemaVersion;

	@Column(name = "comment", nullable = false, length = 8192)
	private String comment;

	@Column(name = "applied", nullable = false)
	private OffsetDateTime applied;

	public String getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public OffsetDateTime getApplied() {
		return applied;
	}

	public void setApplied(OffsetDateTime applied) {
		this.applied = applied;
	}

	@Override
	public int hashCode() { return Objects.hash(applied, comment, schemaVersion); }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SchemaHistoryEntity other = (SchemaHistoryEntity) obj;
		return Objects.equals(applied, other.applied) && Objects.equals(comment, other.comment) && Objects.equals(schemaVersion, other.schemaVersion);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SchemaHistoryEntity [schemaVersion=").append(schemaVersion).append(", comment=").append(comment).append(", applied=").append(applied).append("]");
		return builder.toString();
	}
}
