package se.sundsvall.disturbance.service;

class ServiceConstants {

	private ServiceConstants() {}

	static final String ERROR_DISTURBANCE_NOT_FOUND = "No disturbance found for category:'%s' and id:'%s'!";
	static final String ERROR_DISTURBANCE_ALREADY_EXISTS = "A disturbance with category:'%s' and id:'%s' already exists!";
	static final String ERROR_DISTURBANCE_CLOSED_NO_UPDATES_ALLOWED = "The disturbance with category:'%s' and id:'%s' is closed! No updates are allowed on closed disturbances!";
	static final String ERROR_DISTURBANCE_CLOSED = "A disturbance with category:'%s' and id:'%s' exists, but is closed!";
	static final String ERROR_DISTURBANCE_FEEDBACK_ALREADY_EXISTS = "A disturbance feedback with category:'%s', id:'%s' and partyId:'%s' already exists!";
	static final String ERROR_FEEDBACK_ALREADY_EXISTS = "A feedback entity for partyId:'%s' already exists!";
	static final String ERROR_FEEDBACK_NOT_FOUND = "No feedback entity found for partyId:'%s'!";
}
