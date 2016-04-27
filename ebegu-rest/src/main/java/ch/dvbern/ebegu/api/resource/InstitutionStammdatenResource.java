package ch.dvbern.ebegu.api.resource;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxInstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.errors.EbeguException;
import ch.dvbern.ebegu.services.InstitutionStammdatenService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Resource fuer InstitutionStammdaten
 */
@Path("institutionstammdaten")
@Stateless
@Api
public class InstitutionStammdatenResource {

	@Inject
	private InstitutionStammdatenService institutionStammdatenService;

	@Inject
	private JaxBConverter converter;


	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxInstitutionStammdaten saveInstitutionStammdaten(
		@Nonnull @NotNull @Valid JaxInstitutionStammdaten institutionStammdatenJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) throws EbeguException {

		InstitutionStammdaten institutionStammdaten;
		if (institutionStammdatenJAXP.getId() != null) {
			Optional<InstitutionStammdaten> optional = institutionStammdatenService.findInstitutionStammdaten(converter.toEntityId(institutionStammdatenJAXP.getId()));
			institutionStammdaten = optional.isPresent() ? optional.get() : new InstitutionStammdaten();
		} else {
			institutionStammdaten = new InstitutionStammdaten();
		}
		InstitutionStammdaten convertedInstitutionStammdaten = converter.institutionStammdatenToEntity(institutionStammdatenJAXP, institutionStammdaten);

		InstitutionStammdaten persistedInstitutionStammdaten = institutionStammdatenService.saveInstitutionStammdaten(convertedInstitutionStammdaten);

		return converter.institutionStammdatenToJAX(persistedInstitutionStammdaten);

	}

	@Nullable
	@GET
	@Path("/{institutionStammdatenId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxInstitutionStammdaten findInstitutionStammdaten(
		@Nonnull @NotNull @PathParam("institutionStammdatenId") JaxId institutionStammdatenJAXPId) throws EbeguException {

		Validate.notNull(institutionStammdatenJAXPId.getId());
		String institutionStammdatenID = converter.toEntityId(institutionStammdatenJAXPId);
		Optional<InstitutionStammdaten> optional = institutionStammdatenService.findInstitutionStammdaten(institutionStammdatenID);

		if (!optional.isPresent()) {
			return null;
		}
		return converter.institutionStammdatenToJAX(optional.get());
	}

	@Nonnull
	@GET
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitutionStammdaten> getAllInstitutionStammdaten() {
		return institutionStammdatenService.getAllInstitutionStammdaten().stream()
			.map(instStammdaten -> converter.institutionStammdatenToJAX(instStammdaten))
			.collect(Collectors.toList());
	}

	@Nullable
	@DELETE
	@Path("/{institutionStammdatenId}")
	@Consumes(MediaType.WILDCARD)
	public Response removeInstitutionStammdaten(
		@Nonnull @NotNull @PathParam("institutionStammdatenId") JaxId institutionStammdatenJAXPId,
		@Context HttpServletResponse response) {

		Validate.notNull(institutionStammdatenJAXPId.getId());
		institutionStammdatenService.removeInstitutionStammdaten(converter.toEntityId(institutionStammdatenJAXPId));
		return Response.ok().build();
	}

	/**
	 * Sucht in der DB alle InstitutionStammdaten, bei welchen das gegebene Datum zwischen DatumVon und DatumBis liegt
	 * Wenn das Datum null ist, wird dieses automatisch als heutiges Datum gesetzt.
	 *
	 * @param stringDate Date als String mit Format "dd-MM-yyyy". Wenn null, ehutiges Datum gesetzt
	 * @return Liste mit allen InstitutionStammdaten die den Bedingungen folgen
     */
	@Nonnull
	@GET
	@Path("/date")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxInstitutionStammdaten> getAllInstitutionStammdatenByDate(
		@Nullable @QueryParam("date") String stringDate
		) {

		LocalDate date = LocalDate.now();
		if(stringDate != null && !stringDate.isEmpty()) {
			date = LocalDate.parse(stringDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		}
		return institutionStammdatenService.getAllInstitutionStammdatenByDate(date).stream()
			.map(institutionStammdaten -> converter.institutionStammdatenToJAX(institutionStammdaten))
			.collect(Collectors.toList());
	}

}
