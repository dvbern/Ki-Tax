import {Directive, OnInit} from '@angular/core';
import {AuthServiceRS} from '../authentication/service/AuthServiceRS.rest';
import {TSGesuchsperiodeStatus} from '../models/enums/TSGesuchsperiodeStatus';
import {TSGesuchsperiode} from '../models/TSGesuchsperiode';
import {TSRoleUtil} from '../utils/TSRoleUtil';
import {TSRole} from '../models/enums/TSRole';

@Directive()
export class AbstractAdminViewX implements OnInit {

    public readonly TSRole = TSRole;
    public readonly TSRoleUtil = TSRoleUtil;

    public constructor(public authServiceRS: AuthServiceRS) {
    }

    public ngOnInit(): void {
    }

    public isReadonly(): boolean {
        return !this.authServiceRS.isOneOfRoles(TSRoleUtil.getJAAdministratorRoles());
    }

    public isAnyAdminRole(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getAdministratorRoles());
    }

    public isSuperadmin(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles());
    }

    public periodenParamsEditableForPeriode(gesuchsperiode: TSGesuchsperiode): boolean {
        if (gesuchsperiode && gesuchsperiode.status) {
            // Fuer SuperAdmin immer auch editierbar, wenn AKTIV oder INAKTIV, sonst nur ENTWURF
            if (TSGesuchsperiodeStatus.GESCHLOSSEN === gesuchsperiode.status) {
                return false;
            }
            if (this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles())) {
                return true;
            }
            return TSGesuchsperiodeStatus.ENTWURF === gesuchsperiode.status;
        }
        return false;
    }

}
