package com.example.liu.paperex.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.liu.paperex.R;
import com.example.liu.paperex.adapter.CommonAdapter;
import com.example.liu.paperex.adapter.ViewHolder;
import com.example.liu.paperex.bean.HotBean;
import com.example.liu.paperex.bean.MostNewBean;
import com.example.liu.paperex.task.MyTextTask;
import com.example.liu.paperex.ui.BigPicActivity;
import com.example.liu.paperex.url.URL;
import com.example.liu.paperex.utils.ImageCacheUtils;
import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liu on 16/8/8.
 * 推荐中热门部分的fragment
 */
public class HotFragment extends Fragment {
    PullToRefreshGridView mPullGv;
    private Handler handler = new Handler();
    private ArrayList<HotBean.DataBean.WallpaperListInfoBean> list = new ArrayList<>();
    private CommonAdapter adapter;
    private boolean isSort;
    private String sortUrl;
    private int totalCount;
    private String[] strs;

    public HotFragment() {
    }

    public HotFragment(boolean isSort, String sortUrl) {
        this.isSort = isSort;
        this.sortUrl = sortUrl;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hot, container, false);
        mPullGv = (PullToRefreshGridView) view.findViewById(R.id.gl_hot);

        if (isSort) {
            initData(sortUrl);
            initAdapter();
        } else {
            initData(URL.HOT_URL);
            initAdapter();
        }

        enableLoadMore();
        setListener();
        setPullToRefreshStyle();

        mPullGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getActivity().getApplicationContext(), BigPicActivity.class);
                strs = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    strs[i] = list.get(i).getWallPaperBig();
                }
                intent.putExtra("strs", strs);
                intent.putExtra("position", position);
                startActivity(intent);

            }
        });

        return view;
    }


    private void setPullToRefreshStyle() {
        ILoadingLayout il = mPullGv.getLoadingLayoutProxy();
        //设置右侧显示的提示图片
        il.setLoadingDrawable(getResources().getDrawable(R.mipmap.umeng_xp_loading));
        il.setTextTypeface(Typeface.SANS_SERIF);
        //设置下拉状态时的提示文字
        il.setPullLabel("继续拖动刷新");
        //设置正在刷新过程中的提示文字
        il.setRefreshingLabel("正在刷新");
        //设置松手提示文字
        il.setReleaseLabel("松手刷新");
    }

    private void setListener() {
        mPullGv.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int page = totalCount / 30;
                        if (page == 0) {
                            Toast.makeText(getActivity(), "没有更多图片了", Toast.LENGTH_SHORT).show();
                        } else {
                            initData(URL.subHotURL(sortUrl, page));
                        }
                        mPullGv.onRefreshComplete();
                    }
                }, 2000);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int page = totalCount / 30;
                        if (page == 0) {
                            Toast.makeText(getActivity(), "没有更多图片了", Toast.LENGTH_SHORT).show();
                        } else {
                            initData(URL.subHotURL(sortUrl, page));
                        }
                        mPullGv.onRefreshComplete();
                    }
                }, 2000);
            }
        });
    }

    private void enableLoadMore() {
        mPullGv.setMode(PullToRefreshBase.Mode.BOTH);
    }

    private void initAdapter() {
        adapter = new CommonAdapter(getActivity().getApplicationContext(), list, R.layout.hot_item, new CommonAdapter.SetDataShow<HotBean.DataBean.WallpaperListInfoBean>() {

            @Override
            public void setData(ViewHolder holder, final HotBean.DataBean.WallpaperListInfoBean info, View convertView) {
                ImageView iv = holder.getKongJian(convertView, R.id.hot_imageView);
                //使用ImageCacheUtils
                ImageCacheUtils utils = ImageCacheUtils.getInstance();
                //从缓存获取图片
                Bitmap bitmap = utils.getBitmapFromCache(info.getWallPaperMiddle(), false);

                if (bitmap != null) {
                    //让imageview显示图片
                    iv.setImageBitmap(bitmap);
                } else {
                    /**
                     * 联网下载
                     * String url, 下载地址
                     * final TextView tv, 需要显示到的控件
                     * boolean isRound,是否圆角
                     * final boolean isToLocal,是否存到本地
                     */
                    utils.loadImage(info.getWallPaperMiddle(), iv, false, false);
                }
            }
        });
        mPullGv.setAdapter(adapter);
    }

    private void initData(String url) {
        new MyTextTask<>(new MyTextTask.TextDataCallback<HotBean>() {
            @Override
            public void getTextData(HotBean bean) {
                totalCount = bean.getData().getTotalCount();
                List<HotBean.DataBean.WallpaperListInfoBean> mList = bean.getData().getWallpaperListInfo();
                list.addAll(mList);
                adapter.notifyDataSetChanged();
            }
        }, HotBean.class).execute(url);
    }

}
