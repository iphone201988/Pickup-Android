package com.pickup.sports.ui.base;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import com.pickup.sports.BR;
import com.pickup.sports.data.model.GetAllGameApiResponse;
import com.pickup.sports.data.model.GetAllGames;
import com.pickup.sports.data.model.GetHostGames;
import com.pickup.sports.data.model.GetHostGamesApiResponse;
import com.pickup.sports.data.model.GetMyGames;
import com.pickup.sports.data.model.GetMyGamesApiResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleRecyclerViewAdapter<M, B extends ViewDataBinding> extends RecyclerView.Adapter<SimpleRecyclerViewAdapter.SimpleViewHolder<B>> {

    @LayoutRes
    private final int layoutResId;
    private final int modelVariableId;
    private final SimpleCallback<M> callback;
    private final List<M> dataList = new ArrayList<>();
    private boolean loading = false;

    public void removeItem(int i) {
        Log.i("TAG", "removeItem: "+i);
        try {
            if (i != -1) {
                dataList.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, dataList.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteObject(M bean) {
        try {
            if (bean != null) {
                dataList.remove(bean);
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void set(int i, M scanResult) {
        if (scanResult == null)
            return;
        dataList.add(i, scanResult);
        notifyItemChanged(i);
    }


    public interface SimpleCallback<M> {
        void onItemClick(View v, M m, int pos);
    }

    public SimpleRecyclerViewAdapter(@LayoutRes int layoutResId, int modelVariableId, SimpleCallback<M> callback) {
        this.layoutResId = layoutResId;
        this.modelVariableId = modelVariableId;
        this.callback = callback;
    }

    @NonNull
    @Override
    public SimpleViewHolder<B> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        B binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutResId, parent, false);
        return new SimpleViewHolder<>(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position) {

        if (!loading && position < dataList.size()) {
            holder.binding.setVariable(BR.callback, callback);
            holder.binding.setVariable(modelVariableId, dataList.get(position));
            holder.binding.setVariable(BR.pos, position);
        } else {
            holder.binding.setVariable(BR.callback, null);
            holder.binding.setVariable(modelVariableId, null);
            holder.binding.setVariable(BR.pos, 0);
        }
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        if (loading) return 10;
        else
            return dataList.size();
    }

    public void setList(@Nullable List<M> newDataList) {
        dataList.clear();
        if (newDataList != null)
            dataList.addAll(newDataList);
        notifyDataSetChanged();
    }



    public List<M> getList() {
        return dataList;
    }
    public void addToList(@Nullable List<M> newDataList) {
        if (newDataList == null) {
            newDataList = Collections.emptyList();
        }
        int positionStart = dataList.size();
        int itemCount = newDataList.size();
        dataList.addAll(newDataList);
        notifyItemRangeInserted(positionStart, itemCount);
        Log.i("Dfdsfds", "addToList: "+ dataList.size());
    }


//    public void addToLis1t(@Nullable List<M> newDataList) {
//        if (newDataList == null) {
//            newDataList = Collections.emptyList();
//        }
//
//        for (M newItem : newDataList) {
//            boolean merged = false;
//
//            for (int i = 0; i < dataList.size(); i++) {
//                M existingItem = dataList.get(i);
//
//                // Assuming M is GetAllGames and you want to merge by date
//                if (existingItem instanceof GetAllGames && newItem instanceof GetAllGames) {
//                    GetAllGames existingGroup = (GetAllGames) existingItem;
//                    GetAllGames newGroup = (GetAllGames) newItem;
//
//                    if (existingGroup.getDate().equals(newGroup.getDate())) {
//                        List<GetAllGameApiResponse.Game> mergedGames = new ArrayList<>(existingGroup.getGameList());
//                        mergedGames.addAll(newGroup.getGameList());
//
//                        dataList.set(i, (M) new GetAllGames(existingGroup.getDate(),true, mergedGames,""));
//                        merged = true;
//                        notifyItemChanged(i);
//                        break;
//                    }
//                }
//            }
//
//            if (!merged) {
//                dataList.add(newItem);
//                notifyItemInserted(dataList.size() - 1);
//            }
//        }
//    }
//public void addToLis1t(@Nullable List<M> newDataList) {
//    if (newDataList == null || newDataList.isEmpty()) return;
//
//    int startPosition = dataList.size();
//    dataList.addAll(newDataList);
//    notifyItemRangeInserted(startPosition, newDataList.size());
//}


    public void addToListHost(@Nullable List<M> newDataList) {
        if (newDataList == null) {
            newDataList = Collections.emptyList();
        }

        for (M newItem : newDataList) {
            boolean merged = false;

            for (int i = 0; i < dataList.size(); i++) {
                M existingItem = dataList.get(i);

                // Assuming M is GetHost and you want to merge by date
                if (existingItem instanceof GetHostGames && newItem instanceof GetHostGames) {
                    GetHostGames existingGroup = (GetHostGames) existingItem;
                    GetHostGames newGroup = (GetHostGames) newItem;

                    if (existingGroup.getDate().equals(newGroup.getDate())) {
                        List<GetHostGamesApiResponse.Game> mergedGames = new ArrayList<>(existingGroup.getGameList());
                        mergedGames.addAll(newGroup.getGameList());

                        dataList.set(i, (M) new GetHostGames(existingGroup.getDate(),false, mergedGames));
                        merged = true;
                        notifyItemChanged(i);
                        break;
                    }
                }
            }

            if (!merged) {
                dataList.add(newItem);
                notifyItemInserted(dataList.size() - 1);
            }
        }
    }



    public void addToListPast(@Nullable List<M> newDataList) {
        if (newDataList == null) {
            newDataList = Collections.emptyList();
        }

        for (M newItem : newDataList) {
            boolean merged = false;

            for (int i = 0; i < dataList.size(); i++) {
                M existingItem = dataList.get(i);

                // Assuming M is Getmygames and you want to merge by date
                if (existingItem instanceof GetMyGames && newItem instanceof GetMyGames) {
                    GetMyGames existingGroup = (GetMyGames) existingItem;
                    GetMyGames newGroup = (GetMyGames) newItem;

                    if (existingGroup.getDate().equals(newGroup.getDate())) {
                        List<GetMyGamesApiResponse.Game> mergedGames = new ArrayList<>(existingGroup.getGameList());
                        mergedGames.addAll(newGroup.getGameList());

                        dataList.set(i, (M) new GetMyGames(existingGroup.getDate(),false, mergedGames));
                        merged = true;
                        notifyItemChanged(i);
                        break;
                    }
                }
            }

            if (!merged) {
                dataList.add(newItem);
                notifyItemInserted(dataList.size() - 1);
            }
        }
    }


//    public void addToList(List<M> list) {
//        if (list == null || list.isEmpty()) return;
//
//        list.clear();
//        list.addAll(0, list);
//        notifyDataSetChanged();
//    }


    public void clearList() {
        dataList.clear();
        notifyDataSetChanged();
    }

    public void addData(@NonNull M data) {
        int positionStart = dataList.size();
        dataList.add(data);
        notifyItemInserted(positionStart);
    }

    /**
     * Simple view holder for this adapter
     *
     * @param <S>
     */
    static class SimpleViewHolder<S extends ViewDataBinding> extends RecyclerView.ViewHolder {
        final S binding;

        public SimpleViewHolder(S binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
