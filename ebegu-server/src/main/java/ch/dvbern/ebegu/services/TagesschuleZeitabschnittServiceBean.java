/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschuleZeitabschnitt;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.rechner.TagesschuleRechnerParameterDTO;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator;
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.services.util.TagesschuleBerechnungHelper;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.lib.cdipersistence.Persistence;

import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.ADMIN_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.GESUCHSTELLER;
import static ch.dvbern.ebegu.enums.UserRoleName.JURIST;
import static ch.dvbern.ebegu.enums.UserRoleName.REVISOR;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_BG;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_GEMEINDE;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_INSTITUTION;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_MANDANT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TRAEGERSCHAFT;
import static ch.dvbern.ebegu.enums.UserRoleName.SACHBEARBEITER_TS;
import static ch.dvbern.ebegu.enums.UserRoleName.STEUERAMT;
import static ch.dvbern.ebegu.enums.UserRoleName.SUPER_ADMIN;

/**
 * Service zum berechnen und speichern die Tagesschule Zeitabschnitte
 */
@Stateless
@Local(TagesschuleZeitabschnittService.class)
@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR,
	ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, GESUCHSTELLER,
	STEUERAMT, ADMIN_MANDANT, SACHBEARBEITER_MANDANT, ADMIN_TS, SACHBEARBEITER_TS })
public class TagesschuleZeitabschnittServiceBean extends AbstractBaseService implements TagesschuleZeitabschnittService{

	@Inject
	private Persistence persistence;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private BetreuungService betreuungService;

	@Inject
	private FinanzielleSituationService finanzielleSituationService;

	@Inject
	private RulesService rulesService;

	@Inject
	private GemeindeService gemeindeService;

	@Inject
	private ApplicationPropertyService applicationPropertyService;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_TS, SACHBEARBEITER_TS, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE })
	public List<AnmeldungTagesschuleZeitabschnitt> generateAndPersistZeitabschnitte(@Nonnull String gesuchId,
		@Nonnull String anmeldungTagesschuleId) {
		Gesuch gesuch = gesuchService.findGesuch(gesuchId)
			.orElseThrow(() -> new EbeguEntityNotFoundException("calculateAndExtractVerfuegung", gesuchId));
		Gemeinde gemeinde = gesuch.extractGemeinde();
		Gesuchsperiode gesuchsperiode = gesuch.getGesuchsperiode();

		AnmeldungTagesschule anmeldungTagesschule =
			betreuungService.findAnmeldungTagesschule(anmeldungTagesschuleId).orElseThrow(() -> new EbeguEntityNotFoundException(
				"generateAndPersistZeitabschnitte",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				anmeldungTagesschuleId));

		List<AnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabschnitts = generateZeitabschnitte(gesuch,
			gemeinde, gesuchsperiode, anmeldungTagesschule);

		persistence.persist(anmeldungTagesschuleZeitabschnitts);
		persistence.merge(anmeldungTagesschule);

		return anmeldungTagesschuleZeitabschnitts;
	}

	@Nonnull
	private List<AnmeldungTagesschuleZeitabschnitt> generateZeitabschnitte(@Nonnull Gesuch gesuch,
		@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull AnmeldungTagesschule anmeldungTagesschule){

		Verfuegung verfuegungMitFamiliensituation = evaluateFamiliensituationForTagesschuleAnmeldung(gesuch, gemeinde
			, gesuchsperiode);

		TagesschuleRechnerParameterDTO parameterDTO = loadTagesschuleRechnerParameters(gemeinde, gesuchsperiode);

		List<AnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabschnitts =
			TagesschuleBerechnungHelper.calculateZeitabschnitte(anmeldungTagesschule, parameterDTO, verfuegungMitFamiliensituation);

		Set<AnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabscnittsSet = new TreeSet<>();
		anmeldungTagesschuleZeitabscnittsSet.addAll(anmeldungTagesschuleZeitabschnitts);
		anmeldungTagesschule.setAnmeldungTagesschuleZeitabschnitts(anmeldungTagesschuleZeitabscnittsSet);

		return anmeldungTagesschuleZeitabschnitts;
	}


	private Verfuegung evaluateFamiliensituationForTagesschuleAnmeldung(@Nonnull Gesuch gesuch,
		@Nonnull Gemeinde gemeinde, @Nonnull Gesuchsperiode gesuchsperiode){

		finanzielleSituationService.calculateFinanzDaten(gesuch);

		Sprache sprache = EbeguUtil.extractKorrespondenzsprache(gesuch, gemeindeService);

		List<Rule> rules = rulesService.getRulesForGesuchsperiode(gemeinde, gesuchsperiode, sprache.getLocale());

		Boolean enableDebugOutput = applicationPropertyService.findApplicationPropertyAsBoolean(
			ApplicationPropertyKey.EVALUATOR_DEBUG_ENABLED, true);

		BetreuungsgutscheinEvaluator bgEvaluator = new BetreuungsgutscheinEvaluator(rules, enableDebugOutput);

		return bgEvaluator.evaluateFamiliensituation(gesuch, sprache.getLocale(), false);
	}
}
