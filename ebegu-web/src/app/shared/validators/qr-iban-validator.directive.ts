/*
 * Copyright (C) 2022 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

import {Directive} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, ValidationErrors, Validator, ValidatorFn} from '@angular/forms';

/*
Die Eingabe einer QR-IBAN soll verboten werden.

QR-IBAN können anhand eines Ranges der IID erkannt werden:

Die IID ist Bestandteil der IBAN. Sie folgt dem Ländercode. Der Aufbau der IBAN ist:

2 Zeichen Ländercode CH/LI
2 Zeichen Prüfsumme
5 Zeichen QR-IID
12 Zeichen Interne Kontonummer der Finanzinstitute
QR-IIDs bestehen * exklusiv aus Nummern von 30000 bis 31999.
 */
@Directive({
    selector: '[isNotQrIbanN]',
    providers: [{provide: NG_VALIDATORS, useExisting: QrIbanValidatorDirective, multi: true}]
})
export class QrIbanValidatorDirective implements Validator {
    public validate(control: AbstractControl): ValidationErrors | null {
        return qrIbanValidator()(control);
    }
}

export function qrIbanValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
        const forbidden = isQrIbanLike(control.value);
        return forbidden ? {qrIban: {value: control.value}} : null;
    };
}

const QR_IBAN_PATTERN = /(LI|CH)[\d]{2}3[01][\d]{3}[\w]{12}/;
function isQrIbanLike(value: unknown): boolean {
    return typeof value === 'string' && value.length > 0 && QR_IBAN_PATTERN.test(stripWhiteSpaces(value));
}

function stripWhiteSpaces(value: string): string {
    return value.replace(/\s/g, '');
}
