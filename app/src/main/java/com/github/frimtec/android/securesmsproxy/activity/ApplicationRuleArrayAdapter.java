package com.github.frimtec.android.securesmsproxy.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.R;
import com.github.frimtec.android.securesmsproxy.domain.Application;
import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;

import java.util.List;
import java.util.Optional;

public class ApplicationRuleArrayAdapter extends ArrayAdapter<ApplicationRule> {

  ApplicationRuleArrayAdapter(Context context, List<ApplicationRule> applicationRules) {
    super(context, 0, applicationRules);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.application_rule_item, parent, false);
    }
    ApplicationRule applicationRule = getItem(position);
    if (applicationRule != null) {
      Application application = applicationRule.getApplication();
      ImageView logo = convertView.findViewById(R.id.application_logo);
      lookupIcon(application.getName()).ifPresent(logo::setImageDrawable);
      TextView label = convertView.findViewById(R.id.application_label);
      label.setText(lookupLabel(application.getName()));
      TextView phoneNumbers = convertView.findViewById(R.id.application_allowed_phone_numbers);
      phoneNumbers.setText(TextUtils.join("\n", applicationRule.getAllowedPhoneNumbers()));
    }
    return convertView;
  }

  private Optional<Drawable> lookupIcon(String name) {
    try {
      return Optional.of(getContext().getPackageManager().getApplicationIcon(name));
    } catch (PackageManager.NameNotFoundException e) {
      return Optional.empty();
    }
  }

  private CharSequence lookupLabel(String name) {
    PackageManager packageManager = getContext().getPackageManager();
    try {
      return packageManager.getApplicationLabel(packageManager.getApplicationInfo(name, PackageManager.GET_META_DATA));
    } catch (PackageManager.NameNotFoundException e) {
      return name;
    }
  }


}
