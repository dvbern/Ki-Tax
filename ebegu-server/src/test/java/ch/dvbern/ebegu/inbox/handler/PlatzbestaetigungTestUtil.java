/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.inbox.handler;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.PensumUnits;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.BetreuungEventDTO;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import com.spotify.hamcrest.pojo.IsPojo;
import org.hamcrest.Matcher;

import static ch.dvbern.ebegu.util.EbeguUtil.coalesce;
import static com.google.common.base.Preconditions.checkArgument;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public final class PlatzbestaetigungTestUtil {

	private PlatzbestaetigungTestUtil() {
	}

	@Nonnull
	public static BetreuungEventDTO createBetreuungEventDTO(@Nonnull ZeitabschnittDTO... zeitabschnitte) {
		BetreuungEventDTO betreuungEventDTO = new BetreuungEventDTO();
		betreuungEventDTO.setRefnr("20.007305.002.1.3");
		betreuungEventDTO.setInstitutionId("1234-5678-9101-1121");
		betreuungEventDTO.setZeitabschnitte(Arrays.asList(zeitabschnitte));

		return betreuungEventDTO;
	}

	@Nonnull
	public static ZeitabschnittDTO createZeitabschnittDTO(@Nonnull DateRange range) {
		return createZeitabschnittDTO(range.getGueltigAb(), range.getGueltigBis());
	}

	@Nonnull
	public static ZeitabschnittDTO createZeitabschnittDTO(@Nonnull LocalDate von, @Nonnull LocalDate bis) {
		return ZeitabschnittDTO.newBuilder()
			.setBetreuungskosten(MathUtil.DEFAULT.from(2000))
			.setBetreuungspensum(new BigDecimal(80))
			.setPensumUnit(Zeiteinheit.PERCENTAGE)
			.setVon(von)
			.setBis(bis)
			.build();
	}

	@Nonnull
	public static Betreuungsmitteilung createBetreuungMitteilung(@Nonnull BetreuungsmitteilungPensum... pensen) {
		Betreuungsmitteilung betreuungsmitteilung = new Betreuungsmitteilung();
		addAll(betreuungsmitteilung, Arrays.asList(pensen));

		return betreuungsmitteilung;
	}

	@Nonnull
	public static BetreuungsmitteilungPensum createBetreuungsmitteilungPensum(@Nonnull DateRange range) {
		BetreuungsmitteilungPensum betreuungsmitteilungPensum = new BetreuungsmitteilungPensum();
		betreuungsmitteilungPensum.setMonatlicheBetreuungskosten(MathUtil.DEFAULT.from(2000));
		betreuungsmitteilungPensum.setPensum(new BigDecimal(80));
		betreuungsmitteilungPensum.setUnitForDisplay(PensumUnits.PERCENTAGE);
		betreuungsmitteilungPensum.setGueltigkeit(range);

		return betreuungsmitteilungPensum;
	}

	public static void addAll(
		@Nonnull Betreuungsmitteilung mitteilung,
		@Nonnull Collection<BetreuungsmitteilungPensum> pensen) {

		pensen.forEach(p -> p.setBetreuungsmitteilung(mitteilung));
		mitteilung.getBetreuungspensen().addAll(pensen);
	}

	@Nonnull
	public static Betreuungspensum getSingleContainer(@Nonnull Betreuung betreuung) {
		checkArgument(
			betreuung.getBetreuungspensumContainers().size() == 1,
			"Broken test setup: expected 1 container in %s",
			betreuung);

		return betreuung.getBetreuungspensumContainers().iterator().next().getBetreuungspensumJA();
	}

	@Nonnull
	public static Betreuung betreuungWithSingleContainer(@Nonnull Gesuch gesuch) {
		return betreuungWithSingleContainer(gesuch, LocalDate.of(2020, 8, 1), Constants.END_OF_TIME);
	}

	@Nonnull
	public static Betreuung betreuungWithSingleContainer(
		@Nonnull Gesuch gesuch,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis) {

		Betreuung betreuung = requireNonNull(gesuch.getFirstBetreuung());
		Betreuungspensum betreuungspensum = getSingleContainer(betreuung);
		betreuungspensum.getGueltigkeit().setGueltigAb(von);
		betreuungspensum.getGueltigkeit().setGueltigBis(bis);

		return betreuung;
	}

	@Nonnull
	public static Matcher<Processing> failed(@Nonnull String message) {
		return failed(is(message));
	}

	@Nonnull
	public static Matcher<Processing> failed(@Nonnull Matcher<String> messageMatcher) {
		return pojo(Processing.class)
			.where(Processing::isProcessingSuccess, is(false))
			.where(Processing::getMessage, messageMatcher);
	}

	@Nonnull
	public static IsPojo<AbstractMahlzeitenPensum> matches(@Nonnull ZeitabschnittDTO z) {
		return matches(z, z.getVon(), z.getBis());
	}

	@Nonnull
	public static IsPojo<AbstractMahlzeitenPensum> matches(
		@Nonnull ZeitabschnittDTO z,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis) {

		return matches(z, new DateRange(von, bis));
	}

	@Nonnull
	public static IsPojo<AbstractMahlzeitenPensum> matches(
		@Nonnull ZeitabschnittDTO z,
		@Nonnull DateRange gueltigkeit) {

		return matches(z, z.getBetreuungspensum(), gueltigkeit);
	}

	@Nonnull
	public static IsPojo<AbstractMahlzeitenPensum> matches(
		@Nonnull ZeitabschnittDTO z,
		@Nonnull BigDecimal pensum,
		@Nonnull DateRange gueltigkeit) {

		return pojo(AbstractMahlzeitenPensum.class)
			.where(
				AbstractMahlzeitenPensum::getMonatlicheBetreuungskosten,
				comparesEqualTo(z.getBetreuungskosten()))
			.where(
				AbstractMahlzeitenPensum::getPensum,
				comparesEqualTo(pensum))
			.where(
				AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten,
				comparesEqualTo(coalesce(z.getAnzahlHauptmahlzeiten(), BigDecimal.ZERO)))
			.where(
				AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten,
				comparesEqualTo(coalesce(z.getAnzahlNebenmahlzeiten(), BigDecimal.ZERO)))
			.where(
				AbstractMahlzeitenPensum::getTarifProHauptmahlzeit,
				comparesEqualTo(coalesce(z.getTarifProHauptmahlzeiten(), BigDecimal.ZERO)))
			.where(
				AbstractMahlzeitenPensum::getTarifProNebenmahlzeit,
				comparesEqualTo(coalesce(z.getTarifProNebenmahlzeiten(), BigDecimal.ZERO)))
			.where(
				AbstractMahlzeitenPensum::getGueltigkeit, equalTo(gueltigkeit));
	}

	@Nonnull
	public static IsPojo<AbstractMahlzeitenPensum> matches(
		@Nonnull BetreuungspensumContainer other,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis) {

		return matches(other.getBetreuungspensumJA(), von, bis);
	}

	@Nonnull
	public static IsPojo<AbstractMahlzeitenPensum> matches(
		@Nonnull AbstractMahlzeitenPensum other,
		@Nonnull LocalDate von,
		@Nonnull LocalDate bis) {

		return matches(other, new DateRange(von, bis));
	}

	@Nonnull
	public static IsPojo<AbstractMahlzeitenPensum> matches(
		@Nonnull AbstractMahlzeitenPensum other,
		@Nonnull DateRange gueltigkeit) {

		return pojo(AbstractMahlzeitenPensum.class)
			.where(
				AbstractMahlzeitenPensum::getMonatlicheBetreuungskosten,
				comparesEqualTo(other.getMonatlicheBetreuungskosten()))
			.where(
				AbstractMahlzeitenPensum::getPensum,
				comparesEqualTo(other.getPensum()))
			.where(
				AbstractMahlzeitenPensum::getMonatlicheHauptmahlzeiten,
				comparesEqualTo(other.getMonatlicheHauptmahlzeiten()))
			.where(
				AbstractMahlzeitenPensum::getMonatlicheNebenmahlzeiten,
				comparesEqualTo(other.getMonatlicheNebenmahlzeiten()))
			.where(
				AbstractMahlzeitenPensum::getTarifProHauptmahlzeit,
				comparesEqualTo(other.getTarifProHauptmahlzeit()))
			.where(
				AbstractMahlzeitenPensum::getTarifProNebenmahlzeit,
				comparesEqualTo(other.getTarifProNebenmahlzeit()))
			.where(
				AbstractMahlzeitenPensum::isVollstaendig,
				is(other.isVollstaendig())
			)
			.where(
				AbstractMahlzeitenPensum::getGueltigkeit, equalTo(gueltigkeit));
	}
}
