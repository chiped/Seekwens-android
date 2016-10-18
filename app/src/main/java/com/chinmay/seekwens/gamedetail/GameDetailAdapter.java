package com.chinmay.seekwens.gamedetail;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.model.Player;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

public class GameDetailAdapter extends FirebaseRecyclerAdapter<Player, GameDetailViewHolder> {

    private TeamSelectListener listener;

    public GameDetailAdapter(DatabaseReference ref) {
        super(Player.class, R.layout.game_detail_row, GameDetailViewHolder.class, ref);
    }


    @Override
    protected void populateViewHolder(GameDetailViewHolder viewHolder, final Player model, int position) {
        viewHolder.bind(model);
        viewHolder.setListener(new GameDetailViewHolder.TeamSelectInternalListener() {
            @Override
            public void onTeamSelected(int team) {
                listener.onTeamSelected(model.id, team);
            }
        });
    }

    public void setListener(TeamSelectListener listener) {
        this.listener = listener;
    }
}
