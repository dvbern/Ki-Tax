package ch.dvbern.ebegu.api.resource;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxPerson;
import ch.dvbern.ebegu.entities.Person;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguException;
import ch.dvbern.ebegu.services.PersonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import javax.ws.rs.core.UriInfo;
import java.util.Optional;

/**
 * REST Resource fuer Personen
 */
@Path("personen")
@Stateless
@Api
public class PersonResource {

	@Inject
	private PersonService personService;

	@Inject
	private JaxBConverter converter;


	@ApiOperation(value = "Create a new Person in the database. The transfer object also has a relation to adressen " +
		"(wohnadresse, umzugadresse, korrespondenzadresse) these are stored in the database as well. Note that wohnadresse and" +
		"umzugadresse are both stored as consecutive wohnadressen in the database")
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxPerson create(
		@Nonnull @NotNull @Valid JaxPerson personJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) {

		Person convertedPerson = converter.personToEntity(personJAXP, new Person());
		Person persistedPerson = this.personService.updatePerson(convertedPerson); //immer update

		return converter.personToJAX(persistedPerson);
	}

	@Nullable
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxPerson update(
		@Nonnull @NotNull @Valid JaxPerson personJAXP,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response) throws EbeguException {

		Validate.notNull(personJAXP.getId());
		String personID = converter.toEntityId(personJAXP);
		Optional<Person> optional = personService.findPerson(personID);
		Person personFromDB = optional.orElseThrow(() -> new EbeguEntityNotFoundException("update", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, personJAXP.getId().toString()));
		Person personToMerge = converter.personToEntity(personJAXP, personFromDB);

		Person modifiedPerson = this.personService.updatePerson(personToMerge);
		return converter.personToJAX(modifiedPerson);

	}


	@Nullable
	@GET
	@Path("/{personId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxPerson findPerson(
		@Nonnull @NotNull JaxId personJAXPId) throws EbeguException {

		Validate.notNull(personJAXPId.getId());
		String personID = converter.toEntityId(personJAXPId);
		Optional<Person> optional = personService.findPerson(personID);

		if (!optional.isPresent()) {
			return null;
		}
		Person personToReturn = optional.get();

		return converter.personToJAX(personToReturn);
	}

}