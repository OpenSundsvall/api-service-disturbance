package se.sundsvall.disturbance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.model.FeedbackCreateRequest;
import se.sundsvall.disturbance.integration.db.FeedbackRepository;
import se.sundsvall.disturbance.integration.db.model.FeedbackEntity;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

	@Mock
	private FeedbackRepository feedbackRepositoryMock;

	@InjectMocks
	private FeedbackService feedbackService;

	@Captor
	private ArgumentCaptor<FeedbackEntity> feedbackEntityCaptor;

	@Test
	void createFeedback() throws ServiceException {

		final var partyId = UUID.randomUUID().toString();

		when(feedbackRepositoryMock.findByPartyIdOptional(any(String.class))).thenReturn(Optional.empty());

		feedbackService.createFeedback(FeedbackCreateRequest.create().withPartyId(partyId));

		verify(feedbackRepositoryMock).findByPartyIdOptional(partyId);
		verify(feedbackRepositoryMock).persist(feedbackEntityCaptor.capture());
		verifyNoMoreInteractions(feedbackRepositoryMock);

		final var feedbackEntityCaptorValue = feedbackEntityCaptor.getValue();
		assertThat(feedbackEntityCaptorValue).isNotNull();
		assertThat(feedbackEntityCaptorValue.getPartyId()).isEqualTo(partyId);
	}

	@Test
	void createFeedbackWhenAlreadyCreated() {

		final var partyId = "81471222-5798-11e9-ae24-57fa13b361e1";

		when(feedbackRepositoryMock.findByPartyIdOptional(any())).thenReturn(Optional.of(new FeedbackEntity()));

		final var serviceException = assertThrows(ServiceException.class, () -> feedbackService.createFeedback(FeedbackCreateRequest.create().withPartyId(partyId)));

		assertThat(serviceException.getMessage()).isEqualTo("A feedback entity for partyId:'81471222-5798-11e9-ae24-57fa13b361e1' already exists!");
		assertThat(serviceException.getStatus()).isEqualTo(Status.CONFLICT);

		verify(feedbackRepositoryMock).findByPartyIdOptional(partyId);
		verifyNoMoreInteractions(feedbackRepositoryMock);
	}

	@Test
	void deleteFeedback() throws ServiceException {

		final var partyId = UUID.randomUUID().toString();
		final var feedbackEntity = new FeedbackEntity();
		feedbackEntity.setPartyId(partyId);

		when(feedbackRepositoryMock.findByPartyIdOptional(any())).thenReturn(Optional.of(feedbackEntity));

		feedbackService.deleteFeedback(partyId);

		verify(feedbackRepositoryMock).findByPartyIdOptional(partyId);
		verify(feedbackRepositoryMock).delete(feedbackEntityCaptor.capture());
		verifyNoMoreInteractions(feedbackRepositoryMock);

		final var feedbackEntityCaptorValue = feedbackEntityCaptor.getValue();
		assertThat(feedbackEntityCaptorValue).isNotNull();
		assertThat(feedbackEntityCaptorValue.getPartyId()).isEqualTo(partyId);
	}

	@Test
	void deleteFeedbackNotFound() throws ServiceException {

		final var partyId = "61e4f268-c5db-494c-86a0-4cc9a5cf411f";

		when(feedbackRepositoryMock.findByPartyIdOptional(any())).thenReturn(Optional.empty());

		final var serviceException = assertThrows(ServiceException.class, () -> feedbackService.deleteFeedback(partyId));

		assertThat(serviceException.getMessage()).isEqualTo("No feedback entity found for partyId:'61e4f268-c5db-494c-86a0-4cc9a5cf411f'!");
		assertThat(serviceException.getStatus()).isEqualTo(Status.NOT_FOUND);

		verify(feedbackRepositoryMock).findByPartyIdOptional(partyId);
		verifyNoMoreInteractions(feedbackRepositoryMock);
	}
}
