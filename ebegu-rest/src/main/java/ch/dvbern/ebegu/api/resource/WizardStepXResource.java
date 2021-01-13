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
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.wizardx.Wizard;
import ch.dvbern.ebegu.wizardx.WizardTyp;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.TagesschuleWizard;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("wizardstepX")
@Stateless
@Api(description = "Resource fuer Sozialhilfe Zeitraeume")
@PermitAll // Rollen noch nicht festgestellt Proof of Concept phase
public class WizardStepXResource {

	@Inject
	private PrincipalBean principalBean;

	@Inject
	private JaxBWizardStepXConverter wizardStepXConverter;

	@ApiOperation(value = "Gibt den ersten Step.", response = JaxWizardStepX.class)
	@Nullable
	@GET
	@Path("/initFirstStep/{wizardtyp}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxWizardStepX initWizardStep(
		@Nonnull @NotNull @PathParam("wizardtyp") String wizardtyp,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		assert userRole != null;
		switch (WizardTyp.valueOf(wizardtyp)) {
		case LASTENAUSGLEICH_TS:
			TagesschuleWizard tagesschuleWizard = new TagesschuleWizard(userRole);
			return wizardStepXConverter.convertStepToJax(tagesschuleWizard.getStep());
		default:
			break;
		}
		return null;
	}

	@ApiOperation(value = "Gibt den ersten Step.", response = JaxWizardStepX.class)
	@Nullable
	@GET
	@Path("/getNextStep/{wizardtyp}/{stepName}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxWizardStepX getNextStep(
		@Nonnull @NotNull @PathParam("wizardtyp") String wizardtyp,
		@Nonnull @NotNull @PathParam("stepName") String stepName
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		assert userRole != null;
		switch (WizardTyp.valueOf(wizardtyp)) {
		case LASTENAUSGLEICH_TS:
			TagesschuleWizard tagesschuleWizard = new TagesschuleWizard(userRole);
			tagesschuleWizard.setStep(wizardStepXConverter.convertTagesschuleWizardStepJaxToSTep(stepName));
			tagesschuleWizard.nextState();
			return wizardStepXConverter.convertStepToJax(tagesschuleWizard.getStep());
		default:
			break;
		}
		return null;
	}

	@ApiOperation(value = "Gibt den ersten Step.", response = JaxWizardStepX.class)
	@Nullable
	@GET
	@Path("/getPreviousStep/{wizardtyp}/{stepName}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxWizardStepX getPreviousStep(
		@Nonnull @NotNull @PathParam("wizardtyp") String wizardtyp,
		@Nonnull @NotNull @PathParam("stepName") String stepName
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		assert userRole != null;
		switch (WizardTyp.valueOf(wizardtyp)) {
		case LASTENAUSGLEICH_TS:
			TagesschuleWizard tagesschuleWizard = new TagesschuleWizard(userRole);
			tagesschuleWizard.setStep(wizardStepXConverter.convertTagesschuleWizardStepJaxToSTep(stepName));
			tagesschuleWizard.previousState();
			return wizardStepXConverter.convertStepToJax(tagesschuleWizard.getStep());
		default:
			break;
		}
		return null;
	}

	@ApiOperation(value = "Gibt den ersten Step.", response = JaxWizardStepX.class)
	@Nullable
	@GET
	@Path("/getAllSteps/{wizardtyp}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxWizardStepX> getAllWizardSteps(
		@Nonnull @NotNull @PathParam("wizardtyp") String wizardtyp,
		@Context UriInfo uriInfo,
		@Context HttpServletResponse response
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		assert userRole != null;
		Wizard wizard = null;
		List<JaxWizardStepX> jaxWizardStepXList = new ArrayList<>();
		switch (WizardTyp.valueOf(wizardtyp)) {
		case LASTENAUSGLEICH_TS:
			wizard = new TagesschuleWizard(userRole);
			break;
		default:
			return jaxWizardStepXList;
		}
		WizardStep futurPreviousStep = wizard.getStep();
		jaxWizardStepXList.add(wizardStepXConverter.convertStepToJax(futurPreviousStep));
		wizard.nextState();
		while (!wizard.getStep().getWizardStepName().equals(futurPreviousStep.getWizardStepName())) {
			futurPreviousStep = wizard.getStep();
			jaxWizardStepXList.add(wizardStepXConverter.convertStepToJax(futurPreviousStep));
			wizard.nextState();
		}

		return jaxWizardStepXList;
	}
}
