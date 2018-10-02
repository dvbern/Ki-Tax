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

import {IPromise} from 'angular';
import {AuthLifeCycleService} from '../../authentication/service/authLifeCycle.service';
import TSDokumenteDTO from '../../models/dto/TSDokumenteDTO';
import TSFinanzielleSituationResultateDTO from '../../models/dto/TSFinanzielleSituationResultateDTO';
import {TSAuthEvent} from '../../models/enums/TSAuthEvent';
import TSFinanzModel from '../../models/TSFinanzModel';
import TSGesuch from '../../models/TSGesuch';
import EbeguRestUtil from '../../utils/EbeguRestUtil';
import DokumenteRS from './dokumenteRS.rest';
import EinkommensverschlechterungContainerRS from './einkommensverschlechterungContainerRS.rest';
import FinanzielleSituationRS from './finanzielleSituationRS.rest';

export default class BerechnungsManager {

    public static $inject = ['FinanzielleSituationRS', 'EbeguRestUtil', 'EinkommensverschlechterungContainerRS',
        'DokumenteRS', 'AuthLifeCycleService'];

    public finanzielleSituationResultate: TSFinanzielleSituationResultateDTO;
    public einkommensverschlechterungResultateBjP1: TSFinanzielleSituationResultateDTO;
    public einkommensverschlechterungResultateBjP2: TSFinanzielleSituationResultateDTO;
    public dokumente: TSDokumenteDTO;

    public constructor(private readonly finanzielleSituationRS: FinanzielleSituationRS,
                       private readonly ebeguRestUtil: EbeguRestUtil,
                       private readonly einkommensverschlechterungContainerRS: EinkommensverschlechterungContainerRS,
                       private readonly dokumenteRS: DokumenteRS,
                       private readonly authLifeCycleService: AuthLifeCycleService) {

        this.initValues();

        this.authLifeCycleService.get$(TSAuthEvent.LOGIN_SUCCESS)
            .subscribe(() => this.initValues());
    }

    private initValues(): void {
        this.finanzielleSituationResultate = new TSFinanzielleSituationResultateDTO();
        this.einkommensverschlechterungResultateBjP1 = new TSFinanzielleSituationResultateDTO();
        this.einkommensverschlechterungResultateBjP2 = new TSFinanzielleSituationResultateDTO();
        this.dokumente = new TSDokumenteDTO();
    }

    public calculateFinanzielleSituation(gesuch: TSGesuch): IPromise<TSFinanzielleSituationResultateDTO> {
        return this.finanzielleSituationRS.calculateFinanzielleSituation(
            gesuch)
            .then((finSitContRespo: TSFinanzielleSituationResultateDTO) => {
                this.finanzielleSituationResultate = finSitContRespo;
                return finSitContRespo;
            });
    }

    public calculateFinanzielleSituationTemp(tsFinSitModel: TSFinanzModel)
        : IPromise<TSFinanzielleSituationResultateDTO> {

        return this.finanzielleSituationRS.calculateFinanzielleSituationTemp(
            tsFinSitModel)
            .then((finSitContRespo: TSFinanzielleSituationResultateDTO) => {
                this.finanzielleSituationResultate = finSitContRespo;
                return finSitContRespo;
            });
    }

    public calculateEinkommensverschlechterung(gesuch: TSGesuch,
                                               basisJahrPlus: number): IPromise<TSFinanzielleSituationResultateDTO> {
        return this.einkommensverschlechterungContainerRS
            .calculateEinkommensverschlechterung(gesuch, basisJahrPlus)
            .then(finSitContRespo => this.setEinkommensverschlechterung(basisJahrPlus, finSitContRespo));
    }

    private setEinkommensverschlechterung(basisJahrPlus: number, finSitContRespo: TSFinanzielleSituationResultateDTO)
        : TSFinanzielleSituationResultateDTO {

        if (basisJahrPlus === 2) {
            this.einkommensverschlechterungResultateBjP2 = finSitContRespo;
        } else {
            this.einkommensverschlechterungResultateBjP1 = finSitContRespo;
        }

        return finSitContRespo;
    }

    public calculateEinkommensverschlechterungTemp(finanzModel: TSFinanzModel,
                                                   basisJahrPlus: number)
        : IPromise<TSFinanzielleSituationResultateDTO> {

        return this.einkommensverschlechterungContainerRS
            .calculateEinkommensverschlechterungTemp(finanzModel, basisJahrPlus)
            .then(finSitContRespo => this.setEinkommensverschlechterung(basisJahrPlus, finSitContRespo));

    }

    public getEinkommensverschlechterungResultate(basisJahrPlus: number): TSFinanzielleSituationResultateDTO {
        if (basisJahrPlus === 2) {
            return this.einkommensverschlechterungResultateBjP2;
        }
        return this.einkommensverschlechterungResultateBjP1;
    }

    public getDokumente(gesuch: TSGesuch): IPromise<TSDokumenteDTO> {
        return this.dokumenteRS.getDokumente(
            gesuch)
            .then((promiseValue: TSDokumenteDTO) => {
                this.dokumente = promiseValue;
                return promiseValue;
            });
    }

    /**
     * setzt alle Resultate zureuck so dass sicher nichts mehr gesetzt ist, wird zB gebraucht wenn man den Fall wechselt
     */
    public clear(): void {
        this.einkommensverschlechterungResultateBjP1 = undefined;
        this.einkommensverschlechterungResultateBjP2 = undefined;
        this.finanzielleSituationResultate = undefined;
        this.dokumente = undefined;
    }
}
