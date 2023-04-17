package edu.northeasternn.studycircle;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.northeasternn.studycircle.model.Group;

public class HomeFragment extends Fragment {

    private FloatingActionButton addGroupButton;
    private TextView recyclerTextView;
    private EditText title, description, subject, location;
    private Button newGroup_cancel, newGroup_Add;
    private AlertDialog.Builder newGroupDialog;
    private AlertDialog alertDialog;
    private EditText startTime,endTime;


    private LocalTime meetStartTime,meetEndTime;
    private CheckBox monday, tuesday, wednesday, thursday, friday, saturday, sunday;

    private ProgressBar progressBar;
    private FirebaseFirestore db;

    private  View view;


    public HomeFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home,
                container, false);






        addGroupButton = view.findViewById(R.id.addGroup);
        addGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(FirebaseAuth.getInstance().getCurrentUser() == null){
                    Snackbar.make(HomeFragment.this.view, "You are not a registered user. Please register first", Snackbar.LENGTH_LONG).show();
                }
                else {
                    enablePopUp(view);
                }
            }
        });


        return view;
    }


    public void enablePopUp(View view) {
        // Fetching all the required information for group creation
        newGroupDialog = new AlertDialog.Builder(view.getContext());
        final View popUp = getLayoutInflater().inflate(R.layout.create_group_details, null);
        title = popUp.findViewById(R.id.title);
        subject = popUp.findViewById(R.id.subject);
        description = popUp.findViewById(R.id.description);
        location = popUp.findViewById(R.id.location);

        startTime = popUp.findViewById(R.id.startTimeValue);
        endTime = popUp.findViewById(R.id.endTimeValue);
        startTime.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME);
        endTime.setInputType(InputType.TYPE_DATETIME_VARIATION_TIME);
        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                TimePickerDialog picker = new TimePickerDialog(view.getContext(),
                        (tp, sHour, sMinute) -> startTime.setText(sHour + ":" + sMinute), hour, minutes, true);
                picker.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);
                // time picker dialog
                TimePickerDialog picker = new TimePickerDialog(view.getContext(),
                        (tp, sHour, sMinute) -> endTime.setText(sHour + ":" + sMinute), hour, minutes, true);
                picker.show();
            }
        });


        newGroup_Add = popUp.findViewById(R.id.saveButton);
        newGroup_cancel = popUp.findViewById(R.id.cancelButton);

        monday =  popUp.findViewById(R.id.monday);
        tuesday =  popUp.findViewById(R.id.tuesday);
        wednesday =  popUp.findViewById(R.id.wednesday);
        thursday =  popUp.findViewById(R.id.thursday);
        friday =  popUp.findViewById(R.id.friday);
        saturday =  popUp.findViewById(R.id.saturday);
        sunday =  popUp.findViewById(R.id.sunday);

        progressBar = popUp.findViewById(R.id.groupCreationProgressBar);


        newGroupDialog.setView(popUp);
        alertDialog = newGroupDialog.create();
        alertDialog.show();
        alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // SAVE button listener
        newGroup_Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<DayOfWeek> weekDays = collectUserSelectedDays();
                // Validity of each field is checked
                if (title.getText().length() == 0) {
                    Snackbar.make(view, "Group Title cannot be empty", Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if (subject.getText().length() == 0) {
                    Snackbar.make(view, "Group Subject cannot be empty", Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if (location.getText().length() == 0){
                    Snackbar.make(view, "Group Zip Code cannot be empty", Snackbar.LENGTH_LONG).show();
                    return;
                }

                try {
                    Integer.valueOf(location.getText().toString());
                    if (location.getText().length() != 5) {
                        Snackbar.make(view, "Invalid Zip Code. Maximum 5 characters allowed", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }
                catch (Exception e) {
                    Snackbar.make(view, "Invalid Zip Code. Please Enter Correct Zip Code", Snackbar.LENGTH_LONG).show();
                    return;
                }


                if (description.getText().length() == 0) {
                    Snackbar.make(view, "Group Subject cannot be empty", Snackbar.LENGTH_LONG).show();
                    return;
                }
                else if (weekDays.size() == 0) {
                    Snackbar.make(view, "Please select day(s) of the week to meet", Snackbar.LENGTH_LONG).show();
                    return;
                }

                try {

                    meetStartTime = LocalTime.parse(startTime.getText().toString());
                    meetEndTime = LocalTime.parse(endTime.getText().toString());

                    if (!(meetStartTime.compareTo(meetEndTime) < 0)) {
                        Snackbar.make(view, "End Time Should be after Start Time", Snackbar.LENGTH_LONG).show();
                        return;
                    }
                }
                catch (Exception e) {
                    Snackbar.make(view, "Invalid TImings", Snackbar.LENGTH_LONG).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                CollectionReference dbStudyGroup = db.collection("Groups");

                Group group = new Group(
                        title.getText().toString(),
                        subject.getText().toString(),
                        location.getText().toString(),
                        description.getText().toString(),
                        weekDays,
                        startTime.getText().toString(),
                        endTime.getText().toString()
                );

                dbStudyGroup.add(group).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Your Study group has been created", Toast.LENGTH_LONG).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(), "Some error occurred! Try again", Toast.LENGTH_LONG).show();
                    }
                });
                alertDialog.dismiss();
            }
        });

        newGroup_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // close pop up
                alertDialog.dismiss();
            }
        });
    }

    private List collectUserSelectedDays(){
        List<DayOfWeek> weekDays = new ArrayList<>();
        if (monday.isChecked()) {
            weekDays.add(DayOfWeek.MONDAY);
        }

        if (tuesday.isChecked()) {
            weekDays.add(DayOfWeek.TUESDAY);
        }

        if (wednesday.isChecked()) {
            weekDays.add(DayOfWeek.WEDNESDAY);
        }

        if (thursday.isChecked()) {
            weekDays.add(DayOfWeek.THURSDAY);
        }

        if (friday.isChecked()) {
            weekDays.add(DayOfWeek.FRIDAY);
        }

        if (saturday.isChecked()) {
            weekDays.add(DayOfWeek.SATURDAY);
        }

        if (sunday.isChecked()) {
            weekDays.add(DayOfWeek.SUNDAY);
        }
        return weekDays;
    }
}