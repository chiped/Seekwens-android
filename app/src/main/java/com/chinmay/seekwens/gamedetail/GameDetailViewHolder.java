package com.chinmay.seekwens.gamedetail;

import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chinmay.seekwens.R;
import com.chinmay.seekwens.model.Player;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

public class GameDetailViewHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener {

    @BindView(R.id.playerName) TextView textView;
    @BindView(R.id.playerTeam) View playerTeam;
    @BindView(R.id.playerDragHandle) ImageView playerDragHandle;

    @BindArray(R.array.team_colors) int[] teamColors;
    @BindArray(R.array.team_names) String[] teamNames;

    private final boolean isOwner;
    private TeamSelectInternalListener teamSelectListener;
    private DragStartInternalListener dragStartListener;

    public GameDetailViewHolder(View view, boolean isOwner) {
        super(view);
        ButterKnife.bind(this, view);
        this.isOwner = isOwner;
        if (isOwner) {
            playerTeam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPopup(view);
                }
            });
            playerDragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        dragStartListener.onStartDrag(GameDetailViewHolder.this);
                    }
                    return false;
                }
            });
        }
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
        playerDragHandle.setVisibility(isOwner ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final int team = getTeamNumberFromName(item.getTitle().toString());
        teamSelectListener.onTeamSelected(team);
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

    public void setTeamSelectListener(TeamSelectInternalListener listener) {
        this.teamSelectListener = listener;
    }

    public void setDragStartListener(DragStartInternalListener dragStartListener) {
        this.dragStartListener = dragStartListener;
    }

    interface TeamSelectInternalListener {
        void onTeamSelected(int team);
    }

    interface DragStartInternalListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

}
