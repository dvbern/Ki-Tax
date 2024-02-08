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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnDestroy,
    OnInit,
    Output,
    SimpleChanges,
    ViewEncapsulation,
} from '@angular/core';
import {MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig} from '@angular/material/legacy-dialog';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {BehaviorSubject, Subscription} from 'rxjs';
import {DvNgRemoveDialogComponent} from '../../../../../app/core/component/dv-ng-remove-dialog/dv-ng-remove-dialog.component';
import {ErrorService} from '../../../../../app/core/errors/service/ErrorService';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../../models/enums/TSRole';
import {
    isSteuerdatenAnfrageStatusErfolgreich,
    TSSteuerdatenAnfrageStatus,
} from '../../../../../models/enums/TSSteuerdatenAnfrageStatus';
import {TSBenutzer} from '../../../../../models/TSBenutzer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../../utils/TSRoleUtil';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {
    DialogInitZPVNummerVerknuepfenComponent,
} from '../dialog-init-zpv-nummer-verknuepfen/dialog-init-zpv-nummer-verknpuefen.component';

const LOG = LogFactory.createLog('SteuerabfrageResponseHintsComponent');

@Component({
    selector: 'dv-steuerabfrage-response-hints',
    templateUrl: './steuerabfrage-response-hints.component.html',
    styleUrls: ['./steuerabfrage-response-hints.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None
})
export class SteuerabfrageResponseHintsComponent implements OnInit, OnDestroy, OnChanges {

    @Input()
    public readonly status: TSSteuerdatenAnfrageStatus;

    @Input()
    private readonly gsStatus: TSSteuerdatenAnfrageStatus;

    @Input()
    public readonly timestampAbruf: moment.Moment;

    @Input()
    public steuerAbfrageResponeHintStatusText: string;

    @Input()
    public steuerAbfrageRequestRunning: boolean;

    @Output()
    private readonly tryAgainEvent: EventEmitter<void> = new EventEmitter<void>();
    private principal: TSBenutzer;
    private subscription: Subscription;

    public geburtstagNotMatching$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    public constructor(
        public readonly gesuchModelManager: GesuchModelManager,
        private readonly authServiceRS: AuthServiceRS,
        private readonly dialog: MatDialog,
        private readonly finSitRS: FinanzielleSituationRS,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService
    ) {
    }

    public ngOnChanges(changes: SimpleChanges): void {
        this.changeDetectorRef.markForCheck();
    }

    public ngOnInit(): void {
        this.subscription = this.authServiceRS.principal$
            .subscribe(
                principal => this.principal = principal,
                err => LOG.error(err)
            );

        const gesuchSteller = this.gesuchModelManager.getGesuchstellerNumber() === 1 ?
            this.gesuchModelManager.getGesuch().gesuchsteller1 :
            this.gesuchModelManager.getGesuch().gesuchsteller2;
        // eslint-disable-next-line
        if (this.showZugriffErfolgreich(this.status)) {
            this.finSitRS.geburtsdatumMatchesSteuerabfrage(gesuchSteller.gesuchstellerJA.geburtsdatum,
                gesuchSteller.finanzielleSituationContainer.id).then(isMatching => {
                    this.geburtstagNotMatching$.next(!isMatching);
            });
        }

    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    public showZugriffErfolgreich(statusToCheck: TSSteuerdatenAnfrageStatus): boolean {
        return EbeguUtil.isNotNullOrUndefined(statusToCheck) &&
            isSteuerdatenAnfrageStatusErfolgreich(statusToCheck);
    }

    public showWarningRetry(): boolean {
        return this.showZugriffFailed() || this.showWarningKeinPartnerGemeinsam()
            || this.showWarningGeburtsdatum() || this.showWarningPartnerNichtGemeinsam();
    }

    public getWarningText(): string {
        switch (this.status) {
            case TSSteuerdatenAnfrageStatus.FAILED:
                return this.translate.instant('FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_FAILED',
                    {gs1: this.getGS1Name()});
                break;
            case TSSteuerdatenAnfrageStatus.FAILED_PARTNER_NICHT_GEMEINSAM:
                return this.translate.instant('FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_PARTNER_NICHT_GEMEINSAM');
                break;
            case TSSteuerdatenAnfrageStatus.FAILED_GEBURTSDATUM:
                return this.translate.instant('FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_FAILED_GEBURTSDATUM',
                        {namegs2: this.getGS2name()});
                break;
            case TSSteuerdatenAnfrageStatus.FAILED_KEIN_PARTNER_GEMEINSAM:
                return this.translate.instant('FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_KEIN_PARTNER_GEMEINSAM');
                break;
            case TSSteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL:
                return this.translate.instant('FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_UNTERJAEHRIG');
                break;
            case TSSteuerdatenAnfrageStatus.FAILED_VERAENDERTE_PARTNERSCHAFT:
                return this.translate.instant('FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_VERAENDERTE_PARTNERSCHAFT');
                break;
            case TSSteuerdatenAnfrageStatus.FAILED_UNREGELMAESSIGKEIT:
                return this.translate.instant('FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_UNREGELMAESSIGKEIT');
                break;
            case TSSteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER:
                return this.translate.instant('FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_KEINE_ZPV',
                    {email: this.getEmailBesitzende()});
                break;
            case TSSteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER_GS2:
                return this.translate.instant('FINANZIELLE_SITUATION_STEUERDATEN_ZUGRIFF_KEINE_ZPV_GS2',
                    {gs2: this.getGS2name()});
                break;
            default:
                return '';
        }
    }

    private showZugriffFailed(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED;
    }

    private showWarningKeinPartnerGemeinsam(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_KEIN_PARTNER_GEMEINSAM;
    }

    private showWarningGeburtsdatum(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_GEBURTSDATUM;
    }

    private showWarningPartnerNichtGemeinsam(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_PARTNER_NICHT_GEMEINSAM;
    }

    public showWarningWithoutRetry(): boolean {
        return this.showZugriffUnterjaehrigeFall() || this.showWarningUnregelmaessigkeit()
            || this.showWarningVeraendertePartnerschaft();
    }

    private showZugriffUnterjaehrigeFall(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL;
    }

    private showWarningVeraendertePartnerschaft(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_VERAENDERTE_PARTNERSCHAFT;
    }

    private showWarningUnregelmaessigkeit(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_UNREGELMAESSIGKEIT;
    }

    public showZugriffKeineZPVNummer(): boolean {
        return this.showZugriffKeineZpvNummerGS1() || this.showZugriffKeineZpvNummerGS2();
    }

    private showZugriffKeineZpvNummerGS1(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER;
    }

    private showZugriffKeineZpvNummerGS2(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER_GS2;
    }

    public showRetry(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.RETRY;
    }

    public showRetryForGemeinde(): boolean {
        return this.showZugriffErfolgreich(this.gsStatus) && this.isGemeindeOrSuperadmin();
    }

    public getGS1Name(): string {
        return this.gesuchModelManager.getGesuchstellerNumber() === 1 ?
            this.gesuchModelManager.getGesuch().gesuchsteller1.extractFullName() :
            this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
    }

    public getGS2Name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2.extractFullName();
    }

    public tryAgain(): void {
        this.dialog.open(DvNgRemoveDialogComponent, {
            data: {
                title: this.translate.instant('SCHNITTSTELLE_ERENEUT_ABFRAGEN')
            }
        }).afterClosed().subscribe(confirmation => {
            if (confirmation) {
                this.tryAgainEvent.emit();
            }
        }, () => {
            this.errorService.addMesageAsInfo(this.translate.instant('ERROR_UNEXPECTED'));
        });
    }

    public getEmailBesitzende(): string {
        return this.gesuchModelManager.getGesuch().dossier.fall.besitzer.email;
    }

    public getGS2name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2.gesuchstellerJA.getFullName();
    }

    public isGesuchsteller(): boolean {
        return this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
    }

    public isMutation(): boolean {
        return this.gesuchModelManager.getGesuch().isMutation();
    }

    public openDialogGSZPVVerknuepfen(): void {
        const dialogOptions: MatDialogConfig = {
            data: {
                gs: this.gesuchModelManager.getGesuchstellerNumber() === 1 ?
                    this.gesuchModelManager.getGesuch().gesuchsteller1 :
                    this.gesuchModelManager.getGesuch().gesuchsteller2,
                korrespondenzSprache:
                    this.gesuchModelManager.getGesuch().gesuchsteller1.gesuchstellerJA.korrespondenzSprache
            },
            panelClass: 'steuerdaten-email-dialog'
        };
        this.dialog.open(DialogInitZPVNummerVerknuepfenComponent, dialogOptions);
    }

    public isGemeindeOrSuperadmin() {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getGemeindeOrBGOrTSRoles().concat(TSRole.SUPER_ADMIN));
    }

    public tryAgainPossible(): boolean {
        return  !this.gesuchModelManager.isGesuchReadonly()
            && this.status === TSSteuerdatenAnfrageStatus.PROVISORISCH;
    }

    public isGesuchReadonly(): boolean {
        return this.gesuchModelManager.isGesuchReadonly();
    }

    public translateVeranlagungsstand(): string {
        return this.translate.instant(`VERANLAGUNGSSTAND_${ this.status }`);
    }

    public checkboxInformierenPossible(): boolean {
        return this.isGemeindeOrSuperadmin()
            && this.status === TSSteuerdatenAnfrageStatus.PROVISORISCH;
    }
}
