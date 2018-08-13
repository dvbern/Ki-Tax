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
import {from as fromPromise, Observable, of} from 'rxjs';
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
    @Input() currentDossier: TSDossier;

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
        // this.loadObjects();  --> it is called in ngOnChanges anyway. otherwise it gets called twice
    }

    private loadObjects() {
        if (this.fallId) {
            this.dossierRS.findDossiersByFall(this.fallId).then(dossiers => {
                this.dossierList = dossiers;
                this.setSelectedDossier();
                this.addNewDossierToCreateToDossiersList();
                this.retrieveListOfAvailableGemeinden();
            });
        } else {
            this.emptyDossierList(); // if there is no fall there cannot be any dossier
            this.addNewDossierToCreateToDossiersList(); // only a new dossier can be added to a not yet created fall
        }
    }

    /**
     * In case a currentDossier exists and it is new and it is not already contained in the list then we add it
     */
    private addNewDossierToCreateToDossiersList() {
        if (this.currentDossier && this.currentDossier.isNew() && !this.dossierList.includes(this.currentDossier)) {
            this.removeAllExistingNewDossierToCreate();
            this.dossierList.push(this.currentDossier);
            this.selectedDossier = this.dossierList[this.dossierList.length - 1];
        }
    }

    ngOnChanges(changes: any) {
        if (changes['fallId'] || changes['dossierId'] || changes['currentDossier']) {
            this.loadObjects();
        }
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
    public openDossier(dossier: TSDossier): Observable<TSDossier> {
        if (dossier) {
            return fromPromise(
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
                    return this.selectedDossier;
                })
            );
        }
        return of(this.selectedDossier);
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
        return !this.isOnlineGesuch() === !this.authServiceRS.isRole(TSRole.GESUCHSTELLER)
            && this.availableGemeindeList.length !== 0;
    }

    /**
     * It removes all existing NewDossierToCreate from the list. When a currentDossier comes we need to remove all existing ones
     * that haven't been saved yet because only one new dossier can be created at a time
     */
    private removeAllExistingNewDossierToCreate() {
        this.dossierList = this.dossierList
            .filter(dossier => !dossier.isNew());
    }

    private emptyDossierList() {
        this.dossierList = [];
    }

    /**
     * Navigation will always be disabled when any dossier is new. This solves a lot of problems that arrive when the user leaves
     * the "opened" but not yet existing dossier.
     */
    public isNavigationDisabled(): boolean {
        return this.dossierList.some(dossier => dossier.isNew());
    }
}
