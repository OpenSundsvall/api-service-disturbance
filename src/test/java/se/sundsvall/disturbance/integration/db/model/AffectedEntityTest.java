package se.sundsvall.disturbance.integration.db.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringExcluding;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import org.junit.jupiter.api.Test;

class AffectedEntityTest {

	@Test
	void testBean() {
		assertThat(AffectedEntity.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCodeExcluding("disturbanceEntity"),
			hasValidBeanEqualsExcluding("disturbanceEntity"),
			hasValidBeanToStringExcluding("disturbanceEntity")));
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(new AffectedEntity()).hasAllNullFieldsOrPropertiesExcept("id");
	}
}
