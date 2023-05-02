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

import {Component, Input} from '@angular/core';
import {StateService, TargetState} from '@uirouter/core';
import {LocalLoginDaten} from '../../app/core/constants/LOCALLOGIN_DATA';
import {MandantLocalLoginDatenVisitor} from '../../app/core/constants/MandantLocalLoginDatenVisitor';
import {KiBonMandant} from '../../app/core/constants/MANDANTS';
import {LogFactory} from '../../app/core/logging/LogFactory';
import {ApplicationPropertyRS} from '../../app/core/rest-services/applicationPropertyRS.rest';
import {MandantService} from '../../app/shared/services/mandant.service';
import {GemeindeRS} from '../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import {TSSozialdienst} from '../../models/sozialdienst/TSSozialdienst';
import {TSBenutzer} from '../../models/TSBenutzer';
import {TSGemeinde} from '../../models/TSGemeinde';
import {TSInstitution} from '../../models/TSInstitution';
import {TSMandant} from '../../models/TSMandant';
import {TSTraegerschaft} from '../../models/TSTraegerschaft';
import {returnToOriginalState} from '../../utils/AuthenticationUtil';
import {EbeguUtil} from '../../utils/EbeguUtil';
import {AuthServiceRS} from '../service/AuthServiceRS.rest';

const LOG = LogFactory.createLog('LocalLoginComponent');

/* eslint-disable */
@Component({
    selector: 'dv-local-login',
    templateUrl: './local-login.component.html',
    styleUrls: ['./local-login.component.less'],
})
export class LocalLoginComponent {

    @Input() public returnTo: TargetState;

    // Allgemeine User
    public superadmin: TSBenutzer;

    public administratorKantonBern: TSBenutzer;
    public sachbearbeiterKantonBern: TSBenutzer;

    public administratorInstitutionKitaBruennen: TSBenutzer;
    public sachbearbeiterInstitutionKitaBruennen: TSBenutzer;
    public administratorInstitutionTagesschuleParis: TSBenutzer;
    public sachbearbeiterInstitutionTagesschuleParis: TSBenutzer;
    public sachbearbeiterTraegerschaftStadtBern: TSBenutzer;
    public administratorTraegerschaftStadtBern: TSBenutzer;

    public gesuchstellerEmmaGerber: TSBenutzer;
    public gesuchstellerHeinrichMueller: TSBenutzer;
    public gesuchstellerMichaelBerger: TSBenutzer;
    public gesuchstellerHansZimmermann: TSBenutzer;
    public gesuchstellerJeanChambre: TSBenutzer;

    // Gemeindeabhängige User
    public administratorBGParis: TSBenutzer;
    public sachbearbeiterBGParis: TSBenutzer;
    public administratorTSParis: TSBenutzer;
    public sachbearbeiterTSParis: TSBenutzer;
    public administratorGemeindeParis: TSBenutzer;
    public sachbearbeiterGemeindeParis: TSBenutzer;

    public administratorBGLondon: TSBenutzer;
    public sachbearbeiterBGLondon: TSBenutzer;
    public administratorTSLondon: TSBenutzer;
    public sachbearbeiterTSLondon: TSBenutzer;
    public administratorGemeindeLondon: TSBenutzer;
    public sachbearbeiterGemeindeLondon: TSBenutzer;

    public administratorBGParisLondon: TSBenutzer;
    public sachbearbeiterBGParisLondon: TSBenutzer;
    public administratorTSParisLondon: TSBenutzer;
    public sachbearbeiterTSParisLondon: TSBenutzer;
    public administratorGemeindeParisLondon: TSBenutzer;
    public sachbearbeiterGemeindeParisLondon: TSBenutzer;

    public steueramtParis: TSBenutzer;
    public revisorParis: TSBenutzer;
    public juristParis: TSBenutzer;

    public steueramtLondon: TSBenutzer;
    public revisorLondon: TSBenutzer;
    public juristLondon: TSBenutzer;

    public steueramtParisLondon: TSBenutzer;
    public revisorParisLondon: TSBenutzer;
    public juristParisLondon: TSBenutzer;

    public administratorSozialdienst: TSBenutzer;
    public sachbearbeiterSozialdienst: TSBenutzer;

    public sachbearbeiterinFerienbetreuungGemeindeParis: TSBenutzer;
    public adminFerienbetreuungGemeindeParis: TSBenutzer;
    public sachbearbeiterinFerienbetreuungGemeindeLondon: TSBenutzer;
    public adminFerienbetreuungGemeindeLondon: TSBenutzer;
    public sachbearbeiterinFerienbetreuungGemeindeParisundLondon: TSBenutzer;
    public adminFerienbetreuungGemeindeParisundLondon: TSBenutzer;

    public devMode: boolean;
    private mandant: TSMandant;
    private mandantHostname: string;
    private defaultGemeinde: TSGemeinde;
    private secondGemeinde: TSGemeinde;
    private institution: TSInstitution;
    private tagesschule: TSInstitution;
    private traegerschaftStadtBern: TSTraegerschaft;
    private sozialdienst: TSSozialdienst;
    public hasSecondGemeinde: boolean;

    public localLoginDaten: LocalLoginDaten;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly stateService: StateService,
        private readonly gemeindeRS: GemeindeRS,
        private readonly mandantService: MandantService,
    ) {
        this.mandantService.mandant$.subscribe(mandant => {
            this.mandantHostname = mandant.hostname;
            this.localLoginDaten = LocalLoginComponent.getLocalLoginDaten(mandant);
            this.hasSecondGemeinde = EbeguUtil.isNotNullOrUndefined(this.localLoginDaten.second_gemeinde);
            this.createLocalLoginInstances(this.localLoginDaten);

            this.gemeindeRS.getAktiveGemeinden().then(aktiveGemeinden => {
                this.defaultGemeinde = aktiveGemeinden
                    .find(gemeinde => gemeinde.id === this.localLoginDaten.default_gemeinde.id);
                if (this.hasSecondGemeinde) {
                    this.secondGemeinde = aktiveGemeinden
                        .find(gemeinde => gemeinde.id === this.localLoginDaten.second_gemeinde.id);
                }

                this.initUsers(this.hasSecondGemeinde);
            });
        }, error => LOG.error(error));

        this.applicationPropertyRS.isDevMode().then(response => {
            this.devMode = response;
        });
    }

    private static getLocalLoginDaten(mandant: KiBonMandant): LocalLoginDaten {
        return new MandantLocalLoginDatenVisitor().process(mandant);
    }

    /**
     * Der Mandant wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getMandant(mandantDatum: { name: string; id: string }): TSMandant {
        const mandant = new TSMandant();
        mandant.name = mandantDatum.name;
        mandant.id = mandantDatum.id;
        return mandant;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTraegerschaftStadtBern(traegerschaftDatum: { name: string; id: string }): TSTraegerschaft {
        const traegerschaft = new TSTraegerschaft();
        traegerschaft.name = traegerschaftDatum.name;
        traegerschaft.id = traegerschaftDatum.id;
        return traegerschaft;
    }

    /**
     * Die Institution wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getInstitution(
        institutionDatum: { name: string; id: string },
        traegerschaft: TSTraegerschaft,
        mandant: TSMandant,
    ): TSInstitution {
        const institution = new TSInstitution();
        institution.name = institutionDatum.name;
        institution.id = institutionDatum.id;
        institution.traegerschaft = traegerschaft;
        institution.mandant = mandant;
        return institution;
    }

    /**
     * Die Tagesschule Institution wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTagesschule(institutionDatum: { name: string; id: string }, mandant: TSMandant): TSInstitution {
        const tagesschule = new TSInstitution();
        tagesschule.name = institutionDatum.name;
        tagesschule.id = institutionDatum.id;
        tagesschule.mandant = mandant;
        return tagesschule;
    }

    private static getSozialdienst(datum: { name: string; id: string; }): TSSozialdienst {
        const sozialdienst = new TSSozialdienst();
        sozialdienst.name = datum.name;
        sozialdienst.id = datum.id;
        return sozialdienst;
    }

    private createLocalLoginInstances(datum: LocalLoginDaten): void {
        this.mandant = LocalLoginComponent.getMandant(datum.mandant);
        this.traegerschaftStadtBern = LocalLoginComponent.getTraegerschaftStadtBern(datum.traegerschaft);
        this.institution =
            LocalLoginComponent.getInstitution(datum.institution, this.traegerschaftStadtBern, this.mandant);
        this.tagesschule = LocalLoginComponent.getTagesschule(datum.tagesschule, this.mandant);
        this.sozialdienst = LocalLoginComponent.getSozialdienst(datum.sozialdienst);
    }

    private initUsers(hasGemeindeLondon: boolean): void {
        this.createGeneralUsers();
        this.createUsersOfParis();
        if (!hasGemeindeLondon) {
            return;
        }
        this.createUsersOfLondon();
        this.createUsersOfBothParisAndLondon();
    }

    private createGeneralUsers(): void {
        this.superadmin = new TSBenutzer('E-BEGU',
            'Superuser',
            `ebegu`,
            'password10',
            `superuser.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SUPER_ADMIN);
        this.administratorKantonBern = new TSBenutzer('Bernhard',
            'Röthlisberger',
            'robe',
            'password1',
            `bernhard.roethlisberger.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_MANDANT);
        this.sachbearbeiterKantonBern = new TSBenutzer('Benno',
            'Röthlisberger',
            'brbe',
            'password1',
            `benno.roethlisberger.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_MANDANT);
        this.administratorInstitutionKitaBruennen = new TSBenutzer('Silvia',
            'Bergmann',
            'besi',
            'password1',
            `silvia.bergmann.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_INSTITUTION,
            undefined,
            this.institution);
        this.sachbearbeiterInstitutionKitaBruennen = new TSBenutzer('Sophie',
            'Bergmann',
            'beso',
            'password3',
            `sophie.bergmann.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_INSTITUTION,
            undefined,
            this.institution);
        this.administratorInstitutionTagesschuleParis = new TSBenutzer('Serge',
            'Gainsbourg',
            'serge.gainsbourg@mailbucket.dvbern.ch',
            'password1',
            `serge.gainsbourg.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_INSTITUTION,
            undefined,
            this.tagesschule);
        this.sachbearbeiterInstitutionTagesschuleParis = new TSBenutzer('Charlotte',
            'Gainsbourg',
            'charlotte.gainsbourg@mailbucket.dvbern.ch',
            'password3',
            `charlotte.gainsbourg.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_INSTITUTION,
            undefined,
            this.tagesschule);
        this.sachbearbeiterTraegerschaftStadtBern = new TSBenutzer('Agnes',
            'Krause',
            'krad',
            'password4',
            `agnes.krause.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            this.traegerschaftStadtBern);
        this.administratorTraegerschaftStadtBern = new TSBenutzer('Bernhard',
            'Bern',
            'lelo',
            'password1',
            `bernhard.bern.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_TRAEGERSCHAFT,
            this.traegerschaftStadtBern);
        this.gesuchstellerEmmaGerber = new TSBenutzer('Emma',
            'Gerber',
            'geem',
            'password6',
            `emma.gerber.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerHeinrichMueller = new TSBenutzer('Heinrich',
            'Mueller',
            'muhe',
            'password6',
            `heinrich.mueller.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerMichaelBerger = new TSBenutzer('Michael',
            'Berger',
            'bemi',
            'password6',
            `michael.berger.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerHansZimmermann = new TSBenutzer('Hans',
            'Zimmermann',
            'ziha',
            'password6',
            `hans.zimmermann.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerJeanChambre = new TSBenutzer('Jean',
            'Chambre',
            'chje',
            'password6',
            `jean.chambre.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.administratorSozialdienst = new TSBenutzer('Patrick',
            'Melcher',
            'patrick.melcher@mailbucket.dvbern.ch',
            'password1',
            `patrick.melcher.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_SOZIALDIENST,
            undefined,
            undefined,
            undefined,
            this.sozialdienst);
        this.sachbearbeiterSozialdienst = new TSBenutzer('Max',
            'Palmer',
            'max.palmer@mailbucket.dvbern.ch',
            'password1',
            `max.palmer.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_SOZIALDIENST,
            undefined,
            undefined,
            undefined,
            this.sozialdienst);
    }

    private createUsersOfParis(): void {
        this.administratorBGParis = new TSBenutzer('Kurt',
            'Blaser',
            'blku',
            'password5',
            `kurt.blaser.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.defaultGemeinde]);
        this.sachbearbeiterBGParis = new TSBenutzer('Jörg',
            'Becker',
            'jobe',
            'password1',
            `joerg.becker.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.defaultGemeinde]);
        this.administratorTSParis = new TSBenutzer('Adrian',
            'Schuler',
            'scad',
            'password9',
            `adrian.schuler.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.defaultGemeinde]);
        this.sachbearbeiterTSParis = new TSBenutzer('Julien',
            'Schuler',
            'scju',
            'password9',
            `julien.schuler.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.defaultGemeinde]);
        this.administratorGemeindeParis = new TSBenutzer('Gerlinde',
            'Hofstetter',
            'hoge',
            'password1',
            `gerlinde.hofstetter.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.defaultGemeinde]);
        this.sachbearbeiterGemeindeParis = new TSBenutzer('Stefan',
            'Wirth',
            'wist',
            'password1',
            `stefan.wirth.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.defaultGemeinde]);
        this.sachbearbeiterinFerienbetreuungGemeindeParis = new TSBenutzer('Marlene',
            'Stöckli',
            'stma',
            'password1',
            `marlene.stoeckli.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.defaultGemeinde]);
        this.adminFerienbetreuungGemeindeParis = new TSBenutzer('Sarah',
            'Riesen',
            'risa',
            'password1',
            `sarah.riesen.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.defaultGemeinde]);
        this.steueramtParis = new TSBenutzer('Rodolfo',
            'Geldmacher',
            'gero',
            'password11',
            `rodolfo.geldmacher.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.defaultGemeinde]);
        this.revisorParis = new TSBenutzer('Reto',
            'Revisor',
            'rere',
            'password9',
            `reto.revisor.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.defaultGemeinde]);
        this.juristParis = new TSBenutzer('Julia',
            'Jurist',
            'juju',
            'password9',
            `julia.jurist.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.defaultGemeinde]);
    }

    private createUsersOfLondon(): void {
        this.administratorBGLondon = new TSBenutzer('Kurt',
            'Schmid',
            'scku',
            'password1',
            `kurt.schmid.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.secondGemeinde]);
        this.sachbearbeiterBGLondon = new TSBenutzer('Jörg',
            'Keller',
            'kejo',
            'password1',
            `joerg.keller.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.secondGemeinde]);
        this.administratorTSLondon = new TSBenutzer('Adrian',
            'Huber',
            'huad',
            'password1',
            `adrian.huber.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.secondGemeinde]);
        this.sachbearbeiterTSLondon = new TSBenutzer('Julien',
            'Odermatt',
            'odju',
            'password1',
            `julien.odermatt.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.secondGemeinde]);
        this.administratorGemeindeLondon = new TSBenutzer('Gerlinde',
            'Bader',
            'bage',
            'password1',
            `gerlinde.bader.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.secondGemeinde]);
        this.sachbearbeiterGemeindeLondon = new TSBenutzer('Stefan',
            'Weibel',
            'west',
            'password1',
            `stefan.weibel.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.secondGemeinde]);
        this.steueramtLondon = new TSBenutzer('Rodolfo',
            'Iten',
            'itro',
            'password1',
            `rodolfo.iten.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.secondGemeinde]);
        this.revisorLondon = new TSBenutzer('Reto',
            'Werlen',
            'were',
            'password1',
            `reto.werlen.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.secondGemeinde]);
        this.juristLondon = new TSBenutzer('Julia',
            'Adler',
            'adju',
            'password1',
            `julia.adler.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.secondGemeinde]);
        this.sachbearbeiterinFerienbetreuungGemeindeLondon = new TSBenutzer('Jordan',
            'Hefti',
            'hejo',
            'password1',
            `jordan.hefti.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.secondGemeinde]);
        this.adminFerienbetreuungGemeindeLondon = new TSBenutzer('Jean-Pierre',
            'Kraeuchi',
            'krjp',
            'password1',
            `jordan.hefti.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.secondGemeinde]);
    }

    private createUsersOfBothParisAndLondon(): void {
        this.administratorBGParisLondon = new TSBenutzer('Kurt',
            'Kälin',
            'kaku',
            'password1',
            `kurt.kaelin.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
        this.sachbearbeiterBGParisLondon = new TSBenutzer('Jörg',
            'Aebischer',
            'aejo',
            'password1',
            `joerg.aebischer.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
        this.administratorTSParisLondon = new TSBenutzer('Adrian',
            'Bernasconi',
            'bead',
            'password1',
            `adrian.bernasconi.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
        this.sachbearbeiterTSParisLondon = new TSBenutzer('Julien',
            'Bucheli',
            'buju',
            'password1',
            `julien.bucheli.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
        this.administratorGemeindeParisLondon = new TSBenutzer('Gerlinde',
            'Mayer',
            'mage',
            'password1',
            `gerlinde.mayer.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
        this.sachbearbeiterGemeindeParisLondon = new TSBenutzer('Stefan',
            'Marti',
            'mast',
            'password1',
            `stefan.marti.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
        this.steueramtParisLondon = new TSBenutzer('Rodolfo',
            'Hermann',
            'hero',
            'password1',
            `rodolfo.hermann.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
        this.revisorParisLondon = new TSBenutzer('Reto',
            'Hug',
            'hure',
            'password1',
            `reto.hug.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
        this.juristParisLondon = new TSBenutzer('Julia',
            'Lory',
            'luju',
            'password1',
            `julia.lory.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
        this.adminFerienbetreuungGemeindeParisundLondon = new TSBenutzer('Christoph',
            'Hütter',
            'huch',
            'password1',
            `christoph.huetter.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.ADMIN_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
        this.sachbearbeiterinFerienbetreuungGemeindeParisundLondon = new TSBenutzer('Valentin',
            'Burgener',
            'buva',
            'password1',
            `valentin.burgener.${this.mandantHostname}@mailbucket.dvbern.ch`,
            this.mandant,
            TSRole.SACHBEARBEITER_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.defaultGemeinde, this.secondGemeinde]);
    }

    public logIn(credentials: TSBenutzer): void {
        this.authServiceRS.loginRequest(credentials)
            .then(() => returnToOriginalState(this.stateService, this.returnTo));
    }
}
