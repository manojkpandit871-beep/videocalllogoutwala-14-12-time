package com.example.videocallapp.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.videocallapp.db.AppDatabase;
import com.example.videocallapp.databinding.ActivityCallHistoryBinding;
import com.example.videocallapp.model.CallRecord;
import com.example.videocallapp.ui.adapter.CallHistoryAdapter;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shows the call history list from Room database.
 */
public class CallHistoryActivity extends AppCompatActivity {

    private ActivityCallHistoryBinding binding;
    private CallHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new CallHistoryAdapter();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Observe call history from Room
        AppDatabase.getInstance(this)
                   .callDao()
                   .getAllCalls()
                   .observe(this, records -> {
                       if (records == null || records.isEmpty()) {
                           binding.tvEmpty.setVisibility(View.VISIBLE);
                           binding.recyclerView.setVisibility(View.GONE);
                       } else {
                           binding.tvEmpty.setVisibility(View.GONE);
                           binding.recyclerView.setVisibility(View.VISIBLE);
                           adapter.submitList(records);
                       }
                   });

        // Clear all history
        binding.btnClearHistory.setOnClickListener(v -> {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                AppDatabase.getInstance(getApplicationContext()).callDao().deleteAll();
                executor.shutdown();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
