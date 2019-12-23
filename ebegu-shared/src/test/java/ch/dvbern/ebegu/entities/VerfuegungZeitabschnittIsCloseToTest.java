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

package ch.dvbern.ebegu.entities;

import java.math.BigDecimal;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
public class VerfuegungZeitabschnittIsCloseToTest {

	@Test
	public void isCloseTo_trueForIdentity() {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();

		assertThat(zeitabschnitt.isCloseTo(zeitabschnitt), is(true));
	}

	@Test
	public void isCloseTo_falseWhenDifferentAnspruch() {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setAnspruchberechtigtesPensum(80);

		VerfuegungZeitabschnitt other = new VerfuegungZeitabschnitt();
		other.setAnspruchberechtigtesPensum(79);

		assertThat(zeitabschnitt.isCloseTo(other), is(false));
	}

	@Test
	public void isCloseTo_falseWhenPensumProzentDiffersMoreThanOneHundredth() {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setBetreuungspensumProzent(BigDecimal.valueOf(10.00));

		VerfuegungZeitabschnitt other = new VerfuegungZeitabschnitt();
		other.setBetreuungspensumProzent(BigDecimal.valueOf(10.02));

		assertThat(zeitabschnitt.isCloseTo(other), is(false));
	}

	@Test
	public void isCloseTo_trueWhenPensumProzentDiffersOneHundredth() {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setBetreuungspensumProzent(BigDecimal.valueOf(10.00));

		VerfuegungZeitabschnitt other = new VerfuegungZeitabschnitt();
		other.setBetreuungspensumProzent(BigDecimal.valueOf(10.01));

		assertThat(zeitabschnitt.isCloseTo(other), is(true));
	}

	@Test
	public void isCloseTo_falseWhenVerguenstigungDiffersMoreThan5Rappen() {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setVerguenstigung(BigDecimal.valueOf(10.00));

		VerfuegungZeitabschnitt other = new VerfuegungZeitabschnitt();
		other.setVerguenstigung(BigDecimal.valueOf(10.06));

		assertThat(zeitabschnitt.isCloseTo(other), is(false));
	}

	@Test
	public void isCloseTo_trueWhenVerguenstigungDiffers5Rappen() {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setVerguenstigung(BigDecimal.valueOf(10.00));

		VerfuegungZeitabschnitt other = new VerfuegungZeitabschnitt();
		other.setVerguenstigung(BigDecimal.valueOf(10.05));

		assertThat(zeitabschnitt.isCloseTo(other), is(true));
	}

	@Test
	public void isCloseTo_falseWhenMinimalerElternbeitragDiffersMoreThan5Rappen() {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setMinimalerElternbeitrag(BigDecimal.valueOf(10.00));

		VerfuegungZeitabschnitt other = new VerfuegungZeitabschnitt();
		other.setMinimalerElternbeitrag(BigDecimal.valueOf(10.06));

		assertThat(zeitabschnitt.isCloseTo(other), is(false));
	}

	@Test
	public void isCloseTo_trueWhenMinimalerElternbeitragDiffers5Rappen() {
		VerfuegungZeitabschnitt zeitabschnitt = new VerfuegungZeitabschnitt();
		zeitabschnitt.setMinimalerElternbeitrag(BigDecimal.valueOf(10.00));

		VerfuegungZeitabschnitt other = new VerfuegungZeitabschnitt();
		other.setMinimalerElternbeitrag(BigDecimal.valueOf(10.05));

		assertThat(zeitabschnitt.isCloseTo(other), is(true));
	}
}
