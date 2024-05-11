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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.familiensituation.FamiliensituationUtil;

/**
 * Abstrakte Klasse zum Berechnen der benötigten Dokumente
 */
abstract class AbstractDokumente<T1, T2> {

	public abstract void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale
	);

	public abstract boolean isDokumentNeeded(@Nonnull DokumentTyp dokumentTyp, @Nullable T1 dataForDocument);

	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		@Nullable T1 dataForDocument1,
		@Nullable T2 dataForDocument2,
		@Nullable LocalDate stichtag
	) {
		return isDokumentNeeded(dokumentTyp, dataForDocument1);
	}

	void add(@Nullable DokumentGrund dokumentGrund, @Nonnull Set<DokumentGrund> anlageVerzeichnis) {
		if (dokumentGrund != null) {
			anlageVerzeichnis.add(dokumentGrund);
		}
	}

	@Nullable
	DokumentGrund getDokument(
		@Nonnull DokumentTyp dokumentTyp,
		@Nullable T1 dataForDocument,
		@Nullable String tag,
		@Nullable DokumentGrundPersonType personType,
		@Nullable Integer personNumber,
		@Nonnull DokumentGrundTyp dokumentGrundTyp) {

		return isDokumentNeeded(dokumentTyp, dataForDocument) ?
			new DokumentGrund(dokumentGrundTyp, tag, personType, personNumber, dokumentTyp) :
			null;
	}

	@Nullable
	DokumentGrund getDokument(
		@Nonnull DokumentTyp dokumentTyp,
		@Nonnull T1 dataForDocument1,
		@Nullable T2 dataForDocument2,
		@Nullable String tag,
		@Nullable DokumentGrundPersonType personType,
		@Nullable Integer personNumber,
		@Nonnull DokumentGrundTyp dokumentGrundTyp,
		@Nullable LocalDate stichtag) {

		return isDokumentNeeded(dokumentTyp, dataForDocument1, dataForDocument2, stichtag) ?
			new DokumentGrund(dokumentGrundTyp, tag, personType, personNumber, dokumentTyp) :
			null;
	}

	protected boolean isGemeinsameSteuererklaerung(@Nonnull Gesuch gesuch) {
		return FamiliensituationUtil.isGemeinsameSteuererklaerung(gesuch);
	}

	protected boolean isVerguenstigungGewuenscht(@Nullable Familiensituation familiensituation) {
		return familiensituation != null
			&& familiensituation.getVerguenstigungGewuenscht() != null
			&& familiensituation.getVerguenstigungGewuenscht();
	}

	protected boolean isSozialhilfeempfaenger(@Nullable Familiensituation familiensituation) {
		return familiensituation != null
			&& familiensituation.getSozialhilfeBezueger() != null
			&& familiensituation.getSozialhilfeBezueger();
	}
}
