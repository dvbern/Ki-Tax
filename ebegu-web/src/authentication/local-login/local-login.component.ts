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
import {ApplicationPropertyRS} from '../../app/core/rest-services/applicationPropertyRS.rest';
import {GemeindeRS} from '../../gesuch/service/gemeindeRS.rest';
import {TSRole} from '../../models/enums/TSRole';
import {TSSozialdienst} from '../../models/sozialdienst/TSSozialdienst';
import {TSBenutzer} from '../../models/TSBenutzer';
import {TSGemeinde} from '../../models/TSGemeinde';
import {TSInstitution} from '../../models/TSInstitution';
import {TSMandant} from '../../models/TSMandant';
import {TSTraegerschaft} from '../../models/TSTraegerschaft';
import {returnToOriginalState} from '../../utils/AuthenticationUtil';
import {AuthServiceRS} from '../service/AuthServiceRS.rest';

// tslint:disable:no-duplicate-string no-identical-functions
@Component({
    selector: 'dv-local-login',
    templateUrl: './local-login.component.html',
    styleUrls: ['./local-login.component.less'],
})
export class LocalLoginComponent {

    private static readonly ID_PARIS = 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';
    private static readonly ID_LONDON = '80a8e496-b73c-4a4a-a163-a0b2caf76487';

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
    private readonly mandant: TSMandant;
    private gemeindeParis: TSGemeinde;
    private gemeindeLondon: TSGemeinde;
    private readonly institution: TSInstitution;
    private readonly tagesschule: TSInstitution;
    private readonly traegerschaftStadtBern: TSTraegerschaft;
    private readonly sozialdienst: TSSozialdienst;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly stateService: StateService,
        private readonly gemeindeRS: GemeindeRS,
    ) {

        this.mandant = LocalLoginComponent.getMandant();
        this.traegerschaftStadtBern = LocalLoginComponent.getTraegerschaftStadtBern();
        this.institution = this.getInsitution();
        this.tagesschule = this.getTagesschule();
        this.sozialdienst = this.getSozialdienst();
        this.applicationPropertyRS.isDevMode().then(response => {
            this.devMode = response;
        });

        // getAktiveGemeinden() can be called by anonymous.
        this.gemeindeRS.getAktiveGemeinden().then(aktiveGemeinden => {
            this.gemeindeParis = aktiveGemeinden
                .find(gemeinde => gemeinde.id === LocalLoginComponent.ID_PARIS);
            this.gemeindeLondon = aktiveGemeinden
                .find(gemeinde => gemeinde.id === LocalLoginComponent.ID_LONDON);

            this.initUsers();
        });
    }

    /**
     * Der Mandant wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getMandant(): TSMandant {
        const mandant = new TSMandant();
        mandant.name = 'TestMandant';
        mandant.id = 'e3736eb8-6eef-40ef-9e52-96ab48d8f220';
        return mandant;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTraegerschaftStadtBern(): TSTraegerschaft {
        const traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'Kitas Stadt Bern';
        traegerschaft.id = 'f9ddee82-81a1-4cda-b273-fb24e9299308';
        return traegerschaft;
    }

    private initUsers(): void {
        this.createGeneralUsers();
        this.createUsersOfParis();
        this.createUsersOfLondon();
        this.createUsersOfBothParisAndLondon();
    }

    private createGeneralUsers(): void {
        this.superadmin = new TSBenutzer('E-BEGU',
            'Superuser',
            'ebegu',
            'password10',
            'superuser@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SUPER_ADMIN);
        this.administratorKantonBern = new TSBenutzer('Bernhard',
            'Röthlisberger',
            'robe',
            'password1',
            'bernhard.roethlisberger@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_MANDANT);
        this.sachbearbeiterKantonBern = new TSBenutzer('Benno',
            'Röthlisberger',
            'brbe',
            'password1',
            'benno.roethlisberger@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_MANDANT);
        this.administratorInstitutionKitaBruennen = new TSBenutzer('Silvia',
            'Bergmann',
            'besi',
            'password1',
            'silvia.bergmann@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_INSTITUTION,
            undefined,
            this.institution);
        this.sachbearbeiterInstitutionKitaBruennen = new TSBenutzer('Sophie',
            'Bergmann',
            'beso',
            'password3',
            'sophie.bergmann@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_INSTITUTION,
            undefined,
            this.institution);
        this.administratorInstitutionTagesschuleParis = new TSBenutzer('Serge',
            'Gainsbourg',
            'serge.gainsbourg@mailbucket.dvbern.ch',
            'password1',
            'serge.gainsbourg@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_INSTITUTION,
            undefined,
            this.tagesschule);
        this.sachbearbeiterInstitutionTagesschuleParis = new TSBenutzer('Charlotte',
            'Gainsbourg',
            'charlotte.gainsbourg@mailbucket.dvbern.ch',
            'password3',
            'charlotte.gainsbourg@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_INSTITUTION,
            undefined,
            this.tagesschule);
        this.sachbearbeiterTraegerschaftStadtBern = new TSBenutzer('Agnes',
            'Krause',
            'krad',
            'password4',
            'agnes.krause@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            this.traegerschaftStadtBern);
        this.administratorTraegerschaftStadtBern = new TSBenutzer('Bernhard',
            'Bern',
            'lelo',
            'password1',
            'bernhard.bern@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_TRAEGERSCHAFT,
            this.traegerschaftStadtBern);
        this.gesuchstellerEmmaGerber = new TSBenutzer('Emma',
            'Gerber',
            'geem',
            'password6',
            'emma.gerber@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerHeinrichMueller = new TSBenutzer('Heinrich',
            'Mueller',
            'muhe',
            'password6',
            'heinrich.mueller@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerMichaelBerger = new TSBenutzer('Michael',
            'Berger',
            'bemi',
            'password6',
            'michael.berger@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerHansZimmermann = new TSBenutzer('Hans',
            'Zimmermann',
            'ziha',
            'password6',
            'hans.zimmermann@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.administratorSozialdienst = new TSBenutzer('Patrick',
            'Melcher',
            'patrick.melcher@mailbucket.dvbern.ch',
            'password1',
            'patrick.melcher@mailbucket.dvbern.ch',
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
            'max.palmer@mailbucket.dvbern.ch',
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
            'kurt.blaser@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.gemeindeParis]);
        this.sachbearbeiterBGParis = new TSBenutzer('Jörg',
            'Becker',
            'jobe',
            'password1',
            'joerg.becker@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.gemeindeParis]);
        this.administratorTSParis = new TSBenutzer('Adrian',
            'Schuler',
            'scad',
            'password9',
            'adrian.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.gemeindeParis]);
        this.sachbearbeiterTSParis = new TSBenutzer('Julien',
            'Schuler',
            'scju',
            'password9',
            'julien.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.gemeindeParis]);
        this.administratorGemeindeParis = new TSBenutzer('Gerlinde',
            'Hofstetter',
            'hoge',
            'password1',
            'gerlinde.hofstetter@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeParis]);
        this.sachbearbeiterGemeindeParis = new TSBenutzer('Stefan',
            'Wirth',
            'wist',
            'password1',
            'stefan.wirth@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeParis]);
        this.sachbearbeiterinFerienbetreuungGemeindeParis = new TSBenutzer('Marlene',
            'Stöckli',
            'stma',
            'password1',
            'marlene.stoeckli@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.gemeindeParis]);
        this.adminFerienbetreuungGemeindeParis = new TSBenutzer('Sarah',
            'Riesen',
            'risa',
            'password1',
            'sarah.riesen@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.gemeindeParis]);
        this.steueramtParis = new TSBenutzer('Rodolfo',
            'Geldmacher',
            'gero',
            'password11',
            'rodolfo.geldmacher@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.gemeindeParis]);
        this.revisorParis = new TSBenutzer('Reto',
            'Revisor',
            'rere',
            'password9',
            'reto.revisor@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.gemeindeParis]);
        this.juristParis = new TSBenutzer('Julia',
            'Jurist',
            'juju',
            'password9',
            'julia.jurist@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.gemeindeParis]);
    }

    private createUsersOfLondon(): void {
        this.administratorBGLondon = new TSBenutzer('Kurt',
            'Schmid',
            'scku',
            'password1',
            'kurt.blaser@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.gemeindeLondon]);
        this.sachbearbeiterBGLondon = new TSBenutzer('Jörg',
            'Keller',
            'kejo',
            'password1',
            'joerg.becker@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.gemeindeLondon]);
        this.administratorTSLondon = new TSBenutzer('Adrian',
            'Huber',
            'huad',
            'password1',
            'adrian.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.gemeindeLondon]);
        this.sachbearbeiterTSLondon = new TSBenutzer('Julien',
            'Odermatt',
            'odju',
            'password1',
            'julien.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.gemeindeLondon]);
        this.administratorGemeindeLondon = new TSBenutzer('Gerlinde',
            'Bader',
            'bage',
            'password1',
            'gerlinde.bader@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeLondon]);
        this.sachbearbeiterGemeindeLondon = new TSBenutzer('Stefan',
            'Weibel',
            'west',
            'password1',
            'stefan.weibel@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeLondon]);
        this.steueramtLondon = new TSBenutzer('Rodolfo',
            'Iten',
            'itro',
            'password1',
            'rodolfo.geldmacher@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.gemeindeLondon]);
        this.revisorLondon = new TSBenutzer('Reto',
            'Werlen',
            'were',
            'password1',
            'reto.revisor@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.gemeindeLondon]);
        this.juristLondon = new TSBenutzer('Julia',
            'Adler',
            'adju',
            'password1',
            'julia.jurist@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.gemeindeLondon]);
        this.sachbearbeiterinFerienbetreuungGemeindeLondon = new TSBenutzer('Jordan',
            'Hefti',
            'hejo',
            'password1',
            'jordan.hefti@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.gemeindeLondon]);
        this.adminFerienbetreuungGemeindeLondon = new TSBenutzer('Jean-Pierre',
            'Kraeuchi',
            'krjp',
            'password1',
            'jordan.hefti@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.gemeindeLondon]);
    }

    private createUsersOfBothParisAndLondon(): void {
        this.administratorBGParisLondon = new TSBenutzer('Kurt',
            'Kälin',
            'kaku',
            'password1',
            'kurt.blaser@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
        this.sachbearbeiterBGParisLondon = new TSBenutzer('Jörg',
            'Aebischer',
            'aejo',
            'password1',
            'joerg.becker@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
        this.administratorTSParisLondon = new TSBenutzer('Adrian',
            'Bernasconi',
            'bead',
            'password1',
            'adrian.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
        this.sachbearbeiterTSParisLondon = new TSBenutzer('Julien',
            'Bucheli',
            'buju',
            'password1',
            'julien.schuler@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
        this.administratorGemeindeParisLondon = new TSBenutzer('Gerlinde',
            'Mayer',
            'mage',
            'password1',
            'gerlinde.mayer@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
        this.sachbearbeiterGemeindeParisLondon = new TSBenutzer('Stefan',
            'Marti',
            'mast',
            'password1',
            'stefan.marti@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
        this.steueramtParisLondon = new TSBenutzer('Rodolfo',
            'Hermann',
            'hero',
            'password1',
            'rodolfo.geldmacher@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
        this.revisorParisLondon = new TSBenutzer('Reto',
            'Hug',
            'hure',
            'password1',
            'reto.revisor@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
        this.juristParisLondon = new TSBenutzer('Julia',
            'Lory',
            'luju',
            'password1',
            'julia.jurist@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
        this.adminFerienbetreuungGemeindeParisundLondon = new TSBenutzer('Christoph',
            'Hütter',
            'huch',
            'password1',
            'christoph.huetter@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.ADMIN_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
        this.sachbearbeiterinFerienbetreuungGemeindeParisundLondon = new TSBenutzer('Valentin',
            'Burgener',
            'buva',
            'password1',
            'valentin.burgener@mailbucket.dvbern.ch',
            this.mandant,
            TSRole.SACHBEARBEITER_FERIENBETREUUNG,
            undefined,
            undefined,
            [this.gemeindeParis, this.gemeindeLondon]);
    }

    /**
     * Die Institution wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private getInsitution(): TSInstitution {
        const institution = new TSInstitution();
        institution.name = 'Kita Brünnen';
        institution.id = '1b6f476f-e0f5-4380-9ef6-836d688853a3';
        institution.traegerschaft = this.traegerschaftStadtBern;
        institution.mandant = this.mandant;
        return institution;
    }

    /**
     * Die Tagesschule Institution wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private getTagesschule(): TSInstitution {
        const tagesschule = new TSInstitution();
        tagesschule.name = 'Tagesschule Paris';
        tagesschule.id = 'f44a68f2-dda2-4bf2-936a-68e20264b610';
        tagesschule.mandant = this.mandant;
        return tagesschule;
    }

    public logIn(credentials: TSBenutzer): void {
        this.authServiceRS.loginRequest(credentials)
            .then(() => returnToOriginalState(this.stateService, this.returnTo));
    }

    private getSozialdienst(): TSSozialdienst {
        const sozialdienst = new TSSozialdienst();
        sozialdienst.name = 'BernerSozialdienst';
        sozialdienst.id = 'f44a68f2-dda2-4bf2-936a-68e20264b620';
        return sozialdienst;
    }
}
