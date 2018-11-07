import {ChangeDetectionStrategy, Component, OnInit, ViewChild} from '@angular/core';
import {NgForm} from '@angular/forms';
import {StateService, Transition} from '@uirouter/core';
import {StateDeclaration} from '@uirouter/core/lib/state/interface';
import {from, Observable} from 'rxjs';
import {TSTraegerschaft} from '../../../models/TSTraegerschaft';
import ErrorService from '../../core/errors/service/ErrorService';
import {TraegerschaftRS} from '../../core/service/traegerschaftRS.rest';

@Component({
    selector: 'dv-traegerschaft-edit',
    templateUrl: './traegerschaft-edit.component.html',
    styleUrls: ['./traegerschaft-edit.component.less'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TraegerschaftEditComponent implements OnInit {

    @ViewChild(NgForm) public form: NgForm;

    public traegerschaft$: Observable<TSTraegerschaft>;
    private traegerschaftId: string;
    private navigationSource: StateDeclaration;

    constructor(
        private readonly $transition$: Transition,
        private readonly $state: StateService,
        private readonly errorService: ErrorService,
        private readonly traegerschaftRS: TraegerschaftRS,

    ) { }

    ngOnInit() {
        this.navigationSource = this.$transition$.from();
        this.traegerschaftId = this.$transition$.params().traegerschaftId;
        if (!this.traegerschaftId) {
            return;
        }
        this.traegerschaft$ = from(
            this.traegerschaftRS.findTraegerschaft(this.traegerschaftId).then(result => {
                return result;
            }));
    }

    public save(stammdaten: TSTraegerschaft): void {
        if (!this.form.valid) {
            return;
        }
        this.errorService.clearAll();
        this.traegerschaftRS.updateTraegerschaft(stammdaten).then(() => this.navigateBack());
    }

    public cancel(): void {
        this.navigateBack();
    }

    private navigateBack(): void {
        if (!this.navigationSource.name) {
            this.$state.go('traegerschaft.list');
            return;
        }
        const redirectTo = this.navigationSource.name === 'einladung.abschliessen'
            ? 'traegerschaft.view'
            : this.navigationSource;

        this.$state.go(redirectTo, {traegerschaftId: this.traegerschaftId});
    }
}
