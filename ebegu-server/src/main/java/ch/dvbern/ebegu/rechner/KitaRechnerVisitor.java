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

package ch.dvbern.ebegu.rechner;

import java.util.List;

import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.rechner.rules.RechnerRule;
import ch.dvbern.ebegu.util.mandant.MandantVisitor;
import com.sun.istack.NotNull;

public class KitaRechnerVisitor implements MandantVisitor<AbstractRechner> {

	private final List<RechnerRule> rechnerRulesForGemeinde;

	public KitaRechnerVisitor(List<RechnerRule> rechnerRulesForGemeinde) {
		this.rechnerRulesForGemeinde = rechnerRulesForGemeinde;
	}

	public AbstractRechner getKitaRechnerForMandant(@NotNull Mandant mandant) {
		return mandant.getMandantIdentifier().accept(this);
	}

	@Override
	public AbstractRechner visitBern() {
		return new KitaBernRechner(rechnerRulesForGemeinde);
	}

	@Override
	public AbstractRechner visitLuzern() {
		return new KitaBernRechner(rechnerRulesForGemeinde);
	}

	@Override
	public AbstractRechner visitSolothurn() {
		return new KitaBernRechner(rechnerRulesForGemeinde);
	}
}
