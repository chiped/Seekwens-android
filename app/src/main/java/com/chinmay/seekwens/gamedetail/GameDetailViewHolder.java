package com.chinmay.seekwens.gamedetail;

import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.model.Player;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

public class GameDetailViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

    @BindView(R.id.playerName) TextView textView;
    @BindView(R.id.playerTeam) View playerTeam;
    @BindArray(R.array.team_colors) int[] teamColors;
    @BindArray(R.array.team_names) String[] teamNames;

    private TeamSelectInternalListener listener;

    public GameDetailViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
        playerTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
    }

    private void showPopup(View view) {
        final PopupMenu popup = new PopupMenu(view.getContext(), view);
        final MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.team_select_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    public void bind(Player model) {
        textView.setText(model.getName());
        playerTeam.setBackgroundColor(teamColors[model.team]);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final int team = getTeamNumberFromName(item.getTitle().toString());
        listener.onTeamSelected(team);
        return true;
    }

    private int getTeamNumberFromName(String title) {
        for (int i = 0; i < teamNames.length; i++) {
            if (teamNames[i].equals(title)) {
                return i;
            }
        }
        return -1;
    }

    public void setListener(TeamSelectInternalListener listener) {
        this.listener = listener;
    }

    interface TeamSelectInternalListener {
        void onTeamSelected(int team);
    }

}
