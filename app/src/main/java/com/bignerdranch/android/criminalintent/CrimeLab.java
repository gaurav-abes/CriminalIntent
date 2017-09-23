package com.bignerdranch.android.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Gaurav on 22/09/17.
 */

public class CrimeLab {
    private static CrimeLab sCrimeLab;

    private Map<UUID, Crime> mCrimes;

    private CrimeLab() {
        mCrimes = new LinkedHashMap<>();
    }

    public static CrimeLab get(Context context) {
        if (sCrimeLab == null)
            sCrimeLab = new CrimeLab();
        return sCrimeLab;
    }

    public List<Crime> getCrimes() {
        return new ArrayList<>(mCrimes.values());
    }

    public Crime getCrime(UUID id) {
        return mCrimes.get(id);
    }

    public void addCrime(Crime crime){
        mCrimes.put(crime.getId(),crime);
    }
}
