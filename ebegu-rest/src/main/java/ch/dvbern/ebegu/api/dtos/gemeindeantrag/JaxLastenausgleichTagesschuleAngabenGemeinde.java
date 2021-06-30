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

package ch.dvbern.ebegu.api.dtos.gemeindeantrag;

import java.math.BigDecimal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import ch.dvbern.ebegu.api.dtos.JaxAbstractDTO;
import ch.dvbern.ebegu.enums.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeFormularStatus;

public class JaxLastenausgleichTagesschuleAngabenGemeinde extends JaxAbstractDTO {

	private static final long serialVersionUID = -1526099337176479663L;

	// A: Allgemeine Angaben

	@NotNull @Nonnull
	private LastenausgleichTagesschuleAngabenGemeindeFormularStatus status;

	@NotNull @Nonnull
	private Boolean bedarfBeiElternAbgeklaert;

	@NotNull @Nonnull
	private Boolean angebotFuerFerienbetreuungVorhanden;

	@NotNull @Nonnull
	private Boolean angebotVerfuegbarFuerAlleSchulstufen;

	@Nullable
	private String begruendungWennAngebotNichtVerfuegbarFuerAlleSchulstufen;

	// B: Abrechnung

	@NotNull @Nonnull
	private BigDecimal geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;

	@NotNull @Nonnull
	private BigDecimal geleisteteBetreuungsstundenBesondereBeduerfnisse;

	@NotNull @Nonnull
	private BigDecimal davonStundenZuNormlohnMehrAls50ProzentAusgebildete;

	@NotNull @Nonnull
	private BigDecimal davonStundenZuNormlohnWenigerAls50ProzentAusgebildete;

	@NotNull @Nonnull
	private BigDecimal einnahmenElterngebuehren;

	@Nullable
	private Boolean tagesschuleTeilweiseGeschlossen;

	@Nullable
	private BigDecimal rueckerstattungenElterngebuehrenSchliessung;

	@Nullable
	private BigDecimal ersteRateAusbezahlt;

	// C: Kostenbeteiligung Gemeinde

	@NotNull @Nonnull
	private BigDecimal gesamtKostenTagesschule;

	@NotNull @Nonnull
	private BigDecimal einnnahmenVerpflegung;

	@NotNull @Nonnull
	private BigDecimal einnahmenSubventionenDritter;

	@Nullable
	private Boolean ueberschussErzielt;

	@Nullable
	private String ueberschussVerwendung;

	// D: Angaben zu weiteren Kosten und Ertraegen

	@Nullable
	private String bemerkungenWeitereKostenUndErtraege;

	// E: Kontrollfragen

	@NotNull @Nonnull
	private Boolean betreuungsstundenDokumentiertUndUeberprueft;

	@NotNull @Nonnull
	private Boolean elterngebuehrenGemaessVerordnungBerechnet;

	@NotNull @Nonnull
	private Boolean einkommenElternBelegt;

	@NotNull @Nonnull
	private Boolean maximalTarif;

	@NotNull @Nonnull
	private Boolean mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;

	@NotNull @Nonnull
	private Boolean ausbildungenMitarbeitendeBelegt;

	// Bemerkungen

	@Nullable
	private String internerKommentar;

	@Nullable
	private String bemerkungen;

	// Berechnungen
	@Nullable
	private BigDecimal lastenausgleichberechtigteBetreuungsstunden;
	@Nullable
	private BigDecimal davonStundenZuNormlohnMehrAls50ProzentAusgebildeteBerechnet;
	@Nullable
	private BigDecimal davonStundenZuNormlohnWenigerAls50ProzentAusgebildeteBerechnet;
	@Nullable
	private BigDecimal normlohnkostenBetreuungBerechnet;
	@Nullable
	private BigDecimal lastenausgleichsberechtigerBetrag;
	@Nullable
	private BigDecimal kostenbeitragGemeinde;
	@Nullable
	private BigDecimal kostenueberschussGemeinde;
	@Nullable
	private BigDecimal erwarteterKostenbeitragGemeinde;
	@Nullable
	private BigDecimal schlusszahlung;

	@Nonnull
	public Boolean getBedarfBeiElternAbgeklaert() {
		return bedarfBeiElternAbgeklaert;
	}

	public void setBedarfBeiElternAbgeklaert(@Nonnull Boolean bedarfBeiElternAbgeklaert) {
		this.bedarfBeiElternAbgeklaert = bedarfBeiElternAbgeklaert;
	}

	@Nonnull
	public Boolean getAngebotFuerFerienbetreuungVorhanden() {
		return angebotFuerFerienbetreuungVorhanden;
	}

	public void setAngebotFuerFerienbetreuungVorhanden(@Nonnull Boolean angebotFuerFerienbetreuungVorhanden) {
		this.angebotFuerFerienbetreuungVorhanden = angebotFuerFerienbetreuungVorhanden;
	}

	@Nonnull
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

	@Nonnull
	public BigDecimal getGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse() {
		return geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;
	}

	public void setGeleisteteBetreuungsstundenOhneBesondereBeduerfnisse(@Nonnull BigDecimal geleisteteBetreuungsstundenOhneBesondereBeduerfnisse) {
		this.geleisteteBetreuungsstundenOhneBesondereBeduerfnisse = geleisteteBetreuungsstundenOhneBesondereBeduerfnisse;
	}

	@Nonnull
	public BigDecimal getGeleisteteBetreuungsstundenBesondereBeduerfnisse() {
		return geleisteteBetreuungsstundenBesondereBeduerfnisse;
	}

	public void setGeleisteteBetreuungsstundenBesondereBeduerfnisse(@Nonnull BigDecimal geleisteteBetreuungsstundenBesondereBeduerfnisse) {
		this.geleisteteBetreuungsstundenBesondereBeduerfnisse = geleisteteBetreuungsstundenBesondereBeduerfnisse;
	}

	@Nonnull
	public BigDecimal getDavonStundenZuNormlohnMehrAls50ProzentAusgebildete() {
		return davonStundenZuNormlohnMehrAls50ProzentAusgebildete;
	}

	public void setDavonStundenZuNormlohnMehrAls50ProzentAusgebildete(@Nonnull BigDecimal davonStundenZuNormlohnMehrAls50ProzentAusgebildete) {
		this.davonStundenZuNormlohnMehrAls50ProzentAusgebildete = davonStundenZuNormlohnMehrAls50ProzentAusgebildete;
	}

	@Nonnull
	public BigDecimal getDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete() {
		return davonStundenZuNormlohnWenigerAls50ProzentAusgebildete;
	}

	public void setDavonStundenZuNormlohnWenigerAls50ProzentAusgebildete(@Nonnull BigDecimal davonStundenZuNormlohnWenigerAls50ProzentAusgebildete) {
		this.davonStundenZuNormlohnWenigerAls50ProzentAusgebildete = davonStundenZuNormlohnWenigerAls50ProzentAusgebildete;
	}

	@Nonnull
	public BigDecimal getEinnahmenElterngebuehren() {
		return einnahmenElterngebuehren;
	}

	public void setEinnahmenElterngebuehren(@Nonnull BigDecimal einnahmenElterngebuehren) {
		this.einnahmenElterngebuehren = einnahmenElterngebuehren;
	}

	@Nonnull
	public BigDecimal getGesamtKostenTagesschule() {
		return gesamtKostenTagesschule;
	}

	public void setGesamtKostenTagesschule(@Nonnull BigDecimal gesamtKostenTagesschule) {
		this.gesamtKostenTagesschule = gesamtKostenTagesschule;
	}

	@Nonnull
	public BigDecimal getEinnnahmenVerpflegung() {
		return einnnahmenVerpflegung;
	}

	public void setEinnnahmenVerpflegung(@Nonnull BigDecimal einnnahmenVerpflegung) {
		this.einnnahmenVerpflegung = einnnahmenVerpflegung;
	}

	@Nonnull
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

	@Nonnull
	public Boolean getBetreuungsstundenDokumentiertUndUeberprueft() {
		return betreuungsstundenDokumentiertUndUeberprueft;
	}

	public void setBetreuungsstundenDokumentiertUndUeberprueft(@Nonnull Boolean betreuungsstundenDokumentiertUndUeberprueft) {
		this.betreuungsstundenDokumentiertUndUeberprueft = betreuungsstundenDokumentiertUndUeberprueft;
	}

	@Nonnull
	public Boolean getElterngebuehrenGemaessVerordnungBerechnet() {
		return elterngebuehrenGemaessVerordnungBerechnet;
	}

	public void setElterngebuehrenGemaessVerordnungBerechnet(@Nonnull Boolean elterngebuehrenGemaessVerordnungBerechnet) {
		this.elterngebuehrenGemaessVerordnungBerechnet = elterngebuehrenGemaessVerordnungBerechnet;
	}

	@Nonnull
	public Boolean getEinkommenElternBelegt() {
		return einkommenElternBelegt;
	}

	public void setEinkommenElternBelegt(@Nonnull Boolean einkommenElternBelegt) {
		this.einkommenElternBelegt = einkommenElternBelegt;
	}

	@Nonnull
	public Boolean getMaximalTarif() {
		return maximalTarif;
	}

	public void setMaximalTarif(@Nonnull Boolean maximalTarif) {
		this.maximalTarif = maximalTarif;
	}

	@Nonnull
	public Boolean getMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal() {
		return mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;
	}

	public void setMindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal(@Nonnull Boolean mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal) {
		this.mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal = mindestens50ProzentBetreuungszeitDurchAusgebildetesPersonal;
	}

	@Nonnull
	public Boolean getAusbildungenMitarbeitendeBelegt() {
		return ausbildungenMitarbeitendeBelegt;
	}

	public void setAusbildungenMitarbeitendeBelegt(@Nonnull Boolean ausbildungenMitarbeitendeBelegt) {
		this.ausbildungenMitarbeitendeBelegt = ausbildungenMitarbeitendeBelegt;
	}

	@Nullable
	public String getInternerKommentar() {
		return internerKommentar;
	}

	public void setInternerKommentar(@Nullable String internerKommentar) {
		this.internerKommentar = internerKommentar;
	}

	@Nullable
	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(@Nullable String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	@Nonnull
	public LastenausgleichTagesschuleAngabenGemeindeFormularStatus getStatus() {
		return status;
	}

	public void setStatus(@Nonnull LastenausgleichTagesschuleAngabenGemeindeFormularStatus status) {
		this.status = status;
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
		@Nullable BigDecimal rueckerstattungenElterngebuehrenSchliessung) {
		this.rueckerstattungenElterngebuehrenSchliessung = rueckerstattungenElterngebuehrenSchliessung;
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
}
