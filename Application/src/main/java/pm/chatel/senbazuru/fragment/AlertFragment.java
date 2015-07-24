package pm.chatel.senbazuru.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import pm.chatel.senbazuru.R;

/**
 * Created by pierre on 03/07/15.
 */
@SuppressLint("ValidFragment")
public class AlertFragment extends DialogFragment {

    private int mainMessageID = R.string.mark_all_as_done;
    private int positiveID = R.string.mark_all_as_done_yes;
    private int negativeID = R.string.mark_all_as_done_no;
    private AlertDialogListener mListener;  // Use this instance of the interface to deliver action events

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface AlertDialogListener {
        public void onAlertPositiveClick(AlertFragment alert);
        public void onAlertNegativeClick(AlertFragment alert);
    }

    public AlertFragment(int mainMessageID) {
        this.mainMessageID = mainMessageID;
    }

    public AlertFragment(int mainMessageID, int positiveID, int negativeID) {
        this(mainMessageID);
        this.positiveID = positiveID;
        this.negativeID = negativeID;
    }

    public int getMainMessageID() {
        return mainMessageID;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mainMessageID)
                .setPositiveButton(positiveID, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the positive button event back to the host activity
                        mListener.onAlertPositiveClick(AlertFragment.this);
                    }
                })
                .setNegativeButton(negativeID, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onAlertNegativeClick(AlertFragment.this);
                    }
                });
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the AlertDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the AlertDialogListener so we can send events to the host
            mListener = (AlertDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement AlertDialogListener");
        }
    }

}
