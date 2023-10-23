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

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.InstitutionStammdaten;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenGemeindeContainer;
import ch.dvbern.ebegu.entities.gemeindeantrag.LastenausgleichTagesschuleAngabenInstitutionContainer;

import javax.annotation.Nonnull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	@Nonnull
	LastenausgleichTagesschuleAngabenInstitutionContainer latsAngabenInstitutionContainerWiederOeffnenGemeinde(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer fallContainer);

	@Nonnull
	LastenausgleichTagesschuleAngabenInstitutionContainer latsAngabenInstitutionContainerWiederOeffnenTS(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer fallContainer);

	@Nonnull
	Map<String, Integer> calculateAnzahlEingeschriebeneKinder(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer container
	);

	@Nonnull
	Map<String, BigDecimal> calculateDurchschnittKinderProTag(
		@Nonnull LastenausgleichTagesschuleAngabenInstitutionContainer container
	);

	@Nonnull
	BigDecimal countBetreuungsstundenPerYearForTagesschuleAndPeriode(InstitutionStammdaten stammdaten, Gesuchsperiode gesuchsperiode, boolean countForNextYear);

	@Nonnull
	List<AnmeldungTagesschule> findTagesschuleAnmeldungenForTagesschuleStammdatenAndPeriode(
		@Nonnull InstitutionStammdaten stammdaten,
		@Nonnull Gesuchsperiode gesuchsperiode
	);
	@Nonnull
	List<AnmeldungTagesschule> findTagesschuleAnmeldungenForTagesschuleStammdatenAndPeriodeOneYearAfterStichtag(
		@Nonnull InstitutionStammdaten stammdaten,
		@Nonnull Gesuchsperiode gesuchsperiode
	);
}
