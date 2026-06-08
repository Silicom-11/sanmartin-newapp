package com.iepca.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iepca.app.R;
import com.iepca.app.model.Parent;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for Parents list.
 */
public class ParentAdapter extends RecyclerView.Adapter<ParentAdapter.ViewHolder> {

    private List<Parent> items = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Parent parent);
    }

    public ParentAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Parent> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvChildren;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvChildren = itemView.findViewById(R.id.tvChildren);
        }

        void bind(Parent p) {
            tvName.setText(p.getFirstName() + " " + p.getLastName());
            int count = p.getChildren() != null ? p.getChildren().size() : 0;
            tvChildren.setText(count + " hijo" + (count != 1 ? "s" : "") + " vinculado" + (count != 1 ? "s" : ""));
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(p);
            });
        }
    }
}
