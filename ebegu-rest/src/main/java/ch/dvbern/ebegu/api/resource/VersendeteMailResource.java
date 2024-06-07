package ch.dvbern.ebegu.api.resource;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxVersendeteMail;
import ch.dvbern.ebegu.services.VersendeteMailsService;

import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

@Path("versendeteMails")
public class VersendeteMailResource {
	@Inject
	private VersendeteMailsService versendeteMailsService;

	@Inject
	private JaxBConverter converter;

	@Nonnull
	@GET
	@Path("/allMails")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed(SUPER_ADMIN)
	public List<JaxVersendeteMail> getAllMails() {
		return versendeteMailsService.getAll().stream()
			.map(versendeteMails -> converter.versendeteMailsToJax(versendeteMails))
			.collect(Collectors.toList());
	}
}
