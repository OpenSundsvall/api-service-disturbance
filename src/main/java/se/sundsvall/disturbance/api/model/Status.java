package se.sundsvall.disturbance.api.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Status model")
public enum Status {
	OPEN,
	CLOSED,
	PLANNED;
}
