/*
 * Copyright (c) 2015.
 *
 * This file is part of QIS Surveillance App.
 *
 *  QIS Surveillance App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  QIS Surveillance App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with QIS Surveillance App.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.layout.adapters.survey;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.layout.utils.LayoutUtils;
import org.eyeseetea.malariacare.utils.Utils;
import org.eyeseetea.malariacare.views.TextCard;

import java.util.List;

/**
 * Created by Jose on 21/04/2015.
 */
public class CompositeScoreAdapter extends BaseAdapter implements ITabAdapter {

    List<CompositeScore> items;
    private LayoutInflater lInflater;
    private final Context context;
    int id_layout;
    String tab_name;

    public CompositeScoreAdapter(List<CompositeScore> items, Context context, int id_layout, String tab_name) {
        this.items = items;
        this.context = context;
        this.id_layout = id_layout;
        this.tab_name = tab_name;

        this.lInflater=LayoutInflater.from(context);
    }

    @Override
    public void initializeSubscore() {

        ListView compositeScoreListView = (ListView) ((Activity) context).findViewById(R.id.listView);

        ViewGroup header = (ViewGroup) lInflater.inflate(R.layout.composite_score_header, compositeScoreListView, false);
        compositeScoreListView.addHeaderView(header);

    }

    @Override
    public BaseAdapter getAdapter() {
        return this;
    }

    @Override
    public Float getScore() {
        return null;
    }

    @Override
    public int getLayout() {
        return id_layout;
    }

    @Override
    public String getName() {
        return tab_name;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;

        CompositeScore item = (CompositeScore) getItem(position);

        rowView = lInflater.inflate(R.layout.composite_scores_record, parent, false);

        ((TextCard)rowView.findViewById(R.id.code)).setText(item.getHierarchical_code());
        ((TextCard)rowView.findViewById(R.id.label)).setText(item.getLabel());

        Float compositeScoreValue = ScoreRegister.getCompositeScore(item);

        if (compositeScoreValue == null)
            ((TextCard)rowView.findViewById(R.id.score)).setText(this.context.getString(R.string.number_zero));
        else
            ((TextCard)rowView.findViewById(R.id.score)).setText(Utils.round(compositeScoreValue));

        rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        return rowView;
    }
}