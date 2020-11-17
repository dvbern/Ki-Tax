import {Directive, ElementRef, Injector} from '@angular/core';
import {UpgradeComponent} from '@angular/upgrade/static';

@Directive({
  selector: '[dvNewUserSelect]'
})
export class NewUserSelectDirective extends UpgradeComponent {

  public constructor(elementRef: ElementRef, injector: Injector) {
      super('DVUserselect', elementRef, injector);
  }

}
