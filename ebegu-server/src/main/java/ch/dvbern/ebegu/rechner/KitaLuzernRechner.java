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

import java.math.BigDecimal;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.dto.BGCalculationInput;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;

public class KitaLuzernRechner extends AbstractLuzernRechner {

	private AbstractKitaLuzernRecher rechner;

	@Override
	public void calculate(
		@Nonnull VerfuegungZeitabschnitt verfuegungZeitabschnitt,
		@Nonnull BGRechnerParameterDTO parameterDTO) {

		BGCalculationInput bgCalculationInput = verfuegungZeitabschnitt.getBgCalculationInputAsiv();
		this.rechner = bgCalculationInput.isBabyTarif() ?
			new KitaLuzernBabyRechner(bgCalculationInput.getMassgebendesEinkommen(), parameterDTO) :
			new KitaLuzernKindRechner(bgCalculationInput.getMassgebendesEinkommen(), parameterDTO);

		super.calculate(verfuegungZeitabschnitt, parameterDTO);
	}

	@Override
	BigDecimal getMinimalTarif() {
		return inputParameter.getMinVerguenstigungProTg();
	}

	@Override
	BigDecimal getVollkostenTarif() {
		return rechner.getVollkostenTarif();
	}

	@Override
	BigDecimal calculateSelbstbehaltElternProzent() {
		return rechner.calculateSelbstbehaltElternProzent(this.prozentuallerSelbstbehaltGemaessFormel);
	}

	@Override
	BigDecimal calculateBGProTagByEinkommen() {
		return rechner.calculateBGProTagByEinkommen(this.selbstBehaltElternProzent);
	}

	private abstract static class AbstractKitaLuzernRecher {

		private final BGRechnerParameterDTO inputParameter;
		private final BigDecimal massgebendesEinkommen;

		AbstractKitaLuzernRecher(BigDecimal massgebendesEinkommen, BGRechnerParameterDTO inputParameter) {
			this.inputParameter = inputParameter;
			this.massgebendesEinkommen = massgebendesEinkommen;
		}

		abstract BigDecimal getVollkostenTarif();
		abstract BigDecimal calculateSelbstbehaltElternProzent(BigDecimal selbstbehaltElternProzent);
		abstract BigDecimal calculateBGProTagByEinkommen(BigDecimal selbstbehaltElternProzent);

		protected BGRechnerParameterDTO getInputParameter() {
			return inputParameter;
		}

		protected BigDecimal getMassgebendesEinkommen() {
			return massgebendesEinkommen;
		}
	}

	private static class KitaLuzernBabyRechner extends AbstractKitaLuzernRecher {

		public KitaLuzernBabyRechner(
			BigDecimal massgebendesEinkommen,
			BGRechnerParameterDTO inputParameter) {
			super(massgebendesEinkommen, inputParameter);
		}

		/**
		 * Berechnet den Selbstbehalt der Eltern
		 *
		 * returns prozentuallerSelbstbehaltGemaessFormel, wenn MassgebendesEinkommen <= MaximalMasgebendesEinkommen
		 * {@see AbstractLuzernRechner#calculateSelbstbehaltProzentenGemaessFormel()}
		 *
		 * returns 101, wenn MassgebendesEinkommen > MaximalMasgebendesEinkommen:
		 */
		@Override
		BigDecimal calculateSelbstbehaltElternProzent(BigDecimal selbstbehaltElternProzent) {
			if(getMassgebendesEinkommen().compareTo(getInputParameter().getMaxMassgebendesEinkommen()) > 0) {
				return BigDecimal.valueOf(101);
			}

			return selbstbehaltElternProzent;
		}

		/**
		 * Berechnet den Gutschein pro Tag aufgrund des Einkommens nach
		 *
		 * returns 0, wenn selbstBehaltElternProzent > 100
		 *
		 * returns bgProTag
		 * wenn bgProTag > minBetreuungsgutschein
		 * sonst minBetreuungsgutschein
		 *
		 * formel bgProTag = vollkostenTarif * (1-selbstBehaltElternProzent)
		 */
		@Override
		BigDecimal calculateBGProTagByEinkommen(BigDecimal selbstbehaltElternProzent) {
			if(selbstbehaltElternProzent.compareTo(BigDecimal.valueOf(100)) > 0) {
				return BigDecimal.ZERO;
			}

			BigDecimal einsMinusSelbstbehalt = EXACT.subtract(BigDecimal.ONE, selbstbehaltElternProzent);
			BigDecimal bgProTag = EXACT.multiply(getVollkostenTarif(), einsMinusSelbstbehalt);

			if(bgProTag.compareTo(getMinBetreuungsgutschein()) > 0) {
				return bgProTag;
			}

			return getMinBetreuungsgutschein();
		}

		@Override
		BigDecimal getVollkostenTarif() {
			return super.getInputParameter().getVollkostenTarifBabyKita();
		}


		private BigDecimal getMinBetreuungsgutschein() {
			return super.getInputParameter().getMinBGBabyKita();
		}

	}

	private static class KitaLuzernKindRechner extends  AbstractKitaLuzernRecher {

		public KitaLuzernKindRechner(
			BigDecimal massgebendesEinkommen,
			BGRechnerParameterDTO inputParameter) {
			super(massgebendesEinkommen, inputParameter);
		}

		@Override
		BigDecimal getVollkostenTarif() {
			return super.getInputParameter().getVollkostenTarifKindKita();
		}


		private BigDecimal getMinBetreuungsgutschein() {
			return super.getInputParameter().getMinBGKindKita();
		}

		/**
		 * Berechnet den Selbstbehalt der Eltern
		 *
		 * returns selbstbehaltDerEltern (gemässFormel)
		 * wenn selbstbehaltDerEltern (gemässFormel) <= 100%,
		 *
		 * returns 100, wenn selbstbehaltDerEltern (gemässFormel) > 100%:
		 */
		@Override
		BigDecimal calculateSelbstbehaltElternProzent(BigDecimal selbstbehaltElternProzent) {
			if(selbstbehaltElternProzent.compareTo(BigDecimal.valueOf(100)) > 0) {
				return BigDecimal.valueOf(100);
			}

			return selbstbehaltElternProzent;
		}


		/**
		 * Berechnet den Gutschein pro Tag aufgrund des Einkommens nach
		 *
		 * returns bgProTag, wenn bgProTag > minBetreuungsgutschein, sonst
		 * returns minBetreuungsgutschein, wenn massgebendes Einkomen <= maxMassgebendesEinkommen, sonst
		 * returns 0
		 *
		 * formel bgProTag = vollkostenTarif * (1-selbstBehaltElternProzent)
		 */
		@Override
		BigDecimal calculateBGProTagByEinkommen(BigDecimal selbstbehaltElternProzent) {
			BigDecimal einsMinusSelbstbehalt = EXACT.subtract(BigDecimal.ONE, selbstbehaltElternProzent);
			BigDecimal bgProTag = EXACT.multiply(getVollkostenTarif(), einsMinusSelbstbehalt);

			if(bgProTag.compareTo(getMinBetreuungsgutschein()) > 0) {
				return bgProTag;
			}

			if(getMassgebendesEinkommen().compareTo(super.getInputParameter().getMaxMassgebendesEinkommen()) <= 0) {
				return getMinBetreuungsgutschein();
			}

			return BigDecimal.ZERO;
		}
	}
}
