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

import ch.dvbern.ebegu.enums.RueckforderungStatus;

@XmlAccessorType(XmlAccessType.FIELD)
public class JaxRueckforderungFormular extends JaxAbstractDTO {

	private static final long serialVersionUID = -7620977294104032869L;

	@Nonnull
	private JaxInstitution institution;

	@Nonnull
	private List<JaxRueckforderungMitteilung> rueckforderungMitteilungen = new ArrayList<>();

	@Nonnull
	private RueckforderungStatus status;

	private BigDecimal stufe1KantonKostenuebernahmeAnzahlStunden;

	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlStunden;

	private BigDecimal stufe2KantonKostenuebernahmeAnzahlStunden;

	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlStunden;

	private BigDecimal stufe1KantonKostenuebernahmeAnzahlTage;

	private BigDecimal stufe1InstitutionKostenuebernahmeAnzahlTage;

	private BigDecimal stufe2KantonKostenuebernahmeAnzahlTage;

	private BigDecimal stufe2InstitutionKostenuebernahmeAnzahlTage;

	private BigDecimal stufe1KantonKostenuebernahmeBetreuung;

	private BigDecimal stufe1InstitutionKostenuebernahmeBetreuung;

	private BigDecimal stufe2KantonKostenuebernahmeBetreuung;

	private BigDecimal stufe2InstitutionKostenuebernahmeBetreuung;

	private BigDecimal stufe1FreigabeBetrag;

	private LocalDateTime stufe1FreigabeDatum;

	private LocalDateTime stufe1FreigabeAusbezahltAm;

	private BigDecimal stufe2VerfuegungBetrag;

	private LocalDateTime stufe2VerfuegungDatum;

	private LocalDateTime stufe2VerfuegungAusbezahltAm;


	@Nonnull
	public JaxInstitution getInstitution() {
		return institution;
	}

	public void setInstitution(@Nonnull JaxInstitution jaxInstitution) {
		this.institution = jaxInstitution;
	}

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

	public BigDecimal getStufe1FreigabeBetrag() {
		return stufe1FreigabeBetrag;
	}

	public void setStufe1FreigabeBetrag(BigDecimal stufe1FreigabeBetrag) {
		this.stufe1FreigabeBetrag = stufe1FreigabeBetrag;
	}

	public LocalDateTime getStufe1FreigabeDatum() {
		return stufe1FreigabeDatum;
	}

	public void setStufe1FreigabeDatum(LocalDateTime stufe1FreigabeDatum) {
		this.stufe1FreigabeDatum = stufe1FreigabeDatum;
	}

	public LocalDateTime getStufe1FreigabeAusbezahltAm() {
		return stufe1FreigabeAusbezahltAm;
	}

	public void setStufe1FreigabeAusbezahltAm(LocalDateTime stufe1FreigabeAusbezahltAm) {
		this.stufe1FreigabeAusbezahltAm = stufe1FreigabeAusbezahltAm;
	}

	public BigDecimal getStufe2VerfuegungBetrag() {
		return stufe2VerfuegungBetrag;
	}

	public void setStufe2VerfuegungBetrag(BigDecimal stufe2VerfuegungBetrag) {
		this.stufe2VerfuegungBetrag = stufe2VerfuegungBetrag;
	}

	public LocalDateTime getStufe2VerfuegungDatum() {
		return stufe2VerfuegungDatum;
	}

	public void setStufe2VerfuegungDatum(LocalDateTime stufe2VerfuegungDatum) {
		this.stufe2VerfuegungDatum = stufe2VerfuegungDatum;
	}

	public LocalDateTime getStufe2VerfuegungAusbezahltAm() {
		return stufe2VerfuegungAusbezahltAm;
	}

	public void setStufe2VerfuegungAusbezahltAm(LocalDateTime stufe2VerfuegungAusbezahltAm) {
		this.stufe2VerfuegungAusbezahltAm = stufe2VerfuegungAusbezahltAm;
	}
}
