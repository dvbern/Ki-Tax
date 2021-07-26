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

package ch.dvbern.ebegu.outbox.anmeldung;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.kibon.exchange.commons.tagesschulen.AbholungTagesschule;
import ch.dvbern.kibon.exchange.commons.tagesschulen.ModulAuswahlDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungDetailsDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungEventDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungStatus;
import ch.dvbern.kibon.exchange.commons.types.AdresseDTO;
import ch.dvbern.kibon.exchange.commons.types.Geschlecht;
import ch.dvbern.kibon.exchange.commons.types.Gesuchsperiode;
import ch.dvbern.kibon.exchange.commons.types.Intervall;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;
import ch.dvbern.kibon.exchange.commons.types.GesuchstellerDTO;
import ch.dvbern.kibon.exchange.commons.types.KindDTO;

import static java.util.Objects.requireNonNull;

@ApplicationScoped
public class AnmeldungTagesschuleEventConverter {

	@Nonnull
	public AnmeldungTagesschuleEvent of(@Nonnull AnmeldungTagesschule anmeldung) {
		TagesschuleAnmeldungEventDTO dto = toTagesschuleAnmeldungEventDTO(anmeldung);
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new AnmeldungTagesschuleEvent(anmeldung.getBGNummer(), payload, dto.getSchema());
	}

	/**
	 * Convert einen Kibon Betreuung Entity in einer BetreuungAnfrageEventDTO
	 */
	@Nonnull
	private TagesschuleAnmeldungEventDTO toTagesschuleAnmeldungEventDTO(@Nonnull AnmeldungTagesschule anmeldung) {
		return TagesschuleAnmeldungEventDTO.newBuilder()
			.setInstitutionId(anmeldung.getInstitutionStammdaten().getInstitution().getId())
			.setAntragstellendePerson(toGesuchstellerDTO(requireNonNull(anmeldung.extractGesuch().getGesuchsteller1())))
			.setAnmeldungsDetails(toTagesschuleAnmeldungDetailsDTO(anmeldung))
			.setKind(toKindDTO(anmeldung.getKind().getKindJA()))
			.setStatus(TagesschuleAnmeldungStatus.valueOf(anmeldung.getBetreuungsstatus().name()))
			.setVersion((int) anmeldung.getVersion())
			.setFreigegebenAm(anmeldung.extractGesuch().getFreigabeDatum())
			.setGesuchsperiode(Gesuchsperiode.newBuilder()
				.setId(anmeldung.extractGesuch().getGesuchsperiode().getId())
				.setGueltigAb(anmeldung.extractGesuch().getGesuchsperiode().getGueltigkeit().getGueltigAb())
				.setGueltigBis(anmeldung.extractGesuch().getGesuchsperiode().getGueltigkeit().getGueltigBis())
				.build())
			.build();
	}

	@Nonnull
	private GesuchstellerDTO toGesuchstellerDTO(@Nonnull GesuchstellerContainer gesuchstellerContainer) {
		Adresse adresse = requireNonNull(gesuchstellerContainer.getAdressen().stream().filter(gesuchstellerAdresseContainer -> Objects
			.equals(gesuchstellerAdresseContainer.extractAdresseTyp(), AdresseTyp.WOHNADRESSE)).findFirst().get()).getGesuchstellerAdresseJA();
		Gesuchsteller gesuchsteller = gesuchstellerContainer.getGesuchstellerJA();
		requireNonNull(adresse);
		return GesuchstellerDTO.newBuilder()
			.setVorname(gesuchsteller.getVorname())
			.setNachname(gesuchsteller.getNachname())
			.setEmail(gesuchsteller.getMail())
			.setGeburtsdatum(gesuchsteller.getGeburtsdatum())
			.setAdresse(toAdresseDTO(adresse))
			.setGeschlecht(Geschlecht.valueOf(gesuchsteller.getGeschlecht().name()))
			.build();
	}

	@Nonnull
	private KindDTO toKindDTO(@Nonnull Kind kind) {
		return KindDTO.newBuilder()
			.setVorname(kind.getVorname())
			.setNachname(kind.getNachname())
			.setGeburtsdatum(kind.getGeburtsdatum())
			.setGeschlecht(Geschlecht.valueOf(kind.getGeschlecht().name()))
			.build();
	}

	@Nonnull
	private AdresseDTO toAdresseDTO(@Nonnull Adresse adresse) {
		return AdresseDTO.newBuilder()
			.setOrt(adresse.getOrt())
			.setLand(adresse.getLand().name())
			.setStrasse(adresse.getStrasse())
			.setHausnummer(adresse.getHausnummer())
			.setAdresszusatz(adresse.getZusatzzeile())
			.setPlz(adresse.getPlz())
			.build();
	}

	@Nonnull
	private TagesschuleAnmeldungDetailsDTO toTagesschuleAnmeldungDetailsDTO(
		@Nonnull AnmeldungTagesschule anmeldungTagesschule) {
		assert anmeldungTagesschule.getBelegungTagesschule() != null;
		assert anmeldungTagesschule.getBelegungTagesschule().getAbholungTagesschule() != null;
		return TagesschuleAnmeldungDetailsDTO.newBuilder()
			.setRefnr(anmeldungTagesschule.getBGNummer())
			.setBemerkung(anmeldungTagesschule.getBelegungTagesschule().getBemerkung())
			.setEintrittsdatum(anmeldungTagesschule.getBelegungTagesschule().getEintrittsdatum())
			.setPlanKlasse(anmeldungTagesschule.getBelegungTagesschule().getPlanKlasse())
			.setAbweichungZweitesSemester(anmeldungTagesschule.getBelegungTagesschule().isAbweichungZweitesSemester())
			.setModulSelection(toModulAuswahlDTOList(anmeldungTagesschule.getBelegungTagesschule()))
			.setAbholung(AbholungTagesschule.valueOf(anmeldungTagesschule.getBelegungTagesschule()
				.getAbholungTagesschule()
				.name()))
			.build();
	}

	@Nonnull
	private List<ModulAuswahlDTO> toModulAuswahlDTOList(@Nonnull BelegungTagesschule belegungTagesschule) {
		return belegungTagesschule.getBelegungTagesschuleModule().stream().map(this::toModulAuswahlDTO).collect(
			Collectors.toList());
	}

	private ModulAuswahlDTO toModulAuswahlDTO(@Nonnull BelegungTagesschuleModul belegungTagesschuleModul) {
		return ModulAuswahlDTO.newBuilder()
			.setModulId(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId())
			.setIntervall(Intervall.valueOf(belegungTagesschuleModul.getIntervall().name()))
			.setWeekday(belegungTagesschuleModul.getModulTagesschule().getWochentag().getValue())
			.build();
	}
}
