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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.EinstellungenTagesschule;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.InstitutionStammdatenBetreuungsgutscheine;
import ch.dvbern.ebegu.entities.InstitutionStammdatenTagesschule;
import ch.dvbern.ebegu.entities.KontaktAngaben;
import ch.dvbern.ebegu.entities.ModulTagesschuleGroup;
import ch.dvbern.ebegu.entities.Traegerschaft;
import ch.dvbern.ebegu.enums.ModulTagesschuleIntervall;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.util.Constants;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.kibon.exchange.commons.institution.AltersKategorie;
import ch.dvbern.kibon.exchange.commons.institution.GemeindeDTO;
import ch.dvbern.kibon.exchange.commons.institution.InstitutionEventDTO;
import ch.dvbern.kibon.exchange.commons.institution.InstitutionEventDTO.Builder;
import ch.dvbern.kibon.exchange.commons.institution.InstitutionStatus;
import ch.dvbern.kibon.exchange.commons.institution.KontaktAngabenDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.ModulDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleModuleDTO;
import ch.dvbern.kibon.exchange.commons.types.BetreuungsangebotTyp;
import ch.dvbern.kibon.exchange.commons.types.Intervall;
import ch.dvbern.kibon.exchange.commons.types.Wochentag;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import ch.dvbern.kibon.exchange.commons.util.TimeConverter;
import ch.dvbern.kibon.exchange.commons.util.TimestampConverter;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

@ApplicationScoped
public class InstitutionEventConverter {

	@Nonnull
	public InstitutionChangedEvent of(@Nonnull InstitutionStammdaten stammdaten) {
		InstitutionEventDTO dto = toInstitutionEventDTO(stammdaten);

		return toEvent(stammdaten.getInstitution().getId(), dto);
	}

	@Nonnull
	public InstitutionChangedEvent deleteEvent(@Nonnull InstitutionStammdaten stammdaten) {
		InstitutionEventDTO dto = toInstitutionEventDTO(stammdaten);
		dto.setStatus(InstitutionStatus.DELETED);

		return toEvent(stammdaten.getInstitution().getId(), dto);
	}

	@Nonnull
	private InstitutionChangedEvent toEvent(@Nonnull String institutionId, InstitutionEventDTO dto) {
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new InstitutionChangedEvent(institutionId, payload, dto.getSchema());
	}

	@Nonnull
	private InstitutionEventDTO toInstitutionEventDTO(@Nonnull InstitutionStammdaten stammdaten) {
		Institution institution = stammdaten.getInstitution();

		String alternativeEmail = stammdaten.getInstitutionStammdatenBetreuungsgutscheine() != null ?
			stammdaten.getInstitutionStammdatenBetreuungsgutscheine().getAlternativeEmailFamilienportal() :
			null;

		String email = preferAlternativeEmail(stammdaten, alternativeEmail);
		KontaktAngabenDTO kontaktAngaben = toKontaktAngabenDTO(stammdaten, email);

		//noinspection ConstantConditions
		Builder builder = InstitutionEventDTO.newBuilder()
			.setId(institution.getId())
			.setName(institution.getName())
			.setTraegerschaft(getTraegerschaft(institution))
			.setBetreuungsArt(BetreuungsangebotTyp.valueOf(stammdaten.getBetreuungsangebotTyp().name()))
			.setStatus(InstitutionStatus.valueOf(institution.getStatus().name()))
			.setAdresse(kontaktAngaben)
			.setTimestampMutiert(TimestampConverter.serialize(TimestampConverter.of(LocalDateTime.now())));

		InstitutionStammdatenBetreuungsgutscheine bgStammdaten =
			stammdaten.getInstitutionStammdatenBetreuungsgutscheine();
		if (bgStammdaten != null) {
			List<KontaktAngabenDTO> adressen = getBetreuungsAdressen(kontaktAngaben, bgStammdaten, alternativeEmail);
			setStammdatenBetreuungsgutscheine(builder, stammdaten.getGueltigkeit(), adressen, bgStammdaten);
		}

		InstitutionStammdatenTagesschule tsStammdaten = stammdaten.getInstitutionStammdatenTagesschule();
		if (tsStammdaten != null) {
			setStammdatenTagesschule(builder, tsStammdaten);
		}

		return builder.build();
	}

	@Nonnull
	@CanIgnoreReturnValue
	private Builder setStammdatenBetreuungsgutscheine(
		@Nonnull Builder builder,
		@Nonnull DateRange gueltigkeit,
		@Nonnull List<KontaktAngabenDTO> betreuungsAdressen,
		@Nonnull InstitutionStammdatenBetreuungsgutscheine bgStammdaten) {

		//noinspection ConstantConditions
		return builder
			.setBetreuungsGutscheineAb(gueltigkeit.getGueltigAb())
			.setBetreuungsGutscheineBis(getGueltigBis(gueltigkeit))
			.setBetreuungsAdressen(betreuungsAdressen)
			.setOeffnungsTage(getOeffnungsTage(bgStammdaten))
			.setOffenVon(TimeConverter.serialize(bgStammdaten.getOffenVon()))
			.setOffenBis(TimeConverter.serialize(bgStammdaten.getOffenBis()))
			.setOeffnungsAbweichungen(bgStammdaten.getOeffnungsAbweichungen())
			.setAltersKategorien(getAltersKategorien(bgStammdaten))
			.setSubventioniertePlaetze(bgStammdaten.getSubventioniertePlaetze())
			.setAnzahlPlaetze(MathUtil.ZWEI_NACHKOMMASTELLE.from(bgStammdaten.getAnzahlPlaetze()))
			.setAnzahlPlaetzeFirmen(MathUtil.ZWEI_NACHKOMMASTELLE.from(bgStammdaten.getAnzahlPlaetzeFirmen()));
	}

	@Nonnull
	private List<KontaktAngabenDTO> getBetreuungsAdressen(
		@Nonnull KontaktAngabenDTO institutionKontaktAngaben,
		@Nonnull InstitutionStammdatenBetreuungsgutscheine bgStammdaten,
		@Nullable String alternativeEmail) {

		List<KontaktAngabenDTO> betreuungsStandorte = bgStammdaten.getBetreuungsstandorte().stream()
			.map(k -> toKontaktAngabenDTO(k, preferKontaktAngabenEmail(k, alternativeEmail)))
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
	private KontaktAngabenDTO toKontaktAngabenDTO(
		@Nonnull KontaktAngaben kontaktAngaben,
		@Nullable String email) {
		Adresse adr = kontaktAngaben.getAdresse();

		//noinspection ConstantConditions
		return KontaktAngabenDTO.newBuilder()
			.setAnschrift(adr.getOrganisation())
			.setStrasse(adr.getStrasse())
			.setHausnummer(adr.getHausnummer())
			.setAdresszusatz(adr.getZusatzzeile())
			.setPlz(adr.getPlz())
			.setOrt(adr.getOrt())
			.setLand(adr.getLand().name())
			.setGemeinde(toGemeindeDTO(adr))
			.setEmail(email)
			.setTelefon(kontaktAngaben.getTelefon())
			.setWebseite(kontaktAngaben.getWebseite())
			.build();
	}

	@Nullable
	private String preferKontaktAngabenEmail(
		@Nonnull KontaktAngaben kontaktAngaben,
		@Nullable String alternativeEmailFamilienportal) {

		return Optional.ofNullable(kontaktAngaben.getMail())
			.orElse(alternativeEmailFamilienportal);
	}

	@Nullable
	private String preferAlternativeEmail(
		@Nonnull KontaktAngaben kontaktAngaben,
		@Nullable String alternativeEmailFamilienportal) {

		return Optional.ofNullable(alternativeEmailFamilienportal)
			.orElseGet(kontaktAngaben::getMail);
	}

	@Nonnull
	private GemeindeDTO toGemeindeDTO(@Nonnull Adresse adr) {
		//noinspection ConstantConditions
		return GemeindeDTO.newBuilder()
			.setName(adr.getGemeinde())
			.setBfsNummer(adr.getBfsNummer())
			.build();
	}

	/**
	 * @return NULL, if gueltigBis == END_OF_TIME
	 */
	@Nullable
	private LocalDate getGueltigBis(@Nonnull DateRange gueltigkeit) {
		return Constants.END_OF_TIME.equals(gueltigkeit.getGueltigBis()) ? null : gueltigkeit.getGueltigBis();
	}

	@Nonnull
	@CanIgnoreReturnValue
	private Builder setStammdatenTagesschule(
		@Nonnull Builder builder,
		@Nonnull InstitutionStammdatenTagesschule tsStammdaten) {

		List<TagesschuleModuleDTO> tagesschuleModule = tsStammdaten.getEinstellungenTagesschule().stream()
			.map(this::toTagesschuleModuleDTO)
			.collect(Collectors.toList());

		return builder.setTagesschuleModule(tagesschuleModule);
	}

	@Nonnull
	private TagesschuleModuleDTO toTagesschuleModuleDTO(@Nonnull EinstellungenTagesschule e) {
		return TagesschuleModuleDTO.newBuilder()
			.setPeriodeVon(e.getGesuchsperiode().getGueltigkeit().getGueltigAb())
			.setPeriodeBis(e.getGesuchsperiode().getGueltigkeit().getGueltigBis())
			.setModule(toModule(e.getModulTagesschuleGroups()))
			.build();
	}



	@Nonnull
	private List<ModulDTO> toModule(@Nonnull Set<ModulTagesschuleGroup> modulTagesschuleGroups) {
		return modulTagesschuleGroups.stream()
			.map(this::toModul)
			.collect(Collectors.toList());
	}

	@Nonnull
	private ModulDTO toModul(@Nonnull ModulTagesschuleGroup modulGroup) {
		List<Wochentag> wochentage = modulGroup.getModule().stream()
			.map(modulTagesschule -> Wochentag.valueOf(modulTagesschule.getWochentag().name()))
			.collect(Collectors.toList());

		return ModulDTO.newBuilder()
			.setId(modulGroup.getId())
			.setBezeichnungDE(EbeguUtil.coalesce(modulGroup.getBezeichnung().getTextDeutsch(), StringUtils.EMPTY))
			.setBezeichnungFR(EbeguUtil.coalesce(modulGroup.getBezeichnung().getTextFranzoesisch(), StringUtils.EMPTY))
			.setZeitVon(modulGroup.getZeitVon())
			.setZeitBis(modulGroup.getZeitBis())
			.setWochentage(wochentage)
			.setErlaubteIntervalle(toErlaubteIntervalle(modulGroup.getIntervall()))
			.setWirdPaedagogischBetreut(modulGroup.isWirdPaedagogischBetreut())
			.setVerpflegungsKosten(EbeguUtil.coalesce(modulGroup.getVerpflegungskosten(), BigDecimal.ZERO))
			.build();
	}

	@Nonnull
	public static List<Intervall> toErlaubteIntervalle(@Nonnull ModulTagesschuleIntervall intervall) {
		switch (intervall) {
		case WOECHENTLICH:
			return Collections.singletonList(Intervall.WOECHENTLICH);
		case WOECHENTLICH_ODER_ALLE_ZWEI_WOCHEN:
			return Arrays.asList(Intervall.WOECHENTLICH, Intervall.ALLE_ZWEI_WOCHEN);
		default:
			throw new NotImplementedException("Missing converions of " + intervall + " to ModulIntervalle");
		}
	}
}
