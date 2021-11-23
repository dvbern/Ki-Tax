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

package ch.dvbern.ebegu.util;

import java.io.Serializable;
import java.util.Comparator;

import ch.dvbern.ebegu.dto.VerfuegungsBemerkungDTO;

/**
 * Comparator, Verfuegungsbemerkungen sortiert
 */
public class VerfuegungsBemerkungComparator implements Comparator<VerfuegungsBemerkungDTO>, Serializable {

	private static final long serialVersionUID = -309383917391346314L;

	@Override
	public int compare(VerfuegungsBemerkungDTO bemerkung1, VerfuegungsBemerkungDTO bemerkung2) {
		int ord1 = bemerkung1.getMsgKey().ordinal();
		int ord2 = bemerkung2.getMsgKey().ordinal();
		return Integer.compare(ord1, ord2);
	}
}
