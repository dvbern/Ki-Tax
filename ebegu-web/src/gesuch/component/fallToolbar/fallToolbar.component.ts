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
import {IPromise} from 'angular';
import {from as fromPromise, from, Observable, of} from 'rxjs';
import {filter, switchMap, map} from 'rxjs/operators';
import {DvNgGemeindeDialogComponent} from '../../../app/core/component/dv-ng-gemeinde-dialog/dv-ng-gemeinde-dialog.component';
import {Log, LogFactory} from '../../../app/core/logging/LogFactory';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSCreationAction} from '../../../models/enums/TSCreationAction';
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

const LOG = LogFactory.createLog('FallToolbarComponent');

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
    @Input() mobileMode?: boolean = false;

    public dossierList: TSDossier[] = [];
    selectedDossier?: TSDossier;
    fallNummer: string;
    private availableGemeindeList: TSGemeinde[] = [];
    gemeindeText: string;
    showdropdown: boolean = false;

    constructor(private readonly dossierRS: DossierRS,
                private readonly dialog: MatDialog,
                private readonly gemeindeRS: GemeindeRS,
                private readonly $state: StateService,
                private readonly gesuchRS: GesuchRS,
                private readonly authServiceRS: AuthServiceRS) {
    }

    ngOnInit(): void {
        this.loadObjects(); // todo fragen it gets called twice!! ngChanges. siehe Kommentar unten
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
    public openDossier$(dossier: TSDossier): Observable<TSDossier> {
        if (dossier) {
            if (this.isGesuchsteller()) {
                this.selectedDossier = dossier;
                this.navigateToDashboard();
            } else {
                return this.openNewestGesuchOfDossier$(dossier);
            }
        }
        return of(this.selectedDossier);
    }

    private openNewestGesuchOfDossier$(dossier: TSDossier): Observable<TSDossier> {
        return fromPromise(this.gesuchRS.getIdOfNewestGesuchForDossier(dossier.id).then(newestGesuchID => {
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
        }));
    }

    public createNewDossier(): void {
        this.getGemeindeIDFromDialog$()
            .pipe(filter(chosenGemeindeId => !!chosenGemeindeId))
            .subscribe(
                chosenGemeindeId => {
                    if (this.isGesuchsteller()) {
                        this.createDossier(chosenGemeindeId).then(() => {
                            this.navigateToDashboard();
                        });
                    } else {
                        this.navigateToFallCreation(chosenGemeindeId);
                    }
                }
            );
    }

    /**
     * Creates a new Dossier based on the selectedDossier (which must always be defined at this point) but with
     * the gemeinde given as param.
     */
    private createDossier(chosenGemeindeId: string): IPromise<TSDossier> {
        const newDossier = new TSDossier();
        newDossier.fall = this.selectedDossier.fall;
        newDossier.gemeinde = this.availableGemeindeList.find(gemeinde => gemeinde.id === chosenGemeindeId);
        return this.dossierRS.createDossier(newDossier).then(() => {
            this.selectedDossier = newDossier;
            return this.selectedDossier;
        });
    }

    private navigateToDashboard(): void {
        this.$state.go('gesuchsteller.dashboard', {
            dossierId: this.selectedDossier.id
        });
    }

    private navigateToFallCreation(chosenGemeindeId: string): void {
        const params: INewFallStateParams = {
            gesuchsperiodeId: null,
            creationAction: TSCreationAction.CREATE_NEW_DOSSIER,
            gesuchId: null,
            dossierId: null,
            gemeindeId: chosenGemeindeId,
            eingangsart: this.getEingangsArt(),
        };
        this.$state.go('gesuch.fallcreation', params);
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
        this.authServiceRS.principal$
            .pipe(
                switchMap(principal => {
                    if (principal && TSRoleUtil.isGemeindeabhaengig(principal.getCurrentRole())) {
                        return of(principal.extractCurrentGemeinden());
                    }

                    return from(this.gemeindeRS.getAllGemeinden());
                }),
                map(gemeinden => this.toGemeindenWithoutDossier(gemeinden))
            )
            .subscribe(
                gemeinden => {
                    this.availableGemeindeList = gemeinden;
                },
                err => LOG.error(err)
            );
    }

    /**
     * Takes the list of availableGemenden and removes all Gemeinden for which the fall already has a
     * Dossier.
     */
    private toGemeindenWithoutDossier(gemeinden: TSGemeinde[]): TSGemeinde[] {
        return gemeinden.filter(g => !this.dossierList.some(d => d.gemeinde.id === g.id));
    }

    /**
     * A dialog will always be displayed when creating a new Dossier. So that the user
     */
    private getGemeindeIDFromDialog$(): Observable<string> {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.data = {
            gemeindeList: this.availableGemeindeList
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
     * It removes all existing NewDossierToCreate from the list. When a currentDossier comes we need to remove all
     * existing ones that haven't been saved yet because only one new dossier can be created at a time
     */
    private removeAllExistingNewDossierToCreate() {
        this.dossierList = this.dossierList
            .filter(dossier => !dossier.isNew());
    }

    private emptyDossierList() {
        this.dossierList = [];
    }

    /**
     * Navigation will always be disabled when any dossier is new. This solves a lot of problems that arrive when the
     * user leaves the "opened" but not yet existing dossier.
     */
    public isNavigationDisabled(): boolean {
        return this.dossierList.some(dossier => dossier.isNew());
    }

    public isGesuchsteller(): boolean {
        return this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
    }

    /**
     * Only for a Gesuchsteller it introduces Text in the variable gemeindeText when it is not defined or sets it to
     * undefined when it already has a text
     */
    public toggleGemeindeText(): void {
        if (this.isGesuchsteller()) {
            this.gemeindeText = this.gemeindeText ? undefined : 'GEMEINDE_HINZUFUEGEN';
        }
    }

    public showAddGemeindeText(): boolean {
        return !!this.gemeindeText;
    }

    public getCurrentGemeindeName(): string {
        if (this.currentDossier) {
            return this.currentDossier.extractGemeindeName();
        }
        return '';
    }

    public getDossierListWithoutSelected(): TSDossier[] {
        return this.dossierList.filter(obj => obj !== this.selectedDossier);
    }
}
