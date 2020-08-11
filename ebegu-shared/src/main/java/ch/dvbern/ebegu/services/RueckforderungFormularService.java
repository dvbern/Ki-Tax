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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import ch.dvbern.ebegu.entities.RueckforderungFormular;
import ch.dvbern.ebegu.entities.RueckforderungMitteilung;
import ch.dvbern.ebegu.enums.RueckforderungStatus;

/**
 * Service fuer die Rueckforderungsformulare
 */
public interface RueckforderungFormularService {

	/**
	 * Erstellt leere Rückforderungsformulare für alle Kitas & TFOs die in kiBon existieren
	 * und bisher kein Rückforderungsformular haben
	 */
	@Nonnull
	List<RueckforderungFormular> initializeRueckforderungFormulare();

	@Nonnull
	RueckforderungFormular createRueckforderungFormular(RueckforderungFormular rueckforderungFormular);

	@Nonnull
	List<RueckforderungFormular> getRueckforderungFormulareForCurrentBenutzer();

	@Nonnull
	Optional<RueckforderungFormular> findRueckforderungFormular(String id);

	@Nonnull
	RueckforderungFormular save(RueckforderungFormular rueckforderungFormular);

	@Nonnull
	RueckforderungFormular saveAndChangeStatusIfNecessary(RueckforderungFormular rueckforderungFormular);

	@Nonnull
	Collection<RueckforderungFormular> getRueckforderungFormulareByStatus(@Nonnull List<RueckforderungStatus> status);

	@Nonnull
	RueckforderungFormular addMitteilung(RueckforderungFormular formular, RueckforderungMitteilung mitteilung);

	void initializePhase2();

	@Nonnull
	RueckforderungFormular resetStatusToInBearbeitungInstitutionPhase2(@Nonnull String id);

	@Nonnull
	RueckforderungFormular resetStatusToInPruefungKantonPhase2(@Nonnull String id);

	@Nonnull
	RueckforderungFormular provisorischeVerfuegung(RueckforderungFormular formular);
}
