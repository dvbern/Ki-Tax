/*
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.enums.betreuung;

import java.util.EnumSet;
import java.util.Set;

import ch.dvbern.ebegu.enums.UserRole;

/**
 * Enum fuers Feld status in einer Betreuung.
 */
public enum Betreuungsstatus {

	// Ablauf beim Jugendamt
	WARTEN,
	ABGEWIESEN,
	NICHT_EINGETRETEN,
	STORNIERT,
	BESTAETIGT,
	VERFUEGT,
	UNBEKANNTE_INSTITUTION,
	GESCHLOSSEN_OHNE_VERFUEGUNG,

	// Ablauf beim Schulamt
	SCHULAMT_ANMELDUNG_ERFASST,
	SCHULAMT_ANMELDUNG_AUSGELOEST,
	SCHULAMT_MODULE_AKZEPTIERT,
	SCHULAMT_ANMELDUNG_UEBERNOMMEN,
	SCHULAMT_ANMELDUNG_ABGELEHNT,
	SCHULAMT_FALSCHE_INSTITUTION,
	SCHULAMT_ANMELDUNG_STORNIERT,
	SCHULAMT_MUTATION_IGNORIERT;

	public boolean isGeschlossenJA() {
		return VERFUEGT == this || GESCHLOSSEN_OHNE_VERFUEGUNG == this || NICHT_EINGETRETEN == this;
	}

	public boolean isGeschlossenSchulamt() {
		return SCHULAMT_ANMELDUNG_UEBERNOMMEN == this
			|| SCHULAMT_ANMELDUNG_ABGELEHNT == this
			|| SCHULAMT_ANMELDUNG_STORNIERT == this
			|| SCHULAMT_MUTATION_IGNORIERT == this;
	}

	/**
	 * Alle SCH-Status, die ausgeloest sind, gelten als geschlossen, da sie im Verfuegungsprozess nicht beruecksichtigt werden.
	 */
	public boolean isGeschlossen() {
		return VERFUEGT == this || GESCHLOSSEN_OHNE_VERFUEGUNG == this || NICHT_EINGETRETEN == this
			|| SCHULAMT_ANMELDUNG_UEBERNOMMEN == this || SCHULAMT_ANMELDUNG_ABGELEHNT == this || SCHULAMT_ANMELDUNG_AUSGELOEST == this
			|| SCHULAMT_FALSCHE_INSTITUTION == this || SCHULAMT_ANMELDUNG_STORNIERT == this
			|| SCHULAMT_MUTATION_IGNORIERT == this;
	}

	public boolean isAnyStatusOfVerfuegt() {
		return VERFUEGT == this
			|| SCHULAMT_ANMELDUNG_UEBERNOMMEN == this || SCHULAMT_ANMELDUNG_ABGELEHNT == this || SCHULAMT_ANMELDUNG_STORNIERT == this;
	}

	public boolean isSendToInstitution() {
		return ABGEWIESEN == this || BESTAETIGT == this || WARTEN == this ;
	}

	public boolean isSchulamt() {
		return SCHULAMT_ANMELDUNG_ERFASST  == this || SCHULAMT_ANMELDUNG_AUSGELOEST == this
			|| SCHULAMT_ANMELDUNG_UEBERNOMMEN == this|| SCHULAMT_ANMELDUNG_ABGELEHNT == this  || SCHULAMT_FALSCHE_INSTITUTION == this
			|| SCHULAMT_MODULE_AKZEPTIERT == this || SCHULAMT_ANMELDUNG_STORNIERT == this || SCHULAMT_MUTATION_IGNORIERT == this;
	}

	public boolean isSchulamtStatusWithPotentialVerfuegung() {
		// Tagesschule-Anmeldungen gelten als "verfügt", wenn sie übernommen sind (Normalfall)
		// Wenn aber im EG eine Anmeldung im Status AUSGELOEST ist, und eine Mutation erstellt wird,
		// so wird die Anmeldung des EG ebenfalls gespeichert, damit wir beim Berechnen mit dem
		// richtigen FinSit rechnen! (siehe MutationMerger)
		return SCHULAMT_ANMELDUNG_AUSGELOEST == this || SCHULAMT_ANMELDUNG_UEBERNOMMEN == this || SCHULAMT_ANMELDUNG_STORNIERT == this;
	}

	public static Set<Betreuungsstatus> getBetreuungsstatusWithVerfuegung() {
		return EnumSet.of(VERFUEGT, NICHT_EINGETRETEN, UNBEKANNTE_INSTITUTION);
	}

	public static Set<Betreuungsstatus> getBetreuungsstatusForPendenzInstitution() {
		return EnumSet.of(WARTEN, SCHULAMT_ANMELDUNG_AUSGELOEST);
	}

	public static Set<Betreuungsstatus> getBetreuungsstatusForPendenzSchulamt() {
		return EnumSet.of(SCHULAMT_ANMELDUNG_AUSGELOEST, SCHULAMT_FALSCHE_INSTITUTION);
	}

	public static Set<Betreuungsstatus> getBetreuungsstatusForAnmeldungsstatusAusgeloestNotStorniert() {
		return EnumSet.of(SCHULAMT_ANMELDUNG_AUSGELOEST,
			SCHULAMT_ANMELDUNG_UEBERNOMMEN, SCHULAMT_ANMELDUNG_ABGELEHNT, SCHULAMT_FALSCHE_INSTITUTION, SCHULAMT_MODULE_AKZEPTIERT);
	}

	public static Set<Betreuungsstatus> getBetreuungsstatusForFireAnmeldungTagesschuleEvent() {
		return EnumSet.of(
			Betreuungsstatus.SCHULAMT_ANMELDUNG_AUSGELOEST,
			Betreuungsstatus.SCHULAMT_MODULE_AKZEPTIERT,
			Betreuungsstatus.SCHULAMT_ANMELDUNG_UEBERNOMMEN,
			Betreuungsstatus.SCHULAMT_ANMELDUNG_STORNIERT);
	}

	public boolean isSchulamtAnmeldungUebernommen() {
		return SCHULAMT_ANMELDUNG_UEBERNOMMEN == this;
	}

	public boolean isSchulamtAnmeldungStorniert() {
		return SCHULAMT_ANMELDUNG_STORNIERT == this;
	}

	public boolean isStorniert() {
		return STORNIERT == this;
	}

	public boolean isIgnoriert() {
		return SCHULAMT_MUTATION_IGNORIERT == this;
	}

	@SuppressWarnings({"Duplicates", "checkstyle:CyclomaticComplexity"})
	public static Set<Betreuungsstatus> allowedRoles(UserRole userRole) {
		switch (userRole) {
		case SUPER_ADMIN:
		case ADMIN_BG:
		case GESUCHSTELLER:
		case JURIST:
		case REVISOR:
		case ADMIN_INSTITUTION:
		case SACHBEARBEITER_INSTITUTION:
		case SACHBEARBEITER_BG:
		case ADMIN_TRAEGERSCHAFT:
		case SACHBEARBEITER_TRAEGERSCHAFT:
		case SACHBEARBEITER_TS:
		case ADMIN_GEMEINDE:
		case SACHBEARBEITER_GEMEINDE:
		case STEUERAMT:
		case ADMIN_MANDANT:
		case SACHBEARBEITER_MANDANT:
			return EnumSet.allOf(Betreuungsstatus.class);
		default:
			return EnumSet.noneOf(Betreuungsstatus.class);
		}
	}
}
