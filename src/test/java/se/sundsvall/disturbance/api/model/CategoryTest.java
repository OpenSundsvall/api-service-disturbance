package se.sundsvall.disturbance.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.disturbance.api.model.Category.COMMUNICATION;
import static se.sundsvall.disturbance.api.model.Category.DISTRICT_COOLING;
import static se.sundsvall.disturbance.api.model.Category.DISTRICT_HEATING;
import static se.sundsvall.disturbance.api.model.Category.ELECTRICITY;
import static se.sundsvall.disturbance.api.model.Category.WATER;

import org.junit.jupiter.api.Test;

class CategoryTest {

	@Test
	void categoryEnum() {
		assertThat(Category.values()).containsExactly(COMMUNICATION, DISTRICT_HEATING, DISTRICT_COOLING, ELECTRICITY, WATER);
	}
}
