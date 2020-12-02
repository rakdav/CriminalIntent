package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import com.bignerdranch.android.criminalintent.Model.Crime;
import com.bignerdranch.android.criminalintent.Model.CrimeLab;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.widget.CompoundButton.*;

public class CrimeFragment extends Fragment {

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckbox;
    private Button reportButton;
    private Button suspectButton;
    private ImageButton photoButton;
    private ImageView photoView;
    private File photoFile;
    private static final String ARG_CRIME_ID="crime_id";
    private static final String DIALOG_DATE="Dialog_Date";
    private static final int REQUEST_DATE=0;
    private static final int REQUEST_CONTACT=1;
    private static final int REQUEST_FOTO=2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //mCrime = new Crime();
        UUID crimeId=(UUID)getArguments().getSerializable(ARG_CRIME_ID);
        mCrime= CrimeLab.get(getActivity()).getCrime(crimeId);
        photoFile=CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    public static CrimeFragment newInstance(UUID crimeId)
    {
        Bundle args=new Bundle();
        args.putSerializable(ARG_CRIME_ID,crimeId);
        CrimeFragment fragment=new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);
        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
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

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        mDateButton.setText(mCrime.getDate().toString());
        //mDateButton.setEnabled(false);
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager=getFragmentManager();
               // DatePickerFragment dialog=new DatePickerFragment();
                DatePickerFragment dialog=DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this,REQUEST_DATE);
                dialog.show(manager,DIALOG_DATE);
            }
        });
        mSolvedCheckbox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckbox.setChecked(mCrime.isSolved());
        mSolvedCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, 
                    boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });
        reportButton=(Button)v.findViewById(R.id.crime_report);
        reportButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT,getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.crime_report_subject));
                i=Intent.createChooser(i,getString(R.string.send_report));
                startActivity(i);
            }
        });
        final Intent pickContact=new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContact.addCategory(Intent.CATEGORY_HOME);
        suspectButton=(Button)v.findViewById(R.id.crime_suspect);
        suspectButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact,REQUEST_CONTACT);
            }
        });
        if(mCrime.getSuspect()!=null)
        {
            suspectButton.setText(mCrime.getSuspect());
        }
        PackageManager packageManager=getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,PackageManager.MATCH_DEFAULT_ONLY)==null)
        {
            suspectButton.setEnabled(false);
        }
        photoButton=(ImageButton)v.findViewById(R.id.crime_camera);
        photoView=(ImageView)v.findViewById(R.id.crime_foto);
        final Intent captureImage=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto=photoFile!=null&&captureImage.resolveActivity(packageManager)!=null;
        photoButton.setEnabled(canTakePhoto);
        photoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri= FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        photoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                List<ResolveInfo> cameraActivities=getActivity().getPackageManager().queryIntentActivities(captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY);
                for(ResolveInfo activity:cameraActivities){
                    getActivity().grantUriPermission(activity.activityInfo.packageName,uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage,REQUEST_FOTO);
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!= Activity.RESULT_OK) return;
        if(requestCode==REQUEST_DATE)
        {
            Date date=(Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            mDateButton.setText(mCrime.getDate().toString());

        }
        else if(requestCode==REQUEST_CONTACT&&data!=null)
        {
            Uri contactUri=data.getData();
            String[] queryFields=new String[]{ContactsContract.Contacts.DISPLAY_NAME};
            Cursor c=getActivity().getContentResolver().query(contactUri,queryFields,null,null,null);
            try
            {
                if(c.getCount()==0) return;;
                c.moveToFirst();
                String suspect=c.getString(0);
                mCrime.setSuspect(suspect);
                suspectButton.setText(suspect);
            }finally {
                c.close();
            }
        }
        else if(requestCode==REQUEST_FOTO){
            Uri uri=FileProvider.getUriForFile(getActivity(),"com.bignerdranch.android.criminalintent.fileprovider",photoFile);
            getActivity().revokeUriPermission(uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            updatePhotoView();
        }
    }

    private void updatePhotoView()
    {
        if(photoFile==null||!photoFile.exists()) {photoView.setImageDrawable(null);}
        else {
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inJustDecodeBounds=true;
            Bitmap bitmap= BitmapFactory.decodeFile(photoFile.getPath(),options);
            photoView.setImageBitmap(bitmap);
        }
    }
    private String getCrimeReport()
    {
        String solvedString=null;
        if(mCrime.isSolved())
        {
            solvedString=getString(R.string.crime_report_solved);
        }
        else {
            solvedString=getString(R.string.crime_report_unsolved);
        }
        StringBuffer dateFormat=new StringBuffer("EEE, MMM dd");
        String dateString= DateFormat.format(dateFormat,mCrime.getDate()).toString();
        String suspect=mCrime.getSuspect();
        if(suspect==null)
        {
            suspect=getString(R.string.crime_report_no_suspect);
        }
        else {
            suspect=getString(R.string.crime_report_suspect,suspect);
        }
        String report=getString(R.string.crime_report,mCrime.getTitle(),dateString,solvedString,suspect);
        return report;
    }
    @Override
    public void onStop() {
        super.onStop();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

}
