/*
 * AGPL File-Header
 *
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
import {StateService, Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {getTSEinschulungTypValues, TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import {TSGemeindeStatus} from '../../../models/enums/TSGemeindeStatus';
import {TSGesuchsperiodeStatus} from '../../../models/enums/TSGesuchsperiodeStatus';
import TSGemeindeKonfiguration from '../../../models/TSGemeindeKonfiguration';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';

@Component({
    selector: 'dv-gemeinde-konfiguration',
    templateUrl: './gemeinde-konfig.component.html',
    styleUrls: ['./gemeinde-konfig.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GemeindeKonfigComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;
    @Input() public stammdaten: TSGemeindeStammdaten;

    public beguStart: string;
    public einschulungTypValues: Array<TSEinschulungTyp>;
    private navigationDest: StateDeclaration;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly translate: TranslateService,
    ) {
    }

    public ngOnInit(): void {
        this.navigationDest = this.$transition$.to();
        this.einschulungTypValues = getTSEinschulungTypValues();
        this.initProperties();
    }

    public getKonfigKontingentierungString(gk: TSGemeindeKonfiguration): string {
        const kontStr = gk.konfigKontingentierung ? this.translate.instant('KONTINGENTIERUNG') :
            'Keine ' + this.translate.instant('KONTINGENTIERUNG');
        return kontStr;
    }

    public changeKonfigKontingentierung(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen.forEach(property => {
            if (TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED === property.key) {
                property.value = gk.konfigKontingentierung ? 'true' : 'false';
            }
        });
    }

    public getKonfigBeguBisUndMitSchulstufeString(gk: TSGemeindeKonfiguration): string {
        const bgBisStr = this.translate.instant(gk.konfigBeguBisUndMitSchulstufe.toString());
        return bgBisStr;
    }

    public changeKonfigBeguBisUndMitSchulstufe(gk: TSGemeindeKonfiguration): void {
        gk.konfigurationen.forEach(property => {
            if (TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE === property.key) {
                property.value = gk.konfigBeguBisUndMitSchulstufe;
            }
        });
    }

    public isKonfigurationEditable(stammdaten: TSGemeindeStammdaten, gk: TSGemeindeKonfiguration): boolean {

        return 'gemeinde.edit' === this.navigationDest.name &&
            (TSGemeindeStatus.EINGELADEN === stammdaten.gemeinde.status
            || TSGesuchsperiodeStatus.ENTWURF === gk.gesuchsperiodeStatus);
    }

    private initProperties(): void {
        this.stammdaten.konfigurationsListe.forEach(config => {
            config.konfigBeguBisUndMitSchulstufe = TSEinschulungTyp.KINDERGARTEN2;
            config.konfigKontingentierung = false;
            config.konfigurationen.forEach(property => {
                if (TSEinstellungKey.GEMEINDE_BG_BIS_UND_MIT_SCHULSTUFE === property.key) {
                    config.konfigBeguBisUndMitSchulstufe = (TSEinschulungTyp as any)[property.value];
                }
                if (TSEinstellungKey.GEMEINDE_KONTINGENTIERUNG_ENABLED === property.key) {
                    config.konfigKontingentierung = (property.value === 'true');
                }
            });
        });
    }

}
