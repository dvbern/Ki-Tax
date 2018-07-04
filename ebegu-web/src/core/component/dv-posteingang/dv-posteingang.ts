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

import {Component, OnInit} from '@angular/core';
import {AuthLifeCycleService} from '../../../authentication/service/authLifeCycle.service';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {TSAuthEvent} from '../../../models/enums/TSAuthEvent';
import {TSPostEingangEvent} from '../../../models/enums/TSPostEingangEvent';
import {PosteingangService} from '../../../posteingang/service/posteingang.service';
import {Log} from '../../../utils/LogFactory';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import MitteilungRS from '../../service/mitteilungRS.rest';

@Component({
    selector: 'dv-posteingang',
    template: require('./dv-posteingang.html'),
})
export class DvPosteingangController implements OnInit {

    private log: Log = Log.createLog(AuthLifeCycleService);

    amountMitteilungen: number = 0;
    reloadAmountMitteilungenInterval: number;


    constructor(private mitteilungRS: MitteilungRS,
                private authServiceRS: AuthServiceRS,
                private authLifeCycleService: AuthLifeCycleService,
                private posteingangService: PosteingangService) {

    }

    ngOnInit() {
        this.getAmountNewMitteilungen();

        this.authLifeCycleService.get$(TSAuthEvent.LOGOUT_SUCCESS)
            .subscribe(
                () => {clearInterval(this.reloadAmountMitteilungenInterval); },
                error => this.log.info(`the received TSAuthEvent ${event} threw an error ${error}`),
            );

        this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
            .subscribe(
                () => {this.handleLogIn(); },
                error => this.log.info(`the received TSAuthEvent ${event} threw an error ${error}`),
            );

        this.posteingangService.get$(TSPostEingangEvent.POSTEINGANG_MAY_CHANGED)
            .subscribe(
                () => {
                    this.getAmountNewMitteilungen(); },
                error => this.log.info(`the received TSPostEingangEvent ${event} threw an error ${error}`),
            );
    }

    private handleLogIn() {
        if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getGesuchstellerJugendamtSchulamtRoles())) {
            this.getAmountNewMitteilungen(); // call it a first time

            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorOrAmtRole())) { // not for GS
                // call every 5 minutes (5*60*1000)
                this.reloadAmountMitteilungenInterval = window.setInterval(() => this.getAmountNewMitteilungen(), 300000);
            }
        }
    }

    private getAmountNewMitteilungen(): void {
        this.mitteilungRS.getAmountMitteilungenForCurrentBenutzer().then((response: number) => {
            if (!response || isNaN(response)) { //wenn keine gueltige antwort
                this.amountMitteilungen = 0;
            } else {
                this.amountMitteilungen = response;
            }
        }).catch(() => {
            //Fehler bei deisem request (notokenrefresh )werden bis hier ohne Behandlung
            // (unerwarteter Fehler anzeige, redirect etc.) weitergeschlauft
            this.log.debug('received error message while reading posteingang. Ignoring ...');
            this.amountMitteilungen = 0;
        });
    }

}
