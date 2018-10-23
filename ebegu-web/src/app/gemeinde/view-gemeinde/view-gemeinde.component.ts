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

import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {getTSEinschulungTypValues, TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import {TSEinstellungKey} from '../../../models/enums/TSEinstellungKey';
import TSBenutzer from '../../../models/TSBenutzer';
import TSGemeindeKonfiguration from '../../../models/TSGemeindeKonfiguration';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import ErrorService from '../../core/errors/service/ErrorService';

@Component({
    selector: 'dv-view-gemeinde',
    templateUrl: './view-gemeinde.component.html',
    styleUrls: ['../gemeinde-module.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ViewGemeindeComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;

    public stammdaten$: Observable<TSGemeindeStammdaten>;
    public keineBeschwerdeAdresse: boolean;
    public korrespondenzsprache: string;
    public beguStart: string;
    public einschulungTypValues: Array<TSEinschulungTyp>;
    private fileToUpload!: File;
    public logoImageUrl: string = '#';
    private gemeindeId: string;

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly translate: TranslateService,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly authServiceRS: AuthServiceRS,
    ) {
    }

    public ngOnInit(): void {
        this.gemeindeId = this.$transition$.params().gemeindeId;
        if (!this.gemeindeId) {
            return;
        }
        // TODO: Task KIBON-217: Load from DB
        this.logoImageUrl = 'https://upload.wikimedia.org/wikipedia/commons/e/e8/Ostermundigen-coat_of_arms.svg';
        this.einschulungTypValues = getTSEinschulungTypValues();

        this.stammdaten$ = from(
            this.gemeindeRS.getGemeindeStammdaten(this.gemeindeId).then(stammdaten => {
                this.initProperties(stammdaten);
                this.initStrings(stammdaten);
                return stammdaten;
            }));
    }

    public mitarbeiterBearbeiten(): void {
        // TODO: Implement Mitarbeiter Bearbeiten Button Action
    }

    public editGemeindeStammdaten(): void {
        this.$state.go('gemeinde.edit', {gemeindeId: this.gemeindeId});
    }

    public isStammdatenEditable(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorBgTsGemeindeRole());
    }

    public compareBenutzer(b1: TSBenutzer, b2: TSBenutzer): boolean {
        return b1 && b2 ? b1.username === b2.username : b1 === b2;
    }

    public handleLogoUpload(files: FileList): void {
    // todo KIBON-217 auslagern??? es ist in edit-gemeinde dupliziert
        this.fileToUpload = files[0];
        const tmpFileReader = new FileReader();
        tmpFileReader.onload = (e: any): void => {
            this.logoImageUrl = e.target.result;
        };
        tmpFileReader.readAsDataURL(this.fileToUpload);
    }

    private initProperties(stammdaten: TSGemeindeStammdaten): void {
        this.keineBeschwerdeAdresse = stammdaten.beschwerdeAdresse ? false : true;
        stammdaten.konfigurationsListe.forEach(config => {
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

    private initStrings(stammdaten: TSGemeindeStammdaten): void {
        this.beguStart = stammdaten.gemeinde.betreuungsgutscheineStartdatum.format('DD.MM.YYYY');

        if (stammdaten.korrespondenzspracheDe) {
            this.korrespondenzsprache = this.translate.instant('DEUTSCH');
        }
        if (!stammdaten.korrespondenzspracheFr) {
            return;
        }
        if (this.korrespondenzsprache.length > 0) {
            this.korrespondenzsprache += ', ';
        }
        this.korrespondenzsprache += this.translate.instant('FRANZOESISCH');
    }

    public getKonfigKontingentierungString(gk: TSGemeindeKonfiguration): string {
        const kontStr = gk.konfigKontingentierung ? this.translate.instant('KONTINGENTIERUNG') :
            'Keine ' + this.translate.instant('KONTINGENTIERUNG');
        return kontStr;
    }

    public getKonfigBeguBisUndMitSchulstufeString(gk: TSGemeindeKonfiguration): string {
        const bgBisStr = this.translate.instant(gk.konfigBeguBisUndMitSchulstufe.toString());
        return bgBisStr;
    }

    public isKonfigurationEditable(stammdaten: TSGemeindeStammdaten, gk: TSGemeindeKonfiguration): boolean {
        return false;
    }

    public cancel(): void {
        this.navigateBack();
    }

    private navigateBack(): void {
        this.$state.go('gemeinde.list');
    }
}
