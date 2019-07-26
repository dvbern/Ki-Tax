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

package ch.dvbern.ebegu.outbox.verfuegung;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.kibon.exchange.commons.ObjectMapperUtil;
import ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp;
import ch.dvbern.kibon.exchange.commons.verfuegung.GesuchstellerDTO;
import ch.dvbern.kibon.exchange.commons.verfuegung.KindDTO;
import ch.dvbern.kibon.exchange.commons.verfuegung.VerfuegungEventDTO;
import ch.dvbern.kibon.exchange.commons.verfuegung.ZeitabschnittDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

@ApplicationScoped
public class VerfuegungEventConverter {

	private static final Comparator<ZeitabschnittDTO> ZEITABSCHNITT_COMPARATOR = Comparator
		.comparing(ZeitabschnittDTO::getVon)
		.thenComparing(ZeitabschnittDTO::getBis);

	@Inject
	private VerfuegungService verfuegungService;

	@Nonnull
	public VerfuegungVerfuegtEvent of(@Nonnull Verfuegung verfuegung) {
		VerfuegungEventDTO dto = toVerfuegungEventDTO(verfuegung);

		try {
			byte[] bytes = ObjectMapperUtil.MAPPER.writeValueAsBytes(dto);

			return new VerfuegungVerfuegtEvent(verfuegung.getBetreuung().getBGNummer(), bytes);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("failed converting to jsonb", e);
		}
	}

	@Nonnull
	private VerfuegungEventDTO toVerfuegungEventDTO(@Nonnull Verfuegung verfuegung) {
		Betreuung betreuung = verfuegung.getBetreuung();
		Gesuch gesuch = betreuung.extractGesuch();
		Gesuchsteller gesuchsteller = requireNonNull(gesuch.getGesuchsteller1()).getGesuchstellerJA();
		Kind kind = betreuung.getKind().getKindJA();

		VerfuegungEventDTO verfuegungDTO = new VerfuegungEventDTO(
			toKindDTO(kind),
			toGesuchstellerDTO(gesuchsteller),
			BetreuungsangebotTyp.valueOf(requireNonNull(betreuung.getBetreuungsangebotTyp()).name())
		);
		verfuegungDTO.setRefnr(betreuung.getBGNummer());
		verfuegungDTO.setInstitutionId(betreuung.getInstitutionStammdaten().getInstitution().getId());

		DateRange periode = betreuung.extractGesuchsperiode().getGueltigkeit();
		verfuegungDTO.setVon(periode.getGueltigAb());
		verfuegungDTO.setBis(periode.getGueltigBis());

		verfuegungDTO.setVersion(gesuch.getLaufnummer());
		verfuegungDTO.setVerfuegtAm(requireNonNull(verfuegung.getTimestampErstellt()));

		addZeitabschnitte(verfuegung, verfuegungDTO);

		return verfuegungDTO;
	}

	@Nonnull
	private KindDTO toKindDTO(@Nonnull Kind kind) {
		return new KindDTO(kind.getVorname(), kind.getNachname(), kind.getGeburtsdatum());
	}

	@Nonnull
	private GesuchstellerDTO toGesuchstellerDTO(@Nonnull Gesuchsteller gesuchsteller) {
		return new GesuchstellerDTO(gesuchsteller.getVorname(), gesuchsteller.getNachname(), gesuchsteller.getMail());
	}

	private void addZeitabschnitte(@Nonnull Verfuegung verfuegung, @Nonnull VerfuegungEventDTO verfuegungDTO) {

		Map<Boolean, List<VerfuegungZeitabschnitt>> abschnitteByIgnored = verfuegung.getZeitabschnitte().stream()
			.collect(Collectors.partitioningBy(abschnitt -> abschnitt.getZahlungsstatus().isIgnoriertIgnorierend()));

		List<VerfuegungZeitabschnitt> ignoredAbschnitte = abschnitteByIgnored.getOrDefault(true, emptyList());
		List<VerfuegungZeitabschnitt> verrechnetAbschnitte = abschnitteByIgnored.getOrDefault(false, emptyList());

		// Verrechnete Zeitabschnitte
		Betreuung betreuung = verfuegung.getBetreuung();
		List<VerfuegungZeitabschnitt> allVerrechnet = findVorgaengerZeitabschnitte(betreuung, ignoredAbschnitte);
		allVerrechnet.addAll(verrechnetAbschnitte);

		verfuegungDTO.setZeitabschnitte(convertZeitabschnitte(allVerrechnet));

		// Ignorierte Zeitabschnitte
		verfuegungDTO.setIgnorierteZeitabschnitte(convertZeitabschnitte(ignoredAbschnitte));
	}

	@Nonnull
	private List<VerfuegungZeitabschnitt> findVorgaengerZeitabschnitte(
		@Nonnull Betreuung betreuung,
		@Nonnull List<VerfuegungZeitabschnitt> ignoredAbschnitte) {
		// Zusätzlich zu den Abschnitten der aktuellen Verfuegung müssen auch eventuell noch gueltige Abschnitte
		// von frueheren Verfuegungen exportiert werden: immer dann, wenn in der aktuellen Verfuegung ignoriert wurde!
		List<VerfuegungZeitabschnitt> nochGueltigeZeitabschnitte = new ArrayList<>();

		ignoredAbschnitte.forEach(z -> verfuegungService
			.findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(z, betreuung, nochGueltigeZeitabschnitte));

		return nochGueltigeZeitabschnitte;
	}

	@Nonnull
	private List<ZeitabschnittDTO> convertZeitabschnitte(@Nonnull List<VerfuegungZeitabschnitt> abschnitte) {
		return abschnitte.stream()
			.map(this::toZeitabschnittDTO)
			.sorted(ZEITABSCHNITT_COMPARATOR)
			.collect(Collectors.toList());
	}

	@Nonnull
	private ZeitabschnittDTO toZeitabschnittDTO(@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {

		return new ZeitabschnittDTO(
			zeitabschnitt.getGueltigkeit().getGueltigAb(),
			zeitabschnitt.getGueltigkeit().getGueltigBis(),
			zeitabschnitt.getVerfuegung().getBetreuung().extractGesuch().getLaufnummer(),
			zeitabschnitt.getBetreuungspensum(),
			zeitabschnitt.getAnspruchberechtigtesPensum(),
			zeitabschnitt.getBgPensum(),
			zeitabschnitt.getVollkosten(),
			zeitabschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag(),
			zeitabschnitt.getMinimalerElternbeitragGekuerzt(),
			zeitabschnitt.getVerguenstigung());
	}
}
