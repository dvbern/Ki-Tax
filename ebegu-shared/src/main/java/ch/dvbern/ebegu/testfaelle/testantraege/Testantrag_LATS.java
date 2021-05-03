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
import java.util.HashSet;
import java.util.Objects;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeStatus;

public class Testantrag_LATS extends LastenausgleichTagesschuleAngabenGemeindeContainer {

	private static final long serialVersionUID = -5434973108213523011L;

	public Testantrag_LATS(
		Gemeinde gemeinde,
		Gesuchsperiode gesuchsperiode,
		Collection<InstitutionStammdaten> allTagesschulenForGesuchsperiodeAndGemeinde) {
		this.setGemeinde(gemeinde);
		this.setGesuchsperiode(gesuchsperiode);

		this.setStatus(LastenausgleichTagesschuleAngabenGemeindeStatus.IN_BEARBEITUNG_GEMEINDE);
		this.setAlleAngabenInKibonErfasst(false);

		allTagesschulenForGesuchsperiodeAndGemeinde.forEach(institutionStammdaten -> {
			this.getAngabenInstitutionContainers()
				.add(new Testantrag_LastenausgleichTagesschuleAngabenInstitutionContainer(
					this,
					institutionStammdaten.getInstitution()));
		});

		BigDecimal institutionsBetreuungsstundenSum = this.getAngabenInstitutionContainers()
			.stream()
			.reduce(
				new BigDecimal(0),
				(partialResult, instiContainer) -> partialResult.add(instiContainer.isAntragAtLeastInPruefungGemeinde() ?
					Objects.requireNonNull(instiContainer.getAngabenKorrektur())
						.getBetreuungsstundenEinschliesslichBesondereBeduerfnisse() :
					Objects.requireNonNull(instiContainer.getAngabenDeklaration())
						.getBetreuungsstundenEinschliesslichBesondereBeduerfnisse()), BigDecimal::add);

		this.setAngabenDeklaration(
			new Testantrag_LastenausgleichTagesschuleAngabenGemeinde(institutionsBetreuungsstundenSum)
		);
		this.setAngabenInstitutionContainers(new HashSet<>());

	}
}
