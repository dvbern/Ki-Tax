import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {UIRouterGlobals} from '@uirouter/core';
import {AuthServiceRS} from '../../../authentication/service/AuthServiceRS.rest';
import {GesuchRS} from '../../../gesuch/service/gesuchRS.rest';
import {TSGesuch} from '../../../models/TSGesuch';
import {EbeguUtil} from '../../../utils/EbeguUtil';

@Component({
    selector: 'dv-zpv-nr-success',
    templateUrl: './zpv-nr-success.component.html',
    styleUrls: ['./zpv-nr-success.component.less'],
})
export class ZpvNrSuccessComponent implements OnInit {

    public isAuthenticated: boolean;
    public gesuchOfGS: TSGesuch;

    public constructor(
        private readonly authService: AuthServiceRS,
        private readonly gesuchRS: GesuchRS,
        private readonly uiRouterGlobals: UIRouterGlobals
    ) {
    }

    public ngOnInit(): void {
        this.isAuthenticated = EbeguUtil.isNotNullOrUndefined(this.authService.getPrincipal());
        if (this.isAuthenticated) {
            this.gesuchRS.findGesuchOfGesuchsteller(this.uiRouterGlobals.params.gesuchstellerId)
                .then(gesuch => this.gesuchOfGS = gesuch);
        }
    }

    public isZpvNummerVerknuepft() {
        if (this.getGSNumber() === 2) {
            return this.gesuchOfGS?.gesuchsteller2.gesuchstellerJA.hasZpvNummer;
        }

        return this.gesuchOfGS?.gesuchsteller1.gesuchstellerJA.hasZpvNummer;
    }

    public getGSNumber(): number {
        if (!this.gesuchOfGS) {
            return 0;
        }

        return this.gesuchOfGS.gesuchsteller1.id === this.uiRouterGlobals.params.gesuchstellerId ? 1 : 2;
    }

    public getParams(): any {
        return {
            gesuchId: this.gesuchOfGS?.id,
            gesuchstellerNumber: this.getGSNumber()
        };
    }
}
