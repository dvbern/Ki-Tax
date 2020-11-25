import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {TSRoleUtil} from '../../../utils/TSRoleUtil';

@Component({
    selector: 'dv-lastenausgleich-ts',
    templateUrl: './lastenausgleich-ts.component.html',
    styleUrls: ['./lastenausgleich-ts.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class LastenausgleichTSComponent implements OnInit {

    public constructor(
        private authServiceRS: AuthServiceRS
    ) {
    }

    public ngOnInit(): void {
    }

    public showToolbar(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }

    public showKommentare(): boolean {
        return this.authServiceRS.isOneOfRoles(TSRoleUtil.getMandantRoles());
    }
}
