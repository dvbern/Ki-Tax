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

package ch.dvbern.ebegu.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.validation.Valid;

import ch.dvbern.ebegu.dto.suchfilter.smarttable.MitteilungTableFilterDTO;
import ch.dvbern.ebegu.entities.Benutzer;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Betreuungsmitteilung;
import ch.dvbern.ebegu.entities.BetreuungspensumAbweichung;
import ch.dvbern.ebegu.entities.Dossier;
import ch.dvbern.ebegu.entities.Fall;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Institution;
import ch.dvbern.ebegu.entities.Mitteilung;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Service zum Verwalten von Mitteilungen
 */
public interface MitteilungService {

	/**
	 * Sendet die uebergebene Mitteilung. Der Empfaenger wird automatisch gesetzt
	 */
	@Nonnull
	Mitteilung sendMitteilung(@Nonnull Mitteilung mitteilung);

	/**
	 * Setzt die Mitteilung mit der uebergebenen ID als gelesen
	 */
	@Nonnull
	Mitteilung setMitteilungGelesen(@Nonnull String mitteilungsId);

	/**
	 * Setzt die Mitteilung mit der uebergebenen ID als erledigt
	 */
	@Nonnull
	Mitteilung setMitteilungErledigt(@Nonnull String mitteilungsId);

	/**
	 * Sucht die Mitteilung mit der uebergebenen ID
	 */
	@Nonnull
	Optional<Mitteilung> findMitteilung(@Nonnull String key);

	/**
	 * Sucht die Betreuungsmitteilung mit der uebergebenen ID
	 */
	@Nonnull
	Optional<Betreuungsmitteilung> findBetreuungsmitteilung(@Nonnull String key);

	/**
	 * Löscht alle offenen Mutationsmeldung für eine Betreuung
	 */
	void removeOffeneBetreuungsmitteilungenForBetreuung(Betreuung betreuung);

	/**
	 * Returns all not applied Betreuungsmitteilungen that are linked with the given Betreuung.
	 */
	@Nonnull
	Collection<Betreuungsmitteilung> findOffeneBetreuungsmitteilungenForBetreuung(@Nonnull Betreuung betreuung);

	/**
	 * Returns all Betreuungsmitteilungen that are linked with the given Betreuung.
	 */
	@Nonnull
	Collection<Betreuungsmitteilung> findAllBetreuungsmitteilungenForBetreuung(@Nonnull Betreuung betreuung);

	/**
	 * Returns all BetreuungspensumAbweichung that are linked with the given Betreuung.
	 */
	@Nonnull
	Collection<BetreuungspensumAbweichung> findAllBetreuungspensumAbweichungenForBetreuung(@Nonnull Betreuung betreuung);

	/**
	 * Gibt alle (Betreuungs-) Mitteilungen fuer die uebergebene Betreuung zurueck
	 */
	@Nonnull
	Collection<Mitteilung> findAllMitteilungenForBetreuung(@Nonnull Betreuung betreuung);

	/**
	 * Gibt alle Mitteilungen fuer das uebergebene Dossier zurueck, welche fuer den eingeloggten Benutzer sichtbar
	 * sind.
	 */
	@Nonnull
	Collection<Mitteilung> getMitteilungenForCurrentRolle(@Nonnull Dossier dossier);

	/**
	 * Gibt alle Mitteilungen fuer die uebergebene Betreuung zurueck, welche fuer den eingeloggten Benutzer sichtbar
	 * sind.
	 */
	@Nonnull
	Collection<Mitteilung> getMitteilungenForCurrentRolle(@Nonnull Betreuung betreuung);

	/**
	 * Loescht die uebergebene Mitteilung
	 */
	void removeMitteilung(@Nonnull Mitteilung mitteilung);

	/**
	 * Loescht alle Mitteilungen des uebergebenen Falles
	 */
	void removeAllMitteilungenForFall(@Nonnull Fall fall);

	/**
	 * Loescht alle Betreuungsmitteilungen des uebergebenen Gesuchs.
	 */
	void removeAllBetreuungMitteilungenForGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Loescht alle BetreuungspensumAbweichungen des uebergebenen Gesuchs.
	 */
	void removeAllBetreuungspensumAbweichungenForGesuch(@Nonnull Gesuch gesuch);

	/**
	 * Sucht alle Mitteilungen des uebergebenen Dossiers und fuer jede, die im Status NEU ist, wechselt
	 * ihren Status auf GELESEN.
	 */
	@Nonnull
	Collection<Mitteilung> setAllNewMitteilungenOfDossierGelesen(@Nonnull Dossier dossier);

	/**
	 * Gibt alle ungelesenen Mitteilungen (Status NEU) fuer das uebergebene Dossier zurueck, welche fuer den
	 * eingeloggten Benutzer sichtbar sind
	 */
	@Nonnull
	Collection<Mitteilung> getNewMitteilungenOfDossierForCurrentRolle(@Nonnull Dossier dossier);

	/**
	 * Gibt die Anzahl aller ungelesenen Mitteilungen (Status NEU), welche fuer den eingeloggten Benutzer sichtbar
	 * sind.
	 */
	@Nonnull
	Long getAmountNewMitteilungenForCurrentBenutzer();

	/**
	 * Entfernt alle offenen Betreuungsmitteilungen und speichert stattdessen die übergebene Betreuungsmitteilung.
	 */
	void replaceBetreungsmitteilungen(@Valid @Nonnull Betreuungsmitteilung betreuungsmitteilung);

	/**
	 * Sendet die uebergebene Betreuungsmitteilung. Der Empfaenger wird automatisch gesetzt
	 */
	@Nonnull
	Betreuungsmitteilung sendBetreuungsmitteilung(@Valid @Nonnull Betreuungsmitteilung betreuungsmitteilung);

	/**
	 * Applies all passed Betreuungspensen from the Betreuungsmitteilung to the existing Betreuung with the same
	 * number.
	 * If the newest Antrag is verfuegt, it will create a new Mutation out of it and apply the changes in this new
	 * Antrag.
	 * Returns the Antrag, in which the mitteilung was applied, which is much more useful than the mitteilung itself
	 * since normally you only need to know where the mitteilung was applied.
	 */
	@Nonnull
	Gesuch applyBetreuungsmitteilung(@Nonnull Betreuungsmitteilung mitteilung);

	/**
	 * Returns the newest Betreuungsmitteilung for the given Betreuung
	 */
	@Nonnull
	Optional<Betreuungsmitteilung> findNewestBetreuungsmitteilung(@Nonnull String betreuungId);

	/**
	 * Leitet die Mitteilung an einen weiteren Benutzer weiter.
	 */
	@Nonnull
	Mitteilung mitteilungWeiterleiten(@Nonnull String mitteilungId, @Nonnull String userName);

	/**
	 * Methode welche jeweils eine bestimmte Menge an Suchresultate fuer die Paginatete Suchtabelle zuruckgibt. Wenn
	 * das Flag includeClosed auf true
	 * gesetzt ist, werden auch bereits abgeschlossene Mitteilungen geliefert.
	 *
	 * @return Resultatpaar, der erste Wert im Paar ist die Anzahl Resultate, der zweite Wert ist die Resultatliste
	 */
	@Nonnull
	Pair<Long, List<Mitteilung>> searchMitteilungen(
		@Nonnull MitteilungTableFilterDTO mitteilungTableFilterDto,
		@Nonnull Boolean includeClosed
	);

	/**
	 * Ermittelt, ob der übergebene Benutzer entweder als Sender oder als Empfänger in irgendeiner Mitteilung gesetzt ist
	 * Es wird keine Berechtigungsprüfung durchgeführt, da nur ja/nein zurückgeben wird.
	 */
	boolean hasBenutzerAnyMitteilungenAsSenderOrEmpfaenger(@Nonnull Benutzer benutzer);

	/**
	 * Erstellt und sendet eine Mutationsmeldung aus den bestehenden Betreuungspensen und BetreuungspensumAbweichungen
	 */
	void createMutationsmeldungAbweichungen(@Nonnull Betreuungsmitteilung mitteilung, @Nonnull Betreuung betreuung);

	boolean hasInstitutionOffeneMitteilungen(Institution institution);
}
