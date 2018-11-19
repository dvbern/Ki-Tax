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

package ch.dvbern.ebegu.tests;

import java.time.LocalDate;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import ch.dvbern.ebegu.entities.EbeguVorlage;
import ch.dvbern.ebegu.entities.Vorlage;
import ch.dvbern.ebegu.enums.EbeguVorlageKey;
import ch.dvbern.ebegu.services.EbeguVorlageServiceBean;
import ch.dvbern.ebegu.types.DateRange;

public class EbeguVorlageServiceMock extends EbeguVorlageServiceBean {

	@Nonnull
	@Override
	public Optional<EbeguVorlage> getEbeguVorlageByDatesAndKey(@Nonnull LocalDate abDate, @Nonnull LocalDate bisDate, @Nonnull EbeguVorlageKey ebeguVorlageKey, EntityManager em) {
		EbeguVorlage ebeguVorlage = new EbeguVorlage(ebeguVorlageKey, new DateRange(abDate, bisDate));
		Vorlage vorlage = new Vorlage();

		switch (ebeguVorlageKey) {
		case VORLAGE_MAHNUNG_1:
			vorlage.setFilepfad("vorlagen/1_Mahnung.docx");
			break;
		case VORLAGE_MAHNUNG_2:
			vorlage.setFilepfad("vorlagen/2_Mahnung.docx");
			break;
		case VORLAGE_NICHT_EINTRETENSVERFUEGUNG:
			vorlage.setFilepfad("vorlagen/Nichteintretensverfuegung.docx");
			break;
		case VORLAGE_INFOSCHREIBEN_MAXIMALTARIF:
			vorlage.setFilepfad("vorlagen/Infoschreiben_Maxtarif.docx");
			break;
		case VORLAGE_BEGLEITSCHREIBEN:
			vorlage.setFilepfad("vorlagen/Begleitschreiben.docx");
			break;
		case VORLAGE_FINANZIELLE_SITUATION:
			vorlage.setFilepfad("vorlagen/Berechnungsgrundlagen.docx");
			break;
		case VORLAGE_VERFUEGUNG_KITA:
			vorlage.setFilepfad("vorlagen/Verfuegungsmuster_kita.docx");
			break;
		case VORLAGE_VERFUEGUNG_TAGESFAMILIEN:
			vorlage.setFilepfad("vorlagen/Verfuegungsmuster_tageseltern_kleinkinder.docx");
			break;
		default:
			break;
		}

		ebeguVorlage.setVorlage(vorlage);
		return Optional.of(ebeguVorlage);
	}
}
