package se.sundsvall.disturbance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceFeedbackEntity;

@ExtendWith(MockitoExtension.class)
class DisturbanceFeedbackServiceTest {

	@Mock
	private DisturbanceFeedbackRepository disturbanceFeedbackRepositoryMock;

	@Mock
	private DisturbanceRepository disturbanceRepositoryMock;

	@InjectMocks
	private DisturbanceFeedbackService disturbanceFeedbackService;

	@Captor
	private ArgumentCaptor<DisturbanceFeedbackEntity> disturbanceFeedbackEntityCaptor;

	@Test
	void createDisturbanceFeedback() throws ServiceException {

		final var disturbanceId = "1337";
		final var category = Category.COMMUNICATION;
		final var partyId = "81471222-5798-11e9-ae24-57fa13b361e1";

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.of(new DisturbanceEntity()));
		when(disturbanceFeedbackRepositoryMock.findByCategoryAndDisturbanceIdAndPartyIdOptional(any(Category.class), any(String.class), any(String.class)))
			.thenReturn(Optional.empty());

		disturbanceFeedbackService.createDisturbanceFeedback(category, disturbanceId, DisturbanceFeedbackCreateRequest.create().withPartyId(partyId));

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verify(disturbanceFeedbackRepositoryMock).findByCategoryAndDisturbanceIdAndPartyIdOptional(category, disturbanceId, partyId);
		verify(disturbanceFeedbackRepositoryMock).persist(disturbanceFeedbackEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, disturbanceFeedbackRepositoryMock);

		final var disturbanceFeedbackEntityCaptorValue = disturbanceFeedbackEntityCaptor.getValue();
		assertThat(disturbanceFeedbackEntityCaptorValue).isNotNull();
		assertThat(disturbanceFeedbackEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceFeedbackEntityCaptorValue.getPartyId()).isEqualTo(partyId);
		assertThat(disturbanceFeedbackEntityCaptorValue.getCategory()).isEqualTo(category.toString());
	}

	@Test
	void createDisturbanceFeedbackWhenNoMatchingDisturbanceExists() {

		final var disturbanceId = "1337";
		final var category = Category.COMMUNICATION;
		final var partyId = "81471222-5798-11e9-ae24-57fa13b361e1";

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.empty());

		final var serviceException = assertThrows(ServiceException.class, () -> disturbanceFeedbackService.createDisturbanceFeedback(
			category, disturbanceId, DisturbanceFeedbackCreateRequest.create().withPartyId(partyId)));

		assertThat(serviceException.getMessage()).isEqualTo("No disturbance found for category:'COMMUNICATION' and id:'1337'!");
		assertThat(serviceException.getStatus()).isEqualTo(Status.NOT_FOUND);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(disturbanceFeedbackRepositoryMock);
	}

	@Test
	void createDisturbanceFeedbackWhenAlreadyCreated() {

		final var disturbanceId = "1337";
		final var category = Category.COMMUNICATION;
		final var partyId = "81471222-5798-11e9-ae24-57fa13b361e1";

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.of(new DisturbanceEntity()));
		when(disturbanceFeedbackRepositoryMock.findByCategoryAndDisturbanceIdAndPartyIdOptional(any(Category.class), any(String.class), any(String.class)))
			.thenReturn(Optional.of(new DisturbanceFeedbackEntity()));

		final var serviceException = assertThrows(ServiceException.class,
			() -> disturbanceFeedbackService.createDisturbanceFeedback(category, disturbanceId, DisturbanceFeedbackCreateRequest.create().withPartyId(partyId)));

		assertThat(serviceException.getMessage())
			.isEqualTo("A disturbance feedback with category:'COMMUNICATION', id:'1337' and partyId:'81471222-5798-11e9-ae24-57fa13b361e1' already exists!");
		assertThat(serviceException.getStatus()).isEqualTo(Status.CONFLICT);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verify(disturbanceFeedbackRepositoryMock).findByCategoryAndDisturbanceIdAndPartyIdOptional(category, disturbanceId, partyId);
		verifyNoMoreInteractions(disturbanceRepositoryMock, disturbanceFeedbackRepositoryMock);
	}

	@Test
	void createDisturbanceFeedbackWhenDisturbanceIsClosed() {

		final var disturbanceId = "1337";
		final var category = Category.COMMUNICATION;
		final var partyId = "81471222-5798-11e9-ae24-57fa13b361e1";

		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.setStatus(se.sundsvall.disturbance.api.model.Status.CLOSED.toString());

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.of(disturbanceEntity));

		final var serviceException = assertThrows(ServiceException.class,
			() -> disturbanceFeedbackService.createDisturbanceFeedback(category, disturbanceId, DisturbanceFeedbackCreateRequest.create().withPartyId(partyId)));

		assertThat(serviceException.getMessage()).isEqualTo("A disturbance with category:'COMMUNICATION' and id:'1337' exists, but is closed!");
		assertThat(serviceException.getStatus()).isEqualTo(Status.CONFLICT);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(disturbanceFeedbackRepositoryMock);
	}
}
