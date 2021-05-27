import {ChangeDetectionStrategy, Component, Input, OnInit} from '@angular/core';
import {ErrorService} from '../../../../core/errors/service/ErrorService';
import {LastenausgleichTSService} from '../../../lastenausgleich-ts/services/lastenausgleich-ts.service';

@Component({
    selector: 'dv-verlauf',
    templateUrl: './verlauf.component.html',
    styleUrls: ['./verlauf.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class VerlaufComponent implements OnInit {

    @Input() public lastenausgleichId: string;

    public constructor(
        private readonly lastenausgleichTSService: LastenausgleichTSService,
        private readonly errorService: ErrorService,
    ) {
    }

    public ngOnInit(): void {
        this.lastenausgleichTSService.getVerlauf(this.lastenausgleichId)
            .subscribe(data => console.log(data));
    }

}
