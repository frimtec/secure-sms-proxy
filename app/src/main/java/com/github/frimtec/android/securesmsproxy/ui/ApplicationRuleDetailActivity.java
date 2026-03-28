package com.github.frimtec.android.securesmsproxy.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.frimtec.android.securesmsproxy.R;
import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.service.ApplicationRuleDao;
import com.github.frimtec.android.securesmsproxy.service.PhoneNumberFormatter;
import com.github.frimtec.android.securesmsproxy.utility.AlertDialogHelper;
import com.github.frimtec.android.securesmsproxy.utility.PackageInfoAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationRuleDetailActivity extends BaseActivity {

  public static final String EXTRA_APPLICATION_NAME = "application_name";

  private ApplicationRuleDao dao;
  private String applicationName;
  private ApplicationRule applicationRule;
  private PhoneNumberArrayAdapter adapter;
  private List<String> phoneNumbers;

  @Override
  protected void doOnCreate(@Nullable Bundle savedInstanceState) {
    setContentView(R.layout.activity_application_rule_detail);

    dao = new ApplicationRuleDao();
    applicationName = getIntent().getStringExtra(EXTRA_APPLICATION_NAME);
    if (applicationName == null) {
      finish();
      return;
    }

    refresh();

    if (applicationRule == null) {
      finish();
      return;
    }

    PackageInfoAccessor packageInfoAccessor = new PackageInfoAccessor(this);
    ImageView logo = findViewById(R.id.application_logo);
    packageInfoAccessor.getIcon(applicationName).ifPresent(logo::setImageDrawable);

    TextView label = findViewById(R.id.application_label);
    label.setText(packageInfoAccessor.getLabel(applicationName));

    TextView name = findViewById(R.id.application_name);
    name.setText(applicationName);

    ListView listView = findViewById(R.id.phone_numbers_list);
    phoneNumbers = new ArrayList<>(applicationRule.allowedPhoneNumbers());
    Collections.sort(phoneNumbers);
    adapter = new PhoneNumberArrayAdapter(this, phoneNumbers, this::deletePhoneNumber);
    listView.setAdapter(adapter);

    Button addButton = findViewById(R.id.add_phone_number_button);
    addButton.setOnClickListener(v -> addPhoneNumber());
  }

  private void refresh() {
    applicationRule = dao.byApplicationName(applicationName);
  }

  private void deletePhoneNumber(String phoneNumber) {
    AlertDialogHelper.areYouSure(this, (dialog, which) -> {
      dao.deletePhoneNumber(applicationRule.application().id(), phoneNumber);
      phoneNumbers.remove(phoneNumber);
      adapter.notifyDataSetChanged();
    }, (dialog, which) -> {
    });
  }

  private void addPhoneNumber() {
    EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    input.setHint(R.string.application_rule_detail_phone_number_hint);
    input.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
      boolean startsWithPlus = (dstart == 0 && end > start && source.charAt(start) == '+') ||
          (dstart > 0 && dest.length() > 0 && dest.charAt(0) == '+');
      for (int i = start; i < end; i++) {
        char c = source.charAt(i);
        if (c == '+') {
          if (dstart > 0 || i > start) {
            return "";
          }
        } else if (startsWithPlus) {
          if (!Character.isDigit(c)) {
            return "";
          }
        } else if (!Character.isLetterOrDigit(c)) {
          return "";
        }
      }
      return null;
    }});

    AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle(R.string.application_rule_detail_add_phone_number_title)
        .setView(input)
        .setPositiveButton(R.string.action_add, (d, which) -> {
          String newNumber = input.getText().toString();
          PhoneNumberFormatter formatter = new PhoneNumberFormatter(this);
          String e164Number = formatter.toE164(newNumber);
          if (e164Number != null) {
            String numberToAdd = e164Number.toUpperCase();
            dao.addPhoneNumber(applicationRule.application().id(), numberToAdd);
            if (!phoneNumbers.contains(numberToAdd)) {
              phoneNumbers.add(numberToAdd);
              Collections.sort(phoneNumbers);
              adapter.notifyDataSetChanged();
            }
          }
        })
        .setNegativeButton(android.R.string.cancel, (d, which) -> d.cancel())
        .create();

    dialog.setOnShowListener(d -> {
      Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
      addButton.setEnabled(false);
      PhoneNumberFormatter formatter = new PhoneNumberFormatter(this);
      input.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
          addButton.setEnabled(!TextUtils.isEmpty(formatter.getFormattedNumber(s.toString())));
        }
      });
    });
    dialog.show();
  }
}
