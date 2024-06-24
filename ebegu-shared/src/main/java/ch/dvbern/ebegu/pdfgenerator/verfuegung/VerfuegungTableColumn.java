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

package ch.dvbern.ebegu.pdfgenerator.verfuegung;

import java.awt.Color;
import java.util.function.Function;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import com.lowagie.text.Element;
import lombok.Builder;
import lombok.NonNull;

@Builder
public class VerfuegungTableColumn {

	float width;
	@NonNull
	String title;
	@Builder.Default
	String romanNumber = "";
	@Builder.Default
	int headerAlignment = Element.ALIGN_RIGHT;
	@Builder.Default
	int contentAlignment = Element.ALIGN_RIGHT;
	@Builder.Default
	Color bgColor = Color.WHITE;
	@Builder.Default
	boolean boldContent = false;
	@NonNull
	Function<VerfuegungZeitabschnitt, String> dataExtractor;
}
