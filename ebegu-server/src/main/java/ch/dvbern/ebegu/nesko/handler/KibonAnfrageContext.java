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

package ch.dvbern.ebegu.nesko.handler;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;

public class KibonAnfrageContext {

	@Nonnull
	private Gesuch gesuch;

	@Nullable
	private SteuerdatenAnfrageStatus steuerdatenAnfrageStatus;

	@Nullable
	private SteuerdatenResponse steuerdatenResponse;


	private boolean gemeinsam;

	public KibonAnfrageContext(
		@Nonnull Gesuch gesuch) {
		this.gesuch = gesuch;

		initGemeinsam();
		setSteuerdatenZugriffFlags();
	}

	private void setSteuerdatenZugriffFlags() {
		Objects.requireNonNull(gesuch);
		Objects.requireNonNull(gesuch.getGesuchsteller1());
		Objects.requireNonNull(gesuch.getGesuchsteller1().getFinanzielleSituationContainer());

		gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setSteuerdatenZugriff(Boolean.TRUE);

		if (gesuch.getGesuchsteller2() != null && !this.gemeinsam) {
			Objects.requireNonNull(gesuch.getGesuchsteller2().getFinanzielleSituationContainer());
				gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA().setSteuerdatenZugriff(Boolean.TRUE);
		}
	}

	private void initGemeinsam() {
		Objects.requireNonNull(gesuch.getFamiliensituationContainer());
		Objects.requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());

		this.gemeinsam = Boolean.TRUE
			.equals(gesuch.getFamiliensituationContainer().getFamiliensituationJA().getGemeinsameSteuererklaerung());
		if(null == gesuch.getGesuchsteller2()){
			return;
		}
		createFinSitGS2Container();
	}

	@Nonnull
	public Gesuch getGesuch() {
		return gesuch;
	}

	@Nullable
	public SteuerdatenAnfrageStatus getSteuerdatenAnfrageStatus() {
		return steuerdatenAnfrageStatus;
	}

	public void setSteuerdatenAnfrageStatus(@Nullable SteuerdatenAnfrageStatus steuerdatenAnfrageStatus) {
		this.steuerdatenAnfrageStatus = steuerdatenAnfrageStatus;
	}

	@Nullable
	public SteuerdatenResponse getSteuerdatenResponse() {
		return steuerdatenResponse;
	}

	public void setSteuerdatenResponse(@Nullable SteuerdatenResponse steuerdatenResponse) {
		this.steuerdatenResponse = steuerdatenResponse;
	}

	public boolean isGemeinsam() {
		return gemeinsam;
	}

	public boolean hasGS1SteuerzuriffErlaubt() {
		return Boolean.TRUE.equals(getFinSitCont(1).getFinanzielleSituationJA().getSteuerdatenZugriff());
	}

	public boolean hasGS2() {
		return gesuch.getGesuchsteller2() != null;
	}

	private void createFinSitGS2Container() {
		Objects.requireNonNull(this.getGesuch().getGesuchsteller2());
		Objects.requireNonNull(this.getGesuch().getGesuchsteller1());
		final FinanzielleSituationContainer finSitCont1 =
				this.getGesuch().getGesuchsteller1().getFinanzielleSituationContainer();
		Objects.requireNonNull(finSitCont1);

		FinanzielleSituationContainer finSitGS2Cont =
				this.getGesuch().getGesuchsteller2().getFinanzielleSituationContainer() != null ?
						this.getGesuch().getGesuchsteller2().getFinanzielleSituationContainer() :
						new FinanzielleSituationContainer();
		if (finSitGS2Cont.getFinanzielleSituationJA() == null) {
			finSitGS2Cont.setFinanzielleSituationJA(new FinanzielleSituation());
		}
		finSitGS2Cont.setJahr(finSitCont1.getJahr());
		finSitGS2Cont.getFinanzielleSituationJA().setSteuerdatenZugriff(true);
		finSitGS2Cont.getFinanzielleSituationJA()
				.setSteuererklaerungAusgefuellt(finSitCont1.getFinanzielleSituationJA()
						.getSteuererklaerungAusgefuellt());
		finSitGS2Cont.getFinanzielleSituationJA()
				.setSteuerveranlagungErhalten(finSitCont1.getFinanzielleSituationJA()
						.getSteuerveranlagungErhalten());
		finSitGS2Cont.setGesuchsteller(this.getGesuch().getGesuchsteller2());
		this.getGesuch().getGesuchsteller2().setFinanzielleSituationContainer(finSitGS2Cont);
	}

	public FinanzielleSituationContainer getFinSitCont(int gesuchstellerNumber) {
		if (gesuchstellerNumber == 1) {
			return getGesuch().getGesuchsteller1().getFinanzielleSituationContainer();
		}
		return getGesuch().getGesuchsteller2().getFinanzielleSituationContainer();
	}
}
