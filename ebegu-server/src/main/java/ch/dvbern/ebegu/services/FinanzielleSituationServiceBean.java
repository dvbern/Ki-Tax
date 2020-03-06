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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;
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
 * Service fuer FinanzielleSituation
 */
@Stateless
@Local(FinanzielleSituationService.class)
@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, GESUCHSTELLER, STEUERAMT,
	SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
public class FinanzielleSituationServiceBean extends AbstractBaseService implements FinanzielleSituationService {

	@Inject
	private Persistence persistence;

	@Inject
	private CriteriaQueryHelper criteriaQueryHelper;

	@Inject
	private FinanzielleSituationRechner finSitRechner;

	@Inject
	private WizardStepService wizardStepService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private GesuchService gesuchService;

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS })
	public Gesuch saveFinanzielleSituationStart(
		@Nonnull FinanzielleSituationContainer finanzielleSituation,
		@Nonnull Boolean sozialhilfebezueger,
		@Nonnull Boolean gemeinsameSteuererklaerung,
		@Nonnull Boolean verguenstigungGewuenscht,
		@Nonnull String gesuchId
	) {
		// Die eigentliche FinSit speichern
		FinanzielleSituationContainer finanzielleSituationPersisted = persistence.merge(finanzielleSituation);

		// Die zwei Felder "sozialhilfebezueger" und "gemeinsameSteuererklaerung" befinden sich nicht auf der FinanziellenSituation, sondern auf der
		// FamilienSituation -> Das Gesuch muss hier aus der DB geladen werden, damit nichts überschrieben wird!
		Gesuch gesuch = saveFinanzielleSituationFelderAufGesuch(
			sozialhilfebezueger,
			gemeinsameSteuererklaerung,
			verguenstigungGewuenscht,
			gesuchId
		);

		wizardStepService.updateSteps(
			gesuchId,
			null,
			finanzielleSituationPersisted.getFinanzielleSituationJA(),
			WizardStepName.FINANZIELLE_SITUATION,
			1); // it must be substep 1 since it is finanzielleSituationStart

		return gesuch;
	}

	private Gesuch saveFinanzielleSituationFelderAufGesuch(
		@Nonnull Boolean sozialhilfebezueger,
		@Nonnull Boolean gemeinsameSteuererklaerung,
		@Nonnull Boolean verguenstigungGewuenscht,
		@Nonnull String gesuchId
	) {
		Gesuch gesuch = gesuchService.findGesuch(gesuchId).orElseThrow(() -> new EbeguEntityNotFoundException("saveFinanzielleSituation", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId invalid: " + gesuchId));
		FamiliensituationContainer familiensituationContainer = gesuch.getFamiliensituationContainer();
		Objects.requireNonNull(familiensituationContainer);
		Familiensituation familiensituation = familiensituationContainer.getFamiliensituationJA();
		Objects.requireNonNull(familiensituation);
		familiensituation.setSozialhilfeBezueger(sozialhilfebezueger);
		familiensituation.setGemeinsameSteuererklaerung(gemeinsameSteuererklaerung);
		familiensituation.setVerguenstigungGewuenscht(verguenstigungGewuenscht);
		// Der FinSit-Status wird automatisch auf TRUE gesetzt, wenn der Benutzer keine FinSit angeben muss
		boolean finSitRequired = EbeguUtil.isNullOrFalse(sozialhilfebezueger) && EbeguUtil.isNotNullAndTrue(verguenstigungGewuenscht);
		if (gesuch.getFinSitStatus() == null && !finSitRequired) {
			gesuch.setFinSitStatus(FinSitStatus.AKZEPTIERT);
		}
		return gesuch;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, GESUCHSTELLER, SACHBEARBEITER_TS, ADMIN_TS })
	public FinanzielleSituationContainer saveFinanzielleSituation(
		@Nonnull FinanzielleSituationContainer finanzielleSituation,
		@Nonnull String gesuchId
	) {
		// Die eigentliche FinSit speichern
		FinanzielleSituationContainer finanzielleSituationPersisted = persistence.merge(finanzielleSituation);
		wizardStepService.updateSteps(gesuchId, null, finanzielleSituationPersisted.getFinanzielleSituationJA(), WizardStepName
			.FINANZIELLE_SITUATION);
		return finanzielleSituationPersisted;
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, GESUCHSTELLER, STEUERAMT,
		ADMIN_TS, SACHBEARBEITER_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Optional<FinanzielleSituationContainer> findFinanzielleSituation(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		FinanzielleSituationContainer finanzielleSituation = persistence.find(FinanzielleSituationContainer.class, id);
		authorizer.checkReadAuthorization(finanzielleSituation);
		return Optional.ofNullable(finanzielleSituation);
	}

	@Nonnull
	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, GESUCHSTELLER, STEUERAMT,
		SACHBEARBEITER_TS, ADMIN_TS, ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public Collection<FinanzielleSituationContainer> getAllFinanzielleSituationen() {
		Collection<FinanzielleSituationContainer> finanzielleSituationen = criteriaQueryHelper.getAll(FinanzielleSituationContainer.class);
		authorizer.checkReadAuthorization(finanzielleSituationen);
		return new ArrayList<>(finanzielleSituationen);
	}

	@Override
	@Nonnull
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, GESUCHSTELLER, STEUERAMT,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_TS, SACHBEARBEITER_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public FinanzielleSituationResultateDTO calculateResultate(@Nonnull Gesuch gesuch) {
		return finSitRechner.calculateResultateFinanzielleSituation(gesuch, true);
	}

	@Override
	@RolesAllowed({ SUPER_ADMIN, ADMIN_BG, SACHBEARBEITER_BG, ADMIN_GEMEINDE, SACHBEARBEITER_GEMEINDE, JURIST, REVISOR, GESUCHSTELLER, STEUERAMT,
		ADMIN_INSTITUTION, SACHBEARBEITER_INSTITUTION, ADMIN_TRAEGERSCHAFT, SACHBEARBEITER_TRAEGERSCHAFT, ADMIN_TS, SACHBEARBEITER_TS,
		ADMIN_MANDANT, SACHBEARBEITER_MANDANT })
	public void calculateFinanzDaten(@Nonnull Gesuch gesuch) {
		final BigDecimal minimumEKV = calculateGrenzwertEKV(gesuch);
		finSitRechner.calculateFinanzDaten(gesuch, minimumEKV);
	}

	/**
	 * Es wird nach dem Param PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG gesucht, der einen Wert von 0 bis 100 haben muss.
	 * Sollte der Parameter nicht definiert sein, wird 0 zurueckgegeben, d.h. keine Grenze fuer EKV
	 */
	private BigDecimal calculateGrenzwertEKV(@Nonnull Gesuch gesuch) {
		Einstellung einstellung = einstellungService.findEinstellung(
			EinstellungKey.PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG, gesuch.extractGemeinde(), gesuch.getGesuchsperiode());
		if (einstellung.getValueAsBigDecimal() != null) {
			return einstellung.getValueAsBigDecimal();
		}
		return BigDecimal.ZERO;
	}
}
