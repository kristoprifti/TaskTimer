package me.kristoprifti.android.tasktimer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import static android.content.ContentValues.TAG;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddEditActivityFragment extends Fragment {

    public enum FragmentEditMode { EDIT, ADD }
    private FragmentEditMode mMode;

    private EditText mNameTextView;
    private EditText mDescriptionTextView;
    private EditText mSortOrderTextView;
    private Button mSaveButton;
    private OnSaveClicked mSaveListener = null;

    interface OnSaveClicked {
        void onSaveClicked();
    }

    public AddEditActivityFragment() {
        Log.d(TAG, "AddEditActivityFragment: constructor called");
    }

    public boolean canClose(){
        return false;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach: starts");
        super.onAttach(context);

        //activities containing this fragment must implement its callbacks.
        Activity activity = getActivity();
        if(!(activity instanceof OnSaveClicked)){
            throw new ClassCastException(activity.getClass().getSimpleName() +
                    " must implement AddEditActivityFragment.OnSaveClicked interface");
        }
        mSaveListener = (OnSaveClicked) getActivity();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: starts");
        super.onDetach();
        mSaveListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: starts");
        View view = inflater.inflate(R.layout.fragment_add_edit, container, false);

        mNameTextView = (EditText) view.findViewById(R.id.addedit_name);
        mDescriptionTextView = (EditText) view.findViewById(R.id.addedit_description);
        mSortOrderTextView = (EditText) view.findViewById(R.id.addedit_sortorder);
        mSaveButton = (Button) view.findViewById(R.id.addedit_save);

        Bundle arguments = getArguments();

        final Task task;
        if(arguments != null){
            Log.d(TAG, "onCreateView: retrieving task details");

            task = (Task) arguments.getSerializable(Task.class.getSimpleName());
            if(task != null){
                Log.d(TAG, "onCreateView: task details found, editing...");
                mNameTextView.setText(task.getName());
                mDescriptionTextView.setText(task.getDescription());
                mSortOrderTextView.setText(Integer.toString(task.getSortOrder()));
                mMode = FragmentEditMode.EDIT;
            } else {
                // no task, so we must be adding a new task not editing an existing one
                mMode = FragmentEditMode.ADD;
            }
        } else {
            task = null;
            Log.d(TAG, "onCreateView: no arguments");
            mMode = FragmentEditMode.ADD;
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //update the database if at least one field has changed
                //there is not need to hit the database unless this happens
                int so;
                if(mSortOrderTextView.length() > 0){
                    so = Integer.parseInt(mSortOrderTextView.getText().toString());
                } else {
                    so = 0;
                }

                ContentResolver contentResolver = getActivity().getContentResolver();
                ContentValues contentValues = new ContentValues();

                switch (mMode) {
                    case EDIT:
                        if (task != null && !mNameTextView.getText().toString().equals(task.getName())) {
                            contentValues.put(TasksContract.Columns.TASKS_NAME, mNameTextView.getText().toString());
                        }
                        if (task != null && !mDescriptionTextView.getText().toString().equals(task.getDescription())) {
                            contentValues.put(TasksContract.Columns.TASKS_DESCRIPTION, mDescriptionTextView.getText().toString());
                        }
                        if (task != null && so != task.getSortOrder()) {
                            contentValues.put(TasksContract.Columns.TASKS_SORTORDER, so);
                        }
                        if(contentValues.size() != 0){
                            Log.d(TAG, "onClick: updating task");
                            if (task != null) {
                                contentResolver.update(TasksContract.buildTaskUri(task.getId()), contentValues, null, null);
                            }
                        }
                        break;
                    case ADD:
                        if(mNameTextView.length() > 0){
                            Log.d(TAG, "onClick: adding new task");
                            contentValues.put(TasksContract.Columns.TASKS_NAME, mNameTextView.getText().toString());
                            contentValues.put(TasksContract.Columns.TASKS_DESCRIPTION, mDescriptionTextView.getText().toString());
                            contentValues.put(TasksContract.Columns.TASKS_SORTORDER, so);
                            contentResolver.insert(TasksContract.CONTENT_URI, contentValues);
                        }
                        break;
                }
                Log.d(TAG, "onClick: done editing");

                if(mSaveListener != null){
                    mSaveListener.onSaveClicked();
                }
            }
        });
        Log.d(TAG, "onCreateView: exiting");

        return view;
    }
}
