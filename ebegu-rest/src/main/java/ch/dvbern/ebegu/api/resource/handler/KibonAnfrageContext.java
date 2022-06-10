/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.api.resource.handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;

public class KibonAnfrageContext {

	@Nonnull
	private Gesuch gesuch;

	@Nonnull
	private GesuchstellerContainer gesuchsteller;

	@Nonnull
	private FinanzielleSituationContainer finSitCont;

	@Nullable
	private FinanzielleSituationContainer finSitContGS2;

	@Nonnull String kibonAnfrageId;

	@Nullable
	private SteuerdatenAnfrageStatus steuerdatenAnfrageStatus;

	public KibonAnfrageContext(
		@Nonnull Gesuch gesuch,
		@Nonnull GesuchstellerContainer gesuchsteller,
		@Nonnull FinanzielleSituationContainer finSitCont,
		@Nonnull String kibonAnfrageId) {
		this.gesuch = gesuch;
		this.gesuchsteller = gesuchsteller;
		this.finSitCont = finSitCont;
		this.kibonAnfrageId = kibonAnfrageId;
	}

	@Nonnull
	public Gesuch getGesuch() {
		return gesuch;
	}

	@Nonnull
	public GesuchstellerContainer getGesuchsteller() {
		return gesuchsteller;
	}

	@Nonnull
	public FinanzielleSituationContainer getFinSitCont() {
		return finSitCont;
	}

	@Nullable
	public SteuerdatenAnfrageStatus getSteuerdatenAnfrageStatus() {
		return steuerdatenAnfrageStatus;
	}

	public void setSteuerdatenAnfrageStatus(@Nullable SteuerdatenAnfrageStatus steuerdatenAnfrageStatus) {
		this.steuerdatenAnfrageStatus = steuerdatenAnfrageStatus;
	}

	@Nonnull
	public String getKibonAnfrageId() {
		return kibonAnfrageId;
	}

	@Nullable
	public FinanzielleSituationContainer getFinSitContGS2() {
		return finSitContGS2;
	}

	public void setFinSitContGS2(@Nullable FinanzielleSituationContainer finSitContGS2) {
		this.finSitContGS2 = finSitContGS2;
	}

	public void zwitchGSContainer() {
		FinanzielleSituationContainer finSitGS2Temp = getFinSitContGS2();
		this.finSitContGS2 = finSitCont;
		assert finSitGS2Temp != null;
		this.finSitCont = finSitGS2Temp;
	}
}
