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

package ch.dvbern.ebegu.tests.services;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.services.AbstractBaseService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.ebegu.test.TestDataUtil;

import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION;
import static ch.dvbern.ebegu.enums.EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PENSUM_KITA_MIN;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PENSUM_TAGESELTERN_MIN;
import static ch.dvbern.ebegu.enums.EinstellungKey.PARAM_PENSUM_TAGESSCHULE_MIN;

/**
 * Dummyservice fuer Einstellungen
 */
@Stateless
@Alternative
@Local(EinstellungService.class)
public class EinstellungDummyServiceBean extends AbstractBaseService implements EinstellungService {

	private final Map<EinstellungKey, Einstellung> dummyObjects;

	public EinstellungDummyServiceBean() {
		this.dummyObjects = new EnumMap<>(EinstellungKey.class);
		Gesuchsperiode gesuchsperiode1718 = TestDataUtil.createGesuchsperiode1718();

		dummyObjects.put(
			PARAM_PENSUM_KITA_MIN,
			new Einstellung(PARAM_PENSUM_KITA_MIN, "10", gesuchsperiode1718));
		dummyObjects.put(
			PARAM_PENSUM_TAGESELTERN_MIN,
			new Einstellung(PARAM_PENSUM_TAGESELTERN_MIN, "20", gesuchsperiode1718));
		dummyObjects.put(
			PARAM_PENSUM_TAGESSCHULE_MIN,
			new Einstellung(PARAM_PENSUM_TAGESSCHULE_MIN, "0", gesuchsperiode1718));
		dummyObjects.put(
			GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE,
			new Einstellung(GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE, "KINDERGARTEN2", gesuchsperiode1718));
		dummyObjects.put(
			FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION,
			new Einstellung(FACHSTELLE_MIN_PENSUM_SOZIALE_INTEGRATION, "20", gesuchsperiode1718));
		dummyObjects.put(
			FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION,
			new Einstellung(FACHSTELLE_MAX_PENSUM_SOZIALE_INTEGRATION, "60", gesuchsperiode1718));
		dummyObjects.put(
			FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION,
			new Einstellung(FACHSTELLE_MIN_PENSUM_SPRACHLICHE_INTEGRATION, "40", gesuchsperiode1718));
		dummyObjects.put(
			FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION,
			new Einstellung(FACHSTELLE_MAX_PENSUM_SPRACHLICHE_INTEGRATION, "40", gesuchsperiode1718));
	}

	@Nonnull
	@Override
	public Einstellung saveEinstellung(@Nonnull Einstellung einstellung) {
		Objects.requireNonNull(einstellung);
		this.dummyObjects.put(einstellung.getKey(), einstellung);
		return einstellung;
	}

	@Nonnull
	@Override
	public Optional<Einstellung> findEinstellung(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		return this.dummyObjects.values().stream().filter(einstellung -> einstellung.getId().equals(id)).findFirst();
	}

	@Nonnull
	@Override
	public Einstellung findEinstellung(
		@Nonnull EinstellungKey key,
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		return findEinstellung(key, gemeinde, gesuchsperiode, null);
	}

	@Nonnull
	@Override
	public Einstellung findEinstellung(
		@Nonnull EinstellungKey key, @Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode,
		@Nullable EntityManager em) {

		Einstellung mockParameter = this.dummyObjects.get(key);
		if (mockParameter != null) {
			return mockParameter;
		}
		throw new EntityNotFoundException("");
	}

	@Nonnull
	@Override
	public Collection<Einstellung> getAllEinstellungenBySystem(@Nonnull Gesuchsperiode gesuchsperiode) {
		return dummyObjects.values();
	}

	@Nonnull
	@Override
	public Collection<Einstellung> getAllEinstellungenByMandant(@Nonnull Gesuchsperiode gesuchsperiode) {
		return dummyObjects.values();
	}

	@Nonnull
	@Override
	public Map<EinstellungKey, Einstellung> getAllEinstellungenByGemeindeAsMap(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode) {
		Map<EinstellungKey, Einstellung> result = new HashMap<>();
		Collection<Einstellung> paramsForGesuchsperiode = getAllEinstellungenBySystem(gesuchsperiode);
		paramsForGesuchsperiode.stream().forEach(ebeguParameter -> result.put(ebeguParameter.getKey(),
			ebeguParameter));
		return result;
	}

	@Override
	public void copyEinstellungenToNewGesuchsperiode(
		@Nonnull Gesuchsperiode gesuchsperiodeToCreate,
		@Nonnull Gesuchsperiode lastGesuchsperiode) {
		// nop
	}

	@Override
	public void deleteEinstellungenOfGesuchsperiode(@Nonnull Gesuchsperiode gesuchsperiode) {
		// nop
	}

	@Nonnull
	@Override
	public List<Einstellung> findEinstellungen(
		@Nonnull EinstellungKey key,
		@Nullable Gesuchsperiode gesuchsperiode) {
		return dummyObjects.values()
			.stream()
			.filter(einstellung -> einstellung.getKey() == key)
			.collect(Collectors.toList());
	}
}
