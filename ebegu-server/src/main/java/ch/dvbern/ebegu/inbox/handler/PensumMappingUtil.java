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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Gueltigkeit;
import ch.dvbern.ebegu.util.GueltigkeitsUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

import static ch.dvbern.ebegu.inbox.handler.PlatzbestaetigungEventHandler.GO_LIVE;
import static ch.dvbern.ebegu.util.EbeguUtil.coalesce;
import static java.math.BigDecimal.ZERO;

public final class PensumMappingUtil {

	private PensumMappingUtil() {
		// util
	}

	public static void addZeitabschnitteToBetreuung(@Nonnull ProcessingContext ctx) {
		Betreuung betreuung = ctx.getBetreuung();
		DateRange gueltigkeit = ctx.getGueltigkeitInPeriode();

		List<BetreuungspensumContainer> containersToUpdate = betreuung.getBetreuungspensumContainers().stream()
			.filter(c -> c.getGueltigkeit().intersects(gueltigkeit))
			.collect(Collectors.toList());
		betreuung.getBetreuungspensumContainers().removeAll(containersToUpdate);

		// first deal with gueltigBis, since findLast and findFirst might return the same container, but only for last
		// we create a copy (and we want to copy before we mutate).
		Optional<BetreuungspensumContainer> overlappingGueltigBis = GueltigkeitsUtil.findLast(containersToUpdate)
			.filter(last -> last.getGueltigkeit().endsAfter(gueltigkeit))
			.map(BetreuungspensumContainer::copyWithPensumJA)
			.map(copy -> {
				copy.getGueltigkeit().setGueltigAb(gueltigkeit.getGueltigBis().plusDays(1));

				return copy;
			});

		GueltigkeitsUtil.findFirst(containersToUpdate)
			.filter(first -> first.getGueltigkeit().startsBefore(gueltigkeit))
			.ifPresent(first -> first.getGueltigkeit().setGueltigBis(gueltigkeit.getGueltigAb().minusDays(1)));

		// everything still affecting gueltigkeit is obsolete (will be replaced with import data)
		containersToUpdate.removeIf(c -> c.getGueltigkeit().intersects(gueltigkeit));

		List<BetreuungspensumContainer> toImport =
			convertZeitabschnitte(ctx, gueltigkeit, z -> toBetreuungspensumContainer(z, ctx));

		overlappingGueltigBis.ifPresent(containersToUpdate::add);
		betreuung.getBetreuungspensumContainers().addAll(containersToUpdate);
		betreuung.getBetreuungspensumContainers().addAll(toImport);
	}

	@Nonnull
	private static <T extends Gueltigkeit> List<T> convertZeitabschnitte(
		@Nonnull ProcessingContext ctx,
		@Nonnull DateRange gueltigkeit,
		@Nonnull Function<ZeitabschnittDTO, T> mappingFunction) {

		List<T> toImport = ctx.getDto().getZeitabschnitte().stream()
			.filter(z -> new DateRange(z.getVon(), z.getBis()).intersects(gueltigkeit))
			.map(mappingFunction)
			.collect(Collectors.toList());

		GueltigkeitsUtil.findFirst(toImport)
			.filter(first -> first.getGueltigkeit().startsBefore(gueltigkeit))
			.ifPresent(first -> first.getGueltigkeit().setGueltigAb(gueltigkeit.getGueltigAb()));

		GueltigkeitsUtil.findLast(toImport)
			.filter(last -> last.getGueltigkeit().endsAfter(gueltigkeit))
			.ifPresent(last -> last.getGueltigkeit().setGueltigBis(gueltigkeit.getGueltigBis()));

		return toImport;
	}

	@Nonnull
	private static BetreuungspensumContainer toBetreuungspensumContainer(
		@Nonnull ZeitabschnittDTO zeitabschnittDTO,
		@Nonnull ProcessingContext ctx) {

		Betreuungspensum betreuungspensum = toAbstractMahlzeitenPensum(new Betreuungspensum(), zeitabschnittDTO, ctx);

		BetreuungspensumContainer container = new BetreuungspensumContainer();
		container.setBetreuungspensumJA(betreuungspensum);
		container.setBetreuung(ctx.getBetreuung());

		return container;
	}

	public static void addZeitabschnitteToBetreuungsmitteilung(
		@Nonnull ProcessingContext ctx,
		@Nullable Betreuungsmitteilung latest,
		@Nonnull Betreuungsmitteilung betreuungsmitteilung) {

		DateRange mutationRange = getMutationRange(ctx);

		List<BetreuungsmitteilungPensum> existing = getExisting(ctx, latest, mutationRange);

		List<BetreuungsmitteilungPensum> toImport =
			convertZeitabschnitte(ctx, mutationRange, z -> toBetreuungsmitteilungPensum(z, ctx));

		existing.addAll(toImport);
		betreuungsmitteilung.getBetreuungspensen().addAll(extendGueltigkeit(existing));
		betreuungsmitteilung.getBetreuungspensen().forEach(p -> p.setBetreuungsmitteilung(betreuungsmitteilung));
	}

	@Nonnull
	private static DateRange getMutationRange(@Nonnull ProcessingContext ctx) {
		DateRange gueltigkeitInPeriode = ctx.getGueltigkeitInPeriode();

		return gueltigkeitInPeriode.getGueltigAb().isBefore(GO_LIVE) ?
			new DateRange(GO_LIVE, gueltigkeitInPeriode.getGueltigBis()) :
			gueltigkeitInPeriode;
	}

	@Nonnull
	private static List<BetreuungsmitteilungPensum> getExisting(
		@Nonnull ProcessingContext ctx,
		@Nullable Betreuungsmitteilung latest,
		@Nonnull DateRange mutationRange) {

		LocalDate mutationRangeAb = mutationRange.getGueltigAb();
		LocalDate mutationRangeBis = mutationRange.getGueltigBis();

		List<BetreuungsmitteilungPensum> existing =
			getExistingFromLatestOrBetreuung(ctx.getBetreuung(), latest, mutationRange);

		Optional<BetreuungsmitteilungPensum> overlappingGueltigBis =
			GueltigkeitsUtil.findAnyAtStichtag(existing, mutationRangeBis)
				.filter(overlappingBis -> overlappingBis.getGueltigkeit().endsAfter(mutationRange))
				.map(BetreuungsmitteilungPensum::copy)
				.map(copy -> {
					copy.getGueltigkeit().setGueltigAb(mutationRangeBis.plusDays(1));

					return copy;
				});

		GueltigkeitsUtil.findAnyAtStichtag(existing, mutationRangeAb)
			.filter(overlappingAb -> overlappingAb.getGueltigkeit().startsBefore(mutationRange))
			.ifPresent(overlappingAb -> overlappingAb.getGueltigkeit().setGueltigBis(mutationRangeAb.minusDays(1)));

		overlappingGueltigBis.ifPresent(existing::add);

		return existing;
	}

	@Nonnull
	private static List<BetreuungsmitteilungPensum> getExistingFromLatestOrBetreuung(
		@Nonnull Betreuung betreuung,
		@Nullable Betreuungsmitteilung latest,
		@Nonnull DateRange mutationRange) {

		if (latest == null) {
			return betreuung.getBetreuungspensumContainers().stream()
				.filter(c -> !mutationRange.contains(c.getGueltigkeit()))
				.map(PensumMappingUtil::fromBetreuungspensumContainer)
				.collect(Collectors.toList());
		}

		return latest.getBetreuungspensen().stream()
			.filter(c -> !mutationRange.contains(c.getGueltigkeit()))
			.map(BetreuungsmitteilungPensum::copy)
			.collect(Collectors.toList());
	}

	@Nonnull
	private static BetreuungsmitteilungPensum fromBetreuungspensumContainer(
		@Nonnull BetreuungspensumContainer container) {
		BetreuungsmitteilungPensum pensum = new BetreuungsmitteilungPensum();

		container.getBetreuungspensumJA()
			.copyAbstractBetreuungspensumMahlzeitenEntity(pensum, AntragCopyType.MUTATION);

		return pensum;
	}

	@Nonnull
	private static BetreuungsmitteilungPensum toBetreuungsmitteilungPensum(
		@Nonnull ZeitabschnittDTO zeitabschnitt,
		@Nonnull ProcessingContext ctx) {

		return toAbstractMahlzeitenPensum(new BetreuungsmitteilungPensum(), zeitabschnitt, ctx);
	}

	@Nonnull
	@CanIgnoreReturnValue
	static <T extends AbstractMahlzeitenPensum> T toAbstractMahlzeitenPensum(
		@Nonnull T target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO,
		@Nonnull ProcessingContext ctx) {

		target.getGueltigkeit().setGueltigAb(zeitabschnittDTO.getVon());
		target.getGueltigkeit().setGueltigBis(zeitabschnittDTO.getBis());
		target.setMonatlicheBetreuungskosten(zeitabschnittDTO.getBetreuungskosten());
		setPensum(target, zeitabschnittDTO);
		target.setMonatlicheHauptmahlzeiten(coalesce(zeitabschnittDTO.getAnzahlHauptmahlzeiten(), ZERO));
		target.setMonatlicheNebenmahlzeiten(coalesce(zeitabschnittDTO.getAnzahlNebenmahlzeiten(), ZERO));

		if (ctx.isMahlzeitVergunstigungEnabled()) {
			setTarifeProMahlzeiten(target, zeitabschnittDTO, ctx);
		}

		return target;
	}

	private static <T extends AbstractMahlzeitenPensum> void setPensum(
		@Nonnull T target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO) {

		switch (zeitabschnittDTO.getPensumUnit()) {
		case DAYS:
			target.applyPensumFromDays(zeitabschnittDTO.getBetreuungspensum());
			break;
		case HOURS:
			target.applyPensumFromHours(zeitabschnittDTO.getBetreuungspensum());
			break;
		case PERCENTAGE:
			target.applyPensumFromPercentage(zeitabschnittDTO.getBetreuungspensum());
			break;
		default:
			throw new IllegalArgumentException("Unsupported pensum unit: " + zeitabschnittDTO.getPensumUnit());
		}
	}

	private static <T extends AbstractMahlzeitenPensum> void setTarifeProMahlzeiten(
		@Nonnull T target,
		@Nonnull ZeitabschnittDTO zeitabschnittDTO,
		@Nonnull ProcessingContext ctx) {

		// Die Mahlzeitkosten koennen null sein, wir nehmen dann die default Werten
		if (zeitabschnittDTO.getTarifProHauptmahlzeiten() != null) {
			target.setTarifProHauptmahlzeit(zeitabschnittDTO.getTarifProHauptmahlzeiten());
		} else {
			target.setVollstaendig(false);
			ctx.requireHumanConfirmation();
		}
		if (zeitabschnittDTO.getTarifProNebenmahlzeiten() != null) {
			target.setTarifProNebenmahlzeit(zeitabschnittDTO.getTarifProNebenmahlzeiten());
		} else {
			target.setVollstaendig(false);
			ctx.requireHumanConfirmation();
		}
	}

	/**
	 * When adjacent pensen are comparable, merge them together to one pensum with extended Gueltigkeit
	 */
	@Nonnull
	static Collection<BetreuungsmitteilungPensum> extendGueltigkeit(
		@Nonnull Collection<BetreuungsmitteilungPensum> pensen) {

		if (pensen.size() <= 1) {
			return pensen;
		}

		List<BetreuungsmitteilungPensum> sorted = pensen.stream()
			.sorted(Gueltigkeit.GUELTIG_AB_COMPARATOR)
			.collect(Collectors.toList());

		List<BetreuungsmitteilungPensum> result = new ArrayList<>();
		Iterator<BetreuungsmitteilungPensum> iter = sorted.iterator();
		BetreuungsmitteilungPensum current = iter.next();
		result.add(current);

		while (iter.hasNext()) {
			BetreuungsmitteilungPensum next = iter.next();

			if (areAdjacent(current, next) && areSame(current, next)) {
				// extend gueltigkeit of current
				current.getGueltigkeit().setGueltigBis(next.getGueltigkeit().getGueltigBis());
				continue;
			}

			current = next;
			result.add(next);
		}

		return result;
	}

	private static boolean areAdjacent(@Nonnull Gueltigkeit current, @Nonnull Gueltigkeit next) {
		return current.getGueltigkeit().endsDayBefore(next.getGueltigkeit());
	}

	private static boolean areSame(
		@Nonnull BetreuungsmitteilungPensum current,
		@Nonnull BetreuungsmitteilungPensum next) {

		return PlatzbestaetigungEventHandler.COMPARATOR.compare(current, next) == 0;
	}
}
