/*
 * Kiwix Android
 * Copyright (c) 2019 Kiwix <android.kiwix.org>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.kiwix.kiwixmobile.core.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import org.kiwix.kiwixmobile.core.R;
import org.kiwix.kiwixmobile.core.extensions.ContextExtensionsKt;
import org.kiwix.kiwixmobile.core.extensions.ImageViewExtensionsKt;

import static org.kiwix.kiwixmobile.core.utils.DimenUtils.getToolbarHeight;
import static org.kiwix.kiwixmobile.core.utils.DimenUtils.getWindowHeight;
import static org.kiwix.kiwixmobile.core.utils.DimenUtils.getWindowWidth;
import static org.kiwix.kiwixmobile.core.utils.ImageUtils.getBitmapFromView;
import static org.kiwix.kiwixmobile.core.utils.StyleUtils.fromHtml;

public class TabsAdapter extends RecyclerView.Adapter<TabsAdapter.ViewHolder> {
  private final List<KiwixWebView> webViews;
  private final AppCompatActivity activity;
  private final NightModeViewPainter painter;
  private TabClickListener listener;
  private int selectedPosition = 0;

  TabsAdapter(AppCompatActivity activity, List<KiwixWebView> webViews,
    NightModeViewPainter painter) {
    this.webViews = webViews;
    this.activity = activity;
    this.painter = painter;
    setHasStableIds(true);
  }

  @SuppressLint("ResourceType")
  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    final int margin16 =
      context.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);

    ImageView contentImage = new ImageView(context);
    contentImage.setId(1);
    contentImage.setScaleType(ImageView.ScaleType.FIT_XY);

    ImageView close = new ImageView(context);
    close.setId(2);
    close.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_clear_white_24dp));
    ImageViewExtensionsKt.tint(close,
      ContextExtensionsKt.getAttribute(context, R.attr.colorOnSurface));

    MaterialCardView cardView = new MaterialCardView(context);
    cardView.setId(3);
    cardView.setUseCompatPadding(true);
    cardView.addView(contentImage,
      new MaterialCardView.LayoutParams(MaterialCardView.LayoutParams.MATCH_PARENT,
        MaterialCardView.LayoutParams.MATCH_PARENT));

    ConstraintLayout constraintLayout = new ConstraintLayout(context);
    constraintLayout.setFocusableInTouchMode(true);

    constraintLayout.addView(cardView,
      new ConstraintLayout.LayoutParams(getWindowWidth(activity) / 2,
        -getToolbarHeight(activity) / 2 + getWindowHeight(activity) / 2));
    constraintLayout.addView(close, new ConstraintLayout.LayoutParams(margin16,
      margin16));
    constraintLayout.setLayoutParams(
      new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
        RecyclerView.LayoutParams.MATCH_PARENT));

    TextView textView = new TextView(context);
    textView.setId(4);
    textView.setMaxLines(1);
    textView.setEllipsize(TextUtils.TruncateAt.END);
    constraintLayout.addView(textView,
      new ConstraintLayout.LayoutParams(0, ConstraintLayout.LayoutParams.WRAP_CONTENT));

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(constraintLayout);

    constraintSet.connect(cardView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID,
      ConstraintSet.TOP);
    constraintSet.connect(cardView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID,
      ConstraintSet.BOTTOM);
    constraintSet.connect(cardView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID,
      ConstraintSet.START, margin16);
    constraintSet.connect(cardView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID,
      ConstraintSet.END, margin16);

    constraintSet.connect(close.getId(), ConstraintSet.END, cardView.getId(), ConstraintSet.END);
    constraintSet.connect(close.getId(), ConstraintSet.BOTTOM, cardView.getId(), ConstraintSet.TOP);

    constraintSet.connect(textView.getId(), ConstraintSet.BOTTOM, cardView.getId(),
      ConstraintSet.TOP);
    constraintSet.connect(textView.getId(), ConstraintSet.START, cardView.getId(),
      ConstraintSet.START, margin16 / 8);
    constraintSet.connect(textView.getId(), ConstraintSet.END, close.getId(), ConstraintSet.START);

    constraintSet.applyTo(constraintLayout);
    return new ViewHolder(constraintLayout, contentImage, textView, close);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    KiwixWebView webView = webViews.get(position);
    if (webView.getParent() != null) {
      ((ViewGroup) webView.getParent()).removeView(webView);
    }
    String webViewTitle = fromHtml(webView.getTitle()).toString();
    holder.title.setText(webViewTitle);
    holder.close.setOnClickListener(v -> listener.onCloseTab(v, holder.getAdapterPosition()));
    holder.content.setImageBitmap(
      getBitmapFromView(webView, getWindowWidth(activity), getWindowHeight(activity))
    );
    holder.content.setOnClickListener(v -> {
      selectedPosition = holder.getAdapterPosition();
      listener.onSelectTab(v, selectedPosition);
      notifyDataSetChanged();
    });
    if (!webViewTitle.equals(activity.getString(R.string.menu_home))) {
      painter.update(holder.content);
    }
  }

  @Override
  public int getItemCount() {
    return webViews.size();
  }

  @Override public long getItemId(int position) {
    return webViews.get(position).hashCode();
  }

  int getSelected() {
    return selectedPosition;
  }

  void setSelected(int position) {
    this.selectedPosition = position;
  }

  void setTabClickListener(TabClickListener listener) {
    this.listener = listener;
  }

  public interface TabClickListener {
    void onSelectTab(@NonNull View view, int position);

    void onCloseTab(@NonNull View view, int position);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    final ImageView content;
    final TextView title;
    final ImageView close;

    ViewHolder(View v, ImageView content, TextView title, ImageView close) {
      super(v);
      this.content = content;
      this.title = title;
      this.close = close;
    }
  }
}
