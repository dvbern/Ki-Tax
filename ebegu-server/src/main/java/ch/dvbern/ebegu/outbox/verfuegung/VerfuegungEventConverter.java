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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp;
import ch.dvbern.kibon.exchange.commons.types.Regelwerk;
import ch.dvbern.kibon.exchange.commons.types.Zeiteinheit;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import ch.dvbern.kibon.exchange.commons.verfuegung.GesuchstellerDTO;
import ch.dvbern.kibon.exchange.commons.verfuegung.KindDTO;
import ch.dvbern.kibon.exchange.commons.verfuegung.VerfuegungEventDTO;
import ch.dvbern.kibon.exchange.commons.verfuegung.ZeitabschnittDTO;

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
	public Optional<VerfuegungVerfuegtEvent> of(@Nonnull Verfuegung verfuegung) {
		VerfuegungEventDTO dto = toVerfuegungEventDTO(verfuegung);
		if (dto == null) {
			return Optional.empty();
		}
		byte[] payload = AvroConverter.toAvroBinary(dto);

		Objects.requireNonNull(verfuegung.getBetreuung());
		return Optional.of(new VerfuegungVerfuegtEvent(verfuegung.getBetreuung().getBGNummer(), payload, dto.getSchema()));
	}

	@Nullable
	private VerfuegungEventDTO toVerfuegungEventDTO(@Nonnull Verfuegung verfuegung) {
		Betreuung betreuung = verfuegung.getBetreuung();
		if (betreuung == null) {
			return null;
		}
		Gesuch gesuch = betreuung.extractGesuch();
		Gemeinde gemeinde = gesuch.extractGemeinde();
		Gesuchsteller gesuchsteller = requireNonNull(gesuch.getGesuchsteller1()).getGesuchstellerJA();
		Kind kind = betreuung.getKind().getKindJA();

		DateRange periode = betreuung.extractGesuchsperiode().getGueltigkeit();
		LocalDateTime timestampErstellt = verfuegung.getTimestampErstellt();
		Instant verfuegtAm = requireNonNull(timestampErstellt).atZone(ZoneId.systemDefault()).toInstant();

		VerfuegungEventDTO.Builder builder = VerfuegungEventDTO.newBuilder()
			.setKind(toKindDTO(kind))
			.setGesuchsteller(toGesuchstellerDTO(gesuchsteller))
			.setBetreuungsArt(BetreuungsangebotTyp.valueOf(requireNonNull(betreuung.getBetreuungsangebotTyp()).name()))
			.setRefnr(betreuung.getBGNummer())
			.setInstitutionId(betreuung.getInstitutionStammdaten().getInstitution().getId())
			.setVon(periode.getGueltigAb())
			.setBis(periode.getGueltigBis())
			.setVersion(gesuch.getLaufnummer())
			.setVerfuegtAm(verfuegtAm)
			.setGemeindeBfsNr(gemeinde.getBfsNummer())
			.setGemeindeName(gemeinde.getName());

		setZeitabschnitte(verfuegung, builder);

		return builder.build();
	}

	@Nonnull
	private KindDTO toKindDTO(@Nonnull Kind kind) {
		return KindDTO.newBuilder()
			.setVorname(kind.getVorname())
			.setNachname(kind.getNachname())
			.setGeburtsdatum(kind.getGeburtsdatum())
			.build();
	}

	@Nonnull
	private GesuchstellerDTO toGesuchstellerDTO(@Nonnull Gesuchsteller gesuchsteller) {
		//noinspection ConstantConditions
		return GesuchstellerDTO.newBuilder()
			.setVorname(gesuchsteller.getVorname())
			.setNachname(gesuchsteller.getNachname())
			.setEmail(gesuchsteller.getMail())
			.build();
	}

	private void setZeitabschnitte(@Nonnull Verfuegung verfuegung, @Nonnull VerfuegungEventDTO.Builder builder) {

		Map<Boolean, List<VerfuegungZeitabschnitt>> abschnitteByIgnored = verfuegung.getZeitabschnitte().stream()
			.collect(Collectors.partitioningBy(abschnitt -> abschnitt.getZahlungsstatus().isIgnoriertIgnorierend()));

		List<VerfuegungZeitabschnitt> ignoredAbschnitte = abschnitteByIgnored.getOrDefault(true, emptyList());
		List<VerfuegungZeitabschnitt> verrechnetAbschnitte = abschnitteByIgnored.getOrDefault(false, emptyList());

		// Verrechnete Zeitabschnitte
		Betreuung betreuung = verfuegung.getBetreuung();
		Objects.requireNonNull(betreuung);
		List<VerfuegungZeitabschnitt> allVerrechnet = findVorgaengerZeitabschnitte(betreuung, ignoredAbschnitte);
		allVerrechnet.addAll(verrechnetAbschnitte);

		//noinspection ResultOfMethodCallIgnored
		builder
			.setZeitabschnitte(convertZeitabschnitte(allVerrechnet))
			.setIgnorierteZeitabschnitte(convertZeitabschnitte(ignoredAbschnitte));
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
		MathUtil ROUND = MathUtil.ZWEI_NACHKOMMASTELLE;

		Betreuung betreuung = zeitabschnitt.getVerfuegung().getBetreuung();
		Objects.requireNonNull(betreuung);
		return ZeitabschnittDTO.newBuilder()
			.setVon(zeitabschnitt.getGueltigkeit().getGueltigAb())
			.setBis(zeitabschnitt.getGueltigkeit().getGueltigBis())
			.setVerfuegungNr(betreuung.extractGesuch().getLaufnummer())
			.setEffektiveBetreuungPct(ROUND.from(zeitabschnitt.getBetreuungspensumProzent()))
			.setAnspruchPct(zeitabschnitt.getAnspruchberechtigtesPensum())
			.setVerguenstigtPct(ROUND.from(zeitabschnitt.getBgPensum()))
			.setVollkosten(ROUND.from(zeitabschnitt.getVollkosten()))
			.setBetreuungsgutschein(ROUND.from(zeitabschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag()))
			.setMinimalerElternbeitrag(ROUND.from(zeitabschnitt.getMinimalerElternbeitragGekuerzt()))
			.setVerguenstigung(ROUND.from(zeitabschnitt.getVerguenstigung()))
			.setVerfuegteAnzahlZeiteinheiten(ROUND.from(zeitabschnitt.getVerfuegteAnzahlZeiteinheiten()))
			.setAnspruchsberechtigteAnzahlZeiteinheiten(ROUND.from(zeitabschnitt.getAnspruchsberechtigteAnzahlZeiteinheiten()))
			.setZeiteinheit(Zeiteinheit.valueOf(zeitabschnitt.getZeiteinheit().name()))
			.setRegelwerk(Regelwerk.valueOf(zeitabschnitt.getRegelwerk().name()))
			.build();
	}
}
