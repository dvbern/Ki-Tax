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

import javax.annotation.Nonnull;
import javax.annotation.security.PermitAll;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.AbstractEntity;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
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
		BGRechnerParameterDTO parameterDTO = new BGRechnerParameterDTO(paramMap, gesuchsperiode, gemeinde);
		return parameterDTO;
	}

	/**
	 * Hack, welcher das Gesuch detached, damit es auf keinen Fall gespeichert wird. Vorher muessen die Lazy geloadeten
	 * BetreuungspensumContainers geladen werden, da danach keine Session mehr zur Verfuegung steht!
	 */
	@PermitAll // Gibt nichts zurueck
	public void loadRelationsAndDetach(Gesuch gesuch) {
		for (Betreuung betreuung : gesuch.extractAllBetreuungen()) {
			betreuung.getBetreuungspensumContainers().size();
			betreuung.getAbwesenheitContainers().size();
		}
		if (gesuch.getGesuchsteller1() != null) {
			gesuch.getGesuchsteller1().getAdressen().size();
			gesuch.getGesuchsteller1().getErwerbspensenContainers().size();
		}
		if (gesuch.getGesuchsteller2() != null) {
			gesuch.getGesuchsteller2().getAdressen().size();
			gesuch.getGesuchsteller2().getErwerbspensenContainers().size();
		}
		gesuch.extractAllBetreuungen().forEach(betreuung -> {
			if (betreuung.getBelegungTagesschule() != null && betreuung.getBelegungTagesschule().getModuleTagesschule() != null) {
				betreuung.getBelegungTagesschule().getModuleTagesschule().size();
			}
			if (betreuung.getBelegungFerieninsel() != null) {
				betreuung.getBelegungFerieninsel().getTage().size();
			}
		});
		persistence.getEntityManager().detach(gesuch);
	}

	protected void logExceptionAccordingToEnvironment(@Nonnull Exception e, @Nonnull String message, @Nonnull String arg) {
		if (ebeguConfiguration.getIsDevmode()) {
			LOG.info("{} {}", message, arg, e);
		} else {
			LOG.error("{} {}", message, arg, e);
		}
	}
}
