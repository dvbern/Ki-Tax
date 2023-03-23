/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.nesko.handler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.dto.neskovanp.Veranlagungsstand;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.entities.SteuerdatenResponse.SteuerdatenDatenTraeger;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import ch.dvbern.ebegu.util.MathUtil;

import static java.util.Objects.requireNonNull;

public class KibonAnfrageHelper {

	protected static final MathUtil GANZZAHL = MathUtil.GANZZAHL;
	private static final BigDecimal BIG_DECIMAL_TWO = new BigDecimal(2);

	/**
	 * handle steuerdatenResponse for single GS
	 */
	public static void handleSteuerdatenResponse(
			KibonAnfrageContext kibonAnfrageContext,
			SteuerdatenResponse steuerdatenResponse,
			int gesuchstellerNumber) {
		if (steuerdatenResponse.getVeranlagungsstand() == Veranlagungsstand.OFFEN) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
			return;
		}
		if (steuerdatenResponse.getUnterjaehrigerFall() != null && steuerdatenResponse.getUnterjaehrigerFall()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL);
			return;
		}
		if (steuerdatenResponse.getZpvNrPartner() != null) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_PARTNER_NICHT_GEMEINSAM);
			return;
		}
		setVeranlagungsstand(kibonAnfrageContext, steuerdatenResponse);
		KibonAnfrageHelper.updateFinSitSteuerdatenAbfrageStatusOk(kibonAnfrageContext.getFinSitCont(gesuchstellerNumber)
				.getFinanzielleSituationJA(), steuerdatenResponse);

	}

	public static void handleSteuerdatenGemeinsamResponse(
		KibonAnfrageContext kibonAnfrageContext,
		SteuerdatenResponse steuerdatenResponse) {
		if (steuerdatenResponse.getVeranlagungsstand() == Veranlagungsstand.OFFEN) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
			return;
		}
		if (steuerdatenResponse.getUnterjaehrigerFall() != null && steuerdatenResponse.getUnterjaehrigerFall()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL);
			return;
		}
		if (steuerdatenResponse.getVeraendertePartnerschaft() != null
				&& steuerdatenResponse.getVeraendertePartnerschaft()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_VERAENDERTE_PARTNERSCHAFT);
			return;
		}
		if (steuerdatenResponse.getUnregelmaessigkeitInDerVeranlagung() != null
			&& steuerdatenResponse.getUnregelmaessigkeitInDerVeranlagung()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_UNREGELMAESSIGKEIT);
			return;
		}
		if (steuerdatenResponse.getZpvNrPartner() == null) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_KEIN_PARTNER_GEMEINSAM);
			return;
		}
		if (!KibonAnfrageHelper.isGeburtsdatumPartnerCorrectInResponse(
				kibonAnfrageContext,
				steuerdatenResponse)) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_GEBURTSDATUM);
			return;
		}
		setVeranlagungsstand(kibonAnfrageContext, steuerdatenResponse);
		KibonAnfrageHelper.updateFinSitSteuerdatenAbfrageGemeinsamStatusOk(
				steuerdatenResponse, kibonAnfrageContext);

	}

	public static void updateFinSitSteuerdatenAbfrageStatusOk(
		FinanzielleSituation finSit,
		SteuerdatenResponse steuerdatenResponse) {
		assert steuerdatenResponse.getZpvNrPartner() == null;
		finSit.setSteuerdatenResponse(steuerdatenResponse);
		setValuesToFinSit(finSit, steuerdatenResponse, BigDecimal.ONE, SteuerdatenDatenTraeger.DOSSIERTRAEGER);
	}

	public static void updateFinSitSteuerdatenAbfrageStatus(
		FinanzielleSituation finSitGS,
		@Nullable SteuerdatenAnfrageStatus steuerdatenAnfrageStatus) {
		finSitGS.setSteuerdatenAbfrageStatus(steuerdatenAnfrageStatus);
		finSitGS.setSteuerdatenAbfrageTimestamp(LocalDateTime.now());
	}

	protected static boolean isGeburtsdatumPartnerCorrectInResponse(
		KibonAnfrageContext anfrageContext,
		SteuerdatenResponse steuerdatenResponse) {

		LocalDate geburtsdatumPartner;

		if (isGesuchstellerSteuerdossiertraeger(anfrageContext.getGesuch().getGesuchsteller1().getGesuchstellerJA(), steuerdatenResponse)) {
			geburtsdatumPartner = anfrageContext.getGesuch().getGesuchsteller2().getGesuchstellerJA().getGeburtsdatum();
		} else {
			geburtsdatumPartner = anfrageContext.getGesuch().getGesuchsteller1().getGesuchstellerJA().getGeburtsdatum();
		}

		return geburtsdatumPartner.isEqual(requireNonNull(steuerdatenResponse.getGeburtsdatumPartner()));
	}

	public static void updateFinSitSteuerdatenAbfrageGemeinsamStatusOk(
			SteuerdatenResponse steuerdatenResponse,
			KibonAnfrageContext anfrageContext) {
		FinanzielleSituation finSitGS1 = anfrageContext.getFinSitCont(1).getFinanzielleSituationJA();
		FinanzielleSituation finSitGS2 = anfrageContext.getFinSitCont(2).getFinanzielleSituationJA();
		assert steuerdatenResponse.getZpvNrPartner() != null;
		finSitGS1.setSteuerdatenResponse(steuerdatenResponse);
		finSitGS2.setSteuerdatenResponse(steuerdatenResponse);
		if (isGesuchstellerSteuerdossiertraeger(anfrageContext.getGesuch().getGesuchsteller1().getGesuchstellerJA(), steuerdatenResponse)) {
			//GS1 = Dossierträger GS2 = Partner
			setValuesToFinSit(finSitGS1, steuerdatenResponse, BIG_DECIMAL_TWO, SteuerdatenDatenTraeger.DOSSIERTRAEGER);
			setValuesToFinSit(finSitGS2, steuerdatenResponse, BIG_DECIMAL_TWO, SteuerdatenDatenTraeger.PARTNER);
		} else {
			//GS2 = Dossierträger GS1 = Partner
			setValuesToFinSit(finSitGS1, steuerdatenResponse, BIG_DECIMAL_TWO, SteuerdatenDatenTraeger.PARTNER);
			setValuesToFinSit(finSitGS2, steuerdatenResponse, BIG_DECIMAL_TWO, SteuerdatenDatenTraeger.DOSSIERTRAEGER);
		}
	}


	protected static void setValuesToFinSit(
		FinanzielleSituation finSit,
		SteuerdatenResponse steuerdatenResponse,
		BigDecimal anzahlGesuchsteller,
		SteuerdatenDatenTraeger traeger) {

		// Pflichtfeldern wenn null muessen zu 0 gesetzt werden, Sie sind nicht editierbar im Formular
		finSit.setNettolohn(getPositvValueOrZero(steuerdatenResponse.getErwerbseinkommenUnselbstaendigkeit(traeger)));
		finSit.setFamilienzulage(getPositvValueOrZero(steuerdatenResponse.getWeitereSteuerbareEinkuenfte(traeger)));
		finSit.setErsatzeinkommen(getPositvValueOrZero(steuerdatenResponse.getSteuerpflichtigesErsatzeinkommen(traeger)));
		finSit.setErhalteneAlimente(getPositvValueOrZero(steuerdatenResponse.getErhalteneUnterhaltsbeitraege(traeger)));
		finSit.setNettoertraegeErbengemeinschaft(getValueOrZero(steuerdatenResponse.getNettoertraegeAusEgme(traeger)));

		if (steuerdatenResponse.getAusgewiesenerGeschaeftsertrag(traeger) != null) {
			finSit.setGeschaeftsgewinnBasisjahr(steuerdatenResponse.getAusgewiesenerGeschaeftsertrag(traeger));
			finSit.setGeschaeftsgewinnBasisjahrMinus1(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiode(traeger));
			finSit.setGeschaeftsgewinnBasisjahrMinus2(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiode2(traeger));
		} else {
			finSit.setGeschaeftsgewinnBasisjahr(null);
			finSit.setGeschaeftsgewinnBasisjahrMinus1(null);
			finSit.setGeschaeftsgewinnBasisjahrMinus2(null);
		}

		setBerechneteFelder(finSit, steuerdatenResponse, anzahlGesuchsteller);
	}

	public static boolean isGesuchstellerSteuerdossiertraeger(Gesuchsteller gesuchsteller, SteuerdatenResponse steuerdatenResponse) {
		assert steuerdatenResponse.getGeburtsdatumDossiertraeger() != null;
		return gesuchsteller.getGeburtsdatum().equals(steuerdatenResponse.getGeburtsdatumDossiertraeger());
	}

	protected static void setVeranlagungsstand(
		KibonAnfrageContext kibonAnfrageContext,
		SteuerdatenResponse steuerdatenResponse) {
		if (steuerdatenResponse.getVeranlagungsstand() != null) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(
				SteuerdatenAnfrageStatus.valueOf(steuerdatenResponse.getVeranlagungsstand().name()));
		}
	}

	protected static void setBerechneteFelder(
		FinanzielleSituation finSit,
		SteuerdatenResponse steuerdatenResponse,
		@NotNull BigDecimal anzahlGesuchsteller) {
		// Berechnete Feldern - diese können null bleiben als Sie sind editierbar im Formular
		BigDecimal bruttertraegeVermogenTotal =
			GANZZAHL.addNullSafe(
				getPositvValueOrZero(steuerdatenResponse.getBruttoertraegeAusLiegenschaften()),
				steuerdatenResponse.getBruttoertraegeAusVermoegenOhneLiegenschaftenUndOhneEgme());
		BigDecimal gewinnungskostenTotal =
			GANZZAHL.addNullSafe(
				getPositvValueOrZero(steuerdatenResponse.getGewinnungskostenBeweglichesVermoegen()),
				steuerdatenResponse.getLiegenschaftsAbzuege());

		finSit.setBruttoertraegeVermoegen(divideByAnzahlGesuchsteller(
			bruttertraegeVermogenTotal,
			anzahlGesuchsteller, false));
		finSit.setAbzugSchuldzinsen(divideByAnzahlGesuchsteller(
			steuerdatenResponse.getSchuldzinsen(),
			anzahlGesuchsteller, false));
		finSit.setGewinnungskosten(divideByAnzahlGesuchsteller(gewinnungskostenTotal, anzahlGesuchsteller, false));
		finSit.setGeleisteteAlimente(divideByAnzahlGesuchsteller(
			steuerdatenResponse.getGeleisteteUnterhaltsbeitraege(),
			anzahlGesuchsteller, false));
		finSit.setNettoVermoegen(divideByAnzahlGesuchsteller(
			steuerdatenResponse.getNettovermoegen(),
			anzahlGesuchsteller, true));
	}

	private static BigDecimal divideByAnzahlGesuchsteller(
		@Nullable BigDecimal value,
		@NotNull BigDecimal anzahlGesuchsteller,
		@NotNull boolean allowNegative) {
		assert anzahlGesuchsteller.compareTo(BigDecimal.ZERO) != 0;
		return GANZZAHL.divide(
			allowNegative ? getValueOrZero(value) : getPositvValueOrZero(value),
			anzahlGesuchsteller);

	}

	private static BigDecimal getPositvValueOrZero(@Nullable BigDecimal value) {
		if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
			return BigDecimal.ZERO;
		}

		return value;
	}

	private static BigDecimal getValueOrZero(@Nullable BigDecimal value) {
		if (value == null) {
			return BigDecimal.ZERO;
		}
		return value;
	}
}
