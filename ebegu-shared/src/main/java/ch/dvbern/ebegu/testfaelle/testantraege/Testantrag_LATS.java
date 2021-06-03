/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.testfaelle.testantraege;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;

public class Testantrag_LATS {

	private static final long serialVersionUID = -5434973108213523011L;
	private final LastenausgleichTagesschuleAngabenGemeindeContainer container;

	public Testantrag_LATS(
		Gemeinde gemeinde,
		Gesuchsperiode gesuchsperiode,
		Collection<InstitutionStammdaten> allTagesschulenForGesuchsperiodeAndGemeinde,
		LastenausgleichTagesschuleAngabenGemeindeStatus status) {
		this.container = new LastenausgleichTagesschuleAngabenGemeindeContainer();
		this.container.setGemeinde(gemeinde);
		this.container.setGesuchsperiode(gesuchsperiode);
		// First set it to IN_BEARBEITUNG_GEMEINDE so we can calculate the deklaration correctly
		this.container.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE);
		this.container.setAlleAngabenInKibonErfasst(false);

		allTagesschulenForGesuchsperiodeAndGemeinde.forEach(institutionStammdaten -> this.container.getAngabenInstitutionContainers()
			.add((new Testantrag_LastenausgleichTagesschuleAngabenInstitutionContainer(
				this.container,
				status,
				institutionStammdaten.getInstitution())).getContainer()));

		BigDecimal institutionsBetreuungsstundenSum = this.container.getAngabenInstitutionContainers()
			.stream()
			.reduce(
				BigDecimal.ZERO,
				(partialResult, instiContainer) -> partialResult.add(instiContainer.isAntragAtLeastInPruefungGemeinde() ?
					Objects.requireNonNull(instiContainer.getAngabenKorrektur())
						.getBetreuungsstundenEinschliesslichBesondereBeduerfnisse() :
					Objects.requireNonNull(instiContainer.getAngabenDeklaration())
						.getBetreuungsstundenEinschliesslichBesondereBeduerfnisse()), BigDecimal::add);

		this.container.setAngabenDeklaration(
			(new Testantrag_LastenausgleichTagesschuleAngabenGemeinde(institutionsBetreuungsstundenSum, status)).getAngaben()
		);
		if(status == LastenausgleichTagesschuleAngabenGemeindeStatus.IN_PRUEFUNG_KANTON) {
			this.container.copyForFreigabe();
		}
		// now set correct state
		this.container.setStatus(status);
	}

	public LastenausgleichTagesschuleAngabenGemeindeContainer getContainer() {
		return container;
	}
}
