package br.nom.pedrollo.emilio.mathpp;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import br.nom.pedrollo.emilio.mathpp.adapters.CategoriesAdapter;
import br.nom.pedrollo.emilio.mathpp.entities.Category;
import br.nom.pedrollo.emilio.mathpp.fragments.QuestionsListFragment;
import br.nom.pedrollo.emilio.mathpp.utils.NetworkUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.hide();

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.activity_main_coordinator_layout);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                coordinatorLayout.setTranslationX(slideOffset * drawerView.getWidth());
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
            }
        };
        drawer.addDrawerListener(toggle);
        drawer.setScrimColor(Color.TRANSPARENT);
        drawer.setDrawerElevation(10);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        populateBuildString();

        GridView categoriesGrid = (GridView) findViewById(R.id.categories_grid);
        categoriesGrid.setOnItemClickListener(this);

        refreshCategoriesGrid();

        handleIntent(getIntent());

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public View getToolbarView(){
        return findViewById(R.id.toolbar);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public View getStatusBarBackground(){
        return findViewById(android.R.id.statusBarBackground);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public View getNavigationBarBackground(){
        return findViewById(android.R.id.navigationBarBackground);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        FragmentManager fragmentManager = getFragmentManager ();

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.setCustomAnimations(R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit,
                R.animator.fragment_slide_right_enter, R.animator.fragment_slide_right_exit);

        transaction.add(R.id.frame_container, new QuestionsListFragment());
        transaction.addToBackStack(null).commit();


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager.getBackStackEntryCount() == 1){
            menu.findItem(R.id.action_questions_search).setVisible(true);
            menu.findItem(R.id.action_refresh_categories).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void refreshCategoriesGrid(){

        final TextView categoriesPlaceholder = (TextView)findViewById(R.id.categories_placeholder_text_view);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();


        if (networkInfo != null && networkInfo.isConnected()) {
            categoriesPlaceholder.setText(getResources().getString(R.string.loading_categories));
            new getCategoriesTask(){

                @Override
                public void onPostExecute(String jsonString) {
                    super.onPostExecute(jsonString);

                    GridView categoriesGrid = (GridView) findViewById(R.id.categories_grid);
                    CategoriesAdapter categoriesAdapter = (CategoriesAdapter) categoriesGrid.getAdapter();

                    if (categoriesGrid.getAdapter() == null){
                        categoriesAdapter = new CategoriesAdapter(getApplicationContext());
                        categoriesGrid.setAdapter(categoriesAdapter);
                    }

                    try{
                        JSONArray jsonArray = new JSONArray(jsonString);

                        for (int i=0; i < jsonArray.length(); i++){
                            JSONObject jsonCategory = jsonArray.getJSONObject(i);

                            String name = jsonCategory.getString("name");
                            String img = jsonCategory.getString("image");

                            Category category = new Category(name,img);

                            if (categoriesAdapter.categories.contains(category)){
                                //// FIXME: 26/09/2016 Não entra pois é sempre um novo objeto
                                categoriesAdapter.categories.remove(category);
                                categoriesAdapter.categories.add(category);
                            } else {
                                categoriesAdapter.categories.add(category);
                            }

                        }

                        categoriesAdapter.notifyDataSetInvalidated();

                    } catch (Exception e){
                        Log.e("PARSE_JSON",e.getLocalizedMessage());
                    }

                    categoriesGrid.setVisibility(View.VISIBLE);
                    categoriesPlaceholder.setVisibility(View.GONE);
                }
            }.execute();

        } else {
            if (/* There is local cache? */false) {
                //Todo: Store local cache from categories
            } else {
                categoriesPlaceholder.setText(getResources().getString(R.string.no_internet_connection));
            }

        }

    }

    private class getCategoriesTask extends AsyncTask<Void,Void,String> {

        @SuppressWarnings("ThrowFromFinallyBlock")
        @Override
        protected String doInBackground(Void... params) {
            HashMap<String, String> getParams = new HashMap<>();
            getParams.put("limit","10");

            NetworkUtils.readTimeout = NetworkUtils.MEDIUM_TIMEOUT;
            NetworkUtils.connectionTimeout = NetworkUtils.MEDIUM_TIMEOUT;

            return NetworkUtils.getFromServer(getApplicationContext(),
                    getResources().getString(R.string.categories_fetch_uri), NetworkUtils.Method.GET, getParams);
        }
    }

    private void populateBuildString(){
        try{
            TextView buildString = (TextView) findViewById(R.id.build_string);
            buildString.setText( String.format(getResources().getString(R.string.build_version),
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName) );
        } catch (PackageManager.NameNotFoundException exception){
            Log.e("BUILD_STRING",exception.getLocalizedMessage(),exception);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_questions_search));
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
            ActivityCompat.startActivity(this,intent,null);
            return true;
        } else if (id == R.id.action_refresh_categories) {
            refreshCategoriesGrid();
        } else if (id == R.id.action_add_question) {
            Intent intent = new Intent(getApplicationContext(),WritePostActivity.class);
            ActivityCompat.startActivity(this,intent,null);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    @NonNull
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_categories) {
            // Handle the camera action
        } else if (id == R.id.nav_teachers) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
            ActivityCompat.startActivity(this,intent,null);
            return true;
        } else if (id == R.id.nav_info) {

        } else if (id == R.id.nav_exit) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
