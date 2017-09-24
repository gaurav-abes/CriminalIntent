package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Gaurav on 21/09/17.
 */

public class CrimeFragment extends Fragment {

    private static final String TAG = "CrimeFragment";
    private static final String ARG_CRIME_ID = "crime_id_bundle_arg";
    private static final String DATE_PICKER_DIALOG_TAG = "date_picker_dialog_tag";
    private static final int REQUEST_CODE_DATE = 0;
    private static final int REQUEST_CODE_CONTACT = 1;
    private static final int REQUEST_CODE_PHOTO = 2;

    private Crime mCrime;
    private File mPhotoFile;

    private EditText mTitleEditText;
    private Button mDateButton;
    private Button mChooseSuspectButton;
    private Button mSendReportButton;
    private CheckBox mSolvedCheckBox;
    private ImageView mCrimeImageView;
    private ImageButton mCrimeCameraButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleEditText = (EditText) view.findViewById(R.id.crime_title_edittext);
        mTitleEditText.setText(mCrime.getTitle());
        mTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) view.findViewById(R.id.crime_date_button);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_CODE_DATE);
                dialog.show(getFragmentManager(), DATE_PICKER_DIALOG_TAG);
            }
        });
        mSolvedCheckBox = (CheckBox) view.findViewById(R.id.crime_solved_checkbox);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mChooseSuspectButton = (Button) view.findViewById(R.id.choose_suspect_buttom);
        if (mCrime.getSuspect() != null)
            mChooseSuspectButton.setText(mCrime.getSuspect());
        final Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mChooseSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContactIntent, REQUEST_CODE_CONTACT);
            }
        });
        mSendReportButton = (Button) view.findViewById(R.id.send_crime_report_button);
        mSendReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY) == null)
            mChooseSuspectButton.setEnabled(false);

        mCrimeImageView = (ImageView) view.findViewById(R.id.crime_photo);
        mCrimeCameraButton = (ImageButton) view.findViewById(R.id.crime_camera_button);

        final Intent imageCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && imageCaptureIntent.resolveActivity(packageManager) != null;
        mCrimeCameraButton.setEnabled(canTakePhoto);
        mCrimeCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(), "com.bignerdranch.android.criminalintent.fileprovider", mPhotoFile);
                imageCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                List<ResolveInfo> cameraActivities = getActivity().getPackageManager().queryIntentActivities(imageCaptureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.toString(), uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(imageCaptureIntent, REQUEST_CODE_PHOTO);
            }
        });

        updatePhotoView();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK)
            return;
        if (requestCode == REQUEST_CODE_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CODE_CONTACT && data != null) {
            Uri contractUri = data.getData();
            String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = getActivity().getContentResolver().query(contractUri, queryFields, null, null, null);
            try {
                if (cursor.getCount() == 0)
                    return;
                cursor.moveToFirst();
                String suspect = cursor.getString(0);
                mCrime.setSuspect(suspect);
                mChooseSuspectButton.setText(suspect);
            } finally {
                cursor.close();
            }
        } else if (requestCode == REQUEST_CODE_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(), "com.bignerdranch.android.criminalintent.fileprovider", mPhotoFile);
            getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView();
        }

    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mCrimeImageView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), getActivity());
            mCrimeImageView.setImageBitmap(bitmap);
        }
    }
}
