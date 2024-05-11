import {AfterViewInit, ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {LogFactory} from '../../../../app/core/logging/LogFactory';
import {TSKindContainer} from '../../../../models/TSKindContainer';
import {KinderabzugExchangeService} from '../service/kinderabzug-exchange.service';

const LOG = LogFactory.createLog('HoehereBetraegeBeeintraechtigungComponent');

@Component({
    selector: 'dv-hoehere-betraege-beeintraechtigung',
    templateUrl: './hoehere-betraege-beeintraechtigung.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HoehereBetraegeBeeintraechtigungComponent implements AfterViewInit {

    @ViewChild(NgForm) public form!: NgForm;

    @Input()
    public kindContainer!: TSKindContainer;

    @Input()
    public readOnly = false;

    public constructor(private readonly formExchangeService: KinderabzugExchangeService, private readonly cd: ChangeDetectorRef) {
    }

    public ngAfterViewInit(): void {
        this.formExchangeService.addForm(this.form);
        this.formExchangeService.getFormValidationTriggered$().subscribe(() => this.cd.markForCheck(), error => LOG.error(error));
    }

    public handleBeantragungChange(): void {
        if (this.kindContainer.kindJA.hoehereBeitraegeWegenBeeintraechtigungBeantragen === false) {
            this.kindContainer.kindJA.hoehereBeitraegeUnterlagenDigital = null;
        }
    }
}
