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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.testfaelle;

import ch.dvbern.ebegu.services.InstitutionStammdatenService;

public class InstitutionStammdatenBuilderBe extends InstitutionStammdatenBuilder {

	public InstitutionStammdatenBuilderBe(InstitutionStammdatenService institutionStammdatenService) {
		super(institutionStammdatenService, "945e3eef-8f43-43d2-a684-4aa61089684b", "9a0eb656-b6b7-4613-8f55-4e0e4720455e",
				"6b7beb6e-6cf3-49d6-84c0-5818d9215ecd", "199ac4a1-448f-4d4c-b3a6-5aee21f89613", "9d8ff34f-8856-4dd3-ade2-2469aadac0ed");
	}
}
