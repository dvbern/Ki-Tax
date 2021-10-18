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
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.entities.Einkommensverschlechterung;
import ch.dvbern.ebegu.entities.EinkommensverschlechterungContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.GesuchstellerContainer;
import ch.dvbern.ebegu.entities.Verfuegung;
import ch.dvbern.ebegu.entities.VerfuegungZeitabschnitt;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.Sprache;
import ch.dvbern.ebegu.enums.WizardStepName;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.persistence.CriteriaQueryHelper;
import ch.dvbern.ebegu.util.FinanzielleSituationRechner;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.cdipersistence.Persistence;

/**
 * Service fuer Einkommensverschlechterung
 */
@Stateless
@Local(EinkommensverschlechterungService.class)
public class EinkommensverschlechterungServiceBean extends AbstractBaseService implements EinkommensverschlechterungService {

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
	private VerfuegungService verfuegungService;

	@Override
	@Nonnull
	public EinkommensverschlechterungContainer saveEinkommensverschlechterungContainer(
		@Nonnull EinkommensverschlechterungContainer einkommensverschlechterungContainer, String gesuchId) {
		Objects.requireNonNull(einkommensverschlechterungContainer);
		final EinkommensverschlechterungContainer persistedEKV = persistence.merge(einkommensverschlechterungContainer);
		if (gesuchId != null) {
			wizardStepService.updateSteps(gesuchId, null, einkommensverschlechterungContainer, WizardStepName.EINKOMMENSVERSCHLECHTERUNG);
		}
		return persistedEKV;
	}

	@Override
	@Nonnull
	public Optional<EinkommensverschlechterungContainer> findEinkommensverschlechterungContainer(@Nonnull String key) {
		Objects.requireNonNull(key, "id muss gesetzt sein");
		EinkommensverschlechterungContainer a = persistence.find(EinkommensverschlechterungContainer.class, key);
		return Optional.ofNullable(a);
	}

	@Override
	@Nonnull
	public Collection<EinkommensverschlechterungContainer> getAllEinkommensverschlechterungContainer() {
		return new ArrayList<>(criteriaQueryHelper.getAll(EinkommensverschlechterungContainer.class));
	}

	@Override
	public void removeEinkommensverschlechterungContainer(@Nonnull EinkommensverschlechterungContainer einkommensverschlechterungContainer) {
		Objects.requireNonNull(einkommensverschlechterungContainer);
		einkommensverschlechterungContainer.getGesuchsteller().setEinkommensverschlechterungContainer(null);
		persistence.merge(einkommensverschlechterungContainer.getGesuchsteller());

		EinkommensverschlechterungContainer entityToRemove = findEinkommensverschlechterungContainer(einkommensverschlechterungContainer.getId())
			.orElseThrow(() -> new EbeguEntityNotFoundException("removeEinkommensverschlechterungContainer", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND,
				einkommensverschlechterungContainer));
		persistence.remove(EinkommensverschlechterungContainer.class, entityToRemove.getId());
	}

	@Override
	public void removeEinkommensverschlechterung(@Nonnull Einkommensverschlechterung einkommensverschlechterung) {
		Objects.requireNonNull(einkommensverschlechterung);
		persistence.remove(Einkommensverschlechterung.class, einkommensverschlechterung.getId());
	}

	@Override
	@Nonnull
	public FinanzielleSituationResultateDTO calculateResultate(@Nonnull Gesuch gesuch, int basisJahrPlus) {
		return finSitRechner.calculateResultateEinkommensverschlechterung(gesuch, basisJahrPlus, true);
	}

	@Override
	@Nonnull
	public String calculateProzentualeDifferenz(@Nullable BigDecimal einkommenJahr, @Nullable BigDecimal einkommenJahrPlus1) {
		BigDecimal resultGerundet = FinanzielleSituationRechner.getCalculatedProzentualeDifferenzRounded(einkommenJahr, einkommenJahrPlus1);
		String sign = MathUtil.isPositive(resultGerundet) ? "+" : "-";
		return sign + resultGerundet.abs().intValue();
	}

	@Override
	public boolean removeAllEKVOfGesuch(@Nonnull Gesuch gesuch, int yearPlus) {
		if (yearPlus != 1 && yearPlus != 2) {
			return false;
		}
		removeAllEKVForGSAndYear(gesuch.getGesuchsteller1(), yearPlus);
		removeAllEKVForGSAndYear(gesuch.getGesuchsteller2(), yearPlus);
		return true;
	}

	@Nonnull
	@Override
	public BigDecimal getMinimalesMassgebendesEinkommenForGesuch(@Nonnull Gesuch gesuch) {
		authorizer.checkReadAuthorizationFinSit(gesuch);
		// we disable EKV for calculation, since we want to simulate calculation as it would be without EKV
		gesuch.setEinkommensverschlechterungInfoContainer(null);
		final Verfuegung famGroessenVerfuegung = verfuegungService.calculateFamGroessenVerfuegung(gesuch, Sprache.DEUTSCH);
		return famGroessenVerfuegung.getZeitabschnitte().stream()
			.map(VerfuegungZeitabschnitt::getMassgebendesEinkommen)
			.min(Comparator.naturalOrder())
			.orElse(BigDecimal.ZERO);
	}

	private void removeAllEKVForGSAndYear(GesuchstellerContainer gesuchsteller, int yearPlus) {
		if (gesuchsteller != null
			&& gesuchsteller.getEinkommensverschlechterungContainer() != null) {
			if (yearPlus == 1 && gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1() != null) {
				removeEinkommensverschlechterung(gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus1());
				gesuchsteller.getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus1(null);
			} else if (yearPlus == 2 && gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2() != null) {
				removeEinkommensverschlechterung(gesuchsteller.getEinkommensverschlechterungContainer().getEkvJABasisJahrPlus2());
				gesuchsteller.getEinkommensverschlechterungContainer().setEkvJABasisJahrPlus2(null);
			}
		}
	}
}
