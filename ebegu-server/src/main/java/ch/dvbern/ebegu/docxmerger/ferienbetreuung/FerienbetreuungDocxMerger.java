/*
 * Copyright (C) 2021 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.docxmerger.ferienbetreuung;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.docxmerger.DocxDocument;
import ch.dvbern.ebegu.docxmerger.DocxMerger;
import ch.dvbern.ebegu.docxmerger.mergefield.BigDecimalMergeField;
import ch.dvbern.ebegu.docxmerger.mergefield.StringMergeField;
import ch.dvbern.ebegu.util.Constants;

public class FerienbetreuungDocxMerger extends DocxMerger<FerienbetreuungDocxDTO> {

	public FerienbetreuungDocxMerger(DocxDocument docxDocument) {
		super(docxDocument);
	}

	@Override
	public void addMergeFields(@Nonnull FerienbetreuungDocxDTO dto) {
		this.mergeFields = new ArrayList<>();
		this.mergeFields.add(new StringMergeField("userName", dto.getUserName()));
		this.mergeFields.add(new StringMergeField("userEmail", dto.getUserEmail()));
		this.mergeFields.add(new StringMergeField("gemeindeAnschrift", dto.getGemeindeAnschrift()));
		this.mergeFields.add(new StringMergeField("gemeindeStrasse", dto.getGemeindeStrasse()));
		this.mergeFields.add(new StringMergeField("gemeindeNr", dto.getGemeindeNr()));
		this.mergeFields.add(new StringMergeField("gemeindePLZ", dto.getGemeindePLZ()));
		this.mergeFields.add(new StringMergeField("gemeindeOrt", dto.getGemeindeOrt()));
		this.mergeFields.add(new StringMergeField("fallNummer", dto.getFallNummer()));
		this.mergeFields.add(new StringMergeField("periode", dto.getPeriode()));
		this.mergeFields.add(new StringMergeField("angebot", dto.getAngebot()));
		this.mergeFields.add(new StringMergeField("traegerschaft", dto.getTraegerschaft()));
		this.mergeFields.add(new BigDecimalMergeField(
			"tageSonderschueler",
			dto.getTageSonderschueler(),
			Constants.NO_DECIMAL_PATTERN
		));
		this.mergeFields.add(new BigDecimalMergeField(
			"chfSonderschueler",
			dto.getChfSonderschueler(),
			Constants.CURRENCY_PATTERN
		));
		this.mergeFields.add(new BigDecimalMergeField(
			"tageOhneSonderschueler",
			dto.getTageOhneSonderschueler(),
			Constants.NO_DECIMAL_PATTERN
		));
		this.mergeFields.add(new BigDecimalMergeField(
			"chfOhneSonderschueler",
			dto.getChfOhneSonderschueler(),
			Constants.CURRENCY_PATTERN
		));
		this.mergeFields.add(new BigDecimalMergeField(
			"totalTage",
			dto.getTotalTage(),
			Constants.NO_DECIMAL_PATTERN
		));
		this.mergeFields.add(new BigDecimalMergeField(
			"totalChf",
			dto.getTotalChf(),
			Constants.CURRENCY_PATTERN
		));
		this.mergeFields.add(new StringMergeField("iban", dto.getIban()));
		this.mergeFields.add(new StringMergeField("folgePeriode", dto.getFolgePeriode()));
	}
}
