package com.camera360.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by zhouwei on 14-10-9.
 */
public class SettingActivity extends Activity {
    private List<String> data = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getData();
        ListView lv = (ListView) findViewById(R.id.listview);
        MyAdapter myAdapter = new MyAdapter(this);
        lv.setAdapter(myAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ratio = data.get(position);
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("result",ratio);
                intent.putExtras(bundle);
                setResult(1,intent);
                SettingActivity.this.finish();
            }
        });
    }

    /**
     * 获取传递的数据
     */
    public void getData(){
        data = getIntent().getExtras().getStringArrayList("ratio");
    }
    class MyAdapter extends BaseAdapter{
       private Context context;
      public MyAdapter(Context c){
         this.context = c;
      }
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null){
                holder = new ViewHolder();
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_item,null);
                holder.tv = (TextView) convertView.findViewById(R.id.ratio_name);
                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();
            holder.tv.setText(data.get(position));
            return convertView;
        }

        class ViewHolder{
            TextView tv;
        }
    }
}
