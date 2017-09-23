package com.bignerdranch.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.List;
import java.util.UUID;

/**
 * Created by Gaurav on 23/09/17.
 */

public class CrimePagerActivity extends AppCompatActivity {

    private static final String KEY_CRIME_ID = "com.bignerdranch.android.criminalintent.key_crime_id";

    private ViewPager mCrimeViewPager;
    private Button mFirstPageButton;
    private Button mLastPageButton;

    private List<Crime> mCrimes;
    private UUID mCrimeId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_pager);

        mCrimeId = (UUID) getIntent().getSerializableExtra(KEY_CRIME_ID);

        mCrimes = CrimeLab.get(this).getCrimes();
        mCrimeViewPager = (ViewPager) findViewById(R.id.crime_view_pager);
        mCrimeViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                Crime crime = mCrimes.get(position);
                return CrimeFragment.newInstance(crime.getId());
            }

            @Override
            public int getCount() {
                return mCrimes.size();
            }
        });

        mFirstPageButton = (Button) findViewById(R.id.first_page);
        mLastPageButton = (Button) findViewById(R.id.last_page);

        setUpTheCorrectFragmentAccordingToId();

        mFirstPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCrimeViewPager.setCurrentItem(0);
            }
        });

        mLastPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCrimeViewPager.setCurrentItem(mCrimes.size() - 1);
            }
        });

    }

    private void setUpTheCorrectFragmentAccordingToId() {
        for (int i = 0; i < mCrimes.size(); i++) {
            if (mCrimes.get(i).getId().equals(mCrimeId)) {
                mCrimeViewPager.setCurrentItem(i);
                if (i == 0)
                    mFirstPageButton.setEnabled(false);
                else if (i == mCrimes.size() - 1)
                    mLastPageButton.setEnabled(false);
                break;
            }
        }
    }

    public static Intent newIntent(Context context, UUID crimeId) {
        Intent i = new Intent(context, CrimePagerActivity.class);
        i.putExtra(KEY_CRIME_ID, crimeId);
        return i;
    }
}
