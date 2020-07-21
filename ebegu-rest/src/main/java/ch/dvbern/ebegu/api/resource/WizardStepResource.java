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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.dvbern.ebegu.api.converter.JaxBConverter;
import ch.dvbern.ebegu.api.dtos.JaxId;
import ch.dvbern.ebegu.api.dtos.JaxWizardStep;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.WizardStep;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.services.GesuchService;
import ch.dvbern.ebegu.services.WizardStepService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Resource fuer Wizardsteps
 */
@Path("wizard-steps")
@Stateless
@Api(description = "Resource für die Darstellung des Antrags-Wizards")
@PermitAll // Grundsaetzliche fuer alle Rollen: Datenabhaengig. -> Authorizer
public class WizardStepResource {

	@Inject
	private GesuchService gesuchService;
	@Inject
	private WizardStepService wizardStepService;
	@Inject
	private JaxBConverter converter;

	@ApiOperation(value = "Gibt alle Wizardsteps des Gesuchs mit der uebergebenen id zurueck",
		responseContainer = "Collection", response = JaxWizardStep.class)
	@GET
	@Path("/{gesuchId}")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<JaxWizardStep> findWizardStepsFromGesuch(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId) {
		Objects.requireNonNull(gesuchJAXPId.getId());

		final String gesuchID = converter.toEntityId(gesuchJAXPId);

		return wizardStepService.findWizardStepsFromGesuch(gesuchID).stream()
			.map(wizardStep -> converter.wizardStepToJAX(wizardStep)).collect(Collectors.toList());
	}

	/**
	 * Creates all required WizardSteps for the given Gesuch and returns them as a List. Status for all Steps will be UNBESUCHT except for
	 * GESUCH_ERSTELLEN, which gets OK, because this step is already done when the gesuch is created.
	 */
	@ApiOperation(value = "Creates all required WizardSteps for the given Gesuch and returns them as a List. Status " +
		"for all Steps will be UNBESUCHT except for GESUCH_ERSTELLEN, which gets OK, because this step is already " +
		"done when the gesuch is created.",
		responseContainer = "List", response = JaxWizardStep.class)
	@Nullable
	@GET
	@Path("/createWizardSteps/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JaxWizardStep> createWizardStepList(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId gesuchJAXPId) {
		Objects.requireNonNull(gesuchJAXPId.getId());

		Optional<Gesuch> gesuch = gesuchService.findGesuch(gesuchJAXPId.getId());
		if (gesuch.isPresent()) {
			return wizardStepService.createWizardStepList(gesuch.get()).stream()
				.map(wizardStep -> converter.wizardStepToJAX(wizardStep)).collect(Collectors.toList());
		}
		return null;
	}

	@ApiOperation(value = "Speichert den uebergebenen Wizardstep", response = JaxWizardStep.class)
	@Nullable
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxWizardStep saveWizardStep(
		@Nonnull @NotNull @Valid JaxWizardStep wizardStepJAXP) {

		Optional<Gesuch> gesuch = gesuchService.findGesuch(wizardStepJAXP.getGesuchId());
		if (gesuch.isPresent()) {
			WizardStep wizardStepToMerge = new WizardStep();
			if (wizardStepJAXP.getId() != null) {
				Optional<WizardStep> optional = wizardStepService.findWizardStep(wizardStepJAXP.getId());
				wizardStepToMerge = optional.orElse(new WizardStep());
			}
			WizardStep convertedWizardStep = converter.wizardStepToEntity(wizardStepJAXP, wizardStepToMerge);
			convertedWizardStep.setGesuch(gesuch.get());
			WizardStep persistedWizardStep = this.wizardStepService.saveWizardStep(convertedWizardStep);
			return converter.wizardStepToJAX(persistedWizardStep);
		}
		throw new EbeguEntityNotFoundException("saveWizardStep", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId invalid: " + wizardStepJAXP.getGesuchId());
	}

	@ApiOperation(value = "Setzt den Status des uebergebenen Wizardsteps auf MUTIERT", response = JaxWizardStep.class)
	@Nullable
	@POST
	@Path("/setWizardStepMutiert/{gesuchId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public JaxWizardStep setWizardStepMutiert(
		@Nonnull @NotNull @PathParam("gesuchId") JaxId wizardStepJAXPId) {

		Optional<WizardStep> optional = wizardStepService.findWizardStep(wizardStepJAXPId.getId());
		final WizardStep wizardStep = optional.orElse(new WizardStep());

		this.wizardStepService.setWizardStepOkOrMutiert(wizardStep);
		WizardStep persistedWizardStep = this.wizardStepService.saveWizardStep(wizardStep);
		return converter.wizardStepToJAX(persistedWizardStep);
	}
}
