/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.inbox.handler.pensum;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.AbstractBetreuungsPensum;
import ch.dvbern.ebegu.entities.AbstractMahlzeitenPensum;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Betreuungspensum;
import ch.dvbern.ebegu.entities.BetreuungspensumContainer;
import ch.dvbern.ebegu.entities.EingewoehnungPauschale;
import ch.dvbern.ebegu.enums.AntragCopyType;
import ch.dvbern.ebegu.inbox.handler.ProcessingContext;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Gueltigkeit;
import ch.dvbern.ebegu.util.GueltigkeitsUtil;
import ch.dvbern.kibon.exchange.commons.platzbestaetigung.ZeitabschnittDTO;
import lombok.experimental.UtilityClass;

import static ch.dvbern.ebegu.util.EbeguUtil.collectionComparator;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@UtilityClass
public final class PensumMappingUtil {

	public static final LocalDate GO_LIVE = LocalDate.of(2021, 1, 1);

	public static final Comparator<EingewoehnungPauschale> EINGEWOEHNUNG_PAUSCHALE_COMPARATOR = Comparator
		.comparing(EingewoehnungPauschale::getPauschale)
		.thenComparing(EingewoehnungPauschale::getGueltigkeit);

	public static final Comparator<AbstractBetreuungsPensum> COMPARATOR = Comparator
		.comparing(AbstractBetreuungsPensum::getMonatlicheBetreuungskosten)
		.thenComparing(AbstractBetreuungsPensum::getPensumRounded)
		.thenComparing(AbstractBetreuungsPensum::getTarifProHauptmahlzeit)
		.thenComparing(AbstractBetreuungsPensum::getTarifProNebenmahlzeit)
		.thenComparing(AbstractBetreuungsPensum::getMonatlicheHauptmahlzeiten)
		.thenComparing(AbstractBetreuungsPensum::getMonatlicheNebenmahlzeiten)
		.thenComparing(AbstractBetreuungsPensum::getEingewoehnungPauschale, nullsFirst(EINGEWOEHNUNG_PAUSCHALE_COMPARATOR))
		.thenComparing(AbstractBetreuungsPensum::getBetreuungInFerienzeit, nullsFirst(naturalOrder()));

	public static final Comparator<AbstractBetreuungsPensum> COMPARATOR_WITH_GUELTIGKEIT = PensumMappingUtil.COMPARATOR
		.thenComparing(AbstractMahlzeitenPensum::getGueltigkeit);

	public static final Comparator<Betreuungsmitteilung> MITTEILUNG_COMPARATOR = Comparator
		.comparing(Betreuungsmitteilung::getBetreuungspensen, collectionComparator(COMPARATOR_WITH_GUELTIGKEIT));

	public static void addZeitabschnitteToBetreuung(
		@Nonnull ProcessingContext ctx,
		@Nonnull PensumMapper<Betreuungspensum> mapper
	) {
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

		overlappingGueltigBis.ifPresent(containersToUpdate::add);
		// everything still affecting gueltigkeit is obsolete (will be replaced with import data)
		containersToUpdate.removeIf(c -> c.getGueltigkeit().intersects(gueltigkeit));

		List<BetreuungspensumContainer> toImport =
			convertZeitabschnitte(ctx, gueltigkeit, z -> toBetreuungspensumContainer(z, ctx, mapper));

		writeBack(
			betreuung.getBetreuungspensumContainers(),
			BetreuungspensumContainer::getBetreuungspensumJA,
			ctx,
			containersToUpdate,
			toImport);
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
		@Nonnull ProcessingContext ctx,
		@Nonnull PensumMapper<Betreuungspensum> mapper) {

		Betreuungspensum betreuungspensum = new Betreuungspensum();
		mapper.toAbstractMahlzeitenPensum(betreuungspensum, zeitabschnittDTO);

		BetreuungspensumContainer container = new BetreuungspensumContainer();
		container.setBetreuungspensumJA(betreuungspensum);
		container.setBetreuung(ctx.getBetreuung());

		return container;
	}

	public static void addZeitabschnitteToBetreuungsmitteilung(
		@Nonnull ProcessingContext ctx,
		@Nullable Betreuungsmitteilung latest,
		@Nonnull Betreuungsmitteilung betreuungsmitteilung,
		@Nonnull PensumMapper<BetreuungsmitteilungPensum> mapper
	) {
		DateRange mutationRange = getMutationRange(ctx);

		List<BetreuungsmitteilungPensum> existing = getExisting(ctx, latest, mutationRange);

		List<BetreuungsmitteilungPensum> toImport =
			convertZeitabschnitte(ctx, mutationRange, z -> toBetreuungsmitteilungPensum(z, mapper));

		writeBack(betreuungsmitteilung.getBetreuungspensen(), a -> a, ctx, existing, toImport);

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
		// everything still affecting gueltigkeit is obsolete (will be replaced with import data)
		existing.removeIf(c -> c.getGueltigkeit().intersects(mutationRange));

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
		@Nonnull PensumMapper<BetreuungsmitteilungPensum> mapper) {

		BetreuungsmitteilungPensum betreuungsmitteilungPensum = new BetreuungsmitteilungPensum();
		mapper.toAbstractMahlzeitenPensum(betreuungsmitteilungPensum, zeitabschnitt);

		return betreuungsmitteilungPensum;
	}

	@SafeVarargs
	private static <T extends Gueltigkeit> void writeBack(
		@Nonnull Set<T> target,
		@Nonnull Function<T, AbstractBetreuungsPensum> mapper,
		@Nonnull ProcessingContext ctx,
		@Nonnull Collection<T>... remaining) {

		DateRange periode = ctx.getBetreuung().extractGesuchsperiode().getGueltigkeit();

		Set<T> tmp = Arrays.stream(remaining)
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());

		Collection<T> extended = extendGueltigkeit(tmp, mapper);

		target.addAll(extended);
		target.removeIf(z -> !periode.intersects(z.getGueltigkeit()));


		DateRange institutionGueltigkeit = ctx.getBetreuung().getInstitutionStammdaten().getGueltigkeit();

		//Remove Zeitabschnitte ausserhalb der Institution Gueltigkeit
		target.removeIf(z -> !institutionGueltigkeit.intersects(z.getGueltigkeit()));
		//Adapt die Potentielle uberschrittende Zeitabschnitten
		target.forEach(z ->
			z.setGueltigkeit(institutionGueltigkeit.getOverlap(z.getGueltigkeit()).get())
		);
	}

	/**
	 * When adjacent pensen are comparable, merge them together to one pensum with extended Gueltigkeit
	 */
	@Nonnull
	static <T extends Gueltigkeit> Collection<T> extendGueltigkeit(
		@Nonnull Collection<T> pensen,
		@Nonnull Function<T, AbstractBetreuungsPensum> mapper) {

		if (pensen.size() <= 1) {
			return pensen;
		}

		List<T> sorted = pensen.stream()
			.sorted(Gueltigkeit.GUELTIG_AB_COMPARATOR)
			.collect(Collectors.toList());

		List<T> result = new ArrayList<>();
		Iterator<T> iter = sorted.iterator();
		T current = iter.next();
		result.add(current);

		while (iter.hasNext()) {
			T next = iter.next();

			if (areAdjacent(current, next) && areSame(mapper.apply(current), mapper.apply(next))) {
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
		@Nonnull AbstractBetreuungsPensum current,
		@Nonnull AbstractBetreuungsPensum next) {

		return COMPARATOR.compare(current, next) == 0;
	}
}
