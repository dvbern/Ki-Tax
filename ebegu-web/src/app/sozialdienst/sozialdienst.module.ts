import {NgModule} from '@angular/core';
import {SharedModule} from '../shared/shared.module';
import {ListSozialdienstComponent} from './list-sozialdienst/list-sozialdienst.component';
import {AddSozialdienstComponent} from './add-sozialdienst/add-sozialdienst.component';
import {SozialdienstRoutingModule} from './sozialdienst-routing/sozialdienst-routing.module';

@NgModule({
    declarations: [ListSozialdienstComponent, AddSozialdienstComponent],
    imports: [
        SharedModule,
        SozialdienstRoutingModule,
    ],
})
export class SozialdienstModule {
}
