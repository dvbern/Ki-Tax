/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {StateService} from '@uirouter/core';
import {IComponentOptions, IOnInit, IPromise} from 'angular';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {RemoveDialogController} from '../../../../gesuch/dialog/RemoveDialogController';
import {DossierRS} from '../../../../gesuch/service/dossierRS.rest';
import {GemeindeRS} from '../../../../gesuch/service/gemeindeRS.rest';
import {GesuchModelManager} from '../../../../gesuch/service/gesuchModelManager';
import {GesuchRS} from '../../../../gesuch/service/gesuchRS.rest';
import {TSMitteilungEvent} from '../../../../models/enums/TSMitteilungEvent';
import {TSMitteilungStatus} from '../../../../models/enums/TSMitteilungStatus';
import {TSMitteilungTeilnehmerTyp} from '../../../../models/enums/TSMitteilungTeilnehmerTyp';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSBetreuung} from '../../../../models/TSBetreuung';
import {TSBetreuungsmitteilung} from '../../../../models/TSBetreuungsmitteilung';
import {TSDossier} from '../../../../models/TSDossier';
import {TSGesuchstellerContainer} from '../../../../models/TSGesuchstellerContainer';
import {TSMitteilung} from '../../../../models/TSMitteilung';
import {EbeguUtil} from '../../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {IMitteilungenStateParams} from '../../../mitteilungen/mitteilungen.route';
import {PosteingangService} from '../../../posteingang/service/posteingang.service';
import {DvDialog} from '../../directive/dv-dialog/dv-dialog';
import {TSDemoFeature} from '../../directive/dv-hide-feature/TSDemoFeature';
import {LogFactory} from '../../logging/LogFactory';
import {BetreuungRS} from '../../service/betreuungRS.rest';
import {InstitutionRS} from '../../service/institutionRS.rest';
import {MitteilungRS} from '../../service/mitteilungRS.rest';
import IFormController = angular.IFormController;
import IQService = angular.IQService;
import IRootScopeService = angular.IRootScopeService;
import IScope = angular.IScope;
import ITimeoutService = angular.ITimeoutService;
import IWindowService = angular.IWindowService;

const removeDialogTemplate = require('../../../../gesuch/dialog/removeDialogTemplate.html');
const LOG = LogFactory.createLog('DVMitteilungListConfig');

export class DVMitteilungListConfig implements IComponentOptions {
    public transclude = false;

    public bindings = {
        dossier: '<',
        betreuung: '<',
        form: '<'
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
        'InstitutionRS',
        'GemeindeRS',
        'GesuchRS'
    ];

    public dossier: TSDossier;
    public betreuung: TSBetreuung;
    public form: IFormController;

    public paramSelectedMitteilungId: string;
    public currentMitteilung: TSMitteilung;
    public allMitteilungen: Array<TSMitteilung>;
    public readonly TSRole = TSRole;
    public readonly TSRoleUtil = TSRoleUtil;
    public isLoaded: boolean = true;
    public empfaenger: any;
    public empfaengerValues: Array<any>;
    public isVolksschuleGemeinde: boolean = false;
    public readonly demoFeature =
        TSDemoFeature.ALLE_MUTATIONSMELDUNGEN_VERFUEGEN;

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
        private readonly institutionRS: InstitutionRS,
        private readonly gemeindeRS: GemeindeRS,
        private readonly gesuchRS: GesuchRS
    ) {}

    public $onInit(): void {
        if (this.$stateParams.mitteilungId) {
            // wenn man eine bestimmte Mitteilung oeffnen will, kann man ihr ID als parameter geben
            this.paramSelectedMitteilungId = this.$stateParams.mitteilungId;
        }
        if (this.$stateParams.dossierId) {
            this.dossierRS
                .findDossier(this.$stateParams.dossierId)
                .then(response => {
                    this.dossier = response;
                    this.isVolksschuleGemeinde =
                        this.dossier.gemeinde.besondereVolksschule;
                    if (this.$stateParams.betreuungId) {
                        this.betreuungRS
                            .findBetreuung(this.$stateParams.betreuungId)
                            .then(r => {
                                this.betreuung = r;
                                this.initMitteilungForCurrentBenutzer();
                                this.loadAllMitteilungen();
                            });
                    } else {
                        this.initMitteilungForCurrentBenutzer();
                        // Wenn JA oder Institution -> Neue Mitteilungen als gelesen markieren
                        if (
                            this.authServiceRS.isOneOfRoles(
                                TSRoleUtil.getGesuchstellerSozialdienstJugendamtSchulamtRoles()
                            ) ||
                            this.authServiceRS.isOneOfRoles(
                                TSRoleUtil.getTraegerschaftInstitutionRoles()
                            )
                        ) {
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
        this.$scope.$on(
            TSMitteilungEvent[
                TSMitteilungEvent.MUTATIONSMITTEILUNG_MUTATION_REMOVED
            ],
            () => {
                this.loadAllMitteilungen();
            }
        );
    }

    public cancel(): void {
        this.form.$setPristine();
        this.$window.history.back();
    }

    private initMitteilungForCurrentBenutzer(): void {
        this.gemeindeRS
            .getGemeindeStammdaten(this.dossier.gemeinde.id)
            .then(gemeindeStammdaten => {
                const isGesuchsteller = this.authServiceRS.isRole(
                    TSRole.GESUCHSTELLER
                );
                const isJugendamtOrSchulamtAndFallHasBesitzer =
                    this.authServiceRS.isOneOfRoles(
                        TSRoleUtil.getAdministratorJugendamtSchulamtRoles()
                    );
                const isInstitutionsUser = this.authServiceRS.isOneOfRoles(
                    TSRoleUtil.getTraegerschaftInstitutionOnlyRoles()
                );
                const isSozialdienst = this.authServiceRS.isOneOfRoles(
                    TSRoleUtil.getSozialdienstRolle()
                );
                if (
                    !(
                        isGesuchsteller ||
                        isJugendamtOrSchulamtAndFallHasBesitzer ||
                        isInstitutionsUser ||
                        isSozialdienst
                    )
                ) {
                    return;
                }
                const currentUser = this.authServiceRS.getPrincipal();
                // common attributes
                this.currentMitteilung = new TSMitteilung();
                this.currentMitteilung.dossier = this.dossier;
                if (this.betreuung) {
                    this.currentMitteilung.betreuung = this.betreuung;
                }
                this.currentMitteilung.mitteilungStatus =
                    TSMitteilungStatus.NEU;
                this.currentMitteilung.sender = currentUser;
                if (
                    this.authServiceRS.isOneOfRoles(
                        TSRoleUtil.getAdministratorJugendamtSchulamtRoles().concat(
                            TSRoleUtil.getTraegerschaftOnlyRoles()
                        )
                    )
                ) {
                    this.initReceiverList();
                }
                if (
                    this.authServiceRS.isOneOfRoles(
                        TSRoleUtil.getInstitutionOnlyRoles()
                    )
                ) {
                    this.currentMitteilung.institution =
                        currentUser.currentBerechtigung.institution;
                }

                if (
                    this.authServiceRS.isOneOfRoles(
                        TSRoleUtil.getGemeindeRoles()
                    )
                ) {
                    this.currentMitteilung.message =
                        gemeindeStammdaten.gemeindeStammdatenKorrespondenz?.standardSignatur;
                }
            });
    }

    private initReceiverList(): void {
        this.empfaengerValues = [];
        if (
            this.isCurrentUserAmt() &&
            (this.dossier.fall.besitzer || this.dossier.fall.sozialdienstFall)
        ) {
            this.empfaengerValues.push({
                key: null,
                value: this.dossier.fall.sozialdienstFall
                    ? this.dossier.fall.sozialdienstFall.sozialdienst.name
                    : this.ebeguUtil.translateString('GESUCHSTELLER')
            });
        }
        this.institutionRS.findAllInstitutionen(this.dossier.id).subscribe(
            institutionen => {
                institutionen.forEach(institution =>
                    this.empfaengerValues.push({
                        key: institution,
                        value: institution.name
                    })
                );
            },
            error => LOG.error(error)
        );
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

        return this.mitteilungRS
            .sendMitteilung(this.getCurrentMitteilung())
            .then(() => this.reloadMitteilungen())
            .finally(() => {
                this.form.$setPristine();
                this.form.$setUntouched();
                this.initMitteilungForCurrentBenutzer();
            });
    }

    private reloadMitteilungen(): TSMitteilung {
        this.loadAllMitteilungen();
        return this.currentMitteilung;
    }

    private isMitteilungEmpty(): boolean {
        return (
            (!this.currentMitteilung.message ||
                this.currentMitteilung.message.length <= 0) &&
            (!this.currentMitteilung.subject ||
                this.currentMitteilung.subject.length <= 0)
        );
    }

    private loadAllMitteilungen(): void {
        if (this.betreuung) {
            this.mitteilungRS
                .getMitteilungenForCurrentRolleForBetreuung(this.betreuung.id)
                .then(response => {
                    this.allMitteilungen = response;
                });
        } else {
            this.mitteilungRS
                .getMitteilungenOfDossierForCurrentRolle(this.dossier.id)
                .then(response => {
                    this.allMitteilungen = response;
                });
        }
    }

    /**
     * Gibt true zurueck wenn der aktuelle BenutzerTyp, der Sender der uebergenenen Mitteilung ist.
     */
    public isCurrentUserTypTheSenderTyp(mitteilung: TSMitteilung): boolean {
        const principal = this.authServiceRS.getPrincipal();
        return (
            mitteilung &&
            mitteilung.sender &&
            principal &&
            mitteilung.senderTyp ===
                this.getMitteilungTeilnehmerTypForUserRole(
                    principal.getCurrentRole()
                )
        );
    }

    public isSenderTypInstitution(mitteilung: TSMitteilung): boolean {
        return (
            mitteilung &&
            mitteilung.sender &&
            mitteilung.senderTyp === TSMitteilungTeilnehmerTyp.INSTITUTION
        );
    }

    public isSenderTypGemeinde(mitteilung: TSMitteilung): boolean {
        return (
            mitteilung &&
            mitteilung.sender &&
            mitteilung.senderTyp === TSMitteilungTeilnehmerTyp.JUGENDAMT
        );
    }

    public isSenderTypGesuchsteller(mitteilung: TSMitteilung): boolean {
        return (
            mitteilung &&
            mitteilung.sender &&
            mitteilung.senderTyp === TSMitteilungTeilnehmerTyp.GESUCHSTELLER
        );
    }

    public isMitteilungEmpfaengerInstitution(
        mitteilung: TSMitteilung
    ): boolean {
        return (
            mitteilung.empfaengerTyp === TSMitteilungTeilnehmerTyp.INSTITUTION
        );
    }

    private getMitteilungTeilnehmerTypForUserRole(
        role: TSRole
    ): TSMitteilungTeilnehmerTyp {
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
            case TSRole.ADMIN_SOZIALDIENST:
            case TSRole.SACHBEARBEITER_SOZIALDIENST:
                return TSMitteilungTeilnehmerTyp.SOZIALDIENST;
            default:
                return null;
        }
    }

    private setAllMitteilungenGelesen(): IPromise<Array<TSMitteilung>> {
        return this.mitteilungRS.setAllNewMitteilungenOfDossierGelesen(
            this.dossier.id
        );
    }

    /**
     * Aendert den Status der gegebenen Mitteilung auf ERLEDIGT wenn es GELESEN war oder
     * auf GELESEN wenn es ERLEDIGT war
     */
    public setErledigt(mitteilung: TSMitteilung): void {
        if (
            mitteilung &&
            mitteilung.mitteilungStatus !== TSMitteilungStatus.ERLEDIGT
        ) {
            const currentUser = this.authServiceRS.getPrincipal();
            if (currentUser.username !== mitteilung.empfaenger.username) {
                this.dvDialog
                    .showRemoveDialog(
                        removeDialogTemplate,
                        this.form,
                        RemoveDialogController,
                        {
                            title: 'ERLDEDIGT_NICHT_ALS_EMPFAENGER_TITLE',
                            deleteText: 'ERLDEDIGT_NICHT_ALS_EMPFAENGER_TEXT',
                            parentController: undefined,
                            elementID: undefined
                        }
                    )
                    .then(() => {
                        mitteilung.mitteilungStatus =
                            TSMitteilungStatus.ERLEDIGT;
                        this.mitteilungRS.setMitteilungErledigt(mitteilung.id);
                    });
            } else {
                mitteilung.mitteilungStatus = TSMitteilungStatus.ERLEDIGT;
                this.mitteilungRS.setMitteilungErledigt(mitteilung.id);
            }
        } else {
            mitteilung.mitteilungStatus = TSMitteilungStatus.GELESEN;
            this.mitteilungRS.setMitteilungGelesen(mitteilung.id);
        }
    }

    public betreuungAsString(mitteilung: TSMitteilung): string {
        return mitteilung.betreuung
            ? `${mitteilung.betreuung.kindFullname}, ${mitteilung.betreuung.referenzNummer}`
            : '';
    }

    public gotoBetreuung(mitteilung: TSMitteilung): void {
        this.$state.go('gesuch.betreuung', {
            betreuungNumber: mitteilung.betreuung.betreuungNummer,
            kindNumber: mitteilung.betreuung.kindNummer,
            gesuchId: mitteilung.betreuung.gesuchId
        });
    }

    public async goToFinanzielleSituation(
        mitteilung: TSMitteilung
    ): Promise<void> {
        const gesuch = await this.gesuchRS.findGesuchForFinSit(
            mitteilung.finanzielleSituation.id
        );
        let gesuchstellerNumber: number;
        if (this.finSitIdEquals(mitteilung, gesuch.gesuchsteller1)) {
            gesuchstellerNumber = 1;
        } else if (this.finSitIdEquals(mitteilung, gesuch.gesuchsteller2)) {
            gesuchstellerNumber = 2;
        } else {
            const e = new Error('finSit nicht gefunden');
            LOG.error(e);
            throw e;
        }
        this.$state.go('gesuch.finanzielleSituation', {
            gesuchstellerNumber,
            gesuchId: gesuch.id
        });
    }

    private finSitIdEquals(
        mitteilung: TSMitteilung,
        gesuchstellerCont: TSGesuchstellerContainer
    ): boolean {
        return (
            gesuchstellerCont.finanzielleSituationContainer
                .finanzielleSituationJA.id ===
            mitteilung.finanzielleSituation.id
        );
    }

    public isBetreuungsmitteilungApplied(mitteilung: TSMitteilung): boolean {
        return (
            this.isBetreuungsmitteilung(mitteilung) &&
            (mitteilung as TSBetreuungsmitteilung).applied
        );
    }

    public isBetreuungsmitteilungNotApplied(mitteilung: TSMitteilung): boolean {
        return (
            this.isBetreuungsmitteilung(mitteilung) &&
            !(mitteilung as TSBetreuungsmitteilung).applied
        );
    }

    public canApplyBetreuungsmitteilung(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAdministratorJugendamtRole()
        );
    }

    public showBetreuungsmitteilungApply(mitteilung: TSMitteilung): boolean {
        return (
            this.canApplyBetreuungsmitteilung() &&
            this.isBetreuungsmitteilungNotApplied(mitteilung)
        );
    }

    public isNeueVeranlagungsmitteilungApplied(
        mitteilung: TSMitteilung
    ): boolean {
        return (
            this.isNeueVeranlagungsmitteilung(mitteilung) &&
            mitteilung.isErledigt()
        );
    }

    public isNeueVeranlagungsmitteilungNotErledigt(
        mitteilung: TSMitteilung
    ): boolean {
        return (
            this.isNeueVeranlagungsmitteilung(mitteilung) &&
            !mitteilung.isErledigt() &&
            !mitteilung.isIgnoriert()
        );
    }

    public canErledigenNeueVeranlagungsmitteilung(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAdministratorOrAmtRole()
        );
    }

    public showNeueVeranlagungsmitteilungApply(
        mitteilung: TSMitteilung
    ): boolean {
        return (
            this.canErledigenNeueVeranlagungsmitteilung() &&
            this.isNeueVeranlagungsmitteilungNotErledigt(mitteilung)
        );
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

        this.dvDialog
            .showRemoveDialog(
                removeDialogTemplate,
                this.form,
                RemoveDialogController,
                {
                    title: 'MUTATIONSMELDUNG_UEBERNEHMEN',
                    deleteText: 'MUTATIONSMELDUNG_UEBERNEHMEN_BESCHREIBUNG',
                    parentController: this,
                    elementID: 'Intro'
                }
            )
            .then(() => {
                // User confirmed message
                const betreuungsmitteilung =
                    mitteilung as TSBetreuungsmitteilung;
                // JaxID kommt als response
                this.mitteilungRS
                    .applyBetreuungsmitteilung(betreuungsmitteilung.id)
                    .then((response: any) => {
                        this.loadAllMitteilungen();
                        if (
                            response.id ===
                            this.gesuchModelManager.getGesuch().id
                        ) {
                            // Dies wird gebraucht wenn das Gesuch der Mitteilung schon geladen ist, weil die Daten der
                            // Betreuung geaendert wurden und deshalb neugeladen werden müssen. reloadGesuch ist einfacher
                            // als die entsprechende Betreuung neu zu laden
                            this.gesuchModelManager.reloadGesuch();
                        } else if (response.id) {
                            // eine neue Mutation wurde aus der Muttationsmitteilung erstellt
                            // informieren, dass eine neue Mutation erstellt wurde
                            const event =
                                TSMitteilungEvent[
                                    TSMitteilungEvent
                                        .MUTATIONSMITTEILUNG_NEUE_MUTATION
                                ];
                            this.$rootScope.$broadcast(
                                event,
                                'Mutationsmitteilung einer neuen Mutation hinzugefuegt'
                            );
                        }
                    });
            });
    }

    public neueVeranlagungsmitteilungBearbeiten(
        mitteilung: TSMitteilung
    ): void {
        if (!this.isNeueVeranlagungsmitteilung(mitteilung)) {
            return;
        }

        this.dvDialog
            .showRemoveDialog(
                removeDialogTemplate,
                this.form,
                RemoveDialogController,
                {
                    title: 'VERANLAGUNGSMITTEILUNG_UEBERNEHMEN',
                    deleteText:
                        'VERANLAGUNGSMITTEILUNG_UEBERNEHMEN_BESCHREIBUNG',
                    parentController: this,
                    elementID: 'Intro'
                }
            )
            .then(() => {
                // User confirmed message
                this.mitteilungRS
                    .neueVeranlagungsmitteilungBearbeiten(mitteilung.id)
                    .then((response: any) => {
                        this.loadAllMitteilungen();
                        if (
                            response.id ===
                            this.gesuchModelManager.getGesuch().id
                        ) {
                            // Dies wird gebraucht wenn das Gesuch der Mitteilung schon geladen ist, weil die Daten der
                            // Betreuung geaendert wurden und deshalb neugeladen werden müssen. reloadGesuch ist einfacher
                            // als die entsprechende Betreuung neu zu laden
                            this.gesuchModelManager.reloadGesuch();
                        } else if (response.id) {
                            // eine neue Mutation wurde aus der Muttationsmitteilung erstellt
                            // informieren, dass eine neue Mutation erstellt wurde
                            const event =
                                TSMitteilungEvent[
                                    TSMitteilungEvent
                                        .MUTATIONSMITTEILUNG_NEUE_MUTATION
                                ];
                            this.$rootScope.$broadcast(
                                event,
                                'Mutationsmitteilung einer neuen Mutation hinzugefuegt'
                            );
                        }
                    });
            });
    }

    public neueVeranlagungsmitteilungIgnorieren(
        mitteilung: TSMitteilung
    ): void {
        if (!this.isNeueVeranlagungsmitteilung(mitteilung)) {
            return;
        }
        this.dvDialog
            .showRemoveDialog(
                removeDialogTemplate,
                this.form,
                RemoveDialogController,
                {
                    title: 'MUTATIONSMELDUNG_IGNORIEREN',
                    deleteText: ' ',
                    parentController: this,
                    elementID: 'Intro'
                }
            )
            .then(() => {
                // User confirmed message
                this.mitteilungRS
                    .setMitteilungIgnoriert(mitteilung.id)
                    .then(() => {
                        this.loadAllMitteilungen();
                    });
            });
    }

    public mitteilungWeitergeleitet(): void {
        this.loadAllMitteilungen();
    }

    public canUebergeben(mitteilung: TSMitteilung): boolean {
        return (
            mitteilung.empfaengerTyp !==
                TSMitteilungTeilnehmerTyp.GESUCHSTELLER &&
            mitteilung.empfaengerTyp !==
                TSMitteilungTeilnehmerTyp.INSTITUTION &&
            mitteilung.empfaengerTyp !==
                TSMitteilungTeilnehmerTyp.SOZIALDIENST &&
            this.authServiceRS.isOneOfRoles(
                TSRoleUtil.getAdministratorOrAmtRole()
            ) &&
            !mitteilung.isErledigt() &&
            !mitteilung.isIgnoriert()
        );
    }

    public isNeueVeranlagungErledigt(mitteilung: TSMitteilung): boolean {
        return (
            this.isNeueVeranlagungsmitteilung(mitteilung) &&
            mitteilung.isErledigt()
        );
    }

    public isNeueVeranlagungIgnoriert(mitteilung: TSMitteilung): boolean {
        return (
            this.isNeueVeranlagungsmitteilung(mitteilung) &&
            mitteilung.isIgnoriert()
        );
    }

    private isBetreuungsmitteilung(mitteilung: TSMitteilung): boolean {
        return mitteilung instanceof TSBetreuungsmitteilung;
    }

    private isNeueVeranlagungsmitteilung(mitteilung: TSMitteilung): boolean {
        return (
            mitteilung.empfaengerTyp === TSMitteilungTeilnehmerTyp.JUGENDAMT &&
            mitteilung.senderTyp === TSMitteilungTeilnehmerTyp.JUGENDAMT
        );
    }

    public isCurrentUserAmt(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getAdministratorOrAmtRole()
        );
    }

    public isCurrentMitteilungSozialdienst(): boolean {
        return EbeguUtil.isNotNullOrUndefined(
            this.currentMitteilung.dossier.fall.sozialdienstFall
        );
    }

    public isMitteilungEmpfaengerSozialdienst(
        mitteilung: TSMitteilung
    ): boolean {
        return (
            mitteilung.empfaengerTyp === TSMitteilungTeilnehmerTyp.SOZIALDIENST
        );
    }

    public changeEmpfaenger(): void {
        this.currentMitteilung.institution = this.empfaenger?.key
            ? this.empfaenger.key
            : undefined;
    }

    private isCurrentUserTraegerschaft(): boolean {
        return this.authServiceRS.isOneOfRoles(
            TSRoleUtil.getTraegerschaftOnlyRoles()
        );
    }
}
