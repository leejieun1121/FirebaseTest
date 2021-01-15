package com.example.firebaseauthtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private List<ImageDTO> list = new ArrayList<>();
    private List<String> uidLists = new ArrayList<>();
    private FirebaseDatabase firebaseDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        recyclerView = findViewById(R.id.rv_board);
        BoardRecyclerViewAdapter boardRecyclerViewAdapter = new BoardRecyclerViewAdapter();
        recyclerView.setAdapter(boardRecyclerViewAdapter);
        firebaseDatabase = FirebaseDatabase.getInstance();

        firebaseDatabase.getReference().child("images").addValueEventListener(new ValueEventListener() {
            //옵저버패턴 Pattern
            //데이터가 바뀔때마다 넘어옴 -> 자동 새로고침
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                //images의 자식 노드들을 순회함 !
                for(DataSnapshot dataSnapshot :snapshot.getChildren()){
                    //순회한 데이터를 ImageDTO 클래스 형으로 받아옴 !
                    ImageDTO imageDTO = dataSnapshot.getValue(ImageDTO.class);
                    Log.d("tag",dataSnapshot.getValue().toString());
                    Log.d("tag",imageDTO.description);
                    Log.d("tag",imageDTO.imageUrl);
                    list.add(imageDTO);

                }
                //데이터가 변경되었으니 꼭 해주기 
                boardRecyclerViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    class BoardRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((CustomViewHolder)holder).tvTitle.setText(list.get(position).title);
            ((CustomViewHolder)holder).tvDescription.setText(list.get(position).description);
            Glide.with(holder.itemView).load(list.get(position).imageUrl)
                    .into(((CustomViewHolder)holder).imgContent);

        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imgContent;
        TextView tvTitle;
        TextView tvDescription;

        public CustomViewHolder(View view) {
            super(view);
            imgContent = view.findViewById(R.id.img_content);
            tvTitle = view.findViewById(R.id.tv_title);
            tvDescription = view.findViewById(R.id.tv_description);
        }
    }
}