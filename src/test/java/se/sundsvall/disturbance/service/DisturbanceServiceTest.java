package se.sundsvall.disturbance.service;

import static java.time.OffsetDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.disturbance.service.mapper.DisturbanceFeedbackMapper.toDisturbanceFeedbackEntity;
import static se.sundsvall.disturbance.service.mapper.DisturbanceMapper.toDisturbanceEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import se.sundsvall.disturbance.api.model.Affected;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.integration.db.DisturbanceFeedbackRepository;
import se.sundsvall.disturbance.integration.db.DisturbanceRepository;
import se.sundsvall.disturbance.integration.db.FeedbackRepository;
import se.sundsvall.disturbance.integration.db.model.AffectedEntity;
import se.sundsvall.disturbance.integration.db.model.DisturbanceEntity;
import se.sundsvall.disturbance.integration.db.model.FeedbackEntity;
import se.sundsvall.disturbance.service.message.SendMessageLogic;

@ExtendWith(MockitoExtension.class)
class DisturbanceServiceTest {

	@Mock
	private DisturbanceRepository disturbanceRepositoryMock;

	@Mock
	private DisturbanceFeedbackRepository disturbanceFeedbackRepositoryMock;

	@Mock
	private FeedbackRepository feedbackRepositoryMock;

	@Mock
	private SendMessageLogic sendMessageLogicMock;

	@InjectMocks
	private DisturbanceService disturbanceService;

	@Captor
	private ArgumentCaptor<DisturbanceEntity> disturbanceEntityCaptor;

	@Test
	void findByDisturbanceIdAndCategorySuccess() throws ServiceException {

		// Parameters
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var status = se.sundsvall.disturbance.api.model.Status.OPEN;

		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.setDisturbanceId(disturbanceId);
		disturbanceEntity.setCategory(category.toString());
		disturbanceEntity.setStatus(status.toString());

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).thenReturn(Optional.of(disturbanceEntity));

		final var disturbance = disturbanceService.findByCategoryAndDisturbanceId(category, disturbanceId);

		assertThat(disturbance).isNotNull();
		assertThat(disturbance.getCategory()).isEqualTo(Category.COMMUNICATION);
		assertThat(disturbance.getId()).isEqualTo(disturbanceId);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock, feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);
	}

	@Test
	void findByDisturbanceIdAndCategoryNotFound() {

		// Parameters
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).thenReturn(empty());

		final var serviceException = assertThrows(ServiceException.class, () -> disturbanceService.findByCategoryAndDisturbanceId(category, disturbanceId));

		assertThat(serviceException.getMessage()).isEqualTo("No disturbance found for category:'COMMUNICATION' and id:'12345'!");
		assertThat(serviceException.getStatus()).isEqualTo(Status.NOT_FOUND);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock, feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);
	}

	@Test
	void createDisturbance() throws ServiceException {

		// Parameters
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		final var disturbanceEntity = toDisturbanceEntity(disturbanceCreateRequest);

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(empty());
		when(disturbanceRepositoryMock.persistAndFetch(any(DisturbanceEntity.class))).thenReturn(disturbanceEntity);

		final var disturbance = disturbanceService.createDisturbance(disturbanceCreateRequest);
		assertThat(disturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId());
		verify(disturbanceRepositoryMock).persistAndFetch(disturbanceEntityCaptor.capture());
		verify(sendMessageLogicMock).sendCreateMessage(disturbanceEntity);
		verify(feedbackRepositoryMock).findByPartyIdOptional("partyId-1");
		verify(feedbackRepositoryMock).findByPartyIdOptional("partyId-2");
		verify(feedbackRepositoryMock).findByPartyIdOptional("partyId-3");
		verifyNoMoreInteractions(disturbanceRepositoryMock, feedbackRepositoryMock, sendMessageLogicMock);
		verifyNoInteractions(disturbanceFeedbackRepositoryMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).hasSize(3); // Duplicates removed.
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1", "partyId-2", "partyId-3");
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-1", "reference-2", "reference-3");
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(disturbanceCreateRequest.getCategory().toString());
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(disturbanceCreateRequest.getDescription());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceCreateRequest.getId());
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(disturbanceCreateRequest.getPlannedStartDate());
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(disturbanceCreateRequest.getPlannedStopDate());
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(disturbanceCreateRequest.getStatus().toString());
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(disturbanceCreateRequest.getTitle());
	}

	@Test
	void createDisturbanceWhenFeedbackExists() throws ServiceException {

		// Parameters
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"), // No existing feedback
				Affected.create().withPartyId("partyId-2").withReference("reference-2"), // Will have existing feedback
				Affected.create().withPartyId("partyId-2").withReference("reference-2"), // Will have existing feedback, but removed since duplicate
				Affected.create().withPartyId("partyId-3").withReference("reference-3"))); // Will have existing feedback

		final var disturbanceEntity = toDisturbanceEntity(disturbanceCreateRequest);

		when(feedbackRepositoryMock.findByPartyIdOptional("partyId-1")).thenReturn(Optional.empty());
		when(feedbackRepositoryMock.findByPartyIdOptional("partyId-2")).thenReturn(Optional.of(new FeedbackEntity()));
		when(feedbackRepositoryMock.findByPartyIdOptional("partyId-3")).thenReturn(Optional.of(new FeedbackEntity()));
		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(empty());
		when(disturbanceRepositoryMock.persistAndFetch(any(DisturbanceEntity.class))).thenReturn(disturbanceEntity);

		final var disturbance = disturbanceService.createDisturbance(disturbanceCreateRequest);
		assertThat(disturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId());
		verify(disturbanceRepositoryMock).persistAndFetch(disturbanceEntityCaptor.capture());
		verify(feedbackRepositoryMock).findByPartyIdOptional("partyId-1");
		verify(feedbackRepositoryMock).findByPartyIdOptional("partyId-2");
		verify(feedbackRepositoryMock).findByPartyIdOptional("partyId-3");
		verify(disturbanceFeedbackRepositoryMock).persist(toDisturbanceFeedbackEntity(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId(),
			DisturbanceFeedbackCreateRequest.create().withPartyId("partyId-2")));
		verify(disturbanceFeedbackRepositoryMock).persist(toDisturbanceFeedbackEntity(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId(),
			DisturbanceFeedbackCreateRequest.create().withPartyId("partyId-3")));
		verify(sendMessageLogicMock).sendCreateMessage(disturbanceEntity);
		verifyNoMoreInteractions(disturbanceRepositoryMock, disturbanceFeedbackRepositoryMock, feedbackRepositoryMock, sendMessageLogicMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).hasSize(3); // Duplicates removed.
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1", "partyId-2", "partyId-3");
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-1", "reference-2", "reference-3");
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(disturbanceCreateRequest.getCategory().toString());
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(disturbanceCreateRequest.getDescription());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceCreateRequest.getId());
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(disturbanceCreateRequest.getPlannedStartDate());
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(disturbanceCreateRequest.getPlannedStopDate());
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(disturbanceCreateRequest.getStatus().toString());
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(disturbanceCreateRequest.getTitle());
	}

	@Test
	void createDisturbanceWhenFeedbackExistsButStatusIsClosed() throws ServiceException {

		// Parameters
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.CLOSED)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		final var disturbanceEntity = toDisturbanceEntity(disturbanceCreateRequest);

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(empty());
		when(disturbanceRepositoryMock.persistAndFetch(any(DisturbanceEntity.class))).thenReturn(disturbanceEntity);

		final var disturbance = disturbanceService.createDisturbance(disturbanceCreateRequest);
		assertThat(disturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId());
		verify(disturbanceRepositoryMock).persistAndFetch(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(disturbanceFeedbackRepositoryMock, feedbackRepositoryMock, sendMessageLogicMock); // No interactions here if status is CLOSED.

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).hasSize(3); // Duplicates removed.
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1", "partyId-2", "partyId-3");
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-1", "reference-2", "reference-3");
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(disturbanceCreateRequest.getCategory().toString());
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(disturbanceCreateRequest.getDescription());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceCreateRequest.getId());
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(disturbanceCreateRequest.getPlannedStartDate());
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(disturbanceCreateRequest.getPlannedStopDate());
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(disturbanceCreateRequest.getStatus().toString());
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(disturbanceCreateRequest.getTitle());
	}

	@Test
	void createDisturbanceWhenFeedbackExistsButStatusIsPlanned() throws ServiceException {

		// Parameters
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.PLANNED)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"), // No existing feedback
				Affected.create().withPartyId("partyId-2").withReference("reference-2"), // Will have existing feedback
				Affected.create().withPartyId("partyId-3").withReference("reference-3"))); // Will have existing feedback

		final var disturbanceEntity = toDisturbanceEntity(disturbanceCreateRequest);

		when(feedbackRepositoryMock.findByPartyIdOptional("partyId-1")).thenReturn(empty());
		when(feedbackRepositoryMock.findByPartyIdOptional("partyId-2")).thenReturn(Optional.of(new FeedbackEntity()));
		when(feedbackRepositoryMock.findByPartyIdOptional("partyId-3")).thenReturn(Optional.of(new FeedbackEntity()));
		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(empty());
		when(disturbanceRepositoryMock.persistAndFetch(any(DisturbanceEntity.class))).thenReturn(disturbanceEntity);

		final var disturbance = disturbanceService.createDisturbance(disturbanceCreateRequest);
		assertThat(disturbance).isNotNull();

		verify(feedbackRepositoryMock).findByPartyIdOptional("partyId-1");
		verify(feedbackRepositoryMock).findByPartyIdOptional("partyId-2");
		verify(feedbackRepositoryMock).findByPartyIdOptional("partyId-3");
		verify(disturbanceFeedbackRepositoryMock).persist(toDisturbanceFeedbackEntity(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId(),
			DisturbanceFeedbackCreateRequest.create().withPartyId("partyId-2")));
		verify(disturbanceFeedbackRepositoryMock).persist(toDisturbanceFeedbackEntity(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId(),
			DisturbanceFeedbackCreateRequest.create().withPartyId("partyId-3")));
		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId());
		verify(disturbanceRepositoryMock).persistAndFetch(disturbanceEntityCaptor.capture());

		verifyNoMoreInteractions(disturbanceRepositoryMock, disturbanceFeedbackRepositoryMock, feedbackRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock); // No interactions here if status is PLANNED.

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).hasSize(3); // Duplicates removed.
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1", "partyId-2", "partyId-3");
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-1", "reference-2", "reference-3");
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(disturbanceCreateRequest.getCategory().toString());
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(disturbanceCreateRequest.getDescription());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceCreateRequest.getId());
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(disturbanceCreateRequest.getPlannedStartDate());
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(disturbanceCreateRequest.getPlannedStopDate());
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(disturbanceCreateRequest.getStatus().toString());
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(disturbanceCreateRequest.getTitle());
	}

	@Test
	void createDisturbanceWhenAlreadyCreated() throws ServiceException {

		// Parameters
		final var disturbanceCreateRequest = DisturbanceCreateRequest.create()
			.withCategory(Category.COMMUNICATION)
			.withId("id")
			.withStatus(se.sundsvall.disturbance.api.model.Status.OPEN)
			.withTitle("title")
			.withDescription("description")
			.withAffecteds(List.of(
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.of(new DisturbanceEntity()));

		final var serviceException = assertThrows(ServiceException.class, () -> disturbanceService.createDisturbance(disturbanceCreateRequest));

		assertThat(serviceException.getMessage()).isEqualTo("A disturbance with category:'COMMUNICATION' and id:'id' already exists!");
		assertThat(serviceException.getStatus()).isEqualTo(Status.CONFLICT);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(disturbanceCreateRequest.getCategory(), disturbanceCreateRequest.getId());
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock, feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);
	}

	@Test
	void findByPartyIdAndCategorySuccess() throws ServiceException {

		// Parameters
		final var categoryFilter = List.of(Category.COMMUNICATION);
		final var partyId = "partyId";
		final var statusFilter = List.of(se.sundsvall.disturbance.api.model.Status.OPEN);

		when(disturbanceRepositoryMock.findByPartyIdFilterByCategoryAndStatus(partyId, categoryFilter, statusFilter)).thenReturn(createDisturbanceEntities());

		final var disturbances = disturbanceService.findByPartyIdAndCategoryAndStatus(partyId, categoryFilter, statusFilter);

		assertThat(disturbances).isNotNull();
		assertThat(disturbances.get(0).getCategory()).isEqualTo(Category.COMMUNICATION);
		assertThat(disturbances.get(0).getId()).isEqualTo("disturbanceId1");
		assertThat(disturbances.get(0).getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.OPEN);
		assertThat(disturbances.get(1).getCategory()).isEqualTo(Category.COMMUNICATION);
		assertThat(disturbances.get(1).getId()).isEqualTo("disturbanceId2");
		assertThat(disturbances.get(1).getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.OPEN);

		verify(disturbanceRepositoryMock).findByPartyIdFilterByCategoryAndStatus(partyId, categoryFilter, statusFilter);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock, feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);
	}

	@Test
	void findByPartyIdAndCategoryNotFound() throws ServiceException {

		// Parameters
		final var categoryFilter = List.of(Category.COMMUNICATION);
		final var partyId = "partyId";
		final var statusFilter = List.of(se.sundsvall.disturbance.api.model.Status.OPEN);

		when(disturbanceRepositoryMock.findByPartyIdFilterByCategoryAndStatus(partyId, categoryFilter, statusFilter)).thenReturn(emptyList());

		final var disturbances = disturbanceService.findByPartyIdAndCategoryAndStatus(partyId, categoryFilter, statusFilter);

		assertThat(disturbances).isNotNull().isEmpty();

		verify(disturbanceRepositoryMock).findByPartyIdFilterByCategoryAndStatus(partyId, categoryFilter, statusFilter);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock, feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);
	}

	@Test
	void deleteByDisturbanceByIdAndCategory() throws ServiceException {

		// Parameters
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var status = se.sundsvall.disturbance.api.model.Status.OPEN;

		final var disturbanceEntity = new DisturbanceEntity();
		disturbanceEntity.setDisturbanceId(disturbanceId);
		disturbanceEntity.setCategory(category.toString());
		disturbanceEntity.setStatus(status.toString());

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).thenReturn(Optional.of(disturbanceEntity));

		disturbanceService.deleteDisturbance(category, disturbanceId);

		final var updatedDisturbanceEntity = disturbanceEntity;
		updatedDisturbanceEntity.setDeleted(true);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verify(disturbanceRepositoryMock).persist(disturbanceEntityCaptor.capture());
		verify(disturbanceFeedbackRepositoryMock).deleteByCategoryAndDisturbanceId(category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);
		verifyNoInteractions(sendMessageLogicMock, feedbackRepositoryMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getDeleted()).isTrue();
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(category.toString());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(status.toString());
	}

	@Test
	void deleteByDisturbanceByIdAndCategoryNotFound() {

		// Parameters
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "disturbanceId";

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(category, disturbanceId)).thenReturn(empty());

		final var serviceException = assertThrows(ServiceException.class, () -> disturbanceService.deleteDisturbance(category, disturbanceId));

		assertThat(serviceException.getMessage()).isEqualTo("No disturbance found for category:'COMMUNICATION' and id:'disturbanceId'!");
		assertThat(serviceException.getStatus()).isEqualTo(Status.NOT_FOUND);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock, feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);
	}

	void updateDisturbanceChangeStatusToClosed() throws ServiceException {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var title = "title";
		final var description = "description";
		final var plannedStartDate = LocalDateTime.of(2021, 10, 12, 18, 30, 6).atOffset(now().getOffset());
		final var plannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 6).atOffset(now().getOffset());

		// Parameters
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withStatus(se.sundsvall.disturbance.api.model.Status.CLOSED);

		final var e1 = new AffectedEntity();
		e1.setPartyId("partyId-1");
		e1.setReference("reference-1");

		final var e2 = new AffectedEntity();
		e2.setPartyId("partyId-2");
		e2.setReference("reference-2");

		final var e3 = new AffectedEntity();
		e3.setPartyId("partyId-3");
		e3.setReference("reference-3");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category.toString());
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(se.sundsvall.disturbance.api.model.Status.OPEN.toString());
		existingDisturbanceEntity.setTitle(title);
		existingDisturbanceEntity.setDescription(description);
		existingDisturbanceEntity.setPlannedStartDate(plannedStartDate);
		existingDisturbanceEntity.setPlannedStopDate(plannedStopDate);
		existingDisturbanceEntity.setAffectedEntities(List.of(e1, e2, e3));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.persistAndFetch(any(DisturbanceEntity.class))).thenReturn(existingDisturbanceEntity);

		final var updatedDisturbance = disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest);

		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendCloseMessageToAllApplicableAffecteds(existingDisturbanceEntity);
		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verify(disturbanceRepositoryMock).persistAndFetch(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);
		verifyNoInteractions(feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).hasSize(3);
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1", "partyId-2", "partyId-3");
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-1", "reference-2", "reference-3");
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(category.toString());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(title);
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(description);
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(plannedStartDate);
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(plannedStopDate);
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.CLOSED.toString());
	}

	@Test
	void updateDisturbanceRemoveAffectedsFromDisturbance() throws ServiceException {

		final var category = Category.COMMUNICATION;
		final var status = se.sundsvall.disturbance.api.model.Status.OPEN;
		final var disturbanceId = "12345";
		final var title = "title";
		final var description = "description";
		final var plannedStartDate = LocalDateTime.of(2021, 10, 12, 18, 30, 6).atOffset(now().getOffset());
		final var plannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 6).atOffset(now().getOffset());

		// Parameters
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withAffecteds(List.of(
				// partyId-1 removed (compared to existing entity)
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3")));

		final var e1 = new AffectedEntity();
		e1.setPartyId("partyId-1");
		e1.setReference("reference-1");

		final var e2 = new AffectedEntity();
		e2.setPartyId("partyId-2");
		e2.setReference("reference-2");

		final var e3 = new AffectedEntity();
		e3.setPartyId("partyId-3");
		e3.setReference("reference-3");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category.toString());
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(status.toString());
		existingDisturbanceEntity.setTitle(title);
		existingDisturbanceEntity.setDescription(description);
		existingDisturbanceEntity.setPlannedStartDate(plannedStartDate);
		existingDisturbanceEntity.setPlannedStopDate(plannedStopDate);
		existingDisturbanceEntity.setAffectedEntities(new ArrayList<>(List.of(e1, e2, e3)));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.persistAndFetch(any(DisturbanceEntity.class))).thenReturn(existingDisturbanceEntity);

		final var updatedDisturbance = disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest);

		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendCloseMessageToProvidedApplicableAffecteds(existingDisturbanceEntity, List.of(e1));
		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verify(disturbanceRepositoryMock).persistAndFetch(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);
		verifyNoInteractions(feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);

		final var disturbanceEntityCaptorValue = disturbanceEntityCaptor.getValue();
		assertThat(disturbanceEntityCaptorValue).isNotNull();
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).hasSize(2);
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-2", "partyId-3");
		assertThat(disturbanceEntityCaptorValue.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-2", "reference-3");
		assertThat(disturbanceEntityCaptorValue.getCategory()).isEqualTo(category.toString());
		assertThat(disturbanceEntityCaptorValue.getDisturbanceId()).isEqualTo(disturbanceId);
		assertThat(disturbanceEntityCaptorValue.getTitle()).isEqualTo(title);
		assertThat(disturbanceEntityCaptorValue.getDescription()).isEqualTo(description);
		assertThat(disturbanceEntityCaptorValue.getPlannedStartDate()).isEqualTo(plannedStartDate);
		assertThat(disturbanceEntityCaptorValue.getPlannedStopDate()).isEqualTo(plannedStopDate);
		assertThat(disturbanceEntityCaptorValue.getStatus()).isEqualTo(se.sundsvall.disturbance.api.model.Status.OPEN.toString());
	}

	@Test
	void updateDisturbanceChangeContent() throws ServiceException {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var existingTitle = "title";
		final var newTitle = "new title";
		final var existingDescription = "description";
		final var newDescription = "new description";
		final var plannedStartDate = LocalDateTime.of(2021, 10, 12, 18, 30, 0).atOffset(now().getOffset());
		final var existingPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now().getOffset());
		final var newPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now().getOffset());
		final var status = se.sundsvall.disturbance.api.model.Status.OPEN;

		// Parameters
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withTitle(newTitle)
			.withDescription(newDescription)
			.withPlannedStopDate(newPlannedStopDate)
			.withAffecteds(List.of(
				// partyId-4 added (compared to existing entity)
				Affected.create().withPartyId("partyId-1").withReference("reference-1"),
				Affected.create().withPartyId("partyId-2").withReference("reference-2"),
				Affected.create().withPartyId("partyId-3").withReference("reference-3"),
				Affected.create().withPartyId("partyId-4").withReference("reference-4")));

		final var e1 = new AffectedEntity();
		e1.setPartyId("partyId-1");
		e1.setReference("reference-1");

		final var e2 = new AffectedEntity();
		e2.setPartyId("partyId-2");
		e2.setReference("reference-2");

		final var e3 = new AffectedEntity();
		e3.setPartyId("partyId-3");
		e3.setReference("reference-3");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category.toString());
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(status.toString());
		existingDisturbanceEntity.setTitle(existingTitle);
		existingDisturbanceEntity.setDescription(existingDescription);
		existingDisturbanceEntity.setPlannedStartDate(plannedStartDate);
		existingDisturbanceEntity.setPlannedStopDate(existingPlannedStopDate);
		existingDisturbanceEntity.setAffectedEntities(new ArrayList<>(List.of(e1, e2, e3)));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.persistAndFetch(any(DisturbanceEntity.class))).thenReturn(existingDisturbanceEntity);

		final var updatedDisturbance = disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest);

		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendUpdateMessage(disturbanceEntityCaptor.capture());
		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verify(disturbanceRepositoryMock).persistAndFetch(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);
		verifyNoInteractions(feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);

		// Loop through the captor values (for sendMessageLogicMock and disturbanceRepositoryMock).
		disturbanceEntityCaptor.getAllValues().stream().forEach(updatedEntity -> {
			assertThat(updatedEntity).isNotNull();
			assertThat(updatedEntity.getAffectedEntities()).hasSize(4);
			assertThat(updatedEntity.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1", "partyId-2", "partyId-3", "partyId-4");
			assertThat(updatedEntity.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-1", "reference-2", "reference-3", "reference-4");
			assertThat(updatedEntity.getCategory()).isEqualTo(category.toString());
			assertThat(updatedEntity.getDisturbanceId()).isEqualTo(disturbanceId);
			assertThat(updatedEntity.getTitle()).isEqualTo(newTitle);
			assertThat(updatedEntity.getDescription()).isEqualTo(newDescription);
			assertThat(updatedEntity.getStatus()).isEqualTo(status.toString());
			assertThat(updatedEntity.getPlannedStartDate()).isEqualTo(plannedStartDate);
			assertThat(updatedEntity.getPlannedStopDate()).isEqualTo(newPlannedStopDate);
		});
	}

	@Test
	void updateDisturbanceWhenDisturbanceDoesntExist() throws ServiceException {

		// Parameters
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withStatus(se.sundsvall.disturbance.api.model.Status.CLOSED);

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(empty());

		final var serviceException = assertThrows(ServiceException.class, () -> disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest));

		assertThat(serviceException.getMessage()).isEqualTo("No disturbance found for category:'COMMUNICATION' and id:'12345'!");
		assertThat(serviceException.getStatus()).isEqualTo(Status.NOT_FOUND);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock, feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);
	}

	@Test
	void updateDisturbanceWhenStatusIsClosed() throws ServiceException {

		// Parameters
		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withDescription("Test");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category.toString());
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(se.sundsvall.disturbance.api.model.Status.CLOSED.toString());

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));

		final var serviceException = assertThrows(ServiceException.class, () -> disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest));

		assertThat(serviceException.getMessage())
			.isEqualTo("The disturbance with category:'COMMUNICATION' and id:'12345' is closed! No updates are allowed on closed disturbances!");
		assertThat(serviceException.getStatus()).isEqualTo(Status.CONFLICT);

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock, feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);
	}

	@Test
	void updateDisturbanceWhenStatusIsPlanned() throws ServiceException {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var existingTitle = "title";
		final var newTitle = "new title";
		final var existingDescription = "description";
		final var newDescription = "new description";
		final var plannedStartDate = LocalDateTime.of(2021, 10, 12, 18, 30, 0).atOffset(now().getOffset());
		final var existingPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now().getOffset());
		final var newPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now().getOffset());
		final var status = se.sundsvall.disturbance.api.model.Status.PLANNED;

		// Parameters
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withTitle(newTitle)
			.withDescription(newDescription)
			.withPlannedStopDate(newPlannedStopDate);

		final var e1 = new AffectedEntity();
		e1.setPartyId("partyId-1");
		e1.setReference("reference-1");

		final var e2 = new AffectedEntity();
		e2.setPartyId("partyId-2");
		e2.setReference("reference-2");

		final var e3 = new AffectedEntity();
		e3.setPartyId("partyId-3");
		e3.setReference("reference-3");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category.toString());
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(status.toString());
		existingDisturbanceEntity.setTitle(existingTitle);
		existingDisturbanceEntity.setDescription(existingDescription);
		existingDisturbanceEntity.setPlannedStartDate(plannedStartDate);
		existingDisturbanceEntity.setPlannedStopDate(existingPlannedStopDate);
		existingDisturbanceEntity.setAffectedEntities(new ArrayList<>(List.of(e1, e2, e3)));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.persistAndFetch(any(DisturbanceEntity.class))).thenReturn(existingDisturbanceEntity);

		final var updatedDisturbance = disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest);

		assertThat(updatedDisturbance).isNotNull();

		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verify(disturbanceRepositoryMock).persistAndFetch(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock);
		verifyNoInteractions(sendMessageLogicMock, feedbackRepositoryMock, disturbanceFeedbackRepositoryMock); // No messages sent if status is PLANNED.

		// Loop through the captor values (for sendMessageLogicMock and disturbanceRepositoryMock).
		disturbanceEntityCaptor.getAllValues().stream().forEach(updatedEntity -> {
			assertThat(updatedEntity).isNotNull();
			assertThat(updatedEntity.getAffectedEntities()).hasSize(3);
			assertThat(updatedEntity.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1", "partyId-2", "partyId-3");
			assertThat(updatedEntity.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-1", "reference-2", "reference-3");
			assertThat(updatedEntity.getCategory()).isEqualTo(category.toString());
			assertThat(updatedEntity.getDisturbanceId()).isEqualTo(disturbanceId);
			assertThat(updatedEntity.getTitle()).isEqualTo(newTitle);
			assertThat(updatedEntity.getDescription()).isEqualTo(newDescription);
			assertThat(updatedEntity.getStatus()).isEqualTo(status.toString());
			assertThat(updatedEntity.getPlannedStartDate()).isEqualTo(plannedStartDate);
			assertThat(updatedEntity.getPlannedStopDate()).isEqualTo(newPlannedStopDate);
		});
	}

	@Test
	void updateDisturbanceWhenStatusIsChangedFromPlannedToOpen() throws ServiceException {

		final var category = Category.COMMUNICATION;
		final var disturbanceId = "12345";
		final var existingTitle = "title";
		final var existingDescription = "description";
		final var plannedStartDate = LocalDateTime.of(2021, 10, 12, 18, 30, 0).atOffset(now().getOffset());
		final var existingPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now().getOffset());
		final var newPlannedStopDate = LocalDateTime.of(2021, 11, 10, 12, 0, 0).atOffset(now().getOffset());
		final var existingStatus = se.sundsvall.disturbance.api.model.Status.PLANNED;
		final var newStatus = se.sundsvall.disturbance.api.model.Status.OPEN;

		// Parameters
		final var disturbanceUpdateRequest = DisturbanceUpdateRequest.create()
			.withStatus(newStatus);

		final var e1 = new AffectedEntity();
		e1.setPartyId("partyId-1");
		e1.setReference("reference-1");

		final var e2 = new AffectedEntity();
		e2.setPartyId("partyId-2");
		e2.setReference("reference-2");

		final var e3 = new AffectedEntity();
		e3.setPartyId("partyId-3");
		e3.setReference("reference-3");

		final var existingDisturbanceEntity = new DisturbanceEntity();
		existingDisturbanceEntity.setCategory(category.toString());
		existingDisturbanceEntity.setDisturbanceId(disturbanceId);
		existingDisturbanceEntity.setStatus(existingStatus.toString());
		existingDisturbanceEntity.setTitle(existingTitle);
		existingDisturbanceEntity.setDescription(existingDescription);
		existingDisturbanceEntity.setPlannedStartDate(plannedStartDate);
		existingDisturbanceEntity.setPlannedStopDate(existingPlannedStopDate);
		existingDisturbanceEntity.setAffectedEntities(new ArrayList<>(List.of(e1, e2, e3)));

		when(disturbanceRepositoryMock.findByCategoryAndDisturbanceIdOptional(any(Category.class), any(String.class))).thenReturn(Optional.of(existingDisturbanceEntity));
		when(disturbanceRepositoryMock.persistAndFetch(any(DisturbanceEntity.class))).thenReturn(existingDisturbanceEntity);

		final var updatedDisturbance = disturbanceService.updateDisturbance(category, disturbanceId, disturbanceUpdateRequest);

		assertThat(updatedDisturbance).isNotNull();

		verify(sendMessageLogicMock).sendCreateMessage(disturbanceEntityCaptor.capture()); // New message is sent when status goes from PLANNED -> OPEN.
		verify(disturbanceRepositoryMock).findByCategoryAndDisturbanceIdOptional(category, disturbanceId);
		verify(disturbanceRepositoryMock).persistAndFetch(disturbanceEntityCaptor.capture());
		verifyNoMoreInteractions(disturbanceRepositoryMock, sendMessageLogicMock);
		verifyNoInteractions(feedbackRepositoryMock, disturbanceFeedbackRepositoryMock);

		// Loop through the captor values (for sendMessageLogicMock and disturbanceRepositoryMock).
		disturbanceEntityCaptor.getAllValues().stream().forEach(updatedEntity -> {
			assertThat(updatedEntity).isNotNull();
			assertThat(updatedEntity.getAffectedEntities()).hasSize(3);
			assertThat(updatedEntity.getAffectedEntities()).extracting(AffectedEntity::getPartyId).containsExactly("partyId-1", "partyId-2", "partyId-3");
			assertThat(updatedEntity.getAffectedEntities()).extracting(AffectedEntity::getReference).containsExactly("reference-1", "reference-2", "reference-3");
			assertThat(updatedEntity.getCategory()).isEqualTo(category.toString());
			assertThat(updatedEntity.getDisturbanceId()).isEqualTo(disturbanceId);
			assertThat(updatedEntity.getTitle()).isEqualTo(existingTitle);
			assertThat(updatedEntity.getDescription()).isEqualTo(existingDescription);
			assertThat(updatedEntity.getStatus()).isEqualTo(newStatus.toString());
			assertThat(updatedEntity.getPlannedStartDate()).isEqualTo(plannedStartDate);
			assertThat(updatedEntity.getPlannedStopDate()).isEqualTo(newPlannedStopDate);
		});
	}

	private List<DisturbanceEntity> createDisturbanceEntities() {
		final var disturbanceEntity1 = new DisturbanceEntity();
		disturbanceEntity1.setDisturbanceId("disturbanceId1");
		disturbanceEntity1.setCategory(Category.COMMUNICATION.toString());
		disturbanceEntity1.setStatus(se.sundsvall.disturbance.api.model.Status.OPEN.toString());

		final var disturbanceEntity2 = new DisturbanceEntity();
		disturbanceEntity2.setDisturbanceId("disturbanceId2");
		disturbanceEntity2.setCategory(Category.COMMUNICATION.toString());
		disturbanceEntity2.setStatus(se.sundsvall.disturbance.api.model.Status.OPEN.toString());

		return List.of(disturbanceEntity1, disturbanceEntity2);
	}
}
