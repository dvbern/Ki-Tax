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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.entities.gemeindeantrag;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Audited
@Entity
public class LastenausgleichTagesschuleAngabenGemeinde extends AbstractEntity {

	private static final long serialVersionUID = 7179246039479930826L;

	// A: Allgemeine Angaben

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean alleAngabenInKibonErfasst;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean bedarfBeiElternAbgeklaert;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean angebotFuerFerienbetreuungVorhanden;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean angebotVerfuegbarFuerAlleSchulstufen;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen;

	// B: Abrechnung

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal geleisteteBetreuungsstundenBesondereBeduerfnisse;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal davonStundenZuNormlohnMehrAls50ProzentAusgebildete;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal davonStundenZuNormlohnWenigerAls50ProzentAusgebildete;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal einnahmenElterngebuehren;

	// C: Kostenbeteiligung Gemeinde

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal gesamtKostenTagesschule;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal einnnahmenVerpflegung;

	@NotNull @Nonnull
	@Column(nullable = false)
	private BigDecimal einnahmenSubventionenDritter;

	// D: Angaben zu weiteren Kosten und Ertraegen

	@Nullable
	@Column(nullable = true)
	private String bemerkungenWeitereKostenUndErtraege;

	// E: Kontrollfragen

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean betreuungsstundenDokumentiertUndUeberprueft;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean elterngebuehrenGemaessVerordnungBerechnet;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean einkommenElternBelegt;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean maximalTarif;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;

	@NotNull @Nonnull
	@Column(nullable = false)
	private Boolean ausbildungenMitarbeitendeBelegt;

	// Bemerkungen

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String internerKommentar;

	@Nullable
	@Size(max = DB_DEFAULT_MAX_LENGTH)
	@Column(nullable = true)
	private String bemerkungen;

	public LastenausgleichTagesschuleAngabenGemeinde() {

	}

	public LastenausgleichTagesschuleAngabenGemeinde(@Nonnull LastenausgleichTagesschuleAngabenGemeinde source) {
		// A: Allgemeine Angaben
		this.setAlleAngabenInKibonErfasst(source.getAlleAngabenInKibonErfasst());
		this.setBedarfBeiElternAbgeklaert(source.getBedarfBeiElternAbgeklaert());
		this.setAngebotFuerFerienbetreuungVorhanden(source.getAngebotFuerFerienbetreuungVorhanden());
		this.setAngebotVerfuegbarFuerAlleSchulstufen(source.getAngebotVerfuegbarFuerAlleSchulstufen());
		this.setBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen(source.getBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen());
		// B: Abrechnung
		this.setGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse(source.getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse());
		this.setGeleisteteBetreuungsstundenBesondereBeduerfnisse(source.getGeleisteteBetreuungsstundenBesondereBeduerfnisse());
		this.setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(source.getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete());
		this.setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete(source.getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete());
		this.setEinnahmenElterngebuehren(source.getEinnahmenElterngebuehren());
		// C: Kostenbeteiligung Gemeinde
		this.setGesamtKostenTagesschule(source.getGesamtKostenTagesschule());
		this.setEinnnahmenVerpflegung(source.getEinnnahmenVerpflegung());
		this.setEinnahmenSubventionenDritter(source.getEinnahmenSubventionenDritter());
		// D: Angaben zu weiteren Kosten und Ertraegen
		this.setBemerkungenWeitereKostenUndErtraege(source.getBemerkungenWeitereKostenUndErtraege());
		// E: Kontrollfragen
		this.setBetreuungsstundenDokumentiertUndUeberprueft(source.getBetreuungsstundenDokumentiertUndUeberprueft());
		this.setElterngebuehrenGemaessVerordnungBerechnet(source.getElterngebuehrenGemaessVerordnungBerechnet());
		this.setEinkommenElternBelegt(source.getEinkommenElternBelegt());
		this.setMaximalTarif(source.getMaximalTarif());
		this.setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal(source.getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal());
		this.setAusbildungenMitarbeitendeBelegt(source.getAusbildungenMitarbeitendeBelegt());
		// Bemerkungen
		this.setInternerKommentar(source.getInternerKommentar());
		this.setBemerkungen(source.getBemerkungen());
	}



	@Nonnull
	public final Boolean getAlleAngabenInKibonErfasst() {
		return alleAngabenInKibonErfasst;
	}

	public final void setAlleAngabenInKibonErfasst(@Nonnull Boolean alleAngabenInKibonErfasst) {
		this.alleAngabenInKibonErfasst = alleAngabenInKibonErfasst;
	}

	@Nonnull
	public final Boolean getBedarfBeiElternAbgeklaert() {
		return bedarfBeiElternAbgeklaert;
	}

	public final void setBedarfBeiElternAbgeklaert(@Nonnull Boolean bedarfBeiElternAbgeklaert) {
		this.bedarfBeiElternAbgeklaert = bedarfBeiElternAbgeklaert;
	}

	@Nonnull
	public final Boolean getAngebotFuerFerienbetreuungVorhanden() {
		return angebotFuerFerienbetreuungVorhanden;
	}

	public final void setAngebotFuerFerienbetreuungVorhanden(@Nonnull Boolean angebotFuerFerienbetreuungVorhanden) {
		this.angebotFuerFerienbetreuungVorhanden = angebotFuerFerienbetreuungVorhanden;
	}

	@Nonnull
	public final Boolean getAngebotVerfuegbarFuerAlleSchulstufen() {
		return angebotVerfuegbarFuerAlleSchulstufen;
	}

	public final void setAngebotVerfuegbarFuerAlleSchulstufen(@Nonnull Boolean angebotVerfuegbarFuerAlleSchulstufen) {
		this.angebotVerfuegbarFuerAlleSchulstufen = angebotVerfuegbarFuerAlleSchulstufen;
	}

	@Nullable
	public final String getBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen() {
		return begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen;
	}

	public final void setBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen(@Nullable String begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen) {
		this.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen = begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen;
	}

	@Nonnull
	public final BigDecimal getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse() {
		return geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;
	}

	public final void setGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse(@Nonnull BigDecimal geleisteteBetreuungsstundenOhneBesondereBeduerfnisse) {
		this.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse = geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;
	}

	@Nonnull
	public final BigDecimal getGeleisteteBetreuungsstundenBesondereBeduerfnisse() {
		return geleisteteBetreuungsstundenBesondereBeduerfnisse;
	}

	public final void setGeleisteteBetreuungsstundenBesondereBeduerfnisse(@Nonnull BigDecimal geleisteteBetreuungsstundenBesondereBeduerfnisse) {
		this.geleisteteBetreuungsstundenBesondereBeduerfnisse = geleisteteBetreuungsstundenBesondereBeduerfnisse;
	}

	@Nonnull
	public final BigDecimal getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete() {
		return davonStundenZuNormlohnMehrAls50ProzentAusgebildete;
	}

	public final void setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(@Nonnull BigDecimal davonStundenZuNormlohnMehrAls50ProzentAusgebildetesPersonal) {
		this.davonStundenZuNormlohnMehrAls50ProzentAusgebildete = davonStundenZuNormlohnMehrAls50ProzentAusgebildetesPersonal;
	}

	@Nonnull
	public final BigDecimal getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete() {
		return davonStundenZuNormlohnWenigerAls50ProzentAusgebildete;
	}

	public final void setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete(@Nonnull BigDecimal davonStundenZuNormlohnWenigerAls50ProzentAusgebildetesPersonal) {
		this.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete = davonStundenZuNormlohnWenigerAls50ProzentAusgebildetesPersonal;
	}

	@Nonnull
	public final BigDecimal getEinnahmenElterngebuehren() {
		return einnahmenElterngebuehren;
	}

	public final void setEinnahmenElterngebuehren(@Nonnull BigDecimal einnahmenElterngebuehren) {
		this.einnahmenElterngebuehren = einnahmenElterngebuehren;
	}

	@Nonnull
	public final BigDecimal getGesamtKostenTagesschule() {
		return gesamtKostenTagesschule;
	}

	public final void setGesamtKostenTagesschule(@Nonnull BigDecimal gesamtKostenTagesschule) {
		this.gesamtKostenTagesschule = gesamtKostenTagesschule;
	}

	@Nonnull
	public final BigDecimal getEinnnahmenVerpflegung() {
		return einnnahmenVerpflegung;
	}

	public final void setEinnnahmenVerpflegung(@Nonnull BigDecimal einnnahmenVerpflegung) {
		this.einnnahmenVerpflegung = einnnahmenVerpflegung;
	}

	@Nonnull
	public final BigDecimal getEinnahmenSubventionenDritter() {
		return einnahmenSubventionenDritter;
	}

	public final void setEinnahmenSubventionenDritter(@Nonnull BigDecimal einnahmenSubventionenDritter) {
		this.einnahmenSubventionenDritter = einnahmenSubventionenDritter;
	}

	@Nullable
	public final String getBemerkungenWeitereKostenUndErtraege() {
		return bemerkungenWeitereKostenUndErtraege;
	}

	public final void setBemerkungenWeitereKostenUndErtraege(@Nullable String bemerkungenWeitereKostenUndErtraege) {
		this.bemerkungenWeitereKostenUndErtraege = bemerkungenWeitereKostenUndErtraege;
	}

	@Nonnull
	public final Boolean getBetreuungsstundenDokumentiertUndUeberprueft() {
		return betreuungsstundenDokumentiertUndUeberprueft;
	}

	public final void setBetreuungsstundenDokumentiertUndUeberprueft(@Nonnull Boolean betreuungsstundenDokumentiertUndUeberprueft) {
		this.betreuungsstundenDokumentiertUndUeberprueft = betreuungsstundenDokumentiertUndUeberprueft;
	}

	@Nonnull
	public final Boolean getElterngebuehrenGemaessVerordnungBerechnet() {
		return elterngebuehrenGemaessVerordnungBerechnet;
	}

	public final void setElterngebuehrenGemaessVerordnungBerechnet(@Nonnull Boolean elterngebuehrenGemaessVerordnungBerechnet) {
		this.elterngebuehrenGemaessVerordnungBerechnet = elterngebuehrenGemaessVerordnungBerechnet;
	}

	@Nonnull
	public final Boolean getEinkommenElternBelegt() {
		return einkommenElternBelegt;
	}

	public final void setEinkommenElternBelegt(@Nonnull Boolean einkommenElternBelegt) {
		this.einkommenElternBelegt = einkommenElternBelegt;
	}

	@Nonnull
	public final Boolean getMaximalTarif() {
		return maximalTarif;
	}

	public final void setMaximalTarif(@Nonnull Boolean maximalTarif) {
		this.maximalTarif = maximalTarif;
	}

	@Nonnull
	public final Boolean getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal() {
		return mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;
	}

	public final void setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal(@Nonnull Boolean mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal) {
		this.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal = mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;
	}

	@Nonnull
	public final Boolean getAusbildungenMitarbeitendeBelegt() {
		return ausbildungenMitarbeitendeBelegt;
	}

	public final void setAusbildungenMitarbeitendeBelegt(@Nonnull Boolean ausbildungenMitarbeitendeBelegt) {
		this.ausbildungenMitarbeitendeBelegt = ausbildungenMitarbeitendeBelegt;
	}

	@Nullable
	public final String getInternerKommentar() {
		return internerKommentar;
	}

	public final void setInternerKommentar(@Nullable String internerKommentar) {
		this.internerKommentar = internerKommentar;
	}

	@Nullable
	public final String getBemerkungen() {
		return bemerkungen;
	}

	public final void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}
}
