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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.AbstractPlatz;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.Betreuungsstatus;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;
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
	private EinstellungService einstellungService;

	@Inject
	private EbeguConfiguration ebeguConfiguration;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	private static final Logger LOG = LoggerFactory.getLogger(AbstractBaseService.class.getSimpleName());

	@PermitAll
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

	@PermitAll
	@Nonnull
	public BGRechnerParameterDTO loadCalculatorParameters(@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode) {
		Map<EinstellungKey, Einstellung> paramMap = einstellungService.getAllEinstellungenByGemeindeAsMap(gemeinde, gesuchsperiode);
		return new BGRechnerParameterDTO(paramMap, gesuchsperiode, gemeinde);
	}

	@PermitAll
	@Nonnull
	public KitaxUebergangsloesungParameter loadKitaxUebergangsloesungParameter() {
		KitaxUebergangsloesungParameter parameter = new KitaxUebergangsloesungParameter(
			applicationPropertyService.getStadtBernAsivStartDatum(),
			applicationPropertyService.isStadtBernAsivConfigured()
		);
		return parameter;
	}

	protected void updateGueltigFlagOnPlatzAndVorgaenger(@Nonnull AbstractPlatz platz) {
		// Gueltigkeit auf dem neuen setzen, auf der bisherigen entfernen
		platz.setGueltig(true);
		Optional<Verfuegung> vorgaengerVerfuegungOptional = findVorgaengerVerfuegung(platz);
		if (vorgaengerVerfuegungOptional.isPresent()) {
			Verfuegung vorgaengerVerfuegung = vorgaengerVerfuegungOptional.get();
			Objects.requireNonNull(vorgaengerVerfuegung.getPlatz());
			vorgaengerVerfuegung.getPlatz().setGueltig(false);
		}
	}

	/**
	 * @return gibt die Verfuegung der vorherigen verfuegten Betreuung zurueck.
	 */
	@Nonnull
	protected Optional<Verfuegung> findVorgaengerVerfuegung(@Nonnull AbstractPlatz betreuung) {
		Objects.requireNonNull(betreuung, "betreuung darf nicht null sein");
		if (betreuung.getVorgaengerId() == null) {
			return Optional.empty();
		}

		// Achtung, hier wird persistence.find() verwendet, da ich fuer das Vorgaengergesuch evt. nicht
		// Leseberechtigt bin, fuer die Mutation aber schon!
		AbstractPlatz vorgaengerbetreuung = persistence.find(betreuung.getClass(), betreuung.getVorgaengerId());
		if (vorgaengerbetreuung != null) {
			if (vorgaengerbetreuung.getBetreuungsstatus() != Betreuungsstatus.GESCHLOSSEN_OHNE_VERFUEGUNG) {
				// Hier kann aus demselben Grund die Berechtigung fuer die Vorgaengerverfuegung nicht geprueft werden
				return Optional.ofNullable(vorgaengerbetreuung.getVerfuegung());
			}
			return findVorgaengerVerfuegung(vorgaengerbetreuung);
		}
		return Optional.empty();
	}

	protected void logExceptionAccordingToEnvironment(@Nonnull Exception e, @Nonnull String message, @Nonnull String arg) {
		if (ebeguConfiguration.getIsDevmode()) {
			LOG.info("{} {}", message, arg, e);
		} else {
			LOG.error("{} {}", message, arg, e);
		}
	}
}
