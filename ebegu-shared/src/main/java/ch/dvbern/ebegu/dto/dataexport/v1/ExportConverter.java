/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.dto.dataexport.v1;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.types.DateRange;

import static java.util.Objects.requireNonNull;

/**
 * Converter to change to create the ExportDTO of a given Verfuegung
 */
public class ExportConverter {

	public VerfuegungenExportDTO createVerfuegungenExportDTO(List<Verfuegung> verfuegungenToConvert) {
		List<VerfuegungExportDTO> verfuegungExportDTOS = verfuegungenToConvert
			.stream()
			.map(this::createVerfuegungExportDTOFromVerfuegung)
			.collect(Collectors.toList());
		VerfuegungenExportDTO exportDTO = new VerfuegungenExportDTO();
		exportDTO.setVerfuegungen(verfuegungExportDTOS);
		return exportDTO;
	}

	public List<ZeitabschnittExportDTO> createZeitabschnittExportDTOFromZeitabschnitte(List<VerfuegungZeitabschnitt> gueltigeZeitabschnitte) {
		List<ZeitabschnittExportDTO> zeitabschnitte = gueltigeZeitabschnitte.stream()
			.map(this::createZeitabschnittExportDTOFromZeitabschnitt)
			.collect(Collectors.toList());
		return zeitabschnitte;
	}

	private VerfuegungExportDTO createVerfuegungExportDTOFromVerfuegung(@Nonnull Verfuegung verfuegung) {
		requireNonNull(verfuegung, "verfuegung must be set");

		VerfuegungExportDTO verfuegungDTO = new VerfuegungExportDTO();
		verfuegungDTO.setRefnr(verfuegung.getBetreuung().getBGNummer());
		DateRange periode = verfuegung.getBetreuung().extractGesuchsperiode().getGueltigkeit();
		verfuegungDTO.setVon(periode.getGueltigAb());
		verfuegungDTO.setBis(periode.getGueltigBis());
		verfuegungDTO.setVersion(verfuegung.getBetreuung().extractGesuch().getLaufnummer());
		verfuegungDTO.setVerfuegtAm(verfuegung.getTimestampErstellt());
		verfuegungDTO.setKind(createKindExportDTOFromKind(verfuegung.getBetreuung().getKind()));
		GesuchstellerContainer gs1 = verfuegung.getBetreuung().extractGesuch().getGesuchsteller1();
		requireNonNull(gs1);
		verfuegungDTO.setGesuchsteller(createGesuchstellerExportDTOFromGesuchsteller(gs1));
		verfuegungDTO.setBetreuung(createBetreuungExportDTOFromBetreuung(verfuegung.getBetreuung()));
		// Verrechnete Zeitabschnitte
		List<ZeitabschnittExportDTO> zeitabschnitte = verfuegung.getZeitabschnitte().stream()
			.filter(abschnitt -> !abschnitt.getZahlungsstatus().isIgnoriertIgnorierend())
			.map(this::createZeitabschnittExportDTOFromZeitabschnitt)
			.collect(Collectors.toList());
		verfuegungDTO.setZeitabschnitte(zeitabschnitte);
		// Ignorierte Zeitabschnitte
		List<ZeitabschnittExportDTO> zeitabschnitteIgnoriert = verfuegung.getZeitabschnitte().stream()
			.filter(abschnitt -> abschnitt.getZahlungsstatus().isIgnoriertIgnorierend())
			.map(this::createZeitabschnittExportDTOFromZeitabschnitt)
			.collect(Collectors.toList());
		verfuegungDTO.setIgnorierteZeitabschnitte(zeitabschnitteIgnoriert);
		return verfuegungDTO;
	}

	private KindExportDTO createKindExportDTOFromKind(KindContainer kindCont) {
		Kind kindJA = kindCont.getKindJA();
		return new KindExportDTO(kindJA.getVorname(), kindJA.getNachname(), kindJA.getGeburtsdatum());
	}

	private GesuchstellerExportDTO createGesuchstellerExportDTOFromGesuchsteller(GesuchstellerContainer gesuchstellerContainer) {
		Gesuchsteller gesuchstellerJA = gesuchstellerContainer.getGesuchstellerJA();
		return new GesuchstellerExportDTO(gesuchstellerJA.getVorname(), gesuchstellerJA.getNachname(), gesuchstellerJA.getMail());
	}

	private BetreuungExportDTO createBetreuungExportDTOFromBetreuung(Betreuung betreuung) {
		BetreuungExportDTO betreuungExportDto = new BetreuungExportDTO();
		betreuungExportDto.setBetreuungsArt(betreuung.getBetreuungsangebotTyp());
		betreuungExportDto.setInstitution(createInstitutionExportDTOFromInstStammdaten(betreuung.getInstitutionStammdaten()));
		return betreuungExportDto;
	}

	private InstitutionExportDTO createInstitutionExportDTOFromInstStammdaten(InstitutionStammdaten institutionStammdaten) {
		Institution institution = institutionStammdaten.getInstitution();
		String instID = institution.getId();
		String name = institution.getName();
		String traegerschaft = institution.getTraegerschaft() != null ? institution.getTraegerschaft().getName() : null;
		AdresseExportDTO adresse = createAdresseExportDTOFromAdresse(institutionStammdaten.getAdresse());
		return new InstitutionExportDTO(instID, name, traegerschaft, adresse);
	}

	private AdresseExportDTO createAdresseExportDTOFromAdresse(Adresse adresse) {
		return new AdresseExportDTO(adresse.getStrasse(), adresse.getHausnummer(), adresse.getZusatzzeile(), adresse.getOrt(), adresse.getPlz(), adresse.getLand());
	}

	private ZeitabschnittExportDTO createZeitabschnittExportDTOFromZeitabschnitt(VerfuegungZeitabschnitt zeitabschnitt) {
		LocalDate von = zeitabschnitt.getGueltigkeit().getGueltigAb();
		LocalDate bis = zeitabschnitt.getGueltigkeit().getGueltigBis();
		int verfuegungNr = zeitabschnitt.getVerfuegung().getBetreuung().extractGesuch().getLaufnummer();
		BigDecimal effektiveBetr = zeitabschnitt.getBetreuungspensum();
		int anspruchPct = zeitabschnitt.getAnspruchberechtigtesPensum();
		BigDecimal vergPct = zeitabschnitt.getBgPensum();
		BigDecimal vollkosten = zeitabschnitt.getVollkosten();
		BigDecimal betreuungsgutschein = zeitabschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag();
		BigDecimal minimalerElternbeitrag = zeitabschnitt.getMinimalerElternbeitragGekuerzt();
		BigDecimal verguenstigung = zeitabschnitt.getVerguenstigung();
		return new ZeitabschnittExportDTO(von, bis, verfuegungNr, effektiveBetr, anspruchPct, vergPct, vollkosten, betreuungsgutschein,
			minimalerElternbeitrag, verguenstigung);
	}
}
