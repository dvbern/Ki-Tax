/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.anlageverzeichnis;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.DokumentTyp;

import static ch.dvbern.ebegu.enums.DokumentGrundPersonType.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.DokumentGrundTyp.FINANZIELLESITUATION;

/**
 * In Schwyz wird bei gemeinsamer Steuererklärung {@link Familiensituation#getGemeinsameSteuererklaerung} die letzte
 * rechtskräftige Steuerveranlagung verlangt.
 * <p>
 * Bei getrennter Steuererklärung wird, je nach {@link FinanzielleSituation#getQuellenbesteuert}, die letzte
 * rechtskräftige Steuerveranlagung oder eine Bestätigung über abgerechnete Quellensteuern der Steuerverwaltung SZ verlangt.
 */
public class SchwyzFinanzielleSituationDokumente extends AbstractDokumente<AbstractFinanzielleSituation, Familiensituation> {

	private static final int BEIDE_GESUCHSTELLER = 0;
	private static final int GESUCHSTELLER_1 = 1;
	private static final int GESUCHSTELLER_2 = 2;

	@Override
	public void getAllDokumente(@Nonnull Gesuch gesuch, @Nonnull Set<DokumentGrund> anlageVerzeichnis, @Nonnull Locale locale) {
		Stream<DokumentGrund> stream = isGemeinsameSteuererklaerung(gesuch) ?
			nachweiseGemeinsam(gesuch) :
			nachweiseIndividuell(gesuch);

		stream.forEach(dokument -> add(dokument, anlageVerzeichnis));
	}

	private Stream<DokumentGrund> nachweiseGemeinsam(Gesuch gesuch) {
		return findFinanzielleSituation(gesuch.getGesuchsteller1())
			.map(finSit -> toDokument(DokumentTyp.STEUERVERANLAGUNG, BEIDE_GESUCHSTELLER))
			.stream();
	}

	@Nonnull
	private Stream<DokumentGrund> nachweiseIndividuell(@Nonnull Gesuch gesuch) {
		return Stream.concat(
			streamDokumenteGesuchsteller(gesuch.getGesuchsteller1(), GESUCHSTELLER_1),
			streamDokumenteGesuchsteller(gesuch.getGesuchsteller2(), GESUCHSTELLER_2)
		);
	}

	private Stream<DokumentGrund> streamDokumenteGesuchsteller(
		@Nullable GesuchstellerContainer gesuchsteller,
		int gesuchstellerNumber
	) {
		return findFinanzielleSituation(gesuchsteller)
			.map(this::nachweisTyp)
			.map(nachweisTyp -> toDokument(nachweisTyp, gesuchstellerNumber))
			.stream();
	}

	private DokumentTyp nachweisTyp(FinanzielleSituation finSit) {
		return Boolean.TRUE.equals(finSit.getQuellenbesteuert()) ?
			DokumentTyp.NACHWEIS_ABGERECHNETE_QUELLENSTEUERN :
			DokumentTyp.STEUERVERANLAGUNG;
	}

	private Optional<FinanzielleSituation> findFinanzielleSituation(@Nullable GesuchstellerContainer gesuchsteller) {
		return Optional.ofNullable(gesuchsteller)
			.map(GesuchstellerContainer::getFinanzielleSituationContainer)
			.map(FinanzielleSituationContainer::getFinanzielleSituationJA);
	}

	private DokumentGrund toDokument(DokumentTyp nachweisTyp, int gesuchstellerNumber) {
		return new DokumentGrund(FINANZIELLESITUATION, null, GESUCHSTELLER, gesuchstellerNumber, nachweisTyp);
	}

	@Override
	public boolean isDokumentNeeded(@Nonnull DokumentTyp dokumentTyp, @Nullable AbstractFinanzielleSituation dataForDocument) {
		return true;
	}
}
