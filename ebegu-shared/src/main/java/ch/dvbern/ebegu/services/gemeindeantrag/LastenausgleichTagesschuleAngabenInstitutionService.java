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

package ch.dvbern.ebegu.services.gemeindeantrag;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitution;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;

/**
 * Service fuer den Lastenausgleich der Tagesschulen, Formulare der Institutionen
 */
public interface LastenausgleichTagesschuleAngabenInstitutionService {

	/**
	 * Erstellt einen LastenausgleichTagesschuleAngabenInstitutionContainer fuer jede Tagesschule der Gemeinde
	 */
	void createLastenausgleichTagesschuleInstitution(
		@Nonnull LastenausgleichTagesschuleAngabenGemeindeContainer gemeindeContainer);

	/**
	 * Sucht den LastenausgleichTagesschuleAngabenGemeindeContainer mit der uebergebenen ID
	 */
	@Nonnull
	Optional<LastenausgleichTagesschuleAngabenInstitutionContainer> findLastenausgleichTagesschuleAngabenInstitutionContainer(
		@Nonnull String id);


	/**
	 * Speichert einen LastenausgleichTagesschuleAngabenInstitutionContainer
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenInstitutionContainer saveLastenausgleichTagesschuleInstitution(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer);

	/**
	 * Reicht den Lastenausgleich zum Prüfen durch die Gemeinden ein, inkl. kopieren der Daten vom Deklaration- in den
	 * Korrekturen-Container, falls die Vorbedingungen dazu erfuellt sind.
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenInstitutionContainer lastenausgleichTagesschuleInstitutionFreigeben(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer);

	/**
	 * Gibt den Lastenausgleich zum Lesen für die Kantone frei, inkl. Setzen des Status auf "GEPRUEFT".
	 * @return
	 */
	@Nonnull
	LastenausgleichTagesschuleAngabenInstitutionContainer lastenausgleichTagesschuleInstitutionGeprueft(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer institutionContainer);

	/**
	 * Gibt alle LastenausgleichTagesschuleAngabenInsitutionContainer zurück, die zu diesem Gemeinde-Antrag gehören
	 */
	List<LastenausgleichTagesschuleAngabenInstitutionContainer> findLastenausgleichTagesschuleAngabenInstitutionByGemeindeAntragId(String gemeindeAntragId);

	LastenausgleichTagesschuleAngabenInstitutionContainer latsAngabenInstitutionContainerWiederOeffnen(
			LastenausgleichTagesschuleAngabenInstitutionContainer fallContainer);

	@Nonnull
	Map<String, Integer> calculateAnzahlEingeschriebeneKinder(
		@Nonnull LastenausgleichTagesschuleAngabenInstitution angabenInstitution,
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer container
	);
}
