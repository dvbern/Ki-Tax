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
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import ch.dvbern.ebegu.authentication.PrincipalBean;
import ch.dvbern.ebegu.dto.FinanzielleSituationResultateDTO;
import ch.dvbern.ebegu.dto.FinanzielleSituationStartDTO;
import ch.dvbern.ebegu.entities.Auszahlungsdaten;
import ch.dvbern.ebegu.entities.Einstellung;
import ch.dvbern.ebegu.entities.Familiensituation;
import ch.dvbern.ebegu.entities.FamiliensituationContainer;
import ch.dvbern.ebegu.entities.FinanzielleSituation;
import ch.dvbern.ebegu.entities.FinanzielleSituationContainer;
import ch.dvbern.ebegu.entities.Gesuch;
import ch.dvbern.ebegu.enums.EinstellungKey;
import ch.dvbern.ebegu.enums.ErrorCodeEnum;
import ch.dvbern.ebegu.enums.FinSitStatus;
import ch.dvbern.ebegu.enums.UserRole;
import ch.dvbern.ebegu.errors.EbeguEntityNotFoundException;
import ch.dvbern.ebegu.finanzielleSituationRechner.FinanzielleSituationRechnerFactory;
import ch.dvbern.ebegu.util.EbeguUtil;
import ch.dvbern.lib.cdipersistence.Persistence;
import ch.dvbern.oss.lib.beanvalidation.embeddables.IBAN;

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

		familiensituation.setSozialhilfeBezueger(finSitStartDTO.getSozialhilfeBezueger());
		if (familiensituation.getSozialhilfeBezueger() == null || !familiensituation.getSozialhilfeBezueger()) {
			familiensituationContainer.getSozialhilfeZeitraumContainers().clear();
		}
		familiensituation.setGemeinsameSteuererklaerung(finSitStartDTO.getGemeinsameSteuererklaerung());
		familiensituation.setVerguenstigungGewuenscht(finSitStartDTO.getVerguenstigungGewuenscht());
		if (finSitStartDTO.getVerguenstigungGewuenscht().equals(Boolean.TRUE)) {
			familiensituation.setKeineMahlzeitenverguenstigungBeantragt(finSitStartDTO.isKeineMahlzeitenverguenstigungGewuenscht());
			if (!finSitStartDTO.isKeineMahlzeitenverguenstigungGewuenscht() && finSitStartDTO.getIban() != null && finSitStartDTO.getKontoinhaber() != null) {
				//Hier aufpassen, per Default sind die Mahlzeitverguenstigung Gewunscht
				//aber wenn die Gemeinde keine Mahlzeitverguenstigung anbietet kann der Gesuchsteller oder Gemeinde
				//gar keinen Iban oder Kontoinhaber eingeben, deswegen waren diese Feldern nullable before!
				Auszahlungsdaten auszahlungsdaten = new Auszahlungsdaten();
				auszahlungsdaten.setIban(new IBAN(finSitStartDTO.getIban()));
				auszahlungsdaten.setKontoinhaber(finSitStartDTO.getKontoinhaber());
				auszahlungsdaten.setAdresseKontoinhaber(finSitStartDTO.getZahlungsadresse());
				familiensituation.setAuszahlungsdatenMahlzeiten(auszahlungsdaten);
			}
			familiensituation.setAbweichendeZahlungsadresseMahlzeiten(finSitStartDTO.isAbweichendeZahlungsadresse());
		} else {
			// Wenn das Einkommen nicht deklariert wird, kann auch keine Mahlzeitenverguenstigung gewaehrt werden
			familiensituation.setKeineMahlzeitenverguenstigungBeantragt(true);
			familiensituation.setAuszahlungsdatenMahlzeiten(null);
			familiensituation.setAbweichendeZahlungsadresseMahlzeiten(false);
		}

		if (finSitStartDTO.getIbanInfoma() != null && finSitStartDTO.getKontoinhaberInfoma() != null) {
			setInfomaFields(finSitStartDTO, familiensituation);
		}

		// Steuererklaerungs/-veranlagungs-Flags nachfuehren fuer GS2
		handleGemeinsameSteuererklaerung(gesuch);

		return gesuchService.updateGesuch(gesuch, false);
	}

	private void setInfomaFields(FinanzielleSituationStartDTO finSitStartDTO, Familiensituation familiensituation) {
		Auszahlungsdaten auszahlungsdatenInfoma =
			(familiensituation.getAuszahlungsdatenInfoma() != null
				? familiensituation.getAuszahlungsdatenInfoma()
				: new Auszahlungsdaten()
			);
		auszahlungsdatenInfoma.setIban(new IBAN(finSitStartDTO.getIbanInfoma()));
		Objects.requireNonNull(finSitStartDTO.getKontoinhaberInfoma());
		auszahlungsdatenInfoma.setKontoinhaber(finSitStartDTO.getKontoinhaberInfoma());
		familiensituation.setAbweichendeZahlungsadresseInfoma(finSitStartDTO.isAbweichendeZahlungsadresseInfoma());
		auszahlungsdatenInfoma.setAdresseKontoinhaber(finSitStartDTO.getZahlungsadresseInfoma());
		// gesuchsteller is not allowed to set those fields
		if (!principalBean.isCallerInRole(UserRole.GESUCHSTELLER)) {
			familiensituation.setInfomaKreditorennummer(finSitStartDTO.getInfomaKreditorennummer());
			familiensituation.setInfomaBankcode(finSitStartDTO.getInfomaBankcode());
			familiensituation.setAuszahlungAnEltern(
				finSitStartDTO.getAuszahlungAnEltern() != null && finSitStartDTO.getAuszahlungAnEltern()
			);
		}
		familiensituation.setAuszahlungsdatenInfoma(auszahlungsdatenInfoma);
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
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
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
}
