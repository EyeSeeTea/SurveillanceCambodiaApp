package org.eyeseetea.malariacare.domain.entity;

import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.model.Value;
import org.eyeseetea.malariacare.data.model.QuestionStrategy;
import org.eyeseetea.malariacare.strategies.SurveyFragmentStrategy;

public class SurveyQuestionValue {
    private Survey mSurvey;
    private static final int RDT = 1,
            ACT6 = 2,
            ACT12 = 3,
            ACT18 = 4,
            ACT24 = 5,
            PQ = 6,
            CQ = 7,
            OUT_STOCK = 8;


    public SurveyQuestionValue(Survey survey) {
        mSurvey = survey;
        mSurvey.getValuesFromDB();
    }

    public String getRDTValue() {
        return getValueQuestion(RDT);
    }

    public String getACT6Value() {
        return getValueQuestion(ACT6);
    }

    public String getACT12Value() {
        return getValueQuestion(ACT12);
    }

    public String getACT18Value() {
        return getValueQuestion(ACT18);
    }

    public String getACT24Value() {
        return getValueQuestion(ACT24);
    }

    public String getPqValue() {
        return getValueQuestion(PQ);
    }

    public String getCqValue() {
        return getValueQuestion(CQ);
    }

    public String getOutStockValue() {
        return getValueQuestion(OUT_STOCK);
    }


    public String getValueQuestion(int question) {
        for (Value value : mSurvey.getValues()) {//this values should be get from memory because the treatment options are in memory
            if(value.getQuestion()==null) {
                continue;
            }
            switch (question) {
                case RDT:
                    if (SurveyFragmentStrategy.isStockSurvey(mSurvey) && QuestionStrategy.isStockRDT(value.getQuestion().getUid())) {
                        return value.getValue();
                    }
                    break;
                case ACT6:
                    if (SurveyFragmentStrategy.isStockSurvey(mSurvey) && QuestionStrategy.isACT6(value.getQuestion().getUid())) {
                        return value.getValue();
                    }
                    break;
                case ACT12:
                    if (SurveyFragmentStrategy.isStockSurvey(mSurvey) && QuestionStrategy.isACT12(value.getQuestion().getUid())) {
                        return value.getValue();
                    }
                    break;
                case ACT18:
                    if (SurveyFragmentStrategy.isStockSurvey(mSurvey) && QuestionStrategy.isACT18(value.getQuestion().getUid())) {
                        return value.getValue();
                    }
                    break;
                case ACT24:
                    if (SurveyFragmentStrategy.isStockSurvey(mSurvey) && QuestionStrategy.isACT24(value.getQuestion().getUid())) {
                        return value.getValue();
                    }
                    break;
                case PQ:
                    if (SurveyFragmentStrategy.isStockSurvey(mSurvey) && QuestionStrategy.isPq(value.getQuestion().getUid())) {
                        return value.getValue();
                    }
                    break;
                case CQ:
                    if (SurveyFragmentStrategy.isStockSurvey(mSurvey) && QuestionStrategy.isCq(value.getQuestion().getUid())) {
                        return value.getValue();
                    }
                    break;
                case OUT_STOCK:
                    if (QuestionStrategy.isOutStockQuestion(value.getQuestion().getUid())) {
                        return value.getValue();
                    }
                    break;
            }
        }
        return "0";
    }

}
