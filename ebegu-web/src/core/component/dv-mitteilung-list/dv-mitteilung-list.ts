/*
 * Ki-Tax: System for the management of external childcare subsidies
 * Copyright (C) 2017 City of Bern Switzerland
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

import {IComponentOptions, IPromise} from 'angular';
import {StateService} from '@uirouter/core';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../../gesuch/dialog/RemoveDialogController';
import FallRS from '../../../gesuch/service/fallRS.rest';
import GesuchModelManager from '../../../gesuch/service/gesuchModelManager';
import {IMitteilungenStateParams} from '../../../mitteilungen/mitteilungen.route';
import {TSAmt} from '../../../models/enums/TSAmt';
import {TSMitteilungEvent} from '../../../models/enums/TSMitteilungEvent';
import {TSMitteilungStatus} from '../../../models/enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from '../../../models/enums/TSMitteilungTeilnehmerTyp';
import {TSRole} from '../../../models/enums/TSRole';
import TSBetreuung from '../../../models/TSBetreuung';
import TSBetreuungsmitteilung from '../../../models/TSBetreuungsmitteilung';
import TSFall from '../../../models/TSFall';
import TSMitteilung from '../../../models/TSMitteilung';
import TSUser from '../../../models/TSUser';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import BetreuungRS from '../../service/betreuungRS.rest';
import MitteilungRS from '../../service/mitteilungRS.rest';
import IFormController = angular.IFormController;
import IQService = angular.IQService;
import IRootScopeService = angular.IRootScopeService;
import IScope = angular.IScope;
import IWindowService = angular.IWindowService;
import ITimeoutService = angular.ITimeoutService;

let template = require('./dv-mitteilung-list.html');
require('./dv-mitteilung-list.less');
let removeDialogTemplate = require('../../../gesuch/dialog/removeDialogTemplate.html');

export class DVMitteilungListConfig implements IComponentOptions {
    transclude = false;

    bindings: any = {
        fall: '<',
        betreuung: '<',
        form: '<',
    };

    template = template;
    controller = DVMitteilungListController;
    controllerAs = 'vm';
}

export class DVMitteilungListController {

    fall: TSFall;
    betreuung: TSBetreuung;
    form: IFormController;

    paramSelectedMitteilungId: string;
    currentMitteilung: TSMitteilung;
    allMitteilungen: Array<TSMitteilung>;
    TSRole: any;
    TSRoleUtil: any;
    ebeguUtil: EbeguUtil;

    static $inject: any[] = ['$stateParams', 'MitteilungRS', 'AuthServiceRS', 'FallRS', 'BetreuungRS',
        '$q', '$window', '$rootScope', '$state', 'EbeguUtil', 'DvDialog', 'GesuchModelManager', '$scope', '$timeout'];
    /* @ngInject */
    constructor(private $stateParams: IMitteilungenStateParams, private mitteilungRS: MitteilungRS,
                private authServiceRS: AuthServiceRS,
                private fallRS: FallRS, private betreuungRS: BetreuungRS, private $q: IQService,
                private $window: IWindowService,
                private $rootScope: IRootScopeService, private $state: StateService, ebeguUtil: EbeguUtil,
                private DvDialog: DvDialog,
                private gesuchModelManager: GesuchModelManager, private $scope: IScope,
                private $timeout: ITimeoutService) {
        this.TSRole = TSRole;
        this.TSRoleUtil = TSRoleUtil;
        this.ebeguUtil = ebeguUtil;
    }

    $onInit() {
        if (this.$stateParams.mitteilungId) {
            // wenn man eine bestimmte Mitteilung oeffnen will, kann man ihr ID als parameter geben
            this.paramSelectedMitteilungId = this.$stateParams.mitteilungId;
        }
        if (this.$stateParams.fallId) {
            this.fallRS.findFall(this.$stateParams.fallId).then((response) => {
                this.fall = response;
                if (this.$stateParams.betreuungId) {
                    this.betreuungRS.findBetreuung(this.$stateParams.betreuungId).then((response) => {
                        this.betreuung = response;
                        this.loadEntwurf();
                        this.loadAllMitteilungen();
                    });
                } else {
                    this.loadEntwurf();
                    // Wenn JA oder Institution -> Neue Mitteilungen als gelesen markieren
                    if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerJugendamtSchulamtRoles())) {
                        this.setAllMitteilungenGelesen().then((response) => {
                            this.loadAllMitteilungen();
                            if (this.$rootScope) {
                                this.$rootScope.$emit('POSTEINGANG_MAY_CHANGED', null);
                            }
                        });
                    } else {
                        // Fuer Revisor und Jurist: Nur laden
                        this.loadAllMitteilungen();
                    }
                }
            });
        }
        this.$scope.$on(TSMitteilungEvent[TSMitteilungEvent.MUTATIONSMITTEILUNG_MUTATION_REMOVED], () => {
            this.loadAllMitteilungen();
        });
    }

    public cancel(): void {
        this.form.$setPristine();
        this.$window.history.back();
    }

    /**
     * Diese Methode laedt einen Entwurf wenn es einen existiert. Sonst gibt sie eine leeren
     * Mitteilung zurueck.
     */
    private loadEntwurf() {
        // Wenn der Fall keinen Besitzer hat, darf auch keine Nachricht geschrieben werden
        // Ausser wir sind Institutionsbenutzer
        let isGesuchsteller: boolean = this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
        let isJugendamtOrSchulamtAndFallHasBesitzer: boolean = this.fall.besitzer && this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorJugendamtSchulamtRoles());
        let isInstitutionsUser: boolean = this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles());
        if (isGesuchsteller || isJugendamtOrSchulamtAndFallHasBesitzer || isInstitutionsUser) {
            if (this.betreuung) {
                this.mitteilungRS.getEntwurfForCurrentRolleForBetreuung(this.betreuung.id).then((entwurf: TSMitteilung) => {
                    if (entwurf) {
                        this.currentMitteilung = entwurf;
                    } else {
                        this.initMitteilungForCurrentBenutzer();
                    }
                });
            } else {
                this.mitteilungRS.getEntwurfForCurrentRolleForFall(this.fall.id).then((entwurf: TSMitteilung) => {
                    if (entwurf) {
                        this.currentMitteilung = entwurf;
                    } else {
                        this.initMitteilungForCurrentBenutzer();
                    }
                });
            }
        }
    }

    private initMitteilungForCurrentBenutzer() {
        let currentUser: TSUser = this.authServiceRS.getPrincipal();

        //common attributes
        this.currentMitteilung = new TSMitteilung();
        this.currentMitteilung.fall = this.fall;
        if (this.betreuung) {
            this.currentMitteilung.betreuung = this.betreuung;
        }
        this.currentMitteilung.mitteilungStatus = TSMitteilungStatus.ENTWURF;
        this.currentMitteilung.sender = currentUser;
    }

    public getCurrentMitteilung(): TSMitteilung {
        return this.currentMitteilung;
    }

    /**
     * Speichert die aktuelle Mitteilung als gesendet.
     */
    public sendMitteilung(): IPromise<TSMitteilung> {
        if (this.form.$invalid) {
            EbeguUtil.selectFirstInvalid();
            return undefined;
        }
        if (!this.isMitteilungEmpty()) {
            return this.mitteilungRS.sendMitteilung(this.getCurrentMitteilung()).then((response) => {
                this.loadEntwurf();
                this.loadAllMitteilungen();
                return this.currentMitteilung;
            }).finally(() => {
                this.form.$setPristine();
                this.form.$setUntouched();
            });
        } else {
            return this.$q.when(this.currentMitteilung);
        }
    }

    /**
     * Speichert die aktuelle Mitteilung nur wenn das formular dirty ist.
     * Wenn das Formular leer ist, wird der Entwurf geloescht (falls er bereits existiert)
     */
    public saveEntwurf(): IPromise<TSMitteilung> {
        if (((this.form.$dirty && !this.isMitteilungEmpty()))) {
            return this.mitteilungRS.saveEntwurf(this.getCurrentMitteilung()).then((response) => {
                this.loadEntwurf();
                this.loadAllMitteilungen();
                return this.currentMitteilung;
            }).finally(() => {
                this.form.$setPristine();
                this.form.$setUntouched();
            });

        } else if (this.isMitteilungEmpty() && !this.currentMitteilung.isNew() && this.currentMitteilung.id) {
            return this.mitteilungRS.removeEntwurf(this.getCurrentMitteilung()).then((response) => {
                this.initMitteilungForCurrentBenutzer();
                return this.currentMitteilung;
            });
        } else {
            return this.$q.when(this.currentMitteilung);
        }
    }

    private isMitteilungEmpty() {
        return (!this.currentMitteilung.message || this.currentMitteilung.message.length <= 0)
            && (!this.currentMitteilung.subject || this.currentMitteilung.subject.length <= 0);
    }

    private loadAllMitteilungen(): void {
        if (this.betreuung) {
            this.mitteilungRS.getMitteilungenForCurrentRolleForBetreuung(this.betreuung.id).then((response) => {
                this.allMitteilungen = response;
            });
        } else {
            this.mitteilungRS.getMitteilungenForCurrentRolleForFall(this.fall.id).then((response) => {
                this.allMitteilungen = response;
            });
        }
    }

    /**
     * Gibt true zurueck wenn der aktuelle BenutzerTyp, der Sender der uebergenenen Mitteilung ist.
     */
    private isCurrentUserTypTheSenderTyp(mitteilung: TSMitteilung): boolean {
        return mitteilung && mitteilung.sender && this.authServiceRS.getPrincipal()
            && mitteilung.senderTyp === this.getMitteilungTeilnehmerTypForUserRole(this.authServiceRS.getPrincipal().role);
    }

    public isSenderTypInstitution(mitteilung: TSMitteilung): boolean {
        return mitteilung && mitteilung.sender && mitteilung.senderTyp === TSMitteilungTeilnehmerTyp.INSTITUTION;
    }

    public isSenderTypSchulamt(mitteilung: TSMitteilung): boolean {
        return mitteilung && mitteilung.sender && mitteilung.senderTyp === TSMitteilungTeilnehmerTyp.JUGENDAMT
            && mitteilung.getSenderAmt() === TSAmt.SCHULAMT;
    }

    public isSenderTypJugendamt(mitteilung: TSMitteilung): boolean {
        return mitteilung && mitteilung.sender && mitteilung.senderTyp === TSMitteilungTeilnehmerTyp.JUGENDAMT
            && mitteilung.getSenderAmt() === TSAmt.JUGENDAMT;
    }

    public isSenderTypGesuchsteller(mitteilung: TSMitteilung): boolean {
        return mitteilung && mitteilung.sender && mitteilung.senderTyp === TSMitteilungTeilnehmerTyp.GESUCHSTELLER;
    }

    private getMitteilungTeilnehmerTypForUserRole(role: TSRole): TSMitteilungTeilnehmerTyp {
        switch (role) {
            case TSRole.GESUCHSTELLER: {
                return TSMitteilungTeilnehmerTyp.GESUCHSTELLER;
            }
            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT: {
                return TSMitteilungTeilnehmerTyp.INSTITUTION;
            }
            case TSRole.SUPER_ADMIN:
            case TSRole.ADMIN:
            case TSRole.JURIST:
            case TSRole.REVISOR:
            case TSRole.SACHBEARBEITER_JA:
            case TSRole.SCHULAMT:
            case TSRole.ADMINISTRATOR_SCHULAMT: {
                return TSMitteilungTeilnehmerTyp.JUGENDAMT;
            }
            default:
                return null;
        }
    }

    private setAllMitteilungenGelesen(): IPromise<Array<TSMitteilung>> {
        return this.mitteilungRS.setAllNewMitteilungenOfFallGelesen(this.fall.id);
    }

    /**
     * Aendert den Status der gegebenen Mitteilung auf ERLEDIGT wenn es GELESEN war oder
     * auf GELESEN wenn es ERLEDIGT war
     */
    public setErledigt(mitteilung: TSMitteilung): void {
        if (mitteilung && mitteilung.mitteilungStatus === TSMitteilungStatus.GELESEN) {
            mitteilung.mitteilungStatus = TSMitteilungStatus.ERLEDIGT;
            this.mitteilungRS.setMitteilungErledigt(mitteilung.id);

        } else if (mitteilung && mitteilung.mitteilungStatus === TSMitteilungStatus.ERLEDIGT) {
            mitteilung.mitteilungStatus = TSMitteilungStatus.GELESEN;
            this.mitteilungRS.setMitteilungGelesen(mitteilung.id);
        }
    }

    public isStatusErledigtGelesen(mitteilung: TSMitteilung): boolean {
        return mitteilung && (mitteilung.mitteilungStatus === TSMitteilungStatus.ERLEDIGT || mitteilung.mitteilungStatus === TSMitteilungStatus.GELESEN);
    }

    public getBgNummer(): string {
        let bgNummer: string = '';
        if (this.betreuung) {
            bgNummer = this.ebeguUtil.calculateBetreuungsId(this.betreuung.gesuchsperiode, this.fall, this.betreuung.kindNummer, this.betreuung.betreuungNummer);
        }
        return bgNummer;
    }

    public betreuungAsString(mitteilung: TSMitteilung): string {
        let betreuungAsString: string;
        if (mitteilung.betreuung) {
            let bgNummer: string = this.ebeguUtil.calculateBetreuungsId(mitteilung.betreuung.gesuchsperiode, mitteilung.fall,
                mitteilung.betreuung.kindNummer, mitteilung.betreuung.betreuungNummer);
            betreuungAsString = mitteilung.betreuung.kindFullname + ', ' + bgNummer;
        }
        return betreuungAsString;
    }

    public gotoBetreuung(mitteilung: TSMitteilung): void {
        this.$state.go('gesuch.betreuung', {
            betreuungNumber: mitteilung.betreuung.betreuungNummer,
            kindNumber: mitteilung.betreuung.kindNummer,
            gesuchId: mitteilung.betreuung.gesuchId
        });
    }

    public isBetreuungsmitteilungApplied(mitteilung: TSMitteilung): boolean {
        return this.isBetreuungsmitteilung(mitteilung) && (<TSBetreuungsmitteilung>mitteilung).applied === true;
    }

    public isBetreuungsmitteilungNotApplied(mitteilung: TSMitteilung): boolean {
        return this.isBetreuungsmitteilung(mitteilung) && (<TSBetreuungsmitteilung>mitteilung).applied !== true;
    }

    public canApplyBetreuungsmitteilung(mitteilung: TSMitteilung): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorJugendamtRole());
    }

    public showBetreuungsmitteilungApply(mitteilung: TSMitteilung): boolean {
        return this.canApplyBetreuungsmitteilung(mitteilung) && this.isBetreuungsmitteilungNotApplied(mitteilung);
    }

    $postLink() {
        this.$timeout(() => {
            EbeguUtil.selectFirst();
        }, 200);
    }

    public applyBetreuungsmitteilung(mitteilung: TSMitteilung): void {
        if (this.isBetreuungsmitteilung(mitteilung)) {
            this.DvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
                title: 'MUTATIONSMELDUNG_UEBERNEHMEN',
                deleteText: 'MUTATIONSMELDUNG_UEBERNEHMEN_BESCHREIBUNG',
                parentController: this,
                elementID: 'Intro'
            }).then(() => {   //User confirmed message
                let betreuungsmitteilung: TSBetreuungsmitteilung = <TSBetreuungsmitteilung>mitteilung;
                this.mitteilungRS.applyBetreuungsmitteilung(betreuungsmitteilung.id).then((response: any) => { // JaxID kommt als response
                    this.loadAllMitteilungen();
                    if (response.id === this.gesuchModelManager.getGesuch().id) {
                        // Dies wird gebraucht wenn das Gesuch der Mitteilung schon geladen ist, weil die Daten der
                        // Betreuung geaendert wurden und deshalb neugeladen werden müssen. reloadGesuch ist einfacher
                        // als die entsprechende Betreuung neu zu laden
                        this.gesuchModelManager.reloadGesuch();
                    } else if (response.id) { // eine neue Mutation wurde aus der Muttationsmitteilung erstellt
                        // informieren, dass eine neue Mutation erstellt wurde
                        this.$rootScope.$broadcast(TSMitteilungEvent[TSMitteilungEvent.MUTATIONSMITTEILUNG_NEUE_MUTATION], 'Mutationsmitteilung einer neuen Mutation hinzugefuegt');
                    }
                });
            });
        }
    }

    public mitteilungUebergebenAnJugendamt(mitteilung: TSMitteilung): void {
        this.mitteilungRS.mitteilungUebergebenAnJugendamt(mitteilung.id).then(msg => {
            this.ebeguUtil.replaceElementInList(msg, this.allMitteilungen, false);
        });
    }

    public mitteilungUebergebenAnSchulamt(mitteilung: TSMitteilung): void {
        this.mitteilungRS.mitteilungUebergebenAnSchulamt(mitteilung.id).then(msg => {
            this.ebeguUtil.replaceElementInList(msg, this.allMitteilungen, false);
        });
    }

    public isMessageEditableForMyRole(mitteilung: TSMitteilung): boolean {
        // Ich darf die Mitteilung auf Gelesen setzen oder Delegieren, wenn ich der gleichen Empfängergruppe wie die Meldung selber angehöre
        return this.isUserAndEmpfaengerSameAmt(mitteilung, TSAmt.JUGENDAMT) ||
            this.isUserAndEmpfaengerSameAmt(mitteilung, TSAmt.SCHULAMT);
    }

    public canUebergebenAnSchulamt(mitteilung: TSMitteilung): boolean {
        return !this.isBetreuungsmitteilung(mitteilung) &&
            this.isUserAndEmpfaengerSameAmt(mitteilung, TSAmt.JUGENDAMT) && !mitteilung.isErledigt();
    }

    public canUebergebenAnJugendamt(mitteilung: TSMitteilung): boolean {
        return !this.isBetreuungsmitteilung(mitteilung) &&
            this.isUserAndEmpfaengerSameAmt(mitteilung, TSAmt.SCHULAMT) && !mitteilung.isErledigt();
    }

    private isUserAndEmpfaengerSameAmt(mitteilung: TSMitteilung, amt: TSAmt): boolean {
        let userInAmt: boolean = this.authServiceRS.getPrincipal().amt === amt;
        let empfaengerInAmt: boolean = mitteilung.getEmpfaengerAmt() === amt;
        return userInAmt && empfaengerInAmt;
    }

    private isBetreuungsmitteilung(mitteilung: TSMitteilung): boolean {
        return mitteilung instanceof TSBetreuungsmitteilung;
    }
}
