import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ListSozialdienstComponent} from './list-sozialdienst/list-sozialdienst.component';
import {AddSozialdienstComponent} from './add-sozialdienst/add-sozialdienst.component';
import {SozialdienstRoutingModule} from './sozialdienst-routing/sozialdienst-routing.module';

@NgModule({
    declarations: [ListSozialdienstComponent, AddSozialdienstComponent],
    imports: [
        CommonModule,
        SozialdienstRoutingModule,
    ],
})
export class SozialdienstModule {
}
