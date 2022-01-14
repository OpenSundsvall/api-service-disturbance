package se.sundsvall.disturbance.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.created;
import static javax.ws.rs.core.Response.noContent;
import static javax.ws.rs.core.Response.ok;
import static org.eclipse.microprofile.openapi.annotations.enums.SchemaType.ARRAY;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.disturbance.api.exception.ServiceException;
import se.sundsvall.disturbance.api.exception.model.ServiceErrorResponse;
import se.sundsvall.disturbance.api.model.Category;
import se.sundsvall.disturbance.api.model.Disturbance;
import se.sundsvall.disturbance.api.model.DisturbanceCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceFeedbackCreateRequest;
import se.sundsvall.disturbance.api.model.DisturbanceUpdateRequest;
import se.sundsvall.disturbance.api.model.Status;
import se.sundsvall.disturbance.api.validation.ValidUuid;
import se.sundsvall.disturbance.service.DisturbanceFeedbackService;
import se.sundsvall.disturbance.service.DisturbanceService;

@Path("/disturbances")
@Tag(name = "Disturbance", description = "Disturbance operations")
public class DisturbanceResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(DisturbanceResource.class);

	@Inject
	DisturbanceService disturbanceService;

	@Inject
	DisturbanceFeedbackService disturbanceFeedbackService;

	@Context
	UriInfo uriInfo;

	@POST
	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	@Operation(summary = "Create a new disturbance.")
	@APIResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = Disturbance.class)))
	@APIResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "500", description = "Internal Server error", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	public Response createDisturbance(
		@RequestBody(required = true, content = @Content(schema = @Schema(implementation = DisturbanceCreateRequest.class))) @NotNull @Valid DisturbanceCreateRequest body)
		throws ServiceException {
		LOGGER.debug("Received createDisturbance request: body='{}'", body);

		// URI to the created resource.
		final var locationUri = uriInfo.getAbsolutePathBuilder()
			.path("/{category}/{disturbanceId}")
			.buildFromMap(Map.of("category", body.getCategory(), "disturbanceId", body.getId()));

		return created(locationUri).entity(disturbanceService.createDisturbance(body)).build();
	}

	@GET
	@Path("/{category}/{disturbanceId}")
	@Produces(APPLICATION_JSON)
	@Operation(summary = "Returns information about a specific disturbance.")
	@APIResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = Disturbance.class)))
	@APIResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "500", description = "Internal Server error", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	public Response getDisturbance(
		@Parameter(name = "category", description = "Disturbance category", required = true) @NotNull @PathParam("category") Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @NotBlank @PathParam("disturbanceId") String disturbanceId)
		throws ServiceException {
		LOGGER.debug("Received getDisturbance request: category='{}'. disturbanceId='{}'", category, disturbanceId);

		return ok().entity(disturbanceService.findByCategoryAndDisturbanceId(category, disturbanceId)).build();
	}

	@GET
	@Path("/affecteds/{partyId}")
	@Produces(APPLICATION_JSON)
	@Operation(summary = "Returns all present disturbances for a person or an organization.")
	@APIResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(type = ARRAY, implementation = Disturbance.class)))
	@APIResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "500", description = "Internal Server error", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	public Response getDisturbancesByPartyId(
		@Parameter(name = "partyId", description = "PartyId (e.g. a personId or an organizationId)", required = true, example = "81471222-5798-11e9-ae24-57fa13b361e1") @ValidUuid @PathParam("partyId") String partyId,
		@Parameter(name = "status", description = "Status filter parameter", required = false) @QueryParam("status") List<Status> status,
		@Parameter(name = "category", description = "Category filter parameter", required = false) @QueryParam("category") List<Category> category) {
		LOGGER.debug("Received getDisturbancesByPartyId request: partyId='{}', status='{}', category='{}'", partyId, status, category);

		return ok().entity(disturbanceService.findByPartyIdAndCategoryAndStatus(partyId, category, status)).build();
	}

	@PATCH
	@Path("/{category}/{disturbanceId}")
	@Consumes(APPLICATION_JSON)
	@Produces(APPLICATION_JSON)
	@Operation(summary = "Manage updates of a disturbance. Should be used when the set of affected persons/organizations is changed or the disturbance description is updated.")
	@APIResponse(responseCode = "200", description = "Successful operation", content = @Content(schema = @Schema(implementation = Disturbance.class)))
	@APIResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "409", description = "Conflict", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "500", description = "Internal Server error", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	public Response updateDisturbance(
		@Parameter(name = "category", description = "Disturbance category", required = true) @NotNull @PathParam("category") Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @NotBlank @PathParam("disturbanceId") String disturbanceId,
		@RequestBody(required = true, content = @Content(schema = @Schema(implementation = DisturbanceUpdateRequest.class))) @NotNull @Valid DisturbanceUpdateRequest body)
		throws ServiceException {
		LOGGER.debug("Received updateDisturbance request: category='{}', disturbanceId='{}', body='{}'", category, disturbanceId, body);

		return ok().entity(disturbanceService.updateDisturbance(category, disturbanceId, body)).build();
	}

	@DELETE
	@Path("/{category}/{disturbanceId}")
	@Operation(summary = "Deletes a disturbance. Should be used when the disturbance is resolved. Any affected persons/organizations (with ordered feedback) will be notified of the resolved disturbance.")
	@APIResponse(responseCode = "204", description = "Successful operation")
	@APIResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "500", description = "Internal Server error", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	public Response deleteDisturbance(
		@Parameter(name = "category", description = "Disturbance category", required = true) @NotNull @PathParam("category") Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @NotBlank @PathParam("disturbanceId") String disturbanceId)
		throws ServiceException {
		LOGGER.debug("Received deleteDisturbance request: category='{}', disturbanceId='{}'", category, disturbanceId);

		disturbanceService.deleteDisturbance(category, disturbanceId);

		return noContent().build();
	}

	@POST
	@Path("/{category}/{disturbanceId}/feedback")
	@Consumes(APPLICATION_JSON)
	@Operation(summary = "Create disturbance feedback for a person/organization. I.e. subscribe on notifications when the disturbance is updated or resolved.")
	@APIResponse(responseCode = "204", description = "Successful operation")
	@APIResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	@APIResponse(responseCode = "500", description = "Internal Server error", content = @Content(schema = @Schema(implementation = ServiceErrorResponse.class)))
	public Response createDisturbanceFeedback(
		@Parameter(name = "category", description = "Disturbance category", required = true) @NotNull @PathParam("category") Category category,
		@Parameter(name = "disturbanceId", description = "Disturbance ID", required = true, example = "435553") @NotBlank @PathParam("disturbanceId") String disturbanceId,
		@RequestBody(required = true, content = @Content(schema = @Schema(implementation = DisturbanceFeedbackCreateRequest.class))) @NotNull @Valid DisturbanceFeedbackCreateRequest body)
		throws ServiceException {
		LOGGER.debug("Received createDisturbanceFeedback request: category='{}', disturbanceId='{}', body='{}'", category, disturbanceId, body);

		disturbanceFeedbackService.createDisturbanceFeedback(category, disturbanceId, body);

		return noContent().build();
	}
}
