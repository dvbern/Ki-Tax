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
import * as moment from 'moment';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import TSGemeindeKonfiguration from '../../../models/TSGemeindeKonfiguration';
import {CONSTANTS} from '../../core/constants/CONSTANTS';

@Component({
    selector: 'dv-gemeinde-ts-konfiguration',
    templateUrl: './gemeinde-ts-konfig.component.html',
    styleUrls: ['./gemeinde-ts-konfig.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeTsKonfigComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;
    @Input() public konfigurationsListe: TSGemeindeKonfiguration[];
    @Input() public gemeindeStatus: TSGemeindeStatus;
    @Input() public editMode: boolean = false;

    private navigationDest: StateDeclaration;

    public constructor(
        private readonly $transition$: Transition,
    ) {
    }

    public ngOnInit(): void {
        this.navigationDest = this.$transition$.to();
        this.initProperties();
    }

    public isKonfigurationEditable(gk: TSGemeindeKonfiguration): boolean {
        return 'gemeinde.edit' === this.navigationDest.name
            && this.editMode
            && (TSGemeindeStatus.EINGELADEN === this.gemeindeStatus
                || (gk.gesuchsperiode && gk.gesuchsperiode.status &&
                    TSGesuchsperiodeStatus.GESCHLOSSEN !== gk.gesuchsperiode.status));
    }

    public getTagesschuleAktivierungsdatumAsString(konfiguration: TSGemeindeKonfiguration): string {
        const datum = konfiguration.konfigTagesschuleAktivierungsdatum;
        if (datum && datum.isValid()) {
            return datum.format(CONSTANTS.DATE_FORMAT);
        }
        return '';
    }

    public tagesschuleAktivierungsdatumChanged(config: TSGemeindeKonfiguration): void {
        config.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB === property.key)
            .forEach(property => { property.value = this.getTagesschuleAktivierungsdatumAsString(config); });
    }

    public getTagesschuleErsterSchultagAsString(konfiguration: TSGemeindeKonfiguration): string {
        const datum = konfiguration.konfigTagesschuleErsterSchultag;
        if (datum && datum.isValid()) {
            return datum.format(CONSTANTS.DATE_FORMAT);
        }
        return '';
    }

    public tagesschuleErsterSchultagChanged(config: TSGemeindeKonfiguration): void {
        config.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG === property.key)
            .forEach(property => { property.value = this.getTagesschuleErsterSchultagAsString(config); });
    }

    private initProperties(): void {
        this.konfigurationsListe.forEach(config => {
            config.konfigTagesschuleAktivierungsdatum = config.gesuchsperiode.gueltigkeit.gueltigAb;
            config.konfigTagesschuleErsterSchultag = config.gesuchsperiode.gueltigkeit.gueltigAb;
            config.konfigurationen.forEach(property => {
                if (TSEinstellungKey.GEMEINDE_TAGESSCHULE_ANMELDUNGEN_DATUM_AB === property.key) {
                    config.konfigTagesschuleAktivierungsdatum = moment(property.value, CONSTANTS.DATE_FORMAT);
                }
                if (TSEinstellungKey.GEMEINDE_TAGESSCHULE_ERSTER_SCHULTAG === property.key) {
                    config.konfigTagesschuleErsterSchultag = moment(property.value, CONSTANTS.DATE_FORMAT);
                }
            });
        });
    }
}
