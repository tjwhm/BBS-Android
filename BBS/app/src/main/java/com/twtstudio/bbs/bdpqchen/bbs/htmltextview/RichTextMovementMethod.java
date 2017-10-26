package com.twtstudio.bbs.bdpqchen.bbs.htmltextview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import com.twtstudio.bbs.bdpqchen.bbs.commons.rx.RxDoHttpClient;
import com.twtstudio.bbs.bdpqchen.bbs.commons.utils.CastUtil;
import com.twtstudio.bbs.bdpqchen.bbs.commons.utils.IntentUtil;
import com.twtstudio.bbs.bdpqchen.bbs.commons.utils.LogUtil;

import static com.twtstudio.bbs.bdpqchen.bbs.commons.support.Constants.FORUM;
import static com.twtstudio.bbs.bdpqchen.bbs.commons.support.Constants.THREAD;
import static com.twtstudio.bbs.bdpqchen.bbs.commons.support.Constants.USER;

/**
 * Created by retrox on 16/08/2017.
 * 用于处理图片点击 链接点击 和 自由复制
 * {@link LocalLinkMovementMethod } <-- 这个货是处理图片 链接点击的（不过现在不用了）
 * {@link ArrowKeyMovementMethod} <-- 从 {@link TextView }setTextIsSelectable方法里面看到的 然后继承 拓展功能
 */

public class RichTextMovementMethod extends ArrowKeyMovementMethod {

    private static RichTextMovementMethod sInstance;

    public static MovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new RichTextMovementMethod();
        }

        return sInstance;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        final Context context = widget.getContext();
        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
            ImageSpan[] imageSpans = buffer.getSpans(off, off, ImageSpan.class);

            if (isLink(link)) {
                if (isActionUp(action)) {
                    ClickableSpan span = link[0];
                    if (span instanceof URLSpan) {
                        Uri uri = Uri.parse(((URLSpan) span).getURL());
                        LogUtil.dd("uri", uri.toString());
                        if (isInteriorThreadLink(uri)) {
                            int threadId = CastUtil.parse2intWithMin(uri.getLastPathSegment());
                            Intent intent = IntentUtil.toThread(context, threadId);
                            context.startActivity(intent);
                        } else if (isAtUserLink(uri)) {
                            context.startActivity(IntentUtil.toPeople(context, getAtUid(uri.toString())));
                        } else {
                            // 调用系统默认链接点击事件
                            if (isOuterLink(uri))
                                link[0].onClick(widget);
                        }
                    }
                }
                return true;
            } else if (isImage(imageSpans)) {
                if (isActionUp(action)) {
                    context.startActivity(IntentUtil.toBigPhoto(context, imageSpans[0].getSource()));
                }
                return true;
            }
            return super.onTouchEvent(widget, buffer, event);
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    private boolean isOuterLink(Uri uri) {
        String link = uri.toString();
        if (link.length() > 2 && uri.getHost() != null) {
            return true;
        }
        return false;
    }

    private String getUserLinkKey() {
        return "/" + USER + "/";
    }

    private boolean isAtUserLink(Uri uri) {
        final String str = uri.toString();
        final String key = getUserLinkKey();
        return str.contains(key) && CastUtil.isNumeric(str.substring(str.indexOf(key) + key.length(), str.length()));
    }

    private int getAtUid(String str) {
        final String key = getUserLinkKey();
        int uid = 0;
        String uidStr = str.substring(str.indexOf(key) + key.length(), str.length());
        if (CastUtil.isNumeric(uidStr)) {
            uid = CastUtil.parse2int(uidStr);
        }
        return uid;
    }

    private boolean isLink(ClickableSpan[] link) {
        return link.length != 0;
    }

    private boolean isImage(ImageSpan[] imageSpans) {
        return imageSpans.length != 0;
    }

    private boolean isActionUp(int action) {
        return action == MotionEvent.ACTION_UP;
    }

    private boolean isInteriorLink(Uri uri) {
        if (uri.getHost() == null) return false;
        return uri.getHost().equals(RxDoHttpClient.BASE_HOST);
    }

    private boolean isThread(String url) {
        return url.contains("/" + FORUM + "/" + THREAD + "/");
    }

    private boolean isInteriorThreadLink(Uri uri) {
        if (uri == null) return false;
        return isInteriorLink(uri) && isThread(uri.toString());
    }
}