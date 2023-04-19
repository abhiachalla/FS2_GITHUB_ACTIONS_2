package edu.northeasternn.studycircle;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.northeasternn.studycircle.groupArtifacts.GroupCard;
import edu.northeasternn.studycircle.groupArtifacts.GroupCardAdapter;
import edu.northeasternn.studycircle.model.Group;
import edu.northeasternn.studycircle.model.UserGroups;

public class HomeFragment extends Fragment {

    private FloatingActionButton addGroupButton;
    private TextView recyclerEmptyTextView;
    private EditText title, description, subject, location;
    private Button newGroup_cancel, newGroup_Add;
    private AlertDialog.Builder newGroupDialog;
    private AlertDialog alertDialog;
    private EditText startTime,endTime;


    private LocalTime meetStartTime,meetEndTime;
    private CheckBox monday, tuesday, wednesday, thursday, friday, saturday, sunday;

    private ProgressBar progressBar;
    private FirebaseFirestore db;

    private FirebaseUser currentUser;

    private RecyclerView groupCardsRecyclerView;

    List<GroupCard> groupCardList = new ArrayList<>();

    private  View view;


    public HomeFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        currentUser =  FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home,
                container, false);






        addGroupButton = view.findViewById(R.id.addGroup);
        recyclerEmptyTextView = view.findViewById(R.id.homePageRecyclerViewEmptyText);
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

        processUserGroupData();
        return view;
    }


    public void enablePopUp(View view) {
        // Fetching all the required information for group creation
        newGroupDialog = new AlertDialog.Builder(view.getContext());
        final View popUp = getLayoutInflater().inflate(R.layout.create_group_details, null);
        title = (EditText) popUp.findViewById(R.id.title);
        subject = (EditText) popUp.findViewById(R.id.subject);
        description = (EditText) popUp.findViewById(R.id.description);
        location = (EditText) popUp.findViewById(R.id.location);

        startTime = (EditText) popUp.findViewById(R.id.startTimeValue);
        endTime = (EditText) popUp.findViewById(R.id.endTimeValue);
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


        newGroup_Add = (Button) popUp.findViewById(R.id.saveButton);
        newGroup_cancel = (Button) popUp.findViewById(R.id.cancelButton);

        monday = (CheckBox) popUp.findViewById(R.id.monday);
        tuesday = (CheckBox) popUp.findViewById(R.id.tuesday);
        wednesday = (CheckBox) popUp.findViewById(R.id.wednesday);
        thursday = (CheckBox) popUp.findViewById(R.id.thursday);
        friday = (CheckBox) popUp.findViewById(R.id.friday);
        saturday = (CheckBox) popUp.findViewById(R.id.saturday);
        sunday = (CheckBox) popUp.findViewById(R.id.sunday);

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
                        endTime.getText().toString(),
                        currentUser.getUid()
                );

                dbStudyGroup.add(group).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        progressBar.setVisibility(View.INVISIBLE);
                        addGroupToUserGroups(documentReference.getId());
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

    private void addGroupToUserGroups(String groupId) {
        DocumentReference docRef = db.collection("userGroups").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserGroups userGroups = documentSnapshot.toObject(UserGroups.class);
                UserGroups updateUserGroups = new UserGroups(userGroups);
                updateUserGroups.getGroups().add(groupId);
                DocumentReference docRef = db.collection("userGroups").document(currentUser.getUid());
                docRef.set(updateUserGroups).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("User Group Updated", "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("User Group Update", "Error writing document", e);
                            }
                        });;

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


    void processUserGroupData(){
        CollectionReference userAndGroups = db.collection("userGroups");
        Query query;
        FirebaseUser user =  FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        query = userAndGroups.whereEqualTo("user", userId);
        userAndGroups.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult().isEmpty()){
                                addUserToUserGroups(userId,userAndGroups);
                            }
                            else {
                                groupCardList = new ArrayList<>();
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    UserGroups userGroup = documentSnapshot.toObject(UserGroups.class);
                                    processCollectionData(userGroup);
                                }
                            }
                        }
                    }
                });
            }
        });


    }


    void addUserToUserGroups(String userId, CollectionReference reference){
        UserGroups userGroups = new UserGroups(userId, new ArrayList<>());
        reference.document(userId).set(userGroups).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("New User", "Empty List of Groups created successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("New user", "Empty List Creation Failed!");
                    }
                });
    }





    void processCollectionData(UserGroups userGroup){
        //Obtain all the groupIds here
        CollectionReference collectionReference = db.collection("Groups");
        Set<String> groupIds = new HashSet<>(userGroup.getGroups());
        collectionReference.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Map<String, Group> groups =
                        new HashMap<>();
                for(QueryDocumentSnapshot document : task.getResult()){
                    if(groupIds!= null && groupIds.contains(document.getId())) {
                        groups.put(document.getId(), document.toObject(Group.class));
                    }
                }
                addGroupToCards(groups);
            }
        });
    }


    /**
     * Use the fetched groups to form the card here
     */
    void addGroupToCards(Map<String,Group> groups){
        //Go through a map that relates the two.
        for(String groupId : groups.keySet()){
            Group group = groups.get(groupId);
            GroupCard groupCard = new GroupCard(group.getTitle(),group.getSubject(),
                    group.getLocation(), groupId);
            groupCardList.add(groupCard);
        }
        createRecyclerView();
    }

    /**
     * A method to create a recycler view.
     */
    void createRecyclerView(){
        // If the group size is zero show a text that shows that the user hasn't joined any group
        if(groupCardList.size() == 0){
            recyclerEmptyTextView.setText("No groups joined!");
            recyclerEmptyTextView.setVisibility(View.VISIBLE);
            return;
        }
        recyclerEmptyTextView.setVisibility(View.INVISIBLE);
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        GridLayoutManager layoutManager=new GridLayoutManager(getActivity(),2);
        groupCardsRecyclerView = view.findViewById(R.id.homePageRecyclerView);
        groupCardsRecyclerView.setHasFixedSize(true);
        //Pass groupcards to the adapter
        GroupCardAdapter recyclerViewAdapter = new GroupCardAdapter(groupCardList);


        //TODO : Fetch groupId so that user can join them when one accesses it

        groupCardsRecyclerView.setAdapter(recyclerViewAdapter);
        groupCardsRecyclerView.setLayoutManager(layoutManager);
    }
}