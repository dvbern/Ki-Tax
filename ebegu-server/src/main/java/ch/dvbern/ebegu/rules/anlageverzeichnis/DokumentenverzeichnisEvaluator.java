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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.DokumentGrund;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.AnspruchBeschaeftigungAbhaengigkeitTyp;
import ch.dvbern.ebegu.enums.DokumentGrundTyp;
import ch.dvbern.ebegu.enums.DokumentTyp;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.services.EinstellungService;

@Stateless
public class DokumentenverzeichnisEvaluator {

	@Inject
	private EinstellungService einstellungService;

	private final FamiliensituationDokumenteVisitor familiensituationVisitor = new FamiliensituationDokumenteVisitor();
	private final KindDokumenteVisitor kindDokumenteVisitor = new KindDokumenteVisitor();
	private final BetreuungDokumenteVisitor  betreuungDokumenteVisitor = new BetreuungDokumenteVisitor();

	private final FinanzielleSituationDokumenteVisitor
		finanzielleSituationVisitor = new FinanzielleSituationDokumenteVisitor();
	private final EinkommenVerschlechterungDokumenteVisitor
		einkommenVerschlechterungDokumenteVisitor = new EinkommenVerschlechterungDokumenteVisitor();
	private final ErwerbspensumDokumenteVisitor
		erwerbspensumDokumenteVisitor = new ErwerbspensumDokumenteVisitor();

	/**
	 * Gibt die *zwingenden* DokumentGruende fuer das uebergebene Gesuch zurueck.
	 */
	public Set<DokumentGrund> calculate(
		@Nullable Gesuch gesuch,
		@Nonnull Locale locale
	) {

		Set<DokumentGrund> anlageVerzeichnis = new HashSet<>();

		if (gesuch != null) {
			Mandant mandant = gesuch.extractMandant();

			familiensituationVisitor
				.getFamiliensituationDokumenteForMandant(mandant)
				.getAllDokumente(gesuch, anlageVerzeichnis, locale);
			kindDokumenteVisitor
				.getKindDokumenteForMandant(mandant)
				.getAllDokumente(gesuch, anlageVerzeichnis, locale);
			if (isErwerbpensumDokumenteRequired(gesuch)) {
				erwerbspensumDokumenteVisitor
					.getErwerbspensumeDokumenteForMandant(mandant)
					.getAllDokumente(gesuch, anlageVerzeichnis, locale);
			}
			finanzielleSituationVisitor
				.getFinanzielleSituationDokumenteForFinSitTyp(gesuch.getFinSitTyp())
				.getAllDokumente(gesuch, anlageVerzeichnis, locale);
			einkommenVerschlechterungDokumenteVisitor
				.getEinkommenVerschlechterungDokumenteForFinSitTyp(gesuch.getFinSitTyp())
				.getAllDokumente(gesuch, anlageVerzeichnis, locale);
			betreuungDokumenteVisitor
				.getBetreuungDokumenteForMandant(mandant)
				.getAllDokumente(gesuch, anlageVerzeichnis, locale);
		}

		return anlageVerzeichnis;
	}

	private boolean isErwerbpensumDokumenteRequired(Gesuch gesuch) {
		var anspruchUnabhaengig = einstellungService
			.findEinstellung(
				EinstellungKey.ABHAENGIGKEIT_ANSPRUCH_BESCHAEFTIGUNGPENSUM,
				gesuch.extractGemeinde(),
				gesuch.getGesuchsperiode()
			);
		return !AnspruchBeschaeftigungAbhaengigkeitTyp.getEnumValue(anspruchUnabhaengig).isAnspruchUnabhaengig();
	}

	public void addSonstige(Set<DokumentGrund> dokumentGrunds) {
		DokumentGrund dokumentGrund = new DokumentGrund(DokumentGrundTyp.SONSTIGE_NACHWEISE, DokumentTyp.DIV);
		dokumentGrund.setNeeded(false);
		dokumentGrunds.add(dokumentGrund);
	}

	public void addPapiergesuch(Set<DokumentGrund> dokumentGrunds) {
		DokumentGrund dokumentGrund = new DokumentGrund(DokumentGrundTyp.PAPIERGESUCH, DokumentTyp.ORIGINAL_PAPIERGESUCH);
		dokumentGrund.setNeeded(false);
		dokumentGrunds.add(dokumentGrund);
	}

	public void addFreigabequittung(Set<DokumentGrund> dokumentGrunds) {
		DokumentGrund dokumentGrund = new DokumentGrund(DokumentGrundTyp.FREIGABEQUITTUNG, DokumentTyp.ORIGINAL_FREIGABEQUITTUNG);
		dokumentGrund.setNeeded(false);
		dokumentGrunds.add(dokumentGrund);
	}

	/**
	 * Fuegt alle Dokumente zum Set, welche nicht zwingend sind und daher nicht in der Method calculate() hinzugefuegt
	 * werden
	 */
	public void addOptionalDokumentGruende(Set<DokumentGrund> dokumentGrunds) {
		addSonstige(dokumentGrunds);
		addPapiergesuch(dokumentGrunds);
		addFreigabequittung(dokumentGrunds);
	}

	/**
	 * Fuegt alle Dokumente des gewuenschten Typs zum Set, welche nicht zwingend sind und daher nicht in der Method
	 * calculate() hinzugefuegt werden
	 */
	public void addOptionalDokumentGruendeByType(Set<DokumentGrund> dokumentGrunds, DokumentGrundTyp requestedOptionalDocumentType) {
		if (requestedOptionalDocumentType == DokumentGrundTyp.PAPIERGESUCH) {
			addPapiergesuch(dokumentGrunds);
		} else if (requestedOptionalDocumentType == DokumentGrundTyp.FREIGABEQUITTUNG) {
			addFreigabequittung(dokumentGrunds);
		} else if (requestedOptionalDocumentType == DokumentGrundTyp.SONSTIGE_NACHWEISE) {
			addSonstige(dokumentGrunds);
		}
	}
}
