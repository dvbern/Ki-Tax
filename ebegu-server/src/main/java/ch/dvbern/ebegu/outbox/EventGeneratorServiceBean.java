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
 */

package ch.dvbern.ebegu.outbox;

import ch.dvbern.ebegu.config.EbeguConfiguration;
import ch.dvbern.ebegu.entities.*;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen;
import ch.dvbern.ebegu.entities.gemeindeantrag.gemeindekennzahlen.GemeindeKennzahlen_;
import ch.dvbern.ebegu.enums.*;
import ch.dvbern.ebegu.enums.betreuung.BetreuungsangebotTyp;
import ch.dvbern.ebegu.enums.betreuung.Betreuungsstatus;
import ch.dvbern.ebegu.outbox.gemeinde.GemeindeEventConverter;
import ch.dvbern.ebegu.outbox.gemeindekennzahlen.GemeindeKennzahlenEventConverter;
import ch.dvbern.ebegu.outbox.institution.InstitutionEventConverter;
import ch.dvbern.ebegu.outbox.institution.InstitutionEventUtil;
import ch.dvbern.ebegu.outbox.platzbestaetigung.BetreuungAnfrageEventConverter;
import ch.dvbern.ebegu.outbox.verfuegung.VerfuegungEventAsyncHelper;
import ch.dvbern.ebegu.services.ApplicationPropertyService;
import ch.dvbern.ebegu.services.EinstellungService;
import ch.dvbern.lib.cdipersistence.Persistence;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.dvbern.ebegu.services.util.PredicateHelper.NEW;

@Stateless
public class EventGeneratorServiceBean {
	@Inject
	private Persistence persistence;
	@Inject
	private Event<ExportedEvent> event;
	@Inject
	private InstitutionEventConverter institutionEventConverter;
	@Inject
	private GemeindeKennzahlenEventConverter gemeindeKennzahlenEventConverter;
	@Inject
	private GemeindeEventConverter gemeindeEventConverter;
	@Inject
	private BetreuungAnfrageEventConverter betreuungAnfrageEventConverter;
	@Inject
	private VerfuegungEventAsyncHelper asyncHelper;
	@Inject
	private EinstellungService einstellungService;
	@Inject
	private EbeguConfiguration ebeguConfiguration;
	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void exportInstitutionEvent() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<InstitutionStammdaten> query = cb.createQuery(InstitutionStammdaten.class);
		Root<InstitutionStammdaten> root = query.from(InstitutionStammdaten.class);

		Join<InstitutionStammdaten, Institution> institutionJoin = root.join(InstitutionStammdaten_.institution);

		Predicate isNotPublished = cb.isFalse(institutionJoin.get(Institution_.eventPublished));
		var statusParam = cb.parameter(InstitutionStatus.class, Institution_.STATUS);
		Predicate notLatsStatus = cb.notEqual(institutionJoin.get(Institution_.status), statusParam);

		query.where(isNotPublished, notLatsStatus);

		List<InstitutionStammdaten> institutions = persistence.getEntityManager().createQuery(query)
			.setParameter(statusParam, InstitutionStatus.NUR_LATS)
			.getResultList();

		institutions.stream()
			.filter(InstitutionEventUtil::isExportable)
			.forEach(stammdaten -> {
				event.fire(institutionEventConverter.of(stammdaten));
				Institution institution = stammdaten.getInstitution();
				institution.setSkipPreUpdate(true);
				institution.setEventPublished(true);
				persistence.merge(institution);
			});
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void exportGemeindeKennzahlenEvent() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<GemeindeKennzahlen> query = cb.createQuery(GemeindeKennzahlen.class);
		Root<GemeindeKennzahlen> root = query.from(GemeindeKennzahlen.class);

		Predicate isNotPublished = cb.isFalse(root.get(GemeindeKennzahlen_.eventPublished));

		query.where(isNotPublished);

		List<GemeindeKennzahlen> gemeinden = persistence.getEntityManager().createQuery(query)
			.getResultList();

		gemeinden.forEach(gemeindeKennzahlen -> {
			Map<EinstellungKey, Einstellung> gemeindeKonfigurationMap = einstellungService
				.getGemeindeEinstellungenOnlyAsMap(
					gemeindeKennzahlen.getGemeinde(),
					gemeindeKennzahlen.getGesuchsperiode());

			Einstellung einstellungBgAusstellenBisStufe =
				gemeindeKonfigurationMap.get(EinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE);
			EinschulungTyp bgAusstellenBisUndMitStufe =
				EinschulungTyp.valueOf(einstellungBgAusstellenBisStufe.getValue());

			Einstellung einstellungErwerbspensumZuschlag =
				gemeindeKonfigurationMap.get(EinstellungKey.ERWERBSPENSUM_ZUSCHLAG);

			event.fire(gemeindeKennzahlenEventConverter.of(
				gemeindeKennzahlen,
				bgAusstellenBisUndMitStufe,
				einstellungErwerbspensumZuschlag.getValueAsBigDecimal()));
			gemeindeKennzahlen.setEventPublished(true);
			persistence.merge(gemeindeKennzahlen);
		});
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void exportGemeindeEvent() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Gemeinde> query = cb.createQuery(Gemeinde.class);
		Root<Gemeinde> root = query.from(Gemeinde.class);

		Predicate isNotPublished = cb.isFalse(root.get(Gemeinde_.eventPublished));

		query.where(isNotPublished);

		List<Gemeinde> gemeinden = persistence.getEntityManager().createQuery(query)
			.getResultList();

		gemeinden.forEach(gemeinde -> {
			event.fire(gemeindeEventConverter.of(gemeinde));
			gemeinde.setEventPublished(true);
			persistence.merge(gemeinde);
		});
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void exportBetreuungAnfrageEvent() {
		if (!ebeguConfiguration.isBetreuungAnfrageApiEnabled()) {
			return;
		}

		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<Betreuung> query = cb.createQuery(Betreuung.class);
		Root<Betreuung> root = query.from(Betreuung.class);
		List<Predicate> predicates = new ArrayList<>();

		//Institution Stammdaten Join and check angebot Typ, muss Kita oder TFO sein
		Join<Betreuung, InstitutionStammdaten> institutionStammdatenJoin =
			root.join(Betreuung_.institutionStammdaten);
		Predicate isBetreuungsgutscheinTyp =
			institutionStammdatenJoin.get(InstitutionStammdaten_.betreuungsangebotTyp)
				.in(BetreuungsangebotTyp.getBetreuungsgutscheinTypes());
		predicates.add(isBetreuungsgutscheinTyp);

		//Event muss noch nicht plubliziert sein
		Predicate isNotPublished = cb.isFalse(root.get(Betreuung_.eventPublished));
		predicates.add(isNotPublished);

		//Status muss warten sein
		Predicate statusWarten = cb.equal(root.get(AbstractPlatz_.betreuungsstatus), Betreuungsstatus.WARTEN);
		predicates.add(statusWarten);

		query.where(predicates.toArray(NEW));

		List<Betreuung> betreuungs = persistence.getEntityManager().createQuery(query)
			.getResultList();

		betreuungs.stream()
			.filter(betreuung -> applicationPropertyService.isPublishSchnittstelleEventsAktiviert(betreuung.extractGesuch().extractMandant()))
			.forEach(betreuung -> {
				event.fire(betreuungAnfrageEventConverter.of(betreuung));
				betreuung.setEventPublished(true);
				persistence.merge(betreuung);
			});
	}


	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void exportVerfuegungEvent() {
		CriteriaBuilder cb = persistence.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);
		Root<Verfuegung> root = query.from(Verfuegung.class);
		Path<Betreuung> betreuungPath = root.get(Verfuegung_.betreuung);

		ParameterExpression<Betreuungsstatus> statusParam = cb.parameter(Betreuungsstatus.class);
		Predicate isVerfuegt = cb.equal(betreuungPath.get(AbstractPlatz_.betreuungsstatus), statusParam);

		Predicate isNotPublished = cb.isFalse(root.get(Verfuegung_.eventPublished));

		query.where(isNotPublished, isVerfuegt);
		query.select(root.get(AbstractEntity_.ID));

		List<String> verfuegungen = persistence.getEntityManager().createQuery(query)
			.setParameter(statusParam, Betreuungsstatus.VERFUEGT)
			.getResultList();

		verfuegungen.forEach(v -> asyncHelper.convert(v));
	}
}
