package com.xanadu.queuer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.view.CardListView;
import it.gmariotti.cardslib.library.view.CardView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CardListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CardListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CardListFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private Context mAttachedContext;

    ArrayList<Card> mCards;
    CardListView mCardListView;
    CardArrayAdapter mCardArrayAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CardListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CardListFragment newInstance() {
        CardListFragment fragment = new CardListFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public CardListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }

        initCards();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_card_list, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mAttachedContext = activity.getApplicationContext();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mAttachedContext = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void initCards() {

        //Init an array of Cards
        mCards = new ArrayList<Card>();



        startScan();
    }

    /*
     * Skipping most code and I will only show you the most essential.
     */

    private void startScan() {
        ScanDirectoryTask scanTask = new ScanDirectoryTask(new ScanDirectoryCallback() {

            @Override
            public void onTaskDone(ArrayList<File> results) {
                loadCards(results);
            }
        });

        //TODO real directories
        //String picturesDirectory = Environment.
        //        getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).
        //        getAbsolutePath();

        scanTask.execute("/sdcard/BarcodeScanner/Barcodes");
    }


    //queues a directory scan
    private void loadCards(ArrayList<File> thumbnailList)
    {
        //TODO check context, or assert attached

        for(File f: thumbnailList) {

            //Create a Card
            QRCodeCard card = new QRCodeCard(mAttachedContext);

            //Create a CardHeader
            CardHeader header = new CardHeader(mAttachedContext);
            header.setTitle(f.getName());

            //Add Header to card
            card.addCardHeader(header);

            //Populate card
            card.setBitmapUriFromFile(f);
            //card.setTitle(f.getName());
            card.setSecondaryTitle(new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(
                    new Date(f.lastModified())));

            card.finalize();

            //Add it to the list
            mCards.add(card);

            /*
            //Create a Card
            Card card = new Card(getActivity());

            //Create a CardHeader
            CardHeader header = new CardHeader(getActivity());

            //Set the header title
            header.setTitle(f.getName());

            //Add header to a card
            card.addCardHeader(header);

            //Create thumbnail
            CardThumbnail thumb = new CardThumbnail(getActivity());

            //Set ID resource
            thumb.setUrlResource(f.toURI().toString());

            //Add thumbnail to a card
            card.addCardThumbnail(thumb);

            //Set card in the cardView
            CardView cardView = (CardView) getActivity().findViewById(R.id.list_cardId);
            cardView.setCard(card);

            //Add it to the list
            mCards.add(card);

            return;
            */
        }

        mCardArrayAdapter = new CardArrayAdapter(getActivity(),mCards);

        mCardListView = (CardListView) getActivity().findViewById(R.id.frontCardList);
        if (mCardListView!=null){
            mCardListView.setAdapter(mCardArrayAdapter);
        }
    }
}
