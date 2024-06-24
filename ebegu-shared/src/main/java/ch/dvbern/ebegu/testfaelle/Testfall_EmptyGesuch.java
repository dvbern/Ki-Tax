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

package ch.dvbern.ebegu.testfaelle;

import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.testfaelle.institutionStammdatenBuilder.InstitutionStammdatenBuilder;

@SuppressWarnings("PMD.ClassNamingConventions")
public class Testfall_EmptyGesuch extends AbstractTestfall {

	public Testfall_EmptyGesuch(
		Gesuchsperiode gesuchsperiode,
		boolean betreuungenBestaetigt,
		Gemeinde gemeinde,
		InstitutionStammdatenBuilder institutionStammdatenBuilder
	) {
		super(gesuchsperiode, betreuungenBestaetigt, gemeinde, institutionStammdatenBuilder);
	}

	@Override
	public Gesuch fillInGesuch() {
		return gesuch;
	}

	@Override
	public String getNachname() {
		return "Gesuch";
	}

	@Override
	public String getVorname() {
		return "Empty";
	}
}
