/*
 * Copyright (C) 2018 DV Bern AG, Switzerland
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
import {TranslateService} from '@ngx-translate/core';
import {Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {getTSEinschulungTypGemeindeValues, TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import TSGemeindeKonfiguration from '../../../models/TSGemeindeKonfiguration';
import {ERWERBSPENSUM_ZUSCHLAG_MIN_VALUE} from '../../core/constants/CONSTANTS';


@Component({
    selector: 'dv-gemeinde-bg-konfiguration',
    templateUrl: './gemeinde-bg-konfig.component.html',
    styleUrls: ['./gemeinde-bg-konfig.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeBgKonfigComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;
    @Input() public konfigurationsListe: TSGemeindeKonfiguration[];
    @Input() public gemeindeStatus: TSGemeindeStatus;
    @Input() public editMode: boolean = false;

    public einschulungTypGemeindeValues: Array<TSEinschulungTyp>;
    public readonly ERWERBSPENSUM_ZUSCHLAG_MIN_VALUE = ERWERBSPENSUM_ZUSCHLAG_MIN_VALUE;
    private navigationDest: StateDeclaration;

    public constructor(
        private readonly $transition$: Transition,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        this.navigationDest = this.$transition$.to();
        this.einschulungTypGemeindeValues = getTSEinschulungTypGemeindeValues();
        this.initProperties();
    }

    public getKonfigKontingentierungString(): string {
        return this.translate.instant('KONTINGENTIERUNG');
    }

    public changeKonfigKontingentierung(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED === property.key)
            .forEach(property => { property.value = gk.konfigKontingentierung ? 'true' : 'false'; });
    }

    public getKonfigBeguBisUndMitSchulstufeString(gk: TSGemeindeKonfiguration): string {
        const bgBisStr = this.translate.instant(gk.konfigBeguBisUndMitSchulstufe.toString());
        return bgBisStr;
    }

    public changeKonfigBeguBisUndMitSchulstufe(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE === property.key)
            .forEach(property => { property.value = gk.konfigBeguBisUndMitSchulstufe; });
    }

    public changeErwerbspensumZuschlagOverriden(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => TSEinstellungKey.ERWERBSPENSUM_ZUSCHLAG_OVERRIDEN === property.key)
            .forEach(property => { property.value =  String(gk.erwerbspensumZuschlagOverriden); });

        // if the flag is unchecked, we need to restore the original value
        if (gk.erwerbspensumZuschlagOverriden === false) {
            this.resetErwerbspensumZuschlag(gk);
        }
    }

    public changeErwerbspensumZuschlag(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen
            .filter(property => TSEinstellungKey.ERWERBSPENSUM_ZUSCHLAG === property.key)
            .forEach(property => { property.value =  String(gk.erwerbspensumZuschlag); });
    }

    public resetErwerbspensumZuschlag(gk: TSGemeindeKonfiguration): void {
        gk.erwerbspensumZuschlag = gk.erwerbspensumZuschlagMandant;
        this.changeErwerbspensumZuschlag(gk);
    }

    public isKonfigurationEditable(gk: TSGemeindeKonfiguration): boolean {
        return 'gemeinde.edit' === this.navigationDest.name
            && this.editMode
            && (TSGemeindeStatus.EINGELADEN === this.gemeindeStatus
                || (gk.gesuchsperiode && gk.gesuchsperiode.status &&
                    TSGesuchsperiodeStatus.GESCHLOSSEN !== gk.gesuchsperiode.status));
    }

    private initProperties(): void {
        this.konfigurationsListe.forEach(config => {
            config.initProperties();
        });
    }
}
