package com.github.frimtec.android.securesmsproxy.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.R;
import com.github.frimtec.android.securesmsproxy.domain.Application;
import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.service.PhoneNumberFormatter;
import com.github.frimtec.android.securesmsproxy.utility.PackageInfoAccessor;
import com.github.frimtec.android.securesmsproxyapi.utility.PhoneNumberType;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

class ApplicationRuleArrayAdapter extends ArrayAdapter<ApplicationRule> {

  private final PackageInfoAccessor packageInfoAccessor;
  private final BiFunction<ApplicationRuleArrayAdapter, ApplicationRule, View.OnClickListener> deleteAction;

  ApplicationRuleArrayAdapter(
      Context context,
      List<ApplicationRule> applicationRules,
      BiFunction<ApplicationRuleArrayAdapter, ApplicationRule, View.OnClickListener> deleteAction) {
    super(context, 0, applicationRules);
    this.packageInfoAccessor = new PackageInfoAccessor(getContext());
    this.deleteAction = deleteAction;
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.application_rule_item, parent, false);
    }
    ApplicationRule applicationRule = getItem(position);
    if (applicationRule != null) {
      Application application = applicationRule.application();
      ImageView logo = convertView.findViewById(R.id.application_logo);
      packageInfoAccessor.getIcon(application.name()).ifPresent(logo::setImageDrawable);
      TextView label = convertView.findViewById(R.id.application_label);

      CharSequence labelText = this.packageInfoAccessor.getLabel(application.name());
      if (!packageInfoAccessor.isInstalled(application.name())) {
        labelText = labelText + "\n(not installed)";
      }
      label.setText(labelText);
      TextView phoneNumbers = convertView.findViewById(R.id.application_allowed_phone_numbers);
      String networkCountryIso = PhoneNumberType.networkCountryIso(getContext());
      phoneNumbers.setText(applicationRule.allowedPhoneNumbers()
          .stream()
          .map(number -> PhoneNumberFormatter.getFormattedNumber(number, networkCountryIso))
          .collect(Collectors.joining("\n"))
      );

      ImageButton deleteButton = convertView.findViewById(R.id.application_button_delete);
      deleteButton.setOnClickListener(deleteAction.apply(this, applicationRule));
    }
    return convertView;
  }

}
