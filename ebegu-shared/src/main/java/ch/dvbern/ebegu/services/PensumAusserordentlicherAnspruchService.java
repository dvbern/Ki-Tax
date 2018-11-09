/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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

package ch.dvbern.ebegu.services;

import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.PensumAusserordentlicherAnspruch;

/**
 * Service zum Verwalten von PensumAusserordentlicherAnspruch
 */
public interface PensumAusserordentlicherAnspruchService {

	/**
	 * Aktualisiert die PensumAusserordentlicherAnspruch in der DB
	 *
	 * @param pensumAusserordentlicherAnspruch die PensumAusserordentlicherAnspruch als DTO
	 * @return Die aktualisierte PensumAusserordentlicherAnspruch
	 */
	@Nonnull
	PensumAusserordentlicherAnspruch savePensumAusserordentlicherAnspruch(@Nonnull PensumAusserordentlicherAnspruch pensumAusserordentlicherAnspruch);

	/**
	 * @param pensumAusserordentlicherAnspruchId PK (id) der PensumAusserordentlicherAnspruch
	 * @return PensumAusserordentlicherAnspruch mit dem gegebenen key oder null falls nicht vorhanden
	 */
	@Nonnull
	Optional<PensumAusserordentlicherAnspruch> findPensumAusserordentlicherAnspruch(@Nonnull String pensumAusserordentlicherAnspruchId);

}
