package com.github.frimtec.android.securesmsproxy.ui;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.frimtec.android.securesmsproxy.R;
import com.github.frimtec.android.securesmsproxy.domain.ApplicationRule;
import com.github.frimtec.android.securesmsproxy.service.ApplicationRuleDao;
import com.github.frimtec.android.securesmsproxy.utility.AlertDialogHelper;
import com.github.frimtec.android.securesmsproxy.utility.Permission;

import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.github.frimtec.android.securesmsproxy.utility.Permission.SMS;

public class MainActivity extends AppCompatActivity {

  private static final int MENU_CONTEXT_DELETE_ID = 1;
  private ListView listView;

  private ApplicationRuleDao dao;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (!SMS.isAllowed(this)) {
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
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    menu.add(Menu.NONE, MENU_CONTEXT_DELETE_ID, Menu.NONE, R.string.action_delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    ApplicationRule selectedAlert = (ApplicationRule) listView.getItemAtPosition(info.position);
    if (item.getItemId() == MENU_CONTEXT_DELETE_ID) {
      AlertDialogHelper.areYouSure(this, (dialog, which) -> {
        deleteApplicationRule(selectedAlert);
        refresh();
        Toast.makeText(this, R.string.general_entry_deleted, Toast.LENGTH_SHORT).show();
      }, (dialog, which) -> {
      });
      return true;
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
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);
    if(isDeveloperMode()) {
      menu.findItem(R.id.logcat).setVisible(true);
    }
    return super.onCreateOptionsMenu(menu);
  }

  private boolean isDeveloperMode() {
    return Settings.Global.getInt(getApplicationContext().getContentResolver(),
        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED , 0) != 0;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.about) {
      startActivity(new Intent(this, AboutActivity.class));
      return true;
    } else if (item.getItemId() == R.id.logcat) {
      startActivity(new Intent(this, LogcatActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

}
