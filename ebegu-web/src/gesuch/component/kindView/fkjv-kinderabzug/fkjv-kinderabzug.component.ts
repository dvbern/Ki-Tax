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
    AfterViewInit,
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    OnDestroy,
    OnInit,
    ViewChild,
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {LogFactory} from '../../../../app/core/logging/LogFactory';
import {TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {TSKind} from '../../../../models/TSKind';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {FjkvKinderabzugExchangeService} from './fjkv-kinderabzug-exchange.service';
import {TSFamiliensituation} from '../../../../models/TSFamiliensituation';

const LOG = LogFactory.createLog('FkjvKinderabzugComponent');

@Component({
    selector: 'dv-fkjv-kinderabzug',
    templateUrl: './fkjv-kinderabzug.component.html',
    styleUrls: ['./fkjv-kinderabzug.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FkjvKinderabzugComponent implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild(NgForm)
    public readonly form: NgForm;

    @Input()
    public kindContainer: TSKindContainer;

    private readonly unsubscribe$: Subject<void> = new Subject<void>();
    private kindIsOrGetsVolljaehrig: boolean = false;

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly cd: ChangeDetectorRef,
        private readonly fkjvExchangeService: FjkvKinderabzugExchangeService,
    ) {
    }

    public ngOnInit(): void {
        const gesuchsperiode = this.gesuchModelManager.getGesuchsperiode();
        this.fkjvExchangeService.getFormValidationTriggered$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(() => {
                this.cd.markForCheck();
            }, err => LOG.error(err));
        this.fkjvExchangeService.getGeburtsdatumChanged$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(date => {
                this.kindIsOrGetsVolljaehrig = EbeguUtil.calculateKindIsOrGetsVolljaehrig(date, gesuchsperiode);
                this.change();
            }, err => LOG.error(err));
        this.kindIsOrGetsVolljaehrig = EbeguUtil.calculateKindIsOrGetsVolljaehrig(
            this.getModel().geburtsdatum,
            gesuchsperiode,
        );
        this.change();
    }

    public ngAfterViewInit(): void {
        this.fkjvExchangeService.form = this.form;
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
    }

    public getModel(): TSKind | undefined {
        if (this.kindContainer?.kindJA) {
            return this.kindContainer.kindJA;
        }
        return undefined;
    }

    public getModelGS(): TSKind | undefined {
        if (this.kindContainer?.kindGS) {
            return this.kindContainer.kindGS;
        }
        return undefined;
    }

    public change(): void {
        this.deleteValuesOfHiddenQuestions();
        this.cd.markForCheck();
    }

    public pflegeEntschaedigungErhaltenVisible(): boolean {
        return this.getModel().isPflegekind;
    }

    public obhutAlternierendAusuebenVisible(): boolean {
        return !this.kindIsOrGetsVolljaehrig && !this.getModel().isPflegekind;
    }

    public gemeinsamesGesuchVisible(): boolean {
        return this.getModel().obhutAlternierendAusueben &&
            this.getModel().familienErgaenzendeBetreuung &&
            this.isAlleinerziehenOrShortKonkubinat();
    }

    public inErstausbildungVisible(): boolean {
        return this.kindIsOrGetsVolljaehrig && !this.getModel().isPflegekind;
    }

    public lebtKindAlternierendVisible(): boolean {
        return this.getModel().inErstausbildung;
    }

    public alimenteErhaltenVisible(): boolean {
        return this.getModel().lebtKindAlternierend;
    }

    public alimenteBezahlenVisible(): boolean {
        return EbeguUtil.isNotNullOrUndefined(this.getModel().lebtKindAlternierend)
            && !this.getModel().lebtKindAlternierend;
    }

    private deleteValuesOfHiddenQuestions(): void {
        if (!this.pflegeEntschaedigungErhaltenVisible()) {
            this.getModel().pflegeEntschaedigungErhalten = undefined;
        }
        if (!this.obhutAlternierendAusuebenVisible()) {
            this.getModel().obhutAlternierendAusueben = undefined;
        }
        if (!this.gemeinsamesGesuchVisible()) {
            this.getModel().gemeinsamesGesuch = undefined;
        }
        if (!this.inErstausbildungVisible()) {
            this.getModel().inErstausbildung = undefined;
        }
        if (!this.lebtKindAlternierendVisible()) {
            this.getModel().lebtKindAlternierend = undefined;
        }
        if (!this.alimenteErhaltenVisible()) {
            this.getModel().alimenteErhalten = undefined;
        }
        if (!this.alimenteBezahlenVisible()) {
            this.getModel().alimenteBezahlen = undefined;
        }
        if (!this.geteilteElterlicheSorgeObhutVisible()) {
            this.getModel().geteilteElterlicheSorgeObhut = undefined;
        }
        // Wenn das Kind eine Betreuung hat ist es read-only und darf nicht zurück gesetzt werden
        if (!this.famErgaenzendeBetreuuungVisible() && !this.hasKindBetreuungen()) {
            this.getModel().familienErgaenzendeBetreuung = false;
        }
    }

    public famErgaenzendeBetreuuungVisible(): boolean {
        return !this.kindIsOrGetsVolljaehrig;
    }

    private isAlleinerziehenOrShortKonkubinat(): boolean {
        return this.getFamiliensituationToUse().familienstatus === TSFamilienstatus.ALLEINERZIEHEND ||
            this.isShortKonkubinat();
    }

    public hasKindBetreuungen(): boolean {
        return this.kindContainer.betreuungen?.length > 0;
    }

    public geteilteElterlicheSorgeObhutVisible(): boolean {
        return this.famErgaenzendeBetreuuungVisible()
            && EbeguUtil.isNotNullOrUndefined(this.getModel().familienErgaenzendeBetreuung)
            && !this.getModel().familienErgaenzendeBetreuung;
    }

    private isShortKonkubinat(): boolean {
        if (this.getFamiliensituationToUse().familienstatus !== TSFamilienstatus.KONKUBINAT_KEIN_KIND) {
            return false;
        }

        return this.getFamiliensituationToUse()
            .konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(this.gesuchModelManager.getGesuchsperiode());
    }

    private getFamiliensituationToUse(): TSFamiliensituation {
        //wenn mutation und partner nicht identisch mit vorgesuch dann ist FamSit des Erstantrages relevant
        if (this.gesuchModelManager.getGesuch().isMutation() &&
            EbeguUtil.isNotNullOrUndefined(this.gesuchModelManager.getFamiliensituation().partnerIdentischMitVorgesuch) &&
            !this.gesuchModelManager.getFamiliensituation().partnerIdentischMitVorgesuch) {
            return this.gesuchModelManager.getFamiliensituationErstgesuch();
        }

        return this.gesuchModelManager.getFamiliensituation();
    }
}
