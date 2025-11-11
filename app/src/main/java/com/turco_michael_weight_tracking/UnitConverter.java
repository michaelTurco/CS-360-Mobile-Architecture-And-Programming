package com.turco_michael_weight_tracking;

import android.content.Context;

import com.turco_michael_weight_tracking.LocalStorage.MeasurementUnit;

import java.util.Locale;

/*
    Helper class for converting between units of weight

    note:
    all weights stored in local storage are in pounds

    got conversion formulas from
    https://www.rapidtables.com/convert/weight/kg-to-pound.html
 */

public class UnitConverter {
    public static float unitToPounds(float value, MeasurementUnit unit) {
        switch (unit) {
            case POUNDS:
                return value;
            case KILOGRAMS:
                return value * 2.2046226218488f;
            default:
                return -1;
        }
    }

    public static float unitToKilograms(float value, MeasurementUnit unit) {
        switch (unit) {
            case POUNDS:
                return value / 0.45359237f;
            case KILOGRAMS:
                return value;
            default:
                return -1;
        }
    }

    public static float unitToUnit(float value, MeasurementUnit unit, MeasurementUnit toUnit) {
        if (unit == toUnit) return value;

        switch (toUnit) {
            case POUNDS:
                return unitToPounds(value, unit);
            case KILOGRAMS:
                return unitToKilograms(value, unit);
            default:
                return -1;
        }
    }

    // returns a formatted string like '123.4 lbs'
    public static String poundsToFormattedUnitString(Context context, float value, MeasurementUnit unit) {
        String unitString = getShortUnitString(context, unit);
        value = unitToUnit(value, MeasurementUnit.POUNDS, unit);
        return String.format(Locale.getDefault(), "%.1f %s", value, unitString);
    }

    // returns a string like "lbs"
    public static String getShortUnitString(Context context, MeasurementUnit unit) {
        switch (unit) {
            case POUNDS:
                return context.getString(R.string.measurement_pounds_short);
            case KILOGRAMS:
                return context.getString(R.string.measurement_kilograms_short);
            default:
                return "";
        }
    }

    // returns a string like "pounds (lbs)"
    public static String getLongUnitString(Context context, MeasurementUnit unit) {
        switch (unit) {
            case POUNDS:
                return context.getString(R.string.measurement_pounds);
            case KILOGRAMS:
                return context.getString(R.string.measurement_kilograms);
            default:
                return "";
        }
    }
}
