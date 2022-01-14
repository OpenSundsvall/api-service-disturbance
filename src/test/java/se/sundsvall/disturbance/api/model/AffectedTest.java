package se.sundsvall.disturbance.api.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class AffectedTest {

	@Test
	void testBean() {
		assertThat(Affected.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {

		final var partyId = UUID.randomUUID().toString();
		final var reference = "some description";

		final var affected = Affected.create()
			.withPartyId(partyId)
			.withReference(reference);

		assertThat(affected).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(affected.getPartyId()).isEqualTo(partyId);
		assertThat(affected.getReference()).isEqualTo(reference);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(Affected.create()).hasAllNullFieldsOrProperties();
	}
}
