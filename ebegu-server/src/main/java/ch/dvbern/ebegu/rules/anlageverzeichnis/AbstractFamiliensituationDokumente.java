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

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.EnumGesuchstellerKardinalitaet;
import ch.dvbern.ebegu.enums.UnterhaltsvereinbarungAnswer;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

/**
 * Dokumente für Familiensituation:
 * <p>
 * Trennungsvereinbarung / Scheidungsurteil / Sonstiger Nachweis über Trennung / Eheschutzverfahren:
 * <p>
 * Wird nur bei Mutation der Familiensituation verlangt, nicht bei Erstgesuch.
 * Notwendig beim Wechsel von zwei Gesuchsteller auf einen.
 * Nur eines der drei Dokumente ist notwendig. Die Dokumente werden im Anlageverzeichnis als 1 Punkt geführt
 * <p>
 * Unterstützungsnachweis / Bestätigung Sozialdienst
 * Notwendig, wenn die GS Sozialhilfe bekommen
 **/
public abstract class AbstractFamiliensituationDokumente extends AbstractDokumente<Familiensituation, Familiensituation> {

	@Override
	public void getAllDokumente(
		@Nonnull Gesuch gesuch,
		@Nonnull Set<DokumentGrund> anlageVerzeichnis,
		@Nonnull Locale locale
	) {
		LocalDate gesuchsperiodeBis = gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis();
		Familiensituation famsitErstgesuch = gesuch.extractFamiliensituationErstgesuch();
		if (famsitErstgesuch != null) {
			add(
				getDokument(
					DokumentTyp.NACHWEIS_TRENNUNG,
					famsitErstgesuch,
					gesuch.extractFamiliensituation(),
					null,
					null,
					null,
					DokumentGrundTyp.FAMILIENSITUATION,
					gesuchsperiodeBis
				),
				anlageVerzeichnis
			);
		}
		add(getDokument(DokumentTyp.NACHWEIS_UNTERHALTSVEREINBARUNG, gesuch.extractFamiliensituation(),
				null, null, null, DokumentGrundTyp.FAMILIENSITUATION), anlageVerzeichnis);
		// dieses Dokument gehoert eigentlich zur FinSit aber muss hier hinzugefuegt werden, da es Daten aus der
		// Familiensituation benoetigt
		add(getDokument(DokumentTyp.UNTERSTUETZUNGSBESTAETIGUNG, gesuch.extractFamiliensituation(),
			null, null, null, DokumentGrundTyp.FINANZIELLESITUATION), anlageVerzeichnis);
		add(getDokument(DokumentTyp.NACHWEIS_GETEILTE_OBHUT, gesuch.extractFamiliensituation(),
			null, null, null, DokumentGrundTyp.FAMILIENSITUATION), anlageVerzeichnis);
	}

	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean isDokumentNeeded(@Nonnull DokumentTyp dokumentTyp, @Nullable Familiensituation familiensituation) {
		if (familiensituation == null) {
			return false;
		}
		switch (dokumentTyp) {
		case UNTERSTUETZUNGSBESTAETIGUNG:
			return isUnterstuetzungsbestaetigungNeeded(familiensituation);
		case NACHWEIS_UNTERHALTSVEREINBARUNG:
			return isNachweisunterhaltsverinabrungNeeded(familiensituation);
		case NACHWEIS_GETEILTE_OBHUT:
			return isNachweisGeteilteObhutNeeded(familiensituation);
		default:
			return false;
		}
	}

	protected abstract boolean isUnterstuetzungsbestaetigungNeeded(Familiensituation familiensituation);

	private boolean isNachweisGeteilteObhutNeeded(Familiensituation familiensituation) {
		if (!familiensituation.isFkjvFamSit()) {
			return false;
		}

		if (familiensituation.getGeteilteObhut() == null || !familiensituation.getGeteilteObhut()) {
			return false;
		}

		return familiensituation.getGesuchstellerKardinalitaet() == EnumGesuchstellerKardinalitaet.ALLEINE;
	}

	private boolean isNachweisunterhaltsverinabrungNeeded(Familiensituation familiensituation) {
		if (EbeguUtil.isNullOrFalse(familiensituation.isFkjvFamSit())) {
			return false;
		}

		//Sozialhilfebezüger müssen kein Beleg zur Unterhaltsvereinbarung hochladen
		if (EbeguUtil.isNotNullAndTrue(familiensituation.getSozialhilfeBezueger())) {
			return false;
		}

		return familiensituation.getUnterhaltsvereinbarung() == UnterhaltsvereinbarungAnswer.JA_UNTERHALTSVEREINBARUNG;
	}

	@SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
	@Override
	public boolean isDokumentNeeded(
		@Nonnull DokumentTyp dokumentTyp,
		Familiensituation familiensituationErstgesuch,
		Familiensituation familiensituationMutation,
		@Nullable LocalDate stichtag) {

		if (familiensituationErstgesuch == null || familiensituationMutation == null || stichtag == null) {
			return false;
		}
		switch (dokumentTyp) {
		case NACHWEIS_TRENNUNG:
			//überprüfen, ob ein Wechsel von zwei Gesuchsteller auf einen stattgefunden hat.
			return familiensituationErstgesuch.hasSecondGesuchsteller(stichtag)
				&& !familiensituationMutation.hasSecondGesuchsteller(stichtag);
		default:
			return false;
		}
	}
}
