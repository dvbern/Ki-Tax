/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2018 City of Bern Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {MatDialog, MatDialogConfig} from '@angular/material';
import {StateService} from '@uirouter/core';
import {Observable, of} from 'rxjs';
import {DvNgGemeindeDialogComponent} from '../../../app/core/component/dv-ng-gemeinde-dialog/dv-ng-gemeinde-dialog.component';
import {Log, LogFactory} from '../../../app/core/logging/LogFactory';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {getTSEingangsartFromRole} from '../../../models/enums/TSEingangsart';
import {TSRole} from '../../../models/enums/TSRole';
import TSDossier from '../../../models/TSDossier';
import TSGemeinde from '../../../models/TSGemeinde';
import EbeguUtil from '../../../utils/EbeguUtil';
import {NavigationUtil} from '../../../utils/NavigationUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {INewFallStateParams} from '../../gesuch.route';
import DossierRS from '../../service/dossierRS.rest';
import GemeindeRS from '../../service/gemeindeRS.rest';
import GesuchRS from '../../service/gesuchRS.rest';

@Component({
    selector: 'dv-fall-toolbar',
    templateUrl: './fallToolbar.template.html',
    styleUrls: ['./fallToolbar.less'],
})
export class FallToolbarComponent implements OnInit, OnChanges {

    private readonly LOG: Log = LogFactory.createLog(FallToolbarComponent.name);

    TSRoleUtil: any = TSRoleUtil;

    @Input() fallId: string;
    @Input() dossierId: string;
    @Input() defaultGemeindeName: string;

    dossierList: TSDossier[] = [];
    selectedDossier?: TSDossier;
    fallNummer: string;
    availableGemeindeList: TSGemeinde[] = [];


    constructor(private dossierRS: DossierRS,
            private dialog: MatDialog,
            private gemeindeRS: GemeindeRS,
            private $state: StateService,
            private gesuchRS: GesuchRS,
            private authServiceRS: AuthServiceRS) {
    }

    ngOnInit(): void {
        this.loadObjects();
    }

    private loadObjects() {
        if (!this.useDefaultValues()) {
            this.dossierRS.findDossiersByFall(this.fallId).then(dossiers => {
                this.dossierList = dossiers;
                this.setSelectedDossier();
                this.retrieveListOfAvailableGemeinden();
            });
        }
    }

    ngOnChanges(changes: any) {
        if (changes['fallId'] || changes['dossierId']) {
            this.loadObjects();
        }
    }

    public useDefaultValues(): boolean {
        return !this.dossierId && !this.fallId;
    }

    private setSelectedDossier() {
        this.selectedDossier = this.dossierList.find(dossier => dossier.id === this.dossierId);
        this.calculateFallNummer();
    }

    private isOnlineGesuch(): boolean {
        return this.selectedDossier
            && this.selectedDossier.fall
            && !EbeguUtil.isNullOrUndefined(this.selectedDossier.fall.besitzer);
    }

    private calculateFallNummer(): void {
        if (this.selectedDossier && this.selectedDossier.fall) {
            this.fallNummer = EbeguUtil.addZerosToFallNummer(this.selectedDossier.fall.fallNummer);
        }
    }

    /**
     * Opens a dossier
     * If it is undefined it doesn't do anything
     */
    public openDossier(dossier: TSDossier): void {
        if (dossier) {
            this.gesuchRS.getIdOfNewestGesuchForDossier(dossier.id).then(newestGesuchID => {
                if (newestGesuchID) {
                    this.selectedDossier = dossier;
                    NavigationUtil.navigateToStartsiteOfGesuchForRole(
                        this.authServiceRS.getPrincipalRole(),
                        this.$state,
                        newestGesuchID,
                    );
                } else {
                    this.LOG.warn(`newestGesuchID in method FallToolbarComponent#openDossier for dossier ${dossier.id} is undefined`);
                }
            });
        }
    }

    public createNewDossier(): void {
        this.getGemeindeIDFromDialog().subscribe(
            (chosenGemeindeId) => {
                if (chosenGemeindeId) {
                    const params: INewFallStateParams = {
                        gesuchsperiodeId: null,
                        createMutation: null,
                        createNewFall: 'false',
                        createNewDossier: 'true',
                        gesuchId: null,
                        dossierId: null,
                        gemeindeId: chosenGemeindeId,
                        eingangsart: this.getEingangsArt(),
                    };
                    this.$state.go('gesuch.fallcreation', params);
                }
            }
        );
    }

    private getEingangsArt() {
        return getTSEingangsartFromRole(this.authServiceRS.getPrincipalRole());
    }

    /**
     * For all roles that depend on a Gemeinde we retrieve those Gemeinden available for the user
     * For all roles that don't depend on a Gemeinde we retrieve all Gemeinden.
     * At the end all Gemeinden for which the fall already has a Dossier are removed from the list
     * so that in the end the list only contains those Gemeinden that are still available for new Dossiers.
     */
    private retrieveListOfAvailableGemeinden(): void {
        if (TSRoleUtil.isGemeindeabhaengig(this.authServiceRS.getPrincipalRole())) {
            this.availableGemeindeList = this.authServiceRS.getPrincipal().extractCurrentGemeinden();
            this.cleanGemeindenList();
        } else {
            this.gemeindeRS.getAllGemeinden().then((value: TSGemeinde[]) => {
                this.availableGemeindeList = value;
                this.cleanGemeindenList();
            });
        }
    }

    /**
     * Takes the list of availableGemenden and removes all Gemeinden for which the fall already has a
     * Dossier.
     */
    private cleanGemeindenList(): void {
        this.dossierList.forEach(dossier => {
            this.availableGemeindeList = this.availableGemeindeList.filter(gemeinde =>
                gemeinde.id !== dossier.gemeinde.id
            );
        });
    }

    /**
     * A dialog will always be displayed when creating a new Dossier. So that the user
     */
    private getGemeindeIDFromDialog(): Observable<string> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.disableClose = false; // dialog is canceled by clicking outside
        dialogConfig.autoFocus = true;
        dialogConfig.data = {
            gemeindeList: of(this.availableGemeindeList)
        };

        return this.dialog.open(DvNgGemeindeDialogComponent, dialogConfig).afterClosed();
    }

    public isDossierActive(dossier: TSDossier): boolean {
        return !EbeguUtil.isNullOrUndefined(this.selectedDossier)
            && !EbeguUtil.isNullOrUndefined(dossier)
            && this.selectedDossier.id === dossier.id;
    }

    public showCreateNewDossier(): boolean {
        return !this.useDefaultValues()
            && (!this.isOnlineGesuch() === !this.authServiceRS.isRole(TSRole.GESUCHSTELLER))
            && this.availableGemeindeList.length !== 0;
    }
}
