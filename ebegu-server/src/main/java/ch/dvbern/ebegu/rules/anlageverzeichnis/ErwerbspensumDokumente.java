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

package ch.dvbern.ebegu.rules.anlageverzeichnis;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.Taetigkeit;

import static ch.dvbern.ebegu.enums.DokumentTyp.BESTAETIGUNG_ARZT;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_AUSBILDUNG;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_ERWERBSPENSUM;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_FREIWILLIGENARBEIT;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_INTEGRATION_BESCHAEFTIGUNSPROGRAMM;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_RAV;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT;

/**
 * Dokumente für Erwerbspensum:
 * <p>
 * Arbeitsvertrag / Stundennachweise / sonstiger Nachweis über Erwerbspensum:
 * Wird immer verlangt wenn Erwerbspensum deklariert wurde
 * <p>
 * Nachweis Selbständigkeit oder AHV-Bestätigung:
 * z.B. für Künstler, müssen Projekte belegen
 * Notwendig, wenn ein Pensum für Selbständigkeit erfasst wurde
 * <p>
 * Nachweis über Ausbildung (z.B. Ausbildungsvertrag, Immatrikulationsbestätigung):
 * Notwendig, wenn ein Pensum für Ausbildung erfasst wurde
 * <p>
 * RAV-Bestätigung oder Nachweis der Vermittelbarkeit:
 * Notwendig, wenn ein Pensum für RAV erfasst wurde
 * <p>
 * Bestätigung (ärztliche Indikation):
 * Notwendig, wenn Frage nach GS Gesundheitliche Einschränkung mit Ja beantwortet wird (gesundheitliche Einschränkung)
 **/
public class ErwerbspensumDokumente extends AbstractDokumente<Erwerbspensum, LocalDate> {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale
	) {

		final LocalDate gueltigAb = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb();

		final GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		//if Verguenstigung nicht gewuenscht - keine Dokumenten
		final FamiliensituationContainer famSitCont = gesuch.getFamiliensituationContainer();
		if (famSitCont != null && !isVerguenstigungGewuenscht(famSitCont.getFamiliensituationJA())
			&& !isSozialhilfeempfaenger(famSitCont.getFamiliensituationJA())) {
			return;
		}

		// if nuer TS oder FI - keine Dokumenten
		if (gesuch.hasOnlyBetreuungenOfSchulamt()) {
			return;
		}

		getAllDokumenteGesuchsteller(anlageVerzeichnis, gesuchsteller1, 1, gueltigAb, locale);

		if (gesuch.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode()) {
			final GesuchstellerContainer gesuchsteller2 = gesuch.getGesuchsteller2();
			getAllDokumenteGesuchsteller(anlageVerzeichnis, gesuchsteller2, 2, gueltigAb, locale);
		}
	}

	private void getAllDokumenteGesuchsteller(
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nullable GesuchstellerContainer gesuchsteller,
		@Nonnull Integer gesuchstellerNumber,
		LocalDate gueltigAb,
		@Nonnull Locale locale
	) {

		if (gesuchsteller == null || gesuchsteller.getErwerbspensenContainers().isEmpty()) {
			return;
		}

		final Set<ErwerbspensumContainer> erwerbspensenContainers = gesuchsteller.getErwerbspensenContainers();

		Consumer<DokumentGrund> adder = dokumentGrund -> add(dokumentGrund, anlageVerzeichnis);
		erwerbspensenContainers.stream()
			.map(ErwerbspensumContainer::getErwerbspensumJA)
			.filter(Objects::nonNull)
			.forEach(pensumJA -> {
				adder.accept(getDokument(
					NACHWEIS_ERWERBSPENSUM,
					pensumJA,
					gueltigAb,
					pensumJA.getName(locale),
					DokumentGrundPersonType.GESUCHSTELLER,
					gesuchstellerNumber,
					DokumentGrundTyp.ERWERBSPENSUM,
					null));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_SELBSTAENDIGKEIT, locale));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_AUSBILDUNG, locale));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_RAV, locale));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_FREIWILLIGENARBEIT, locale));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, BESTAETIGUNG_ARZT, locale));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_INTEGRATION_BESCHAEFTIGUNSPROGRAMM,
					locale));
			});
	}

	@Nullable
	private DokumentGrund getDokument(
		@Nonnull Integer gesuchstellerNumber,
		@Nonnull Erwerbspensum erwerbspensumJA,
		@Nonnull DokumentTyp dokumentTyp,
		@Nonnull Locale locale
	) {

		return getDokument(
			dokumentTyp,
			erwerbspensumJA,
			erwerbspensumJA.getName(locale),
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber,
			DokumentGrundTyp.ERWERBSPENSUM);
	}

	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		Erwerbspensum erwerbspensum,
		LocalDate periodenstart,
		LocalDate stichtag
	) {

		return isDokumentNeeded(dokumentTyp, erwerbspensum);
	}

	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean isDokumentNeeded(@Nonnull DokumentTyp dokumentTyp, @Nullable Erwerbspensum erwerbspensum) {
		if (erwerbspensum == null) {
			return false;
		}

		switch (dokumentTyp) {
		case NACHWEIS_ERWERBSPENSUM:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.ANGESTELLT;
		case NACHWEIS_SELBSTAENDIGKEIT:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.SELBSTAENDIG;
		case NACHWEIS_AUSBILDUNG:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.AUSBILDUNG;
		case NACHWEIS_RAV:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.RAV;
		case BESTAETIGUNG_ARZT:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN;
		case NACHWEIS_INTEGRATION_BESCHAEFTIGUNSPROGRAMM:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.INTEGRATION_BESCHAEFTIGUNSPROGRAMM;
		case NACHWEIS_FREIWILLIGENARBEIT:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.FREIWILLIGENARBEIT;
		default:
			return false;
		}
	}
}
