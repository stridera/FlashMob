package com.stridera.connectivitycreations.flashmob.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stridera.connectivitycreations.flashmob.R;
import com.stridera.connectivitycreations.flashmob.models.DrawerBaseItem;
import com.stridera.connectivitycreations.flashmob.models.DrawerHeader;
import com.stridera.connectivitycreations.flashmob.models.DrawerItem;
import com.stridera.connectivitycreations.flashmob.models.DrawerSeparator;

import java.util.ArrayList;


public class StreamDrawerAdapter extends RecyclerView.Adapter<StreamDrawerAdapter.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_SEPARATOR = 2;

    Context context;
    private ArrayList<DrawerBaseItem> drawerItems;

    // The ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        int id;

        // Header
        ImageView ivProfile;
        TextView tvName;
        TextView tvEmail;
        TextView tvBio;
        // Item
        TextView tvTitle;
        ImageView ivIcon;
        // Separator
        TextView tvSeperatorTitle;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);

            if (viewType == TYPE_HEADER) {
                ivProfile = (ImageView) itemView.findViewById(R.id.civDrawerHeaderProfile);
                tvName = (TextView) itemView.findViewById(R.id.tvDrawerHeaderName);
                tvEmail = (TextView) itemView.findViewById(R.id.tvDrawerHeaderEmail);
                tvBio = (TextView) itemView.findViewById(R.id.tvDrawerHeaderBio);
            } else if (viewType == TYPE_ITEM) {
                tvTitle = (TextView) itemView.findViewById(R.id.tvDrawerItemTitle);
                ivIcon = (ImageView) itemView.findViewById(R.id.ivDrawerItemIcon);
            } else if (viewType == TYPE_SEPARATOR) {
                tvSeperatorTitle = (TextView) itemView.findViewById(R.id.tvDrawerSeparatorTitle);
            } else {
                // Invalid Type
            }
        }
    }

    // Constructor
    public StreamDrawerAdapter(ArrayList<DrawerBaseItem> drawerItems, Context context) {
        this.drawerItems = drawerItems;
        this.context = context;
    }

    // Overrides

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == TYPE_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header, parent, false);
        } else if (viewType == TYPE_ITEM) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_item, parent, false);
        } else { // if (viewType == TYPE_SEPARATOR)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_separator, parent, false);
        }

        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        int viewType = viewHolder.getItemViewType();

        if (viewType == TYPE_HEADER) {
            DrawerHeader header = (DrawerHeader) drawerItems.get(position);
            if (header.getProfileImageUrl().isEmpty()) {
                viewHolder.ivProfile.setImageResource(R.drawable.unknown_user);
            } else {
                Picasso.with(context)
                        .load(header.getProfileImageUrl())
                        .resize(70, 70)
                        .placeholder(R.drawable.unknown_user)
                        .into(viewHolder.ivProfile);
            }
            viewHolder.tvName.setText(header.getName());
            viewHolder.tvEmail.setText(header.getEmail());
            viewHolder.tvBio.setText(header.getBio());
        } else if (viewType == TYPE_ITEM) {
            DrawerItem item = (DrawerItem) drawerItems.get(position);
            viewHolder.ivIcon.setImageResource(item.getImgResID());
            viewHolder.tvTitle.setText(item.getTitle());
        } else { // if (viewType == TYPE_SEPARATOR)
            DrawerSeparator separator = (DrawerSeparator) drawerItems.get(position);
            viewHolder.tvSeperatorTitle.setText(separator.getTitle());
        }
    }

    @Override
    public int getItemCount() {
        return drawerItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return drawerItems.get(position).getItemType();
    }
}