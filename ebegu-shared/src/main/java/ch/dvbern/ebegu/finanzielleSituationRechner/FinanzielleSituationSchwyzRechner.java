/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.finanzielleSituationRechner;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.FinanzDatenDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.util.MathUtil;
import org.apache.commons.lang.NotImplementedException;

public class FinanzielleSituationSchwyzRechner extends AbstractFinanzielleSituationRechner {

	@Override
	public void setFinanzielleSituationParameters(
		@Nonnull Gesuch gesuch,
		FinanzielleSituationResultateDTO finSitResultDTO,
		boolean hasSecondGesuchsteller
	) {
		FinanzielleSituation finanzielleSituationGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());

		FinanzielleSituation finanzielleSituationGS2 = hasSecondGesuchsteller && gesuch.getGesuchsteller2() != null ?
			getFinanzielleSituationGS(gesuch.getGesuchsteller2()) :
			null;

		calculateAbstractFinSit(
			finSitResultDTO,
			finanzielleSituationGS1,
			finanzielleSituationGS1 != null && Boolean.TRUE.equals(finanzielleSituationGS1.getQuellenbesteuert()),
			finanzielleSituationGS2,
			finanzielleSituationGS2 != null && Boolean.TRUE.equals(finanzielleSituationGS2.getQuellenbesteuert()));
	}

	@Override
	public void setEinkommensverschlechterungParameters(
		@Nonnull Gesuch gesuch,
		int basisJahrPlus,
		FinanzielleSituationResultateDTO einkVerResultDTO,
		boolean hasSecondGesuchsteller
	) {
		Einkommensverschlechterung ekvGS1 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller1(), 1);
		FinanzielleSituation finSitGS1 = getFinanzielleSituationGS(gesuch.getGesuchsteller1());

		Einkommensverschlechterung ekvGS2 = null;
		FinanzielleSituation finSitGS2 = null;
		if (hasSecondGesuchsteller && gesuch.getGesuchsteller2() != null) {
			ekvGS2 = getEinkommensverschlechterungGS(gesuch.getGesuchsteller2(), 1);
			finSitGS2 = getFinanzielleSituationGS(gesuch.getGesuchsteller2());
		}

		calculateAbstractFinSit(
			einkVerResultDTO,
			ekvGS1,
			finSitGS1 != null && Boolean.TRUE.equals(finSitGS1.getQuellenbesteuert()),
			ekvGS2,
			finSitGS2 != null && Boolean.TRUE.equals(finSitGS2.getQuellenbesteuert()));
	}

	@SuppressWarnings("PMD.UnusedPrivateMethod")
	private void calculateAbstractFinSit(
		@Nonnull FinanzielleSituationResultateDTO finSitResultDTO,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS1,
		boolean gs1Quellenbesteuert,
		@Nullable AbstractFinanzielleSituation finanzielleSituationGS2,
		boolean gs2Quellenbesteuert
	) {
		if (finanzielleSituationGS1 != null) {
			var einkommen1 = gs1Quellenbesteuert ?
				calculateForQuellenBesteuerte(finanzielleSituationGS1) :
				calculateForNichtQuellenBesteuerte(finanzielleSituationGS1);
			finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS1(einkommen1);
		}
		if (finanzielleSituationGS2 != null) {
			var einkommen2 = gs2Quellenbesteuert ?
				calculateForQuellenBesteuerte(finanzielleSituationGS2) :
				calculateForNichtQuellenBesteuerte(finanzielleSituationGS2);
			finSitResultDTO.setMassgebendesEinkVorAbzFamGrGS2(einkommen2);
		}
		finSitResultDTO.setMassgebendesEinkVorAbzFamGr(
			add(finSitResultDTO.getMassgebendesEinkVorAbzFamGrGS1(), finSitResultDTO.getMassgebendesEinkVorAbzFamGrGS2())
		);
	}

	private BigDecimal calculateForNichtQuellenBesteuerte(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		return calculateMassgebendesEinkommen(
			calcEinkommen(finanzielleSituation),
			calcEinkaeufeVorsorge(finanzielleSituation),
			calcAbzuegeLiegenschaftsaufwand(finanzielleSituation),
			calcReinvermoegenNachAbzug(finanzielleSituation)
		);
	}

	@Nonnull
	private BigDecimal calcEinkommen(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		return MathUtil.positiveNonNullAndRound(finanzielleSituation.getSteuerbaresEinkommen());
	}

	@Nonnull
	private BigDecimal calcEinkaeufeVorsorge(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		return MathUtil.positiveNonNullAndRound(finanzielleSituation.getEinkaeufeVorsorge());
	}

	@Nonnull
	private BigDecimal calcAbzuegeLiegenschaftsaufwand(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		return MathUtil.positiveNonNullAndRound(finanzielleSituation.getAbzuegeLiegenschaft());
	}

	@Nonnull
	private BigDecimal calcReinvermoegenNachAbzug(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		var reinvermoegenMitAbzug = subtract(finanzielleSituation.getSteuerbaresVermoegen(), new BigDecimal(200_000));

		return BigDecimal.ZERO.compareTo(reinvermoegenMitAbzug) < 0 ?
			MathUtil.positiveNonNullAndRound(percent(reinvermoegenMitAbzug, 10)) :
			BigDecimal.ZERO;
	}

	private BigDecimal calculateMassgebendesEinkommen(
		@Nonnull BigDecimal einkommen,
		@Nonnull BigDecimal einkaufBeruflicheVorsorge,
		@Nonnull BigDecimal abzuegeLiegenschaftsaufwand,
		@Nonnull BigDecimal reinvermoegenNachAbzug
	) {
		BigDecimal anrechenbaresEinkommen = einkommen
			.add(einkaufBeruflicheVorsorge)
			.add(abzuegeLiegenschaftsaufwand)
			.add(reinvermoegenNachAbzug);

		return MathUtil.positiveNonNullAndRound(anrechenbaresEinkommen);
	}

	private BigDecimal calculateForQuellenBesteuerte(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		var bruttoeinkommen = calcBruttoeinkommen(finanzielleSituation);
		var bruttopauschale = calcBruttopauschale(finanzielleSituation);

		return calculateMassgebendesEinkommen(bruttoeinkommen, bruttopauschale);
	}

	@Nonnull
	private BigDecimal calcBruttoeinkommen(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		return MathUtil.positiveNonNullAndRound(finanzielleSituation.getBruttoLohn());
	}

	@Nonnull
	private BigDecimal calcBruttopauschale(@Nonnull AbstractFinanzielleSituation finanzielleSituation) {
		return MathUtil.positiveNonNullAndRound(percent(finanzielleSituation.getBruttoLohn(), 20));
	}

	private BigDecimal calculateMassgebendesEinkommen(@Nonnull BigDecimal bruttoeinkommen, @Nonnull BigDecimal bruttopauschale) {
		BigDecimal anrechenbaresEinkommen = subtract(bruttoeinkommen, bruttopauschale);
		return MathUtil.positiveNonNullAndRound(anrechenbaresEinkommen);
	}

	@Override
	public boolean calculateByVeranlagung(@Nonnull AbstractFinanzielleSituation abstractFinanzielleSituation) {
		// bei Schwyz rechnen wir nie nach Veranlagung.
		throw new NotImplementedException();
	}

	@Override
	public void calculateFinanzDaten(@Nonnull Gesuch gesuch, BigDecimal minimumEKV) {
		super.calculateFinanzDaten(gesuch, minimumEKV);
		copyEKV1ResultateToEKV2(gesuch.getFinanzDatenDTO_alleine());
		copyEKV1ResultateToEKV2(gesuch.getFinanzDatenDTO_zuZweit());
	}

	private static void copyEKV1ResultateToEKV2(FinanzDatenDTO finanzDatenDTOAlleine) {
		finanzDatenDTOAlleine.setEkv2Accepted(finanzDatenDTOAlleine.isEkv1Accepted());
		finanzDatenDTOAlleine.setEkv2Annulliert(finanzDatenDTOAlleine.isEkv1Annulliert());
		finanzDatenDTOAlleine.setEkv2Erfasst(finanzDatenDTOAlleine.isEkv1Erfasst());
	}
}
