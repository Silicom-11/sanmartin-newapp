package com.iepca.app.util;

import android.content.Context;
import com.iepca.app.R;
import com.iepca.app.config.Constants;

/**
 * Grade calculation and display utilities.
 */
public final class GradeUtils {

    private GradeUtils() {}

    public static String getGradeLetter(double score) {
        if (score >= Constants.GRADE_AD_MIN) return "AD";
        if (score >= Constants.GRADE_A_MIN) return "A";
        if (score >= Constants.GRADE_B_MIN) return "B";
        return "C";
    }

    public static int getGradeColor(Context ctx, double score) {
        if (score >= Constants.GRADE_AD_MIN) return ctx.getColor(R.color.grade_ad);
        if (score >= Constants.GRADE_A_MIN) return ctx.getColor(R.color.grade_a);
        if (score >= Constants.GRADE_B_MIN) return ctx.getColor(R.color.grade_b);
        return ctx.getColor(R.color.grade_c);
    }

    public static String getGradeDescription(double score) {
        if (score >= Constants.GRADE_AD_MIN) return "Logro Destacado";
        if (score >= Constants.GRADE_A_MIN) return "Logro Esperado";
        if (score >= Constants.GRADE_B_MIN) return "En Proceso";
        return "En Inicio";
    }

    public static boolean isPassing(double score) {
        return score >= Constants.GRADE_PASSING;
    }
}
