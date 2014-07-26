package com.xanadu.queuer;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardThumbnail;

/**
 * Created by trogdan on 7/6/14.
 */
public class QRCodeCard  extends Card {

    public QRCodeCard(Context context) {
        this(context, R.layout.frontcard_inner_content);
    }

    public QRCodeCard(Context context, int innerLayout) {
        super(context, innerLayout);
        //init();
    }

    protected TextView mTitle;
    protected TextView mSecondaryTitle;
    protected Uri mBitmapUri;

    protected String title;
    protected String secondaryTitle;

    protected CardThumbnail mCardThumbnail;

    public void finalize() {

        //Add thumbnail
        mCardThumbnail = new CardThumbnail(mContext);

        mCardThumbnail.setUrlResource(mBitmapUri.toString());

        addCardThumbnail(mCardThumbnail);

        //Add ClickListener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getContext(), "Click Listener card=" + title, Toast.LENGTH_SHORT).show();
            }
        });

        //Make it swipey
        setSwipeable(true);
        setOnSwipeListener(new OnSwipeListener() {
            @Override
            public void onSwipe(Card card) {
                Toast.makeText(getContext(), "Removed card=" + title, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        //Retrieve elements
        mTitle = (TextView) parent.findViewById(R.id.frontcard_main_inner_title);
        mSecondaryTitle = (TextView) parent.findViewById(R.id.frontcard_main_inner_secondaryTitle);

        if (mTitle != null)
            mTitle.setText(title);

        if (mSecondaryTitle != null)
            mSecondaryTitle.setText(secondaryTitle);

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSecondaryTitle() {
        return secondaryTitle;
    }

    public void setSecondaryTitle(String secondaryTitle) {
        this.secondaryTitle = secondaryTitle;
    }

    public Uri getBitmapUri() {
        return mBitmapUri;
    }

    public void setBitmapUri(Uri bitmapUri) {
        this.mBitmapUri = bitmapUri;
    }

    public void setBitmapUriFromFile(File bitmapFile) {
        this.mBitmapUri = Uri.fromFile(bitmapFile);
    }

}

