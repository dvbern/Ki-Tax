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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.SteuerdatenResponse;
import ch.dvbern.ebegu.enums.GesuchstellerTyp;
import ch.dvbern.ebegu.enums.SteuerdatenAnfrageStatus;
import org.apache.commons.lang3.StringUtils;

public class KibonAnfrageContext {

	@Nonnull
	private final Gesuch gesuch;

	private Integer zpvNummerForRequest;

	private GesuchstellerTyp gesuchstellerTyp;

	@Nullable
	private SteuerdatenAnfrageStatus steuerdatenAnfrageStatus;

	@Nullable
	private SteuerdatenResponse steuerdatenResponse;

	private boolean gemeinsam;

	private boolean useGeburtrsdatumFromOtherGesuchsteller = false;

	public KibonAnfrageContext(
			@Nonnull Gesuch gesuch,
			int zpvNummerForRequest,
			GesuchstellerTyp gesuchstellerTyp) {
		this.gesuch = gesuch;
		this.zpvNummerForRequest = zpvNummerForRequest;
		this.gesuchstellerTyp = gesuchstellerTyp;

		initGemeinsam();
		createFinSitGS2Container();
	}

	public KibonAnfrageContext(
			@Nonnull Gesuch gesuch,
			@Nonnull GesuchstellerTyp gesuchstellerTyp,
			@Nullable String zpvBesizter) {
		this.gesuch = gesuch;
		this.gesuchstellerTyp = gesuchstellerTyp;

		initGemeinsam();
		initZpvNummerForRequest(zpvBesizter);
		createFinSitGS2Container();
	}

	private void initZpvNummerForRequest(@Nullable String zpvBesitzer) {
		String zpvNummer = null;

		if (gesuchstellerTyp == GesuchstellerTyp.GESUCHSTELLER_2) {
			zpvNummer = getZpvNummerFromGS2();
		} else {
			zpvNummer = getZpvNummerFromGS1OrBesitzer(zpvBesitzer);
		}

		if (StringUtils.isNotEmpty(zpvNummer)) {
			this.zpvNummerForRequest = Integer.parseInt(zpvNummer);
		}
	}

	@Nullable
	private String getZpvNummerFromGS1OrBesitzer(@Nullable String zpvBesitzer) {
		if (gesuch.extractGesuchsteller1().isPresent()) {
			String zpvNr = gesuch.extractGesuchsteller1().get().getZpvNummer();
			if (StringUtils.isNotEmpty(zpvNr)) {
				return zpvNr;
			}
		}

		return zpvBesitzer;
	}

	@Nullable
	private String getZpvNummerFromGS2() {
		if (gesuch.extractGesuchsteller2().isPresent()) {
			return gesuch.extractGesuchsteller2().get().getZpvNummer();
		}

		return null;
	}

	private void initGemeinsam() {
		Objects.requireNonNull(gesuch.getFamiliensituationContainer());
		Objects.requireNonNull(gesuch.getFamiliensituationContainer().getFamiliensituationJA());

		this.gemeinsam = Boolean.TRUE
			.equals(gesuch.getFamiliensituationContainer().getFamiliensituationJA().getGemeinsameSteuererklaerung());
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
		getFinanzielleSituationJAToUse().setSteuerdatenAbfrageStatus(steuerdatenAnfrageStatus);
	}

	public void setSteuerdatenAbfrageTimestampNow() {
		getFinanzielleSituationJAToUse().setSteuerdatenAbfrageTimestamp(LocalDateTime.now());
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

	public Optional<Integer> getZpvNummerForRequest() {
		return Optional.ofNullable(zpvNummerForRequest);
	}

	public void useGeburtrsdatumFromOtherGesuchsteller() {
		this.useGeburtrsdatumFromOtherGesuchsteller = true;
	}

	public Optional<LocalDate> getGeburstdatumForRequest() {
		if (this.useGeburtrsdatumFromOtherGesuchsteller) {
			return getGeburtsdatumFromOtherGesuchsteller();
		}
		return Optional.ofNullable(getGesuchstellerContainerToUse().getGesuchstellerJA().getGeburtsdatum());
	}

	private Optional<LocalDate> getGeburtsdatumFromOtherGesuchsteller() {
		if (this.gesuchstellerTyp == GesuchstellerTyp.GESUCHSTELLER_1) {
			if (gesuch.extractGesuchsteller2().isPresent()) {
				return Optional.ofNullable(gesuch.extractGesuchsteller2().get().getGeburtsdatum());
			}

			return Optional.empty();
		}

		Objects.requireNonNull(this.gesuch.getGesuchsteller1());
		Objects.requireNonNull(this.gesuch.getGesuchsteller1().getGesuchstellerJA());
		return Optional.ofNullable(this.gesuch.getGesuchsteller1().getGesuchstellerJA().getGeburtsdatum());

	}

	@Nonnull
	public GesuchstellerContainer getGesuchstellerContainerToUse() {
		if (this.gesuchstellerTyp == GesuchstellerTyp.GESUCHSTELLER_2) {
			Objects.requireNonNull(this.gesuch.getGesuchsteller2());
			return this.gesuch.getGesuchsteller2();
		}

		Objects.requireNonNull(this.gesuch.getGesuchsteller1());
		return this.gesuch.getGesuchsteller1();
	}

	private void createFinSitGS2Container() {
		if (this.getGesuch().getGesuchsteller2() == null){
			return;
		}

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

	public FinanzielleSituationContainer getFinanzielleSituationContainerToUse() {
		Objects.requireNonNull(getGesuchstellerContainerToUse().getFinanzielleSituationContainer());
		return getGesuchstellerContainerToUse().getFinanzielleSituationContainer();
	}

	public FinanzielleSituation getFinanzielleSituationJAToUse() {
		Objects.requireNonNull(getFinanzielleSituationContainerToUse().getFinanzielleSituationJA());
		return getFinanzielleSituationContainerToUse().getFinanzielleSituationJA();
	}

	public void setSteuerdatenAnfrageStatusFailedNoZPV() {
		if (this.gesuchstellerTyp == GesuchstellerTyp.GESUCHSTELLER_2) {
			this.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER_GS2);
		} else {
			this.setSteuerdatenAnfrageStatus(SteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER);
		}
	}
}
