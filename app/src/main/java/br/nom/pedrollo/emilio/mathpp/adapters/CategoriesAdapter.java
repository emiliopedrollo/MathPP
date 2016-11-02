package br.nom.pedrollo.emilio.mathpp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.nom.pedrollo.emilio.mathpp.R;
import br.nom.pedrollo.emilio.mathpp.entities.Category;

public class CategoriesAdapter extends BaseAdapter {

    public List<Category> categories;
    private Context context;

    public CategoriesAdapter(Context context){
        this.categories = new ArrayList<>();
        this.context = context;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view;

        if (convertView == null){
            view = inflater.inflate(R.layout.categories_grid_item,null);
        } else {
            view = convertView;
        }

        final ImageView imageView = (ImageView) view.findViewById(R.id.category_grid_item_image);
        TextView textView = (TextView) view.findViewById(R.id.category_grid_item_name);


        imageView.setContentDescription(categories.get(position).getName());
        textView.setText(categories.get(position).getName());
        Picasso.with(context).load(categories.get(position).getImgSrc()).into(imageView);

        return view;
    }


    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
