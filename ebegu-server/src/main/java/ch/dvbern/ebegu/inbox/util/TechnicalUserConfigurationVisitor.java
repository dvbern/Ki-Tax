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

package ch.dvbern.ebegu.inbox.util;

import ch.dvbern.ebegu.util.mandant.MandantIdentifier;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class TechnicalUserConfigurationVisitor implements MandantVisitor<TechnicalUserConfiguration> {

	public TechnicalUserConfiguration process(MandantIdentifier mandant) {
		return mandant.accept(this);
	}

	@Override
	public TechnicalUserConfiguration visitBern() {
		return new TechnicalUserConfiguration(
			"88888888-2222-2222-2222-222222222222",
			"99999999-2222-2222-2222-222222222222"
		);
	}

	@Override
	public TechnicalUserConfiguration visitLuzern() {
		return new TechnicalUserConfiguration(
			"88888888-2224-2222-2222-222222222222",
			"99999999-2224-2222-2222-222222222222"
		);	}

	@Override
	public TechnicalUserConfiguration visitSolothurn() {
		return new TechnicalUserConfiguration(
			"88888888-2225-2222-2222-222222222222",
			"99999999-2225-2222-2222-222222222222"
		);
	}

	@Override
	public TechnicalUserConfiguration visitAppenzellAusserrhoden() {
		return new TechnicalUserConfiguration(
			"88888888-2223-2222-2222-222222222222",
			"99999999-2223-2222-2222-222222222222"
		);
	}

	@Override
	public TechnicalUserConfiguration visitSchwyz() {
		return new TechnicalUserConfiguration(
			"88888888-2226-2222-2222-222222222222",
			"99999999-2226-2222-2222-222222222222"
		);
	}
}
