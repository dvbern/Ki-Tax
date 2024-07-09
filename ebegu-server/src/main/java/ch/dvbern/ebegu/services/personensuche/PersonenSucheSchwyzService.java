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
 *
 */

package ch.dvbern.ebegu.services.personensuche;

import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.AbstractPersonEntity;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.errors.PersonenSucheServiceBusinessException;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import ch.dvbern.ebegu.ws.ewk.GeresClient;
import ch.dvbern.ebegu.ws.ewk.GeresUtil;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Implementation of https://intra.dvbern.ch/display/KIB/GERES+Schwyz
 */
@RequiredArgsConstructor
public class PersonenSucheSchwyzService implements PersonenSucheService {

	private final GeresClient geresClient;

	@Nonnull
	@Override
	public EWKResultat suchePersonen(@Nonnull Gesuch gesuch)
		throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		Set<EWKPerson> personen = new HashSet<>();
		var resultatGs1 = suchePersonMitAhvNrInGemeinde(gesuch.getGesuchsteller1(), gesuch.getDossier().getGemeinde());
		resultatGs1.setGesuchsteller(true);
		personen.add(resultatGs1);

		if (resultatGs1.isGefunden()) {
			personen.addAll(suchePersonenImGleichenHaushalt(resultatGs1));
		}

		if (gesuch.getGesuchsteller2() != null) {
			List<EWKPerson> inResultGs2 =
				findInResult(gesuch.getGesuchsteller2().getGesuchstellerJA(), personen);

			if (inResultGs2.isEmpty()) {
				personen.add(suchePersonMitAhvNrInGemeinde(gesuch.getGesuchsteller2(), gesuch.getDossier().getGemeinde()));
			} else {
				personen.addAll(inResultGs2);
			}
		}

		for (KindContainer kindContainer : gesuch.getKindContainers()) {
			Kind kind = Objects.requireNonNull(kindContainer.getKindJA());
			List<EWKPerson> ewkPeople =
				findInResult(kind, personen).stream()
					.map(ewkPerson -> ewkPerson.setKind(true))
					.collect(Collectors.toList());
			if (ewkPeople.isEmpty()) {
				EWKPerson notFoundPerson = GeresUtil.createNotFoundPerson(kind);
				notFoundPerson.setKind(true);
				personen.add(notFoundPerson);
			} else {
				personen.addAll(ewkPeople);
			}
		}

		return wrapInResult(personen);
	}

	private EWKResultat wrapInResult(Set<EWKPerson> personen) {
		EWKResultat ewkResultat = new EWKResultat();
		List<EWKPerson> personenList = new ArrayList<>(personen);
		Collections.sort(personenList);
		ewkResultat.setPersonen(personenList);
		return ewkResultat;
	}

	private Set<EWKPerson> suchePersonenImGleichenHaushalt(EWKPerson ewkGs1)
		throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		EWKResultat personenImGleichenHaushalt = geresClient.suchePersonenInHaushalt(
			ewkGs1.getAdresse().getWohnungsId(),
			ewkGs1.getAdresse().getGebaeudeId());
		return new HashSet<>(personenImGleichenHaushalt.getPersonen());
	}

	private EWKPerson suchePersonMitAhvNrInGemeinde(GesuchstellerContainer gesuchsteller, Gemeinde gemeinde)
		throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		Gesuchsteller gesuchstellerJA = Objects.requireNonNull(gesuchsteller.getGesuchstellerJA());
		return geresClient.suchePersonMitAhvNummerInGemeinde(gesuchstellerJA, gemeinde);
	}

	private List<EWKPerson> findInResult(@NotNull AbstractPersonEntity personEntity, Set<EWKPerson> personen) {
		return personen.stream().filter(GeresUtil.matches(personEntity)).collect(Collectors.toList());
	}
}
