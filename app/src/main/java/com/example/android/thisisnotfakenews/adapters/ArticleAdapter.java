package com.example.android.thisisnotfakenews.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.thisisnotfakenews.R;
import com.example.android.thisisnotfakenews.model.Article;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Reggie on 14/07/2017.
 * A custom adapter for displaying a list of articles.
 */

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private static final String LOG_TAG = ArticleAdapter.class.getSimpleName();
    private List<Article> articleList;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.article_title) TextView titleView;
        @BindView(R.id.article_subtitle) TextView subtitleView;
        @BindView(R.id.article_author) TextView authorView;
        @BindView(R.id.article_date) TextView dateView;
        @BindView(R.id.read_more_button) TextView readMoreButton;
        @BindView(R.id.article_section)
        TextView sectionView;

        public ViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public ArticleAdapter(Context context, List<Article> articleList){
        this.context = context;
        this.articleList = articleList;
    }

    /**
     * Reload the bookList.
     *
     * @param articleList the articleList
     */
    public void reloadList(List<Article> articleList){
        clear();
        this.articleList.addAll(articleList);
        notifyDataSetChanged();
    }

    /**
     * Clear the bookList.
     */
    public void clear(){
        this.articleList.clear();
    }

    /**
     * Setting the views for all the elements in the RecyclerView item.
     * @param holder        the view holder
     * @param position      the current item position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        final Article article = articleList.get(position);

        String title = article.getTitle();
        String subtitle = article.getSubtitle();
        String author = article.getAuthor();
        String date = article.getDate();
        String section = article.getSectionName();

        holder.titleView.setText(title);
        holder.subtitleView.setText(subtitle);
        holder.authorView.setText(author);
        holder.dateView.setText(date);
        holder.sectionView.setText(section);

        holder.readMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getWebUrl()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount(){
        return articleList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_list_item, parent, false);
        return new ViewHolder(view);
    }
}
