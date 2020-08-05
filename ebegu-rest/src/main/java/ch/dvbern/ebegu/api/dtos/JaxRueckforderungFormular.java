/*
 * Copyright (C) 2020 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ch.dvbern.ebegu.enums.RueckforderungInstitutionTyp;
import ch.dvbern.ebegu.enums.RueckforderungStatus;
import ch.dvbern.lib.date.converters.LocalDateTimeXMLConverter;
import ch.dvbern.lib.date.converters.LocalDateXMLConverter;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxRueckforderungFormular extends JaxAbstractDTO {

	private static final long serialVersionUID = -7620977294104032869L;

	@Nonnull
	private JaxInstitutionStammdatenSummary institutionStammdaten;

	@Nonnull
	private List<JaxRueckforderungMitteilung> rueckforderungMitteilungen = new ArrayList<>();

	@Nonnull
	private RueckforderungStatus status;

	private boolean hasBeenProvisorisch = false; // Wird zur Anzeige der korrekten Confirmationmessage benoetigt

	@Nullable
	private RueckforderungInstitutionTyp institutionTyp = null;

	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden;

	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden;

	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden;

	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden;

	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeAnzahlTage;

	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage;

	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeAnzahlTage;

	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage;

	@Nullable
	private BigDecimal stufe1KantonKostenuebernahmeBetreuung;

	@Nullable
	private BigDecimal stufe1InstitutionKostenuebernahmeBetreuung;

	@Nullable
	private BigDecimal stufe2KantonKostenuebernahmeBetreuung;

	@Nullable
	private BigDecimal stufe2InstitutionKostenuebernahmeBetreuung;

	@Nullable
	private BigDecimal stufe1FreigabeBetrag;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime stufe1FreigabeDatum;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime stufe1FreigabeAusbezahltAm;

	@Nullable
	private BigDecimal stufe2VerfuegungBetrag;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime stufe2VerfuegungDatum;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateTimeXMLConverter.class)
	private LocalDateTime stufe2VerfuegungAusbezahltAm;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate extendedEinreichefrist = null;

	@Nullable
	@XmlJavaTypeAdapter(LocalDateXMLConverter.class)
	private LocalDate relevantEinreichungsfrist;

	@Nullable
	private BigDecimal betragEntgangeneElternbeitraege;

	@Nullable
	private BigDecimal betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten; // Kita in TAGE, TFO in STUNDEN

	@Nullable
	private BigDecimal anzahlNichtAngeboteneEinheiten; // Neu: Rueckerstattung fuer nicht angebotene Einheiten

	@Nullable
	private Boolean kurzarbeitBeantragt;

	@Nullable
	private BigDecimal kurzarbeitBetrag;

	@Nullable
	private Boolean kurzarbeitDefinitivVerfuegt;

	@Nullable
	private String kurzarbeitKeinAntragBegruendung;

	@Nullable
	private String kurzarbeitSonstiges;

	@Nullable
	private Boolean coronaErwerbsersatzBeantragt;

	@Nullable
	private BigDecimal coronaErwerbsersatzBetrag;

	@Nullable
	private Boolean coronaErwerbsersatzDefinitivVerfuegt;

	@Nullable
	private String coronaErwerbsersatzKeinAntragBegruendung;

	@Nullable
	private String coronaErwerbsersatzSonstiges;

	@Nullable
	private String verantwortlicherName;

	@Nonnull
	public List<JaxRueckforderungMitteilung> getRueckforderungMitteilungen() {
		return rueckforderungMitteilungen;
	}

	public void setRueckforderungMitteilungen(@Nonnull List<JaxRueckforderungMitteilung> rueckforderungMitteilungen) {
		this.rueckforderungMitteilungen = rueckforderungMitteilungen;
	}

	@Nonnull
	public RueckforderungStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull RueckforderungStatus status) {
		this.status = status;
	}

	public boolean isHasBeenProvisorisch() {
		return hasBeenProvisorisch;
	}

	public void setHasBeenProvisorisch(boolean hasBeenProvisorisch) {
		this.hasBeenProvisorisch = hasBeenProvisorisch;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeAnzahlStunden() {
		return stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden) {
		this.stufe1KantonKostenuebernahmeAnzahlStunden = stufe1KantonKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden) {
		this.stufe1InstitutionKostenuebernahmeAnzahlStunden = stufe1InstitutionKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeAnzahlStunden() {
		return stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden) {
		this.stufe2KantonKostenuebernahmeAnzahlStunden = stufe2KantonKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlStunden() {
		return stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlStunden(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden) {
		this.stufe2InstitutionKostenuebernahmeAnzahlStunden = stufe2InstitutionKostenuebernahmeAnzahlStunden;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeAnzahlTage() {
		return stufe1KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe1KantonKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe1KantonKostenuebernahmeAnzahlTage) {
		this.stufe1KantonKostenuebernahmeAnzahlTage = stufe1KantonKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeAnzahlTage() {
		return stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe1InstitutionKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage) {
		this.stufe1InstitutionKostenuebernahmeAnzahlTage = stufe1InstitutionKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeAnzahlTage() {
		return stufe2KantonKostenuebernahmeAnzahlTage;
	}

	public void setStufe2KantonKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe2KantonKostenuebernahmeAnzahlTage) {
		this.stufe2KantonKostenuebernahmeAnzahlTage = stufe2KantonKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeAnzahlTage() {
		return stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	public void setStufe2InstitutionKostenuebernahmeAnzahlTage(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage) {
		this.stufe2InstitutionKostenuebernahmeAnzahlTage = stufe2InstitutionKostenuebernahmeAnzahlTage;
	}

	@Nullable
	public BigDecimal getStufe1KantonKostenuebernahmeBetreuung() {
		return stufe1KantonKostenuebernahmeBetreuung;
	}

	public void setStufe1KantonKostenuebernahmeBetreuung(@Nullable BigDecimal stufe1KantonKostenuebernahmeBetreuung) {
		this.stufe1KantonKostenuebernahmeBetreuung = stufe1KantonKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe1InstitutionKostenuebernahmeBetreuung() {
		return stufe1InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe1InstitutionKostenuebernahmeBetreuung(@Nullable BigDecimal stufe1InstitutionKostenuebernahmeBetreuung) {
		this.stufe1InstitutionKostenuebernahmeBetreuung = stufe1InstitutionKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe2KantonKostenuebernahmeBetreuung() {
		return stufe2KantonKostenuebernahmeBetreuung;
	}

	public void setStufe2KantonKostenuebernahmeBetreuung(@Nullable BigDecimal stufe2KantonKostenuebernahmeBetreuung) {
		this.stufe2KantonKostenuebernahmeBetreuung = stufe2KantonKostenuebernahmeBetreuung;
	}

	@Nullable
	public BigDecimal getStufe2InstitutionKostenuebernahmeBetreuung() {
		return stufe2InstitutionKostenuebernahmeBetreuung;
	}

	public void setStufe2InstitutionKostenuebernahmeBetreuung(@Nullable BigDecimal stufe2InstitutionKostenuebernahmeBetreuung) {
		this.stufe2InstitutionKostenuebernahmeBetreuung = stufe2InstitutionKostenuebernahmeBetreuung;
	}

	@Nonnull
	public JaxInstitutionStammdatenSummary getInstitutionStammdaten() {
		return institutionStammdaten;
	}

	public void setInstitutionStammdatenSummary(@Nonnull JaxInstitutionStammdatenSummary institutionStammdaten) {
		this.institutionStammdaten = institutionStammdaten;
	}

	@Nullable
	public BigDecimal getStufe1FreigabeBetrag() {
		return stufe1FreigabeBetrag;
	}

	public void setStufe1FreigabeBetrag(@Nullable BigDecimal stufe1FreigabeBetrag) {
		this.stufe1FreigabeBetrag = stufe1FreigabeBetrag;
	}

	@Nullable
	public LocalDateTime getStufe1FreigabeDatum() {
		return stufe1FreigabeDatum;
	}

	public void setStufe1FreigabeDatum(@Nullable LocalDateTime stufe1FreigabeDatum) {
		this.stufe1FreigabeDatum = stufe1FreigabeDatum;
	}

	@Nullable
	public LocalDateTime getStufe1FreigabeAusbezahltAm() {
		return stufe1FreigabeAusbezahltAm;
	}

	public void setStufe1FreigabeAusbezahltAm(@Nullable LocalDateTime stufe1FreigabeAusbezahltAm) {
		this.stufe1FreigabeAusbezahltAm = stufe1FreigabeAusbezahltAm;
	}

	@Nullable
	public BigDecimal getStufe2VerfuegungBetrag() {
		return stufe2VerfuegungBetrag;
	}

	public void setStufe2VerfuegungBetrag(@Nullable BigDecimal stufe2VerfuegungBetrag) {
		this.stufe2VerfuegungBetrag = stufe2VerfuegungBetrag;
	}

	@Nullable
	public LocalDateTime getStufe2VerfuegungDatum() {
		return stufe2VerfuegungDatum;
	}

	public void setStufe2VerfuegungDatum(@Nullable LocalDateTime stufe2VerfuegungDatum) {
		this.stufe2VerfuegungDatum = stufe2VerfuegungDatum;
	}

	@Nullable
	public LocalDateTime getStufe2VerfuegungAusbezahltAm() {
		return stufe2VerfuegungAusbezahltAm;
	}

	public void setStufe2VerfuegungAusbezahltAm(@Nullable LocalDateTime stufe2VerfuegungAusbezahltAm) {
		this.stufe2VerfuegungAusbezahltAm = stufe2VerfuegungAusbezahltAm;
	}

	@Nullable
	public RueckforderungInstitutionTyp getInstitutionTyp() {
		return institutionTyp;
	}

	public void setInstitutionTyp(@Nullable RueckforderungInstitutionTyp institutionTyp) {
		this.institutionTyp = institutionTyp;
	}

	@Nullable
	public LocalDate getExtendedEinreichefrist() {
		return extendedEinreichefrist;
	}

	public void setExtendedEinreichefrist(@Nullable LocalDate extendedEinreichefrist) {
		this.extendedEinreichefrist = extendedEinreichefrist;
	}

	@Nullable
	public LocalDate getRelevantEinreichungsfrist() {
		return relevantEinreichungsfrist;
	}

	public void setRelevantEinreichungsfrist(@Nullable LocalDate relevantEinreichungsfrist) {
		this.relevantEinreichungsfrist = relevantEinreichungsfrist;
	}

	@Nullable
	public BigDecimal getBetragEntgangeneElternbeitraege() {
		return betragEntgangeneElternbeitraege;
	}

	public void setBetragEntgangeneElternbeitraege(@Nullable BigDecimal betragEntgangeneElternbeitraege) {
		this.betragEntgangeneElternbeitraege = betragEntgangeneElternbeitraege;
	}

	@Nullable
	public BigDecimal getBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten() {
		return betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
	}

	public void setBetragEntgangeneElternbeitraegeNichtAngeboteneEinheiten(@Nullable BigDecimal betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten) {
		this.betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten = betragEntgangeneElternbeitraegeNichtAngeboteneEinheiten;
	}

	@Nullable
	public BigDecimal getAnzahlNichtAngeboteneEinheiten() {
		return anzahlNichtAngeboteneEinheiten;
	}

	public void setAnzahlNichtAngeboteneEinheiten(@Nullable BigDecimal anzahlNichtAngeboteneEinheiten) {
		this.anzahlNichtAngeboteneEinheiten = anzahlNichtAngeboteneEinheiten;
	}

	@Nullable
	public Boolean getKurzarbeitBeantragt() {
		return kurzarbeitBeantragt;
	}

	public void setKurzarbeitBeantragt(@Nullable Boolean kurzarbeitBeantragt) {
		this.kurzarbeitBeantragt = kurzarbeitBeantragt;
	}

	@Nullable
	public BigDecimal getKurzarbeitBetrag() {
		return kurzarbeitBetrag;
	}

	public void setKurzarbeitBetrag(@Nullable BigDecimal kurzarbeitBetrag) {
		this.kurzarbeitBetrag = kurzarbeitBetrag;
	}

	@Nullable
	public Boolean getKurzarbeitDefinitivVerfuegt() {
		return kurzarbeitDefinitivVerfuegt;
	}

	public void setKurzarbeitDefinitivVerfuegt(@Nullable Boolean kurzarbeitDefinitivVerfuegt) {
		this.kurzarbeitDefinitivVerfuegt = kurzarbeitDefinitivVerfuegt;
	}

	@Nullable
	public String getKurzarbeitKeinAntragBegruendung() {
		return kurzarbeitKeinAntragBegruendung;
	}

	public void setKurzarbeitKeinAntragBegruendung(@Nullable String kurzarbeitKeinAntragBegruendung) {
		this.kurzarbeitKeinAntragBegruendung = kurzarbeitKeinAntragBegruendung;
	}

	@Nullable
	public String getKurzarbeitSonstiges() {
		return kurzarbeitSonstiges;
	}

	public void setKurzarbeitSonstiges(@Nullable String kurzarbeitSonstiges) {
		this.kurzarbeitSonstiges = kurzarbeitSonstiges;
	}

	@Nullable
	public Boolean getCoronaErwerbsersatzBeantragt() {
		return coronaErwerbsersatzBeantragt;
	}

	public void setCoronaErwerbsersatzBeantragt(@Nullable Boolean coronaErwerbsersatzBeantragt) {
		this.coronaErwerbsersatzBeantragt = coronaErwerbsersatzBeantragt;
	}

	@Nullable
	public BigDecimal getCoronaErwerbsersatzBetrag() {
		return coronaErwerbsersatzBetrag;
	}

	public void setCoronaErwerbsersatzBetrag(@Nullable BigDecimal coronaErwerbsersatzBetrag) {
		this.coronaErwerbsersatzBetrag = coronaErwerbsersatzBetrag;
	}

	@Nullable
	public Boolean getCoronaErwerbsersatzDefinitivVerfuegt() {
		return coronaErwerbsersatzDefinitivVerfuegt;
	}

	public void setCoronaErwerbsersatzDefinitivVerfuegt(@Nullable Boolean coronaErwerbsersatzDefinitivVerfuegt) {
		this.coronaErwerbsersatzDefinitivVerfuegt = coronaErwerbsersatzDefinitivVerfuegt;
	}

	@Nullable
	public String getCoronaErwerbsersatzKeinAntragBegruendung() {
		return coronaErwerbsersatzKeinAntragBegruendung;
	}

	public void setCoronaErwerbsersatzKeinAntragBegruendung(@Nullable String coronaErwerbsersatzKeinAntragBegruendung) {
		this.coronaErwerbsersatzKeinAntragBegruendung = coronaErwerbsersatzKeinAntragBegruendung;
	}

	@Nullable
	public String getCoronaErwerbsersatzSonstiges() {
		return coronaErwerbsersatzSonstiges;
	}

	public void setCoronaErwerbsersatzSonstiges(@Nullable String coronaErwerbsersatzSonstiges) {
		this.coronaErwerbsersatzSonstiges = coronaErwerbsersatzSonstiges;
	}

	@Nullable
	public String getVerantwortlicherName() {
		return verantwortlicherName;
	}

	public void setVerantwortlicherName(@Nullable String verantwortlicherName) {
		this.verantwortlicherName = verantwortlicherName;
	}
}
