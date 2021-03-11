/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import ch.dvbern.ebegu.api.converter.JaxBWizardStepXConverter;
import ch.dvbern.ebegu.api.dtos.JaxWizardStepX;
import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.entities.gemeindeantrag.FerienbetreuungAngabenContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.gemeindeantrag.FerienbetreuungService;
import ch.dvbern.ebegu.services.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeService;
import ch.dvbern.ebegu.wizardx.Wizard;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.WizardTyp;
import ch.dvbern.ebegu.wizardx.ferienbetreuung.FerienbetreuungWizard;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.TagesschuleWizard;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("wizardstepX")
@Stateless
@Api(description = "Resource für den WizardStep")
@PermitAll // Alle Rollen dürfen WizardStep abfragen
public class WizardStepXResource {

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private JaxBWizardStepXConverter wizardStepXConverter;

	@Inject
	private LastenausgleichTagesschuleAngabenGemeindeService lastenausgleichTagesschuleAngabenGemeindeService;

	@Inject
	private FerienbetreuungService ferienbetreuungService;

	@ApiOperation(value = "Gibt den ersten Step.", response = JaxWizardStepX.class)
	@Nullable
	@GET
	@Path("/initFirstStep/{wizardtyp}/{id}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxWizardStepX initWizardStep(
		@Nonnull @NotNull @PathParam("wizardtyp") String wizardtyp,
		@Nonnull @PathParam("id") String id,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		Objects.requireNonNull(userRole);
		Objects.requireNonNull(id);

		Wizard wizard;
		switch (WizardTyp.valueOf(wizardtyp)) {
		case LASTENAUSGLEICH_TS:
			LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleAngabenGemeindeContainer =
				lastenausgleichTagesschuleAngabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(id)
				.orElseThrow(() -> new EbeguEntityNotFoundException("initWizardStep", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

			wizard = new TagesschuleWizard(userRole, lastenausgleichTagesschuleAngabenGemeindeContainer);
			break;
		case FERIENBETREUUNG:
			FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer =
				ferienbetreuungService.findFerienbetreuungAngabenContainer(id)
					.orElseThrow(() -> new EbeguEntityNotFoundException("initWizardStep", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

			wizard = new FerienbetreuungWizard(userRole, ferienbetreuungAngabenContainer);
			break;
		default:
			return null;
		}
		return wizardStepXConverter.convertStepToJax(wizard.getStep(), wizard);
	}

	@ApiOperation(value = "Gibt den nachfolgenden Step zurück.", response = JaxWizardStepX.class)
	@Nullable
	@GET
	@Path("/getNextStep/{wizardtyp}/{stepName}/{id}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxWizardStepX getNextStep(
		@Nonnull @NotNull @PathParam("wizardtyp") String wizardtyp,
		@Nonnull @NotNull @PathParam("stepName") String stepName,
		@Nonnull @PathParam("id") String id
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		Objects.requireNonNull(userRole);
		Objects.requireNonNull(id);

		Wizard wizard;

		switch (WizardTyp.valueOf(wizardtyp)) {
		case LASTENAUSGLEICH_TS:

			LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleAngabenGemeindeContainer =
				lastenausgleichTagesschuleAngabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(id)
					.orElseThrow(() -> new EbeguEntityNotFoundException("getNextStep", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

			wizard = new TagesschuleWizard(userRole, lastenausgleichTagesschuleAngabenGemeindeContainer);
			wizard.setStep(
				wizardStepXConverter.convertTagesschuleWizardStepJaxToStep(stepName)
			);
			break;

		case FERIENBETREUUNG:

			FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer =
				ferienbetreuungService.findFerienbetreuungAngabenContainer(id)
					.orElseThrow(() -> new EbeguEntityNotFoundException("getNextStep", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

			wizard = new FerienbetreuungWizard(userRole, ferienbetreuungAngabenContainer);
			wizard.setStep(
				wizardStepXConverter.convertFerienbetreuungWizardStepJaxToStep(stepName)
			);
			break;

		default:
			return null;
		}

		wizard.nextState();
		return wizardStepXConverter.convertStepToJax(wizard.getStep(), wizard);

	}

	@ApiOperation(value = "Gibt den vorherigen Step zurück.", response = JaxWizardStepX.class)
	@Nullable
	@GET
	@Path("/getPreviousStep/{wizardtyp}/{stepName}/{id}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxWizardStepX getPreviousStep(
		@Nonnull @NotNull @PathParam("wizardtyp") String wizardtyp,
		@Nonnull @NotNull @PathParam("stepName") String stepName,
		@Nonnull @PathParam("id") String id
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		Objects.requireNonNull(userRole);
		Objects.requireNonNull(id);

		Wizard wizard;

		switch (WizardTyp.valueOf(wizardtyp)) {
		case LASTENAUSGLEICH_TS:

			FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer =
				ferienbetreuungService.findFerienbetreuungAngabenContainer(id)
					.orElseThrow(() -> new EbeguEntityNotFoundException("getPreviousStep", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

			wizard = new FerienbetreuungWizard(userRole, ferienbetreuungAngabenContainer);
			wizard.setStep(wizardStepXConverter.convertTagesschuleWizardStepJaxToStep(stepName));
			break;

		case FERIENBETREUUNG:

			LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleAngabenGemeindeContainer =
				lastenausgleichTagesschuleAngabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(id)
					.orElseThrow(() -> new EbeguEntityNotFoundException("getPreviousStep", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

			wizard = new TagesschuleWizard(userRole, lastenausgleichTagesschuleAngabenGemeindeContainer);
			wizard.setStep(wizardStepXConverter.convertTagesschuleWizardStepJaxToStep(stepName));
			break;

		default:
			return null;
		}
		wizard.previousState();
		return wizardStepXConverter.convertStepToJax(wizard.getStep(), wizard);
	}

	@ApiOperation(value = "Gibt alle steps zurück.", response = JaxWizardStepX.class)
	@Nullable
	@GET
	@Path("/getAllSteps/{wizardtyp}/{id}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxWizardStepX> getAllWizardSteps(
		@Nonnull @NotNull @PathParam("wizardtyp") String wizardtyp,
		@Nonnull @PathParam("id") String id,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		Objects.requireNonNull(userRole);
		Objects.requireNonNull(id);

		Wizard wizard = null;
		List<JaxWizardStepX> jaxWizardStepXList = new ArrayList<>();
		switch (WizardTyp.valueOf(wizardtyp)) {
		case LASTENAUSGLEICH_TS:

			LastenausgleichTagesschuleAngabenGemeindeContainer lastenausgleichTagesschuleAngabenGemeindeContainer =
				lastenausgleichTagesschuleAngabenGemeindeService.findLastenausgleichTagesschuleAngabenGemeindeContainer(id)
					.orElseThrow(() -> new EbeguEntityNotFoundException("getAllWizardSteps", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

			wizard = new TagesschuleWizard(userRole, lastenausgleichTagesschuleAngabenGemeindeContainer);
			break;

		case FERIENBETREUUNG:

			FerienbetreuungAngabenContainer ferienbetreuungAngabenContainer =
				ferienbetreuungService.findFerienbetreuungAngabenContainer(id)
					.orElseThrow(() -> new EbeguEntityNotFoundException("getAllWizardSteps", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND));

			wizard = new FerienbetreuungWizard(userRole, ferienbetreuungAngabenContainer);
			break;
		default:
			return jaxWizardStepXList;
		}
		WizardStep futurPreviousStep = wizard.getStep();
		jaxWizardStepXList.add(wizardStepXConverter.convertStepToJax(futurPreviousStep, wizard));
		wizard.nextState();
		while (!wizard.getStep().getWizardStepName().equals(futurPreviousStep.getWizardStepName())) {
			futurPreviousStep = wizard.getStep();
			jaxWizardStepXList.add(wizardStepXConverter.convertStepToJax(futurPreviousStep, wizard));
			wizard.nextState();
		}

		return jaxWizardStepXList;
	}
}
