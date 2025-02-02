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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ch.dvbern.ebegu.dto.personensuche.EWKAdresse;
import ch.dvbern.ebegu.dto.personensuche.EWKPerson;
import ch.dvbern.ebegu.dto.personensuche.EWKResultat;
import ch.dvbern.ebegu.entities.AbstractPersonEntity;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Gesuchsteller;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Kind;
import ch.dvbern.ebegu.entities.KindContainer;
import ch.dvbern.ebegu.errors.PersonenSucheServiceBusinessException;
import ch.dvbern.ebegu.errors.PersonenSucheServiceException;
import ch.dvbern.ebegu.ws.ewk.GeresClient;
import ch.dvbern.ebegu.ws.ewk.GeresUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Implementation of https://intra.dvbern.ch/display/KIB/GERES+Bern
 */
@RequiredArgsConstructor
public class PersonenSucheBernService implements PersonenSucheService {

	private static final Logger LOG = LoggerFactory.getLogger(PersonenSucheBernService.class);

	private final GeresClient geresClient;

	/**
	 * Sucht die Personen eine Gesuchs innerhalb der Einwohnerkontrolle. Es wird wie folgt gesucht:
	 *
	 * Gesuchsteller 1 inkl. aller Personen im gleichen Haushalt
	 * Gsuchsteller 2
	 * Alle Kinder
	 *
	 * Zum schluss wird geschaut, dass alle Personen nur einmal vorkommen
	 *
	 */

	@Override
	@Nonnull
	public EWKResultat suchePersonen(@Nonnull Gesuch gesuch) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		EWKResultat resultat = new EWKResultat();
		EWKPerson  personMitWohnsitzInGemeindeUndPeriode = suchePersonMitWohnsitzInGemeindeUndPeriode(gesuch.getGesuchsteller1(), gesuch);
		if (personMitWohnsitzInGemeindeUndPeriode != null) {
			final EWKAdresse adresseOfPersonMitWohnsitzInGemeindeUndPeriode = personMitWohnsitzInGemeindeUndPeriode.getAdresse();
			if (adresseOfPersonMitWohnsitzInGemeindeUndPeriode != null) {
				resultat.getPersonen().addAll(geresClient.suchePersonenInHaushalt(
					adresseOfPersonMitWohnsitzInGemeindeUndPeriode.getWohnungsId(),
					adresseOfPersonMitWohnsitzInGemeindeUndPeriode.getGebaeudeId()).getPersonen());
			}
		}
		sucheGesuchstellerInHaushaltOderSonstOhneBfsEinschraenkung(resultat, gesuch.getGesuchsteller1());
		sucheGesuchstellerInHaushaltOderSonstOhneBfsEinschraenkung(resultat, gesuch.getGesuchsteller2());
		if (gesuch.getKindContainers() != null) {
			for (KindContainer kindContainer : gesuch.getKindContainers()) {
				sucheKindInHaushaltOderSonstOhneBfsEinschraenkung(resultat, kindContainer.getKindJA());
			}
		}
		resultat.setPersonen(entferneNichtAktuelleDaten(resultat.getPersonen(), gesuch.getGesuchsperiode()));
		Collections.sort(resultat.getPersonen());
		return resultat;
	}

	/**
	 *
	 * @param personen Liste von Personeneintraegen zum filtern
	 * @param gesuchsperiode
	 * @return neue Liste ohne die Personeneintragen deren Aufenthaltsperiode sich nicht mit der Gesuchsperiode schneidet
	 */
	@Nonnull
	private List<EWKPerson> entferneNichtAktuelleDaten(@Nonnull List<EWKPerson> personen, @Nonnull Gesuchsperiode gesuchsperiode) {
		return personen
			.stream()
			.filter(person ->
				person.isWohnsitzInPeriode(gesuchsperiode))
			.collect(Collectors.toList());
	}

	/**
	 * Suche in den bisher gefundenen Haushaltspersonen nach den uebergebenen Personendaten.
	 * Wenn wir sie finden setzen wir die ensprechenden Flags. Wen nicht suchen wir die Person ueber EWK
	 *
	 * @param resultat bereits bekantne personen
	 * @param name der gesuchten Person
	 * @param vorname vorname der gesuchten Person
	 * @param geburtsdatum geburtsdatum der gesuchten Person
	 * @param geschlecht geschlecht der gesuchten person
	 * @param isGesuchsteller true wenn die gesuchte person Gesuchsteller ist
	 * @param isKind true wenn die gescuhte Person ein Kind ist
	 * @throws PersonenSucheServiceException
	 * @throws PersonenSucheServiceBusinessException
	 */
	private void suchePersonInHaushaltOderSonstOhneBfsEinschraenkung(
		@Nonnull EWKResultat resultat,
		AbstractPersonEntity personEntity,
		boolean isGesuchsteller,
		boolean isKind
	) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		Objects.requireNonNull(resultat, "resultat darf nicht null sein");
		Objects.requireNonNull(personEntity.getNachname(), "name darf nicht null sein");
		Objects.requireNonNull(personEntity.getVorname(), "vorname darf nicht null sein");
		Objects.requireNonNull(personEntity.getGeburtsdatum(), "geburtsdatum darf nicht null sein");
		Objects.requireNonNull(personEntity.getGeschlecht(), "geschlecht darf nicht null sein");
		// versuche die gesuchte person zu matchen. wenn gefunde
			List<EWKPerson> personenInHaushalt = resultat.getPersonen().stream()
				.filter(GeresUtil.matches(personEntity))
				.collect(Collectors.toList());
			personenInHaushalt.forEach(person -> {
				person.setGesuchsteller(isGesuchsteller);
				person.setKind(isKind);
			});
			// wenn Person noch nicht gefunden wurde suchen wir sie in ewk
			if (personenInHaushalt.isEmpty()) {
				EWKResultat personenOhneBfs = geresClient.suchePersonMitFallbackOhneVorname(
					personEntity.getNachname(),
					personEntity.getVorname(),
					personEntity.getGeburtsdatum(),
					personEntity.getGeschlecht());
				 // add "not-found" resultat
				if (personenOhneBfs.getPersonen().isEmpty()) {
					personenOhneBfs.getPersonen().add(GeresUtil.createNotFoundPerson(personEntity));
				}
				personenOhneBfs.getPersonen()
					.forEach(person->{
						person.setGesuchsteller(isGesuchsteller);
						person.setKind(isKind);
					});
				resultat.getPersonen().addAll(personenOhneBfs.getPersonen());
			}
	}


	private void sucheGesuchstellerInHaushaltOderSonstOhneBfsEinschraenkung(
		@Nonnull EWKResultat resultat,
		@Nullable GesuchstellerContainer gesuchstellerContainer
	) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		if (gesuchstellerContainer != null && gesuchstellerContainer.getGesuchstellerJA() != null) {
			Gesuchsteller gesuchsteller = gesuchstellerContainer.getGesuchstellerJA();
			suchePersonInHaushaltOderSonstOhneBfsEinschraenkung(resultat, gesuchsteller, true, false);
		}
	}

	private void sucheKindInHaushaltOderSonstOhneBfsEinschraenkung(
		@Nonnull EWKResultat resultat,
		@Nullable Kind kind
	) throws PersonenSucheServiceException,
		PersonenSucheServiceBusinessException {
		if (kind != null) {
			suchePersonInHaushaltOderSonstOhneBfsEinschraenkung(resultat, kind, false, true);
		}
	}

	@Nullable
	private EWKPerson suchePersonMitWohnsitzInGemeindeUndPeriode(
		@Nullable GesuchstellerContainer gesuchstellerContainer,
		@Nonnull Gesuch gesuch
	) throws PersonenSucheServiceException, PersonenSucheServiceBusinessException {
		if (gesuchstellerContainer == null || gesuchstellerContainer.getGesuchstellerJA() == null) {
			return null;
		}
		Gesuchsteller gesuchsteller = gesuchstellerContainer.getGesuchstellerJA();
		final Long bfsNummer = gesuch.getDossier().getGemeinde().getBfsNummer();
		Objects.requireNonNull(bfsNummer);
		final String nachname = gesuchsteller.getNachname();
		final String vorname = gesuchsteller.getVorname();
		final LocalDate geburtsdatum = gesuchsteller.getGeburtsdatum();
		EWKResultat ewkResultat =  geresClient.suchePersonMitFallbackOhneVorname(nachname, vorname, geburtsdatum, gesuchsteller.getGeschlecht(), bfsNummer);
		if (!ewkResultat.getPersonen().isEmpty()) {
			EWKPerson person = ewkResultat.getPersonen().get(0);
			if (ewkResultat.getPersonen().size() > 1) {
				LOG.warn("Mehr als eine Person in Gemeinde mit matchenden suchresultaten gefunden fuer nachname {}, vorname {} , gebdatum {}, bfsnummer {}", nachname, vorname, geburtsdatum, bfsNummer);
				// leer zuruckgeben
				return null;
			}
			if (person.isWohnsitzInPeriode(gesuch.getGesuchsperiode())) {
				return person;
			}
		}
		return null;
	}
}
