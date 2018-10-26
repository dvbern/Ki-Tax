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
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.Taetigkeit;
import ch.dvbern.ebegu.enums.Zuschlagsgrund;

import static ch.dvbern.ebegu.enums.DokumentTyp.BESTAETIGUNG_ARZT;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_AUSBILDUNG;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_ERWERBSPENSUM;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_FIXE_ARBEITSZEITEN;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_GLEICHE_ARBEITSTAGE_BEI_TEILZEIT;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_INTEGRATION_BESCHAEFTIGUNSPROGRAMM;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_LANG_ARBEITSWEG;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_RAV;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_SONSTIGEN_ZUSCHLAG;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_UNREG_ARBEITSZ;

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
 * <p>
 * Dokumente für Erwerbspensumzuschlag:
 * <p>
 * Nachweis über die unregelmässige Arbeitszeit (z.B. ArbG-Bestätigung):
 * Wenn Zuschlag zum Erwerbspensum mit Ja beantwortet und als Grund „Unregelmässige Arbeitszeit“ ausgewählt
 * <p>
 * Nachweis über langen Arbeitsweg:
 * Wenn Zuschlag zum Erwerbspensum mit Ja beantwortet und als Grund „Langer Arbeitsweg“ ausgewählt
 * <p>
 * Grund für sonstigen Zuschlag (z.B. Tod) …. Bessere Formulierung folg:
 * Wenn Zuschlag zum Erwerbspensum mit Ja beantwortet und als Grund „Andere“ ausgewählt
 * Wird nur von JA hochgeladen
 * <p>
 * Gleiche Arbeitstage bei Teilzeit:
 * Wenn Zuschlag zum Erwerbspensum mit Ja beantwortet und als Grund „Überlappende Arbeitszeiten“ ausgewählt
 * <p>
 * Fixe Arbeitszeiten:
 * Wenn Zuschlag zum Erwerbspensum mit Ja beantwortet und als Grund „Fixe Arbeitszeiten“ ausgewählt
 **/
public class ErwerbspensumDokumente extends AbstractDokumente<Erwerbspensum, LocalDate> {

	@Override
	public void getAllDokumente(@Nonnull Gesuch gesuch, @Nonnull Set<DokumentGrund> anlageVerzeichnis) {

		final LocalDate gueltigAb = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb();

		final GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		getAllDokumenteGesuchsteller(anlageVerzeichnis, gesuchsteller1, 1, gueltigAb);

		final GesuchstellerContainer gesuchsteller2 = gesuch.getGesuchsteller2();
		getAllDokumenteGesuchsteller(anlageVerzeichnis, gesuchsteller2, 2, gueltigAb);
	}

	private void getAllDokumenteGesuchsteller(
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nullable GesuchstellerContainer gesuchsteller,
		@Nonnull Integer gesuchstellerNumber,
		LocalDate gueltigAb) {

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
					pensumJA.getName(),
					DokumentGrundPersonType.GESUCHSTELLER,
					gesuchstellerNumber,
					DokumentGrundTyp.ERWERBSPENSUM));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_SELBSTAENDIGKEIT));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_AUSBILDUNG));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_RAV));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, BESTAETIGUNG_ARZT));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_INTEGRATION_BESCHAEFTIGUNSPROGRAMM));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_UNREG_ARBEITSZ));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_LANG_ARBEITSWEG));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_SONSTIGEN_ZUSCHLAG));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_GLEICHE_ARBEITSTAGE_BEI_TEILZEIT));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_FIXE_ARBEITSZEITEN));
			});
	}

	@Nullable
	private DokumentGrund getDokument(
		@Nonnull Integer gesuchstellerNumber,
		@Nonnull Erwerbspensum erwerbspensumJA,
		@Nonnull DokumentTyp dokumentTyp) {

		return getDokument(
			dokumentTyp,
			erwerbspensumJA,
			erwerbspensumJA.getName(),
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber,
			DokumentGrundTyp.ERWERBSPENSUM);
	}

	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		Erwerbspensum erwerbspensum,
		LocalDate periodenstart) {

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
			return erwerbspensum.getTaetigkeit() == Taetigkeit.GESUNDHEITLICHE_INDIKATION;
		case NACHWEIS_INTEGRATION_BESCHAEFTIGUNSPROGRAMM:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.INTEGRATION_BESCHAEFTIGUNSPROGRAMM;
		case NACHWEIS_UNREG_ARBEITSZ:
			return erwerbspensum.getZuschlagZuErwerbspensum()
				&& erwerbspensum.getZuschlagsgrund() == Zuschlagsgrund.UNREGELMAESSIGE_ARBEITSZEITEN;
		case NACHWEIS_LANG_ARBEITSWEG:
			return erwerbspensum.getZuschlagZuErwerbspensum()
				&& erwerbspensum.getZuschlagsgrund() == Zuschlagsgrund.LANGER_ARBWEITSWEG;
		case NACHWEIS_SONSTIGEN_ZUSCHLAG:
			return erwerbspensum.getZuschlagZuErwerbspensum()
				&& erwerbspensum.getZuschlagsgrund() == Zuschlagsgrund.ANDERE;
		case NACHWEIS_GLEICHE_ARBEITSTAGE_BEI_TEILZEIT:
			return erwerbspensum.getZuschlagZuErwerbspensum()
				&& erwerbspensum.getZuschlagsgrund() == Zuschlagsgrund.UEBERLAPPENDE_ARBEITSZEITEN;
		case NACHWEIS_FIXE_ARBEITSZEITEN:
			return erwerbspensum.getZuschlagZuErwerbspensum()
				&& erwerbspensum.getZuschlagsgrund() == Zuschlagsgrund.FIXE_ARBEITSZEITEN;
		default:
			return false;
		}
	}
}
