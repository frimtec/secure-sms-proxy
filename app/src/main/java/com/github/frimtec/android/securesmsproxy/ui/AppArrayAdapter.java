package com.github.frimtec.android.securesmsproxy.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.R;
import com.github.frimtec.android.securesmsproxy.utility.PackageInfoAccessor;

import java.util.List;

public class AppArrayAdapter extends ArrayAdapter<String> {

  private final PackageInfoAccessor packageInfoAccessor;

  public AppArrayAdapter(Context context, List<String> packages) {
    super(context, R.layout.app_selection_item, packages);
    this.packageInfoAccessor = new PackageInfoAccessor(context);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.app_selection_item, parent, false);
    }

    String packageName = getItem(position);
    if (packageName != null) {
      ImageView icon = convertView.findViewById(R.id.app_icon);
      TextView label = convertView.findViewById(R.id.app_label);

      packageInfoAccessor.getIcon(packageName).ifPresent(icon::setImageDrawable);
      label.setText(packageInfoAccessor.getLabel(packageName));
    }

    return convertView;
  }
}
