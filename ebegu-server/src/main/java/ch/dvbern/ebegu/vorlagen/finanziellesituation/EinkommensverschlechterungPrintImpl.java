/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.vorlagen.finanziellesituation;

import java.math.BigDecimal;

import ch.dvbern.ebegu.entities.AbstractFinanzielleSituation;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;

import static java.util.Objects.requireNonNull;

/**
 * Implementiert den {@link EinkommensverschlechterungPrint}
 */
public class EinkommensverschlechterungPrintImpl extends FinanzDatenPrintImpl implements EinkommensverschlechterungPrint {

	private final String einkommensverschlechterungJahr;
	private final String ereigniseintritt;
	private final String grund;
	private final Einkommensverschlechterung ekvGS1;
	private Einkommensverschlechterung ekvGS2;
	private final int basisJahrPlus;

	/**
	 * Konstruktor
	 *
	 * @param fsGesuchsteller1 das {@link FinanzSituationPrintGesuchsteller}
	 * @param fsGesuchsteller2 das {@link FinanzSituationPrintGesuchsteller}
	 * @param einkommensverschlechterungJahr das Jahr des Einkommenverschleschterung
	 * @param ereigniseintritt Ereingis datum
	 * @param grund Grund
	 * @param basisJahrPlus Anzahl Jahre nach dem Basisjahr
	 */
	public EinkommensverschlechterungPrintImpl(
		FinanzSituationPrintGesuchsteller fsGesuchsteller1,
		FinanzSituationPrintGesuchsteller fsGesuchsteller2,
		String einkommensverschlechterungJahr,
		String ereigniseintritt,
		String grund,
		int basisJahrPlus) {

		super(fsGesuchsteller1, fsGesuchsteller2);

		this.basisJahrPlus = basisJahrPlus;
		this.einkommensverschlechterungJahr = einkommensverschlechterungJahr;
		this.ereigniseintritt = ereigniseintritt;
		this.grund = grund;
		if (basisJahrPlus == 1) {
			this.ekvGS1 = fsGesuchsteller1.getEinkommensverschlechterung1();
		} else {
			this.ekvGS1 = fsGesuchsteller1.getEinkommensverschlechterung2();
		}
		if (fsGesuchsteller2 != null) {
			if (basisJahrPlus == 1 && fsGesuchsteller2.getEinkommensverschlechterung1() != null) {
				this.ekvGS2 = fsGesuchsteller2.getEinkommensverschlechterung1();
			} else if (fsGesuchsteller2.getEinkommensverschlechterung2() != null) {
				this.ekvGS2 = fsGesuchsteller2.getEinkommensverschlechterung2();
			}
		}

	}

	@Override
	public String getEinkommensverschlechterungJahr() {
		return einkommensverschlechterungJahr;
	}

	@Override
	public String getEreigniseintritt() {

		return ereigniseintritt;
	}

	@Override
	public String getGrund() {

		return grund;
	}

	@Override
	public boolean isExistEreigniseintritt() {
		return (this.ereigniseintritt != null && !this.ereigniseintritt.isEmpty());
	}

	@Override
	public boolean isExistGrund() {
		return (this.grund != null && !this.grund.isEmpty());
	}

	@Override
	public BigDecimal getGeschaeftsgewinnG1() {
		final FinanzSituationPrintGesuchsteller fsGesuchsteller = requireNonNull(getFsGesuchsteller1());

		return FinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(
			fsGesuchsteller.getFinanzielleSituation(),
			fsGesuchsteller.getEinkommensverschlechterung1(),
			fsGesuchsteller.getEinkommensverschlechterung2(),
			fsGesuchsteller.getEinkommensverschlechterungInfo(),
			basisJahrPlus);
	}

	@Override
	public BigDecimal getGeschaeftsgewinnG2() {
		//hier muessen zum berechnen die Einkommensverschlechterung und die finanzielle Situation benutzt werden
		if (getFsGesuchsteller2() != null) {
			final FinanzSituationPrintGesuchsteller fsGesuchsteller = getFsGesuchsteller2();
			return FinanzielleSituationRechner.calcGeschaeftsgewinnDurchschnitt(fsGesuchsteller.getFinanzielleSituation(),
				fsGesuchsteller.getEinkommensverschlechterung1(),
				fsGesuchsteller.getEinkommensverschlechterung2(),
				fsGesuchsteller.getEinkommensverschlechterungInfo(),
				basisJahrPlus);
		}
		return null;
	}

	@Override
	protected AbstractFinanzielleSituation getFinanzSituationGS1() {
		return ekvGS1;
	}

	@Override
	protected AbstractFinanzielleSituation getFinanzSituationGS2() {
		return ekvGS2;
	}

}
