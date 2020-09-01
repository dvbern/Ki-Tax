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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.dto.geoadmin.JaxWohnadresse;
import ch.dvbern.ebegu.entities.Adresse;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer Adresse
 */
@Stateless
@Local(AdresseService.class)
public class AdresseServiceBean extends AbstractBaseService implements AdresseService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private GeoadminSearchService geoadminSearchService;

	@Nonnull
	@Override
	public Adresse createAdresse(@Nonnull Adresse adresse) {
		Objects.requireNonNull(adresse);
		return persistence.persist(adresse);
	}

	@Nonnull
	@Override
	public Adresse updateAdresse(@Nonnull Adresse adresse) {
		Objects.requireNonNull(adresse);
		return persistence.merge(adresse);
	}

	@Nonnull
	@Override
	public Optional<Adresse> findAdresse(@Nonnull final String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		Adresse a = persistence.find(Adresse.class, id);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	public Collection<Adresse> getAllAdressen() {
		return new ArrayList<>(criteriaQueryHelper.getAll(Adresse.class));
	}

	@Override
	public void updateGemeindeAndBFS(Adresse adresse) {
		List<JaxWohnadresse> wohnadresseList = geoadminSearchService.findWohnadressenByStrasseAndOrt(
			adresse.getStrasse(),
			adresse.getHausnummer(),
			adresse.getOrt());

		Adresse adresseFromDB = persistence.find(Adresse.class, adresse.getId());
		adresseFromDB.setGemeinde(wohnadresseList.get(0).getGemeinde());
		adresseFromDB.setBfsNummer(wohnadresseList.get(0).getGemeindeBfsNr());
		persistence.merge(adresseFromDB);
	}
}
