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

import {StateService} from '@uirouter/core';
import {IComponentOptions, IOnInit, IPromise} from 'angular';
import AuthServiceRS from '../../../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../../../gesuch/dialog/RemoveDialogController';
import DossierRS from '../../../../gesuch/service/dossierRS.rest';
import GesuchModelManager from '../../../../gesuch/service/gesuchModelManager';
import {TSAmt} from '../../../../models/enums/TSAmt';
import {TSMitteilungEvent} from '../../../../models/enums/TSMitteilungEvent';
import {TSMitteilungStatus} from '../../../../models/enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from '../../../../models/enums/TSMitteilungTeilnehmerTyp';
import {TSRole} from '../../../../models/enums/TSRole';
import TSBetreuung from '../../../../models/TSBetreuung';
import TSBetreuungsmitteilung from '../../../../models/TSBetreuungsmitteilung';
import TSDossier from '../../../../models/TSDossier';
import TSMitteilung from '../../../../models/TSMitteilung';
import EbeguUtil from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {IMitteilungenStateParams} from '../../../mitteilungen/mitteilungen.route';
import {PosteingangService} from '../../../posteingang/service/posteingang.service';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import BetreuungRS from '../../service/betreuungRS.rest';
import MitteilungRS from '../../service/mitteilungRS.rest';
import IFormController = angular.IFormController;
import IQService = angular.IQService;
import IRootScopeService = angular.IRootScopeService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import IWindowService = angular.IWindowService;

const removeDialogTemplate = require('../../../../gesuch/dialog/removeDialogTemplate.html');

export class DVMitteilungListConfig implements IComponentOptions {
    public transclude = false;

    public bindings = {
        dossier: '<',
        betreuung: '<',
        form: '<',
    };

    public template = require('./dv-mitteilung-list.html');
    public controller = DVMitteilungListController;
    public controllerAs = 'vm';
}

export class DVMitteilungListController implements IOnInit {

    public static $inject: ReadonlyArray<string> = [
        '$stateParams',
        'MitteilungRS',
        'AuthServiceRS',
        'BetreuungRS',
        '$q',
        '$window',
        '$rootScope',
        '$state',
        'EbeguUtil',
        'DvDialog',
        'GesuchModelManager',
        '$scope',
        '$timeout',
        'DossierRS',
        'PosteingangService',
    ];

    public dossier: TSDossier;
    public betreuung: TSBetreuung;
    public form: IFormController;

    public paramSelectedMitteilungId: string;
    public currentMitteilung: TSMitteilung;
    public allMitteilungen: Array<TSMitteilung>;
    public readonly TSRole = TSRole;
    public readonly TSRoleUtil = TSRoleUtil;

    public constructor(
        private readonly $stateParams: IMitteilungenStateParams,
        private readonly mitteilungRS: MitteilungRS,
        private readonly authServiceRS: AuthServiceRS,
        private readonly betreuungRS: BetreuungRS,
        private readonly $q: IQService,
        private readonly $window: IWindowService,
        private readonly $rootScope: IRootScopeService,
        private readonly $state: StateService,
        public ebeguUtil: EbeguUtil,
        private readonly dvDialog: DvDialog,
        private readonly gesuchModelManager: GesuchModelManager,
        private readonly $scope: IScope,
        private readonly $timeout: ITimeoutService,
        private readonly dossierRS: DossierRS,
        private readonly posteingangService: PosteingangService,
    ) {
    }

    public $onInit(): void {
        if (this.$stateParams.mitteilungId) {
            // wenn man eine bestimmte Mitteilung oeffnen will, kann man ihr ID als parameter geben
            this.paramSelectedMitteilungId = this.$stateParams.mitteilungId;
        }
        if (this.$stateParams.dossierId) {
            this.dossierRS.findDossier(this.$stateParams.dossierId).then(response => {
                this.dossier = response;
                if (this.$stateParams.betreuungId) {
                    this.betreuungRS.findBetreuung(this.$stateParams.betreuungId).then(r => {
                        this.betreuung = r;
                        this.loadEntwurf();
                        this.loadAllMitteilungen();
                    });
                } else {
                    this.loadEntwurf();
                    // Wenn JA oder Institution -> Neue Mitteilungen als gelesen markieren
                    if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerJugendamtSchulamtRoles())) {
                        this.setAllMitteilungenGelesen().then(() => {
                            this.loadAllMitteilungen();
                            this.posteingangService.posteingangChanged();
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
    private loadEntwurf(): void {
        // Wenn der Fall keinen Besitzer hat, darf auch keine Nachricht geschrieben werden
        // Ausser wir sind Institutionsbenutzer
        const isGesuchsteller = this.authServiceRS.isRole(TSRole.GESUCHSTELLER);
        const isJugendamtOrSchulamtAndFallHasBesitzer = this.dossier.fall.besitzer && this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAdministratorJugendamtSchulamtRoles());
        const isInstitutionsUser = this.authServiceRS.isOneOfRoles(TSRoleUtil.getTraegerschaftInstitutionOnlyRoles());
        if (!(isGesuchsteller || isJugendamtOrSchulamtAndFallHasBesitzer || isInstitutionsUser)) {
            return;
        }

        const entwurfHandler = (entwurf: TSMitteilung) => {
            if (entwurf) {
                this.currentMitteilung = entwurf;
            } else {
                this.initMitteilungForCurrentBenutzer();
            }
        };

        if (this.betreuung) {
            this.mitteilungRS.getEntwurfForCurrentRolleForBetreuung(this.betreuung.id).then(entwurfHandler);
        } else {
            this.mitteilungRS.getEntwurfOfDossierForCurrentRolle(this.dossier.id).then(entwurfHandler);
        }
    }

    private initMitteilungForCurrentBenutzer(): void {
        const currentUser = this.authServiceRS.getPrincipal();
        // common attributes
        this.currentMitteilung = new TSMitteilung();
        this.currentMitteilung.dossier = this.dossier;
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

        if (this.isMitteilungEmpty()) {
            return this.$q.when(this.currentMitteilung);
        }

        return this.mitteilungRS.sendMitteilung(this.getCurrentMitteilung())
            .then(() => this.reloadEntwurfAndMitteilungen())
            .finally(() => {
                this.form.$setPristine();
                this.form.$setUntouched();
            });
    }

    private reloadEntwurfAndMitteilungen(): TSMitteilung {
        this.loadEntwurf();
        this.loadAllMitteilungen();

        return this.currentMitteilung;
    }

    /**
     * Speichert die aktuelle Mitteilung nur wenn das formular dirty ist.
     * Wenn das Formular leer ist, wird der Entwurf geloescht (falls er bereits existiert)
     */
    public saveEntwurf(): IPromise<TSMitteilung> {
        if (this.form.$dirty && !this.isMitteilungEmpty()) {
            return this.mitteilungRS.saveEntwurf(this.getCurrentMitteilung())
                .then(() => this.reloadEntwurfAndMitteilungen())
                .finally(() => {
                    this.form.$setPristine();
                    this.form.$setUntouched();
                });

        }
        if (this.isMitteilungEmpty() && !this.currentMitteilung.isNew() && this.currentMitteilung.id) {
            return this.mitteilungRS.removeEntwurf(this.getCurrentMitteilung()).then(() => {
                this.initMitteilungForCurrentBenutzer();
                return this.currentMitteilung;
            });
        }
        return this.$q.when(this.currentMitteilung);
    }

    private isMitteilungEmpty(): boolean {
        return (!this.currentMitteilung.message || this.currentMitteilung.message.length <= 0)
            && (!this.currentMitteilung.subject || this.currentMitteilung.subject.length <= 0);
    }

    private loadAllMitteilungen(): void {
        if (this.betreuung) {
            this.mitteilungRS.getMitteilungenForCurrentRolleForBetreuung(this.betreuung.id).then(response => {
                this.allMitteilungen = response;
            });
        } else {
            this.mitteilungRS.getMitteilungenOfDossierForCurrentRolle(this.dossier.id).then(response => {
                this.allMitteilungen = response;
            });
        }
    }

    /**
     * Gibt true zurueck wenn der aktuelle BenutzerTyp, der Sender der uebergenenen Mitteilung ist.
     */
    public isCurrentUserTypTheSenderTyp(mitteilung: TSMitteilung): boolean {
        const principal = this.authServiceRS.getPrincipal();
        return mitteilung && mitteilung.sender && principal
            && mitteilung.senderTyp === this.getMitteilungTeilnehmerTypForUserRole(principal.getCurrentRole());
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
            case TSRole.GESUCHSTELLER:
                return TSMitteilungTeilnehmerTyp.GESUCHSTELLER;
            case TSRole.ADMIN_INSTITUTION:
            case TSRole.SACHBEARBEITER_INSTITUTION:
            case TSRole.ADMIN_TRAEGERSCHAFT:
            case TSRole.SACHBEARBEITER_TRAEGERSCHAFT:
                return TSMitteilungTeilnehmerTyp.INSTITUTION;
            case TSRole.SUPER_ADMIN:
            case TSRole.ADMIN_BG:
            case TSRole.JURIST:
            case TSRole.REVISOR:
            case TSRole.SACHBEARBEITER_BG:
            case TSRole.ADMIN_GEMEINDE:
            case TSRole.SACHBEARBEITER_GEMEINDE:
            case TSRole.SACHBEARBEITER_TS:
            case TSRole.ADMIN_TS:
            case TSRole.ADMIN_MANDANT:
            case TSRole.SACHBEARBEITER_MANDANT:
                return TSMitteilungTeilnehmerTyp.JUGENDAMT;
            default:
                return null;
        }
    }

    private setAllMitteilungenGelesen(): IPromise<Array<TSMitteilung>> {
        return this.mitteilungRS.setAllNewMitteilungenOfDossierGelesen(this.dossier.id);
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
        return mitteilung && (
            mitteilung.mitteilungStatus === TSMitteilungStatus.ERLEDIGT
            || mitteilung.mitteilungStatus === TSMitteilungStatus.GELESEN
        );
    }

    public getBgNummer(): string {
        let bgNummer = '';
        if (this.betreuung) {
            bgNummer = this.ebeguUtil.calculateBetreuungsId(this.betreuung.gesuchsperiode,
                this.dossier.fall,
                this.dossier.gemeinde,
                this.betreuung.kindNummer,
                this.betreuung.betreuungNummer);
        }
        return bgNummer;
    }

    public betreuungAsString(mitteilung: TSMitteilung): string {
        let betreuungAsString: string;
        if (mitteilung.betreuung) {
            const bgNummer = this.ebeguUtil.calculateBetreuungsId(mitteilung.betreuung.gesuchsperiode,
                mitteilung.dossier.fall,
                mitteilung.dossier.gemeinde,
                mitteilung.betreuung.kindNummer,
                mitteilung.betreuung.betreuungNummer);
            betreuungAsString = `${mitteilung.betreuung.kindFullname}, ${bgNummer}`;
        }
        return betreuungAsString;
    }

    public gotoBetreuung(mitteilung: TSMitteilung): void {
        this.$state.go('gesuch.betreuung', {
            betreuungNumber: mitteilung.betreuung.betreuungNummer,
            kindNumber: mitteilung.betreuung.kindNummer,
            gesuchId: mitteilung.betreuung.gesuchId,
        });
    }

    public isBetreuungsmitteilungApplied(mitteilung: TSMitteilung): boolean {
        return this.isBetreuungsmitteilung(mitteilung) && (mitteilung as TSBetreuungsmitteilung).applied;
    }

    public isBetreuungsmitteilungNotApplied(mitteilung: TSMitteilung): boolean {
        return this.isBetreuungsmitteilung(mitteilung) && !(mitteilung as TSBetreuungsmitteilung).applied;
    }

    public canApplyBetreuungsmitteilung(_mitteilung: TSMitteilung): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorJugendamtRole());
    }

    public showBetreuungsmitteilungApply(mitteilung: TSMitteilung): boolean {
        return this.canApplyBetreuungsmitteilung(mitteilung) && this.isBetreuungsmitteilungNotApplied(mitteilung);
    }

    public $postLink(): void {
        const selectDelay = 200;
        this.$timeout(() => {
            EbeguUtil.selectFirst();
        }, selectDelay);
    }

    public applyBetreuungsmitteilung(mitteilung: TSMitteilung): void {
        if (!this.isBetreuungsmitteilung(mitteilung)) {
            return;
        }

        this.dvDialog.showRemoveDialog(removeDialogTemplate, this.form, RemoveDialogController, {
            title: 'MUTATIONSMELDUNG_UEBERNEHMEN',
            deleteText: 'MUTATIONSMELDUNG_UEBERNEHMEN_BESCHREIBUNG',
            parentController: this,
            elementID: 'Intro',
        }).then(() => {   // User confirmed message
            const betreuungsmitteilung = mitteilung as TSBetreuungsmitteilung;
            // JaxID kommt als response
            this.mitteilungRS.applyBetreuungsmitteilung(betreuungsmitteilung.id).then((response: any) => {
                this.loadAllMitteilungen();
                if (response.id === this.gesuchModelManager.getGesuch().id) {
                    // Dies wird gebraucht wenn das Gesuch der Mitteilung schon geladen ist, weil die Daten der
                    // Betreuung geaendert wurden und deshalb neugeladen werden müssen. reloadGesuch ist einfacher
                    // als die entsprechende Betreuung neu zu laden
                    this.gesuchModelManager.reloadGesuch();
                } else if (response.id) {
                    // eine neue Mutation wurde aus der Muttationsmitteilung erstellt
                    // informieren, dass eine neue Mutation erstellt wurde
                    const event = TSMitteilungEvent[TSMitteilungEvent.MUTATIONSMITTEILUNG_NEUE_MUTATION];
                    this.$rootScope.$broadcast(event, 'Mutationsmitteilung einer neuen Mutation hinzugefuegt');
                }
            });
        });
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
        // Ich darf die Mitteilung auf Gelesen setzen oder Delegieren, wenn ich der gleichen Empfängergruppe wie die
        // Meldung selber angehöre
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
        const userInAmt = this.authServiceRS.getPrincipal().amt === amt;
        const empfaengerInAmt = mitteilung.getEmpfaengerAmt() === amt;
        return userInAmt && empfaengerInAmt;
    }

    private isBetreuungsmitteilung(mitteilung: TSMitteilung): boolean {
        return mitteilung instanceof TSBetreuungsmitteilung;
    }
}
