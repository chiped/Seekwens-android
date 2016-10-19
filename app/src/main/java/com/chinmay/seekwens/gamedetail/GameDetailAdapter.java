package com.chinmay.seekwens.gamedetail;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.model.Player;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

public class GameDetailAdapter extends FirebaseRecyclerAdapter<Player, GameDetailViewHolder> {

    private final boolean isOwner;
    private TeamSelectListener teamSelectListener;
    private DragStartListener dragStartListener;

    public GameDetailAdapter(Query ref, boolean isOwner) {
        super(Player.class, R.layout.game_detail_row, GameDetailViewHolder.class, ref);
        this.isOwner = isOwner;
    }

    @Override
    protected void populateViewHolder(GameDetailViewHolder viewHolder, final Player model, int position) {
        viewHolder.bind(model);
        if (isOwner) {
            viewHolder.setTeamSelectListener(new GameDetailViewHolder.TeamSelectInternalListener() {
                @Override
                public void onTeamSelected(int team) {
                    teamSelectListener.onTeamSelected(model.id, team);
                }
            });
            viewHolder.setDragStartListener(new GameDetailViewHolder.DragStartInternalListener() {
                @Override
                public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                    dragStartListener.onStartDrag(viewHolder);
                }
            });
        }
    }

    @Override
    public GameDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new GameDetailViewHolder(view, isOwner);
    }

    public void setTeamSelectListener(TeamSelectListener teamSelectListener) {
        this.teamSelectListener = teamSelectListener;
    }

    public void setDragStartListener(DragStartListener dragStartListener) {
        this.dragStartListener = dragStartListener;
    }
}
