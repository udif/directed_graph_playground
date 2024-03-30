package com.udifink.dgplayground;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.normalisiert.utils.graphs.ElementaryCyclesSearch;

public class MainActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private TextView textView;
    // does not gets updated when a TableLayout entry is modified!
    public ArrayList<List<String>> directed_graph;
    // This is updated by TableLayout add/delete, not directed_graph !
    // Should match the logical number of rows (including header)
    // directed_graph may be larger due to TableLayout deleted lines which are not deleted from directed_graph array
    // the array is resized only on in/out serialization and when rows are added
    private int node_count;
    //private Drawable border;

    private TextWatcher watcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // You can leave this empty if you're not using it
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // You can leave this empty if you're not using it
        }

        @Override
        public void afterTextChanged(Editable s) {
            // This method is called to notify you that, somewhere within `s`, the text has
            // been changed.
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                int[] ij = getRowIndex(tableLayout, view);
                if (ij == null) {
                    about(R.string.error, R.string.delete_error);
                    return;
                }
                directed_graph.get(ij[0] - 1).set(ij[1], s.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tableLayout = findViewById(R.id.tableLayout);
        textView = findViewById(R.id.textView);

        // Add initial row with headers
        tableLayout.addView(new_row(R.string.source, R.string.destination));
        //border = AppCompatResources.getDrawable(this, R.drawable.border);

        node_count = 0;

        load_shared_prefs();

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRow();
            }
        });

        Button runButton = findViewById(R.id.runButton);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                run();
            }
        });

        Button randButton = findViewById(R.id.randButton);
        randButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                randomize();
            }
        });

        Button sortButton = findViewById(R.id.sortButton);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sort();
            }
        });
        ViewTreeObserver viewTreeObserver = addButton.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Remove the listener to only get the height once
                addButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Get the height of the tallest button
                List<Button> b = Arrays.asList(addButton, runButton, randButton, sortButton);
                int maxHeight = b.get(0).getHeight();
                for (Button btn : b) {
                    int h = btn.getHeight();
                    if (h > maxHeight) {
                        maxHeight = h;
                    }
                }

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) b.get(0).getLayoutParams();

                // Update the height
                params.height = maxHeight;

                // Set the updated LayoutParams back to the LinearLayout
                for (Button btn : b) {
                    btn.setLayoutParams(params);
                }
            }
        });
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
        } else if (item.getItemId() == R.id.nav_help) {
            about(R.string.help_title, R.string.help);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Only works for String, int (resource ID) or Integer (same)
    private <T extends CharSequence> TableRow new_row(T source, T destination) {
        EditText s = new EditText(this);
        EditText d = new EditText(this);
        s.setText(source);
        d.setText(destination);
        return new_row(s, d, true);
    }
    private TableRow new_row(int source, int destination) {
        TextView s = new TextView(this);
        TextView d = new TextView(this);
        s.setText(source);
        d.setText(destination);
        return new_row(s, d, false);
    }

    private TableRow new_row(TextView s, TextView d, boolean del) {
        TableRow.LayoutParams layoutParams =
                new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableRow row = new TableRow(this);
        //s.setBackground(border);
        //d.setBackground(border);
        s.setLayoutParams(layoutParams);
        d.setLayoutParams(layoutParams);
        s.addTextChangedListener(watcher);
        d.addTextChangedListener(watcher);
        //s.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        //d.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        row.addView(s);
        row.addView(d);
        if (del) {
            ImageButton deleteButton = new ImageButton(this);
            deleteButton.setLayoutParams(layoutParams);
            deleteButton.setImageResource(android.R.drawable.ic_delete);
            deleteButton.setBackgroundColor(Color.TRANSPARENT);
            //deleteButton.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
            //deleteButton.setPadding(0, 0, 0, 0);
            //int paddingLeft = deleteButton.getPaddingLeft();
            //int paddingTop = deleteButton.getPaddingTop();
            //int paddingRight = deleteButton.getPaddingRight();
            //int paddingBottom = deleteButton.getPaddingBottom();
            //deleteButton.setBackground(border);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int[] ij = getRowIndex(tableLayout, v);
                    if (ij == null) {
                        about(R.string.error, R.string.delete_error);
                        return;
                    }
                    tableLayout.removeView(tableLayout.getChildAt(ij[0]));
                    directed_graph.remove(ij[0] - 1);
                    node_count--; // no need to update directed_graph
                    save_shared_prefs();
                }
            });
            row.addView(deleteButton);
        }
        return row;
    }
    private int[] getRowIndex(TableLayout table, View view) {
        int[] res = new int[2];
        // we start at 1 so we don't hit the heading
        for (int i = 1; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            int j = row.indexOfChild(view);
            if (j != -1) {
                res[0] = i;
                res[1] = j;
                return res;
            }
        }
        return null;  // View not found
    }
    private void addRow() {
        final TableRow row = new_row("", "");
        tableLayout.addView(row);
        node_count++;
        directed_graph.add(Arrays.asList("", ""));
        save_shared_prefs();
    }
    private void randomize() {
        Random rand = new Random();

        if (node_count == 0) {
            about(R.string.no_rows_title, R.string.no_rows);
            return;
        }
        // Subtract 1 because of header, divide by 2 as density factor,
        // add 1 to make it interesting for low number of nodes
        int nodes = Math.max(2, node_count / 2 + 1);

        int randomNum1 = rand.nextInt(nodes);
        for (int i = 0; i < node_count; i++) {
            int randomNum0 = randomNum1;
            // Make sure these two are different
            while(randomNum1 == randomNum0)
                randomNum1 = rand.nextInt(nodes);
            final List<String> l = directed_graph.get(i);
            final String s0 = String.valueOf(randomNum0);
            final String s1 = String.valueOf(randomNum1);
            l.set(0, s0);
            l.set(1, s1);
            TableRow t = (TableRow)tableLayout.getChildAt(i+1);
            ((TextView)t.getChildAt(0)).setText(s0);
            ((TextView)t.getChildAt(1)).setText(s1);
        }
        save_shared_prefs();
    }
    private void sort() {
        // Assuming tableLayout is your TableLayout

        if (node_count == 0) {
            about(R.string.no_rows_title, R.string.no_rows);
            return;
        }

        // Sort the row list based on the values in the first column
        Collections.sort(directed_graph, new Comparator<List<String>>() {
            @Override
            public int compare(List<String> list1, List<String> list2) {
                try {
                    // Try to parse first strings as integers
                    int num1 = Integer.parseInt(list1.get(0));
                    int num2 = Integer.parseInt(list2.get(0));

                    // If first strings are equal, compare second strings
                    if (num1 == num2) {
                        return list1.get(1).compareTo(list2.get(1));
                    }

                    // Compare first strings numerically
                    return Integer.compare(num1, num2);
                } catch (NumberFormatException e) {
                    // Compare first strings lexicographically
                    int result = list1.get(0).compareTo(list2.get(0));

                    // If first strings are equal, compare second strings
                    if (result == 0) {
                        return list1.get(1).compareTo(list2.get(1));
                    }

                    return result;
                }
            }
        });
        save_shared_prefs();
        update_TableLayout();
    }

    private void run() {
        if (node_count == 0) {
            about(R.string.no_rows_title, R.string.no_rows);
            return;
        }
        int num_vertices = 0;
        HashMap<String, Integer> nodes = new HashMap<String, Integer>();
        List<Object> node_names = new ArrayList<Object>();
        for (int i = 0; i < node_count; i++) { // start from 1 to skip headers
            final List<String> l = directed_graph.get(i);
            for (int j = 0; j < 2; j++) { // check both source (0) and destination (1)
                String s = l.get(j);
                if (!nodes.containsKey(s)) {
                    node_names.add(s);
                    nodes.put(s, num_vertices++);
                }
            }
        }
        boolean[][] adjMatrix = new boolean[num_vertices][num_vertices];

        for (int i = 0; i < node_count; i++) { // start from 1 to skip headers
            final List<String> l = directed_graph.get(i);
            int s = nodes.get(l.get(0));
            int d = nodes.get(l.get(1));
            adjMatrix[s][d] = true;
        }
        ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(adjMatrix, node_names);

        StringBuilder sb = new StringBuilder();
        sb.append("Here is a list of all the elementary circuits of the graph:\n");
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

        // Set the title
        builder.setTitle(String.format(getResources().getString(title), getResources().getString(R.string.app_name), BuildConfig.VERSION_NAME));

        // Create a Spanned from the HTML string
        Spanned message = Html.fromHtml(String.format(getResources().getString(body), BuildConfig.VERSION_NAME), Html.FROM_HTML_MODE_COMPACT);

        // Add an OK button
        builder.setPositiveButton("OK", null);

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Set the message
        dialog.setMessage(message);

        // Show the dialog
        dialog.show();

        // Get the TextView of the AlertDialog and make it clickable
        TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void save_shared_prefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(directed_graph);
        editor.putString("directed_graph", json);
        editor.apply();
    }

    // Assume we call this only when Activity is created, and TableLayout is empty
    private void load_shared_prefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString("directed_graph", null);
        Type type = new TypeToken<List<List<String>>>() {}.getType();
        if (json == null) {
            directed_graph = new ArrayList<>();
        } else {
            directed_graph = gson.fromJson(json, type);
            node_count = directed_graph.size();
        }
        update_TableLayout();
    }

    private void update_directed_graph() {
        directed_graph.ensureCapacity(node_count);
        for (int i = 0; i < node_count; i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i+1);
            TextView tvs = (TextView) row.getChildAt(0);
            TextView tvd = (TextView) row.getChildAt(1);
            String source = tvs.getText().toString();
            String destination = tvd.getText().toString();
            directed_graph.set(i, Arrays.asList(source, destination));
        }
    }

    private void update_TableLayout() {
        int childCount = tableLayout.getChildCount();
        // First, create new rows if needed
        if (node_count > childCount - 1) {
            for (int i = childCount;  i < node_count + 1; i++) {
                final List<String> l = directed_graph.get(i-1);
                tableLayout.addView(new_row(l.get(0), l.get(1)));
            }
        }
        // Now copy rows that already existed
        for (int i = 1; i < childCount; i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            final List<String> l = directed_graph.get(i-1);
            TextView tvs = (TextView) row.getChildAt(0);
            TextView tvd = (TextView) row.getChildAt(1);
            tvs.setText(l.get(0));
            tvd.setText(l.get(1));
        }
    }

}
