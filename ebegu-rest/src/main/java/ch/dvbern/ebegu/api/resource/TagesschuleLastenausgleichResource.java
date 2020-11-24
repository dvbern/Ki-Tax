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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.DenyAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.AngabenGemeinde;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.Angaben;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.Lastenausgleich;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.TagesschuleWizard;
import ch.dvbern.ebegu.wizardx.WizardStep;
import ch.dvbern.ebegu.wizardx.tagesschuleLastenausgleich.TagesschuleWizardStepsEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Path("tagesschuleLastenausgleich")
@Stateless
@Api(description = "Resource fuer Sozialhilfe Zeitraeume")
@DenyAll // Absichtlich keine Rolle zugelassen, erzwingt, dass es für neue Methoden definiert werden muss
public class TagesschuleLastenausgleichResource {

	@Inject
	private PrincipalBean principalBean;

	@ApiOperation(value = "Gibt den ersten Step.", response = String.class)
	@Nullable
	@GET
	@Path("/initFirstStep")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public String initTagesschuleLastenausgleich() {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		assert userRole != null;
		TagesschuleWizard tagesschuleWizard = new TagesschuleWizard(userRole);
		return convertStepToJax(tagesschuleWizard.getStep());
	}

	@ApiOperation(value = "Gibt den ersten Step.", response = String.class)
	@Nullable
	@GET
	@Path("/getNextStep/{stepName}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public String getNextStep(
		@Nonnull @NotNull @PathParam("stepName") String stepName
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		assert userRole != null;
		TagesschuleWizard tagesschuleWizard = new TagesschuleWizard(userRole);
		return convertStepToJax(tagesschuleWizard.getStep());
	}

	@ApiOperation(value = "Gibt den ersten Step.", response = String.class)
	@Nullable
	@GET
	@Path("/getPreviousStep/{stepName}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public String getPreviousStep(
		@Nonnull @NotNull @PathParam("stepName") String stepName
	) {
		UserRole userRole = principalBean.discoverMostPrivilegedRole();
		assert userRole != null;
		TagesschuleWizard tagesschuleWizard = new TagesschuleWizard(userRole);
		return convertStepToJax(tagesschuleWizard.getStep());
	}


	private String convertStepToJax(WizardStep step) {
		return step.getTagesschuleWizardStepName().toString();
	}


	private WizardStep convertJaxToSTep(String step) {
		switch (TagesschuleWizardStepsEnum.valueOf(step)) {
		case ANGABEN_GEMEINDE:
			return new AngabenGemeinde();
		case ANGABEN_TAGESSCHULE:
			return new Angaben();
		case LASTENAUSGLEICH:
			return new Lastenausgleich();
		}
		return null;
	}
}
