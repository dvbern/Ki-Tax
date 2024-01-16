/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.rules.initalizer;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;

public class RestanspruchInitializerVisitor implements MandantVisitor<RestanspruchInitializer> {

	private final boolean isDebug;

	public RestanspruchInitializerVisitor(boolean isDebug) {
		this.isDebug = isDebug;
	}

	public RestanspruchInitializer getRestanspruchInitialzier(Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public RestanspruchInitializer visitBern() {
		return new RestanspruchInitializer(isDebug);
	}

	@Override
	public RestanspruchInitializer visitLuzern() {
		return new RestanspruchInitializer(isDebug);
	}

	@Override
	public RestanspruchInitializer visitSolothurn() {
		return new RestanspruchInitializer(isDebug);
	}

	@Override
	public RestanspruchInitializer visitAppenzellAusserrhoden() {
		return new RestanspruchInitializerAR(isDebug);
	}

	@Override
	public RestanspruchInitializer visitSchwyz() {
		return new RestanspruchInitializer(isDebug);
	}
}
