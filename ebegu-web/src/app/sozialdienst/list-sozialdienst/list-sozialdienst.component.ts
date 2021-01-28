import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {StateService} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

@Component({
    selector: 'dv-list-sozialdienst',
    templateUrl: './list-sozialdienst.component.html',
    styleUrls: ['./list-sozialdienst.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ListSozialdienstComponent implements OnInit {

    public hiddenDVTableColumns = [
        'fallNummer',
        'familienName',
        'kinder',
        'aenderungsdatum',
        'dokumenteHochgeladen',
        'angebote',
        'institutionen',
        'verantwortlicheTS',
        'verantwortlicheBG',
    ];

    public constructor(private readonly $state: StateService, private authServiceRS: AuthServiceRS) {
    }

    public ngOnInit(): void {
    }

    public hatBerechtigungHinzufuegen(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getSuperAdminRoles());
    }

    public addSozialdienst(): void {
        this.$state.go('sozialdienst.add');
    }
}
