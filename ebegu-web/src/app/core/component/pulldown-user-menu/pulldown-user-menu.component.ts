import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {StateService} from '@uirouter/core';
import {from, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {AuthServiceRS} from '../../../../authentication/service/AuthServiceRS.rest';
import {TSRole} from '../../../../models/enums/TSRole';
import {TSRoleUtil} from '../../../../utils/TSRoleUtil';
import {ApplicationPropertyRS} from '../../rest-services/applicationPropertyRS.rest';

@Component({
    selector: 'dv-pulldown-user-menu',
    templateUrl: './pulldown-user-menu.component.html',
    styleUrls: ['./pulldown-user-menu.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PulldownUserMenuComponent implements OnInit {

    public constructor(
        private readonly authService: AuthServiceRS,
        private readonly applicationPropertyRS: ApplicationPropertyRS,
        private readonly state: StateService
    ) {
    }

    public ngOnInit(): void {
    }

    public getFullName(): Observable<string> {
        return this.authService.principal$.pipe(
            map(principal => principal.getFullName())
        );
    }

    public getSuperAdminRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getSuperAdminRoles();
    }

    public getAllAdministratorRevisorRole(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllAdministratorRevisorRole();
    }

    public getInstitutionProfilRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getInstitutionProfilRoles();
    }

    public getMandantRoles(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getMandantRoles();
    }

    public getTraegerschaftId(): Observable<string> {
        return this.authService.principal$.pipe(
            map(principal => principal.currentBerechtigung?.traegerschaft?.id)
        );
    }

    public getAdministratorMandantRevisorRole(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAdministratorMandantRevisorRole();
    }

    public getAllRolesForSozialdienst(): ReadonlyArray<TSRole> {
        return TSRoleUtil.getAllRolesForSozialdienst();
    }

    public mandantSwitchVisible(): Observable<boolean> {
        return from(this.applicationPropertyRS.getPublicPropertiesCached())
            .pipe(
                map(publicAppConfig => publicAppConfig.mulitmandantAktiv)
            );
    }

    public logout(): void {
        this.state.go('authentication.login', {type: 'logout'});
    }

    public frenchEnabled(): Observable<boolean> {
        return from(this.applicationPropertyRS.getPublicPropertiesCached())
            .pipe(
                map(publicAppConfig => publicAppConfig.frenchEnabled)
            );
    }
}
