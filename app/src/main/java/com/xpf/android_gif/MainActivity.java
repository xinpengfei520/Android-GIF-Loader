package com.xpf.android_gif;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private ImageView ivGif;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            ivGif.setVisibility(View.GONE);
        }
    };

    private RequestListener listener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            setLoadOnce(resource);
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        ivGif = (ImageView) findViewById(R.id.ivGif);
        TextView tvLoadGif = (TextView) findViewById(R.id.tvLoadGif);
        tvLoadGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadGifOnce();
            }
        });
    }

    private void loadGifOnce() {
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
        Glide.with(MainActivity.this)
                .load(R.drawable.gif1)
                .apply(options)
                .listener(listener) // 设置监听
                .into(ivGif);
    }

    private void setLoadOnce(Drawable resource) {
        // Glide 4.0 后没法再直接获取GifDecoder对象了,原因是因为GlideDrawable不再提供这个方法了。
        // 我这里是采用反射的方法获取到GifDecoder变量的,具体代码如下
        // 参考链接：https://www.aliyun.com/jiaocheng/1344.html
        try {
            Field gifStateField = GifDrawable.class.getDeclaredField("state");
            gifStateField.setAccessible(true);
            Class gifStateClass = Class.forName("com.bumptech.glide.load.resource.gif.GifDrawable$GifState");
            Field gifFrameLoaderField = gifStateClass.getDeclaredField("frameLoader");
            gifFrameLoaderField.setAccessible(true);
            Class gifFrameLoaderClass = Class.forName("com.bumptech.glide.load.resource.gif.GifFrameLoader");
            Field gifDecoderField = gifFrameLoaderClass.getDeclaredField("gifDecoder");
            gifDecoderField.setAccessible(true);
            Class gifDecoderClass = Class.forName("com.bumptech.glide.gifdecoder.GifDecoder");
            Object gifDecoder = gifDecoderField.get(gifFrameLoaderField.get(gifStateField.get(resource)));
            Method getDelayMethod = gifDecoderClass.getDeclaredMethod("getDelay", int.class);
            getDelayMethod.setAccessible(true);
            GifDrawable drawable = (GifDrawable) resource;
            // 设置只播放一次
            drawable.setLoopCount(1);
            // 获得总帧数
            int count = drawable.getFrameCount();
            int delay = 0;
            for (int i = 0; i < count; i++) {
                // 计算每一帧所需要的时间进行累加
                delay += (int) getDelayMethod.invoke(gifDecoder, i);
            }
            handler.sendEmptyMessageDelayed(1, delay);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
