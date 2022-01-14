package se.sundsvall.disturbance.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static com.google.code.beanmatchers.BeanMatchers.registerValueGenerator;
import static java.time.OffsetDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DisturbanceTest {

	@BeforeAll
	static void setup() {
		registerValueGenerator(() -> now().plusDays(new Random().nextInt()), OffsetDateTime.class);
	}

	@Test
	void testBean() {
		assertThat(Disturbance.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var category = Category.COMMUNICATION;
		final var created = now();
		final var updated = now().plusHours(5);
		final var description = "some description";
		final var id = "1234567890";
		final var status = Status.CLOSED;
		final var title = "Title";
		final var affecteds = List.of(Affected.create());
		final var plannedStartDate = now();
		final var plannedStopDate = now();

		final var disturbance = Disturbance.create()
			.withCategory(category)
			.withCreated(created)
			.withDescription(description)
			.withId(id)
			.withAffecteds(affecteds)
			.withPlannedStartDate(plannedStartDate)
			.withPlannedStopDate(plannedStopDate)
			.withStatus(status)
			.withTitle(title)
			.withUpdated(updated);

		assertThat(disturbance).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(disturbance.getAffecteds()).isEqualTo(affecteds);
		assertThat(disturbance.getCategory()).isEqualTo(category);
		assertThat(disturbance.getCreated()).isEqualTo(created);
		assertThat(disturbance.getDescription()).isEqualTo(description);
		assertThat(disturbance.getId()).isEqualTo(id);
		assertThat(disturbance.getPlannedStartDate()).isEqualTo(plannedStartDate);
		assertThat(disturbance.getPlannedStopDate()).isEqualTo(plannedStopDate);
		assertThat(disturbance.getStatus()).isEqualTo(status);
		assertThat(disturbance.getTitle()).isEqualTo(title);
		assertThat(disturbance.getUpdated()).isEqualTo(updated);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Disturbance.create()).hasAllNullFieldsOrProperties();
	}
}
