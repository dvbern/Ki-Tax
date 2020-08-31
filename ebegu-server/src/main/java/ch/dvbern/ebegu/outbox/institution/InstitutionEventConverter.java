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

package ch.dvbern.ebegu.outbox.institution;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.KontaktAngaben;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.kibon.exchange.commons.institution.AltersKategorie;
import ch.dvbern.kibon.exchange.commons.institution.GemeindeDTO;
import ch.dvbern.kibon.exchange.commons.institution.InstitutionEventDTO;
import ch.dvbern.kibon.exchange.commons.institution.InstitutionEventDTO.Builder;
import ch.dvbern.kibon.exchange.commons.institution.KontaktAngabenDTO;
import ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp;
import ch.dvbern.kibon.exchange.commons.types.Wochentag;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import ch.dvbern.kibon.exchange.commons.util.DateConverter;
import ch.dvbern.kibon.exchange.commons.util.TimeConverter;

@ApplicationScoped
public class InstitutionEventConverter {

	@Nonnull
	public InstitutionChangedEvent of(@Nonnull InstitutionStammdaten stammdaten) {
		InstitutionEventDTO dto = toInstitutionEventDTO(stammdaten);
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new InstitutionChangedEvent(stammdaten.getInstitution().getId(), payload, dto.getSchema());
	}

	@Nonnull
	private InstitutionEventDTO toInstitutionEventDTO(@Nonnull InstitutionStammdaten stammdaten) {
		Institution institution = stammdaten.getInstitution();

		KontaktAngabenDTO institutionKontaktAngaben = toKontaktAngabenDTO(stammdaten);

		Builder builder = InstitutionEventDTO.newBuilder()
			.setId(institution.getId())
			.setName(institution.getName())
			.setTraegerschaft(getTraegerschaft(institution))
			.setBetreuungsArt(BetreuungsangebotTyp.valueOf(stammdaten.getBetreuungsangebotTyp().name()))
			.setAdresse(institutionKontaktAngaben)
			.setTimestampMutiert(DateConverter.serialize(DateConverter.of(LocalDateTime.now())));

		InstitutionStammdatenBetreuungsgutscheine bgStammdaten =
			stammdaten.getInstitutionStammdatenBetreuungsgutscheine();
		if (bgStammdaten != null) {
			builder
				.setBetreuungsAdressen(getBetreuungsAdressen(institutionKontaktAngaben, bgStammdaten))
				.setOeffnungsTage(getOeffnungsTage(bgStammdaten))
				.setOffenVon(TimeConverter.serialize(bgStammdaten.getOffenVon()))
				.setOffenBis(TimeConverter.serialize(bgStammdaten.getOffenBis()))
				.setOeffnungsAbweichungen(bgStammdaten.getOeffnungsAbweichungen())
				.setAltersKategorien(getAltersKategorien(bgStammdaten))
				.setSubventioniertePlaetze(bgStammdaten.getSubventioniertePlaetze())
				.setAnzahlPlaetze(MathUtil.ZWEI_NACHKOMMASTELLE.from(bgStammdaten.getAnzahlPlaetze()))
				.setAnzahlPlaetzeFirmen(MathUtil.ZWEI_NACHKOMMASTELLE.from(bgStammdaten.getAnzahlPlaetzeFirmen()))
			;
		}

		return builder.build();
	}

	@Nonnull
	private List<KontaktAngabenDTO> getBetreuungsAdressen(
		@Nonnull KontaktAngabenDTO institutionKontaktAngaben,
		@Nonnull InstitutionStammdatenBetreuungsgutscheine bgStammdaten) {

		List<KontaktAngabenDTO> betreuungsStandorte = bgStammdaten.getBetreuungsstandorte().stream()
			.map(this::toKontaktAngabenDTO)
			.collect(Collectors.toList());

		// implicitly, the institution address is also a betreuungs address
		betreuungsStandorte.add(0, institutionKontaktAngaben);

		return betreuungsStandorte;
	}

	@Nonnull
	private List<Wochentag> getOeffnungsTage(@Nonnull InstitutionStammdatenBetreuungsgutscheine bgStammdaten) {
		return bgStammdaten.getOeffnungsTage().stream()
			.sorted()
			.map(dayOfWeek -> Wochentag.valueOf(dayOfWeek.name()))
			.collect(Collectors.toList());
	}

	@Nonnull
	private List<AltersKategorie> getAltersKategorien(InstitutionStammdatenBetreuungsgutscheine bgStammdaten) {
		List<AltersKategorie> result = new ArrayList<>();

		if (bgStammdaten.getAlterskategorieBaby()) {
			result.add(AltersKategorie.BABY);
		}

		if (bgStammdaten.getAlterskategorieVorschule()) {
			result.add(AltersKategorie.VORSCHULE);
		}

		if (bgStammdaten.getAlterskategorieKindergarten()) {
			result.add(AltersKategorie.KINDERGARTEN);
		}

		if (bgStammdaten.getAlterskategorieSchule()) {
			result.add(AltersKategorie.SCHULE);
		}

		return result;
	}

	@Nullable
	private String getTraegerschaft(@Nonnull Institution institution) {
		return Optional.ofNullable(institution.getTraegerschaft())
			.map(Traegerschaft::getName)
			.orElse(null);
	}

	@Nonnull
	private KontaktAngabenDTO toKontaktAngabenDTO(@Nonnull KontaktAngaben kontaktAngaben) {
		Adresse adr = kontaktAngaben.getAdresse();

		return KontaktAngabenDTO.newBuilder()
			.setAnschrift(adr.getOrganisation())
			.setStrasse(adr.getStrasse())
			.setHausnummer(adr.getHausnummer())
			.setAdresszusatz(adr.getZusatzzeile())
			.setPlz(adr.getPlz())
			.setOrt(adr.getOrt())
			.setLand(adr.getLand().name())
			.setGemeinde(toGemeindeDTO(adr))
			.setEmail(kontaktAngaben.getMail())
			.setTelefon(kontaktAngaben.getTelefon())
			.setWebseite(kontaktAngaben.getWebseite())
			.build();
	}

	@Nonnull
	private GemeindeDTO toGemeindeDTO(@Nonnull Adresse adr) {
		return GemeindeDTO.newBuilder()
			.setName(adr.getGemeinde()).build();
	}
}
