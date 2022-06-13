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

package ch.dvbern.ebegu.api.resource.handler;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.dto.neskovanp.Veranlagungsstand;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
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
		SteuerdatenResponse steuerdatenResponse) {
		if (steuerdatenResponse.getVeranlagungsstand() == Veranlagungsstand.OFFEN) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
		} else if (steuerdatenResponse.getUnterjaehrigerFall() != null && steuerdatenResponse.getUnterjaehrigerFall()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL);
		} else if (steuerdatenResponse.getZpvNrPartner() != null) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_PARTNER_NICHT_GEMEINSAM);
		} else {
			setVeranlagungsstand(kibonAnfrageContext, steuerdatenResponse);
			KibonAnfrageHelper.updateFinSitSteuerdatenAbfrageStatusOk(kibonAnfrageContext.getFinSitCont()
				.getFinanzielleSituationJA(), steuerdatenResponse);
		}
	}

	public static void handleSteuerdatenGemeinsamResponse(
		KibonAnfrageContext kibonAnfrageContext,
		SteuerdatenResponse steuerdatenResponse) {
		if (steuerdatenResponse.getVeranlagungsstand() == Veranlagungsstand.OFFEN) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED);
		} else if (steuerdatenResponse.getUnterjaehrigerFall() != null && steuerdatenResponse.getUnterjaehrigerFall()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL);
		} else if (steuerdatenResponse.getVeraendertePartnerschaft() != null
			&& steuerdatenResponse.getVeraendertePartnerschaft()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_VERAENDERTE_PARTNERSCHAFT);
		} else if (steuerdatenResponse.getUnregelmaessigkeitInDerVeranlagung() != null
			&& steuerdatenResponse.getUnregelmaessigkeitInDerVeranlagung()) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_UNREGELMAESSIGKEIT);
		} else if (steuerdatenResponse.getZpvNrPartner() == null) {
			kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_KEIN_PARTNER_GEMEINSAM);
		} else {
			assert kibonAnfrageContext.getFinSitContGS2() != null;
			if (!KibonAnfrageHelper.isGebrutsdatumGS2CorrectInResponse(
				kibonAnfrageContext.getFinSitContGS2(),
				steuerdatenResponse)) {
				kibonAnfrageContext.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_GEBURTSDATUM);
			} else {
				setVeranlagungsstand(kibonAnfrageContext, steuerdatenResponse);
				KibonAnfrageHelper.updateFinSitSteuerdatenAbfrageGemeinsamStatusOk(
					kibonAnfrageContext.getFinSitCont().getFinanzielleSituationJA(),
					kibonAnfrageContext.getFinSitContGS2().getFinanzielleSituationJA(),
					steuerdatenResponse);
			}
		}
	}

	protected static void updateFinSitSteuerdatenAbfrageStatusOk(
		FinanzielleSituation finSit,
		SteuerdatenResponse steuerdatenResponse) {
		assert steuerdatenResponse.getZpvNrPartner() == null;
		finSit.setSteuerdatenResponse(steuerdatenResponse);
		setValuesFromDossiertraegerToFinSit(finSit, steuerdatenResponse, BigDecimal.ONE);
	}

	public static void updateFinSitSteuerdatenAbfrageStatus(
		FinanzielleSituation finSitGS1,
		@Nullable SteuerdatenAnfrageStatus steuerdatenAnfrageStatus) {
		finSitGS1.setSteuerdatenAbfrageStatus(steuerdatenAnfrageStatus);
	}

	protected static boolean isGebrutsdatumGS2CorrectInResponse(
		FinanzielleSituationContainer finanzielleSituationContainer,
		SteuerdatenResponse steuerdatenResponse) {

		LocalDate geburstdatumGS2 =
			finanzielleSituationContainer.getGesuchsteller().getGesuchstellerJA().getGeburtsdatum();

		if (isGS2Dossiertraeger(steuerdatenResponse)) {
			return geburstdatumGS2.compareTo(
				requireNonNull(steuerdatenResponse.getGeburtsdatumDossiertraeger())) == 0;
		}

		return geburstdatumGS2.compareTo(
			requireNonNull(steuerdatenResponse.getGeburtsdatumPartner())) == 0;
	}

	protected static void updateFinSitSteuerdatenAbfrageGemeinsamStatusOk(
		@Nonnull FinanzielleSituation finSitGS1,
		@Nonnull FinanzielleSituation finSitGS2,
		SteuerdatenResponse steuerdatenResponse) {
		assert steuerdatenResponse.getZpvNrPartner() != null;
		finSitGS1.setSteuerdatenResponse(steuerdatenResponse);
		finSitGS2.setSteuerdatenResponse(steuerdatenResponse);
		if (isAntragstellerDossiertraeger(steuerdatenResponse)) {
			//GS1 = Dossierträger GS2 = Partner
			setValuesFromDossiertraegerToFinSit(finSitGS1, steuerdatenResponse, BIG_DECIMAL_TWO);
			setValuesFromPartnerToFinSit(finSitGS2, steuerdatenResponse);
		} else {
			//GS2 = Dossierträger GS1 = Partner
			setValuesFromPartnerToFinSit(finSitGS1, steuerdatenResponse);
			setValuesFromDossiertraegerToFinSit(finSitGS2, steuerdatenResponse, BIG_DECIMAL_TWO);
		}
	}

	private static boolean isGS2Dossiertraeger(SteuerdatenResponse steuerdatenResponse) {
		return !isAntragstellerDossiertraeger(steuerdatenResponse);
	}

	protected static void setValuesFromPartnerToFinSit(
		FinanzielleSituation finSit,
		SteuerdatenResponse steuerdatenResponse) {
		finSit.setNettolohn(getPositvValueOrZero(steuerdatenResponse.getErwerbseinkommenUnselbstaendigkeitPartner()));
		finSit.setFamilienzulage(getPositvValueOrZero(steuerdatenResponse.getWeitereSteuerbareEinkuenftePartner()));
		finSit.setErsatzeinkommen(getPositvValueOrZero(steuerdatenResponse.getSteuerpflichtigesErsatzeinkommenPartner()));
		finSit.setErhalteneAlimente(getPositvValueOrZero(steuerdatenResponse.getErhalteneUnterhaltsbeitraegePartner()));
		finSit.setNettoertraegeErbengemeinschaft(getValueOrZero(steuerdatenResponse.getNettoertraegeAusEgmePartner()));

		if (steuerdatenResponse.getAusgewiesenerGeschaeftsertragPartner() != null) {
			finSit.setGeschaeftsgewinnBasisjahr(steuerdatenResponse.getAusgewiesenerGeschaeftsertragPartner());
			finSit.setGeschaeftsgewinnBasisjahrMinus1(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiodePartner());
			finSit.setGeschaeftsgewinnBasisjahrMinus2(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiode2Partner());
		}
		setBerechneteFelder(finSit, steuerdatenResponse, BIG_DECIMAL_TWO);
	}

	protected static void setValuesFromDossiertraegerToFinSit(
		FinanzielleSituation finSit,
		SteuerdatenResponse steuerdatenResponse,
		BigDecimal anzahlGesuchsteller) {

		// Pflichtfeldern wenn null muessen zu 0 gesetzt werden, Sie sind nicht editierbar im Formular
		finSit.setNettolohn(getPositvValueOrZero(steuerdatenResponse.getErwerbseinkommenUnselbstaendigkeitDossiertraeger()));
		finSit.setFamilienzulage(getPositvValueOrZero(steuerdatenResponse.getWeitereSteuerbareEinkuenfteDossiertraeger()));
		finSit.setErsatzeinkommen(getPositvValueOrZero(steuerdatenResponse.getSteuerpflichtigesErsatzeinkommenDossiertraeger()));
		finSit.setErhalteneAlimente(getPositvValueOrZero(steuerdatenResponse.getErhalteneUnterhaltsbeitraegeDossiertraeger()));
		finSit.setNettoertraegeErbengemeinschaft(getValueOrZero(steuerdatenResponse.getNettoertraegeAusEgmeDossiertraeger()));

		if (steuerdatenResponse.getAusgewiesenerGeschaeftsertragDossiertraeger() != null) {
			finSit.setGeschaeftsgewinnBasisjahr(steuerdatenResponse.getAusgewiesenerGeschaeftsertragDossiertraeger());
			finSit.setGeschaeftsgewinnBasisjahrMinus1(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiodeDossiertraeger());
			finSit.setGeschaeftsgewinnBasisjahrMinus2(steuerdatenResponse.getAusgewiesenerGeschaeftsertragVorperiode2Dossiertraeger());
		}
		setBerechneteFelder(finSit, steuerdatenResponse, anzahlGesuchsteller);
	}

	protected static boolean isAntragstellerDossiertraeger(SteuerdatenResponse steuerdatenResponse) {
		assert steuerdatenResponse.getZpvNrAntragsteller() != null;
		return steuerdatenResponse.getZpvNrAntragsteller().equals(steuerdatenResponse.getZpvNrDossiertraeger());
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
