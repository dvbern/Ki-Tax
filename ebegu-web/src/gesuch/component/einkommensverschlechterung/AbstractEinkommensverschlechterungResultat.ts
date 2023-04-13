/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
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
import {ChangeDetectorRef} from '@angular/core';
import {Transition} from '@uirouter/core';
import {IPromise} from 'angular';
import {TSFinanzielleSituationResultateDTO} from '../../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSWizardStepName} from '../../../models/enums/TSWizardStepName';
import {TSWizardStepStatus} from '../../../models/enums/TSWizardStepStatus';
import {TSFinanzModel} from '../../../models/TSFinanzModel';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import {BerechnungsManager} from '../../service/berechnungsManager';
import {GesuchModelManager} from '../../service/gesuchModelManager';
import {WizardStepManager} from '../../service/wizardStepManager';
import {AbstractGesuchViewX} from '../abstractGesuchViewX';
import {EKVViewUtil} from './EKVViewUtil';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import {TSEinstellung} from '../../../models/TSEinstellung';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {take} from "rxjs/operators";

export abstract class AbstractEinkommensverschlechterungResultat extends AbstractGesuchViewX<TSFinanzModel> {
    public resultatBasisjahr?: TSFinanzielleSituationResultateDTO;
    public resultatProzent: string;
    private grenze?: number;

    public constructor(
        public gesuchModelManager: GesuchModelManager,
        protected wizardStepManager: WizardStepManager,
        protected berechnungsManager: BerechnungsManager,
        protected ref: ChangeDetectorRef,
        protected stepName: TSWizardStepName,
        protected readonly einstellungRS: EinstellungRS,
        protected readonly $transition$: Transition
    ) {
        super(gesuchModelManager, wizardStepManager, stepName);
        const parsedBasisJahrPlusNum = parseInt(this.$transition$.params().basisjahrPlus, 10);
        this.model = new TSFinanzModel(this.gesuchModelManager.getBasisjahr(),
            this.gesuchModelManager.isGesuchsteller2Required(),
            null,
            parsedBasisJahrPlusNum);
        this.model.copyEkvDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.model.copyFinSitDataFromGesuch(this.gesuchModelManager.getGesuch());
        this.gesuchModelManager.setBasisJahrPlusNumber(parsedBasisJahrPlusNum);
        this.calculate();
        this.resultatBasisjahr = null;
        this.calculateResultateVorjahr();

        if(EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuchsperiode())) {
            this.einstellungRS.getAllEinstellungenBySystemCached(
                this.gesuchModelManager.getGesuchsperiode().id
            ).pipe(take(1)).subscribe((response: TSEinstellung[]) => {
                response.filter(r => r.key === TSEinstellungKey.PARAM_GRENZWERT_EINKOMMENSVERSCHLECHTERUNG)
                    .forEach(value => {
                        this.grenze = Number(value.value);
                    });
            });
        }
    }

    public calculate(): void {
        if (!this.model || !this.model.getBasisJahrPlus()) {
            console.log('No gesuch and Basisjahr to calculate');
            return;
        }
        this.berechnungsManager.calculateEinkommensverschlechterungTemp(this.model, this.model.getBasisJahrPlus())
            .then(() => {
                this.resultatProzent = this.calculateVeraenderung();
            });
    }

    public calculateVeraenderung(): string {
        if (EbeguUtil.isNotNullOrUndefined(this.resultatBasisjahr)) {
            const resultatJahrPlus1 = this.getResultate();
            if (EbeguUtil.isNotNullOrUndefined(resultatJahrPlus1)) {
                this.berechnungsManager.calculateProzentualeDifferenz(
                    this.resultatBasisjahr.massgebendesEinkVorAbzFamGr, resultatJahrPlus1.massgebendesEinkVorAbzFamGr)
                    .then(abweichungInProzentZumVorjahr => {
                        this.resultatProzent = abweichungInProzentZumVorjahr;
                        this.ref.markForCheck();
                        return abweichungInProzentZumVorjahr;
                    });
            }
        }
        return '';
    }

    public calculateResultateVorjahr(): void {
        this.berechnungsManager.calculateFinanzielleSituationTemp(this.model).then(resultatVorjahr => {
            this.resultatBasisjahr = resultatVorjahr;
            this.resultatProzent = this.calculateVeraenderung();
            this.ref.markForCheck();
        });
    }

    public getResultate(): TSFinanzielleSituationResultateDTO {
        return this.model.getBasisJahrPlus() === 2 ?
            this.berechnungsManager.einkommensverschlechterungResultateBjP2 :
            this.berechnungsManager.einkommensverschlechterungResultateBjP1;
    }

    public ekvAkzeptiert(): boolean {
        if (EbeguUtil.isNotNullOrUndefined(this.resultatProzent) && (Number(this.resultatProzent) >= this.grenze ||
            Number(this.resultatProzent) <= -this.grenze)) {
            return true;
        }
        return false;
    }

    public ekvGrenzWerte(): number {
        return this.grenze;
    }

    public hasSecondAntragstellende(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getGesuch().gesuchsteller2);
    }

    public getGemeinsameFullname(): string {
        return EKVViewUtil.getGemeinsameFullname(this.gesuchModelManager);
    }

    public getAntragsteller1Name(): string {
        return EKVViewUtil.getAntragsteller1Name(this.gesuchModelManager);
    }

    public getAntragsteller2Name(): string {
        return EKVViewUtil.getAntragsteller2Name(this.gesuchModelManager);
    }

    /**
     * Hier wird der Status von WizardStep auf OK (MUTIERT fuer Mutationen) aktualisiert aber nur wenn die letzte
     * Seite EVResultate gespeichert wird. Sonst liefern wir einfach den aktuellen GS als Promise zurueck.
     */
    public updateStatus(changes: boolean): IPromise<any> {
        if (this.isLastEinkVersStep()) {
            if (this.gesuchModelManager.getGesuch().isMutation()) {
                if (this.wizardStepManager.getCurrentStep().wizardStepStatus === TSWizardStepStatus.NOK || changes) {
                    this.wizardStepManager.updateCurrentWizardStepStatusMutiert();
                }
            } else {
                return this.wizardStepManager.updateCurrentWizardStepStatusSafe(
                    this.wizardStepManager.getCurrentStepName(),
                    TSWizardStepStatus.OK);
            }
        }
        // wenn nichts gespeichert einfach den aktuellen GS zurueckgeben
        return Promise.resolve(this.gesuchModelManager.getStammdatenToWorkWith());
    }

    /**
     * Prueft ob es die letzte Seite von EVResultate ist. Es ist die letzte Seite wenn es zum letzten EV-Jahr gehoert
     */
    private isLastEinkVersStep(): boolean {
        // Letztes Jahr haengt von den eingegebenen Daten ab
        const info = this.gesuchModelManager.getGesuch().extractEinkommensverschlechterungInfo();

        return info.ekvFuerBasisJahrPlus2 && this.gesuchModelManager.basisJahrPlusNumber === 2
            || !info.ekvFuerBasisJahrPlus2 && this.gesuchModelManager.basisJahrPlusNumber === 1;
    }
}
