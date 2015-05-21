/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.database.utils;

import com.orm.query.Select;

import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.utils.Constants;

import java.util.List;

/**
 * Created by Jose on 26/04/2015.
 */
public class ReadWriteDB {

    public static String readValueQuestion(Question question){
        String result = null;

        Value value = question.getValueBySession();

        if (value != null)
            result = value.getValue();

        return result;
    }

    public static int readPositionOption (Question question) {
        int result = 0;

        Value value = question.getValueBySession();
        if (value!=null) {

            List<Option> optionList = question.getAnswer().getOptions();
            optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));
            result = optionList.indexOf(value.getOption());
        }

        return result;
    }

    public static void saveValuesDDL(Question question, Option option) {

        Value value = question.getValueBySession();

        if (!option.getName().equals(Constants.DEFAULT_SELECT_OPTION)) {
            if (value == null) {
                value = new Value(option, question, Session.getSurvey());
            } else {
                value.setOption(option);
                value.setValue(option.getName());
            }
            value.save();
        }
        else {
            if (value != null) value.delete();
        }
    }

    public static void saveValuesText(Question question, String answer) {

        Value value = question.getValueBySession();

        // If the value is not found we create one
        if (value == null) {
            value = new Value(answer, question, Session.getSurvey());
        } else {
            value.setOption(null);
            value.setValue(answer);
        }
        value.save();
    }

    public static void resetValue(Question question) {

        Value value = question.getValueBySession();

        if (value != null)
            value.delete();
    }

    // Returns the 5 last surveys (by date) with status yet not put to "Sent"
    public static List<Survey> getLastNotSentSurveys(int number){
        List<Survey> surveys = getAllNotSentSurveys();
        if (surveys.size() <= number) return surveys;
        else return surveys.subList(0, number);
    }

    // Returns all the surveys with status yet not put to "Sent"
    public static List<Survey> getAllNotSentSurveys(){
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("status").notEq(Constants.SURVEY_SENT))
                .orderBy("event_date").list();
    }

    // Returns a concrete survey, if it exists
    public static List<Survey> getNotSentSurvey(OrgUnit orgUnit, Program program){
        return Select.from(Survey.class)
                .where(com.orm.query.Condition.prop("org_unit").eq(orgUnit.getId()))
                .and(com.orm.query.Condition.prop("program").eq(program.getId()))
                .orderBy("event_date").list();
    }
}