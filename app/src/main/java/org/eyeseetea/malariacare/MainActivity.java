package org.eyeseetea.malariacare;

import android.app.ActionBar;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.layout.Layout;
import org.eyeseetea.malariacare.utils.PopulateDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    // layouts array configuration
    //      { Tab parent Layout ID     || add num/den layout || Child layout to include (-1 Integer for generate it programmaticaly)}
    private static Object tabsLayouts [][] = {
            {new Integer(R.id.tab1parent), new Boolean(true), new Integer(-1)},     // PROFILE
            {new Integer(R.id.tab2parent), new Boolean(true), new Integer(-1)},     // C1-GENERAL
            {new Integer(R.id.tab3parent), new Boolean(true), new Integer(-1)},     // C1-RDT
            {new Integer(R.id.tab4parent), new Boolean(true), new Integer(-1)},     // C1-MICROSCOPY
            {new Integer(R.id.tab5parent), new Boolean(true), new Integer(-1)},     // C2-GENERAL
            {new Integer(R.id.tab6parent), new Boolean(true), new Integer(-1)},     // C2-RDT
            {new Integer(R.id.tab7parent), new Boolean(true), new Integer(-1)},     // C2-MICROSCOPY
            {new Integer(R.id.tab8parent), new Boolean(true), new Integer(-1)},     // C3-GENERAL
            {new Integer(R.id.tab9parent), new Boolean(true), new Integer(-1)},     // C3-RDT
            {new Integer(R.id.tab10parent), new Boolean(true), new Integer(-1)},    // C3-MICROSCOPY
            {new Integer(R.id.tab11parent), new Boolean(false), new Integer(-1)},   // ADHERENCE
            {new Integer(R.id.tab12parent), new Boolean(false), new Integer(-1)},   // FEEDBACK
            {new Integer(R.id.tab13parent), new Boolean(true), new Integer(-1)},    // ENVIRONMENT & MATERIALS
            {new Integer(R.id.tab14parent), new Boolean(false), new Integer(-1)},   // REPORTING
            {new Integer(R.id.tab15parent), new Boolean(false), new Integer(-1)},   // IQA EQA
            {new Integer(R.id.tab16parent), new Boolean(false), new Integer(-1)}  // SCORE (Score tab layout is fixed)
            //{new Integer(R.id.tab16parent), new Boolean(false), R.layout.scoretab}  // SCORE (Score tab layout is fixed)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.i(".MainActivity", "App started");
        setContentView(R.layout.main_layout);
        final ActionBar actionBar = getActionBar();

//        adb pull /data/data/org.eyeseetea.malariacare/databases/malariacare.db ~/malariacare.db

        // We import the initial data in case it has been done yet
        if (Tab.count(Tab.class, null, null)==0) {
            AssetManager assetManager = getAssets();
            PopulateDB.populateDB(assetManager);
        }




        // We get all tabs and insert their content in their layout
        List<Object[]> tabLayoutList = Arrays.asList(tabsLayouts);
        List<Tab> tabList2 = Tab.listAll(Tab.class);
        int tabLayout;
        for (int i = 0; i< tabLayoutList.size(); i++){
            Tab tabItem = tabList2.get(i);
            Log.d(".MainActivity", tabItem.toString());
            Layout.insertTab(this, tabItem, ((Integer)tabLayoutList.get(i)[0]).intValue(), ((Boolean)tabLayoutList.get(i)[1]).booleanValue(), ((Integer)tabLayoutList.get(i)[2]).intValue());
        }
        // Score tab is a little bit special, we don't need to add it
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveClearResults(View view) {

        if (view.getId() == R.id.save) {
            Log.d(".MainActivity", "Button save pressed");
        }
        else if (view.getId() == R.id.clear) {
            Log.d(".MainActivity", "Button clear pressed");
        }
        ArrayList<View> allViewsWithinMyTopView = getAllChildren(view);
        for (View child : allViewsWithinMyTopView) {
            if (child instanceof TextView) {
                TextView childTextView = (TextView) child;
                Log.d(".MainActivity", childTextView.getText().toString());
            }
            else if(child instanceof Spinner){

            }
        }

    }

    private ArrayList<View> getAllChildren(View v) {

        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<View>();

        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {

            View child = vg.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            viewArrayList.addAll(getAllChildren(child));

            result.addAll(viewArrayList);
        }
        return result;
    }

    public static List<Integer> getLayoutIds(){
        List<Integer> ids = new ArrayList<Integer>();
        for(int i=0; i<tabsLayouts.length; i++){
            ids.add((Integer) tabsLayouts[i][0]);
        }
        return ids;
    }
}
