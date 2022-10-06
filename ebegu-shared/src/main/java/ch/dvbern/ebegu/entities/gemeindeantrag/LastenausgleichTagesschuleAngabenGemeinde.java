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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeFormularStatus;
import ch.dvbern.ebegu.util.Constants;
import com.google.common.base.Preconditions;
import org.hibernate.envers.Audited;

import static ch.dvbern.ebegu.util.Constants.DB_DEFAULT_MAX_LENGTH;

@Audited
@Entity
public class LastenausgleichTagesschuleAngabenGemeinde extends AbstractEntity {

	private static final long serialVersionUID = 7179246039479930826L;

	@NotNull
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private LastenausgleichTagesschuleAngabenGemeindeFormularStatus status;

	// A: Allgemeine Angaben

	@Nullable
	@Column(nullable = true)
	private Boolean bedarfBeiElternAbgeklaert;

	@Nullable
	@Column(nullable = true)
	private Boolean angebotFuerFerienbetreuungVorhanden;

	@Nullable
	@Column(nullable = true)
	private Boolean angebotVerfuegbarFuerAlleSchulstufen;

	@Nullable
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Column(nullable = true)
	private String begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen;

	// B: Abrechnung

	@Nullable
	@Column(nullable = true)
	private BigDecimal geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geleisteteBetreuungsstundenBesondereBeduerfnisse;

	@Nullable
	@Column(nullable = true)
	private BigDecimal geleisteteBetreuungsstundenBesondereVolksschulangebot;

	@Nullable
	@Column(nullable = true)
	private BigDecimal davonStundenZuNormlohnMehrAls50ProzentAusgebildete;

	@Nullable
	@Column(nullable = true)
	private BigDecimal davonStundenZuNormlohnWenigerAls50ProzentAusgebildete;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einnahmenElterngebuehren;

	@Nullable
	@Column(nullable = true)
	private Boolean tagesschuleTeilweiseGeschlossen;

	@Nullable
	@Column(nullable = true)
	private BigDecimal rueckerstattungenElterngebuehrenSchliessung;


	@Nullable
	@Column(nullable = true)
	private BigDecimal ersteRateAusbezahlt;

	// C: Kostenbeteiligung Gemeinde

	@Nullable
	@Column(nullable = true)
	private BigDecimal gesamtKostenTagesschule;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einnnahmenVerpflegung;

	@Nullable
	@Column(nullable = true)
	private BigDecimal einnahmenSubventionenDritter;

	@Nullable
	@Column(nullable = true)
	private Boolean ueberschussErzielt;

	@Nullable
	@Column(nullable = true)
	private String ueberschussVerwendung;



	// D: Angaben zu weiteren Kosten und Ertraegen

	@Nullable
	@Column(nullable = true)
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	private String bemerkungenWeitereKostenUndErtraege;

	// E: Kontrollfragen

	@Nullable
	@Column(nullable = true)
	private Boolean betreuungsstundenDokumentiertUndUeberprueft;

	@Nullable
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Column(nullable = true)
	private String betreuungsstundenDokumentiertUndUeberprueftBemerkung;

	@Nullable
	@Column(nullable = true)
	private Boolean elterngebuehrenGemaessVerordnungBerechnet;

	@Nullable
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Column(nullable = true)
	private String elterngebuehrenGemaessVerordnungBerechnetBemerkung;

	@Nullable
	@Column(nullable = true)
	private Boolean einkommenElternBelegt;

	@Nullable
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Column(nullable = true)
	private String einkommenElternBelegtBemerkung;

	@Nullable
	@Column(nullable = true)
	private Boolean maximalTarif;

	@Nullable
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Column(nullable = true)
	private String maximalTarifBemerkung;

	@Nullable
	@Column(nullable = true)
	private Boolean mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;

	@Nullable
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Column(name="mindestens50prozent_betreuungszeit_bemerkung", nullable = true)
	private String mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung;

	@Nullable
	@Column(nullable = true)
	private Boolean ausbildungenMitarbeitendeBelegt;

	@Nullable
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Column(nullable = true)
	private String ausbildungenMitarbeitendeBelegtBemerkung;

	// Bemerkungen

	@Nullable
	@Size(max = Constants.DB_TEXTAREA_LENGTH)
	@Column(nullable = true)
	private String bemerkungen;

	// Berechnungen
	@Nullable
	@Column(nullable = true)
	private BigDecimal lastenausgleichberechtigteBetreuungsstunden;

	@Nullable
	@Column(name="stunden_mehr_als_50_prozent_ausgebildete_berechnet", nullable = true)
	private BigDecimal davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet;

	@Nullable
	@Column(name="stunden_weniger_als_50_prozent_ausgebildete_berechnet", nullable = true)
	private BigDecimal davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet;

	@Nullable
	@Column(nullable = true)
	private BigDecimal normlohnkostenBetreuungBerechnet;

	@Nullable
	@Column(nullable = true)
	private BigDecimal lastenausgleichsberechtigerBetrag;

	@Nullable
	@Column(nullable = true)
	private BigDecimal kostenbeitragGemeinde;

	@Nullable
	@Column(nullable = true)
	private BigDecimal kostenueberschussGemeinde;

	@Nullable
	@Column(nullable = true)
	private BigDecimal erwarteterKostenbeitragGemeinde;

	@Nullable
	@Column(nullable = true)
	private BigDecimal schlusszahlung;


	public LastenausgleichTagesschuleAngabenGemeinde() {

	}

	public LastenausgleichTagesschuleAngabenGemeinde(@Nonnull LastenausgleichTagesschuleAngabenGemeinde source) {
		this.status = LastenausgleichTagesschuleAngabenGemeindeFormularStatus.IN_BEARBEITUNG;
		// A: Allgemeine Angaben
		this.bedarfBeiElternAbgeklaert = source.bedarfBeiElternAbgeklaert;
		this.angebotFuerFerienbetreuungVorhanden = source.angebotFuerFerienbetreuungVorhanden;
		this.angebotVerfuegbarFuerAlleSchulstufen = source.angebotVerfuegbarFuerAlleSchulstufen;
		this.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen = source.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen;
		this.tagesschuleTeilweiseGeschlossen = source.tagesschuleTeilweiseGeschlossen;
		this.rueckerstattungenElterngebuehrenSchliessung = source.rueckerstattungenElterngebuehrenSchliessung;
		// B: Abrechnung
		this.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse = source.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;
		this.geleisteteBetreuungsstundenBesondereBeduerfnisse = source.geleisteteBetreuungsstundenBesondereBeduerfnisse;
		this.davonStundenZuNormlohnMehrAls50ProzentAusgebildete = source.davonStundenZuNormlohnMehrAls50ProzentAusgebildete;
		this.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete = source.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete;
		this.ersteRateAusbezahlt = source.ersteRateAusbezahlt;
		this.einnahmenElterngebuehren = source.einnahmenElterngebuehren;
		this.geleisteteBetreuungsstundenBesondereVolksschulangebot = source.geleisteteBetreuungsstundenBesondereVolksschulangebot;
		// C: Kostenbeteiligung Gemeinde
		this.gesamtKostenTagesschule = source.gesamtKostenTagesschule;
		this.einnnahmenVerpflegung = source.einnnahmenVerpflegung;
		this.einnahmenSubventionenDritter = source.einnahmenSubventionenDritter;
		this.ueberschussErzielt = source.ueberschussErzielt;
		this.ueberschussVerwendung = source.ueberschussVerwendung;
		// D: Angaben zu weiteren Kosten und Ertraegen
		this.bemerkungenWeitereKostenUndErtraege = source.bemerkungenWeitereKostenUndErtraege;
		// E: Kontrollfragen
		this.betreuungsstundenDokumentiertUndUeberprueft = source.betreuungsstundenDokumentiertUndUeberprueft;
		this.betreuungsstundenDokumentiertUndUeberprueftBemerkung = source.betreuungsstundenDokumentiertUndUeberprueftBemerkung;
		this.elterngebuehrenGemaessVerordnungBerechnet = source.elterngebuehrenGemaessVerordnungBerechnet;
		this.elterngebuehrenGemaessVerordnungBerechnetBemerkung = source.elterngebuehrenGemaessVerordnungBerechnetBemerkung;
		this.einkommenElternBelegt = source.einkommenElternBelegt;
		this.einkommenElternBelegtBemerkung = source.einkommenElternBelegtBemerkung;
		this.maximalTarif = source.maximalTarif;
		this.maximalTarifBemerkung = source.maximalTarifBemerkung;
		this.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal = source.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;
		this.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung = source.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung;
		this.ausbildungenMitarbeitendeBelegt = source.ausbildungenMitarbeitendeBelegt;
		this.ausbildungenMitarbeitendeBelegtBemerkung = source.ausbildungenMitarbeitendeBelegtBemerkung;
		// Bemerkungen
		this.bemerkungen = source.bemerkungen;
	}

	@Nullable
	public Boolean getBedarfBeiElternAbgeklaert() {
		return bedarfBeiElternAbgeklaert;
	}

	public void setBedarfBeiElternAbgeklaert(@Nonnull Boolean bedarfBeiElternAbgeklaert) {
		this.bedarfBeiElternAbgeklaert = bedarfBeiElternAbgeklaert;
	}

	@Nullable
	public Boolean getAngebotFuerFerienbetreuungVorhanden() {
		return angebotFuerFerienbetreuungVorhanden;
	}

	public void setAngebotFuerFerienbetreuungVorhanden(@Nonnull Boolean angebotFuerFerienbetreuungVorhanden) {
		this.angebotFuerFerienbetreuungVorhanden = angebotFuerFerienbetreuungVorhanden;
	}

	@Nullable
	public Boolean getAngebotVerfuegbarFuerAlleSchulstufen() {
		return angebotVerfuegbarFuerAlleSchulstufen;
	}

	public void setAngebotVerfuegbarFuerAlleSchulstufen(@Nonnull Boolean angebotVerfuegbarFuerAlleSchulstufen) {
		this.angebotVerfuegbarFuerAlleSchulstufen = angebotVerfuegbarFuerAlleSchulstufen;
	}

	@Nullable
	public String getBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen() {
		return begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen;
	}

	public void setBegruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen(@Nullable String begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen) {
		this.begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen = begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen;
	}

	@Nullable
	public BigDecimal getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse() {
		return geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;
	}

	public void setGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse(@Nonnull BigDecimal geleisteteBetreuungsstundenOhneBesondereBeduerfnisse) {
		this.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse = geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;
	}

	@Nullable
	public BigDecimal getGeleisteteBetreuungsstundenBesondereBeduerfnisse() {
		return geleisteteBetreuungsstundenBesondereBeduerfnisse;
	}

	public void setGeleisteteBetreuungsstundenBesondereBeduerfnisse(@Nonnull BigDecimal geleisteteBetreuungsstundenBesondereBeduerfnisse) {
		this.geleisteteBetreuungsstundenBesondereBeduerfnisse = geleisteteBetreuungsstundenBesondereBeduerfnisse;
	}

	@Nullable
	public BigDecimal getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete() {
		return davonStundenZuNormlohnMehrAls50ProzentAusgebildete;
	}

	public void setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(@Nonnull BigDecimal davonStundenZuNormlohnMehrAls50ProzentAusgebildetesPersonal) {
		this.davonStundenZuNormlohnMehrAls50ProzentAusgebildete = davonStundenZuNormlohnMehrAls50ProzentAusgebildetesPersonal;
	}

	@Nullable
	public BigDecimal getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete() {
		return davonStundenZuNormlohnWenigerAls50ProzentAusgebildete;
	}

	public void setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete(@Nonnull BigDecimal davonStundenZuNormlohnWenigerAls50ProzentAusgebildetesPersonal) {
		this.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete = davonStundenZuNormlohnWenigerAls50ProzentAusgebildetesPersonal;
	}

	@Nullable
	public BigDecimal getEinnahmenElterngebuehren() {
		return einnahmenElterngebuehren;
	}

	public void setEinnahmenElterngebuehren(@Nonnull BigDecimal einnahmenElterngebuehren) {
		this.einnahmenElterngebuehren = einnahmenElterngebuehren;
	}

	@Nullable
	public BigDecimal getGesamtKostenTagesschule() {
		return gesamtKostenTagesschule;
	}

	public void setGesamtKostenTagesschule(@Nonnull BigDecimal gesamtKostenTagesschule) {
		this.gesamtKostenTagesschule = gesamtKostenTagesschule;
	}

	@Nullable
	public BigDecimal getEinnnahmenVerpflegung() {
		return einnnahmenVerpflegung;
	}

	public void setEinnnahmenVerpflegung(@Nonnull BigDecimal einnnahmenVerpflegung) {
		this.einnnahmenVerpflegung = einnnahmenVerpflegung;
	}

	@Nullable
	public BigDecimal getEinnahmenSubventionenDritter() {
		return einnahmenSubventionenDritter;
	}

	public void setEinnahmenSubventionenDritter(@Nonnull BigDecimal einnahmenSubventionenDritter) {
		this.einnahmenSubventionenDritter = einnahmenSubventionenDritter;
	}

	@Nullable
	public String getBemerkungenWeitereKostenUndErtraege() {
		return bemerkungenWeitereKostenUndErtraege;
	}

	public void setBemerkungenWeitereKostenUndErtraege(@Nullable String bemerkungenWeitereKostenUndErtraege) {
		this.bemerkungenWeitereKostenUndErtraege = bemerkungenWeitereKostenUndErtraege;
	}

	@Nullable
	public Boolean getBetreuungsstundenDokumentiertUndUeberprueft() {
		return betreuungsstundenDokumentiertUndUeberprueft;
	}

	public void setBetreuungsstundenDokumentiertUndUeberprueft(@Nonnull Boolean betreuungsstundenDokumentiertUndUeberprueft) {
		this.betreuungsstundenDokumentiertUndUeberprueft = betreuungsstundenDokumentiertUndUeberprueft;
	}

	@Nullable
	public Boolean getElterngebuehrenGemaessVerordnungBerechnet() {
		return elterngebuehrenGemaessVerordnungBerechnet;
	}

	public void setElterngebuehrenGemaessVerordnungBerechnet(@Nonnull Boolean elterngebuehrenGemaessVerordnungBerechnet) {
		this.elterngebuehrenGemaessVerordnungBerechnet = elterngebuehrenGemaessVerordnungBerechnet;
	}

	@Nullable
	public Boolean getEinkommenElternBelegt() {
		return einkommenElternBelegt;
	}

	public void setEinkommenElternBelegt(@Nonnull Boolean einkommenElternBelegt) {
		this.einkommenElternBelegt = einkommenElternBelegt;
	}

	@Nullable
	public Boolean getMaximalTarif() {
		return maximalTarif;
	}

	public void setMaximalTarif(@Nonnull Boolean maximalTarif) {
		this.maximalTarif = maximalTarif;
	}

	@Nullable
	public Boolean getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal() {
		return mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;
	}

	public void setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal(@Nonnull Boolean mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal) {
		this.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal = mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;
	}

	@Nullable
	public Boolean getAusbildungenMitarbeitendeBelegt() {
		return ausbildungenMitarbeitendeBelegt;
	}

	public void setAusbildungenMitarbeitendeBelegt(@Nonnull Boolean ausbildungenMitarbeitendeBelegt) {
		this.ausbildungenMitarbeitendeBelegt = ausbildungenMitarbeitendeBelegt;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Override
	public boolean isSame(AbstractEntity other) {
		return getId().equals(other.getId());
	}

	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeFormularStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull LastenausgleichTagesschuleAngabenGemeindeFormularStatus status) {
		this.status = status;
	}

	public boolean plausibilisierungLATSBerechtigteStundenHolds() {
		Preconditions.checkState(
			getGeleisteteBetreuungsstundenBesondereBeduerfnisse() != null,
			"geleisteteBetreuungsstundenBesondereBeduerfnisse darf nicht null sein"
		);
		Preconditions.checkState(
			getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse() != null,
			"geleisteteBetreuungsstundenOhneBesondereBeduerfnisse darf nicht null sein"
		);
		Preconditions.checkState(
			getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete() != null,
			"davonStundenZuNormlohnMehrAls50ProzentAusgebildete darf nicht null sein"
		);
		Preconditions.checkState(
			getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete() != null,
			"davonStundenZuNormlohnWenigerAls50ProzentAusgebildete darf nicht null sein"
		);
		assert getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse() != null;
		assert getGeleisteteBetreuungsstundenBesondereBeduerfnisse() != null;

		final BigDecimal angabenStunden = getGeleisteteBetreuungsstundenBesondereBeduerfnisse().add(
				getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse()).add(
				getGeleisteteBetreuungsstundenBesondereVolksschulangebot());
		final BigDecimal angabenStundenSplittedByNormlohn = getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete().add(
				getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete());

		return angabenStunden
				.compareTo(angabenStundenSplittedByNormlohn) == 0;
	}

	@Nullable
	public Boolean getTagesschuleTeilweiseGeschlossen() {
		return tagesschuleTeilweiseGeschlossen;
	}

	public void setTagesschuleTeilweiseGeschlossen(@Nullable Boolean tagesschuleTeilweiseGeschlossen) {
		this.tagesschuleTeilweiseGeschlossen = tagesschuleTeilweiseGeschlossen;
	}

	@Nullable
	public BigDecimal getRueckerstattungenElterngebuehrenSchliessung() {
		return rueckerstattungenElterngebuehrenSchliessung;
	}

	public void setRueckerstattungenElterngebuehrenSchliessung(
		@Nullable BigDecimal rueckerstattungenElterngebuehrenSchliesseung) {
		this.rueckerstattungenElterngebuehrenSchliessung = rueckerstattungenElterngebuehrenSchliesseung;
	}

	@Nullable
	public BigDecimal getErsteRateAusbezahlt() {
		return ersteRateAusbezahlt;
	}

	public void setErsteRateAusbezahlt(@Nullable BigDecimal ersteRateAusbezahlt) {
		this.ersteRateAusbezahlt = ersteRateAusbezahlt;
	}

	@Nullable
	public Boolean getUeberschussErzielt() {
		return ueberschussErzielt;
	}

	public void setUeberschussErzielt(@Nullable Boolean ueberschussErzielt) {
		this.ueberschussErzielt = ueberschussErzielt;
	}

	@Nullable
	public String getUeberschussVerwendung() {
		return ueberschussVerwendung;
	}

	public void setUeberschussVerwendung(@Nullable String ueberschussVerwendung) {
		this.ueberschussVerwendung = ueberschussVerwendung;
	}

	public boolean isAbgeschlossen() {
		return status == LastenausgleichTagesschuleAngabenGemeindeFormularStatus.ABGESCHLOSSEN;
	}

	@Nullable
	public BigDecimal getLastenausgleichberechtigteBetreuungsstunden() {
		return lastenausgleichberechtigteBetreuungsstunden;
	}

	public void setLastenausgleichberechtigteBetreuungsstunden(@Nullable BigDecimal lastenausgleichberechtigteBetreuungsstunden) {
		this.lastenausgleichberechtigteBetreuungsstunden = lastenausgleichberechtigteBetreuungsstunden;
	}

	@Nullable
	public BigDecimal getDavonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet() {
		return davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet;
	}

	public void setDavonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet(@Nullable BigDecimal davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet) {
		this.davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet = davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet;
	}

	@Nullable
	public BigDecimal getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet() {
		return davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet;
	}

	public void setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet(@Nullable BigDecimal davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet) {
		this.davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet = davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet;
	}

	@Nullable
	public BigDecimal getNormlohnkostenBetreuungBerechnet() {
		return normlohnkostenBetreuungBerechnet;
	}

	public void setNormlohnkostenBetreuungBerechnet(@Nullable BigDecimal normlohnkostenBetreuungBerechnet) {
		this.normlohnkostenBetreuungBerechnet = normlohnkostenBetreuungBerechnet;
	}

	@Nullable
	public BigDecimal getLastenausgleichsberechtigerBetrag() {
		return lastenausgleichsberechtigerBetrag;
	}

	public void setLastenausgleichsberechtigerBetrag(@Nullable BigDecimal lastenausgleichsberechtigerBetrag) {
		this.lastenausgleichsberechtigerBetrag = lastenausgleichsberechtigerBetrag;
	}

	@Nullable
	public BigDecimal getKostenbeitragGemeinde() {
		return kostenbeitragGemeinde;
	}

	public void setKostenbeitragGemeinde(@Nullable BigDecimal kostenbeitragGemeinde) {
		this.kostenbeitragGemeinde = kostenbeitragGemeinde;
	}

	@Nullable
	public BigDecimal getKostenueberschussGemeinde() {
		return kostenueberschussGemeinde;
	}

	public void setKostenueberschussGemeinde(@Nullable BigDecimal kostenueberschussGemeinde) {
		this.kostenueberschussGemeinde = kostenueberschussGemeinde;
	}

	@Nullable
	public BigDecimal getErwarteterKostenbeitragGemeinde() {
		return erwarteterKostenbeitragGemeinde;
	}

	public void setErwarteterKostenbeitragGemeinde(@Nullable BigDecimal erwarteterKostenbeitragGemeinde) {
		this.erwarteterKostenbeitragGemeinde = erwarteterKostenbeitragGemeinde;
	}

	@Nullable
	public BigDecimal getSchlusszahlung() {
		return schlusszahlung;
	}

	public void setSchlusszahlung(@Nullable BigDecimal schlusszahlung) {
		this.schlusszahlung = schlusszahlung;
	}

	@Nullable
	public String getBetreuungsstundenDokumentiertUndUeberprueftBemerkung() {
		return betreuungsstundenDokumentiertUndUeberprueftBemerkung;
	}

	public void setBetreuungsstundenDokumentiertUndUeberprueftBemerkung(
		@Nullable String betreuungsstundenDokumentiertUndUeberprueftBemerkung) {
		this.betreuungsstundenDokumentiertUndUeberprueftBemerkung =
			betreuungsstundenDokumentiertUndUeberprueftBemerkung;
	}

	@Nullable
	public BigDecimal getGeleisteteBetreuungsstundenBesondereVolksschulangebot() {
		return geleisteteBetreuungsstundenBesondereVolksschulangebot;
	}

	public void setGeleisteteBetreuungsstundenBesondereVolksschulangebot(
		@Nullable BigDecimal geleisteteBetreuungsstundenBesondereVolksschulangebot) {
		this.geleisteteBetreuungsstundenBesondereVolksschulangebot =
			geleisteteBetreuungsstundenBesondereVolksschulangebot;
	}

	@Nullable
	public String getElterngebuehrenGemaessVerordnungBerechnetBemerkung() {
		return elterngebuehrenGemaessVerordnungBerechnetBemerkung;
	}

	public void setElterngebuehrenGemaessVerordnungBerechnetBemerkung(
		@Nullable String elterngebuehrenGemaessVerordnungBerechnetBemerkung) {
		this.elterngebuehrenGemaessVerordnungBerechnetBemerkung = elterngebuehrenGemaessVerordnungBerechnetBemerkung;
	}

	@Nullable
	public String getEinkommenElternBelegtBemerkung() {
		return einkommenElternBelegtBemerkung;
	}

	public void setEinkommenElternBelegtBemerkung(@Nullable String einkommenElternBelegtBemerkung) {
		this.einkommenElternBelegtBemerkung = einkommenElternBelegtBemerkung;
	}

	@Nullable
	public String getMaximalTarifBemerkung() {
		return maximalTarifBemerkung;
	}

	public void setMaximalTarifBemerkung(@Nullable String maximalTarifBemerkung) {
		this.maximalTarifBemerkung = maximalTarifBemerkung;
	}

	@Nullable
	public String getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung() {
		return mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung;
	}

	public void setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung(
		@Nullable String mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung) {
		this.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung =
			mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonalBemerkung;
	}

	@Nullable
	public String getAusbildungenMitarbeitendeBelegtBemerkung() {
		return ausbildungenMitarbeitendeBelegtBemerkung;
	}

	public void setAusbildungenMitarbeitendeBelegtBemerkung(@Nullable String ausbildungenMitarbeitendeBelegtBemerkung) {
		this.ausbildungenMitarbeitendeBelegtBemerkung = ausbildungenMitarbeitendeBelegtBemerkung;
	}
}
