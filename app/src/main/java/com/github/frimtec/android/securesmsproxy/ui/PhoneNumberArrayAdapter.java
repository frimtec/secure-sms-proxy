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
import com.github.frimtec.android.securesmsproxy.service.PhoneNumberFormatter;
import com.github.frimtec.android.securesmsproxyapi.utility.PhoneNumberType;

import java.util.List;
import java.util.function.Consumer;

class PhoneNumberArrayAdapter extends ArrayAdapter<String> {

  private final Consumer<String> deleteAction;

  PhoneNumberArrayAdapter(Context context, List<String> phoneNumbers, Consumer<String> deleteAction) {
    super(context, 0, phoneNumbers);
    this.deleteAction = deleteAction;
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(getContext()).inflate(R.layout.phone_number_item, parent, false);
    }
    String phoneNumber = getItem(position);
    if (phoneNumber != null) {
      String networkCountryIso = PhoneNumberType.networkCountryIso(getContext());
      PhoneNumberType type = PhoneNumberType.fromNumber(phoneNumber, networkCountryIso);
      ImageView typeIcon = convertView.findViewById(R.id.phone_number_type_icon);
      typeIcon.setImageResource(switch (type) {
        case STANDARD, NUMERIC_SHORT_CODE -> R.drawable.swap_vert_24px;
        case ALPHANUMERIC_SHORT_CODE -> R.drawable.south_24px;
        case EMPTY -> 0;
      });

      TextView phoneNumberText = convertView.findViewById(R.id.phone_number_text);
      phoneNumberText.setText(PhoneNumberFormatter.getFormattedNumber(phoneNumber, networkCountryIso));

      ImageButton deleteButton = convertView.findViewById(R.id.phone_number_delete);
      deleteButton.setOnClickListener(v -> deleteAction.accept(phoneNumber));
    }
    return convertView;
  }
}
