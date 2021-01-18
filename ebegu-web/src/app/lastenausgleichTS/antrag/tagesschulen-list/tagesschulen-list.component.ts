import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {TagesschuleAngabenRS} from '../../services/tagesschule-angaben.service.rest';

@Component({
    selector: 'dv-tagesschulen-list',
    templateUrl: './tagesschulen-list.component.html',
    styleUrls: ['./tagesschulen-list.component.less'],
    changeDetection: ChangeDetectionStrategy.Default
})
export class TagesschulenListComponent implements OnInit {

    @Input() public lastenausgleichId: string;

    public datas: { institutionName: string; status: string }[];
    public tableColumns = [
        {displayedName: 'Tagesschule', attributeName: 'institutionName'},
        {displayedName: 'STATUS', attributeName: 'status'},
    ];

    public constructor(
        private tagesschuleAngabenService: TagesschuleAngabenRS,
        private cd: ChangeDetectorRef
    ) {
    }

    public ngOnInit(): void {
        this.tagesschuleAngabenService.getAllVisibleTagesschulenAngabenForTSLastenausgleich(this.lastenausgleichId)
            .subscribe(data => {
                this.datas = data.map(latsInstitutionContainer => {
                        return {
                            institutionName: latsInstitutionContainer.institution.name,
                            status: latsInstitutionContainer.status,
                        };
                    }
                );
                //this.cd.detectChanges();
            });
    }
}
