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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {TSKind} from '../../../../models/TSKind';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {GesuchModelManager} from '../../../service/gesuchModelManager';

@Component({
    selector: 'dv-fkjv-kinderabzug',
    templateUrl: './fkjv-kinderabzug.component.html',
    styleUrls: ['./fkjv-kinderabzug.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class FkjvKinderabzugComponent implements OnInit {

    @Input()
    public kindContainer: TSKindContainer;

    public constructor(
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly cd: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
    }

    public getModel(): TSKind | undefined {
        if (this.kindContainer?.kindJA) {
            return this.kindContainer.kindJA;
        }
        return undefined;
    }

    public change(): void {
        this.deleteValuesOfHiddenQuestions();
        this.cd.markForCheck();
    }

    public isPflegekindVisible(): boolean {
        return true;
    }

    public pflegeEntschaedigungErhaltenVisible(): boolean {
        return this.getModel().isPflegekind;
    }

    public obhutAlternierendAusuebenVisible(): boolean {
        return !this.kindIsOrGetsVolljaehrig() && !this.getModel().isPflegekind;
    }

    public gemeinsamesGesuchVisible(): boolean {
        return this.getModel().obhutAlternierendAusueben;
    }

    public inErstausbildungVisible(): boolean {
        return this.kindIsOrGetsVolljaehrig() && !this.getModel().isPflegekind;
    }

    public lebtKindAlternierendVisible(): boolean {
        return this.getModel().inErstausbildung;
    }

    public alimenteErhaltenVisible(): boolean {
        return this.getModel().lebtKindAlternierend;
    }

    public alimenteBezahlenVisible(): boolean {
        return this.getModel().lebtKindAlternierend === false;
    }

    private kindIsOrGetsVolljaehrig(): boolean {
        return true;
    }

    private deleteValuesOfHiddenQuestions(): void {
        if (!this.isPflegekindVisible()) {
            this.getModel().isPflegekind = undefined;
        }
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
    }

}
