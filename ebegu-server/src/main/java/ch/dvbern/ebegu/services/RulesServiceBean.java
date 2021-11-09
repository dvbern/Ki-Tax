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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Gemeinde;
import ch.dvbern.ebegu.entities.Gesuchsperiode;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.rules.BetreuungsgutscheinConfigurator;
import ch.dvbern.ebegu.rules.Rule;
import ch.dvbern.ebegu.util.KitaxUebergangsloesungParameter;

/**
 * Services fuer Rules
 */
@Stateless
@Local(RulesService.class)
public class RulesServiceBean extends AbstractBaseService implements RulesService {

	@Inject
	private EinstellungService einstellungService;

	/**
	 * Diese Methode initialisiert den Calculator mit den richtigen Parametern und benotigten Regeln fuer den Mandanten der
	 * gebraucht wird
	 */
	@Override
	public List<Rule> getRulesForGesuchsperiode(
		@Nonnull Gemeinde gemeinde,
		@Nonnull Gesuchsperiode gesuchsperiode,
		@Nonnull KitaxUebergangsloesungParameter kitaxParameterDTO,
		@Nonnull Locale locale
	) {
		BetreuungsgutscheinConfigurator ruleConfigurator = new BetreuungsgutscheinConfigurator();
		Set<EinstellungKey> keysToLoad = ruleConfigurator.getRequiredParametersForGemeinde();
		Map<EinstellungKey, Einstellung> einstellungen = einstellungService.loadRuleParameters(gemeinde, gesuchsperiode, keysToLoad);
		return ruleConfigurator.configureRulesForMandant(gemeinde, einstellungen, kitaxParameterDTO, locale);
	}
}
