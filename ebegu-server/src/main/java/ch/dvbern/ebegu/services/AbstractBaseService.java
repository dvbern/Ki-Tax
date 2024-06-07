/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
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

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.lib.cdipersistence.Persistence;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uebergeordneter Service. Alle Services sollten von diesem Service erben. Wird verwendet um Interceptors einzuschalten
 */
public abstract class AbstractBaseService {

	@Inject
	private Persistence persistence;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	private static final Logger LOG = LoggerFactory.getLogger(AbstractBaseService.class.getSimpleName());

	@SuppressWarnings("PMD.CloseResource")
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void updateLuceneIndex(Class<? extends AbstractEntity> clazz, String id) {
		// Den Lucene-Index manuell nachführen, da es bei unidirektionalen Relationen nicht automatisch geschieht!
		Session session = persistence.getEntityManager().unwrap(Session.class);
		FullTextSession fullTextSession = Search.getFullTextSession(session);
		// Den Index loeschen...
		fullTextSession.purge(clazz, id);
		// ... und neu erstellen
		Object customer = fullTextSession.load(clazz, id);
		fullTextSession.index(customer);
	}

	public void updateGueltigFlagOnPlatzAndVorgaenger(@Nonnull AbstractPlatz platz) {
		// Gueltigkeit auf dem neuen setzen, auf der bisherigen entfernen
		platz.setGueltig(true);
		Optional<AbstractPlatz> vorgaengerPlatzOptional = findVorgaengerPlatz(platz);
		if (vorgaengerPlatzOptional.isPresent()) {
			AbstractPlatz vorgaengerPlatz = vorgaengerPlatzOptional.get();
			vorgaengerPlatz.setGueltig(false);
			// falls der Vorgänger im Status SCHULAMT_MUTATION_IGNORIERT ist
			// müssen wir weiter zurück, weil die letzte gültige Betreuung dann möglicherweise
			// älter als die Vormutation ist
			updateVorgaengerPlatzOfSchulamtIgnoriert(vorgaengerPlatz);
		}
	}

	/**
	Setzt rekursiv die Vorgängerplätze von plätzen im Status SCHULAMT_MUTATION_IGNORIERT
	zurück
	 */
	private void updateVorgaengerPlatzOfSchulamtIgnoriert(@Nonnull AbstractPlatz platz) {
		if (platz.getBetreuungsstatus() != Betreuungsstatus.SCHULAMT_MUTATION_IGNORIERT) {
			return;
		}
		Optional<AbstractPlatz> vorgaengerPlatzOptional = findVorgaengerPlatz(platz);
		if (vorgaengerPlatzOptional.isEmpty()) {
			return;
		}
		AbstractPlatz vorgaengerPlatz = vorgaengerPlatzOptional.get();
		vorgaengerPlatz.setGueltig(false);
		updateVorgaengerPlatzOfSchulamtIgnoriert(vorgaengerPlatz);
	}

	/**
	 * @return gibt die Betreuung/Anmeldunbg der vorherigen verfuegten Betreuung zurueck.
	 */
	@Nonnull
	protected Optional<AbstractPlatz> findVorgaengerPlatz(@Nonnull AbstractPlatz abstractPlatz) {
		Objects.requireNonNull(abstractPlatz, "abstractPlatz darf nicht null sein");
		if (abstractPlatz.getVorgaengerId() == null) {
			return Optional.empty();
		}

		// Achtung, hier wird persistence.find() verwendet, da ich fuer das Vorgaengergesuch evt. nicht
		// Leseberechtigt bin, fuer die Mutation aber schon!
		AbstractPlatz vorgaengerPlatz = persistence.find(abstractPlatz.getClass(), abstractPlatz.getVorgaengerId());
		if (vorgaengerPlatz != null) {
			if (vorgaengerPlatz.getBetreuungsstatus() != Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG &&
				vorgaengerPlatz.getBetreuungsstatus() != Betreuungsstatus.SCHULAMT_MUTATION_IGNORIERT) {
				// Hier kann aus demselben Grund die Berechtigung fuer die Vorgaengerverfuegung nicht geprueft werden
				return Optional.of(vorgaengerPlatz);
			}
			return findVorgaengerPlatz(vorgaengerPlatz);
		}
		return Optional.empty();
	}

	protected void logExceptionAccordingToEnvironment(
		@Nonnull Exception e,
		@Nonnull String message,
		@Nonnull String arg) {
		if (ebeguConfiguration.getIsDevmode()) {
			LOG.info("{} {}", message, arg, e);
		} else {
			LOG.error("{} {}", message, arg, e);
		}
	}
}
