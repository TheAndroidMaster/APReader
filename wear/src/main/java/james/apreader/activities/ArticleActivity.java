package james.apreader.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.wearable.view.drawer.WearableActionDrawer;
import android.support.wearable.view.drawer.WearableDrawerLayout;
import android.text.Html;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import james.apreader.R;
import james.apreader.common.Supplier;
import james.apreader.common.data.ArticleData;
import james.apreader.utils.WearMovementMethod;

public class ArticleActivity extends Activity implements WearableActionDrawer.OnMenuItemClickListener {

    public static final String EXTRA_ARTICLE = "james.apreader.EXTRA_ARTICLE";

    private WearableDrawerLayout drawerLayout;
    private ProgressBar progressBar;
    private TextView content;

    private ArticleData article;
    private Supplier supplier;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        supplier = (Supplier) getApplicationContext();
        article = getIntent().getParcelableExtra(EXTRA_ARTICLE);

        drawerLayout = (WearableDrawerLayout) findViewById(R.id.drawerLayout);
        WearableActionDrawer actionDrawer = (WearableActionDrawer) findViewById(R.id.actionDrawer);
        NestedScrollView scrollView = (NestedScrollView) findViewById(R.id.scrollView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        TextView title = (TextView) findViewById(R.id.title);
        content = (TextView) findViewById(R.id.content);
        TextView date = (TextView) findViewById(R.id.date);

        drawerLayout.peekDrawer(Gravity.BOTTOM);
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == 0 || scrollY < oldScrollY)
                    drawerLayout.peekDrawer(Gravity.BOTTOM);
                else drawerLayout.closeDrawer(Gravity.BOTTOM);
            }
        });

        title.setText(article.name);
        content.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(article.desc, 0) : Html.fromHtml(article.desc));
        content.setMovementMethod(new WearMovementMethod(this));
        date.setText(article.date);

        actionDrawer.setOnMenuItemClickListener(this);

        MenuItem favoriteItem = actionDrawer.getMenu().findItem(R.id.action_favorite);
        boolean isFavorite = supplier.isFavorite(article);
        favoriteItem.setTitle(isFavorite ? R.string.action_unfavorite : R.string.action_favorite);
        favoriteItem.setIcon(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);

        supplier.getFullContent(article, new Supplier.AsyncListener<String>() {
            @Override
            public void onTaskComplete(String value) {
                if (content != null && progressBar != null) {
                    content.setText(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(value, 0) : Html.fromHtml(value));
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure() {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_favorite:
                if (supplier.isFavorite(article)) {
                    supplier.unfavoriteArticle(article);
                    menuItem.setTitle(R.string.action_favorite);
                    menuItem.setIcon(R.drawable.ic_favorite_border);
                } else {
                    supplier.favoriteArticle(article);
                    menuItem.setTitle(R.string.action_unfavorite);
                    menuItem.setIcon(R.drawable.ic_favorite);
                }
                break;
            case R.id.action_phone:
                Intent intent = new Intent(this, WearSenderActivity.class);
                intent.putExtra(WearSenderActivity.EXTRA_MESSAGE, new Gson().toJson(article));
                startActivity(intent);
                break;
        }
        return false;
    }
}
