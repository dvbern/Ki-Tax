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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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
import ch.dvbern.ebegu.enums.ZahlungslaufTyp;
import ch.dvbern.ebegu.services.VerfuegungService;
import ch.dvbern.ebegu.types.DateRange;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

/**
 * Converter to change to create the ExportDTO of a given Verfuegung
 */
@ApplicationScoped
public class ExportConverter {

	private static final Comparator<ZeitabschnittExportDTO> ZEITABSCHNITT_COMPARATOR = Comparator
		.comparing(ZeitabschnittExportDTO::getVon)
		.thenComparing(ZeitabschnittExportDTO::getBis);

	@Inject
	private VerfuegungService verfuegungService;

	@Nonnull
	public VerfuegungenExportDTO createVerfuegungenExportDTO(@Nonnull Collection<Verfuegung> verfuegungenToConvert) {
		List<VerfuegungExportDTO> verfuegungExportDTOS = verfuegungenToConvert.stream()
			.map(this::createVerfuegungExportDTOFromVerfuegung)
			.collect(Collectors.toList());

		VerfuegungenExportDTO exportDTO = new VerfuegungenExportDTO();
		exportDTO.setVerfuegungen(verfuegungExportDTOS);

		return exportDTO;
	}

	@Nonnull
	private VerfuegungExportDTO createVerfuegungExportDTOFromVerfuegung(@Nonnull Verfuegung verfuegung) {
		Betreuung betreuung = verfuegung.getBetreuung();

		VerfuegungExportDTO verfuegungDTO = new VerfuegungExportDTO();
		verfuegungDTO.setRefnr(betreuung.getBGNummer());
		DateRange periode = betreuung.extractGesuchsperiode().getGueltigkeit();
		verfuegungDTO.setVon(periode.getGueltigAb());
		verfuegungDTO.setBis(periode.getGueltigBis());
		verfuegungDTO.setVersion(betreuung.extractGesuch().getLaufnummer());
		verfuegungDTO.setVerfuegtAm(verfuegung.getTimestampErstellt());

		verfuegungDTO.setKind(createKindExportDTOFromKind(betreuung.getKind()));

		GesuchstellerContainer gs1 = betreuung.extractGesuch().getGesuchsteller1();
		requireNonNull(gs1);
		verfuegungDTO.setGesuchsteller(createGesuchstellerExportDTOFromGesuchsteller(gs1));

		verfuegungDTO.setBetreuung(createBetreuungExportDTOFromBetreuung(betreuung));

		addZeitabschnitte(verfuegung, verfuegungDTO);

		return verfuegungDTO;
	}

	private KindExportDTO createKindExportDTOFromKind(KindContainer kindCont) {
		Kind kindJA = kindCont.getKindJA();

		return new KindExportDTO(kindJA.getVorname(), kindJA.getNachname(), kindJA.getGeburtsdatum());
	}

	private GesuchstellerExportDTO createGesuchstellerExportDTOFromGesuchsteller(GesuchstellerContainer gesuchstellerContainer) {
		Gesuchsteller gesuchstellerJA = gesuchstellerContainer.getGesuchstellerJA();

		return new GesuchstellerExportDTO(
			gesuchstellerJA.getVorname(),
			gesuchstellerJA.getNachname(),
			gesuchstellerJA.getMail());
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
		String traegerschaft = institution.getTraegerschaft() != null ?
			institution.getTraegerschaft().getName() :
			null;

		AdresseExportDTO adresse = createAdresseExportDTOFromAdresse(institutionStammdaten.getAdresse());

		return new InstitutionExportDTO(instID, name, traegerschaft, adresse);
	}

	private AdresseExportDTO createAdresseExportDTOFromAdresse(Adresse adresse) {
		return new AdresseExportDTO(
			adresse.getStrasse(),
			adresse.getHausnummer(),
			adresse.getZusatzzeile(),
			adresse.getOrt(),
			adresse.getPlz(),
			adresse.getLand());
	}

	private void addZeitabschnitte(
		@Nonnull Verfuegung verfuegung,
		@Nonnull VerfuegungExportDTO verfuegungDTO) {

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
			.findVerrechnetenZeitabschnittOnVorgaengerVerfuegung(
				ZahlungslaufTyp.GEMEINDE_INSTITUTION,
				z,
				betreuung,
				nochGueltigeZeitabschnitte));

		return nochGueltigeZeitabschnitte;
	}

	@Nonnull
	private List<ZeitabschnittExportDTO> convertZeitabschnitte(@Nonnull List<VerfuegungZeitabschnitt> abschnitte) {
		return abschnitte.stream()
			.map(this::createZeitabschnittExportDTOFromZeitabschnitt)
			.sorted(ZEITABSCHNITT_COMPARATOR)
			.collect(Collectors.toList());
	}

	@Nonnull
	private ZeitabschnittExportDTO createZeitabschnittExportDTOFromZeitabschnitt(
		@Nonnull VerfuegungZeitabschnitt zeitabschnitt) {

		LocalDate von = zeitabschnitt.getGueltigkeit().getGueltigAb();
		LocalDate bis = zeitabschnitt.getGueltigkeit().getGueltigBis();
		int verfuegungNr = zeitabschnitt.getVerfuegung().getBetreuung().extractGesuch().getLaufnummer();
		BigDecimal effektiveBetr = zeitabschnitt.getBetreuungspensumProzent();
		int anspruchPct = zeitabschnitt.getAnspruchberechtigtesPensum();
		BigDecimal vergPct = zeitabschnitt.getBgPensum();
		BigDecimal vollkosten = zeitabschnitt.getVollkosten();
		BigDecimal betreuungsgutschein = zeitabschnitt.getVerguenstigungOhneBeruecksichtigungMinimalbeitrag();
		BigDecimal minimalerElternbeitrag = zeitabschnitt.getMinimalerElternbeitragGekuerzt();
		BigDecimal verguenstigung = zeitabschnitt.getVerguenstigung();

		return new ZeitabschnittExportDTO(
			von,
			bis,
			verfuegungNr,
			effektiveBetr,
			anspruchPct,
			vergPct,
			vollkosten,
			betreuungsgutschein,
			minimalerElternbeitrag,
			verguenstigung);
	}
}
