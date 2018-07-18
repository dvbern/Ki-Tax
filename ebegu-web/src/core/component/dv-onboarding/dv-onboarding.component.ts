import {Component} from '@angular/core';
import {IComponentOptions} from 'angular';

const template = require('./dv-onboarding.component.html');
require('./dv-onboarding.component.less');
@Component({
  selector: 'dv-onboarding',
  templateUrl: './dv-onboarding.component.html',
  styleUrls: ['./dv-onboarding.component.less']
})
export class DvOnboardingComponent {

  constructor() { }
}

export class DvOnboardingComponentConfig implements IComponentOptions {
    transclude = false;
    template = template;
}
