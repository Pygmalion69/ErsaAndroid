package eu.sergehelfrich.ersaandroid;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.List;
import java.util.Set;

public class SelectOriginDialogFragment extends AppCompatDialogFragment {

    private List<String> mAllOrigins;
    private Set<String> mExcludedOrigins;
    private SharedPreferences mPreferences;
    private OnCloseListener mOnClosedListener;

    public void setOrigins(List<String> allOrigins, Set<String> excludedOrigins) {
        mAllOrigins = allOrigins;
        mExcludedOrigins = excludedOrigins;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_select_origin, null);
        builder.setView(view);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        OriginAdapter adapter = new OriginAdapter(mAllOrigins);
        recyclerView.setAdapter(adapter);

        mPreferences = getActivity().getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);

        mOnClosedListener = (OnCloseListener) getActivity();

        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mOnClosedListener.dialogClosed();
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        mOnClosedListener.dialogClosed();
        super.onCancel(dialog);
    }

    public class OriginAdapter extends RecyclerView.Adapter<OriginAdapter.ViewHolder> implements CompoundButton.OnCheckedChangeListener {

        private final List<String> mItems;

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String origin = (String) buttonView.getTag();
            if (!TextUtils.isEmpty(origin)) {
                if (isChecked) {
                    mExcludedOrigins.remove(origin);
                } else {
                    mExcludedOrigins.add(origin);
                }
                mPreferences.edit().putStringSet(MainActivity.PREF_EXCLUDED_ORIGINS, mExcludedOrigins).apply();
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            Switch mSwitch;

            ViewHolder(Switch v) {
                super(v);
                mSwitch = v;
            }
        }

        OriginAdapter(List<String> items) {
            mItems = items;
        }

        @Override
        public OriginAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
            Switch v = (Switch) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_select_origin, parent, false);
            v.setOnCheckedChangeListener(this);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mSwitch.setText(mItems.get(position));
            holder.mSwitch.setChecked(!mExcludedOrigins.contains(mItems.get(position)));
            holder.mSwitch.setTag(mItems.get(position));
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    interface OnCloseListener {
        void dialogClosed();
    }
}
