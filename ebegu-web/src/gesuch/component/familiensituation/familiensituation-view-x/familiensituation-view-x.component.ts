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

import {Component, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {EinstellungRS} from '../../../../admin/service/einstellungRS.rest';
import {
    DvNgRemoveDialogComponent
} from '../../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {CONSTANTS} from '../../../../app/core/constants/CONSTANTS';
import {TSDemoFeature} from '../../../../app/core/directive/dv-hide-feature/TSDemoFeature';
import {ErrorService} from '../../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../../app/core/logging/LogFactory';
import {DemoFeatureRS} from '../../../../app/core/service/demoFeatureRS.rest';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSEinstellungKey} from '../../../../models/enums/TSEinstellungKey';
import {getTSFamilienstatusValues, TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {
    getTSGesuchstellerKardinalitaetValues,
    TSGesuchstellerKardinalitaet
} from '../../../../models/enums/TSGesuchstellerKardinalitaet';
import {
    getTSUnterhaltsvereinbarungAnswerValues,
    TSUnterhaltsvereinbarungAnswer
} from '../../../../models/enums/TSUnterhaltsvereinbarungAnswer';
import {TSEinstellung} from '../../../../models/TSEinstellung';
import {TSFamiliensituation} from '../../../../models/TSFamiliensituation';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {BerechnungsManager} from '../../../service/berechnungsManager';
import {FamiliensituationRS} from '../../../service/familiensituationRS.service';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {WizardStepManager} from '../../../service/wizardStepManager';
import {AbstractFamiliensitutaionView} from '../AbstractFamiliensitutaionView';

const LOG = LogFactory.createLog('FamiliensitutionViewComponent');

@Component({
    selector: 'dv-familiensituation-view-x',
    templateUrl: './familiensituation-view-x.component.html',
    styleUrls: ['./familiensituation-view-x.component.less']
})
export class FamiliensituationViewXComponent extends AbstractFamiliensitutaionView implements OnInit {

    private readonly familienstatusValues: Array<TSFamilienstatus>;
    public initialFamiliensituation: TSFamiliensituation;
    public gesuchstellerKardinalitaetValues: Array<TSGesuchstellerKardinalitaet>;
    public unterhaltsvereinbarungAnswerValues: Array<TSUnterhaltsvereinbarungAnswer>;
    public gesuchBeendenDemoFeature = TSDemoFeature.GESUCH_BEENDEN_FAMSIT;
    public demoFeatureGesuchBeendenFamSitActive: boolean;

    public constructor(
        protected readonly gesuchModelManager: GesuchModelManager,
        private readonly berechnungsManager: BerechnungsManager,
        protected readonly errorService: ErrorService,
        protected readonly wizardStepManager: WizardStepManager,
        private readonly dialog: MatDialog,
        private readonly $translate: TranslateService,
        protected readonly familiensituationRS: FamiliensituationRS,
        private readonly einstellungRS: EinstellungRS,
        protected readonly authService: AuthServiceRS,
        private readonly demoFeatureRS: DemoFeatureRS
    ) {
        super(gesuchModelManager, errorService, wizardStepManager, familiensituationRS, authService);
        this.initialFamiliensituation = this.gesuchModelManager.getFamiliensituation();
        this.gesuchstellerKardinalitaetValues = getTSGesuchstellerKardinalitaetValues();
        this.unterhaltsvereinbarungAnswerValues = getTSUnterhaltsvereinbarungAnswerValues();
        this.familienstatusValues = getTSFamilienstatusValues();
        demoFeatureRS.isDemoFeatureAllowed(this.gesuchBeendenDemoFeature)
            .then(isAllowed => this.demoFeatureGesuchBeendenFamSitActive = isAllowed);
    }

    public ngOnInit(): void {
        this.einstellungRS.getAllEinstellungenBySystemCached(
            this.gesuchModelManager.getGesuchsperiode().id
        ).subscribe((response: TSEinstellung[]) => {
            response.filter(r => r.key === TSEinstellungKey.FKJV_FAMILIENSITUATION_NEU)
                .forEach(value => {
                    this.getFamiliensituation().fkjvFamSit = value.getValueAsBoolean();
                });
            response.filter(r => r.key === TSEinstellungKey.MINIMALDAUER_KONKUBINAT)
                .forEach(value => {
                    this.getFamiliensituation().minDauerKonkubinat = Number(value.value);
                });
        }, error => LOG.error(error));
    }

    protected async confirm(onResult: (arg: any) => void): Promise<void> {
            if (this.isConfirmationRequired()) {
                const descriptionText: any = this.$translate.instant('FAMILIENSITUATION_WARNING_BESCHREIBUNG', {
                    gsfullname: this.getGesuch().gesuchsteller2
                        ? this.getGesuch().gesuchsteller2.extractFullName() : ''
                });
                const dialogResult = await this.dialog.open(DvNgRemoveDialogComponent, {
                    data: {
                        title: 'FAMILIENSITUATION_WARNING',
                        text: descriptionText
                    }
                }).afterClosed().toPromise();

                if (dialogResult) {
                    const savedContaier = await this.save();
                    onResult(savedContaier);
                } else {
                    onResult(undefined);
                }
                return;

            }

            const result = await this.save();
            onResult(result);
    }

    public getFamiliensituation(): TSFamiliensituation {
        return this.model.familiensituationJA;
    }

    public getFamiliensituationGS(): TSFamiliensituation {
        return this.model.familiensituationGS;
    }

    public isStartKonkubinatVisible(): boolean {
        return this.getFamiliensituation()?.familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND;
    }

    public showFragePartnerWieBisher(): boolean {

        const isVorPeriode = this.getFamiliensituation().aenderungPer?.isBefore(
            this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigAb);
        if (isVorPeriode) {
            return false;
        }
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return EbeguUtil.isNotNullOrUndefined(this.getFamiliensituation()?.aenderungPer) &&
            !this.getFamiliensituationErstgesuch()?.isSameFamiliensituation(this.getFamiliensituation()) &&
            this.getFamiliensituationErstgesuch().hasSecondGesuchsteller(bis) &&
            this.getFamiliensituation().hasSecondGesuchsteller(bis);
    }

    /**
     * Removes startKonkubinat when the familienstatus doesn't require it.
     * If we are in a mutation and we change to KONKUBINAT_KEIN_KIND we need to copy aenderungPer into startKonkubinat
     */
    public familienstatusChanged(): void {
        if (this.getFamiliensituation().familienstatus !== TSFamilienstatus.KONKUBINAT_KEIN_KIND) {
            this.getFamiliensituation().startKonkubinat = undefined;

        } else if (this.isMutation() && this.getFamiliensituation().aenderungPer && this.isStartKonkubinatVisible()) {
            this.getFamiliensituation().startKonkubinat = this.getFamiliensituation().aenderungPer;
        }
        // eslint-disable-next-line
        if (!this.isFamilienstatusAlleinerziehendOrShortKonkubinat()) {
            this.getFamiliensituation().gesuchstellerKardinalitaet = undefined;
            this.getFamiliensituation().unterhaltsvereinbarung = undefined;
            this.getFamiliensituation().unterhaltsvereinbarungBemerkung = undefined;
            this.getFamiliensituation().geteilteObhut = undefined;
        }
        this.getFamiliensituation().partnerIdentischMitVorgesuch = undefined;
    }

    /**
     * This should happen only in a Mutation, where we can change the field aenderungPer but not startKonkubinat.
     * Any change in aenderungPer will copy the value into startKonkubinat if the last is visible
     */
    public aenderungPerChanged(): void {
        if (this.isStartKonkubinatVisible()) {
            this.getFamiliensituation().startKonkubinat = this.getFamiliensituation().aenderungPer;
        }
        this.onDatumBlur();
    }

    /**
     * Confirmation is required when the GS2 already exists and the familiensituation changes from 2GS to 1GS. Or when
     * in a Mutation the GS2 is new and will be removed
     */
    private isConfirmationRequired(): boolean {
        return (!this.isKorrekturModusJugendamt()
                || (this.isKorrekturModusJugendamt() && this.getGesuch().gesuchsteller2))
            && ((!this.isMutation() && this.checkChanged2To1GS())
                || (this.isMutation() && (this.isChanged1To2Reverted() || this.checkChanged2To1GSMutation())));
    }

    private checkChanged2To1GS(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.getGesuch().gesuchsteller2
            && this.getGesuch().gesuchsteller2.id
            && this.initialFamiliensituation.hasSecondGesuchsteller(bis)
            && this.isScheidung();
    }

    private isChanged1To2Reverted(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.getGesuch().gesuchsteller2
            && this.getGesuch().gesuchsteller2.id
            && this.isScheidung()
            && this.model.familiensituationErstgesuch
            && !this.model.familiensituationErstgesuch.hasSecondGesuchsteller(bis);
    }

    private checkChanged2To1GSMutation(): boolean {
        const ab = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigAb;
        return (this.getFamiliensituation()?.aenderungPer?.isBefore(ab)
            && this.getGesuch().getRegelStartDatum().isBefore(ab));
    }

    private isScheidung(): boolean {
        const bis = this.gesuchModelManager.getGesuchsperiode().gueltigkeit.gueltigBis;
        return this.initialFamiliensituation.hasSecondGesuchsteller(bis)
            && !this.getFamiliensituation()?.hasSecondGesuchsteller(bis);
    }

    public isStartKonkubinatDisabled(): boolean {
        return this.isMutation() || (this.isGesuchReadonly() && !this.isKorrekturModusJugendamt());
    }

    public isNotPartnerIdentischMitVorgesuch(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.getFamiliensituation().partnerIdentischMitVorgesuch) &&
            !this.getFamiliensituation().partnerIdentischMitVorgesuch;
    }

    public getFamiliensituationValues(): Array<TSFamilienstatus> {
        return this.familienstatusValues;
    }

    public getUnterhaltvereinbarungValues(): Array<TSUnterhaltsvereinbarungAnswer> {
        return this.unterhaltsvereinbarungAnswerValues;
    }

    public showGesuchstellerKardinalitaet(): boolean {
        if (this.getFamiliensituation() && this.isFKJVFamSit() &&
            this.isFamilienstatusAlleinerziehendOrShortKonkubinat()) {
            return this.getFamiliensituation().geteilteObhut;
        }
        return false;
    }

    public showFrageUnterhaltsvereinbarung(): boolean {
        if (this.getFamiliensituation() && this.isFKJVFamSit() &&
            this.isFamilienstatusAlleinerziehendOrShortKonkubinat()) {
            return EbeguUtil.isNotNullAndFalse(this.getFamiliensituation().geteilteObhut);
        }
        return false;
    }

    public showBemerkungUnterhaltsvereinbarung(): boolean {
        return this.getFamiliensituation()?.unterhaltsvereinbarung
            === TSUnterhaltsvereinbarungAnswer.UNTERHALTSVEREINBARUNG_NICHT_MOEGLICH;
    }

    public showFrageGeteilteObhut(): boolean {
        if (this.getFamiliensituation() && this.isFKJVFamSit()) {
            return this.isFamilienstatusAlleinerziehendOrShortKonkubinat()
                || this.isFamilienstatusKonkubinatKeinKindAndSmallerThanXYears();
        }
        return false;
    }

    private isFamilienstatusAlleinerziehendOrShortKonkubinat(): boolean {
        if (!this.getFamiliensituation()) {
            return false;
        }
        if (this.getFamiliensituation().familienstatus === TSFamilienstatus.ALLEINERZIEHEND) {
            return true;
        }
        return this.getFamiliensituation().familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND
            && this.getFamiliensituation()
                .konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(this.getGesuch().gesuchsperiode);
    }

    private isFamilienstatusKonkubinatKeinKindAndSmallerThanXYears(): boolean {
        if (!this.getFamiliensituation()) {
            return false;
        }
        return this.getFamiliensituation().familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND
            && this.getFamiliensituation()
                .konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(this.getGesuch().gesuchsperiode);
    }

    public frageGeteiltObhutClicked(): void {
        this.getFamiliensituation().gesuchstellerKardinalitaet = undefined;
        this.getFamiliensituation().unterhaltsvereinbarung = undefined;
        this.getFamiliensituation().unterhaltsvereinbarungBemerkung = undefined;
    }

    public frageUnterhaltsvereinbarungClicked(): void {
        this.getFamiliensituation().unterhaltsvereinbarungBemerkung = undefined;
    }

    public getTextForFamSitFrage2Tooltip(): string {
        return this.$translate.instant('FAMILIENSITUATION_HELP',
            {jahr: this.getFamiliensituation().minDauerKonkubinat});
    }

    public getTextForFamSitGeteilteObhut(): string {
        return this.$translate.instant('FAMILIENSITUATION_FRAGE_GEMEINSAME_OBHUT_INFO');
    }

    public getTextForFamSitUnterhaltsvereinbarung(): string {
        return this.$translate.instant('FAMILIENSITUATION_FRAGE_UNTERHALTSVEREINBARUNG_INFO');
    }

    public getTextForFamSitUnterhaltsvereinbarungGrund(): string {
        return this.$translate.instant('UNTERHALTSVEREINBARUNG_GRUND_INFO');
    }

    public getToday(): moment.Moment {
        return moment();
    }

    public getNameGesuchsteller2(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2 ?
            this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName() : '';
    }

    private getDatumEndOfMonthAfterAenderungPer(): string {
        return this.getFamiliensituation()
                .aenderungPer.endOf('month')
                .format(CONSTANTS.DATE_FORMAT);
    }

    public getNotPertnerIdentischMitVorgesuchWarning(): string {
        let warning: string = this.$translate.instant('FAMILIENSITUATION_FRAGE_PARTNERIDENTISCH_WARNING', {
            namegs2: this.getNameGesuchsteller2(),
            endeDatum: this.getDatumEndOfMonthAfterAenderungPer()
        });

        const partnerNotIdentischWarningBeiPaaren: string =
                this.$translate.instant('FAMILIENSITUATION_FRAGE_PARTNERIDENTISCH_WARNING_PAAR',
                        { warnbez: this.getWarningPaarBezeichnung() });
        warning = warning.concat(' '.toString(), partnerNotIdentischWarningBeiPaaren.toString());
        return warning;
    }

    private getWarningPaarBezeichnung(): string {
        const familienstatus: TSFamilienstatus = this.gesuchModelManager.getGesuch().extractFamiliensituation().familienstatus;
        if (familienstatus === TSFamilienstatus.VERHEIRATET) {
            return this.$translate.instant('FAMILIENSITUATION_FRAGE_PARTNERIDENTISCH_WARNBEZ_EHEPARTNER');
        }
        if (familienstatus === TSFamilienstatus.ALLEINERZIEHEND){
            return this.$translate.instant('FAMILIENSITUATION_FRAGE_PARTNERIDENTISCH_WARNBEZ_ANDERER_ELTERNTEIL');
        }
        return this.$translate.instant('FAMILIENSITUATION_FRAGE_PARTNERIDENTISCH_WARNBEZ_KONKUBINTASPARTNER');
    }

    public isFKJVFamSit(): boolean {
        return this.getFamiliensituation().fkjvFamSit;
    }

    public getPartnerIdentischFrage(): string {
        const familienstatus: TSFamilienstatus = this.gesuchModelManager.getGesuch().extractFamiliensituation().familienstatus;
        if (familienstatus === TSFamilienstatus.VERHEIRATET) {
            return this.$translate.instant('FAMILIENSITUATION_FRAGE_PARTNERIDENTISCH_EHE' , {
                namegs2: this.getNameGesuchsteller2()
            } );
        }
        if (familienstatus === TSFamilienstatus.ALLEINERZIEHEND){
            return this.$translate.instant('FAMILIENSITUATION_FRAGE_PARTNERIDENTISCH_ANDERER_ELTERNTEIL', {
                namegs2: this.getNameGesuchsteller2()
            } );
        }

        if (familienstatus === TSFamilienstatus.KONKUBINAT_KEIN_KIND && !this.konkubinatIsTwoYearsOld() ){
            return this.$translate.instant('FAMILIENSITUATION_FRAGE_PARTNERIDENTISCH_ANDERER_ELTERNTEIL', {
                namegs2: this.getNameGesuchsteller2()
            } );
        }
        return this.$translate.instant('FAMILIENSITUATION_FRAGE_PARTNERIDENTISCH_KONKUBINAT', {
            namegs2: this.getNameGesuchsteller2()
        } );
    }

    private konkubinatIsTwoYearsOld(): Boolean {
        let startKonkubinat: moment.Moment = this.gesuchModelManager.getGesuch().extractFamiliensituation().startKonkubinat;
        let twoYearsAfterKonkubinat: moment.Moment = moment(startKonkubinat).add(2, 'year');
        return moment().isAfter(twoYearsAfterKonkubinat);

    }
}
