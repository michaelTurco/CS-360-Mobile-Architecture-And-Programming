package com.turco_michael_weight_tracking;

import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;

/*
    Helper class for converting between units of weight

    got conversion formulas from
    https://www.rapidtables.com/convert/weight/kg-to-pound.html
 */

public class UnitConverter {
    public static float convertUnitToPounds(float value, MeasurementUnit unit) {
        switch (unit) {
            case POUNDS:
                return value;
            case KILOGRAMS:
                return value * 2.2046226218488f;
            default:
                return -1;
        }
    }

    public static float convertUnitToKilograms(float value, MeasurementUnit unit) {
        switch (unit) {
            case POUNDS:
                return value / 0.45359237f;
            case KILOGRAMS:
                return value;
            default:
                return -1;
        }
    }

    public static float convertUnitToUnit(float value, MeasurementUnit unit, MeasurementUnit toUnit) {
        if (unit == toUnit) return value;

        switch (toUnit) {
            case POUNDS:
                return convertUnitToPounds(value, unit);
            case KILOGRAMS:
                return convertUnitToKilograms(value, unit);
            default:
                return -1;
        }
    }
}
