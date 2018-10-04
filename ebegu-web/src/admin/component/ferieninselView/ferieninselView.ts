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

import {IComponentOptions, IFormController} from 'angular';
import GesuchsperiodeRS from '../../../app/core/service/gesuchsperiodeRS.rest';
import AuthServiceRS from '../../../authentication/service/AuthServiceRS.rest';
import {getTSFeriennameValues, TSFerienname} from '../../../models/enums/TSFerienname';
import TSFerieninselStammdaten from '../../../models/TSFerieninselStammdaten';
import TSFerieninselZeitraum from '../../../models/TSFerieninselZeitraum';
import TSGesuchsperiode from '../../../models/TSGesuchsperiode';
import {TSDateRange} from '../../../models/types/TSDateRange';
import EbeguUtil from '../../../utils/EbeguUtil';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';
import AbstractAdminViewController from '../../abstractAdminView';
import {FerieninselStammdatenRS} from '../../service/ferieninselStammdatenRS.rest';
import ITimeoutService = angular.ITimeoutService;

export class FerieninselViewComponentConfig implements IComponentOptions {
    public transclude = false;
    public template = require('./ferieninselView.html');
    public controller = FerieninselViewController;
    public controllerAs = 'vm';
}

export class FerieninselViewController extends AbstractAdminViewController {
    public static $inject = ['GesuchsperiodeRS', 'FerieninselStammdatenRS', '$timeout', 'AuthServiceRS'];

    public form: IFormController;

    public gesuchsperiodenList: Array<TSGesuchsperiode> = [];
    public gesuchsperiode: TSGesuchsperiode;

    public ferieninselStammdatenMap: { [key: string]: TSFerieninselStammdaten; } = {};

    public readonly TSRoleUtil = TSRoleUtil;

    public constructor(private readonly gesuchsperiodeRS: GesuchsperiodeRS,
                       private readonly ferieninselStammdatenRS: FerieninselStammdatenRS,
                       private readonly $timeout: ITimeoutService, authServiceRS: AuthServiceRS,
    ) {
        super(authServiceRS);
        this.$timeout(() => {
            this.readGesuchsperioden();
        });
    }

    private readGesuchsperioden(): void {
        this.gesuchsperiodeRS.getAllGesuchsperioden().then((response: Array<TSGesuchsperiode>) => {
            this.gesuchsperiodenList = response;
        });
    }

    public gesuchsperiodeClicked(gesuchsperiode: any): void {
        if (gesuchsperiode.isSelected) {
            this.gesuchsperiode = gesuchsperiode;
            this.readFerieninselStammdatenByGesuchsperiode();
        } else {
            this.gesuchsperiode = undefined;
        }
    }

    private readFerieninselStammdatenByGesuchsperiode(): void {
        this.ferieninselStammdatenMap = {};
        this.ferieninselStammdatenRS.findFerieninselStammdatenByGesuchsperiode(this.gesuchsperiode.id)
            .then((response: TSFerieninselStammdaten[]) => {
                const ferieninselStammdatenList = response;
                for (const obj of ferieninselStammdatenList) {
                    this.ferieninselStammdatenMap[obj.ferienname] = obj;
                }
                this.resetErrors();
            });
    }

    public getFeriennamen(): TSFerienname[] {
        return getTSFeriennameValues();
    }

    public getFerieninselStammdaten(ferienname: TSFerienname): TSFerieninselStammdaten {
        let stammdaten = this.ferieninselStammdatenMap[ferienname];
        if (!stammdaten) {
            stammdaten = new TSFerieninselStammdaten();
            stammdaten.ferienname = ferienname;
            stammdaten.gesuchsperiode = this.gesuchsperiode;
            stammdaten.zeitraum = new TSFerieninselZeitraum();
            stammdaten.zeitraum.gueltigkeit = new TSDateRange();
            this.ferieninselStammdatenMap[ferienname] = stammdaten;
        }

        return stammdaten;
    }

    public saveFerieninselStammdaten(ferieninselStammdaten: TSFerieninselStammdaten): void {
        if (!(this.form.$valid && this.isFerieninselStammdatenValid(ferieninselStammdaten))) {
            return;
        }

        this.ferieninselStammdatenRS.saveFerieninselStammdaten(ferieninselStammdaten)
            .then((response: TSFerieninselStammdaten) => {
                this.ferieninselStammdatenMap[response.ferienname] = response;
            });
    }

    public addFerieninselZeitraum(ferieninselStammdaten: TSFerieninselStammdaten): void {
        if (!ferieninselStammdaten.zeitraumList) {
            ferieninselStammdaten.zeitraumList = [];
        }
        ferieninselStammdaten.zeitraumList.push(new TSFerieninselZeitraum());
    }

    public removeFerieninselZeitraum(
        ferieninselStammdaten: TSFerieninselStammdaten,
        ferieninselZeitraum: TSFerieninselZeitraum,
    ): void {
        const index = ferieninselStammdaten.zeitraumList.indexOf(ferieninselZeitraum, 0);
        ferieninselStammdaten.zeitraumList.splice(index, 1);
    }

    public isFerieninselStammdatenValid(ferieninselStammdaten: TSFerieninselStammdaten): boolean {
        return !(EbeguUtil.isNullOrUndefined(ferieninselStammdaten.anmeldeschluss)
            || EbeguUtil.isNullOrUndefined(ferieninselStammdaten.zeitraum.gueltigkeit.gueltigAb)
            || EbeguUtil.isNullOrUndefined(ferieninselStammdaten.zeitraum.gueltigkeit.gueltigBis));
    }

    public isSaveButtonDisabled(ferieninselStammdaten: TSFerieninselStammdaten): boolean {
        // Disabled, solange noch keines der Felder ausgefuellt ist
        return EbeguUtil.isNullOrUndefined(ferieninselStammdaten.anmeldeschluss)
            && EbeguUtil.isNullOrUndefined(ferieninselStammdaten.zeitraum.gueltigkeit.gueltigAb)
            && EbeguUtil.isNullOrUndefined(ferieninselStammdaten.zeitraum.gueltigkeit.gueltigBis);
    }

    public isAnmeldeschlussRequired(ferieninselStammdaten: TSFerieninselStammdaten): boolean {
        // Wenn mindestens ein Zeitraum erfasst ist
        return EbeguUtil.isNotNullOrUndefined(ferieninselStammdaten.zeitraum.gueltigkeit.gueltigAb)
            || EbeguUtil.isNotNullOrUndefined(ferieninselStammdaten.zeitraum.gueltigkeit.gueltigBis);
    }

    public isDatumAbRequired(
        ferieninselZeitraum: TSFerieninselZeitraum,
        ferieninselStammdaten: TSFerieninselStammdaten,
    ): boolean {
        // Wenn entweder der Anmeldeschluss erfasst ist, oder das Datum bis
        return EbeguUtil.isNotNullOrUndefined(ferieninselStammdaten.anmeldeschluss)
            || (EbeguUtil.isNotNullOrUndefined(ferieninselZeitraum.gueltigkeit)
                && EbeguUtil.isNotNullOrUndefined(ferieninselZeitraum.gueltigkeit.gueltigBis));
    }

    public isDatumBisRequired(
        ferieninselZeitraum: TSFerieninselZeitraum,
        ferieninselStammdaten: TSFerieninselStammdaten,
    ): boolean {
        // Wenn entweder der Anmeldeschluss erfasst ist, oder das Datum ab
        return EbeguUtil.isNotNullOrUndefined(ferieninselStammdaten.anmeldeschluss)
            || (EbeguUtil.isNotNullOrUndefined(ferieninselZeitraum.gueltigkeit)
                && EbeguUtil.isNotNullOrUndefined(ferieninselZeitraum.gueltigkeit.gueltigAb));
    }

    /**
     * Alle errors werden zurueckgesetzt. Dies ist notwendig, weil beim Wechseln zwischen Gesuchsperiode, das Form
     * nicht neugemacht wird. Deswegen werden alle alten Daten bzw. Errors beibehalten und deshalb falsche Failures
     * gegeben. Ausserdem wird das Form als Pristine gesetzt damit keine Reste aus den alten Daten uebernommen werden.
     */
    private resetErrors(): void {
        this.form.$setPristine();
        this.form.$setUntouched();

        // iterate over all from properties
        angular.forEach(this.form, (ctrl, name) => {
            // ignore angular fields and functions
            if (name.indexOf('$') === 0) {
                return;
            }
            angular.forEach(ctrl.$error, (_value, n) => {
                // reset validity
                ctrl.$setValidity(n, null);
            });
        });
    }
}
