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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Erwerbspensum;
import ch.dvbern.ebegu.entities.ErwerbspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.Taetigkeit;
import org.apache.commons.collections.CollectionUtils;

public class LuzernErwerbspensumDokumente extends AbstractDokumente<Erwerbspensum, LocalDate> {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale
	) {
		addAllDokumenteForGesuchsteller(anlageVerzeichnis, gesuch, gesuch.getGesuchsteller1(), 1, locale);

		if(gesuch.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode()) {
			addAllDokumenteForGesuchsteller(anlageVerzeichnis, gesuch, gesuch.getGesuchsteller2(), 2, locale);
		}
	}

	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		@Nullable Erwerbspensum erwerbspensum) {

		if (erwerbspensum == null) {
			return false;
		}

		switch (dokumentTyp) {
		case NACHWEIS_ARBEITSSUCHEND:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.RAV;
		case NACHWEIS_AUSBILDUNG:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.AUSBILDUNG;
		case NACHWEIS_SELBSTAENDIGKEIT:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.SELBSTAENDIG;
		case NACHWEIS_GESUNDHEITLICHE_INDIKATION:
			return erwerbspensum.getTaetigkeit() == Taetigkeit.GESUNDHEITLICHE_EINSCHRAENKUNGEN;
		default:
			return false;
		}
	}

	private void addAllDokumenteForGesuchsteller(
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Gesuch gesuch,
		@Nullable GesuchstellerContainer gesuchsteller,
		@Nonnull Integer gesuchstellerNumber,
		@Nonnull Locale local) {

		if(gesuchsteller == null || CollectionUtils.isEmpty(gesuchsteller.getErwerbspensenContainers())) {
			return;
		}

		Mandant mandant = gesuch.extractMandant();

		gesuchsteller.getErwerbspensenContainers()
			.stream()
			.map(ErwerbspensumContainer::getErwerbspensumJA)
			.filter(Objects::nonNull)
			.forEach(erwerbspensum -> {
				add(getDokument(gesuchstellerNumber, erwerbspensum, DokumentTyp.NACHWEIS_ARBEITSSUCHEND, local, mandant), anlageVerzeichnis);
				add(getDokument(gesuchstellerNumber, erwerbspensum, DokumentTyp.NACHWEIS_AUSBILDUNG, local, mandant), anlageVerzeichnis);
				add(getDokument(gesuchstellerNumber, erwerbspensum, DokumentTyp.NACHWEIS_GESUNDHEITLICHE_INDIKATION, local, mandant), anlageVerzeichnis);
				add(getDokument(gesuchstellerNumber, erwerbspensum, DokumentTyp.NACHWEIS_SELBSTAENDIGKEIT, local, mandant), anlageVerzeichnis);
			});
	}

	@Nullable
	private DokumentGrund getDokument(
		@Nonnull Integer gesuchstellerNumber,
		@Nonnull Erwerbspensum erwerbspensumJA,
		@Nonnull DokumentTyp dokumentTyp,
		@Nonnull Locale locale,
		@Nonnull Mandant mandant
	) {

		return getDokument(
			dokumentTyp,
			erwerbspensumJA,
			erwerbspensumJA.getName(locale, mandant),
			DokumentGrundPersonType.GESUCHSTELLER,
			gesuchstellerNumber,
			DokumentGrundTyp.ERWERBSPENSUM);
	}
}

