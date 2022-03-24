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
    Component,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output
} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material/dialog';
import {BehaviorSubject, Subscription} from 'rxjs';
import {LogFactory} from '../../../../../app/core/logging/LogFactory';
import {AuthServiceRS} from '../../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../../models/enums/TSRole';
import {
    isSteuerdatenAnfrageStatusErfolgreich,
    TSSteuerdatenAnfrageStatus,
} from '../../../../../models/enums/TSSteuerdatenAnfrageStatus';
import {TSBenutzer} from '../../../../../models/TSBenutzer';
import {EbeguUtil} from '../../../../../utils/EbeguUtil';
import {FinanzielleSituationRS} from '../../../../service/finanzielleSituationRS.rest';
import {GesuchModelManager} from '../../../../service/gesuchModelManager';
import {DialogInitZPVNummerVerknuepfen} from '../dialog-init-zpv-nummer-verknuepfen/dialog-init-zpv-nummer-verknpuefen.component';

const LOG = LogFactory.createLog('SteuerabfrageResponseHintsComponent');

@Component({
    selector: 'dv-steuerabfrage-response-hints',
    templateUrl: './steuerabfrage-response-hints.component.html',
    styleUrls: ['./steuerabfrage-response-hints.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SteuerabfrageResponseHintsComponent implements OnInit, OnDestroy {

    @Input()
    private readonly status: TSSteuerdatenAnfrageStatus;

    @Output()
    private readonly tryAgainEvent: EventEmitter<void> = new EventEmitter<void>();
    private principal: TSBenutzer;
    private subscription: Subscription;

    public geburtstagNotMatching$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly authServiceRS: AuthServiceRS,
        private readonly dialog: MatDialog,
        private readonly finSitRS: FinanzielleSituationRS,
    ) {
    }

    public ngOnInit(): void {
        this.subscription = this.authServiceRS.principal$
            .subscribe(
                principal => this.principal = principal,
                err => LOG.error(err),
            );

        const gs = this.gesuchModelManager.getGesuchstellerNumber() === 1 ?
            this.gesuchModelManager.getGesuch().gesuchsteller1 :
            this.gesuchModelManager.getGesuch().gesuchsteller2;
        // tslint:disable-next-line:early-exit
        if (this.showZugriffErfolgreich()) {
            this.finSitRS.geburtsdatumMatchesSteuerabfrage(gs.gesuchstellerJA.geburtsdatum,
                gs.finanzielleSituationContainer.id).then(isMatching => {
                    this.geburtstagNotMatching$.next(!isMatching);
            });
        }
    }

    public ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    public showZugriffErfolgreich(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.status) &&
            isSteuerdatenAnfrageStatusErfolgreich(this.status);
    }

    public showZugriffFailed(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED;
    }

    public showZugriffUnterjaehrigeFall(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_UNTERJAEHRIGER_FALL;
    }

    public showWarningKeinPartnerGemeinsam(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_KEIN_PARTNER_GEMEINSAM;
    }

    public showWarningGeburtsdatum(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_GEBURTSDATUM;
    }

    public showZugriffKeineZpvNummer(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER;
    }

    public showWarningPartnerNichtGemeinsam(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_PARTNER_NICHT_GEMEINSAM;
    }

    public showZugriffKeineZpvNummerGS2(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.FAILED_KEINE_ZPV_NUMMER_GS2;
    }

    public showRetry(): boolean {
        return this.status === TSSteuerdatenAnfrageStatus.RETRY;
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
        this.tryAgainEvent.emit();
    }

    public getEmailLoggedIn(): string {
        return this.principal.email;
    }

    public getGS2name(): string {
        return this.gesuchModelManager.getGesuch().gesuchsteller2.gesuchstellerJA.getFullName();
    }

    public isGesuchsteller(): boolean {
        return this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
    }

    public openDialogGSZPVVerknuepfen(): void {
        const dialogOptions: MatDialogConfig = {
            data: {
                gs: this.gesuchModelManager.getGesuchstellerNumber() === 1 ?
                    this.gesuchModelManager.getGesuch().gesuchsteller1 :
                    this.gesuchModelManager.getGesuch().gesuchsteller2,
                korrespondenzSprache:
                    this.gesuchModelManager.getGesuch().gesuchsteller1.gesuchstellerJA.korrespondenzSprache,
            },
        };
        this.dialog.open(DialogInitZPVNummerVerknuepfen, dialogOptions);
    }
}
