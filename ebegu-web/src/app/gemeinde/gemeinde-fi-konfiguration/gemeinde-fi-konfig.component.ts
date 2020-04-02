/*
 * Copyright (C) 2019 DV Bern AG, Switzerland
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

import {ChangeDetectionStrategy, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {Moment} from 'moment';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import {TSFerieninselStammdaten} from '../../../models/TSFerieninselStammdaten';
import {TSFerieninselZeitraum} from '../../../models/TSFerieninselZeitraum';
import {TSGemeindeKonfiguration} from '../../../models/TSGemeindeKonfiguration';
import {TSDateRange} from '../../../models/types/TSDateRange';
import {EbeguUtil} from '../../../utils/EbeguUtil';
import * as moment from 'moment';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

@Component({
    selector: 'dv-gemeinde-fi-konfiguration',
    templateUrl: './gemeinde-fi-konfig.component.html',
    styleUrls: ['./gemeinde-fi-konfig.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeFiKonfigComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;
    @Input() public konfigurationsListe: TSGemeindeKonfiguration[];
    @Input() public gemeindeStatus: TSGemeindeStatus;
    @Input() public editMode: boolean = false;
    @Input() public fiAnmeldungenStartDatum: Moment;

    private navigationDest: StateDeclaration;

    public constructor(
        private readonly $transition$: Transition,
    ) {
    }

    public ngOnInit(): void {
        this.navigationDest = this.$transition$.to();
    }

    public isKonfigurationEditable(gk: TSGemeindeKonfiguration): boolean {
        return 'gemeinde.edit' === this.navigationDest.name
            && this.editMode
            && (TSGemeindeStatus.EINGELADEN === this.gemeindeStatus
                || (gk.gesuchsperiode && gk.gesuchsperiode.status &&
                    TSGesuchsperiodeStatus.GESCHLOSSEN !== gk.gesuchsperiode.status));
    }

    public addFerieninselZeitraum(ferieninselStammdaten: TSFerieninselStammdaten): void {
        if (!ferieninselStammdaten.zeitraumList) {
            ferieninselStammdaten.zeitraumList = [];
        }
        const zeitraum = new TSFerieninselZeitraum();
        zeitraum.gueltigkeit = new TSDateRange();
        ferieninselStammdaten.zeitraumList.push(zeitraum);
    }

    public isAnmeldeschlussRequired(fiStammdaten: TSFerieninselStammdaten): boolean {
        // Wenn mindestens ein Zeitraum erfasst ist
        return EbeguUtil.isNotNullOrUndefined(fiStammdaten.ersterZeitraum.gueltigkeit.gueltigAb)
            || EbeguUtil.isNotNullOrUndefined(fiStammdaten.ersterZeitraum.gueltigkeit.gueltigBis);
    }

    public isDatumAbRequired(zeitraum: TSFerieninselZeitraum, fiStammdaten: TSFerieninselStammdaten): boolean {
        // Wenn entweder der Anmeldeschluss erfasst ist, oder das Datum bis
        return EbeguUtil.isNotNullOrUndefined(fiStammdaten.anmeldeschluss)
            || (EbeguUtil.isNotNullOrUndefined(zeitraum.gueltigkeit)
                && EbeguUtil.isNotNullOrUndefined(zeitraum.gueltigkeit.gueltigBis));
    }

    public isDatumBisRequired(zeitraum: TSFerieninselZeitraum, fiStammdaten: TSFerieninselStammdaten): boolean {
        // Wenn entweder der Anmeldeschluss erfasst ist, oder das Datum ab
        return EbeguUtil.isNotNullOrUndefined(fiStammdaten.anmeldeschluss)
            || (EbeguUtil.isNotNullOrUndefined(zeitraum.gueltigkeit)
                && EbeguUtil.isNotNullOrUndefined(zeitraum.gueltigkeit.gueltigAb));
    }

    public removeFerieninselZeitraum(fiStammdaten: TSFerieninselStammdaten, zeitraum: TSFerieninselZeitraum): void {
        const index = fiStammdaten.zeitraumList.indexOf(zeitraum, 0);
        fiStammdaten.zeitraumList.splice(index, 1);
    }

    public formatDate(date: moment.Moment): string {
        if (!date || !date.isValid()) {
            return '';
        }

        return date.format(CONSTANTS.DATE_FORMAT);
    }
}
