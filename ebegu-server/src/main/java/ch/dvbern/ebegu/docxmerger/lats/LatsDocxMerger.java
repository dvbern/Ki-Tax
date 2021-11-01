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

package ch.dvbern.ebegu.docxmerger.lats;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.docxmerger.DocxDocument;
import ch.dvbern.ebegu.docxmerger.DocxMerger;
import ch.dvbern.ebegu.docxmerger.mergefield.BigDecimalMergeField;
import ch.dvbern.ebegu.docxmerger.mergefield.StringMergeField;
import ch.dvbern.ebegu.util.Constants;

public class LatsDocxMerger extends DocxMerger<LatsDocxDTO> {

	public LatsDocxMerger(DocxDocument docxDocument) {
		super(docxDocument);
	}

	@Override
	public void addMergeFields(@Nonnull LatsDocxDTO dto) {
		this.mergeFields = new ArrayList<>();
		this.mergeFields.add(new StringMergeField("userName", dto.getUserName()));
		this.mergeFields.add(new StringMergeField("userEmail", dto.getUserEmail()));
		this.mergeFields.add(new StringMergeField("gemeindeAnschrift", dto.getGemeindeAnschrift()));
		this.mergeFields.add(new StringMergeField("gemeindeStrasse", dto.getGemeindeStrasse()));
		this.mergeFields.add(new StringMergeField("gemeindeNr", dto.getGemeindeNr()));
		this.mergeFields.add(new StringMergeField("gemeindePLZ", dto.getGemeindePLZ()));
		this.mergeFields.add(new StringMergeField("gemeindeOrt", dto.getGemeindeOrt()));
		this.mergeFields.add(new StringMergeField("gemeindeName", dto.getGemeindeName()));
		this.mergeFields.add(new StringMergeField("fallNummer", dto.getFallNummer()));
		this.mergeFields.add(new BigDecimalMergeField("betreuungsstunden", dto.getBetreuungsstunden(), Constants.NO_DECIMAL_PATTERN));
		this.mergeFields.add(new BigDecimalMergeField("betreuungsstundenProg", dto.getBetreuungsstundenProg(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new StringMergeField("normlohnkosten", dto.getNormlohnkosten()));
		this.mergeFields.add(new StringMergeField("normlohnkostenProg", dto.getNormlohnkostenProg()));
		this.mergeFields.add(new BigDecimalMergeField("normlohnkostenTotal", dto.getNormlohnkostenTotal(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new BigDecimalMergeField("normlohnkostenTotalProg", dto.getNormlohnkostenTotalProg(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new BigDecimalMergeField("elterngebuehren", dto.getElterngebuehren(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new BigDecimalMergeField("elterngebuehrenProg", dto.getElterngebuehrenProg(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new BigDecimalMergeField("lastenausgleichsberechtigterBetrag", dto.getLastenausgleichsberechtigterBetrag(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new BigDecimalMergeField("lastenausgleichsberechtigterBetragProg", dto.getLastenausgleichsberechtigterBetragProg(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new BigDecimalMergeField("ersteRate", dto.getErsteRate(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new BigDecimalMergeField("ersteRateProg", dto.getErsteRateProg(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new BigDecimalMergeField("zweiteRate", dto.getZweiteRate(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new BigDecimalMergeField("auszahlungTotal", dto.getAuszahlungTotal(), Constants.CURRENCY_PATTERN));
		this.mergeFields.add(new StringMergeField("textPaedagogischOderNicht", dto.getTextPaedagogischOderNicht()));
	}
}
