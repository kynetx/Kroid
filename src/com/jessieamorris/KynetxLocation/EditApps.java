package com.jessieamorris.KynetxLocation;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class EditApps extends Activity {
    private EditText mAppidText;
    private Spinner mVersion;
    private Long mRowId;
    private KynetxSQLHelper mDbHelper;
	protected ArrayAdapter<CharSequence> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new KynetxSQLHelper(this);
        mDbHelper.open();

        setContentView(R.layout.app_edit);
        setTitle(R.string.edit_app);

        mAppidText = (EditText) findViewById(R.id.appid);
        
        
		mVersion = (Spinner) findViewById(R.id.Spinner);
		adapter = ArrayAdapter.createFromResource(this, R.array.Versions, android.R.layout.simple_spinner_dropdown_item);
		
		mVersion.setAdapter(adapter);
        Button confirmButton = (Button) findViewById(R.id.save);

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(KynetxSQLHelper.KEY_ROWID);
		if (mRowId == null) {
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(KynetxSQLHelper.KEY_ROWID)
									: null;
		}

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	storeData();
                setResult(RESULT_OK);
                finish();
            }

        });
        
        Button deleteButton = (Button) findViewById(R.id.delete);
        
        deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mRowId != null){
					mDbHelper.deleteApp(mRowId);
				}
				setResult(RESULT_OK);
				finish();
			}
		});
        
        Button cancelButton = (Button) findViewById(R.id.cancel);
        
        cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
    }

    private void storeData(){
    	if(mRowId != null){
    		mDbHelper.updateApp(mRowId, mAppidText.getText().toString(), (String)mVersion.getSelectedItem());
    	} else {
    		mDbHelper.createApp(mAppidText.getText().toString(), (String)mVersion.getSelectedItem());
    	}
    }
    
    private void populateFields() {
        if (mRowId != null) {
            Cursor app = mDbHelper.fetchApp(mRowId);
            startManagingCursor(app);
            mAppidText.setText(app.getString(
                    app.getColumnIndexOrThrow(KynetxSQLHelper.KEY_APPID)));
            String version = app.getString(app.getColumnIndexOrThrow(KynetxSQLHelper.KEY_VERSION));
            
            
            for(int i = 0; i < adapter.getCount(); i++){
            	if(version == adapter.getItem(i)){
            		mVersion.setSelection(i);
            	}
            }
        }
    }
}