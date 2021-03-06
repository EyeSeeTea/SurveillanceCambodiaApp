package org.eyeseetea.malariacare.presentation.factory.monitor.rows;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.domain.entity.SurveyQuestionTreatmentValue;
import org.eyeseetea.malariacare.presentation.factory.monitor.utils.SurveyMonitor;

/**
 * Created by idelcano on 21/07/2016.
 */
public class CqRowBuilder extends CounterRowBuilder {
    public CqRowBuilder(Context context) {
        super(context, context.getString(R.string.Cq));
    }

    @Override
    protected Integer incrementCount(SurveyMonitor surveyMonitor) {
        return Math.round(Float.parseFloat(new SurveyQuestionTreatmentValue(surveyMonitor.getSurvey()).getCqValue()));
    }
}
