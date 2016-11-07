package br.nom.pedrollo.emilio.mathpp;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import br.nom.pedrollo.emilio.mathpp.adapters.CategoriesAdapter;
import br.nom.pedrollo.emilio.mathpp.entities.Category;
import br.nom.pedrollo.emilio.mathpp.fragments.QuestionsListFragment;
import br.nom.pedrollo.emilio.mathpp.utils.NetworkUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

    Boolean alreadyLoadedCategoriesOnce = false;

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

        populateMenuInfo(navigationView);
        populateBuildString();

        GridView categoriesGrid = (GridView) findViewById(R.id.categories_grid);
        categoriesGrid.setOnItemClickListener(this);

        refreshCategoriesGrid();

        handleIntent(getIntent());

    }

    @Override
    protected void onResume() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        populateMenuInfo(navigationView);
        super.onResume();
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

        QuestionsListFragment questionsListFragment = new QuestionsListFragment();
        GridView categoriesGrid = (GridView) findViewById(R.id.categories_grid);
        CategoriesAdapter categoriesAdapter = (CategoriesAdapter)categoriesGrid.getAdapter();

        Bundle bundle = new Bundle();

        bundle.putInt(QuestionsListFragment.CATEGORY,categoriesAdapter.categories.get(position).getId());

        questionsListFragment.setArguments(bundle);
        transaction.add(R.id.frame_container, questionsListFragment);
        transaction.addToBackStack(null).commit();


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager.getBackStackEntryCount() == 1){
            menu.findItem(R.id.action_questions_search).setVisible(true);
            menu.findItem(R.id.action_refresh_categories).setVisible(false);
            menu.findItem(R.id.action_add_question).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private class getCategoriesTask extends AsyncTask<Void,Void,String> {

        @SuppressWarnings("ThrowFromFinallyBlock")
        @Override
        protected String doInBackground(Void... params) {
            HashMap<String, String> getParams = new HashMap<>();
            //getParams.put("limit","10");

            NetworkUtils.readTimeout = NetworkUtils.MEDIUM_TIMEOUT;
            NetworkUtils.connectionTimeout = NetworkUtils.MEDIUM_TIMEOUT;

            return NetworkUtils.getFromServer(getApplicationContext(),
                    getResources().getString(R.string.categories_fetch_uri), NetworkUtils.Method.GET, getParams);
        }
    }

    private void refreshCategoriesGrid(){


        final RelativeLayout categoriesPlaceholder =         (RelativeLayout) findViewById(R.id.categories_placeholder);
        final ProgressBar categoriesPlaceholderProgressbar = (ProgressBar) findViewById(R.id.categories_placeholder_progressbar);
        final TextView categoriesPlaceholderTextView =       (TextView)findViewById(R.id.categories_placeholder_text_view);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();


        if (networkInfo != null && networkInfo.isConnected()) {
            if (!alreadyLoadedCategoriesOnce) categoriesPlaceholder.setVisibility(View.VISIBLE);
            categoriesPlaceholderProgressbar.setVisibility(View.VISIBLE);
            categoriesPlaceholderTextView.setText(getResources().getString(R.string.loading_categories));
            new getCategoriesTask(){

                @Override
                public void onPostExecute(String serverResponse) {
                    super.onPostExecute(serverResponse);

                    final CategoriesAdapter categoriesAdapter;
                    final GridView categoriesGrid = (GridView) findViewById(R.id.categories_grid);

                    if (categoriesGrid.getAdapter() == null){
                        categoriesAdapter = new CategoriesAdapter(getApplicationContext());
                        categoriesGrid.setAdapter(categoriesAdapter);
                    } else {
                        categoriesAdapter = (CategoriesAdapter) categoriesGrid.getAdapter();
                    }

                    NetworkUtils.getJSONObjectsFromServerResponse(serverResponse, new NetworkUtils.OnGetJSONFromServerResponseEvents() {
                        @Override
                        public void onJsonObjectFound(JSONObject jsonObject) {
                            try{

                                Boolean foundExisting = false;
                                Integer id = jsonObject.getInt("id");
                                String name = jsonObject.getString("name");
                                String img = jsonObject.getString("image");
                                Category category = new Category(id,name,img);

                                for (int i=0; i < categoriesAdapter.categories.size(); i++){
                                    if (categoriesAdapter.categories.get(i).getId() ==  category.getId() ){
                                        foundExisting = true;
                                        categoriesAdapter.categories.set(i,category);
                                        break;
                                    }
                                }
                                if (!foundExisting){
                                    categoriesAdapter.categories.add(category);
                                }

                            } catch (JSONException e){
                                Log.e("PARSE_JSON",e.getLocalizedMessage());
                            }
                        }

                        @Override
                        public void onFinish(Boolean success) {
                            if (success){
                                if (categoriesAdapter.categories.size() > 0){
                                    alreadyLoadedCategoriesOnce = true;
                                    categoriesPlaceholder.setVisibility(View.GONE);
                                    categoriesGrid.setVisibility(View.VISIBLE);
                                } else {
                                    categoriesGrid.setVisibility(View.GONE);
                                    categoriesPlaceholder.setVisibility(View.VISIBLE);
                                    categoriesPlaceholderProgressbar.setVisibility(View.GONE);
                                    categoriesPlaceholderTextView.setText(getResources().getString(R.string.no_categories_available));
                                }
                            } else {
                                categoriesGrid.setVisibility(View.GONE);

                                categoriesPlaceholder.setVisibility(View.VISIBLE);
                                categoriesPlaceholderProgressbar.setVisibility(View.GONE);
                                categoriesPlaceholderTextView.setText(getResources().getString(R.string.unable_retrieve_categories));
                            }
                        }
                    });
                }
            }.execute();

        } else {
            if (/* There is local cache? */false) {
                //Todo: Store local cache from categories
            } else {
                categoriesPlaceholderTextView.setText(getResources().getString(R.string.no_internet_connection));

                categoriesPlaceholder.setVisibility(View.VISIBLE);
                categoriesPlaceholderProgressbar.setVisibility(View.GONE);
            }

        }

    }

    private void populateMenuInfo(NavigationView navigationView){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        View headerLayout = navigationView.getHeaderView(0);

        TextView displayNameTextView = (TextView) headerLayout.findViewById(R.id.display_name);
        TextView userEmailTextView = (TextView) headerLayout.findViewById(R.id.user_email);


        displayNameTextView.setText( prefs.getString("display_name","Anonymous") );
        userEmailTextView.setText( prefs.getString("user_email","") );
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
        Intent intent;
        int id = item.getItemId();

        switch (id){
//            case R.id.action_settings:
//                intent = new Intent(getApplicationContext(),SettingsActivity.class);
//                ActivityCompat.startActivity(this,intent,null);
//                return true;
            case R.id.action_refresh_categories:
                refreshCategoriesGrid();
                return true;
//            case R.id.action_add_question:
//                intent = new Intent(getApplicationContext(),WritePostActivity.class);
//                ActivityCompat.startActivity(this,intent,null);
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        try {

            switch (id){
                case R.id.nav_categories:
                    return true;
                case R.id.nav_my_messages:
                case R.id.nav_favorites:
                    Toast.makeText(this, getResources().getString(R.string.not_available_yet), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_settings:
                    Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                    ActivityCompat.startActivity(this,intent,null);
                    return true;
                case R.id.nav_report:
                    String url = "https://github.com/emiliopedrollo/MathPP/issues/new";
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setToolbarColor(ContextCompat.getColor(this,R.color.colorPrimary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(this, Uri.parse(url));
                    return true;
                case R.id.nav_info:
                    Toast.makeText(this, getResources().getString(R.string.not_available_yet), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_exit:
                    this.finishAffinity();
                    return true;
            }
            return false;
        } finally {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
}
