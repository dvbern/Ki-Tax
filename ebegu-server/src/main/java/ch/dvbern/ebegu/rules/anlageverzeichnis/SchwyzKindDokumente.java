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
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.entities.PensumFachstelle;
import ch.dvbern.ebegu.enums.DokumentGrundPersonType;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.ServerMessageUtil;

/**
 * Dokumente für Kinder:
 * <p>
 * Fachstellenbestätigung:
 * Notwendig, wenn Frage nach Kind Fachstelle involviert mit Ja beantwortet
 * Es gibt nur ein Dokument, egal ob es soziale Integration oder sprachliche Integration ist.
 * </p>
 **/
public class SchwyzKindDokumente extends AbstractDokumente<Kind, Object> {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale
	) {
		final Set<KindContainer> kindContainers = gesuch.getKindContainers();
		if (kindContainers == null || kindContainers.isEmpty()) {
			return;
		}

		for (KindContainer kindContainer : kindContainers) {
			final Kind kindJA = kindContainer.getKindJA();
			add(getDokumentNachweisHoehereBeitraegeBeeintraechtigung(kindContainer, kindJA), anlageVerzeichnis);
		}
	}

	@Nullable
	private DokumentGrund getDokumentNachweisHoehereBeitraegeBeeintraechtigung(KindContainer kindContainer, @Nonnull Kind kindJA) {
		return getDokument(
			DokumentTyp.NACHWEIS_HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG,
			kindJA,
			kindJA.getFullName(),
			null,
			DokumentGrundPersonType.KIND,
			kindContainer.getKindNummer(),
			DokumentGrundTyp.KINDER,
			null);
	}

	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean isDokumentNeeded(@Nonnull DokumentTyp dokumentTyp, @Nullable Kind kind) {
		if (dokumentTyp == DokumentTyp.NACHWEIS_HOEHERE_BEITRAEGE_BEEINTRAECHTIGUNG) {
			return kind != null
				&& Boolean.TRUE.equals(kind.getHoehereBeitraegeWegenBeeintraechtigungBeantragen())
				&& Boolean.TRUE.equals(kind.getHoehereBeitraegeUnterlagenDigital());
		}
		return false;
	}
}
