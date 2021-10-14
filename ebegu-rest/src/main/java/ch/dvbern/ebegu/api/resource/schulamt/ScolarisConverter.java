/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource.schulamt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.enterprise.context.Dependent;

import ch.dvbern.ebegu.api.dtos.JaxExternalAnmeldungFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxExternalAnmeldungTagesschule;
import ch.dvbern.ebegu.api.dtos.JaxExternalFerieninsel;
import ch.dvbern.ebegu.api.dtos.JaxExternalFinanzielleSituation;
import ch.dvbern.ebegu.api.dtos.JaxExternalModul;
import ch.dvbern.ebegu.api.dtos.JaxExternalRechnungsAdresse;
import ch.dvbern.ebegu.api.enums.JaxExternalAntragstatus;
import ch.dvbern.ebegu.api.enums.JaxExternalBetreuungsangebotTyp;
import ch.dvbern.ebegu.api.enums.JaxExternalBetreuungsstatus;
import ch.dvbern.ebegu.api.enums.JaxExternalFerienName;
import ch.dvbern.ebegu.api.enums.JaxExternalModulName;
import ch.dvbern.ebegu.api.enums.JaxExternalTarifart;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerAdresse;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.AntragStatus;
import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.Ferienname;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.ModulTagesschuleName;
import ch.dvbern.ebegu.errors.ScolarisException;
import ch.dvbern.ebegu.util.EbeguUtil;
import org.apache.commons.collections.CollectionUtils;

@Dependent
public class ScolarisConverter {

	@Nonnull
	public JaxExternalAnmeldungTagesschule anmeldungTagesschuleToScolaris(@Nonnull AnmeldungTagesschule betreuung)
		throws ScolarisException {
		Objects.requireNonNull(betreuung.getBelegungTagesschule());

		List<JaxExternalModul> anmeldungen = new ArrayList<>();
		betreuung.getBelegungTagesschule()
			.getBelegungTagesschuleModule()
			.forEach(modulTagesschule -> anmeldungen.add(modulToScolaris(modulTagesschule)));
		if (CollectionUtils.isEmpty(anmeldungen)) {
			throw new ScolarisException("No Modules found for " + betreuung.getBGNummer());
		}
		return new JaxExternalAnmeldungTagesschule(
			betreuung.getBGNummer(),
			betreuungsstatusToScolaris(betreuung.getBetreuungsstatus()),
			betreuung.getInstitutionStammdaten().getInstitution().getName(),
			anmeldungen,
			betreuung.getKind().getKindJA().getVorname(),
			betreuung.getKind().getKindJA().getNachname());
	}

	@Nonnull
	public JaxExternalAnmeldungFerieninsel anmeldungFerieninselToScolaris(@Nonnull AnmeldungFerieninsel betreuung)
		throws ScolarisException {
		Objects.requireNonNull(betreuung.getBelegungFerieninsel());

		List<LocalDate> datumList = new ArrayList<>();
		betreuung.getBelegungFerieninsel()
			.getTage()
			.forEach(belegungFerieninselTag -> datumList.add(belegungFerieninselTag.getTag()));

		JaxExternalFerienName jaxExternalFerienName =
			feriennameToScolaris(betreuung.getBelegungFerieninsel().getFerienname());
		JaxExternalFerieninsel ferieninsel =
			new JaxExternalFerieninsel(jaxExternalFerienName, datumList);

		return new JaxExternalAnmeldungFerieninsel(
			betreuung.getBGNummer(),
			betreuungsstatusToScolaris(betreuung.getBetreuungsstatus()),
			betreuung.getInstitutionStammdaten().getInstitution().getName(),
			ferieninsel,
			betreuung.getKind().getKindJA().getVorname(),
			betreuung.getKind().getKindJA().getNachname());
	}

	@Nonnull
	public Optional<JaxExternalFinanzielleSituation> finanzielleSituationToScolaris(
		long fallNummer,
		LocalDate stichtag,
		Gesuch neustesGesuch,
		Verfuegung famGroessenVerfuegung
	) {

		final Familiensituation familiensituation = neustesGesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);

		if (EbeguUtil.isNotNullAndTrue(familiensituation.getSozialhilfeBezueger())
			&& neustesGesuch.getFinSitStatus() == FinSitStatus.AKZEPTIERT) {
			// SozialhilfeBezüger Ja -> Basiszahler (keine finSit!)
			return Optional.of(convertToJaxExternalFinanzielleSituationWithoutFinDaten(
				fallNummer, stichtag, neustesGesuch, JaxExternalTarifart.BASISZAHLER));
		}

		if ((EbeguUtil.isNotNullAndFalse(familiensituation.getSozialhilfeBezueger())
			&& EbeguUtil.isNotNullAndFalse(familiensituation.getVerguenstigungGewuenscht()))
			|| neustesGesuch.getFinSitStatus() == FinSitStatus.ABGELEHNT) {
			// SozialhilfeBezüger Nein + Vergünstigung gewünscht Nein  -> Vollzahler (keine finSit!)
			return Optional.of(convertToJaxExternalFinanzielleSituationWithoutFinDaten(
				fallNummer, stichtag, neustesGesuch, JaxExternalTarifart.VOLLZAHLER));

		}
		// SozialhilfeBezüger Nein + Vergünstigung gewünscht ja  oder Kita-Betreuung vorhanden -> Detailrechnung (mit
		// finSit!)
		// Find and return Finanzdaten on Verfügungszeitabschnitt from Stichtag
		List<VerfuegungZeitabschnitt> zeitabschnitten = famGroessenVerfuegung.getZeitabschnitte();
		// get finanzielleSituation only for stichtag
		for (VerfuegungZeitabschnitt zeitabschnitt : zeitabschnitten) {
			if (zeitabschnitt.getGueltigkeit().contains(stichtag)) {
				return Optional.of(convertToJaxExternalFinanzielleSituation(
					fallNummer, stichtag, neustesGesuch, zeitabschnitt));
			}
		}
		// If no Finanzdaten found on Verfügungszeitabschnitt from Stichtag, return ErrorObject
		return Optional.empty();
	}

	@Nonnull
	JaxExternalAntragstatus antragstatusToScolaris(@Nonnull AntragStatus status) {
		// Es sind in Scolaris alle Status vorhanden, ausser KEIN_KONTINGENT. Dieses behandeln wir
		// wie GEPRUEFT
		if (AntragStatus.KEIN_KONTINGENT == status) {
			return JaxExternalAntragstatus.GEPRUEFT;
		}
		return JaxExternalAntragstatus.valueOf(status.name());
	}

	@Nonnull
	JaxExternalBetreuungsangebotTyp betreuungsangebotTypToScolaris(@Nonnull BetreuungsangebotTyp typ)
		throws ScolarisException {
		// In Scolaris werden nur TAGESSCHULE und FERIENINSEL behandelt
		if (BetreuungsangebotTyp.TAGESSCHULE == typ) {
			return JaxExternalBetreuungsangebotTyp.TAGESSCHULE;
		}
		if (BetreuungsangebotTyp.FERIENINSEL == typ) {
			return JaxExternalBetreuungsangebotTyp.FERIENINSEL;
		}
		throw new ScolarisException("Could not convert BetreuungsangebotTyp " + typ);
	}

	@Nonnull
	JaxExternalBetreuungsstatus betreuungsstatusToScolaris(@Nonnull Betreuungsstatus status) throws ScolarisException {
		switch (status) {
		case SCHULAMT_ANMELDUNG_ERFASST:
			return JaxExternalBetreuungsstatus.SCHULAMT_ANMELDUNG_ERFASST;
		case SCHULAMT_ANMELDUNG_AUSGELOEST:
			return JaxExternalBetreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST;
		case SCHULAMT_MODULE_AKZEPTIERT: // Neuer Status, aus Sicht Scolaris wie UEBERNOMMEN
		case SCHULAMT_ANMELDUNG_UEBERNOMMEN:
			return JaxExternalBetreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN;
		case SCHULAMT_ANMELDUNG_ABGELEHNT:
			return JaxExternalBetreuungsstatus.SCHULAMT_ANMELDUNG_ABGELEHNT;
		case SCHULAMT_FALSCHE_INSTITUTION:
			return JaxExternalBetreuungsstatus.SCHULAMT_FALSCHE_INSTITUTION;
		default:
			throw new ScolarisException("Could not convert Betreuungsstatus " + status);
		}
	}

	@Nonnull
	JaxExternalFerienName feriennameToScolaris(@Nonnull Ferienname ferienname) {
		return JaxExternalFerienName.valueOf(ferienname.name());
	}

	@Nonnull
	JaxExternalModulName modulnameToScolaris(@Nonnull ModulTagesschuleName modulTagesschuleName)
		throws ScolarisException {
		if (ModulTagesschuleName.DYNAMISCH == modulTagesschuleName) {
			throw new ScolarisException("Could not convert ModulTagesschuleName " + modulTagesschuleName);
		}
		return JaxExternalModulName.valueOf(modulTagesschuleName.name());
	}

	@Nonnull
	JaxExternalModul modulToScolaris(@Nonnull BelegungTagesschuleModul tagesschuleModul) throws ScolarisException {
		ModulTagesschuleName modulTagesschuleName =
			tagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getModulTagesschuleName();
		JaxExternalModulName jaxModulname = modulnameToScolaris(modulTagesschuleName);
		return new JaxExternalModul(
			tagesschuleModul.getModulTagesschule().getWochentag(),
			jaxModulname);
	}

	@Nonnull
	JaxExternalFinanzielleSituation convertToJaxExternalFinanzielleSituation(
		long fallNummer,
		@Nonnull LocalDate stichtag,
		@Nonnull Gesuch neustesGesuch,
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt
	) {
		BigDecimal abzugFamGroesse = zeitabschnitt.getAbzugFamGroesse() != null
			? zeitabschnitt.getAbzugFamGroesse() : BigDecimal.ZERO;
		return new JaxExternalFinanzielleSituation(
			fallNummer,
			stichtag,
			zeitabschnitt.getMassgebendesEinkommenVorAbzFamgr(),
			abzugFamGroesse,
			antragstatusToScolaris(neustesGesuch.getStatus()),
			JaxExternalTarifart.DETAILBERECHNUNG,
			rechnungsAdresseToScolaris(neustesGesuch, stichtag));
	}

	@Nonnull
	JaxExternalFinanzielleSituation convertToJaxExternalFinanzielleSituationWithoutFinDaten(
		long fallNummer,
		@Nonnull LocalDate stichtag,
		@Nonnull Gesuch neustesGesuch,
		@Nonnull JaxExternalTarifart tarifart) {
		return new JaxExternalFinanzielleSituation(
			fallNummer,
			stichtag,
			antragstatusToScolaris(neustesGesuch.getStatus()),
			tarifart,
			rechnungsAdresseToScolaris(neustesGesuch, stichtag));
	}

	@Nonnull
	JaxExternalRechnungsAdresse rechnungsAdresseToScolaris(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDate stichtag
	) {
		final GesuchstellerContainer gesuchsteller1 = gesuch.getGesuchsteller1();
		Objects.requireNonNull(gesuchsteller1);
		final GesuchstellerAdresse rechnungsAdresse = gesuchsteller1.extractEffectiveRechnungsAdresse(stichtag);
		Objects.requireNonNull(rechnungsAdresse);

		return new JaxExternalRechnungsAdresse(
			gesuchsteller1.extractVorname(),
			gesuchsteller1.extractNachname(),
			rechnungsAdresse.getStrasse(),
			rechnungsAdresse.getHausnummer(),
			rechnungsAdresse.getZusatzzeile(),
			rechnungsAdresse.getPlz(),
			rechnungsAdresse.getOrt(),
			rechnungsAdresse.getLand().name(),
			rechnungsAdresse.getOrganisation());
	}
}
