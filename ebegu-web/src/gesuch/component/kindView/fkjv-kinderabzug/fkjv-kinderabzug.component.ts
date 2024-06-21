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
    ViewChild
} from '@angular/core';
import {NgForm} from '@angular/forms';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {LogFactory} from '../../../../app/core/logging/LogFactory';
import {TSFamilienstatus} from '../../../../models/enums/TSFamilienstatus';
import {TSGesuchstellerKardinalitaet} from '../../../../models/enums/TSGesuchstellerKardinalitaet';
import {TSUnterhaltsvereinbarungAnswer} from '../../../../models/enums/TSUnterhaltsvereinbarungAnswer';
import {TSFamiliensituation} from '../../../../models/TSFamiliensituation';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {GesuchModelManager} from '../../../service/gesuchModelManager';
import {KinderabzugExchangeService} from '../service/kinderabzug-exchange.service';

const LOG = LogFactory.createLog('FkjvKinderabzugComponent');

@Component({
    selector: 'dv-fkjv-kinderabzug',
    templateUrl: './fkjv-kinderabzug.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FkjvKinderabzugComponent
    implements OnInit, AfterViewInit, OnDestroy
{
    @ViewChild(NgForm)
    public readonly form: NgForm;

    @Input()
    public kindContainer: TSKindContainer;

    private readonly unsubscribe$: Subject<void> = new Subject<void>();
    private kindIsOrGetsVolljaehrig: boolean = false;

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly cd: ChangeDetectorRef,
        private readonly fkjvExchangeService: KinderabzugExchangeService
    ) {}

    public ngOnInit(): void {
        const gesuchsperiode = this.gesuchModelManager.getGesuchsperiode();
        this.fkjvExchangeService
            .getFormValidationTriggered$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                () => {
                    this.cd.markForCheck();
                },
                err => LOG.error(err)
            );
        this.fkjvExchangeService
            .getGeburtsdatumChanged$()
            .pipe(takeUntil(this.unsubscribe$))
            .subscribe(
                date => {
                    this.kindIsOrGetsVolljaehrig =
                        EbeguUtil.calculateKindIsOrGetsVolljaehrig(
                            date,
                            gesuchsperiode
                        );
                    this.change();
                },
                err => LOG.error(err)
            );
        this.kindIsOrGetsVolljaehrig =
            EbeguUtil.calculateKindIsOrGetsVolljaehrig(
                this.kindContainer?.kindJA.geburtsdatum,
                gesuchsperiode
            );
        this.change();
    }

    public ngAfterViewInit(): void {
        this.fkjvExchangeService.addForm(this.form);
    }

    public ngOnDestroy(): void {
        this.unsubscribe$.next();
    }

    public change(): void {
        this.deleteValuesOfHiddenQuestions();
    }

    public pflegeEntschaedigungErhaltenVisible(): boolean {
        return this.kindContainer?.kindJA.isPflegekind;
    }

    public obhutAlternierendAusuebenVisible(): boolean {
        return (
            !this.kindIsOrGetsVolljaehrig &&
            !this.kindContainer?.kindJA.isPflegekind
        );
    }

    public gemeinsamesGesuchVisible(): boolean {
        return (
            this.kindContainer?.kindJA.obhutAlternierendAusueben &&
            EbeguUtil.isNotNullOrUndefined(
                this.kindContainer?.kindJA.familienErgaenzendeBetreuung
            ) &&
            (this.gesuchModelManager.getFamiliensituation()
                .gesuchstellerKardinalitaet ===
                TSGesuchstellerKardinalitaet.ZU_ZWEIT ||
                this.gesuchModelManager.getFamiliensituation()
                    .unterhaltsvereinbarung ===
                    TSUnterhaltsvereinbarungAnswer.NEIN_UNTERHALTSVEREINBARUNG)
        );
    }

    public inErstausbildungVisible(): boolean {
        return (
            this.kindIsOrGetsVolljaehrig &&
            !this.kindContainer?.kindJA.isPflegekind
        );
    }

    public lebtKindAlternierendVisible(): boolean {
        return this.kindContainer?.kindJA.inErstausbildung;
    }

    public alimenteErhaltenVisible(): boolean {
        return this.kindContainer?.kindJA.lebtKindAlternierend;
    }

    public alimenteBezahlenVisible(): boolean {
        return (
            EbeguUtil.isNotNullOrUndefined(
                this.kindContainer?.kindJA.lebtKindAlternierend
            ) && !this.kindContainer?.kindJA.lebtKindAlternierend
        );
    }

    private deleteValuesOfHiddenQuestions(): void {
        if (this.kindContainer?.kindJA) {
            if (!this.pflegeEntschaedigungErhaltenVisible()) {
                this.kindContainer.kindJA.pflegeEntschaedigungErhalten =
                    undefined;
            }
            if (!this.obhutAlternierendAusuebenVisible()) {
                this.kindContainer.kindJA.obhutAlternierendAusueben = undefined;
            }
            if (!this.gemeinsamesGesuchVisible()) {
                this.kindContainer.kindJA.gemeinsamesGesuch = undefined;
            }
            if (!this.inErstausbildungVisible()) {
                this.kindContainer.kindJA.inErstausbildung = undefined;
            }
            if (!this.lebtKindAlternierendVisible()) {
                this.kindContainer.kindJA.lebtKindAlternierend = undefined;
            }
            if (!this.alimenteErhaltenVisible()) {
                this.kindContainer.kindJA.alimenteErhalten = undefined;
            }
            if (!this.alimenteBezahlenVisible()) {
                this.kindContainer.kindJA.alimenteBezahlen = undefined;
            }
            // Wenn das Kind eine Betreuung hat ist es read-only und darf nicht zurück gesetzt werden
            if (
                !this.famErgaenzendeBetreuuungVisible() &&
                !this.hasKindBetreuungen()
            ) {
                this.kindContainer.kindJA.familienErgaenzendeBetreuung = false;
            }
        }
    }

    public famErgaenzendeBetreuuungVisible(): boolean {
        return !this.kindIsOrGetsVolljaehrig;
    }

    public hasKindBetreuungen(): boolean {
        return this.kindContainer.betreuungen?.length > 0;
    }

    private isShortKonkubinat(): boolean {
        if (
            this.getFamiliensituationToUse().familienstatus !==
            TSFamilienstatus.KONKUBINAT_KEIN_KIND
        ) {
            return false;
        }

        return this.getFamiliensituationToUse().konkubinatIsShorterThanXYearsAtAnyTimeAfterStartOfPeriode(
            this.gesuchModelManager.getGesuchsperiode()
        );
    }

    private getFamiliensituationToUse(): TSFamiliensituation {
        //wenn mutation und partner nicht identisch mit vorgesuch dann ist FamSit des Erstantrages relevant
        if (
            this.gesuchModelManager.getGesuch().isMutation() &&
            EbeguUtil.isNotNullOrUndefined(
                this.gesuchModelManager.getFamiliensituation()
                    .partnerIdentischMitVorgesuch
            ) &&
            !this.gesuchModelManager.getFamiliensituation()
                .partnerIdentischMitVorgesuch
        ) {
            return this.gesuchModelManager.getFamiliensituationErstgesuch();
        }

        return this.gesuchModelManager.getFamiliensituation();
    }
}
