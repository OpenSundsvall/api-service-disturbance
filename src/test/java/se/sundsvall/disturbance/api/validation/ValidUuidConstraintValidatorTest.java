package se.sundsvall.disturbance.api.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.stream.Stream;

import javax.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidUuidConstraintValidatorTest {

	@Mock
	private ConstraintValidatorContext constraintValidatorContextMock;

	@InjectMocks
	private ValidUuidConstraintValidator validator;

	@ParameterizedTest
	@MethodSource("generate100RandomUUIDs")
	void validUuid(String uuid) {
		assertThat(validator.isValid(uuid, constraintValidatorContextMock)).isTrue();
	}

	@Test
	void invalidUuid() {
		assertThat(validator.isValid("not-valid", constraintValidatorContextMock)).isFalse();
	}

	private static Stream<Arguments> generate100RandomUUIDs() {
		return Stream.generate(UUID::randomUUID)
			.limit(100)
			.map(uuid -> Arguments.of(uuid.toString()));
	}
}
