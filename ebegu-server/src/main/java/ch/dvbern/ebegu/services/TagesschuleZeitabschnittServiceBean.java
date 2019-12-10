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

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.AnmeldungTagesschule;
import ch.dvbern.ebegu.entities.AnmeldungTagesschuleZeitabschnitt;
import ch.dvbern.ebegu.entities.BelegungTagesschuleModul;
import ch.dvbern.ebegu.entities.Betreuung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.entities.ModulTagesschule;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.ApplicationPropertyKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.rechner.BGRechnerParameterDTO;
import ch.dvbern.ebegu.rechner.TagesschuleRechner;
import ch.dvbern.ebegu.rechner.TagesschuleRechnerParameterDTO;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinEvaluator;
import ch.dvbern.ebegu.rules.Rule;
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

		Verfuegung verfuegungMitFamiliensituation = evaluateFamiliensituationForTagesschuleAnmeldung(gesuch, gemeinde
			, gesuchsperiode);

		//TODO Add the new parameters to the Gesuchperiode GUI + DB or other story
		TagesschuleRechnerParameterDTO parameterDTO = loadTagesschuleRechnerParameters(gemeinde, gesuchsperiode);

		AnmeldungTagesschule anmeldungTagesschule =
			betreuungService.findAnmeldungTagesschule(anmeldungTagesschuleId).orElseThrow(() -> new EbeguEntityNotFoundException(
				"generateAndPersistZeitabschnitte",
				ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				anmeldungTagesschuleId));

		calculateStundenUndVerpflegungKostenProWoche(anmeldungTagesschule, parameterDTO);

		List<AnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabschnitts =
			calculateTagesschuleZeitabschnitte(verfuegungMitFamiliensituation, parameterDTO);


		return anmeldungTagesschuleZeitabschnitts;
	}

	private  List<AnmeldungTagesschuleZeitabschnitt> calculateTagesschuleZeitabschnitte(@Nonnull Verfuegung verfuegungMitFamiliensituation,
		@Nonnull TagesschuleRechnerParameterDTO parameterDTO){

		TagesschuleRechner tagesschuleRechner = new TagesschuleRechner();

		List<AnmeldungTagesschuleZeitabschnitt> anmeldungTagesschuleZeitabschnitts = new ArrayList<>();

		for(VerfuegungZeitabschnitt verfuegungZeitabschnitt: verfuegungMitFamiliensituation.getZeitabschnitte()){
			AnmeldungTagesschuleZeitabschnitt anmeldungTagesschuleZeitabschnitt =
				new AnmeldungTagesschuleZeitabschnitt();
			anmeldungTagesschuleZeitabschnitt.setMassgebendesEinkommenInklAbzugFamgr(verfuegungZeitabschnitt.getMassgebendesEinkommen());
			BigDecimal tarif = tagesschuleRechner.calculateTarif(verfuegungZeitabschnitt, parameterDTO, true);
			//TODO fill the anmeldungTagesschuleZeitabschnitt Object with the value and calculate the total kosten
		}

		return anmeldungTagesschuleZeitabschnitts;
	}

	private void calculateStundenUndVerpflegungKostenProWoche(AnmeldungTagesschule anmeldungTagesschule,
		@Nonnull TagesschuleRechnerParameterDTO parameterDTO){
		assert anmeldungTagesschule.getBelegungTagesschule() != null;
		int stundenProWocheMitBetreuung = 0;
		int minutesProWocheMitBetreuung  = 0;
		BigDecimal verpflegKostenProWocheMitBetreuung  = new BigDecimal("0.0");
		int stundenProWocheOhneBetreuung = 0;
		int minutesProWocheOhneBetreuung = 0;
		BigDecimal verpflegKostenProWocheOhneBetreuung = new BigDecimal("0.0");
		for (BelegungTagesschuleModul belegungTagesschuleModul :
			anmeldungTagesschule.getBelegungTagesschule().getBelegungTagesschuleModule()) {
			ModulTagesschule modulTagesschule = belegungTagesschuleModul.getModulTagesschule();
			int hours = modulTagesschule.getModulTagesschuleGroup().getZeitVon().getHour();
			int minutes = modulTagesschule.getModulTagesschuleGroup().getZeitVon().getMinute();
			LocalTime zeitBis = modulTagesschule.getModulTagesschuleGroup().getZeitBis();
			zeitBis = zeitBis.minusHours(hours);
			zeitBis = zeitBis.minusMinutes(minutes);
			if(modulTagesschule.getModulTagesschuleGroup().isWirdPaedagogischBetreut()){
				stundenProWocheMitBetreuung += zeitBis.getHour();
				minutesProWocheMitBetreuung += zeitBis.getMinute();
				if (modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten() != null) {
					verpflegKostenProWocheMitBetreuung =
						verpflegKostenProWocheMitBetreuung.add(modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten());
				}
			}
			else{
				stundenProWocheOhneBetreuung += zeitBis.getHour();
				minutesProWocheOhneBetreuung += zeitBis.getMinute();
				if (modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten() != null) {
					verpflegKostenProWocheOhneBetreuung =
						verpflegKostenProWocheOhneBetreuung.add(modulTagesschule.getModulTagesschuleGroup().getVerpflegungskosten());
				}
			}
		}

		Double additionalHours = minutesProWocheMitBetreuung / 60.0;
		if (additionalHours >= 1.0) {
			int hoursToAdd = additionalHours.intValue();
			stundenProWocheMitBetreuung += hoursToAdd;
			minutesProWocheMitBetreuung -= hoursToAdd * 60;
		}
		additionalHours = minutesProWocheOhneBetreuung / 60.0;
		if (additionalHours >= 1.0) {
			int hoursToAdd = additionalHours.intValue();
			stundenProWocheOhneBetreuung += hoursToAdd;
			minutesProWocheOhneBetreuung -= hoursToAdd * 60;
		}

		parameterDTO.setStundenProWocheMitBetreuung(stundenProWocheMitBetreuung);
		parameterDTO.setMinutesProWocheMitBetreuung(minutesProWocheMitBetreuung);
		parameterDTO.setVerpflegKostenProWocheMitBetreuung(verpflegKostenProWocheMitBetreuung);
		parameterDTO.setStundenProWocheOhneBetreuung(stundenProWocheOhneBetreuung);
		parameterDTO.setMinutesProWocheOhneBetreuung(minutesProWocheOhneBetreuung);
		parameterDTO.setVerpflegKostenProWocheOhneBetreuung(verpflegKostenProWocheOhneBetreuung);
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
