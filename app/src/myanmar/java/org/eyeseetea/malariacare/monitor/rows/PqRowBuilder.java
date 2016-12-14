package org.eyeseetea.malariacare.monitor.rows;

import android.content.Context;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.monitor.utils.SurveyMonitor;

/**
 * Created by idelcano on 21/07/2016.
 */
public class PqRowBuilder extends CounterRowBuilder {

    public PqRowBuilder(Context context) {
        super(context, context.getString(R.string.Pq));
    }

    @Override
    protected Integer incrementCount(SurveyMonitor surveyMonitor) {
        return (surveyMonitor.isPq()) ? 1 : 0;
    }
}
