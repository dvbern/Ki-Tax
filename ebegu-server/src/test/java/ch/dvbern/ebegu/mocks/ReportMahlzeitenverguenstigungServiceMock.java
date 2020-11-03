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

package ch.dvbern.ebegu.mocks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.enums.BetreuungsangebotTyp;
import ch.dvbern.ebegu.reporting.mahlzeiten.MahlzeitenverguenstigungDataRow;
import ch.dvbern.ebegu.services.reporting.ReportMahlzeitenServiceBean;

public class ReportMahlzeitenverguenstigungServiceMock extends ReportMahlzeitenServiceBean {

	@Nonnull
	@Override
	public List<MahlzeitenverguenstigungDataRow> getReportMahlzeitenverguenstigung(
		@Nonnull LocalDate datumVon,
		@Nonnull LocalDate datumBis
	) {

		List<MahlzeitenverguenstigungDataRow> dataRows = new ArrayList<>();
		dataRows.add(createDataRow("John", "20.007569.002.1.1", BetreuungsangebotTyp.KITA));
		dataRows.add(createDataRow("John", "20.007569.002.1.2", BetreuungsangebotTyp.TAGESSCHULE));
		dataRows.add(createDataRow("Jade", "20.007569.002.2.1", BetreuungsangebotTyp.KITA));
		dataRows.add(createDataRow("Petra", "20.007569.002.3.1", BetreuungsangebotTyp.KITA));
		return dataRows;
	}

	private MahlzeitenverguenstigungDataRow createDataRow(
		String kindVorname,
		String bgNummer,
		BetreuungsangebotTyp betreuungsangebotTyp) {
		MahlzeitenverguenstigungDataRow mahlzeitenverguenstigungDataRow = new MahlzeitenverguenstigungDataRow();
		mahlzeitenverguenstigungDataRow.setKostenNebenmahlzeiten(new BigDecimal(10));
		mahlzeitenverguenstigungDataRow.setKostenHauptmahlzeiten(new BigDecimal(30));
		mahlzeitenverguenstigungDataRow.setAnzahlHauptmahlzeiten(new BigDecimal(3));
		mahlzeitenverguenstigungDataRow.setAnzahlNebenmahlzeiten(new BigDecimal(2));
		mahlzeitenverguenstigungDataRow.setTraegerschaft("Bern Traegerschaft");
		mahlzeitenverguenstigungDataRow.setBerechneteMahlzeitenverguenstigung(new BigDecimal(25));
		mahlzeitenverguenstigungDataRow.setBetreuungsTyp(betreuungsangebotTyp);
		mahlzeitenverguenstigungDataRow.setBgNummer(bgNummer);
		mahlzeitenverguenstigungDataRow.setGs1Name("Thompson");
		mahlzeitenverguenstigungDataRow.setGs1Vorname("Jack");
		mahlzeitenverguenstigungDataRow.setGs2Name("Thompson");
		mahlzeitenverguenstigungDataRow.setGs2Vorname("Jane");
		mahlzeitenverguenstigungDataRow.setInstitution("Kita Bermuda");
		mahlzeitenverguenstigungDataRow.setKindName("Thompson");
		mahlzeitenverguenstigungDataRow.setKindVorname(kindVorname);
		mahlzeitenverguenstigungDataRow.setKindGeburtsdatum(LocalDate.now().minusYears(1));
		mahlzeitenverguenstigungDataRow.setZeitabschnittVon(LocalDate.now().minusMonths(1));
		mahlzeitenverguenstigungDataRow.setZeitabschnittBis(LocalDate.now().plusMonths(1));
		return mahlzeitenverguenstigungDataRow;
	}

}
