package se.sundsvall.disturbance.api.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Category model")
public enum Category {
	COMMUNICATION,
	DISTRICT_HEATING,
	DISTRICT_COOLING,
	ELECTRICITY,
	WATER;
}
