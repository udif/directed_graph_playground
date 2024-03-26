package com.udifink.johnson75;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.normalisiert.utils.graphs.ElementaryCyclesSearch;

public class MainActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tableLayout = findViewById(R.id.tableLayout);
        textView = findViewById(R.id.textView);

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRow();
            }
        });

        Button sumButton = findViewById(R.id.runButton);
        sumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                run();
            }
        });

        Button sortButton = findViewById(R.id.sortButton);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sort();
            }
        });

        // Add initial row
        addHeaders();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_about) {
            about(R.string.about_title, R.string.about_message);
            return true;
        } else if (item.getItemId() == R.id.nav_about_implementation) {
            about(R.string.about_title, R.string.implementation_license);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void addHeaders() {
        TableRow row = new TableRow(this);
        TextView source = new TextView(this);
        source.setText("Source");
        row.addView(source);

        TextView destination = new TextView(this);
        destination.setText("Destination");
        row.addView(destination);

        tableLayout.addView(row);
    }

    private void addRow() {
        final TableRow row = new TableRow(this);
        TextView source = new EditText(this);
        row.addView(source);

        TextView destination = new EditText(this);
        row.addView(destination);

        ImageButton deleteButton = new ImageButton(this);
        deleteButton.setImageResource(android.R.drawable.ic_delete);
        deleteButton.setBackgroundColor(Color.TRANSPARENT);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tableLayout.removeView(row);
                run();
            }
        });
        row.addView(deleteButton);

        tableLayout.addView(row);
    }

    private void sort() {
        // Assuming tableLayout is your TableLayout
        int childCount = tableLayout.getChildCount();

        // Create a list of all rows in the table
        List<TableRow> rows = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            rows.add((TableRow) tableLayout.getChildAt(i));
        }
        TableRow row0 = rows.get(0);
        // Remove all views from the layout
        tableLayout.removeAllViews();

        // Sort the row list based on the values in the first column
        Collections.sort(rows, new Comparator<TableRow>() {
            @Override
            public int compare(TableRow row1, TableRow row2) {
                if (row1 == row0)
                    return -1;
                if (row2 == row0)
                    return 1;
                TextView textView1 = (TextView) row1.getChildAt(0);
                TextView textView2 = (TextView) row2.getChildAt(0);
                Integer value2 = Integer.parseInt(textView2.getText().toString());
                Integer value1 = Integer.parseInt(textView1.getText().toString());
                if (!value1.equals(value2))
                    return value1.compareTo(value2);
                textView1 = (TextView) row1.getChildAt(1);
                textView2 = (TextView) row2.getChildAt(1);
                value1 = Integer.parseInt(textView1.getText().toString());
                value2 = Integer.parseInt(textView2.getText().toString());
                return value1.compareTo(value2);
            }
        });

        // Add rows back to the table in sorted order
        for (TableRow row : rows) {
            tableLayout.addView(row);
        }
    }

    private void run() {
        int num_vertices = 0;
        for (int i = 1; i < tableLayout.getChildCount(); i++) { // start from 1 to skip headers
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            TextView tvs = (TextView) row.getChildAt(0);
            TextView tvd = (TextView) row.getChildAt(1);
            int source = Integer.parseInt(tvs.getText().toString());
            int destination = Integer.parseInt(tvd.getText().toString());
            if (source < 0 || destination < 0)
                textView.setText(R.string.illegal_values);
            if (num_vertices < source)
                num_vertices = source;
            if (num_vertices < destination)
                num_vertices = destination;
        }
        num_vertices++; // because nodes starts at 0
        boolean[][] adjMatrix = new boolean[num_vertices][num_vertices];
        String[] nodes = new String[num_vertices];

        for (int i = 0; i < num_vertices; i++) {
            nodes[i] = Integer.toString(i);
        }

        for (int i = 1; i < tableLayout.getChildCount(); i++) { // start from 1 to skip headers
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            TextView tvs = (TextView) row.getChildAt(0);
            TextView tvd = (TextView) row.getChildAt(1);
            int source = Integer.parseInt(tvs.getText().toString());
            int destination = Integer.parseInt(tvd.getText().toString());
            adjMatrix[source][destination] = true;
        }
        ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(adjMatrix, nodes);

        StringBuilder sb = new StringBuilder();
        List cycles = ecs.getElementaryCycles();
        for (int i = 0; i < cycles.size(); i++) {
            List cycle = (List) cycles.get(i);
            for (int j = 0; j < cycle.size(); j++) {
                String node = (String) cycle.get(j);
                sb.append(node);
                if (j < cycle.size() - 1) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        textView.setText(sb.toString());
    }

    private void about(int title, int body) {
        // Create an AlertDialog.Builder instance
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // Set the title and message
        builder.setTitle(title);
        builder.setMessage(body);

        // Add an OK button
        builder.setPositiveButton("OK", null);

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
