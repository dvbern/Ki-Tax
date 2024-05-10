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

package ch.dvbern.ebegu.util;

import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfo;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungInfoContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;

import java.math.BigDecimal;

public final class TestUtils {

	/**
	 * Stellt alle für die Berechnung benötigten Parameter zusammen
	 */
	public static BGRechnerParameterDTO getParameter() {
		BGRechnerParameterDTO parameterDTO = new BGRechnerParameterDTO();
		parameterDTO.setMaxVerguenstigungVorschuleBabyProTg(MathUtil.GANZZAHL.from(150));
		parameterDTO.setMaxVerguenstigungVorschuleKindProTg(MathUtil.GANZZAHL.from(100));
		parameterDTO.setMaxVerguenstigungKindergartenKindProTg(MathUtil.GANZZAHL.from(75));
		parameterDTO.setMaxVerguenstigungVorschuleBabyProStd(MathUtil.DEFAULT.from(12.75));
		parameterDTO.setMaxVerguenstigungVorschuleKindProStd(MathUtil.DEFAULT.from(8.50));
		parameterDTO.setMaxVerguenstigungKindergartenKindProStd(MathUtil.DEFAULT.from(8.50));
		parameterDTO.setMaxVerguenstigungPrimarschuleKindProStd(MathUtil.DEFAULT.from(8.50));
		parameterDTO.setMaxMassgebendesEinkommen(MathUtil.GANZZAHL.from(160000));
		parameterDTO.setMinMassgebendesEinkommen(MathUtil.GANZZAHL.from(43000));
		parameterDTO.setOeffnungstageKita(MathUtil.GANZZAHL.from(240));
		parameterDTO.setOeffnungstageTFO(MathUtil.GANZZAHL.from(240));
		parameterDTO.setOeffnungsstundenTFO(MathUtil.GANZZAHL.from(11));
		parameterDTO.setZuschlagBehinderungProTg(MathUtil.GANZZAHL.from(50));
		parameterDTO.setZuschlagBehinderungProStd(MathUtil.DEFAULT.from(4.25));
		parameterDTO.setMinVerguenstigungProTg(MathUtil.GANZZAHL.from(7));
		parameterDTO.setMinVerguenstigungProStd(MathUtil.DEFAULT.from(0.70));
		parameterDTO.setMaxTarifTagesschuleMitPaedagogischerBetreuung(MathUtil.DEFAULT.from(12.24));
		parameterDTO.setMaxTarifTagesschuleOhnePaedagogischerBetreuung(MathUtil.DEFAULT.from(6.11));
		parameterDTO.setMinTarifTagesschule(MathUtil.DEFAULT.from(0.78));
		parameterDTO.getGemeindeParameter().setGemeindeZusaetzlicherGutscheinEnabled(false);
		parameterDTO.getGemeindeParameter().setGemeindeZusaetzlicherBabyGutscheinEnabled(false);
		parameterDTO.getMahlzeitenverguenstigungParameter().setEnabled(false);
		parameterDTO.getGemeindeParameter().setGemeindePauschalbetragEnabled(false);
		return parameterDTO;
	}


	public static void prepareEKVInfoTwoYears(Gesuch gesuch) {
		EinkommensverschlechterungInfoContainer ekvInfoContainer = new EinkommensverschlechterungInfoContainer();
		EinkommensverschlechterungInfo ekvInfo = new EinkommensverschlechterungInfo();
		ekvInfo.setEinkommensverschlechterung(true);
		ekvInfo.setEkvFuerBasisJahrPlus1(true);
		ekvInfo.setEkvFuerBasisJahrPlus2(true);
		ekvInfoContainer.setEinkommensverschlechterungInfoJA(ekvInfo);
		gesuch.setEinkommensverschlechterungInfoContainer(ekvInfoContainer);
	}

	public static void prepareEKVTwoYears(GesuchstellerContainer gesuchsteller1) {
		EinkommensverschlechterungContainer ekvContainer = new EinkommensverschlechterungContainer();

		Einkommensverschlechterung ekv = new Einkommensverschlechterung();
		Einkommensverschlechterung ekv2 = new Einkommensverschlechterung();

		ekvContainer.setEkvJABasisJahrPlus1(ekv);
		ekvContainer.setEkvJABasisJahrPlus2(ekv2);

		gesuchsteller1.setEinkommensverschlechterungContainer(ekvContainer);
	}

	/**
	 * Stellt alle für die Berechnung benötigten Parameter für Luzern zusammen
	 */
	public static BGRechnerParameterDTO getRechnerParameterLuzern() {
		BGRechnerParameterDTO defaultParameter = getParameter();

		//SET Parameters for LU
		defaultParameter.setMinVerguenstigungProTg(BigDecimal.valueOf(15));
		defaultParameter.setMinMassgebendesEinkommen(BigDecimal.valueOf(48000));
		defaultParameter.setMaxMassgebendesEinkommen(BigDecimal.valueOf(125000));
		defaultParameter.setMinVerguenstigungProStd(BigDecimal.valueOf(0.7));
		defaultParameter.setOeffnungstageKita(BigDecimal.valueOf(246));
		defaultParameter.setOeffnungstageTFO(BigDecimal.valueOf(246));
		defaultParameter.setOeffnungsstundenTFO(BigDecimal.valueOf(11));
		defaultParameter.setMaxVerguenstigungVorschuleKindProTg(BigDecimal.valueOf(130));
		defaultParameter.setMaxVerguenstigungVorschuleBabyProTg(BigDecimal.valueOf(160));
		defaultParameter.setMaxVerguenstigungVorschuleBabyProStd(BigDecimal.valueOf(16.3));
		defaultParameter.setMaxVerguenstigungVorschuleKindProStd(BigDecimal.valueOf(12.4));
		return defaultParameter;
	}

	public static BGRechnerParameterDTO getRechnerParamterAppenzell() {
		BGRechnerParameterDTO defaultParameter = getParameter();

		//SET Parameters for AR
		defaultParameter.setMaxVerguenstigungVorschuleBabyProStd(BigDecimal.valueOf(13.50));
		defaultParameter.setMaxVerguenstigungVorschuleKindProStd(BigDecimal.valueOf(11.50));
		defaultParameter.setOeffnungstageKita(BigDecimal.valueOf(240));

		return defaultParameter;
	}
	public static BGRechnerParameterDTO getRechnerParamterSchwyz() {
		BGRechnerParameterDTO defaultParameter = getParameter();

		defaultParameter.setOeffnungstageKita(BigDecimal.valueOf(246));
		defaultParameter.setMinMassgebendesEinkommen(BigDecimal.valueOf(47_193));
		defaultParameter.setMaxMassgebendesEinkommen(BigDecimal.valueOf(153_215));
		defaultParameter.setMinVerguenstigungProTg(BigDecimal.valueOf(30));
		defaultParameter.setMaxVerguenstigungVorschuleBabyProTg(BigDecimal.valueOf(185));
		defaultParameter.setMaxVerguenstigungVorschuleKindProTg(BigDecimal.valueOf(130));
		defaultParameter.setOeffnungsstundenTFO(BigDecimal.valueOf(10));
		defaultParameter.setOeffnungstageTFO(BigDecimal.valueOf(246));
		defaultParameter.setMinVerguenstigungProStd(BigDecimal.valueOf(3));
		defaultParameter.setMaxVerguenstigungVorschuleBabyProStd(BigDecimal.valueOf(12));
		return defaultParameter;
	}
}
