package com.example.mynotesapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class notesactivity extends AppCompatActivity {

    FloatingActionButton mcreatenotesfab;
    private FirebaseAuth firebaseAuth;

    RecyclerView mrecycleview;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    FirebaseUser firebaseUser;
    FirebaseFirestore firebaseFirestore;
    FirestoreRecyclerAdapter<firebasemodel,NoteViewHolder> noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        mcreatenotesfab = findViewById(R.id.createnotefab);
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseFirestore = firebaseFirestore.getInstance();

        //Toolbar toolbar=findViewById(R.id.toolbar);



       // getSupportActionBar().setTitle("All My Notes");
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle("My Notes");
        }

        mcreatenotesfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(notesactivity.this, createnote.class));

            }
        });

        Query query=firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").orderBy("title",Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<firebasemodel>allusernotes=new FirestoreRecyclerOptions.Builder<firebasemodel>().setQuery(query,firebasemodel.class).build();

        noteAdapter=new FirestoreRecyclerAdapter<firebasemodel, NoteViewHolder>(allusernotes) {
            @RequiresApi(api= Build.VERSION_CODES.M)
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewholder, int i, @NonNull firebasemodel firebasemodel) {


                ImageView popupbutton=noteViewholder.itemView.findViewById(R.id.menupopbutton);


                int colourcode=getRandomColor();
                noteViewholder.mnote.setBackgroundColor(noteViewholder.itemView.getResources().getColor(colourcode,null));

                noteViewholder.notetitle.setText(firebasemodel.getTitle());
                noteViewholder.notecontent.setText(firebasemodel.getContent());

                String docID=noteAdapter.getSnapshots().getSnapshot(i).getId();

                noteViewholder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //we have to open note details activity

                        Intent intent=new Intent(v.getContext(),notedetails.class);
                        intent.putExtra("title",firebasemodel.getTitle());
                        intent.putExtra("content",firebasemodel.getContent());
                        intent.putExtra("noteId",docID);

                        v.getContext().startActivity(intent);
                        //Toast.makeText(getApplicationContext(),"This is Clicked",Toast.LENGTH_SHORT).show();

                    }
                });

                popupbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                       PopupMenu popupMenu=new PopupMenu(v.getContext(),v);
                       popupMenu.setGravity(Gravity.END);
                       popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                           @Override
                           public boolean onMenuItemClick( MenuItem item) {

                               Intent intent=new Intent(v.getContext(),editnoteactivity.class);
                               intent.putExtra("title",firebasemodel.getTitle());
                               intent.putExtra("content",firebasemodel.getContent());
                               intent.putExtra("noteId",docID);
                               v.getContext().startActivity(intent);


                               return false;
                           }
                       });

                       popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                           @Override
                           public boolean onMenuItemClick( MenuItem item) {
                               //Toast.makeText(v.getContext(),"This note is deleted",Toast.LENGTH_SHORT).show();
                               DocumentReference documentReference=firebaseFirestore.collection("notes").document(firebaseUser.getUid()).collection("myNotes").document(docID);
                               documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void unused) {
                                       Toast.makeText(v.getContext(),"This note is deleted",Toast.LENGTH_SHORT).show();

                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {
                                       Toast.makeText(v.getContext(),"Failed to Delete",Toast.LENGTH_SHORT).show();
                                   }
                               });

                               return false;
                           }
                       });

                       popupMenu.show();
                    }
                });


            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };

        mrecycleview=findViewById(R.id.recyclerview);
        mrecycleview.setHasFixedSize(true);
        staggeredGridLayoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        mrecycleview.setLayoutManager(staggeredGridLayoutManager);
        mrecycleview.setAdapter(noteAdapter);


    }
    public class NoteViewHolder extends RecyclerView.ViewHolder
    {

        private TextView notetitle;
        private TextView notecontent;
        LinearLayout mnote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            notetitle=itemView.findViewById(R.id.notetitle);
            notecontent=itemView.findViewById(R.id.notecontent);
            mnote=itemView.findViewById(R.id.note);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {



        if (item.getItemId()==R.id.logout) {
            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(notesactivity.this, MainActivity.class));

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.startListening();
        if(noteAdapter!=null)
        {
            noteAdapter.stopListening();
        }


    }

    private int getRandomColor() {
        List<Integer> colorcode = new ArrayList<>();
        colorcode.add(R.color.bache);
        colorcode.add(R.color.Blue);
        colorcode.add(R.color.lighblue);
        colorcode.add(R.color.lime);
        colorcode.add(R.color.orange);
        colorcode.add(R.color.purpule);
        colorcode.add(R.color.aqua);
        colorcode.add(R.color.brown);
        colorcode.add(R.color.Darkred);
        colorcode.add(R.color.green);
        colorcode.add(R.color.Pink);
        colorcode.add(R.color.red);
        colorcode.add(R.color.yellow);

        Random random = new Random();
        int number = random.nextInt(colorcode.size());
        return colorcode.get(number);


    }

}