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

package ch.dvbern.ebegu.pdfgenerator.finanzielleSituation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.lowagie.text.Rectangle;
import lombok.Value;

@Value
public class MassgebendesEinkommenTabelleConfig {

	Rectangle pageSize;
	List<MassgebendesEinkommenColumn> columns;

	public static MassgebendesEinkommenTabelleConfig of(
		Rectangle pageSize,
		MassgebendesEinkommenColumn... columns
	) {
		return new MassgebendesEinkommenTabelleConfig(pageSize, Arrays.stream(columns).collect(Collectors.toUnmodifiableList()));
	}
}
