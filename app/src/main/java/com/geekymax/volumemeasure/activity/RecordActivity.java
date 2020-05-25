package com.geekymax.volumemeasure.activity;

import android.os.Bundle;

import com.geekymax.volumemeasure.R;
import com.geekymax.volumemeasure.adapter.HistorySwipeAdapter;
import com.geekymax.volumemeasure.manager.RecordManager;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class RecordActivity extends BaseActivity {
    private ListView listView;
    private HistorySwipeAdapter listAdapter;
    private static final String TAG = "Geeky-History";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_history);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        setSupportActionBar(toolbar);
//        toolbar.inflateMenu(R.menu.history_menu);
        RecyclerView listView = findViewById(R.id.recycler_view);
        listAdapter = new HistorySwipeAdapter(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(listAdapter);
        RecordManager.getInstance().getAllRecord(list -> {
            runOnUiThread(() -> {
                listAdapter.setRecordList(list);
                listAdapter.notifyDataSetChanged();
            });
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.clear) {
            RecordManager.getInstance().clearHistory();
            listAdapter.clearRecord();
        }
        return super.onOptionsItemSelected(item);
    }

    // 为toolbar添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        return true;
    }
}
