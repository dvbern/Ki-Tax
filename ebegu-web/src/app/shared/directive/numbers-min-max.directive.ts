import {Directive, forwardRef, Input} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, Validator} from '@angular/forms';
import {EbeguUtil} from '../../../utils/EbeguUtil';

@Directive({
    selector:
        '[customMinMax][formControlName],[customMinMax][formControl],[customMinMax][ngModel]',
    providers: [
        {
            provide: NG_VALIDATORS,
            // eslint-disable-next-line @angular-eslint/no-forward-ref
            useExisting: forwardRef(() => NumbersMinMaxDirective),
            multi: true
        }
    ]
})
export class NumbersMinMaxDirective implements Validator {
    @Input()
    private readonly minValue: number;
    @Input()
    private readonly maxValue: number;

    public validate(c: AbstractControl): {[key: string]: any} {
        const v = c.value;

        // only validate if user actually put a value
        if (v !== null) {
            // check if minValue is set and if its valid
            if (
                EbeguUtil.isNotNullOrUndefined(this.minValue) &&
                v < this.minValue
            ) {
                return {min: true, value: this.minValue};
            }

            // check if maxValue is set and if its valid
            if (
                EbeguUtil.isNotNullOrUndefined(this.maxValue) &&
                v > this.maxValue
            ) {
                return {max: true, value: this.maxValue};
            }
        }

        return null;
    }
}
