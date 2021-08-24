import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import {UIRouterModule} from '@uirouter/angular';
import {WizardstepXModule} from '../../wizardstepX/wizardstep-x.module';
import { GemeindeKennzahlenFormularComponent } from './gemeinde-kennzahlen-formular/gemeinde-kennzahlen-formular.component';
import { GemeindeKennzahlenUiComponent } from './gemeinde-kennzahlen-ui/gemeinde-kennzahlen-ui.component';

@NgModule({
  declarations: [GemeindeKennzahlenFormularComponent, GemeindeKennzahlenUiComponent],
  imports: [
    CommonModule,
    TranslateModule,
    WizardstepXModule,
    UIRouterModule,
  ],
})
export class GemeindeKennzahlenModule { }
