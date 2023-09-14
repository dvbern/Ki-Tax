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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.ebegu.services;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationStartDTO;
import ch.dvbern.ebegu.dto.JaxFinanzielleSituationAufteilungDTO;
import ch.dvbern.ebegu.entities.AbstractEntity_;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation_;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.entities.NeueVeranlagungsMitteilung;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.errors.EbeguRuntimeException;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationRechnerFactory;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.ebegu.util.MathUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;
import org.apache.commons.lang.StringUtils;

/**
 * Service fuer FinanzielleSituation
 */
@Stateless
@Local(FinanzielleSituationService.class)
public class FinanzielleSituationServiceBean extends AbstractBaseService implements FinanzielleSituationService {

	@Inject
	private Persistence persistence;

	@Inject
	private WizardStepService wizardStepService;

	@Inject
	private Authorizer authorizer;

	@Inject
	private EinstellungService einstellungService;

	@Inject
	private GesuchService gesuchService;

	@Inject
	private PrincipalBean principalBean;

	@Nonnull
	@Override
	public Gesuch saveFinanzielleSituationStart(
		@Nonnull FinanzielleSituationContainer finanzielleSituation,
		@Nonnull FinanzielleSituationStartDTO finSitStartDTO,
		@Nonnull String gesuchId
	) {
		authorizer.checkWriteAuthorization(finanzielleSituation);

		// Die eigentliche FinSit speichern
		final boolean isNew = finanzielleSituation.isNew();
		FinanzielleSituationContainer finanzielleSituationPersisted = persistence.merge(finanzielleSituation);
		Gesuch gesuch = gesuchService.findGesuch(gesuchId).orElseThrow(() -> new EbeguEntityNotFoundException("saveFinanzielleSituation", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, "GesuchId invalid: " + gesuchId));
		if (isNew) {
			// Die FinSit war neu und muss noch mit dem GS 1 verknuepft werden
			Objects.requireNonNull(gesuch.getGesuchsteller1());
			gesuch.getGesuchsteller1().setFinanzielleSituationContainer(finanzielleSituationPersisted);
			gesuch = persistence.merge(gesuch);
		}

		// Die zwei Felder "sozialhilfebezueger" und "gemeinsameSteuererklaerung" befinden sich nicht auf der FinanziellenSituation, sondern auf der
		// FamilienSituation -> Das Gesuch muss hier aus der DB geladen werden, damit nichts überschrieben wird!
		gesuch = saveFinanzielleSituationFelderAufGesuch(
			finSitStartDTO,
			gesuch
		);

		wizardStepService.updateSteps(
			gesuchId,
			null,
			finanzielleSituationPersisted.getFinanzielleSituationJA(),
			wizardStepService.getFinSitWizardStepNameForGesuch(gesuch),
			1); // it must be substep 1 since it is finanzielleSituationStart

		return gesuch;
	}

	private Gesuch saveFinanzielleSituationFelderAufGesuch(
		@Nonnull FinanzielleSituationStartDTO finSitStartDTO,
		@Nonnull Gesuch gesuch
	) {
		FamiliensituationContainer familiensituationContainer = gesuch.getFamiliensituationContainer();
		Objects.requireNonNull(familiensituationContainer);
		Familiensituation familiensituation = familiensituationContainer.getFamiliensituationJA();
		Objects.requireNonNull(familiensituation);

		// Falls vorher keine Vergünstigung gewünscht war, müssen wir den FinSitStatus wieder zurücksetzen, da dieser automatisch auf
		// AKZEPTIERT gesetzt wurde
		Boolean verguenstigungGewuenschtVorher = familiensituation.getVerguenstigungGewuenscht();
		if (!finSitStartDTO.getVerguenstigungGewuenscht().equals(verguenstigungGewuenschtVorher)
			&& EbeguUtil.isNotNullAndFalse(verguenstigungGewuenschtVorher)) {
			// Es war vorher explizit nicht gewünscht -> wir setzen den Wert zurück
			gesuch.setFinSitStatus(null);
		}

		if (EbeguUtil.isNotNullAndFalse(finSitStartDTO.getVerguenstigungGewuenscht())) {
			// Es ist neu explizit nicht mehr gewünscht -> wir setzen den Wert auf AKZEPTIERT
			gesuch.setFinSitStatus(FinSitStatus.AKZEPTIERT);
		}

		gesuch.setFinSitAenderungGueltigAbDatum(finSitStartDTO.getFinSitAenderungGueltigAbDatum());

		familiensituation.setSozialhilfeBezueger(finSitStartDTO.getSozialhilfeBezueger());
		familiensituation.setZustaendigeAmtsstelle(finSitStartDTO.getZustaendigeAmtsstelle());
		familiensituation.setNameBetreuer(finSitStartDTO.getNameBetreuer());
		if (familiensituation.getSozialhilfeBezueger() == null || !familiensituation.getSozialhilfeBezueger()) {
			familiensituationContainer.getSozialhilfeZeitraumContainers().clear();
		}
		familiensituation.setGemeinsameSteuererklaerung(finSitStartDTO.getGemeinsameSteuererklaerung());
		familiensituation.setVerguenstigungGewuenscht(finSitStartDTO.getVerguenstigungGewuenscht());
		familiensituation.setKeineMahlzeitenverguenstigungBeantragt(finSitStartDTO.isKeineMahlzeitenverguenstigungGewuenscht());
		familiensituation.setAbweichendeZahlungsadresse(finSitStartDTO.isAbweichendeZahlungsadresse());
		if (principalBean.isCallerInAnyOfRole(UserRole.getJugendamtSuperadminRoles())) {
			familiensituation.setAuszahlungAusserhalbVonKibon(finSitStartDTO.isAuszahlungAusserhalbVonKibon());
		}
		if (StringUtils.isNotBlank(finSitStartDTO.getIban()) && StringUtils.isNotBlank(finSitStartDTO.getKontoinhaber())) {
			if (familiensituation.getAuszahlungsdaten() == null) {
				familiensituation.setAuszahlungsdaten(new Auszahlungsdaten());
			}
			familiensituation.getAuszahlungsdaten().setIban(new IBAN(finSitStartDTO.getIban()));
			familiensituation.getAuszahlungsdaten().setKontoinhaber(finSitStartDTO.getKontoinhaber());
			familiensituation.getAuszahlungsdaten().setAdresseKontoinhaber(finSitStartDTO.getZahlungsadresse());

			// gesuchsteller is not allowed to set those fields
			if (!principalBean.isCallerInRole(UserRole.GESUCHSTELLER)) {
				familiensituation.getAuszahlungsdaten().setInfomaKreditorennummer(finSitStartDTO.getInfomaKreditorennummer());
				familiensituation.getAuszahlungsdaten().setInfomaBankcode(finSitStartDTO.getInfomaBankcode());
			}
		} else {
			// Wenn die IBAN und der Kontoinhaber nicht gesetzt sind, wurden die Auszahlungsdaten resetd
			familiensituation.setAuszahlungsdaten(null);
			familiensituation.setAbweichendeZahlungsadresse(false);
		}

		// Steuererklaerungs/-veranlagungs-Flags nachfuehren fuer GS2
		handleGemeinsameSteuererklaerung(gesuch);

		return gesuchService.updateGesuch(gesuch, false);
	}

	@Nonnull
	@Override
	public FinanzielleSituationContainer saveFinanzielleSituation(
		@Nonnull FinanzielleSituationContainer finanzielleSituation,
		@Nonnull String gesuchId
	) {
		authorizer.checkWriteAuthorization(finanzielleSituation);

		// Die eigentliche FinSit speichern
		FinanzielleSituationContainer finanzielleSituationPersisted = persistence.merge(finanzielleSituation);

		final Gesuch gesuch = gesuchService.findGesuch(gesuchId).orElseThrow(() -> new EbeguEntityNotFoundException(
			"saveFinanzielleSituation", ErrorCodeEnum.ERROR_ENTITY_NOT_FOUND, gesuchId));

		// Steuererklaerungs/-veranlagungs-Flags nachfuehren fuer GS2
		handleGemeinsameSteuererklaerung(gesuch);

		wizardStepService.updateSteps(gesuchId, null, finanzielleSituationPersisted.getFinanzielleSituationJA(), wizardStepService.getFinSitWizardStepNameForGesuch(gesuch));

		return finanzielleSituationPersisted;
	}

	private void handleGemeinsameSteuererklaerung(@Nonnull Gesuch gesuch) {
		final Familiensituation familiensituation = gesuch.extractFamiliensituation();
		Objects.requireNonNull(familiensituation);
		Objects.requireNonNull(gesuch.getGesuchsteller1(), "GS1 darf zu diesem Zeitpunkt nicht null sein");
		// Steuererklaerungs/-veranlagungs-Flags nachfuehren fuer GS2
		if (familiensituation.getGemeinsameSteuererklaerung() != null
			&& familiensituation.getGemeinsameSteuererklaerung()
			&& gesuch.hasSecondGesuchstellerAtAnyTimeOfGesuchsperiode()
			&& gesuch.getGesuchsteller1().getFinanzielleSituationContainer() != null
		) {
			Objects.requireNonNull(gesuch.getGesuchsteller2(), "GS2 darf zu diesem Zeitpunkt nicht null sein");
			if (gesuch.getGesuchsteller2().getFinanzielleSituationContainer() == null) {
				// Falls der GS2 Container zu diesem Zeitpunkt noch nicht existiert, wird er hier erstellt
				gesuch.getGesuchsteller2().setFinanzielleSituationContainer(new FinanzielleSituationContainer());
				gesuch.getGesuchsteller2().getFinanzielleSituationContainer().setFinanzielleSituationJA(new FinanzielleSituation());
				gesuch.getGesuchsteller2().getFinanzielleSituationContainer()
					.setJahr(gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getJahr());
				gesuch.getGesuchsteller2().getFinanzielleSituationContainer().setGesuchsteller(gesuch.getGesuchsteller2());
			}
			FinanzielleSituation finanzielleSituationGS2 = gesuch.getGesuchsteller2().getFinanzielleSituationContainer().getFinanzielleSituationJA();
			FinanzielleSituation finanzielleSituationGS1 = gesuch.getGesuchsteller1().getFinanzielleSituationContainer().getFinanzielleSituationJA();

			finanzielleSituationGS2.setSteuerveranlagungErhalten(finanzielleSituationGS1.getSteuerveranlagungErhalten());
			finanzielleSituationGS2.setSteuererklaerungAusgefuellt(finanzielleSituationGS1.getSteuererklaerungAusgefuellt());
		}
	}

	@Nonnull
	@Override
	public Optional<FinanzielleSituationContainer> findFinanzielleSituation(@Nonnull String id) {
		Objects.requireNonNull(id, "id muss gesetzt sein");
		FinanzielleSituationContainer finanzielleSituation = persistence.find(FinanzielleSituationContainer.class, id);
		authorizer.checkReadAuthorization(finanzielleSituation);
		return Optional.ofNullable(finanzielleSituation);
	}

	@Override
	@Nonnull
	public FinanzielleSituationResultateDTO calculateResultate(@Nonnull Gesuch gesuch) {
		// Die Berechnung der FinSit Resultate beruht auf einem "Pseudo-Gesuch", dieses hat
		// keinen Status und kann/muss nicht geprueft werden!
		return FinanzielleSituationRechnerFactory.getRechner(gesuch).calculateResultateFinanzielleSituation(gesuch, true);
	}

	@Override
	public void calculateFinanzDaten(@Nonnull Gesuch gesuch) {
		final BigDecimal minimumEKV = calculateGrenzwertEKV(gesuch);
		FinanzielleSituationRechnerFactory.getRechner(gesuch).calculateFinanzDaten(gesuch, minimumEKV);
	}

	@Nonnull
	@Override
	public FinanzielleSituationContainer saveFinanzielleSituationTemp(FinanzielleSituationContainer finanzielleSituation) {
		authorizer.checkWriteAuthorization(finanzielleSituation);

		// Die eigentliche FinSit speichern
		FinanzielleSituationContainer finanzielleSituationPersisted = persistence.merge(finanzielleSituation);

		return  finanzielleSituationPersisted;
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

	public void setValuesFromAufteilungDTO(@Nonnull FinanzielleSituation finSitGs1, @Nonnull FinanzielleSituation finSitGs2, @Nonnull JaxFinanzielleSituationAufteilungDTO dto) {

		assertSumIsEqual(
			dto.getBruttoertraegeVermoegenGS1(),
			dto.getBruttoertraegeVermoegenGS2(),
			finSitGs1.getBruttoertraegeVermoegen(),
			finSitGs2.getBruttoertraegeVermoegen(),
			"bruttoertraegeVermoegen"
		);
		finSitGs1.setBruttoertraegeVermoegen(dto.getBruttoertraegeVermoegenGS1());
		finSitGs2.setBruttoertraegeVermoegen(dto.getBruttoertraegeVermoegenGS2());

		assertSumIsEqual(
			dto.getAbzugSchuldzinsenGS1(),
			dto.getAbzugSchuldzinsenGS2(),
			finSitGs1.getAbzugSchuldzinsen(),
			finSitGs2.getAbzugSchuldzinsen(),
			"abzugSchuldzinsen"
		);
		finSitGs1.setAbzugSchuldzinsen(dto.getAbzugSchuldzinsenGS1());
		finSitGs2.setAbzugSchuldzinsen(dto.getAbzugSchuldzinsenGS2());

		assertSumIsEqual(
			dto.getGewinnungskostenGS1(),
			dto.getGewinnungskostenGS2(),
			finSitGs1.getGewinnungskosten(),
			finSitGs2.getGewinnungskosten(),
			"gewinnungskosten"
		);
		finSitGs1.setGewinnungskosten(dto.getGewinnungskostenGS1());
		finSitGs2.setGewinnungskosten(dto.getGewinnungskostenGS2());

		assertSumIsEqual(
			dto.getGeleisteteAlimenteGS1(),
			dto.getGeleisteteAlimenteGS2(),
			finSitGs1.getGeleisteteAlimente(),
			finSitGs2.getGeleisteteAlimente(),
			"geleisteteAlimente"
		);
		finSitGs1.setGeleisteteAlimente(dto.getGeleisteteAlimenteGS1());
		finSitGs2.setGeleisteteAlimente(dto.getGeleisteteAlimenteGS2());

		assertSumIsEqual(
			dto.getNettovermoegenGS1(),
			dto.getNettovermoegenGS2(),
			finSitGs1.getNettoVermoegen(),
			finSitGs2.getNettoVermoegen(),
			"nettovermoegen"
		);
		finSitGs1.setNettoVermoegen(dto.getNettovermoegenGS1());
		finSitGs2.setNettoVermoegen(dto.getNettovermoegenGS2());
		assertSumIsEqual(
			dto.getNettoertraegeErbengemeinschaftGS1(),
			dto.getNettoertraegeErbengemeinschaftGS2(),
			finSitGs1.getNettoertraegeErbengemeinschaft(),
			finSitGs2.getNettoertraegeErbengemeinschaft(),
			"NettoertraegeErbengemeinschaft"
		);
		finSitGs1.setNettoertraegeErbengemeinschaft(dto.getNettoertraegeErbengemeinschaftGS1());
		finSitGs2.setNettoertraegeErbengemeinschaft(dto.getNettoertraegeErbengemeinschaftGS2());
	}

	private void assertSumIsEqual(
		BigDecimal gs1ValueNew,
		BigDecimal gs2ValueNew,
		BigDecimal gs1ValueOld,
		BigDecimal gs2ValueOld,
		@Nonnull String valueName
	) {
		Objects.requireNonNull(gs1ValueNew);
		Objects.requireNonNull(gs2ValueNew);
		Objects.requireNonNull(gs1ValueOld);
		Objects.requireNonNull(gs2ValueOld);

		var sumNew = MathUtil.DEFAULT.addNullSafe(gs1ValueNew, gs2ValueNew);
		var sumOld = MathUtil.DEFAULT.addNullSafe(gs1ValueOld, gs2ValueOld);
		if (sumNew.compareTo(sumOld) == 0) {
			return;
		}
		throw new EbeguRuntimeException("assertSumIsEqual", "Sum is not the same for " + valueName);
	}

	@Override
	@Nullable
	public FinanzielleSituation findFinanzielleSituationForNeueVeranlagungsMitteilung(@Nonnull
		NeueVeranlagungsMitteilung persistedMitteilung) {
		final CriteriaBuilder cb = persistence.getCriteriaBuilder();
		final CriteriaQuery<FinanzielleSituation> query = cb.createQuery(FinanzielleSituation.class);
		Root<FinanzielleSituation> root = query.from(FinanzielleSituation.class);

		ParameterExpression<String>
			steuerDatenResponseIdParam = cb.parameter(String.class, "steuerDatenResponseId");
		Predicate predicateSteuerdatenResponseId = cb.equal(root.get(FinanzielleSituation_.steuerdatenResponse).get(
			AbstractEntity_.id), steuerDatenResponseIdParam);

		query.where(predicateSteuerdatenResponseId);
		TypedQuery<FinanzielleSituation> q = persistence.getEntityManager().createQuery(query);
		q.setParameter(steuerDatenResponseIdParam, persistedMitteilung.getSteuerdatenResponse().getId());

		return q.getResultList().size() > 0 ? q.getResultList().get(0) : null;
	}
}
