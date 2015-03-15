package org.eyeseetea.malariacare.layout;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.CompositiveScore;
import org.eyeseetea.malariacare.data.Header;
import org.eyeseetea.malariacare.data.Option;
import org.eyeseetea.malariacare.data.Question;
import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.layout.components.DialogDispatcher;
import org.eyeseetea.malariacare.utils.CompositiveScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.NumDenRecord;
import org.eyeseetea.malariacare.utils.TabConfiguration;
import org.eyeseetea.malariacare.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adrian on 19/02/15.
 */
public class Layout {

    //static final Score scores=new Score();
    static final SparseArray<NumDenRecord> numDenRecordMap = new SparseArray<NumDenRecord>();
    static final int [] backgrounds = {R.drawable.background_even, R.drawable.background_odd};
    //Iterator for background (odd and even)
    static int iterBacks = 0;

    public static void setNumDenRecordMap(Activity mainActivity){
        LinearLayout root = (LinearLayout) mainActivity.findViewById(R.id.Grid);
        root.setTag(numDenRecordMap);
    }

    // This method fill in a tab layout
    public static void insertTab(MainActivity mainActivity, Tab tab, final TabConfiguration tabConfiguration) {
        // We reset backgrounds counter
        iterBacks = 0;
        Log.i(".Layout", "Generating Tab " + tab.getName());

        // This layout inflater is for joining other layouts
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // This layout is for the tab content (questions)
        LinearLayout layoutGrandParent = (LinearLayout) mainActivity.findViewById(tabConfiguration.getTabId());
        layoutGrandParent.setTag(tab);
        ViewGroup layoutParentScroll = null;
        GridLayout layoutParent = null;
        if (tabConfiguration.isAutomaticTab()) layoutParentScroll = (ScrollView) layoutGrandParent.getChildAt(0);
        else layoutParentScroll = (ViewGroup) layoutGrandParent.getChildAt(0);
        layoutParent = (GridLayout) layoutParentScroll.getChildAt(0);

        //Initialize numerator and denominator record map
        numDenRecordMap.put(tabConfiguration.getTabId(), new NumDenRecord());

        // We do this to have a default value in the ddl
        Option defaultOption = new Option(Constants.DEFAULT_SELECT_OPTION);

        Log.i(".Layout", "Get View For Tab");
        TabHost tabHost = (TabHost)mainActivity.findViewById(R.id.tabHost);
        tabHost.setup();

        Log.i(".Layout", "Generate Tab");
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(Long.toString(tab.getId())); // Here we set the tag, we'll use later to move between tabs
        tabSpec.setIndicator(tab.getName());
        tabSpec.setContent(tabConfiguration.getTabId());
        tabHost.addTab(tabSpec);

        if (!tabConfiguration.isAutomaticTab() && tabConfiguration.getLayoutId() != null){
            generateCustomTab(mainActivity, tab, tabConfiguration, inflater, layoutParent);
        }else {
            generateAutomaticTab(mainActivity, tab, tabConfiguration, inflater, layoutParent, defaultOption);
        }
        if (tabConfiguration.getScoreFieldId() != null) generateScore(tab, tabConfiguration, inflater, layoutGrandParent);
    }


    private static void generateScore(Tab tab, TabConfiguration tabConfiguration, LayoutInflater inflater, LinearLayout layoutGrandParent) {
        // This layout is for showing the accumulated score
        GridLayout layoutParentScore = (GridLayout) layoutGrandParent.getChildAt(1);
        View subtotalView = null;
        TextView totalNumText = null;
        TextView totalDenText = null;
        if (tabConfiguration.isAutomaticTab()) {
            subtotalView = inflater.inflate(R.layout.subtotal_num_dem, layoutParentScore, false);
            totalNumText = (TextView) subtotalView.findViewById(R.id.totalNum);
            totalDenText = (TextView) subtotalView.findViewById(R.id.totalDen);
            totalNumText.setText("0.0");
            List<Float> numDenSubTotal = numDenRecordMap.get(tabConfiguration.getTabId()).calculateTotal();
            totalDenText.setText(Utils.round(numDenSubTotal.get(1)));
        } else {
            subtotalView = inflater.inflate(R.layout.subtotal_custom, layoutParentScore, false);
        }
        TextView tabName = (TextView) subtotalView.findViewById(R.id.tabName);
        tabName.setText(tab.getName());

        TextView subscoreView = (TextView) subtotalView.findViewById(R.id.score);

        // Now, for being able to write Score in the score tab and score averages in its place (in score tab), we use setTag() to include a pointer to
        // the score View id, and in that id, we include a pointer to the average view id. This way, we can do the calculus here and represent there
        Integer generalScoreId = tabConfiguration.getScoreFieldId();
        subscoreView.setTag(generalScoreId);
        layoutParentScore.addView(subtotalView);
    }


    private static void createDropDownListener(final TabConfiguration tabConfiguration, Spinner dropdown, final MainActivity mainActivity) {
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Spinner spinner = (Spinner) parentView;
                Option triggeredOption = (Option) spinner.getItemAtPosition(position);

                Question triggeredQuestion = (Question) spinner.getTag(R.id.QuestionTag);
                TextView numeratorView = (TextView) spinner.getTag(R.id.NumeratorViewTag);
                TextView denominatorView = (TextView) spinner.getTag(R.id.DenominatorViewTag);
                LinearLayout tabLayout = ((LinearLayout) LayoutUtils.findParentRecursively(spinner, (Integer) spinner.getTag(R.id.Tab)));

                // Tab scores View
                TextView numSubtotal = (TextView) tabLayout.findViewById(R.id.totalNum);
                TextView denSubtotal = (TextView) tabLayout.findViewById(R.id.totalDen);
                TextView partialScoreView = (TextView) tabLayout.findViewById(R.id.score);
                TextView partialScorePercentageView = (TextView) tabLayout.findViewById(R.id.percentageSymbol);
                TextView partialCualitativeScoreView = (TextView) tabLayout.findViewById(R.id.cualitativeScore);
                View gridView = null;
                // General scores View
                Integer generalScoreId = null, generalScoreAvgId = null;
                TextView generalScoreView = null, generalScoreAvgView = null;
                if (tabConfiguration.getScoreFieldId() != null) {
                    generalScoreId = (Integer) tabConfiguration.getScoreFieldId();
                    gridView = LayoutUtils.findParentRecursively(spinner, R.id.Grid);
                    generalScoreView = (TextView) gridView.findViewById(generalScoreId);
                    if (tabConfiguration.getScoreAvgFieldId() != null) {
                        generalScoreAvgId = (Integer) tabConfiguration.getScoreAvgFieldId();
                        generalScoreAvgView = (TextView) gridView.findViewById(generalScoreAvgId);
                    }
                }

                Float numerator, denominator;
                if (triggeredOption.getName() != null && !(triggeredOption.getName().equals(Constants.DEFAULT_SELECT_OPTION))) { // This is for capture the user selection
                    // First we do the calculus
                    numerator = triggeredOption.getFactor() * triggeredQuestion.getNumerator_w();
                    Log.i(".Layout", "numerator: " + numerator);
                    denominator = 0.0F;

                    if (triggeredQuestion.getNumerator_w().compareTo(triggeredQuestion.getDenominator_w()) == 0) {
                        denominator = triggeredQuestion.getDenominator_w();
                        Log.i(".Layout", "denominator: " + denominator);
                    } else {
                        if (triggeredQuestion.getNumerator_w().compareTo(0.0F) == 0 && triggeredQuestion.getDenominator_w().compareTo(0.0F) != 0) {
                            denominator = triggeredOption.getFactor() * triggeredQuestion.getDenominator_w();
                            Log.i(".Layout", "denominator: " + denominator);
                        }
                    }

                    numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).addRecord(triggeredQuestion, numerator, denominator);

                    // If the option is changed to positive numerator and has children, we need to show the children and take their denominators into account
                    if (triggeredQuestion.hasChildren()) {
                        toggleVisibleChildren(position, spinner, triggeredQuestion);
                    }

                    numeratorView.setText(Utils.round(numerator));
                    denominatorView.setText(Utils.round(denominator));

                } else {
                    // This is for capturing the event when the user leaves the dropdown list without selecting any option
                    numerator = 0.0F;
                    if (triggeredQuestion.hasChildren()){
                        denominator = 0.0F;
                        toggleVisibleChildren(position, spinner, triggeredQuestion);
                    }
                    else{
                        denominator = triggeredQuestion.getDenominator_w();
                    }

                    if (selectedItemView != null) {
                        numeratorView.setText(Utils.round(numerator));
                         denominatorView.setText(Utils.round(denominator));
                    }
                    numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).addRecord(triggeredQuestion, numerator, denominator);
                }


                // FIXME: THIS PART NEEDS A REFACTOR. FROM HERE...
                List<Float> numDenSubTotal = numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).calculateTotal();
                CompositiveScoreRegister.updateCompositivesScore(triggeredQuestion, gridView);

                if (numSubtotal != null && denSubtotal != null && partialScoreView != null) {
                    numSubtotal.setText(Utils.round(numDenSubTotal.get(0)));
                    denSubtotal.setText(Utils.round(numDenSubTotal.get(1)));
                    float score;
                    float average = 0.0F, totalAverage = 0.0F;
                    if (numDenSubTotal.get(0) == 0 && numDenSubTotal.get(1) == 0){
                       score = 100;
                    }
                    else if (numDenSubTotal.get(0) > 0 && numDenSubTotal.get(1) == 0){
                        score = 0;
                        DialogDispatcher mf = DialogDispatcher.newInstance(null);
                        mf.showDialog(mainActivity.getFragmentManager(), DialogDispatcher.ERROR_DIALOG);
                    }
                    else {
                       score = (numDenSubTotal.get(0) / numDenSubTotal.get(1)) * 100;
                    }
                    TextView elementView = null;

                    LayoutUtils.setScore(score, partialScoreView, partialScorePercentageView, partialCualitativeScoreView); // We set the score in the tab score

                    if (tabConfiguration.getScoreFieldId() != null) {
                        LayoutUtils.setScore(score, generalScoreView);
                        if(tabConfiguration.getScoreAvgFieldId() != null){
                            List<Integer> averageElements = (ArrayList<Integer>) generalScoreAvgView.getTag();
                            if (averageElements == null) {
                                averageElements = new ArrayList<Integer>();
                                averageElements.add(generalScoreId);
                                LayoutUtils.setScore(score, generalScoreAvgView);
                                generalScoreAvgView.setTag(averageElements);
                            } else {
                                boolean found = false;
                                for (Integer element : averageElements) {
                                    if (element.intValue() == generalScoreId) found = true;
                                    average += Float.parseFloat((String) ((TextView) LayoutUtils.findParentRecursively(generalScoreView, R.id.scoreTable).findViewById(element)).getText());
                                }
                                if (!found) averageElements.add(generalScoreId);
                                average = average / averageElements.size();
                                LayoutUtils.setScore(average, generalScoreAvgView);
                                generalScoreAvgView.setTag(averageElements);
                            }
                        }
                        List<Integer> scoreElements = (ArrayList<Integer>) gridView.findViewById(R.id.totalScore).getTag();
                        TextView totalScoreView = (TextView) gridView.findViewById(R.id.totalScore);
                        if (scoreElements == null) {
                            scoreElements = new ArrayList<Integer>();
                            if (tabConfiguration.getScoreAvgFieldId() != null) scoreElements.add(generalScoreAvgId);
                            else scoreElements.add(generalScoreId);
                            totalScoreView.setTag(scoreElements);
                        } else {
                            boolean foundElement = false;
                            for (Integer element : scoreElements){
                                if (tabConfiguration.getScoreAvgFieldId() != null) {
                                    if (element.intValue() == generalScoreAvgId.intValue()) foundElement = true;
                                } else {
                                    if (element.intValue() == generalScoreId.intValue()) foundElement = true;
                                }
                                totalAverage += Float.parseFloat((String) ((TextView) LayoutUtils.findParentRecursively(generalScoreView, R.id.scoreTable).findViewById(element)).getText());
                            }
                            if (!foundElement){
                                if (tabConfiguration.getScoreAvgFieldId() != null) scoreElements.add(generalScoreAvgId);
                                else scoreElements.add(generalScoreId);
                            }
                            totalAverage = totalAverage / scoreElements.size();
                            LayoutUtils.setScore(totalAverage, totalScoreView);
                            totalScoreView.setTag(scoreElements);
                        }
                    }
                }
                // FIXME: ...TO HERE
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });
    }



    private static void toggleVisibleChildren(int position, Spinner spinner, Question triggeredQuestion) {
        View parent = LayoutUtils.findParentRecursively(spinner, (Integer) spinner.getTag(R.id.Tab));
        for (Question childQuestion : triggeredQuestion.getQuestionChildren()) {
            View childView = LayoutUtils.findChildRecursively(parent, childQuestion);
            if (position == 1) { //FIXME: There must be a smarter way for saying "if the user selected yes"
                LayoutUtils.toggleVisible(childView, View.VISIBLE);
                ((View) ((View) childView).getTag(R.id.HeaderViewTag)).setVisibility(View.VISIBLE);
                numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).addRecord(childQuestion, 0F, childQuestion.getDenominator_w());
            } else {
                LayoutUtils.toggleVisible(childView, View.GONE);
                if (LayoutUtils.isHeaderEmpty(triggeredQuestion.getQuestionChildren(), childQuestion.getHeader().getQuestions())) {
                    ((View) ((View) childView).getTag(R.id.HeaderViewTag)).setVisibility(View.GONE);
                }
                numDenRecordMap.get((Integer) spinner.getTag(R.id.Tab)).deleteRecord(childQuestion);
            }
        }
    }

    private static View getView(int iterBacks, LayoutInflater inflater, GridLayout layoutParent, View headerView, Question question, Integer componentType, int questionType) {
        View questionView = inflater.inflate(componentType, layoutParent, false);
        questionView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);
        TextView statement = (TextView) questionView.findViewById(R.id.statement);
        statement.setText(question.getForm_name());
        EditText answerI = (EditText) questionView.findViewById(R.id.answer);
        answerI.setTag(R.id.QuestionTag, question);
        answerI.setTag(R.id.HeaderViewTag, headerView);
        answerI.setTag(R.id.QuestionTypeTag, questionType);

        // If the question has children, we load the denominator, else we hide the question
        if (!question.hasParent()) {
            //set header to visible
            headerView.setVisibility(View.VISIBLE);
        } else {
            questionView.setVisibility(View.GONE);
        }
        return questionView;
    }

    private static void generateAutomaticTab(MainActivity mainActivity, Tab tab, TabConfiguration tabConfiguration, LayoutInflater inflater, GridLayout layoutParent, Option defaultOption) {
        Log.i(".Layout", "Generate Headers");
        for (Header header: tab.getOrderedHeaders()){
            // First we introduce header text according to the template
            //Log.i(".Layout", "Reading header " + header.toString());
            View headerView = inflater.inflate(R.layout.headers, layoutParent, false);

            TextView headerText = (TextView) headerView.findViewById(R.id.headerName);
            headerText.setBackgroundResource(R.drawable.background_header);
            headerText.setText(header.getName());
            //Set Visibility to false until we check if it has any question visible
            headerView.setVisibility(View.GONE);
            layoutParent.addView(headerView);


            //Log.i(".Layout", "Reader questions for header " + header.toString());
            for (Question question : header.getQuestions()){
                View questionView = null;
                EditText answerI = null;
                // The statement is present in every kind of question
                switch(question.getAnswer().getOutput()){
                    case Constants.DROPDOWN_LIST:
                        questionView = inflater.inflate(R.layout.ddl, layoutParent, false);
                        questionView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);

                        TextView statement = (TextView) questionView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        TextView denominator = (TextView) questionView.findViewById(R.id.den);

                        Spinner dropdown = (Spinner)questionView.findViewById(R.id.answer);
                        dropdown.setTag(R.id.QuestionTag, question);
                        dropdown.setTag(R.id.HeaderViewTag, headerView);
                        dropdown.setTag(R.id.NumeratorViewTag, questionView.findViewById(R.id.num));
                        dropdown.setTag(R.id.DenominatorViewTag, questionView.findViewById(R.id.den));
                        dropdown.setTag(R.id.Tab, tabConfiguration.getTabId());
                        dropdown.setTag(R.id.QuestionTypeTag, Constants.DROPDOWN_LIST);

                        // If the question has children, we load the denominator, else we hide the question
                        if (!question.hasParent()) {
                            if (question.hasChildren()) questionView.setBackgroundResource(R.drawable.background_parent);

                            denominator.setText(Utils.round(question.getDenominator_w()));
                            headerView.setVisibility(View.VISIBLE);

                            numDenRecordMap.get(tabConfiguration.getTabId()).addRecord(question, 0F, question.getDenominator_w());
                        } else {
                            questionView.setVisibility(View.GONE);
                        }

                        createDropDownListener(tabConfiguration, dropdown, mainActivity);

                        List<Option> optionList = question.getAnswer().getOptions();
                        optionList.add(0, defaultOption);
                        ArrayAdapter adapter = new ArrayAdapter(mainActivity, android.R.layout.simple_spinner_item, optionList);
                        adapter.setDropDownViewResource(R.layout.simple_spinner_item);
                        dropdown.setAdapter(adapter);
                        break;
                    case Constants.INT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.integer, Constants.INT);
                        break;
                    case Constants.LONG_TEXT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.longtext, Constants.LONG_TEXT);
                        break;
                    case Constants.SHORT_TEXT:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.shorttext, Constants.SHORT_TEXT);
                        break;
                    case Constants.SHORT_DATE: case Constants. LONG_DATE:
                        questionView = getView(iterBacks, inflater, layoutParent, headerView, question, R.layout.date, Constants.SHORT_TEXT);
                        break;
                }
                if (questionView != null) layoutParent.addView(questionView);
                iterBacks++;
            }
        }
    }

    private static void generateCustomTab(MainActivity mainActivity, Tab tab, TabConfiguration tabConfiguration, LayoutInflater inflater, GridLayout layoutParent) {
        View customView = inflater.inflate(tabConfiguration.getLayoutId(), layoutParent, false);
        boolean getFromDatabase = false;
        boolean hasScoreLayout = false;
        // Array to get the needed layouts during question insertion
        List<Integer> layoutsToUse = new ArrayList<Integer>();
        // Array to capture and process events when user selection is done
        List<AdapterView.OnItemSelectedListener> listeners = new ArrayList<AdapterView.OnItemSelectedListener>(),
                listeners2 = new ArrayList<AdapterView.OnItemSelectedListener>();
        List<List> listenerTypes = new ArrayList<List>();
        List<TextWatcher> editListeners = new ArrayList<TextWatcher>();
        int iterListenerType = 0;
        int iterListeners = 0;
        int iterEditListeners = 0;

        switch (tabConfiguration.getLayoutId()){
            case R.layout.scoretab:
                layoutParent.addView(customView);
                break;
            case R.layout.reportingtab:
                getFromDatabase = true;
                layoutsToUse.add(R.layout.reporting_record);
                layoutsToUse.add(R.layout.reporting_record2);
                TextWatcher editListener = createReportingListener(mainActivity);
                TextWatcher editListener2 = createReportingListener(mainActivity);
                editListeners.add(editListener);
                editListeners.add(editListener2);
                layoutParent.addView(customView);
                break;
            case R.layout.adherencetab:
                Switch visibility = (Switch) customView.findViewById(R.id.visibilitySwitch);
                createAdherenceSwitchListener(visibility);
                getFromDatabase = true;
                layoutsToUse.add(R.layout.pharmacy_register);
                layoutsToUse.add(R.layout.pharmacy_register2);
                // Add onItemSelectedListener to manage score
                AdapterView.OnItemSelectedListener listener = createAdherenceListener(1);
                listeners.add(listener);
                listenerTypes.add(listeners);
                AdapterView.OnItemSelectedListener listener2 = createAdherenceListener(2);
                listeners2.add(listener2);
                listenerTypes.add(listeners2);
                layoutParent.addView(customView);
                break;

            case R.layout.iqatab:
                getFromDatabase = true;
                layoutsToUse.add(R.layout.iqatab_record);
                layoutsToUse.add(R.layout.iqatab_record);
                // Add onItemSelectedListener to manage score
                AdapterView.OnItemSelectedListener tabListener = createIQAListener(customView, R.id.labStaffTable, R.id.matchTable);
                listeners.add(tabListener);
                listenerTypes.add(listeners);
                AdapterView.OnItemSelectedListener tabListener2 = createIQAListener(customView, R.id.supervisorTable, R.id.matchTable);
                listeners2.add(tabListener2);
                listenerTypes.add(listeners2);
                layoutParent.addView(customView);
                break;
            case R.layout.compositivescoretab:
                List<CompositiveScore> compositiveScoresList = CompositiveScore.listAll(CompositiveScore.class);
                for (CompositiveScore compositiveScore : compositiveScoresList){
                    CompositiveScoreRegister.registerScore(compositiveScore);
                    List<View> tables = LayoutUtils.getChildrenByTag((ViewGroup)customView, null, "CompositivesScore");
                    if(tables.size() == 1) {
                        TableLayout table = (TableLayout) tables.get(0);
                        View rowView = inflater.inflate(R.layout.compositive_scores_record, table, false);
                        rowView.setTag("CompositiveScore_" + compositiveScore.getId());
                        rowView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);
                        ((TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(0)).getChildAt(0)).setText(compositiveScore.getCode());
                        ((TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(1)).getChildAt(0)).setText(compositiveScore.getLabel());
                        table.addView(rowView);
                    }
                    else{
                        Log.e(".Layout", "Error: Header name is supposed to be used to distinguish where to place associated questions in custom tabs, but when looking for header named CompositivesScore we've found " + tables.size() + " results");
                    }
                }
                layoutParent.addView(customView);
                break;
        }

        // Some manual tabs, like adherence and IQA EQA get their questions from the database, here we manage how they are represented in the layout
        // as long as they don't use the same convention.
        // Standards:
        //  * They use phantom questions to group some related questions and so have them referenced (a parent question only indicates their children are related)
        //  * Questions are represented in tables. Header name will be set as a tag in the TableLayout (directly in the xml) component where its questions have to be represented
        //  * Once found, we iterate on the parent questions (with children) and represent each of their questions in a different column
        //  * Any score or thing that affects all the row questions, will be added to the parent question
        //  * Another important thing to improve is that at this moment I'm creating a List called layoutsToUse that contains, ordered, the different layouts that must be used for each questions group
        if (getFromDatabase){
            iterListenerType = 0;
            List<Header> headers = tab.getOrderedHeaders();
            for (int i=0; i<headers.size(); i++){
                iterBacks = 0;
                String headerName = headers.get(i).getName(); // this is also the ID
                // This tables list must be a list of only one element if we have not failed in layout creation
                List<View> tables = LayoutUtils.getChildrenByTag((ViewGroup)customView, null, headerName);
                if(tables.size() == 1){
                    TableLayout table = (TableLayout)tables.get(0);
                    // Now we have the table element, we have to search for the parent questions
                    List <Question> questions = headers.get(i).getQuestions(); // FIXME: improve this search to get only the parent questions
                    for (Question question: questions) {
                        // If the question is a parent, do don't show it but use it to put the row layout
                        if (question.getQuestion() == null) { // FIXME: when the search above is improve this check will be unnecessary

                            View rowView = inflater.inflate(layoutsToUse.get(i), table, false);

                            if (question.hasChildren()){
                                List<Question> children = question.getQuestionChildren();

                                iterListeners = 0;
                                iterEditListeners = 0;
                                TextView answer = null;
                                int offset = 0;

                                // Set the row number
                                TextView number = (TextView) rowView.findViewById(R.id.number);
                                if (number != null) {
                                    number.setText(question.getForm_name());
                                    offset = offset + 1;
                                }

                                // Set the row background
                                rowView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);
                                table.addView(rowView);
                                Log.d(".Layout", "Row Question");

                                for (int j = 0; j < children.size(); j++) {
                                    if (children.get(j).getAnswer() != null) {
                                        switch (children.get(j).getAnswer().getOutput()) {
                                            case Constants.DROPDOWN_LIST:
                                                Option defaultOption = new Option(Constants.DEFAULT_SELECT_OPTION);
                                                List<Option> optionList = children.get(j).getAnswer().getOptions();
                                                optionList.add(0, defaultOption);
                                                ArrayAdapter adapter = new ArrayAdapter(mainActivity, android.R.layout.simple_spinner_item, optionList);
                                                adapter.setDropDownViewResource(R.layout.simple_spinner_item);
                                                Spinner dropdown = (Spinner) ((ViewGroup)((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0)).getChildAt(0); // We take the spinner
                                                dropdown.setTag(R.id.QuestionTypeTag, Constants.DROPDOWN_LIST);
                                                dropdown.setAdapter(adapter);
                                                if ("listener".equals(dropdown.getTag())) {
                                                    dropdown.setOnItemSelectedListener(((List<AdapterView.OnItemSelectedListener>)listenerTypes.get(iterListenerType)).get(iterListeners));
                                                    iterListeners++;
                                                }

                                                break;
                                            case Constants.INT:
                                                Log.d(".Layout", "Question int");
                                                answer = (TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0); // We take the textfield
                                                answer.setTag(R.id.QuestionTypeTag, Constants.INT);
                                                if ("listener".equals(answer.getTag())) {
                                                    answer.addTextChangedListener(editListeners.get(iterEditListeners));
                                                    iterEditListeners++;
                                                }
                                                break;
                                            case Constants.LONG_TEXT:
                                                Log.i(".Layout", "Question longtext");
                                                answer = (TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0); // We take the textfield
                                                answer.setTag(R.id.QuestionTypeTag, Constants.LONG_TEXT);
                                                if ("listener".equals(answer.getTag())) {
                                                    answer.addTextChangedListener(editListeners.get(iterEditListeners));
                                                    iterEditListeners++;
                                                }
                                                break;
                                            case Constants.SHORT_TEXT:
                                                Log.i(".Layout", "Question shorttext");
                                                answer = (TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0); // We take the textfield
                                                answer.setTag(R.id.QuestionTypeTag, Constants.SHORT_TEXT);
                                                if ("listener".equals(answer.getTag())) {
                                                    answer.addTextChangedListener(editListeners.get(iterEditListeners));
                                                    iterEditListeners++;
                                                }
                                                break;
                                            case Constants.SHORT_DATE:
                                            case Constants.LONG_DATE:
                                                Log.i(".Layout", "Question date");
                                                answer = (TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0); // We take the textfield
                                                answer.setTag(R.id.QuestionTypeTag, Constants.SHORT_DATE);
                                                break;
                                        }
                                    } else {
                                        ((TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(j + offset)).getChildAt(0)).setText(children.get(j).getForm_name());
                                    }
                                }
                                iterBacks++;
                            } else{
                                ((TextView) ((ViewGroup) ((ViewGroup) rowView).getChildAt(0)).getChildAt(0)).setText(question.getForm_name());
                                table.addView(rowView);
                            }
                        }
                    }
                }else{
                    Log.e(".Layout", "Error: Header name is supposed to be used to distinguish where to place associated questions in custom tabs, but when looking for header named " + headerName + " we've found " + tables.size() + " results");
                }
                iterListenerType++;
            }
        }
    }

    public static AdapterView.OnItemSelectedListener createAdherenceListener(int type){
        switch(type){
            case 1:
                return new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // This will occur when an item is selected in first table Adherence spinners
                        // For this table, when Test result is "Malaria Positive" score=1, score=0 otherwise
                        int score = 0;
                        float totalScore = 0.0F;
                        if (position == 1) score = 1;
                        else score = 0;
                        TextView scoreText = (TextView)((ViewGroup)((ViewGroup)((ViewGroup)parent.getParent().getParent().getParent())).getChildAt(5)).getChildAt(0);
                        scoreText.setText((String)Integer.toString(score));
                        // Set the total score in the score tab
                        LinearLayout tabLayout = (LinearLayout)LayoutUtils.findParentRecursively(parent, MainActivity.getTabsLayouts());
                        TableLayout table1 = (TableLayout)tabLayout.findViewById(R.id.register1Table);
                        for (int i=1; i<((ViewGroup) table1).getChildCount(); i++){
                            TableRow row = (TableRow) table1.getChildAt(i);
                            TextView scoreCell = ((TextView) ((ViewGroup) row.getChildAt(5)).getChildAt(0));
                            String stringFloat = scoreCell.getText().toString();
                            if (!("".equals(scoreCell.getText()))) totalScore += Float.parseFloat(stringFloat);
                        }
                        TableLayout table2 = (TableLayout)tabLayout.findViewById(R.id.register2Table);
                        for (int i=1; i<((ViewGroup) table2).getChildCount(); i++){
                            TableRow row = (TableRow) table2.getChildAt(i);
                            TextView scoreCell = ((TextView) ((ViewGroup) row.getChildAt(4)).getChildAt(0));
                            String stringFloat = scoreCell.getText().toString();
                            if (!("".equals(scoreCell.getText()))) totalScore += Float.parseFloat(stringFloat);
                        }
                        LinearLayout root = (LinearLayout) LayoutUtils.findParentRecursively(parent, R.id.Grid);
                        TextView totalScoreView = (TextView) root.findViewById(R.id.adherenceScore);
                        totalScore = totalScore*100.0F/40.0F;
                        LayoutUtils.setScore(totalScore, totalScoreView);

                        TextView subScoreView = (TextView)tabLayout.findViewById(R.id.score);
                        TextView percentageView = (TextView)tabLayout.findViewById(R.id.percentageSymbol);
                        TextView cualitativeView = (TextView)tabLayout.findViewById(R.id.cualitativeScore);
                        LayoutUtils.setScore(totalScore, subScoreView, percentageView, cualitativeView);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                };
            case 2:
                return new AdapterView.OnItemSelectedListener(){
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        // This will occur when an item is selected in second table Adherence spinners
                        // For this table, when Test results is RDT* then ACT Prescribed=Yes means score=1, otherwise score=0
                        //               , when Test results is Microscopy* then ACT Prescribed=No means Score=1, otherwise score=0
                        int score = 0;
                        float totalScore = 0.0F;
                        TextView actPrescribed = (TextView)((ViewGroup)((ViewGroup)((ViewGroup)parent.getParent().getParent().getParent())).getChildAt(2)).getChildAt(0);
                        if("RDT Positive".equals(actPrescribed.getText()) || "RDT Negative".equals(actPrescribed.getText())){
                            if (position == 1) score=1;
                            else score=0;
                        }else if("Microscopy Positive".equals(actPrescribed.getText()) || "Microscopy Negative".equals(actPrescribed.getText())){
                            if (position == 2) score=1;
                            else score=0;
                        }
                        TextView scoreText = (TextView)((ViewGroup)((ViewGroup)((ViewGroup)parent.getParent().getParent().getParent())).getChildAt(4)).getChildAt(0);
                        scoreText.setText((String)Integer.toString(score));
                        // Set the total score in the score tab
                        LinearLayout tabLayout = (LinearLayout)LayoutUtils.findParentRecursively(parent, MainActivity.getTabsLayouts());
                        TableLayout table1 = (TableLayout)tabLayout.findViewById(R.id.register1Table);
                        for (int i=1; i<((ViewGroup) table1).getChildCount(); i++){
                            TableRow row = (TableRow) table1.getChildAt(i);
                            TextView scoreCell = ((TextView) ((ViewGroup) row.getChildAt(5)).getChildAt(0));
                            String stringFloat = scoreCell.getText().toString();
                            if (!("".equals(scoreCell.getText()))) totalScore += Float.parseFloat(stringFloat);
                        }
                        TableLayout table2 = (TableLayout)tabLayout.findViewById(R.id.register2Table);
                        for (int i=1; i<((ViewGroup) table2).getChildCount(); i++){
                            TableRow row = (TableRow) table2.getChildAt(i);
                            TextView scoreCell = ((TextView) ((ViewGroup) row.getChildAt(4)).getChildAt(0));
                            String stringFloat = scoreCell.getText().toString();
                            if (!("".equals(scoreCell.getText()))) totalScore += Float.parseFloat(stringFloat);
                        }
                        LinearLayout root = (LinearLayout) LayoutUtils.findParentRecursively(parent, R.id.Grid);
                        TextView totalScoreView = (TextView) root.findViewById(R.id.adherenceScore);
                        totalScore = totalScore*100.0F/40.0F;
                        LayoutUtils.setScore(totalScore, totalScoreView);

                        TextView subScoreView = (TextView)tabLayout.findViewById(R.id.score);
                        TextView percentageView = (TextView)tabLayout.findViewById(R.id.percentageSymbol);
                        TextView cualitativeView = (TextView)tabLayout.findViewById(R.id.cualitativeScore);
                        LayoutUtils.setScore(totalScore, subScoreView, percentageView, cualitativeView);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                };
        }
        return null;
    }

    public static void createAdherenceSwitchListener(Switch switchView){
        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LinearLayout grandpa = (LinearLayout) ((ViewGroup) buttonView.getParent()).getParent();
                for (int i=1; i<grandpa.getChildCount(); i++){
                    if (isChecked) (grandpa.getChildAt(i)).setVisibility(View.VISIBLE);
                    else (grandpa.getChildAt(i)).setVisibility(View.GONE);
                }
                // We set invisible also the subscore layout
                if (isChecked) ((GridLayout)((ViewGroup)((ViewGroup)((ViewGroup)grandpa.getParent()).getParent()).getParent()).getChildAt(1)).setVisibility(View.VISIBLE);
                else ((GridLayout)((ViewGroup)((ViewGroup)((ViewGroup)grandpa.getParent()).getParent()).getParent()).getChildAt(1)).setVisibility(View.GONE);
                return;
            }
        });
    }

    public static AdapterView.OnItemSelectedListener createIQAListener(View view, int opositeTableLayout, int matchTableLayout){
        final TableLayout opositeTable = (TableLayout) view.findViewById(opositeTableLayout);
        final TableLayout matchTable = (TableLayout) view.findViewById(matchTableLayout);
        return new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // This will occur when an item is selected in IQAEQA spinners
                // For IQAEQA, when file each file spinner option match with its equivalent in the other table, that means score = 1
                int score = 0;
                float totalScore = 0.0F;
                TextView totalScoreView = null;
                TableRow thisRow = (TableRow)((ViewGroup)((ViewGroup)parent.getParent()).getParent()).getParent();
                int numberOfRow = Integer.parseInt((String)((TextView) thisRow.getChildAt(0)).getText());
                int thisPosition = parent.getSelectedItemPosition();
                int oppositePosition = ((Spinner)((ViewGroup)((ViewGroup)((ViewGroup)opositeTable.getChildAt(numberOfRow)).getChildAt(1)).getChildAt(0)).getChildAt(0)).getSelectedItemPosition();
                if (thisPosition == oppositePosition && thisPosition != 0 && oppositePosition != 0) score = 1;
                TextView scoreView = (TextView)((ViewGroup)matchTable.getChildAt(numberOfRow)).getChildAt(1);
                scoreView.setText(Integer.toString(score));

                // Update in score tab
                LinearLayout root = (LinearLayout) LayoutUtils.findParentRecursively(view, R.id.Grid);
                for (int i=1; i<matchTable.getChildCount(); i++){
                    totalScoreView = (TextView)((ViewGroup) matchTable.getChildAt(i)).getChildAt(1);
                    if (!("".equals((String)totalScoreView.getText()))) totalScore += Float.parseFloat((String)totalScoreView.getText());
                }
                totalScore = totalScore*10.0F;
                TextView iqaEqaScoreView = (TextView) root.findViewById(R.id.iqaeqaScore);
                LayoutUtils.setScore(totalScore, iqaEqaScoreView);
                LinearLayout tabLayout = (LinearLayout)LayoutUtils.findParentRecursively(parent, MainActivity.getTabsLayouts());
                TextView subScoreView = (TextView)tabLayout.findViewById(R.id.score);
                TextView percentageView = (TextView)tabLayout.findViewById(R.id.percentageSymbol);
                TextView cualitativeView = (TextView)tabLayout.findViewById(R.id.cualitativeScore);
                LayoutUtils.setScore(totalScore, subScoreView, percentageView, cualitativeView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    public static TextWatcher createReportingListener(final Activity myActivity){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float totalScore = 0.0F;
                TextView totalScoreView = null;
                View myView = myActivity.getCurrentFocus();
                Log.d(".Layout", "instance of: ");
                if (s.length() == 0) return;
                EditText myEdit = (EditText)myActivity.getCurrentFocus();
                TableRow myRow = (TableRow) ((ViewGroup) myEdit.getParent()).getParent();
                TableLayout myTable = (TableLayout)((ViewGroup)myRow).getParent();
                EditText registerView = (EditText)((ViewGroup)myRow.getChildAt(1)).getChildAt(0);
                EditText monthlyView = (EditText)((ViewGroup)myRow.getChildAt(2)).getChildAt(0);
                TextView scoreView = (TextView)((ViewGroup)myRow.getChildAt(3)).getChildAt(0);
                if (!("".equals(registerView.getText())) && !("".equals(monthlyView.getText()))){
                    if (registerView.getText().toString().equals(monthlyView.getText().toString())){
                        scoreView.setText("1");
                    } else {
                        scoreView.setText("0");
                    }
                } else {
                    scoreView.setText("0");
                }

                // Update in score tab
                LinearLayout root = (LinearLayout) LayoutUtils.findParentRecursively(myEdit, R.id.Grid);
                for (int i=1; i<myTable.getChildCount(); i++){
                    totalScoreView = (TextView)((ViewGroup)((ViewGroup) myTable.getChildAt(i)).getChildAt(3)).getChildAt(0);
                    if (!("".equals((String)totalScoreView.getText()))) totalScore += Float.parseFloat((String)totalScoreView.getText());
                }
                totalScore = totalScore*10.0F;
                TextView reportingScoreView = (TextView) root.findViewById(R.id.reportingScore);
                LayoutUtils.setScore(totalScore, reportingScoreView);
                LinearLayout tabLayout = (LinearLayout)LayoutUtils.findParentRecursively(myEdit, MainActivity.getTabsLayouts());
                TextView subScoreView = (TextView)tabLayout.findViewById(R.id.score);
                TextView percentageView = (TextView)tabLayout.findViewById(R.id.percentageSymbol);
                TextView cualitativeView = (TextView)tabLayout.findViewById(R.id.cualitativeScore);
                LayoutUtils.setScore(totalScore, subScoreView, percentageView, cualitativeView);
            }
        };
    }
}


