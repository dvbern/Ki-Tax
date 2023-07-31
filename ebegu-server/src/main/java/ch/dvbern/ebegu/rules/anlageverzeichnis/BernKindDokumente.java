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
public class BernKindDokumente extends AbstractDokumente<Kind, Object> {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale
	) {
		var mandant = gesuch.extractMandant();
		final Set<KindContainer> kindContainers = gesuch.getKindContainers();
		if (kindContainers == null || kindContainers.isEmpty()) {
			return;
		}

		for (KindContainer kindContainer : kindContainers) {
			final Kind kindJA = kindContainer.getKindJA();

			for (PensumFachstelle pensumFachstelle : kindJA.getPensumFachstelle()) {
				add(getDokumentFachstellenbestaetigung(kindContainer, kindJA, pensumFachstelle, locale, mandant), anlageVerzeichnis);
			}
			add(getDokumentAbsageschreibenHortplatz(kindContainer, kindJA), anlageVerzeichnis);
		}
	}

	@Nullable
	private DokumentGrund getDokumentFachstellenbestaetigung(KindContainer kindContainer, @Nonnull Kind kindJA,
		PensumFachstelle pensumFachstelle, @Nonnull Locale locale, @Nonnull Mandant mandant) {
		return getDokument(
			DokumentTyp.FACHSTELLENBESTAETIGUNG,
			kindJA,
			pensumFachstelle,
			getPensumFachstelleTag(pensumFachstelle, locale, mandant),
			DokumentGrundPersonType.KIND,
			kindContainer.getKindNummer(),
			DokumentGrundTyp.KINDER,
			null);
	}

	private String getPensumFachstelleTag(@Nonnull PensumFachstelle pensumFachstelle, @Nonnull Locale locale,
			@Nonnull Mandant mandant) {
		var gueltigAbStr = pensumFachstelle.getGueltigkeit().getGueltigAb().format(Constants.DATE_FORMATTER);
		var gueltigBis = pensumFachstelle.getGueltigkeit().getGueltigBis();
		if (gueltigBis.equals(Constants.END_OF_TIME))  {
			return ServerMessageUtil.getMessage("Ab", locale, mandant) + ' ' + gueltigAbStr;
		}
		return gueltigBis.format(Constants.DATE_FORMATTER) + " - " + gueltigBis.format(Constants.DATE_FORMATTER);
	}

	@Nullable
	private DokumentGrund getDokumentAbsageschreibenHortplatz(KindContainer kindContainer, @Nonnull Kind kindJA) {
		return getDokument(
			DokumentTyp.ABSAGESCHREIBEN_HORTPLATZ,
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
		switch (dokumentTyp) {
		case FACHSTELLENBESTAETIGUNG:
			return kind != null && !kind.getPensumFachstelle().isEmpty();
		case ABSAGESCHREIBEN_HORTPLATZ:
			return kind != null && kind.getKeinPlatzInSchulhort();
		default:
			return false;
		}
	}
}
