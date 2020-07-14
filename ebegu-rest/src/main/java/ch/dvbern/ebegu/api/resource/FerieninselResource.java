/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */


package ch.dvbern.ebegu.api.resource;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxBelegungFerieninselTag;
import ch.dvbern.ebegu.api.dtos.JaxGemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.entities.BelegungFerieninselTag;
import ch.dvbern.ebegu.entities.GemeindeStammdatenGesuchsperiodeFerieninsel;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.services.FerieninselStammdatenService;
import ch.dvbern.ebegu.services.GesuchsperiodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


/**
 * REST Resource fuer FerieninselStammdaten
 */

@Path("ferieninselStammdaten")
@Stateless
@Api(description = "Resource fuer die Verwaltung von FerieninselStammdaten")
@PermitAll
public class FerieninselResource {


	@Inject
	private FerieninselStammdatenService ferieninselStammdatenService;

	@Inject
	private GesuchsperiodeService gesuchsperiodeService;

	@SuppressWarnings("CdiInjectionPointsInspection")
	@Inject
	private JaxBConverter converter;


	@ApiOperation(value = "Returns the FerieninselStammdaten for the Gesuchsperiode and Gemeinde with the specified "
		+ "ID for the given Ferien. The result also contains a "
		+ "list of potentially available dates of this Ferieninsel (time period minus weekends and holidays)",
		response = JaxGemeindeStammdatenGesuchsperiodeFerieninsel.class)
	@Nullable
	@GET
	@Path("/gesuchsperiode/{gesuchsperiodeId}/{gemeindeId}/{ferienname}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxGemeindeStammdatenGesuchsperiodeFerieninsel findFerieninselStammdatenForGesuchsperiodeAndFerienname(
		@Nonnull @NotNull @PathParam("gesuchsperiodeId") JaxId gesuchsperiodeId,
		@Nonnull @NotNull @PathParam("gemeindeId") JaxId gemeindeId,
		@Nonnull @NotNull @PathParam("ferienname") String feriennameParam) throws EbeguEntityNotFoundException {

		Objects.requireNonNull(gemeindeId.getId());
		Objects.requireNonNull(gesuchsperiodeId.getId());
		String gpEntityID = converter.toEntityId(gesuchsperiodeId);
		Ferienname ferienname = Ferienname.valueOf(feriennameParam);

		Gesuchsperiode gesuchsperiode = gesuchsperiodeService.findGesuchsperiode(gpEntityID).orElseThrow(()
			-> new EbeguRuntimeException("findFerieninselStammdatenForGesuchsperiodeAndFerienname", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gpEntityID));

		Optional<GemeindeStammdatenGesuchsperiodeFerieninsel> stammdatenOptional = ferieninselStammdatenService
			.findFerieninselStammdatenForGesuchsperiodeAndFerienname(gemeindeId.getId(), gesuchsperiode.getId(),
				ferienname);

		if (stammdatenOptional.isPresent()) {
			GemeindeStammdatenGesuchsperiodeFerieninsel stammdaten = stammdatenOptional.get();
			JaxGemeindeStammdatenGesuchsperiodeFerieninsel ferieninselStammdatenJAX = converter.ferieninselStammdatenToJAX(stammdaten);
			// Zur gefundenen Ferieninsel die tatsaechlich verfuegbaren Tage fuer die Belegung ermitteln (nur Wochentage, ohne Feiertage)
			List<BelegungFerieninselTag> possibleFerieninselTage = ferieninselStammdatenService.getPossibleFerieninselTage(stammdaten);
			List<JaxBelegungFerieninselTag> possibleFerieninselTageJAX = converter.belegungFerieninselTageListToJAX(possibleFerieninselTage);
			ferieninselStammdatenJAX.setPotenzielleFerieninselTageFuerBelegung(possibleFerieninselTageJAX);
			return ferieninselStammdatenJAX;
		}
		return null;
	}
}
