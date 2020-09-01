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

package ch.dvbern.ebegu.services;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.Valid;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.UnbezahlterUrlaub;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer {@link ErwerbspensumContainer} diese beinhalten einzelne Objekte mit den Daten von GS und JA
 */
@Stateless
@Local(ErwerbspensumService.class)
public class ErwerbspensumServiceBean extends AbstractBaseService implements ErwerbspensumService {

	@Inject
	private Persistence persistence;

	@Inject
	private WizardStepService wizardStepService;

	@Inject
	private Authorizer authorizer;

	@Nonnull
	@Override
	public ErwerbspensumContainer saveErwerbspensum(
		@Valid @Nonnull ErwerbspensumContainer erwerbspensumContainer, @Nonnull Gesuch gesuch
	) {
		Objects.requireNonNull(erwerbspensumContainer);
		authorizer.checkWriteAuthorization(erwerbspensumContainer);

		final ErwerbspensumContainer mergedErwerbspensum = persistence.merge(erwerbspensumContainer);
		mergedErwerbspensum.getGesuchsteller().addErwerbspensumContainer(mergedErwerbspensum);
		wizardStepService.updateSteps(
			gesuch.getId(),
			null,
			mergedErwerbspensum.getErwerbspensumJA(),
			WizardStepName.ERWERBSPENSUM);
		return mergedErwerbspensum;
	}

	@Nonnull
	@Override
	public Optional<ErwerbspensumContainer> findErwerbspensum(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		ErwerbspensumContainer ewpCnt = persistence.find(ErwerbspensumContainer.class, key);
		authorizer.checkReadAuthorization(ewpCnt);
		return Optional.ofNullable(ewpCnt);
	}

	@Override
	public void removeErwerbspensum(@Nonnull String erwerbspensumContainerID, Gesuch gesuch) {
		Objects.requireNonNull(erwerbspensumContainerID);
		authorizer.checkWriteAuthorization(gesuch);

		ErwerbspensumContainer ewpCont = this.findErwerbspensum(erwerbspensumContainerID).orElseThrow(
			() -> new EbeguEntityNotFoundException(
				"removeErwerbspensum",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				erwerbspensumContainerID)
		);
		authorizer.checkWriteAuthorization(ewpCont);
		GesuchstellerContainer gesuchsteller = ewpCont.getGesuchsteller();
		persistence.remove(ewpCont);

		// the kind needs to be removed from the object as well
		gesuchsteller.getErwerbspensenContainersNotEmpty()
			.removeIf(k -> k.getId().equalsIgnoreCase(erwerbspensumContainerID));
		wizardStepService.updateSteps(gesuch.getId(), null, null, WizardStepName.ERWERBSPENSUM);
	}

	@Override
	public boolean isErwerbspensumRequired(@Nonnull Gesuch gesuch) {
		authorizer.checkReadAuthorization(gesuch);
		return gesuch.extractAllBetreuungen().stream()
			.anyMatch(this::isErwerbspensumRequired);
	}

	@Nonnull
	@Override
	public Optional<UnbezahlterUrlaub> findUnbezahlterUrlaub(@Nonnull String unbezahlterUrlaubId) {
		final UnbezahlterUrlaub unbezahlterUrlaub = persistence.find(UnbezahlterUrlaub.class, unbezahlterUrlaubId);
		return Optional.ofNullable(unbezahlterUrlaub);
	}

	private boolean isErwerbspensumRequired(@Nonnull Betreuung betreuung) {
		// Erwerbspensum ist zwingend, wenn:
		// das Kind keine Fachstelle zugewiesen hat
		// UND das Angebot ein JugendamtKleinkind (Kita / TFO)
		// Und das Kind kein Auserordentlicher Anspruch hat
		return betreuung.getKind().getKindJA().getPensumFachstelle() == null
			&& betreuung.getKind().getKindJA().getPensumAusserordentlicherAnspruch() == null
			&& betreuung.getInstitutionStammdaten().getBetreuungsangebotTyp().isAngebotJugendamtKleinkind();
	}
}
