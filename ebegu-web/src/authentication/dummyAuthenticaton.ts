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

import {Component, Inject} from '@angular/core';
import {UIRouter} from '@uirouter/core';
import {ApplicationPropertyRS} from '../admin/service/applicationPropertyRS.rest';
import {TSRole} from '../models/enums/TSRole';
import TSGemeinde from '../models/TSGemeinde';
import TSInstitution from '../models/TSInstitution';
import {TSMandant} from '../models/TSMandant';
import {TSTraegerschaft} from '../models/TSTraegerschaft';
import TSUser from '../models/TSUser';
import AuthenticationUtil from '../utils/AuthenticationUtil';
import AuthServiceRS from './service/AuthServiceRS.rest';

require('./dummyAuthentication.less');


@Component({
    selector: 'dummy-authentication-view',
    template: require('./dummyAuthentication.html'),
})
export class DummyAuthenticationListViewComponent {

    // Allgemeine User
    public superadmin: TSUser;
    public sachbearbeiterInstitutionKitaBruennen: TSUser;
    public sachbearbeiterTraegerschaftStadtBern: TSUser;
    public sachbearbeiterTraegerschaftLeoLea: TSUser;
    public sachbearbeiterTraegerschaftSGF: TSUser;

    public gesuchstellerEmmaGerber: TSUser;
    public gesuchstellerHeinrichMueller: TSUser;
    public gesuchstellerMichaelBerger: TSUser;
    public gesuchstellerHansZimmermann: TSUser;

    // Gemeindeabhängige User
    public administratorBGBern: TSUser;
    public sachbearbeiterBGBern: TSUser;
    public administratorTSBern: TSUser;
    public sachbearbeiterTSBern: TSUser;

    public administratorBGOstermundigen: TSUser;
    public sachbearbeiterBGOstermundigen: TSUser;
    public administratorTSOstermundigen: TSUser;
    public sachbearbeiterTSOstermundigen: TSUser;

    public administratorBGBernOstermundigen: TSUser;
    public sachbearbeiterBGBernOstermundigen: TSUser;
    public administratorTSBernOstermundigen: TSUser;
    public sachbearbeiterTSBernOstermundigen: TSUser;

    public steueramtBern: TSUser;
    public revisorBern: TSUser;
    public juristBern: TSUser;

    public steueramtOstermundigen: TSUser;
    public revisorOstermundigen: TSUser;
    public juristOstermundigen: TSUser;

    public steueramtBernOstermundigen: TSUser;
    public revisorBernOstermundigen: TSUser;
    public juristBernOstermundigen: TSUser;

    private readonly mandant: TSMandant;
    private readonly gemeindeBern: TSGemeinde;
    private readonly gemeindeOstermundigen: TSGemeinde;
    private readonly institution: TSInstitution;
    private readonly traegerschaftStadtBern: TSTraegerschaft;
    private readonly traegerschaftLeoLea: TSTraegerschaft;
    private readonly traegerschaftSGF: TSTraegerschaft;
    private traegerschaftFamex: TSTraegerschaft;
    private devMode: boolean;


    constructor(@Inject(AuthServiceRS) private readonly authServiceRS: AuthServiceRS,
                @Inject(ApplicationPropertyRS) private readonly applicationPropertyRS: ApplicationPropertyRS,
                @Inject(UIRouter) private readonly uiRouter: UIRouter) {

        this.mandant = DummyAuthenticationListViewComponent.getMandant();
        this.gemeindeBern = DummyAuthenticationListViewComponent.getGemeindeBern();
        this.gemeindeOstermundigen = DummyAuthenticationListViewComponent.getGemeindeOstermundigen();
        this.traegerschaftStadtBern = DummyAuthenticationListViewComponent.getTraegerschaftStadtBern();
        this.traegerschaftLeoLea = DummyAuthenticationListViewComponent.getTraegerschaftLeoLea();
        this.traegerschaftSGF = DummyAuthenticationListViewComponent.getTraegerschaftSGF();
        this.traegerschaftFamex = DummyAuthenticationListViewComponent.getTraegerschaftFamex();
        this.institution = this.getInsitution();
        this.applicationPropertyRS.isDevMode().then((response) => {
            this.devMode = response;
        });
        this.initUsers();
    }

    private initUsers(): void {
        this.createGeneralUsers();
        this.createUsersOfBern();
        this.createUsersOfOstermundigen();
        this.createUsersOfBothBernAndOstermundigen();
    }

    private createGeneralUsers() {
        this.superadmin = new TSUser('E-BEGU', 'Superuser', 'ebegu', 'password10', 'superuser@example.com',
            this.mandant, TSRole.SUPER_ADMIN);

        this.sachbearbeiterInstitutionKitaBruennen = new TSUser('Sophie', 'Bergmann', 'beso', 'password3', 'sophie.bergmann@example.com',
            this.mandant, TSRole.SACHBEARBEITER_INSTITUTION, undefined, this.institution);
        this.sachbearbeiterTraegerschaftStadtBern = new TSUser('Agnes', 'Krause', 'krad', 'password4', 'agnes.krause@example.com',
            this.mandant, TSRole.SACHBEARBEITER_TRAEGERSCHAFT, this.traegerschaftStadtBern);
        this.sachbearbeiterTraegerschaftLeoLea = new TSUser('Lea', 'Lehmann', 'lele', 'password7', 'lea.lehmann@gexample.com',
            this.mandant, TSRole.SACHBEARBEITER_TRAEGERSCHAFT, this.traegerschaftLeoLea);
        this.sachbearbeiterTraegerschaftSGF = new TSUser('Simon', 'Gfeller', 'gfsi', 'password8', 'simon.gfeller@example.com',
            this.mandant, TSRole.SACHBEARBEITER_TRAEGERSCHAFT, this.traegerschaftSGF);

        this.gesuchstellerEmmaGerber = new TSUser('Emma', 'Gerber', 'geem', 'password6', 'emma.gerber@example.com',
            this.mandant, TSRole.GESUCHSTELLER);
        this.gesuchstellerHeinrichMueller = new TSUser('Heinrich', 'Mueller', 'muhe', 'password6', 'heinrich.mueller@example.com',
            this.mandant, TSRole.GESUCHSTELLER);
        this.gesuchstellerMichaelBerger = new TSUser('Michael', 'Berger', 'bemi', 'password6', 'michael.berger@example.com',
            this.mandant, TSRole.GESUCHSTELLER);
        this.gesuchstellerHansZimmermann = new TSUser('Hans', 'Zimmermann', 'ziha', 'password6', 'hans.zimmermann@example.com',
            this.mandant, TSRole.GESUCHSTELLER);
    }

    private createUsersOfBern() {
        this.administratorBGBern = new TSUser('Kurt', 'Blaser', 'blku', 'password5', 'kurt.blaser@example.com',
            this.mandant, TSRole.ADMIN, undefined, undefined, [this.gemeindeBern]);
        this.sachbearbeiterBGBern = new TSUser('Jörg', 'Becker', 'jobe', 'password1', 'joerg.becker@example.com',
            this.mandant, TSRole.SACHBEARBEITER_JA, undefined, undefined, [this.gemeindeBern]);
        this.administratorTSBern = new TSUser('Adrian', 'Schuler', 'scad', 'password9', 'adrian.schuler@example.com',
            this.mandant, TSRole.ADMINISTRATOR_SCHULAMT, undefined, undefined, [this.gemeindeBern]);
        this.sachbearbeiterTSBern = new TSUser('Julien', 'Schuler', 'scju', 'password9', 'julien.schuler@example.com',
            this.mandant, TSRole.SCHULAMT, undefined, undefined, [this.gemeindeBern]);

        this.steueramtBern = new TSUser('Rodolfo', 'Geldmacher', 'gero', 'password11', 'rodolfo.geldmacher@example.com',
            this.mandant, TSRole.STEUERAMT, undefined, undefined, [this.gemeindeBern]);
        this.revisorBern = new TSUser('Reto', 'Revisor', 'rere', 'password9', 'reto.revisor@example.com',
            this.mandant, TSRole.REVISOR, undefined, undefined, [this.gemeindeBern]);
        this.juristBern = new TSUser('Julia', 'Jurist', 'juju', 'password9', 'julia.jurist@example.com',
            this.mandant, TSRole.JURIST, undefined, undefined, [this.gemeindeBern]);
    }

    private createUsersOfOstermundigen() {
        this.administratorBGOstermundigen = new TSUser('Kurt', 'Schmid', 'scku', 'password1', 'kurt.blaser@example.com',
            this.mandant, TSRole.ADMIN, undefined, undefined, [this.gemeindeOstermundigen]);
        this.sachbearbeiterBGOstermundigen = new TSUser('Jörg', 'Keller', 'kejo', 'password1', 'joerg.becker@example.com',
            this.mandant, TSRole.SACHBEARBEITER_JA, undefined, undefined, [this.gemeindeOstermundigen]);
        this.administratorTSOstermundigen = new TSUser('Adrian', 'Huber', 'huad', 'password1', 'adrian.schuler@example.com',
            this.mandant, TSRole.ADMINISTRATOR_SCHULAMT, undefined, undefined, [this.gemeindeOstermundigen]);
        this.sachbearbeiterTSOstermundigen = new TSUser('Julien', 'Odermatt', 'odju', 'password1', 'julien.schuler@example.com',
            this.mandant, TSRole.SCHULAMT, undefined, undefined, [this.gemeindeOstermundigen]);

        this.steueramtOstermundigen = new TSUser('Rodolfo', 'Iten', 'itro', 'password1', 'rodolfo.geldmacher@example.com',
            this.mandant, TSRole.STEUERAMT, undefined, undefined, [this.gemeindeOstermundigen]);
        this.revisorOstermundigen = new TSUser('Reto', 'Werlen', 'were', 'password1', 'reto.revisor@example.com',
            this.mandant, TSRole.REVISOR, undefined, undefined, [this.gemeindeOstermundigen]);
        this.juristOstermundigen = new TSUser('Julia', 'Adler', 'adju', 'password1', 'julia.jurist@example.com',
            this.mandant, TSRole.JURIST, undefined, undefined, [this.gemeindeOstermundigen]);

    }

    private createUsersOfBothBernAndOstermundigen() {
        this.administratorBGBernOstermundigen = new TSUser('Kurt', 'Kälin', 'kaku', 'password1', 'kurt.blaser@example.com',
            this.mandant, TSRole.ADMIN, undefined, undefined, [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.sachbearbeiterBGBernOstermundigen = new TSUser('Jörg', 'Aebischer', 'aejo', 'password1', 'joerg.becker@example.com',
            this.mandant, TSRole.SACHBEARBEITER_JA, undefined, undefined, [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.administratorTSBernOstermundigen = new TSUser('Adrian', 'Bernasconi', 'bead', 'password1', 'adrian.schuler@example.com',
            this.mandant, TSRole.ADMINISTRATOR_SCHULAMT, undefined, undefined, [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.sachbearbeiterTSBernOstermundigen = new TSUser('Julien', 'Bucheli', 'buju', 'password1', 'julien.schuler@example.com',
            this.mandant, TSRole.SCHULAMT, undefined, undefined, [this.gemeindeBern, this.gemeindeOstermundigen]);


        this.steueramtBernOstermundigen = new TSUser('Rodolfo', 'Hermann', 'hero', 'password1', 'rodolfo.geldmacher@example.com',
            this.mandant, TSRole.STEUERAMT, undefined, undefined, [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.revisorBernOstermundigen = new TSUser('Reto', 'Hug', 'hure', 'password1', 'reto.revisor@example.com',
            this.mandant, TSRole.REVISOR, undefined, undefined, [this.gemeindeBern, this.gemeindeOstermundigen]);
        this.juristBernOstermundigen = new TSUser('Julia', 'Lory', 'luju', 'password1', 'julia.jurist@example.com',
            this.mandant, TSRole.JURIST, undefined, undefined, [this.gemeindeBern, this.gemeindeOstermundigen]);
    }

    /**
     * Der Mandant wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     * @returns {TSMandant}
     */
    private static getMandant(): TSMandant {
        let mandant = new TSMandant();
        mandant.name = 'TestMandant';
        mandant.id = 'e3736eb8-6eef-40ef-9e52-96ab48d8f220';
        return mandant;
    }

    /**
     * Gemeinde Bern wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     * @returns {TSGemeinde}
     */
    private static getGemeindeBern(): TSGemeinde {
        let bern = new TSGemeinde();
        bern.name = 'Bern';
        bern.id = 'ea02b313-e7c3-4b26-9ef7-e413f4046db2';
        bern.enabled = true;
        return bern;
    }

    /**
     * Gemeinde Ostermundigen wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     * @returns {TSGemeinde}
     */
    private static getGemeindeOstermundigen(): TSGemeinde {
        let ostermundigen = new TSGemeinde();
        ostermundigen.name = 'Ostermundigen';
        ostermundigen.id = '80a8e496-b73c-4a4a-a163-a0b2caf76487';
        ostermundigen.enabled = true;
        return ostermundigen;
    }

    /**
     * Die Institution wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private getInsitution(): TSInstitution {
        let institution = new TSInstitution();
        institution.name = 'Kita Brünnen';
        institution.id = '1b6f476f-e0f5-4380-9ef6-836d688853a3';
        institution.mail = 'kita.bruennen@example.com';
        institution.traegerschaft = this.traegerschaftStadtBern;
        institution.mandant = this.mandant;
        return institution;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTraegerschaftStadtBern(): TSTraegerschaft {
        let traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'Kitas & Tagis Stadt Bern';
        traegerschaft.mail = 'kitasundtagis@example.com';
        traegerschaft.id = 'f9ddee82-81a1-4cda-b273-fb24e9299308';
        return traegerschaft;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTraegerschaftLeoLea(): TSTraegerschaft {
        let traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'LeoLea';
        traegerschaft.mail = 'leolea@example.com';
        traegerschaft.id = 'd667e2d0-3702-4933-8fb7-be7a39755232';
        return traegerschaft;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTraegerschaftSGF(): TSTraegerschaft {
        let traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'SGF';
        traegerschaft.mail = 'sgf@example.com';
        traegerschaft.id = 'bb5d4bd8-84c9-4cb6-8134-a97312dead67';
        return traegerschaft;
    }

    /**
     * Die Traegerschaft wird direkt gegeben. Diese Daten und die Daten der DB muessen uebereinstimmen
     */
    private static getTraegerschaftFamex(): TSTraegerschaft {
        let traegerschaft = new TSTraegerschaft();
        traegerschaft.name = 'FAMEX';
        traegerschaft.mail = 'famex@example.com';
        traegerschaft.id = '4a552145-5ccd-4bf8-b827-c77c930daaa8';
        return traegerschaft;
    }

    public logIn(user: TSUser): void {
        console.log('loginnnnnnnnn', user);
        this.authServiceRS.loginRequest(user).then(() => {
            AuthenticationUtil.navigateToStartPageForRole(user, this.uiRouter.stateService);
        });
    }
}
