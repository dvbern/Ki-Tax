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

import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    Input,
    OnChanges,
    OnInit,
    SimpleChanges
} from '@angular/core';
import {ControlContainer, NgForm} from '@angular/forms';
import {TranslateService} from '@ngx-translate/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSBetreuungsangebotTyp} from '../../../models/enums/TSBetreuungsangebotTyp';
import {TSAdresse} from '../../../models/TSAdresse';
import {TSInstitutionStammdaten} from '../../../models/TSInstitutionStammdaten';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {CONSTANTS} from '../../core/constants/CONSTANTS';
import {ApplicationPropertyRS} from '../../core/rest-services/applicationPropertyRS.rest';

@Component({
    selector: 'dv-edit-institution-betreuungsgutscheine',
    templateUrl: './edit-institution-betreuungsgutscheine.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    styleUrls: ['./edit-institution-betreuungsgutscheine.component.less'],
    viewProviders: [{provide: ControlContainer, useExisting: NgForm}],
})

export class EditInstitutionBetreuungsgutscheineComponent implements OnInit, OnChanges {

    @Input() public stammdaten: TSInstitutionStammdaten;
    @Input() public editMode: boolean;

    public abweichendeZahlungsAdresse: boolean;
    public incompleteOeffnungszeiten: boolean = false;
    public isInfomazahlungen: boolean = false;
    public zusatzinformationenInstitution: boolean;

    public readonly CONSTANTS = CONSTANTS;

    public constructor(
        private readonly translate: TranslateService,
        private readonly authServiceRS: AuthServiceRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly cd: ChangeDetectorRef
    ) {
    }

    //
    public ngOnInit(): void {
        const stammdatenBg = this.stammdaten.institutionStammdatenBetreuungsgutscheine;
        this.abweichendeZahlungsAdresse = stammdatenBg && !!stammdatenBg.adresseKontoinhaber;
        this.applicationPropertyRS.getPublicPropertiesCached().then(res => {
            this.isInfomazahlungen = res.infomaZahlungen;
            this.cd.markForCheck();
        });
        this.applicationPropertyRS.getZusatzinformationenInstitutionEnabled().then(enabled => {
            this.zusatzinformationenInstitution = enabled;
            this.cd.markForCheck();
        });
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.editMode) {
            this.initIncompleteOeffnungszeiten();
        }
    }

    public onPrePersist(): void {
        if (!this.abweichendeZahlungsAdresse) { // Reset Adresse Kontoinhaber if not used
            this.stammdaten.institutionStammdatenBetreuungsgutscheine.adresseKontoinhaber = undefined;
        }
    }

    public onAbweichendeZahlungsAdresseClick(): void {
        if (!this.stammdaten.institutionStammdatenBetreuungsgutscheine.adresseKontoinhaber) {
            this.stammdaten.institutionStammdatenBetreuungsgutscheine.adresseKontoinhaber = new TSAdresse();
        }
    }

    public getPlaceholderForPlaetze(): string {
        if (this.stammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.KITA) {
            return this.translate.instant('INSTITUTION_ANZAHL_PLAETZE_PLACEHOLDER_1');
        }
        if (this.stammdaten.betreuungsangebotTyp === TSBetreuungsangebotTyp.TAGESFAMILIEN) {
            return this.translate.instant('INSTITUTION_ANZAHL_PLAETZE_PLACEHOLDER_2');
        }
        return '';
    }

    public getAlterskategorien(): string {
        const alterskategorien: string[] = [];
        if (this.stammdaten.institutionStammdatenBetreuungsgutscheine.alterskategorieBaby) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_BABY'));
        }
        if (this.stammdaten.institutionStammdatenBetreuungsgutscheine.alterskategorieVorschule) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_VORSCHULE'));
        }
        if (this.stammdaten.institutionStammdatenBetreuungsgutscheine.alterskategorieKindergarten) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_KINDERGARTEN'));
        }
        if (this.stammdaten.institutionStammdatenBetreuungsgutscheine.alterskategorieSchule) {
            alterskategorien.push(this.translate.instant('INSTITUTION_ALTERSKATEGORIE_SCHULE'));
        }
        return alterskategorien.join(', ');
    }

    public getOeffnungsTageAbkuerzungListe(): string {
        return this.stammdaten.institutionStammdatenBetreuungsgutscheine.oeffnungstage
            .getActiveDaysAsList()
            .map(day => {
                return this.translate.instant(`${day}_SHORT`);
            })
            .join(', ');
    }

    private initIncompleteOeffnungszeiten(): void {
        const stammdatenBg = this.stammdaten.institutionStammdatenBetreuungsgutscheine;
        this.incompleteOeffnungszeiten = stammdatenBg && (!stammdatenBg.offenVon || !stammdatenBg.offenBis);
    }

    public showInfomaFields(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles())
        && this.isInfomazahlungen;
    }
}
