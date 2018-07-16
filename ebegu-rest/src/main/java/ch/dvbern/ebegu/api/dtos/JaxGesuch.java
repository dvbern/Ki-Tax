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

package ch.dvbern.ebegu.api.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.AntragStatusDTO;
import ch.dvbern.ebegu.enums.AntragTyp;
import ch.dvbern.ebegu.enums.Eingangsart;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.GesuchBetreuungenStatus;
import ch.dvbern.lib.date.converters.LocalDateTimeXMLConverter;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;

/**
 * DTO fuer Faelle
 */
@XmlRootElement(name = "gesuch")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxGesuch extends JaxAbstractDTO {

	private static final long serialVersionUID = -1217019901364130097L;

	@NotNull
	private JaxDossier dossier;

	@NotNull
	private JaxGesuchsperiode gesuchsperiode;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate eingangsdatum = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate regelnGultigAb = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate eingangsdatumSTV = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate freigabeDatum = null;

	@NotNull
	private AntragStatusDTO status;

	@NotNull
	private AntragTyp typ;

	@NotNull
	private Eingangsart eingangsart;

	@Nullable
	private JaxGesuchstellerContainer gesuchsteller1;

	@Nullable
	private JaxGesuchstellerContainer gesuchsteller2;

	@NotNull
	private Set<JaxKindContainer> kindContainers = new LinkedHashSet<>();

	@Nullable
	private JaxFamiliensituationContainer familiensituationContainer;

	@Nullable
	private JaxEinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer;

	@Nullable
	private String bemerkungen;

	@Nullable
	private String bemerkungenSTV;

	@Nullable
	private String bemerkungenPruefungSTV;

	private int laufnummer;

	private boolean geprueftSTV;

	private boolean hasFSDokument;

	@Nullable
	private FinSitStatus finSitStatus;

	private boolean gesperrtWegenBeschwerde;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate datumGewarntNichtFreigegeben;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate datumGewarntFehlendeQuittung;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime timestampVerfuegt;

	private boolean gueltig;

	private boolean dokumenteHochgeladen;


	@NotNull
	private GesuchBetreuungenStatus gesuchBetreuungenStatus = GesuchBetreuungenStatus.ALLE_BESTAETIGT;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	@Nullable
	public JaxGesuchstellerContainer getGesuchsteller1() {
		return gesuchsteller1;
	}

	public void setGesuchsteller1(@Nullable final JaxGesuchstellerContainer gesuchsteller1) {
		this.gesuchsteller1 = gesuchsteller1;
	}

	@Nullable
	public JaxGesuchstellerContainer getGesuchsteller2() {
		return gesuchsteller2;
	}

	public void setGesuchsteller2(@Nullable final JaxGesuchstellerContainer gesuchsteller2) {
		this.gesuchsteller2 = gesuchsteller2;
	}

	public Set<JaxKindContainer> getKindContainers() {
		return kindContainers;
	}

	public void setKindContainers(final Set<JaxKindContainer> kindContainers) {
		this.kindContainers = kindContainers;
	}

	@Nullable
	public JaxFamiliensituationContainer getFamiliensituationContainer() {
		return familiensituationContainer;
	}

	public void setFamiliensituationContainer(@Nullable JaxFamiliensituationContainer familiensituationContainer) {
		this.familiensituationContainer = familiensituationContainer;
	}

	@Nullable
	public JaxEinkommensverschlechterungInfoContainer getEinkommensverschlechterungInfoContainer() {
		return einkommensverschlechterungInfoContainer;
	}

	public void setEinkommensverschlechterungInfoContainer(@Nullable final JaxEinkommensverschlechterungInfoContainer einkommensverschlechterungInfoContainer) {
		this.einkommensverschlechterungInfoContainer = einkommensverschlechterungInfoContainer;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Nullable
	public String getBemerkungenSTV() {
		return bemerkungenSTV;
	}

	public void setBemerkungenSTV(@Nullable String bemerkungenSTV) {
		this.bemerkungenSTV = bemerkungenSTV;
	}

	@Nullable
	public String getBemerkungenPruefungSTV() {
		return bemerkungenPruefungSTV;
	}

	public void setBemerkungenPruefungSTV(@Nullable String bemerkungenPruefungSTV) {
		this.bemerkungenPruefungSTV = bemerkungenPruefungSTV;
	}

	public int getLaufnummer() {
		return laufnummer;
	}

	public void setLaufnummer(int laufnummer) {
		this.laufnummer = laufnummer;
	}

	public boolean isGeprueftSTV() {
		return geprueftSTV;
	}

	public void setGeprueftSTV(boolean geprueftSTV) {
		this.geprueftSTV = geprueftSTV;
	}

	public boolean isHasFSDokument() {
		return hasFSDokument;
	}

	public void setHasFSDokument(boolean hasFSDokument) {
		this.hasFSDokument = hasFSDokument;
	}

	public boolean isGesperrtWegenBeschwerde() {
		return gesperrtWegenBeschwerde;
	}

	public void setGesperrtWegenBeschwerde(boolean gesperrtWegenBeschwerde) {
		this.gesperrtWegenBeschwerde = gesperrtWegenBeschwerde;
	}

	@Nullable
	public LocalDate getDatumGewarntNichtFreigegeben() {
		return datumGewarntNichtFreigegeben;
	}

	public void setDatumGewarntNichtFreigegeben(@Nullable LocalDate datumGewarntNichtFreigegeben) {
		this.datumGewarntNichtFreigegeben = datumGewarntNichtFreigegeben;
	}

	@Nullable
	public LocalDate getDatumGewarntFehlendeQuittung() {
		return datumGewarntFehlendeQuittung;
	}

	public void setDatumGewarntFehlendeQuittung(@Nullable LocalDate datumGewarntFehlendeQuittung) {
		this.datumGewarntFehlendeQuittung = datumGewarntFehlendeQuittung;
	}

	@Nullable
	public LocalDateTime getTimestampVerfuegt() {
		return timestampVerfuegt;
	}

	public void setTimestampVerfuegt(@Nullable LocalDateTime timestampVerfuegt) {
		this.timestampVerfuegt = timestampVerfuegt;
	}

	public boolean isGueltig() {
		return gueltig;
	}

	public void setGueltig(boolean gueltig) {
		this.gueltig = gueltig;
	}

	public GesuchBetreuungenStatus getGesuchBetreuungenStatus() {
		return gesuchBetreuungenStatus;
	}

	public void setGesuchBetreuungenStatus(GesuchBetreuungenStatus gesuchBetreuungenStatus) {
		this.gesuchBetreuungenStatus = gesuchBetreuungenStatus;
	}

	public JaxDossier getDossier() {
		return dossier;
	}

	public void setDossier(JaxDossier dossier) {
		this.dossier = dossier;
	}

	public JaxGesuchsperiode getGesuchsperiode() {
		return gesuchsperiode;
	}

	public void setGesuchsperiode(JaxGesuchsperiode gesuchsperiode) {
		this.gesuchsperiode = gesuchsperiode;
	}

	@Nullable
	public LocalDate getEingangsdatum() {
		return eingangsdatum;
	}

	public void setEingangsdatum(@Nullable LocalDate eingangsdatum) {
		this.eingangsdatum = eingangsdatum;
	}

	@Nullable
	public LocalDate getRegelnGultigAb() {
		return regelnGultigAb;
	}

	public void setRegelnGultigAb(@Nullable LocalDate regelnGultigAb) {
		this.regelnGultigAb = regelnGultigAb;
	}

	@Nullable
	public LocalDate getEingangsdatumSTV() {
		return eingangsdatumSTV;
	}

	public void setEingangsdatumSTV(@Nullable LocalDate eingangsdatumSTV) {
		this.eingangsdatumSTV = eingangsdatumSTV;
	}

	@Nullable
	public LocalDate getFreigabeDatum() {
		return freigabeDatum;
	}

	public void setFreigabeDatum(@Nullable LocalDate freigabeDatum) {
		this.freigabeDatum = freigabeDatum;
	}

	public AntragStatusDTO getStatus() {
		return status;
	}

	public void setStatus(AntragStatusDTO status) {
		this.status = status;
	}

	public AntragTyp getTyp() {
		return typ;
	}

	public void setTyp(AntragTyp typ) {
		this.typ = typ;
	}

	public Eingangsart getEingangsart() {
		return eingangsart;
	}

	public void setEingangsart(Eingangsart eingangsart) {
		this.eingangsart = eingangsart;
	}

	public boolean isDokumenteHochgeladen() {
		return dokumenteHochgeladen;
	}

	public void setDokumenteHochgeladen(boolean dokumenteHochgeladen) {
		this.dokumenteHochgeladen = dokumenteHochgeladen;
	}

	@Nullable
	public FinSitStatus getFinSitStatus() {
		return finSitStatus;
	}

	public void setFinSitStatus(@Nullable FinSitStatus finSitStatus) {
		this.finSitStatus = finSitStatus;
	}
}

