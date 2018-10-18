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

import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {StateService, Transition} from '@uirouter/core';
import {EinstellungRS} from '../../../admin/service/einstellungRS.rest';
import GemeindeRS from '../../../gesuch/service/gemeindeRS.rest';
import {getTSEinschulungTypValues, TSEinschulungTyp} from '../../../models/enums/TSEinschulungTyp';
import TSBenutzer from '../../../models/TSBenutzer';
import TSEinstellung from '../../../models/TSEinstellung';
import TSGemeindeKonfiguration from '../../../models/TSGemeindeKonfiguration';
import TSGemeindeStammdaten from '../../../models/TSGemeindeStammdaten';
import ErrorService from '../../core/errors/service/ErrorService';

@Component({
    selector: 'dv-view-gemeinde',
    templateUrl: './view-gemeinde.component.html',
    styleUrls: ['../gemeinde-module.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ViewGemeindeComponent implements OnInit {
    @ViewChild(NgForm) public form: NgForm;

    public stammdaten: TSGemeindeStammdaten;
    public konfigurationList: Array<TSGemeindeKonfiguration>;
    public korrespondenzsprache: string;
    public kontinggentierung: string;
    public beguStart: string;
    public einschulungTypValues: Array<TSEinschulungTyp>;
    private fileToUpload!: File;
    public logoImageUrl: string = '#';

    public constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly translate: TranslateService,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private readonly errorService: ErrorService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly einstellungRS: EinstellungRS,
    ) {
    }

    public ngOnInit(): void {
        const gemeindeId: string = this.$transition$.params().gemeindeId;
        if (!gemeindeId) {
            return;
        }
        // TODO: Task KIBON-217: Load from DB
        this.logoImageUrl = 'https://upload.wikimedia.org/wikipedia/commons/e/e8/Ostermundigen-coat_of_arms.svg';
        this.einschulungTypValues = getTSEinschulungTypValues();

        this.initTestKonfigurationen();

        this.gemeindeRS.getGemeindeStammdaten(gemeindeId).then(resStamm => {
            // TODO: GemeindeStammdaten über ein Observable laden, so entfällt changeDetectorRef.markForCheck(), siehe
            this.stammdaten = resStamm;

            this.initStrings();

            this.changeDetectorRef.markForCheck();
        });
    }

    public cancel(): void {
        this.navigateBack();
    }

    public persistGemeindeStammdaten(): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        this.gemeindeRS.saveGemeindeStammdaten(this.stammdaten).then(response => {
            this.stammdaten = response;
            this.navigateBack();
        });
    }

    public mitarbeiterBearbeiten(): void {
        // TODO: Implement Mitarbeiter Bearbeiten Button Action
    }

    public editGemeindeStammdaten(): void {
        this.$state.go('gemeinde.edit', {gemeindeId: this.stammdaten.gemeinde.id});
    }

    public compareBenutzer(b1: TSBenutzer, b2: TSBenutzer): boolean {
        return b1 && b2 ? b1.username === b2.username : b1 === b2;
    }

    public handleLogoUpload(files: FileList): void {
        this.fileToUpload = files[0];
        const tmpFileReader = new FileReader();
        tmpFileReader.onload = (e: any): void => {
            this.logoImageUrl = e.target.result;
        };
        tmpFileReader.readAsDataURL(this.fileToUpload);
    }

    private initStrings(): void {
        this.beguStart = this.stammdaten.gemeinde.betreuungsgutscheineStartdatum.format('DD.MM.YYYY');
        this.kontinggentierung = 'Keine ' + this.translate.instant('KONTINGENTIERUNG');
        if (this.stammdaten.kontingentierung) {
            this.kontinggentierung = this.translate.instant('KONTINGENTIERUNG');
        }
        if (this.stammdaten.korrespondenzspracheDe) {
            this.korrespondenzsprache = this.translate.instant('DEUTSCH');
        }
        if (!this.stammdaten.korrespondenzspracheFr) {
            return;
        }
        if (this.korrespondenzsprache.length > 0) {
            this.korrespondenzsprache += ', ';
        }
        this.korrespondenzsprache += this.translate.instant('FRANZOESISCH');
    }

    private initTestKonfigurationen(): void {
        this.konfigurationList = new Array<TSGemeindeKonfiguration>();

        let gk = new TSGemeindeKonfiguration();
        gk.id = 1;
        gk.gesuchsperiode = '01.08.2017 - 31.07.2018';
        gk.kontingentierung = 'Keine Kontingentierung';
        gk.beguBisUndMitSchulstufe = TSEinschulungTyp.KINDERGARTEN1;
        this.konfigurationList.push(gk);

        gk.id = 2;
        gk = new TSGemeindeKonfiguration();
        gk.gesuchsperiode = '01.08.2018 - 31.07.2019';
        gk.kontingentierung = 'Kontingentierung';
        gk.beguBisUndMitSchulstufe = TSEinschulungTyp.KINDERGARTEN2;
        this.konfigurationList.push(gk);

        gk.id = 3;
        gk = new TSGemeindeKonfiguration();
        gk.gesuchsperiode = '01.08.2019 - 31.07.2020';
        gk.kontingentierung = 'Keine Kontingentierung';
        gk.beguBisUndMitSchulstufe = TSEinschulungTyp.KLASSE1;
        this.konfigurationList.push(gk);
    }

    private navigateBack(): void {
        this.$state.go('gemeinde.list');
    }
}
