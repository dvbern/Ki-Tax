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
import {TSGemeindeStatus} from '../../models/enums/TSGemeindeStatus';
import {TSRole} from '../../models/enums/TSRole';
import TSBenutzer from '../../models/TSBenutzer';
import TSGemeinde from '../../models/TSGemeinde';
import TSInstitution from '../../models/TSInstitution';
import {TSMandant} from '../../models/TSMandant';
import {TSTraegerschaft} from '../../models/TSTraegerschaft';
import {returnToOriginalState} from '../../utils/AuthenticationUtil';
import AuthServiceRS from '../service/AuthServiceRS.rest';

// tslint:disable:no-duplicate-string no-identical-functions
@Component({
    selector: 'dv-local-login',
    templateUrl: './local-login.component.html',
    styleUrls: ['./local-login.component.less'],
})
export class LocalLoginComponent {

    private static readonly BFS_BERN = 351;
    private static readonly BFS_OSTERMUNDIGEN = 363;

    @Input() public returnTo: TargetState;

    // Allgemeine User
    public superadmin: TSBenutzer;

    public administratorKantonBern: TSBenutzer;
    public sachbearbeiterKantonBern: TSBenutzer;

    public administratorInstitutionKitaBruennen: TSBenutzer;
    public sachbearbeiterInstitutionKitaBruennen: TSBenutzer;
    public sachbearbeiterTraegerschaftStadtBern: TSBenutzer;
    public administratorTraegerschaftLeoLea: TSBenutzer;
    public sachbearbeiterTraegerschaftLeoLea: TSBenutzer;
    public sachbearbeiterTraegerschaftSGF: TSBenutzer;

    public gesuchstellerEmmaGerber: TSBenutzer;
    public gesuchstellerHeinrichMueller: TSBenutzer;
    public gesuchstellerMichaelBerger: TSBenutzer;
    public gesuchstellerHansZimmermann: TSBenutzer;

    // Gemeindeabhängige User
    public administratorBGBern: TSBenutzer;
    public sachbearbeiterBGBern: TSBenutzer;
    public administratorTSBern: TSBenutzer;
    public sachbearbeiterTSBern: TSBenutzer;
    public administratorGemeindeBern: TSBenutzer;
    public sachbearbeiterGemeindeBern: TSBenutzer;

    public administratorBGOstermundigen: TSBenutzer;
    public sachbearbeiterBGOstermundigen: TSBenutzer;
    public administratorTSOstermundigen: TSBenutzer;
    public sachbearbeiterTSOstermundigen: TSBenutzer;
    public administratorGemeindeOstermundigen: TSBenutzer;
    public sachbearbeiterGemeindeOstermundigen: TSBenutzer;

    public administratorBGBernOstermundigen: TSBenutzer;
    public sachbearbeiterBGBernOstermundigen: TSBenutzer;
    public administratorTSBernOstermundigen: TSBenutzer;
    public sachbearbeiterTSBernOstermundigen: TSBenutzer;
    public administratorGemeindeBernOstermundigen: TSBenutzer;
    public sachbearbeiterGemeindeBernOstermundigen: TSBenutzer;

    public steueramtBern: TSBenutzer;
    public revisorBern: TSBenutzer;
    public juristBern: TSBenutzer;

    public steueramtOstermundigen: TSBenutzer;
    public revisorOstermundigen: TSBenutzer;
    public juristOstermundigen: TSBenutzer;

    public steueramtBernOstermundigen: TSBenutzer;
    public revisorBernOstermundigen: TSBenutzer;
    public juristBernOstermundigen: TSBenutzer;

    public devMode: boolean;

    private readonly mandant: TSMandant;
    private readonly gemeindeBern: TSGemeinde;
    private readonly gemeindeOstermundigen: TSGemeinde;
    private readonly institution: TSInstitution;
    private readonly traegerschaftStadtBern: TSTraegerschaft;
    private readonly traegerschaftLeoLea: TSTraegerschaft;
    private readonly traegerschaftSGF: TSTraegerschaft;

    public constructor(
        private readonly authServiceRS: AuthServiceRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly stateService: StateService,
    ) {

        this.mandant = LocalLoginComponent.getMandant();
        this.gemeindeBern = LocalLoginComponent.getGemeindeBern();
        this.gemeindeOstermundigen = LocalLoginComponent.getGemeindeOstermundigen();
        this.traegerschaftStadtBern = LocalLoginComponent.getTraegerschaftStadtBern();
        this.traegerschaftLeoLea = LocalLoginComponent.getTraegerschaftLeoLea();
        this.traegerschaftSGF = LocalLoginComponent.getTraegerschaftSGF();
        this.institution = this.getInsitution();
        this.applicationPropertyRS.isDevMode().then(response => {
            this.devMode = response;
        });
        this.initUsers();
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
     * Gemeinde Bern wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getGemeindeBern(): TSGemeinde {
        const bern = new TSGemeinde();
        bern.name = 'Bern';
        bern.id = 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';
        bern.gemeindeNummer = 1;
        bern.bfsNummer = LocalLoginComponent.BFS_BERN;
        bern.status = TSGemeindeStatus.AKTIV;
        return bern;
    }

    /**
     * Gemeinde Ostermundigen wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getGemeindeOstermundigen(): TSGemeinde {
        const ostermundigen = new TSGemeinde();
        ostermundigen.name = 'Ostermundigen';
        ostermundigen.id = '80a8e496-b73c-4a4a-a163-a0b2caf76487';
        ostermundigen.gemeindeNummer = 2;
        ostermundigen.bfsNummer = LocalLoginComponent.BFS_OSTERMUNDIGEN;
        ostermundigen.status = TSGemeindeStatus.AKTIV;
        return ostermundigen;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTraegerschaftStadtBern(): TSTraegerschaft {
        const traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'Kitas & Tagis Stadt Bern';
        traegerschaft.mail = 'kitasundtagis@example.com';
        traegerschaft.id = 'f9ddee82-81a1-4cda-b273-fb24e9299308';
        return traegerschaft;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTraegerschaftLeoLea(): TSTraegerschaft {
        const traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'LeoLea';
        traegerschaft.mail = 'leolea@example.com';
        traegerschaft.id = 'd667e2d0-3702-4933-8fb7-be7a39755232';
        return traegerschaft;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTraegerschaftSGF(): TSTraegerschaft {
        const traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'SGF';
        traegerschaft.mail = 'sgf@example.com';
        traegerschaft.id = 'bb5d4bd8-84c9-4cb6-8134-a97312dead67';
        return traegerschaft;
    }

    private initUsers(): void {
        this.createGeneralUsers();
        this.createUsersOfBern();
        this.createUsersOfOstermundigen();
        this.createUsersOfBothBernAndOstermundigen();
    }

    private createGeneralUsers(): void {
        this.superadmin = new TSBenutzer('E-BEGU',
            'Superuser',
            'ebegu',
            'password10',
            'superuser@example.com',
            this.mandant,
            TSRole.SUPER_ADMIN);
        this.administratorKantonBern = new TSBenutzer('Bernhard',
            'Röthlisberger',
            'robe',
            'password1',
            'anyone@example.com',
            this.mandant,
            TSRole.ADMIN_MANDANT);
        this.sachbearbeiterKantonBern = new TSBenutzer('Benno',
            'Röthlisberger',
            'brbe',
            'password1',
            'anyone@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_MANDANT);
        this.administratorInstitutionKitaBruennen = new TSBenutzer('Silvia',
            'Bergmann',
            'besi',
            'password1',
            'anyone@example.com',
            this.mandant,
            TSRole.ADMIN_INSTITUTION,
            undefined,
            this.institution);
        this.sachbearbeiterInstitutionKitaBruennen = new TSBenutzer('Sophie',
            'Bergmann',
            'beso',
            'password3',
            'sophie.bergmann@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_INSTITUTION,
            undefined,
            this.institution);
        this.sachbearbeiterTraegerschaftStadtBern = new TSBenutzer('Agnes',
            'Krause',
            'krad',
            'password4',
            'agnes.krause@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            this.traegerschaftStadtBern);
        this.administratorTraegerschaftLeoLea = new TSBenutzer('Leo',
            'Lehmann',
            'lelo',
            'password1',
            'anyone@gexample.com',
            this.mandant,
            TSRole.ADMIN_TRAEGERSCHAFT,
            this.traegerschaftLeoLea);
        this.sachbearbeiterTraegerschaftLeoLea = new TSBenutzer('Lea',
            'Lehmann',
            'lele',
            'password7',
            'lea.lehmann@gexample.com',
            this.mandant,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            this.traegerschaftLeoLea);
        this.sachbearbeiterTraegerschaftSGF = new TSBenutzer('Simon',
            'Gfeller',
            'gfsi',
            'password8',
            'simon.gfeller@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_TRAEGERSCHAFT,
            this.traegerschaftSGF);
        this.gesuchstellerEmmaGerber = new TSBenutzer('Emma',
            'Gerber',
            'geem',
            'password6',
            'emma.gerber@example.com',
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerHeinrichMueller = new TSBenutzer('Heinrich',
            'Mueller',
            'muhe',
            'password6',
            'heinrich.mueller@example.com',
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerMichaelBerger = new TSBenutzer('Michael',
            'Berger',
            'bemi',
            'password6',
            'michael.berger@example.com',
            this.mandant,
            TSRole.GESUCHSTELLER);
        this.gesuchstellerHansZimmermann = new TSBenutzer('Hans',
            'Zimmermann',
            'ziha',
            'password6',
            'hans.zimmermann@example.com',
            this.mandant,
            TSRole.GESUCHSTELLER);
    }

    private createUsersOfBern(): void {
        this.administratorBGBern = new TSBenutzer('Kurt',
            'Blaser',
            'blku',
            'password5',
            'kurt.blaser@example.com',
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.sachbearbeiterBGBern = new TSBenutzer('Jörg',
            'Becker',
            'jobe',
            'password1',
            'joerg.becker@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.administratorTSBern = new TSBenutzer('Adrian',
            'Schuler',
            'scad',
            'password9',
            'adrian.schuler@example.com',
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.sachbearbeiterTSBern = new TSBenutzer('Julien',
            'Schuler',
            'scju',
            'password9',
            'julien.schuler@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.administratorGemeindeBern = new TSBenutzer('Gerlinde',
            'Hofstetter',
            'hoge',
            'password1',
            'anyone@example.com',
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.sachbearbeiterGemeindeBern = new TSBenutzer('Stefan',
            'Wirth',
            'wist',
            'password1',
            'anyone@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.steueramtBern = new TSBenutzer('Rodolfo',
            'Geldmacher',
            'gero',
            'password11',
            'rodolfo.geldmacher@example.com',
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.revisorBern = new TSBenutzer('Reto',
            'Revisor',
            'rere',
            'password9',
            'reto.revisor@example.com',
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.gemeindeBern]);
        this.juristBern = new TSBenutzer('Julia',
            'Jurist',
            'juju',
            'password9',
            'julia.jurist@example.com',
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.gemeindeBern]);
    }

    private createUsersOfOstermundigen(): void {
        this.administratorBGOstermundigen = new TSBenutzer('Kurt',
            'Schmid',
            'scku',
            'password1',
            'kurt.blaser@example.com',
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.sachbearbeiterBGOstermundigen = new TSBenutzer('Jörg',
            'Keller',
            'kejo',
            'password1',
            'joerg.becker@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.administratorTSOstermundigen = new TSBenutzer('Adrian',
            'Huber',
            'huad',
            'password1',
            'adrian.schuler@example.com',
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.sachbearbeiterTSOstermundigen = new TSBenutzer('Julien',
            'Odermatt',
            'odju',
            'password1',
            'julien.schuler@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.administratorGemeindeOstermundigen = new TSBenutzer('Gerlinde',
            'Bader',
            'bage',
            'password1',
            'anyone@example.com',
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.sachbearbeiterGemeindeOstermundigen = new TSBenutzer('Stefan',
            'Weibel',
            'west',
            'password1',
            'anyone@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.steueramtOstermundigen = new TSBenutzer('Rodolfo',
            'Iten',
            'itro',
            'password1',
            'rodolfo.geldmacher@example.com',
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.revisorOstermundigen = new TSBenutzer('Reto',
            'Werlen',
            'were',
            'password1',
            'reto.revisor@example.com',
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
        this.juristOstermundigen = new TSBenutzer('Julia',
            'Adler',
            'adju',
            'password1',
            'julia.jurist@example.com',
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.gemeindeOstermundigen]);
    }

    private createUsersOfBothBernAndOstermundigen(): void {
        this.administratorBGBernOstermundigen = new TSBenutzer('Kurt',
            'Kälin',
            'kaku',
            'password1',
            'kurt.blaser@example.com',
            this.mandant,
            TSRole.ADMIN_BG,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.sachbearbeiterBGBernOstermundigen = new TSBenutzer('Jörg',
            'Aebischer',
            'aejo',
            'password1',
            'joerg.becker@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_BG,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.administratorTSBernOstermundigen = new TSBenutzer('Adrian',
            'Bernasconi',
            'bead',
            'password1',
            'adrian.schuler@example.com',
            this.mandant,
            TSRole.ADMIN_TS,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.sachbearbeiterTSBernOstermundigen = new TSBenutzer('Julien',
            'Bucheli',
            'buju',
            'password1',
            'julien.schuler@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_TS,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.administratorGemeindeBernOstermundigen = new TSBenutzer('Gerlinde',
            'Mayer',
            'mage',
            'password1',
            'anyone@example.com',
            this.mandant,
            TSRole.ADMIN_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.sachbearbeiterGemeindeBernOstermundigen = new TSBenutzer('Stefan',
            'Marti',
            'mast',
            'password1',
            'anyone@example.com',
            this.mandant,
            TSRole.SACHBEARBEITER_GEMEINDE,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.steueramtBernOstermundigen = new TSBenutzer('Rodolfo',
            'Hermann',
            'hero',
            'password1',
            'rodolfo.geldmacher@example.com',
            this.mandant,
            TSRole.STEUERAMT,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.revisorBernOstermundigen = new TSBenutzer('Reto',
            'Hug',
            'hure',
            'password1',
            'reto.revisor@example.com',
            this.mandant,
            TSRole.REVISOR,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.juristBernOstermundigen = new TSBenutzer('Julia',
            'Lory',
            'luju',
            'password1',
            'julia.jurist@example.com',
            this.mandant,
            TSRole.JURIST,
            undefined,
            undefined,
            [this.gemeindeBern, this.gemeindeOstermundigen]);
    }

    /**
     * Die Institution wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private getInsitution(): TSInstitution {
        const institution = new TSInstitution();
        institution.name = 'Kita Brünnen';
        institution.id = '1b6f476f-e0f5-4380-9ef6-836d688853a3';
        institution.mail = 'kita.bruennen@example.com';
        institution.traegerschaft = this.traegerschaftStadtBern;
        institution.mandant = this.mandant;
        return institution;
    }

    public logIn(credentials: TSBenutzer): void {
        this.authServiceRS.loginRequest(credentials)
            .then(() => returnToOriginalState(this.stateService, this.returnTo));
    }
}
