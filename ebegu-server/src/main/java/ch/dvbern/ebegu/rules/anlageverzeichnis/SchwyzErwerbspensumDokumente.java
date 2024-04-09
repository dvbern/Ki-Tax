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
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.Taetigkeit;

import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_AUSBILDUNG;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_ERWERBSPENSUM;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_RAV;
import static ch.dvbern.ebegu.enums.DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT;

public class SchwyzErwerbspensumDokumente extends BernErwerbspensumDokumente {

	@Override
	protected void getAllDokumenteGesuchsteller(
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nullable GesuchstellerContainer gesuchsteller,
		@Nonnull Integer gesuchstellerNumber,
		LocalDate gueltigAb,
		@Nonnull Locale locale,
		Mandant mandant) {

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
					pensumJA.getName(locale, mandant),
					DokumentGrundPersonType.GESUCHSTELLER,
					gesuchstellerNumber,
					DokumentGrundTyp.ERWERBSPENSUM,
					null));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_SELBSTAENDIGKEIT, locale, mandant));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_AUSBILDUNG, locale, mandant));
				adder.accept(getDokument(gesuchstellerNumber, pensumJA, NACHWEIS_RAV, locale, mandant));
			});
	}

	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		Erwerbspensum erwerbspensum,
		LocalDate periodenstart,
		LocalDate stichtag
	) {

		return isDokumentNeeded(dokumentTyp, erwerbspensum);
	}

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
		default:
			return false;
		}
	}
}
