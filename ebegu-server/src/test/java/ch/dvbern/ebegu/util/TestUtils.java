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

import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;

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
}
