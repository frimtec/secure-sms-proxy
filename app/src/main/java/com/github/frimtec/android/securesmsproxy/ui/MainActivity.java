package com.github.frimtec.android.securesmsproxy.ui;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.github.frimtec.android.securesmsproxy.utility.Permission.SMS;

import android.app.AlertDialog;
import android.app.LocaleManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.LocaleList;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.frimtec.android.securesmsproxy.R;
import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.service.ApplicationRuleDao;
import com.github.frimtec.android.securesmsproxy.utility.AlertDialogHelper;
import com.github.frimtec.android.securesmsproxy.utility.Permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  private static final int MENU_CONTEXT_DELETE_ID = 1;
  private ListView listView;

  private ApplicationRuleDao dao;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (SMS.isForbidden(this)) {
      SMS.request(this);
    }

    this.dao = new ApplicationRuleDao();
    SwipeRefreshLayout pullToRefresh = findViewById(R.id.list_pull_to_request);
    pullToRefresh.setOnRefreshListener(() -> {
      refresh();
      pullToRefresh.setRefreshing(false);
    });

    this.listView = findViewById(R.id.list);
    listView.setClickable(true);

    View headerView = getLayoutInflater().inflate(R.layout.application_rule_header, listView, false);
    listView.addHeaderView(headerView);
    registerForContextMenu(listView);
    refresh();
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    refresh();
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    menu.add(Menu.NONE, MENU_CONTEXT_DELETE_ID, Menu.NONE, R.string.action_delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    if (info != null) {
      ApplicationRule selectedAlert = (ApplicationRule) listView.getItemAtPosition(info.position);
      if (selectedAlert != null && item.getItemId() == MENU_CONTEXT_DELETE_ID) {
        AlertDialogHelper.areYouSure(this, (dialog, which) -> {
          deleteApplicationRule(selectedAlert);
          refresh();
          Toast.makeText(this, R.string.general_entry_deleted, Toast.LENGTH_SHORT).show();
        }, (dialog, which) -> {
        });
        return true;
      }
    }
    return super.onContextItemSelected(item);
  }

  private void deleteApplicationRule(ApplicationRule applicationRule) {
    dao.delete(applicationRule.getApplication());
  }

  private void refresh() {
    List<ApplicationRule> all = dao.all();
    listView.setAdapter(new ApplicationRuleArrayAdapter(this, all,
        (adapter, applicationRule) -> view -> AlertDialogHelper.areYouSure(adapter.getContext(), (dialog, which) -> {
          dao.delete(applicationRule.getApplication());
          adapter.remove(applicationRule);
          adapter.notifyDataSetChanged();
          Toast.makeText(adapter.getContext(), R.string.general_entry_deleted, Toast.LENGTH_SHORT).show();
        }, (dialog, which) -> {
        })));
    if (all.isEmpty()) {
      Toast.makeText(this, getString(R.string.general_no_data), Toast.LENGTH_LONG).show();
    }
  }

  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (Permission.RequestCodes.PERMISSION_CHANGED_REQUEST_CODE == requestCode) {
      String text = grantResults[0] == PERMISSION_GRANTED ? getString(R.string.permission_accepted) : getString(R.string.permission_rejected);
      Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(@NonNull Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    if (isDeveloperMode()) {
      menu.findItem(R.id.logcat).setVisible(true);
    }
    if (android.os.Build.VERSION.SDK_INT >= 33) {
      menu.findItem(R.id.language).setVisible(true);
    }
    return super.onCreateOptionsMenu(menu);
  }

  private boolean isDeveloperMode() {
    return Settings.Global.getInt(getApplicationContext().getContentResolver(),
        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.about) {
      startActivity(new Intent(this, AboutActivity.class));
      return true;
    } else if (itemId == R.id.language) {
      if (android.os.Build.VERSION.SDK_INT >= 33) {
        LocaleManager localeManager = getSystemService(LocaleManager.class);
        String currentAppLocales = localeManager.getApplicationLocales().toLanguageTags();
        String[] languagesValues = getResources().getStringArray(R.array.languages_values);
        Map<String, Integer> lookup = new HashMap<>();
        for (int i = 0; i < languagesValues.length; i++) {
          lookup.put(languagesValues[i], i);
        }
        Integer selectedItem = lookup.get(currentAppLocales);
        new AlertDialog.Builder(this)
            .setTitle(R.string.pref_title_app_language)
            .setSingleChoiceItems(
                R.array.languages,
                selectedItem != null ? selectedItem : 0,
                (dialogInterface, which) -> updateAppLocales(languagesValues[which])
            ).create()
            .show();
        return true;
      }
    } else if (itemId == R.id.logcat) {
      startActivity(new Intent(this, LogcatActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @RequiresApi(api = 33)
  private void updateAppLocales(String locale) {
    LocaleManager localeManager = getSystemService(LocaleManager.class);
    localeManager.setApplicationLocales(LocaleList.forLanguageTags(locale));
  }

}
