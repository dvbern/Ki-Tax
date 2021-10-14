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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.AdresseTyp;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschule;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerAdresseContainer;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.TSCalculationResult;
import ch.dvbern.kibon.exchange.commons.tagesschulen.AbholungTagesschule;
import ch.dvbern.kibon.exchange.commons.tagesschulen.ModulAuswahlDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungDetailsDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungEventDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungStatus;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TagesschuleAnmeldungTarifeDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TarifDTO;
import ch.dvbern.kibon.exchange.commons.tagesschulen.TarifZeitabschnittDTO;
import ch.dvbern.kibon.exchange.commons.types.AdresseDTO;
import ch.dvbern.kibon.exchange.commons.types.Geschlecht;
import ch.dvbern.kibon.exchange.commons.types.GesuchstellerDTO;
import ch.dvbern.kibon.exchange.commons.types.Intervall;
import ch.dvbern.kibon.exchange.commons.types.KindDTO;
import ch.dvbern.kibon.exchange.commons.types.Wochentag;
import ch.dvbern.kibon.exchange.commons.util.AvroConverter;

import static java.util.Objects.requireNonNull;

@ApplicationScoped
public class AnmeldungTagesschuleEventConverter {

	@Nonnull
	public AnmeldungTagesschuleEvent of(@Nonnull AnmeldungTagesschule anmeldung) {
		TagesschuleAnmeldungEventDTO dto = toTagesschuleAnmeldungEventDTO(anmeldung);
		byte[] payload = AvroConverter.toAvroBinary(dto);

		return new AnmeldungTagesschuleEvent(anmeldung.getBGNummer(), payload, dto.getSchema());
	}

	@Nonnull
	private TagesschuleAnmeldungEventDTO toTagesschuleAnmeldungEventDTO(@Nonnull AnmeldungTagesschule anmeldung) {
		Gesuch gesuch = anmeldung.extractGesuch();

		//noinspection ConstantConditions
		return TagesschuleAnmeldungEventDTO.newBuilder()
			.setInstitutionId(anmeldung.getInstitutionStammdaten().getInstitution().getId())
			.setVersion(gesuch.getLaufnummer())
			// bei Papiergesuch gibt es kein Freigabedatum
			.setFreigegebenAm(gesuch.getFreigabeDatum() != null ?
				gesuch.getFreigabeDatum() :
				requireNonNull(gesuch.getEingangsdatum()))

			.setPeriodeVon(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigAb())
			.setPeriodeBis(gesuch.getGesuchsperiode().getGueltigkeit().getGueltigBis())
			.setKind(toKindDTO(anmeldung.getKind().getKindJA()))
			.setGesuchsteller(toGesuchstellerDTO(requireNonNull(gesuch.getGesuchsteller1())))
			.setGesuchsteller2(gesuch.getGesuchsteller2() != null ?
				toGesuchstellerDTO(gesuch.getGesuchsteller2()) :
				null)
			.setAnmeldungsDetails(toTagesschuleAnmeldungDetailsDTO(anmeldung))
			.setStatus(TagesschuleAnmeldungStatus.valueOf(anmeldung.getBetreuungsstatus().name()))
			// TODO muss noch richtig ausgefüllt weren. Abhängig von Betreuungsstatus
			.setAnmeldungZurueckgezogen(false)
			.setTarife(toTagesschuleAnmeldungTarifeDTO(anmeldung))
			.build();
	}

	@Nullable
	private TagesschuleAnmeldungTarifeDTO toTagesschuleAnmeldungTarifeDTO(
		@Nonnull AnmeldungTagesschule anmeldungTagesschule) {
		if (anmeldungTagesschule.getBetreuungsstatus().isSchulamtAnmeldungUebernommen()) {
			return TagesschuleAnmeldungTarifeDTO.newBuilder()
				.setTarifeDefinitivAkzeptiert(anmeldungTagesschule.getVerfuegung() != null)
				.setTarifZeitabschnitte(toTarifZeitabschnittDTO(anmeldungTagesschule))
				.build();
		}
		return null;
	}

	@Nonnull
	private List<TarifZeitabschnittDTO> toTarifZeitabschnittDTO(@Nonnull AnmeldungTagesschule anmeldungTagesschule) {
		if (anmeldungTagesschule.getVerfuegung() == null) {
			return Collections.emptyList();
		}

		//noinspection ConstantConditions
		return anmeldungTagesschule.getVerfuegung().getZeitabschnitte().stream()
			.map(z -> TarifZeitabschnittDTO.newBuilder()
				.setVon(z.getGueltigkeit().getGueltigAb())
				.setBis(z.getGueltigkeit().getGueltigBis())
				.setMassgebendesEinkommen(z.getMassgebendesEinkommen())
				.setTarifPaedagogisch(toTarifDTO(z.getTsCalculationResultMitPaedagogischerBetreuung()))
				.setTarifNichtPaedagogisch(toTarifDTO(z.getTsCalculationResultOhnePaedagogischerBetreuung()))
				.build())
			.collect(Collectors.toList());
	}

	@Nullable
	private TarifDTO toTarifDTO(@Nullable TSCalculationResult tsCalculationResult) {
		if (tsCalculationResult == null) {
			return null;
		}

		return TarifDTO.newBuilder()
			.setBetreuungsKostenProStunde(tsCalculationResult.getGebuehrProStunde())
			.setBetreuungsMinutenProWoche(tsCalculationResult.getBetreuungszeitProWoche())
			.setTotalKostenProWoche(tsCalculationResult.getTotalKostenProWoche())
			.setVerpflegungsKostenProWoche(tsCalculationResult.getVerpflegungskosten())
			.setVerpflegungsKostenVerguenstigung(tsCalculationResult.getVerpflegungskostenVerguenstigt())
			.build();
	}

	@Nonnull
	private GesuchstellerDTO toGesuchstellerDTO(@Nonnull GesuchstellerContainer gesuchstellerContainer) {
		AdresseDTO adresse = gesuchstellerContainer.getAdressen().stream()
			.filter(a -> a.extractAdresseTyp() == AdresseTyp.WOHNADRESSE)
			.findFirst()
			.map(GesuchstellerAdresseContainer::getGesuchstellerAdresseJA)
			.map(this::toAdresseDTO)
			.orElse(null);
		Gesuchsteller gesuchsteller = gesuchstellerContainer.getGesuchstellerJA();

		//noinspection ConstantConditions
		return GesuchstellerDTO.newBuilder()
			.setVorname(gesuchsteller.getVorname())
			.setNachname(gesuchsteller.getNachname())
			.setEmail(gesuchsteller.getMail())
			.setGeburtsdatum(gesuchsteller.getGeburtsdatum())
			.setAdresse(adresse)
			.setGeschlecht(Geschlecht.valueOf(gesuchsteller.getGeschlecht().name()))
			.setMobile(gesuchsteller.getMobile())
			.setTelefon(gesuchsteller.getTelefon())
			.setTelefonAusland(gesuchsteller.getTelefonAusland())
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
		//noinspection ConstantConditions
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

		BelegungTagesschule belegung = requireNonNull(anmeldungTagesschule.getBelegungTagesschule());

		AbholungTagesschule abholung = belegung.getAbholungTagesschule() != null ?
			AbholungTagesschule.valueOf(belegung.getAbholungTagesschule().name()) :
			null;

		//noinspection ConstantConditions
		return TagesschuleAnmeldungDetailsDTO.newBuilder()
			.setRefnr(anmeldungTagesschule.getBGNummer())
			.setEintrittsdatum(belegung.getEintrittsdatum())
			.setPlanKlasse(belegung.getPlanKlasse())
			.setAbholung(abholung)
			.setAbweichungZweitesSemester(belegung.isAbweichungZweitesSemester())
			.setBemerkung(belegung.getBemerkung())
			.setModule(toModulAuswahlDTOList(belegung))
			.build();
	}

	@Nonnull
	private List<ModulAuswahlDTO> toModulAuswahlDTOList(@Nonnull BelegungTagesschule belegungTagesschule) {
		return belegungTagesschule.getBelegungTagesschuleModule().stream()
			.map(this::toModulAuswahlDTO)
			.collect(Collectors.toList());
	}

	@Nonnull
	private ModulAuswahlDTO toModulAuswahlDTO(@Nonnull BelegungTagesschuleModul belegungTagesschuleModul) {
		return ModulAuswahlDTO.newBuilder()
			.setModulId(belegungTagesschuleModul.getModulTagesschule().getModulTagesschuleGroup().getId())
			.setIntervall(Intervall.valueOf(belegungTagesschuleModul.getIntervall().name()))
			.setWochentag(Wochentag.valueOf(belegungTagesschuleModul.getModulTagesschule().getWochentag().name()))
			.build();
	}
}
