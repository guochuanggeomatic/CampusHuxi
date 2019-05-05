package com.wuzhexiaolu.campusui.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v7.widget.SearchView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wuzhexiaolu.campusui.R;
import com.wuzhexiaolu.campusui.geocomponent.LandmarkComponent;

/**
 * 应该把 HuxiActivity 中那一段复杂的初始化封装到这儿。
 * 这个类掌控着搜索。应该封装得更多。
 *
 * 这个类和地标组件的搜索功能响应。
 */
public class SearchDialog extends Dialog {

    private ListView listView ;
    private SearchView searchView;
    private Context context;

    public SearchDialog(Context context, View layout, int style) {
        super(context, style);
        setContentView(layout);
        this.context = context;
        listView = findViewById(R.id.list_view);
        searchView = findViewById(R.id.search_view);

        Window window = getWindow();
        assert window != null;
        WindowManager.LayoutParams params = window.getAttributes();
        params.format = PixelFormat.TRANSLUCENT;
        params.alpha = 0.75F;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);

        setCancelable(true);
    }

    /**
     * 接受一个Landmark，然后利用它的内容来填充ListView。点击对应Item的时候，
     * 地标组件能够响应。
     */
    public void stuffWithLandmark(LandmarkComponent landmarkComponent) {
        //对布局内的控件进行设置
//        final ArrayAdapter arrayAdapter = new ArrayAdapter<String>(context,
//                android.R.layout.simple_dropdown_item_1line, landmarkComponent.getLandmarkNames());
        final ArrayAdapter arrayAdapter = new ArrayAdapter<String>(context,
            R.layout.menu_item, landmarkComponent.getLandmarkNames()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                String hint = getItem(position);
                @SuppressLint("ViewHolder")
                View view = LayoutInflater.from(getContext()).inflate(R.layout.menu_item, parent, false);
                TextView menuItem = view.findViewById(R.id.menu_item_text_view);
                menuItem.setText(hint);
                return view;
            }
        };
        listView.setAdapter(arrayAdapter);
        //ListView启动过滤
        listView.setTextFilterEnabled(true);
        //一开始不显示
        listView.setVisibility(View.VISIBLE);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String itemName = (String) arrayAdapter.getItem(position);
            // 不使用传入下标是因为这儿地标名字的下标发生了改变，只能传入名字。
            landmarkComponent.flyToSpecifiedLand(itemName);
            hide();
        });
        //显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //单击搜索按钮的监听
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            //输入字符的监听
            @Override
            public boolean onQueryTextChange(String newText) {
                arrayAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }
}