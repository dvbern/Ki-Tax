/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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
import java.util.function.Function;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.util.MathUtil;
import com.google.common.base.MoreObjects;

import static ch.dvbern.ebegu.util.MathUtil.roundToFrankenRappen;
import static ch.dvbern.ebegu.util.MathUtil.toTwoKommastelle;

public class BGCalculationResult {

	@Nonnull
	private BigDecimal minimalerElternbeitrag = BigDecimal.ZERO;
	@Nonnull
	private BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag = BigDecimal.ZERO;
	@Nonnull
	private BigDecimal verguenstigungOhneBeruecksichtigungVollkosten = BigDecimal.ZERO;
	@Nonnull
	private BigDecimal verguenstigung = BigDecimal.ZERO;
	@Nonnull
	private BigDecimal vollkosten = BigDecimal.ZERO;
	@Nonnull
	private BigDecimal elternbeitrag = BigDecimal.ZERO;
	@Nonnull
	private BigDecimal verfuegteAnzahlZeiteinheiten = BigDecimal.ZERO;
	@Nonnull
	private BigDecimal anspruchsberechtigteAnzahlZeiteinheiten = BigDecimal.ZERO;
	@Nonnull
	private PensumUnits zeiteinheit = PensumUnits.DAYS;
	@Nonnull
	private BigDecimal betreuungspensumZeiteinheit = BigDecimal.ZERO;
	@Nonnull
	private Function<BigDecimal, BigDecimal> zeiteinheitenRoundingStrategy = MathUtil::toTwoKommastelle;

	public void toVerfuegungZeitabschnitt(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {
		zeitabschnitt.setMinimalerElternbeitrag(roundToFrankenRappen(minimalerElternbeitrag));
		zeitabschnitt.setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(
				roundToFrankenRappen(verguenstigungOhneBeruecksichtigungMinimalbeitrag));
		zeitabschnitt.setVerguenstigungOhneBeruecksichtigungVollkosten(
				roundToFrankenRappen(verguenstigungOhneBeruecksichtigungVollkosten));
		zeitabschnitt.setVerguenstigung(roundToFrankenRappen(verguenstigung));
		zeitabschnitt.setVollkosten(roundToFrankenRappen(vollkosten));
		zeitabschnitt.setElternbeitrag(roundToFrankenRappen(elternbeitrag));

		zeitabschnitt.setBetreuungspensumZeiteinheit(
			zeiteinheitenRoundingStrategy.apply(betreuungspensumZeiteinheit));
		zeitabschnitt.setVerfuegteAnzahlZeiteinheiten(
			zeiteinheitenRoundingStrategy.apply(verfuegteAnzahlZeiteinheiten));
		zeitabschnitt.setAnspruchsberechtigteAnzahlZeiteinheiten(
			zeiteinheitenRoundingStrategy.apply(anspruchsberechtigteAnzahlZeiteinheiten));

		zeitabschnitt.setZeiteinheit(zeiteinheit);

	}

	@Override
	@Nonnull
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("minimalerElternbeitrag", minimalerElternbeitrag)
			.add(
				"verguenstigungOhneBeruecksichtigungMinimalbeitrag",
				verguenstigungOhneBeruecksichtigungMinimalbeitrag)
			.add("verguenstigungOhneBeruecksichtigungVollkosten", verguenstigungOhneBeruecksichtigungVollkosten)
			.add("verguenstigung", verguenstigung)
			.add("vollkosten", vollkosten)
			.add("elternbeitrag", elternbeitrag)
			.add("verfuegteAnzahlZeiteinheiten", verfuegteAnzahlZeiteinheiten)
			.add("anspruchsberechtigteAnzahlZeiteinheiten", anspruchsberechtigteAnzahlZeiteinheiten)
			.add("zeiteinheit", zeiteinheit)
			.add("betreuungspensumZeiteinheit", betreuungspensumZeiteinheit)
			.toString();
	}

	@Nonnull
	public BigDecimal getMinimalerElternbeitrag() {
		return minimalerElternbeitrag;
	}

	public void setMinimalerElternbeitrag(@Nonnull BigDecimal minimalerElternbeitrag) {
		this.minimalerElternbeitrag = minimalerElternbeitrag;
	}

	@Nonnull
	public BigDecimal getVerguenstigungOhneBeruecksichtigungMinimalbeitrag() {
		return verguenstigungOhneBeruecksichtigungMinimalbeitrag;
	}

	public void setVerguenstigungOhneBeruecksichtigungMinimalbeitrag(
		@Nonnull BigDecimal verguenstigungOhneBeruecksichtigungMinimalbeitrag) {
		this.verguenstigungOhneBeruecksichtigungMinimalbeitrag = verguenstigungOhneBeruecksichtigungMinimalbeitrag;
	}

	@Nonnull
	public BigDecimal getVerguenstigungOhneBeruecksichtigungVollkosten() {
		return verguenstigungOhneBeruecksichtigungVollkosten;
	}

	public void setVerguenstigungOhneBeruecksichtigungVollkosten(
		@Nonnull BigDecimal verguenstigungOhneBeruecksichtigungVollkosten) {
		this.verguenstigungOhneBeruecksichtigungVollkosten = verguenstigungOhneBeruecksichtigungVollkosten;
	}

	@Nonnull
	public BigDecimal getVerguenstigung() {
		return verguenstigung;
	}

	public void setVerguenstigung(@Nonnull BigDecimal verguenstigung) {
		this.verguenstigung = verguenstigung;
	}

	@Nonnull
	public BigDecimal getVollkosten() {
		return vollkosten;
	}

	public void setVollkosten(@Nonnull BigDecimal vollkosten) {
		this.vollkosten = vollkosten;
	}

	@Nonnull
	public BigDecimal getElternbeitrag() {
		return elternbeitrag;
	}

	public void setElternbeitrag(@Nonnull BigDecimal elternbeitrag) {
		this.elternbeitrag = elternbeitrag;
	}

	@Nonnull
	public BigDecimal getVerfuegteAnzahlZeiteinheiten() {
		return verfuegteAnzahlZeiteinheiten;
	}

	public void setVerfuegteAnzahlZeiteinheiten(@Nonnull BigDecimal verfuegteAnzahlZeiteinheiten) {
		this.verfuegteAnzahlZeiteinheiten = verfuegteAnzahlZeiteinheiten;
	}

	@Nonnull
	public BigDecimal getAnspruchsberechtigteAnzahlZeiteinheiten() {
		return anspruchsberechtigteAnzahlZeiteinheiten;
	}

	public void setAnspruchsberechtigteAnzahlZeiteinheiten(
		@Nonnull BigDecimal anspruchsberechtigteAnzahlZeiteinheiten) {
		this.anspruchsberechtigteAnzahlZeiteinheiten = anspruchsberechtigteAnzahlZeiteinheiten;
	}

	@Nonnull
	public PensumUnits getZeiteinheit() {
		return zeiteinheit;
	}

	public void setZeiteinheit(@Nonnull PensumUnits zeiteinheit) {
		this.zeiteinheit = zeiteinheit;
	}

	@Nonnull
	public BigDecimal getBetreuungspensumZeiteinheit() {
		return betreuungspensumZeiteinheit;
	}

	public void setBetreuungspensumZeiteinheit(@Nonnull BigDecimal betreuungspensumZeiteinheit) {
		this.betreuungspensumZeiteinheit = betreuungspensumZeiteinheit;
	}

	@Nonnull
	public Function<BigDecimal, BigDecimal> getZeiteinheitenRoundingStrategy() {
		return zeiteinheitenRoundingStrategy;
	}

	public void setZeiteinheitenRoundingStrategy(@Nonnull Function<BigDecimal, BigDecimal> strategy) {
		this.zeiteinheitenRoundingStrategy = strategy;
	}
}
