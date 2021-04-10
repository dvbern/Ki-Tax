/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.pdfgenerator;

import ch.dvbern.lib.invoicegenerator.dto.BaseLayoutConfiguration;
import ch.dvbern.lib.invoicegenerator.dto.OnPage;
import ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent;

import static ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent.ADRESSE_HEIGHT;
import static ch.dvbern.lib.invoicegenerator.dto.component.AddressComponent.ADRESSE_WIDTH;

public class VollmachtPdfLayoutConfiguration extends BaseLayoutConfiguration {

	private static final int TOP_PAGE_MARGIN = 98;

	public VollmachtPdfLayoutConfiguration() {
		// we don't need this address here.
		super(new AddressComponent(
			null,
			0,
			0,
			ADRESSE_WIDTH,
			ADRESSE_HEIGHT,
			OnPage.FIRST));

		setMargins(
			LEFT_PAGE_DEFAULT_MARGIN_MM,
			LEFT_PAGE_DEFAULT_MARGIN_MM,
			TOP_PAGE_MARGIN,
			BOTTOM_PAGE_DEFAULT_MARGIN_MM
		);
	}
}
