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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.entities.ApplicationProperty;
import ch.dvbern.ebegu.entities.Mandant;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.DemoFeatureTyp;

/**
 * Service zum Verwalten von Application Properties
 */
public interface ApplicationPropertyService {

	/**
	 * Speichert das property neu in der DB falls der Key noch nicht existeirt. Ansonsten wird ein neues Property mit
	 * diesem
	 * Key erstellt
	 *
	 * @param key name des Property
	 * @param value Wert des Property
	 * @return ApplicationProperty mit key und value
	 */
	@Nonnull
	ApplicationProperty saveOrUpdateApplicationProperty(
			@Nonnull ApplicationPropertyKey key,
			@Nonnull String value,
			@Nonnull Mandant mandant);

	/**
	 * @param key name des Property
	 * @return Property mit demg egebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<ApplicationProperty> readApplicationProperty(
			@Nonnull ApplicationPropertyKey key,
			@Nonnull Mandant mandant);

	/**
	 * List die Liste der zugelassenen Mimetypes
	 */
	@Nonnull
	Collection<String> readMimeTypeWhitelist(@Nonnull Mandant mandant);

	/**
	 * Versucht den uebergebenen String in einene  key umzuwandeln und gibt dann das ensprechende property zurueck.
	 * Wenn der String keinem key enspricht exception
	 */
	Optional<ApplicationProperty> readApplicationProperty(String keyParam, @Nonnull Mandant mandant);

	/**
	 * @return Liste aller ApplicationProperties aus der DB
	 */
	@Nonnull
	List<ApplicationProperty> getAllApplicationProperties(@Nonnull Mandant mandant);

	/**
	 * removs an Application Property From the Databse
	 */
	void removeApplicationProperty(@Nonnull ApplicationPropertyKey testKey, @Nonnull Mandant mandant);

	/**
	 * Sucht das Property mit dem uebergebenen Key und gibt dessen Wert als String zurueck.
	 */
	@Nullable
	String findApplicationPropertyAsString(@Nonnull ApplicationPropertyKey name, @Nonnull Mandant mandant);

	/**
	 * Sucht das Property mit dem uebergebenen Key und gibt dessen Wert als BigDecimal zurueck.
	 */
	@Nullable
	BigDecimal findApplicationPropertyAsBigDecimal(@Nonnull ApplicationPropertyKey name, @Nonnull Mandant mandant);

	/**
	 * Sucht das Property mit dem uebergebenen Key und gibt dessen Wert als Integer zurueck.
	 */
	@Nullable
	Integer findApplicationPropertyAsInteger(@Nonnull ApplicationPropertyKey name, @Nonnull Mandant mandant);

	/**
	 * Sucht das Property mit dem uebergebenen Key und gibt dessen Wert als Boolean zurueck.
	 */
	@Nullable
	Boolean findApplicationPropertyAsBoolean(@Nonnull ApplicationPropertyKey name, @Nonnull Mandant mandant);

	/**
	 * Sucht das Property mit dem uebergebenen Key und gibt dessen Wert als Boolean zurueck.
	 * Falls das Property nicht gefunden wird, wird defaultValue zurueckgegeben.
	 */
	@Nonnull
	Boolean findApplicationPropertyAsBoolean(
			@Nonnull ApplicationPropertyKey name,
			@Nonnull Mandant mandant,
			boolean defaultValue);

	/**
	 * Ab diesem Datum gelten fuer die Stadt Bern die ASIV Regeln
	 */
	@Nonnull
	LocalDate getStadtBernAsivStartDatum(@Nonnull Mandant mandant);

	/**
	 * Wenn TRUE koennen die Zeitraeume ab ASIV_START_DATUM verfuegt werden
	 */
	@Nonnull
	Boolean isStadtBernAsivConfigured(@Nonnull Mandant mandant);

	/**
	 * Wenn TRUE ist die zweite Phase von der Notverordnung aktiv
	 */
	@Nonnull
	Boolean isKantonNotverordnungPhase2Aktiviert(@Nonnull Mandant mandant);

	/**
	 * Wenn TRUE sind die schnittstelle events publisht
	 */
	@Nonnull
	Boolean isPublishSchnittstelleEventsAktiviert(@Nonnull Mandant mandant);

	/**
	 * Gibt eine Liste mit den aktivierten DemoFeatures zurück
	 */
	List<DemoFeatureTyp> getActivatedDemoFeatures(@Nonnull Mandant mandant);
}
