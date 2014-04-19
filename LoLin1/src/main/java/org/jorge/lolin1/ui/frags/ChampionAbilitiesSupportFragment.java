package org.jorge.lolin1.ui.frags;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.jorge.lolin1.func.custom.ChampionAbilitiesListAdapter;

/**
 * This file is part of LoLin1.
 * <p/>
 * LoLin1 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * LoLin1 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with LoLin1. If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by JorgeAntonio on 19/04/2014.
 */
public class ChampionAbilitiesSupportFragment extends ChampionDetailSupportFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        ListView abilitiesListView = (ListView) view.findViewById(android.R.id.list);

        abilitiesListView
                .setAdapter(new ChampionAbilitiesListAdapter(getActivity(), getSelectedChampion()));

        return view;
    }
}

//TODO (Could be anywhere) When implementing the showCaseView, use the Snipt that you saved to lock the position of the news reader as portrait until an article has been selected