/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;

import ch.dvbern.ebegu.entities.AbstractAnmeldung;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Abwesenheit;
import ch.dvbern.ebegu.entities.AnmeldungFerieninsel;
import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.BetreuungsmitteilungPensum;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.types.DateRange;
import ch.dvbern.ebegu.validationgroups.BetreuungBestaetigenValidationGroup;

/**
 * Service zum Verwalten von Betreuungen
 */
public interface BetreuungService {

	/**
	 * Speichert die Betreuung neu in der DB falls der Key noch nicht existiert. Sonst wird die existierende Betreuung aktualisiert
	 * Bean validation wird eingeschaltet
	 *
	 * @param betreuung Die Betreuung als DTO
	 */
	@Nonnull
	Betreuung saveBetreuung(@Valid @Nonnull Betreuung betreuung, @Nonnull Boolean isAbwesenheit, @Nullable String externalClient);

	/**
	 * Speichert die AnmeldungTagesschule neu in der DB falls der Key noch nicht existiert. Sonst wird die existierende AnmeldungTagesschule aktualisiert
	 * Bean validation wird eingeschaltet
	 */
	@Nonnull
	AnmeldungTagesschule saveAnmeldungTagesschule(@Valid @Nonnull AnmeldungTagesschule anmeldungTagesschule);

	/**
	 * Speichert die AnmeldungFerieninsel neu in der DB falls der Key noch nicht existiert. Sonst wird die existierende AnmeldungFerieninsel aktualisiert
	 * Bean validation wird eingeschaltet
	 */
	@Nonnull
	AnmeldungFerieninsel saveAnmeldungFerieninsel(@Valid @Nonnull AnmeldungFerieninsel anmeldungFerieninsel);

	/**
	 * Setzt die Betreuungsplatzanfrage auf ABGEWIESEN und sendet dem Gesuchsteller eine E-Mail
	 */
	@Nonnull
	Betreuung betreuungPlatzAbweisen(@Valid @Nonnull Betreuung betreuung, @Nullable String externalClient);

	/**
	 * Setzt die Betreuungsplatzanfrage auf BESTAETIGT und sendet dem Gesuchsteller eine E-Mail,
	 * falls damit alle Betreuungen des Gesuchs bestaetigt sind.
	 */
	@Nonnull
	Betreuung betreuungPlatzBestaetigen(@Valid @ConvertGroup(to = BetreuungBestaetigenValidationGroup.class) @Nonnull Betreuung betreuung, @Nullable String externalClient);

	/**
	 * Setzt die Schulamt-Anmeldung auf SCHULAMT_ANMELDUNG_ABGELEHNT und sendet dem Gesuchsteller eine E-Mail
	 */
	@Nonnull
	AbstractAnmeldung anmeldungSchulamtAblehnen(@Valid @Nonnull AbstractAnmeldung anmeldung);

	/**
	 * Setzt die Schulamt-Anmeldung auf SCHULAMT_FALSCHE_INSTITUTION.
	 */
	@Nonnull
	AbstractAnmeldung anmeldungSchulamtFalscheInstitution(@Valid @Nonnull AbstractAnmeldung anmeldung);

	/**
	 * @param key PK (id) der Betreuung
	 * @return Betreuung mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Betreuung> findBetreuung(@Nonnull String key);

	/**
	 * Sucht die AnmeldungTagesschule mit der uebergebenen ID
	 */
	@Nonnull
	Optional<AnmeldungTagesschule> findAnmeldungTagesschule(@Nonnull String id);

	/**
	 * Sucht die AnmeldungFerieninsel mit der uebergebenen ID
	 */
	@Nonnull
	Optional<AnmeldungFerieninsel> findAnmeldungFerieninsel(@Nonnull String id);

	/**
	 * Sucht die (Tageschule oder Ferieninsel-) Anmeldung mit der uebergebenen ID
	 */
	@Nonnull
	Optional<? extends AbstractAnmeldung> findAnmeldung(@Nonnull String id);

	/**
	 * Sucht den Platz (Betreuung oder Anmeldung) mit der uebergebenen ID
	 */
	@Nonnull
	Optional<? extends AbstractPlatz> findPlatz(@Nonnull String id);

	/**
	 * @param betreuungId PK (id) der Betreuung
	 * @param doAuthCheck: Definiert, ob die Berechtigungen (Lesen/Schreiben) für diese Betreuung geprüft werden muss.
	 * @return Betreuung mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<Betreuung> findBetreuung(@Nonnull String betreuungId, boolean doAuthCheck);

	/**
	 * @param referenzNummer BGNummer der Anmeldung
	 * @return Anmeldung mit der angegebenen ID (z.B. 18.000116.1.2) oder null falls nicht vorhanden
	 */
	List<AbstractAnmeldung> findAnmeldungenByReferenzNummer(@Nonnull String referenzNummer);

	/**
	 * @param referenzNummer BGNummer der Anmeldung
	 * @return Anmeldung mit der angegebenen ID (z.B. 18.000116.1.2) die AKTUELLE oder NULL ist.
	 */
	List<AbstractAnmeldung> findNewestAnmeldungByReferenzNummer(@Nonnull String referenzNummer);

	/**
	 * Findet die entsprechende Betreuung in der uebergebenen Gesuchsperiode
	 */
	@Nonnull
	Optional<Betreuung> findSameBetreuungInDifferentGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull Dossier dossier,
		int betreuungNummer,
		int kindNummer);

	/**
	 * Wenn onlyGueltig = true:
	 * Gibt die aktuell gültige Betreuung für die übergebene BG Nummer zurück (z.B. 18.000116.1.2)
	 * Achtung: Diese kann sich auf einem noch nicht verfügten Gesuch befinden! (VERFUEGEN)
	 * Wenn onlyGueltig = false:
	 * return auch die Betreuung in andere Status (Warten Z.B.)
	 */
	Optional<Betreuung> findBetreuungByReferenzNummer(@Nonnull String referenzNummer, boolean onlyGueltig);

	/**
	 * @param betreuungId PK (id) der Betreuung
	 * @return Betreuung mit eager gefetchten {@link Betreuung#getBetreuungspensumContainers()} und
	 * {@link Betreuung#getAbwesenheitContainers()}
	 */
	@Nonnull
	Optional<Betreuung> findBetreuungWithBetreuungsPensen(@Nonnull String betreuungId);

	/**
	 * entfernt eine Betreuung aus der Database
	 *
	 * @param betreuungId Id der Betreuung zu entfernen
	 */
	void removeBetreuung(@Nonnull String betreuungId);

	/**
	 * entfernt eine Betreuuung aus der Databse. Um diese Methode aufzurufen muss man sich vorher vergewissern, dass die Betreuuung existiert
	 */
	void removeBetreuung(@Nonnull Betreuung betreuung, @Nullable String externalClient);

	/**
	 * Gibt die Pendenzen fuer einen Benutzer mit Rolle Institution oder Traegerschaft zurueck.
	 * Dies sind Betreuungen, welche zu einer Institution gehoeren, fuer welche der Benutzer berechtigt ist,
	 * und deren Status "WARTEN" ist.
	 */
	@Nonnull
	Collection<AbstractPlatz> getPendenzenBetreuungen();

	/**
	 * @param dossier Dossier, dessen verfuegte Betreuungen zurueckgegeben werden
	 * @return BetreuungList, welche zum Dossier gehoeren oder null
	 */
	@Nonnull
	List<Betreuung> findAllBetreuungenWithVerfuegungForDossier(@Nonnull Dossier dossier);

	/**
	 * Schliesst die Betreuung (Status GESCHLOSSEN_OHNE_VERFUEGUNG) und verfügt das Gesuch wenn moeglich
	 * ohne eine neue Verfuegung zu erstellen
	 * (bei gleichbleibenden Daten)
	 */
	@Nonnull
	Betreuung schliessenOhneVerfuegen(@Nonnull Betreuung betreuung);

	/**
	 * Schliesst die Betreuung (Status GESCHLOSSEN_OHNE_VERFUEGUNG) only
	 */
	@Nonnull
	Betreuung schliessenOnly(@Nonnull Betreuung betreuung);

	/**
	 * Gibt alle Betreuungen zurueck, welche Mutationen betreffen, die verfügt sind und deren
	 * betreuungMutiert-Flag noch nicht gesetzt sind
	 */
	@Nonnull
	List<Betreuung> getAllBetreuungenWithMissingStatistics(Mandant mandant);

	/**
	 * Gibt alle Abwesenheiten zurueck, welche Mutationen betreffen, die verfügt sind und deren
	 * abwesenheitMutiert-Flag noch nicht gesetzt sind
	 */
	@Nonnull
	List<Abwesenheit> getAllAbwesenheitenWithMissingStatistics(Mandant mandant);

	/**
	 * Sendet eine E-Mail an alle Institutionen die aktuell offene Pendenzen haben.
	 */
	void sendInfoOffenePendenzenNeuMitteilungInstitution();

	/**
	 * entfernt eine Anmeldung aus der Datenbank
	 *
	 * @param anmeldungId Id der zu entfernenden Anmeldung
	 */
	void removeAnmeldung(@Nonnull String anmeldungId);

	/**
	 * entfernt eine Anmeldung aus der Datenbank. Um diese Methode aufzurufen muss man sich vorher vergewissern, dass
	 * die Anmeldung existiert
	 */
	void removeAnmeldung(@Nonnull AbstractAnmeldung anmeldung);

	/**
	 * Setzt die Schulamt-Anmeldung auf SCHULAMT_MODULE_AKZEPTIERT und sendet dem Gesuchsteller eine E-Mail.
	 */
	@Nonnull
	AbstractAnmeldung anmeldungSchulamtModuleAkzeptieren(@Valid @Nonnull AbstractAnmeldung anmeldung);

	/**
	 Setzt die Schulamt-Anmeldung auf SCHULAMT_MUTATION_IGNORIERT
	 */
	@Nonnull
	AbstractAnmeldung anmeldungMutationIgnorieren(@Valid @Nonnull AbstractAnmeldung anmeldung);

	@Nonnull
	AbstractAnmeldung anmeldungSchulamtStornieren(@Valid @Nonnull AbstractAnmeldung anmeldung);

	void fireAnmeldungTagesschuleAddedEvent(@Nonnull AnmeldungTagesschule anmeldungTagesschule);

	@Nonnull
	Optional<AnmeldungTagesschule> findAnmeldungenTagesschuleByReferenzNummer(@Nonnull String referenzNummer);

	@Nonnull
	Set<BetreuungsmitteilungPensum> capBetreuungspensenToGueltigkeit(@Nonnull Set<BetreuungsmitteilungPensum> pensen, @Nonnull DateRange gueltigkeit);

	@Nonnull
	BigDecimal getMultiplierForAbweichnungen(@Nonnull Betreuung betreuung);

	void updateGueltigFlagOnPlatzAndVorgaenger(@Nonnull AbstractPlatz platz);

	/**
	 * Findet für eine Anmeldung die letzte gültige nicht ignorierte
	 */
	@Nonnull
	AbstractAnmeldung findVorgaengerAnmeldungNotIgnoriert(AbstractAnmeldung betreuung);

	@Nonnull
	List<AnmeldungTagesschule> findAnmeldungenTagesschuleByInstitution(@Nonnull Institution institution);
}
