/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

export abstract class MathUtil {
    public static subtractFloatPrecisionSafe(value: number, toSubtract: number, decimalPrecision = 2): number {
        const numToSub: number  = +toSubtract; //convert to number
        const valueToSub: number  = +value; //convert to number
        return parseFloat((valueToSub - numToSub).toFixed(decimalPrecision));
    }

    public static addFloatPrecisionSafe(value: number, toAdd: number, decimalPrecision = 2): number {
        const numToAdd: number  = +toAdd; //convert to number
        const valueToAdd: number = +value; //convert to number
        return parseFloat((valueToAdd + numToAdd).toFixed(decimalPrecision));
    }

    public static subtractArrayFloatPrecisionSafe(value: number, toSubtract: number[], decimalPrecision = 2): number {
        let result = value;
        for (const current of toSubtract) {
            result = MathUtil.subtractFloatPrecisionSafe(result, current, decimalPrecision);
        }
        return result;
    }

    public static addArrayFloatPrecisionSafe(value: number, toAdd: number[], decimalPrecision = 2): number {
        let result = value;
        for (const current of toAdd) {
            result = MathUtil.addFloatPrecisionSafe(result, current, decimalPrecision);
        }
        return result;
    }
}
